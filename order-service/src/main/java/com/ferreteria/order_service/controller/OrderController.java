package com.ferreteria.order_service.controller;

import com.ferreteria.order_service.dto.OrderRequestDTO;
import com.ferreteria.order_service.dto.OrderResponseDTO;
import com.ferreteria.order_service.dto.PageResponseDTO;
import com.ferreteria.order_service.model.Order;
import com.ferreteria.order_service.model.Payment;
import com.ferreteria.order_service.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
@Tag(name = "Órdenes", description = "Gestión de órdenes, despachos y pagos")
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    @Operation(summary = "Crear nueva orden")
    public ResponseEntity<OrderResponseDTO> crear(
            @Valid @RequestBody OrderRequestDTO request,
            @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey,
            @AuthenticationPrincipal Jwt jwt,
            HttpServletRequest httpRequest) {
        UUID userId = getUserIdFromToken(jwt);
        OrderResponseDTO response = orderService.crear(userId, request, idempotencyKey);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener orden por ID")
    public ResponseEntity<OrderResponseDTO> obtenerPorId(
            @Parameter(description = "ID de la orden") @PathVariable UUID id,
            @AuthenticationPrincipal Jwt jwt) {
        UUID userId = getUserIdFromToken(jwt);
        return ResponseEntity.ok(orderService.obtenerPorId(id, userId, esAdmin(jwt)));
    }

    @GetMapping
    @Operation(summary = "Listar órdenes: propias para clientes, todas para ADMIN/VENDEDOR")
    public ResponseEntity<PageResponseDTO<OrderResponseDTO>> listarMisOrdenes(
            @Parameter(description = "Número de página (0-based)") @RequestParam(defaultValue = "0") @Min(0) int page,
            @Parameter(description = "Tamaño de página") @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size,
            @Parameter(description = "Campo para ordenar") @RequestParam(defaultValue = "createdAt") String sortBy,
            @Parameter(description = "Dirección de orden (asc/desc)") @RequestParam(defaultValue = "desc") String sortDir,
            @Parameter(description = "Filtrar por estado") @RequestParam(required = false) String status,
            @AuthenticationPrincipal Jwt jwt) {
        // El panel admin (también accesible para vendedores) alimenta esta misma ruta.
        if (esAdmin(jwt) || esVendedor(jwt)) {
            return ResponseEntity.ok(orderService.listarTodas(page, size, sortBy, sortDir, status));
        }
        UUID userId = getUserIdFromToken(jwt);
        return ResponseEntity.ok(orderService.listarPorUsuario(userId, page, size, sortBy, sortDir, status));
    }

    @GetMapping("/admin")
    @Operation(summary = "Listar todas las órdenes (solo ADMIN)")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PageResponseDTO<OrderResponseDTO>> listarTodas(
            @Parameter(description = "Número de página (0-based)") @RequestParam(defaultValue = "0") @Min(0) int page,
            @Parameter(description = "Tamaño de página") @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size,
            @Parameter(description = "Campo para ordenar") @RequestParam(defaultValue = "createdAt") String sortBy,
            @Parameter(description = "Dirección de orden (asc/desc)") @RequestParam(defaultValue = "desc") String sortDir,
            @Parameter(description = "Filtrar por estado") @RequestParam(required = false) String status) {
        return ResponseEntity.ok(orderService.listarTodas(page, size, sortBy, sortDir, status));
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "Actualizar estado de la orden")
    @PreAuthorize("hasAnyRole('ADMIN', 'VENDEDOR')")
    public ResponseEntity<OrderResponseDTO> actualizarEstado(
            @Parameter(description = "ID de la orden") @PathVariable UUID id,
            @Parameter(description = "Nuevo estado") @RequestParam Order.OrderStatus status) {
        return ResponseEntity.ok(orderService.actualizarEstado(id, status));
    }

    @PostMapping("/{id}/payments")
    @Operation(summary = "Registrar pago de una orden")
    @PreAuthorize("hasAnyRole('ADMIN', 'VENDEDOR')")
    public ResponseEntity<OrderResponseDTO> registrarPago(
            @Parameter(description = "ID de la orden") @PathVariable UUID id,
            @Parameter(description = "Método de pago") @RequestParam Payment.PaymentMethod method,
            @Parameter(description = "Monto") @RequestParam BigDecimal amount,
            @Parameter(description = "ID de transacción del gateway") @RequestParam String transactionId) {
        return ResponseEntity.ok(orderService.agregarPago(id, method, amount, transactionId));
    }

    @PostMapping("/{id}/cancel")
    @Operation(summary = "Cancelar una orden")
    public ResponseEntity<Void> cancelar(
            @Parameter(description = "ID de la orden") @PathVariable UUID id,
            @Parameter(description = "Motivo de cancelación") @RequestParam String motivo,
            @AuthenticationPrincipal Jwt jwt) {
        UUID userId = getUserIdFromToken(jwt);
        orderService.cancelar(id, motivo, userId, esAdmin(jwt));
        return ResponseEntity.noContent().build();
    }

    // auth-service identifica al usuario con un id numérico en "sub", pero orders.user_id es UUID.
    // Se deriva un UUID estable del subject para no romper el esquema ni las consultas por usuario.
    private UUID getUserIdFromToken(Jwt jwt) {
        String sub = jwt.getClaimAsString("sub");
        if (sub == null || sub.isBlank()) {
            throw new com.ferreteria.order_service.exception.BadRequestException("El token no contiene el usuario");
        }
        try {
            return UUID.fromString(sub);
        } catch (IllegalArgumentException ex) {
            return UUID.nameUUIDFromBytes(("ferreteria-user:" + sub).getBytes(StandardCharsets.UTF_8));
        }
    }

    private boolean esAdmin(Jwt jwt) {
        var roles = jwt.getClaimAsStringList("roles");
        return roles != null && roles.contains("ADMIN");
    }

    private boolean esVendedor(Jwt jwt) {
        var roles = jwt.getClaimAsStringList("roles");
        return roles != null && roles.contains("VENDEDOR");
    }
}