package com.ferreteria.order_service.controller;

import com.ferreteria.order_service.assemblers.OrderModelAssembler;
import com.ferreteria.order_service.dto.OrderResponseDTO;
import com.ferreteria.order_service.dto.PageResponseDTO;
import com.ferreteria.order_service.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.MediaTypes;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
@RequestMapping("/orders/v2")
@RequiredArgsConstructor
@Tag(name = "Órdenes v2", description = "Consulta HAL de órdenes, propias para clientes, todas para ADMIN/VENDEDOR")
public class OrderControllerV2 {

    private final OrderService orderService;
    private final OrderModelAssembler assembler;

    @GetMapping(produces = MediaTypes.HAL_JSON_VALUE)
    @Operation(summary = "Listar órdenes: propias para clientes, todas para ADMIN/VENDEDOR")
    public CollectionModel<EntityModel<OrderResponseDTO>> listar(
            @Parameter(description = "Número de página (0-based)") @RequestParam(defaultValue = "0") @Min(0) int page,
            @Parameter(description = "Tamaño de página") @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size,
            @Parameter(description = "Campo para ordenar") @RequestParam(defaultValue = "createdAt") String sortBy,
            @Parameter(description = "Dirección de orden (asc/desc)") @RequestParam(defaultValue = "desc") String sortDir,
            @Parameter(description = "Filtrar por estado") @RequestParam(required = false) String status,
            @AuthenticationPrincipal Jwt jwt) {
        // El panel admin (también accesible para vendedores) alimenta esta misma ruta.
        PageResponseDTO<OrderResponseDTO> ordenes;
        if (esAdmin(jwt) || esVendedor(jwt)) {
            ordenes = orderService.listarTodas(page, size, sortBy, sortDir, status);
        } else {
            ordenes = orderService.listarPorUsuario(getUserIdFromToken(jwt), page, size, sortBy, sortDir, status);
        }
        List<EntityModel<OrderResponseDTO>> content = ordenes.getContent().stream()
                .map(assembler::toModel)
                .toList();
        return CollectionModel.of(
                content,
                linkTo(methodOn(OrderControllerV2.class).listar(0, 20, "createdAt", "desc", null, null)).withSelfRel()
        );
    }

    @GetMapping(value = "/{id}", produces = MediaTypes.HAL_JSON_VALUE)
    @Operation(summary = "Obtener orden por ID")
    public EntityModel<OrderResponseDTO> buscarPorId(
            @Parameter(description = "ID de la orden") @PathVariable UUID id,
            @AuthenticationPrincipal Jwt jwt) {
        return assembler.toModel(orderService.obtenerPorId(id, getUserIdFromToken(jwt), esAdmin(jwt) || esVendedor(jwt)));
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
