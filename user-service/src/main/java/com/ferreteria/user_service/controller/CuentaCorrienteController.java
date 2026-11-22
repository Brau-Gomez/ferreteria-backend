package com.ferreteria.user_service.controller;

import com.ferreteria.user_service.dto.CuentaCorrienteRequestDTO;
import com.ferreteria.user_service.dto.CuentaCorrienteResponseDTO;
import com.ferreteria.user_service.dto.PageResponseDTO;
import com.ferreteria.user_service.service.CuentaCorrienteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/cuentas-corrientes")
@RequiredArgsConstructor
@Tag(name = "Cuentas Corrientes", description = "Gestión de cuentas corrientes de contratistas")
public class CuentaCorrienteController {

    private final CuentaCorrienteService cuentaCorrienteService;

    @PostMapping("/usuario/{usuarioId}")
    @Operation(summary = "Crear cuenta corriente para un usuario")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<CuentaCorrienteResponseDTO> crear(
            @Parameter(description = "ID del usuario") @PathVariable UUID usuarioId,
            @Valid @RequestBody CuentaCorrienteRequestDTO request) {
        CuentaCorrienteResponseDTO response = cuentaCorrienteService.crear(usuarioId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener cuenta corriente por ID")
    public ResponseEntity<CuentaCorrienteResponseDTO> obtenerPorId(
            @Parameter(description = "ID de la cuenta corriente") @PathVariable UUID id) {
        return ResponseEntity.ok(cuentaCorrienteService.obtenerPorId(id));
    }

    @GetMapping("/usuario/{usuarioId}")
    @Operation(summary = "Obtener cuenta corriente por ID de usuario")
    public ResponseEntity<CuentaCorrienteResponseDTO> obtenerPorUsuarioId(
            @Parameter(description = "ID del usuario") @PathVariable UUID usuarioId) {
        return ResponseEntity.ok(cuentaCorrienteService.obtenerPorUsuarioId(usuarioId));
    }

    @GetMapping
    @Operation(summary = "Listar cuentas corrientes con paginación")
    public ResponseEntity<PageResponseDTO<CuentaCorrienteResponseDTO>> listar(
            @Parameter(description = "Número de página") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Tamaño de página") @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "Filtrar por estado") @RequestParam(required = false) String estado) {
        return ResponseEntity.ok(cuentaCorrienteService.listar(page, size, estado));
    }

    @GetMapping("/estado/{estado}")
    @Operation(summary = "Listar cuentas corrientes por estado")
    public ResponseEntity<PageResponseDTO<CuentaCorrienteResponseDTO>> listarPorEstado(
            @Parameter(description = "Estado de la cuenta") @PathVariable String estado,
            @Parameter(description = "Número de página") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Tamaño de página") @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(cuentaCorrienteService.listarPorEstado(estado, page, size));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar cuenta corriente")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<CuentaCorrienteResponseDTO> actualizar(
            @Parameter(description = "ID de la cuenta corriente") @PathVariable UUID id,
            @Valid @RequestBody CuentaCorrienteRequestDTO request) {
        return ResponseEntity.ok(cuentaCorrienteService.actualizar(id, request));
    }

    @PatchMapping("/{id}/estado")
    @Operation(summary = "Actualizar estado de la cuenta corriente")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<CuentaCorrienteResponseDTO> actualizarEstado(
            @Parameter(description = "ID de la cuenta corriente") @PathVariable UUID id,
            @Parameter(description = "Nuevo estado") @RequestParam String estado) {
        return ResponseEntity.ok(cuentaCorrienteService.actualizarEstado(id, estado));
    }

    @PatchMapping("/{id}/limite")
    @Operation(summary = "Actualizar límite de crédito")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<CuentaCorrienteResponseDTO> actualizarLimite(
            @Parameter(description = "ID de la cuenta corriente") @PathVariable UUID id,
            @Parameter(description = "Nuevo límite de crédito") @RequestParam BigDecimal nuevoLimite) {
        return ResponseEntity.ok(cuentaCorrienteService.actualizarLimiteCredito(id, nuevoLimite));
    }

    @PostMapping("/{id}/cargo")
    @Operation(summary = "Aplicar cargo a la cuenta corriente")
    @PreAuthorize("hasRole('ADMINISTRADOR') or hasRole('VENDEDOR')")
    public ResponseEntity<CuentaCorrienteResponseDTO> aplicarCargo(
            @Parameter(description = "ID de la cuenta corriente") @PathVariable UUID id,
            @Parameter(description = "Monto del cargo") @RequestParam BigDecimal monto,
            @Parameter(description = "Descripción del cargo") @RequestParam String descripcion,
            @Parameter(description = "ID de referencia (orden, factura, etc.)") @RequestParam(required = false) UUID referenciaId,
            @Parameter(description = "Tipo de referencia") @RequestParam(required = false) String referenciaTipo) {
        return ResponseEntity.ok(cuentaCorrienteService.aplicarCargo(id, monto, descripcion, referenciaId, referenciaTipo));
    }

    @PostMapping("/{id}/abono")
    @Operation(summary = "Aplicar abono a la cuenta corriente")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<CuentaCorrienteResponseDTO> aplicarAbono(
            @Parameter(description = "ID de la cuenta corriente") @PathVariable UUID id,
            @Parameter(description = "Monto del abono") @RequestParam BigDecimal monto,
            @Parameter(description = "Descripción del abono") @RequestParam String descripcion,
            @Parameter(description = "ID de referencia") @RequestParam(required = false) UUID referenciaId,
            @Parameter(description = "Tipo de referencia") @RequestParam(required = false) String referenciaTipo) {
        return ResponseEntity.ok(cuentaCorrienteService.aplicarAbono(id, monto, descripcion, referenciaId, referenciaTipo));
    }

    @PostMapping("/{id}/procesar-interes")
    @Operation(summary = "Procesar interés mensual")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<CuentaCorrienteResponseDTO> procesarInteresMensual(
            @Parameter(description = "ID de la cuenta corriente") @PathVariable UUID id) {
        return ResponseEntity.ok(cuentaCorrienteService.procesarInteresMensual(id));
    }

    @GetMapping("/vencidas/corte/{diaCorte}")
    @Operation(summary = "Obtener cuentas vencidas para corte")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<List<CuentaCorrienteResponseDTO>> obtenerVencidasParaCorte(
            @Parameter(description = "Día de corte") @PathVariable Integer diaCorte) {
        return ResponseEntity.ok(cuentaCorrienteService.obtenerVencidasParaCorte(diaCorte));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar cuenta corriente")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminar(@Parameter(description = "ID de la cuenta corriente") @PathVariable UUID id) {
        cuentaCorrienteService.eliminar(id);
    }
}