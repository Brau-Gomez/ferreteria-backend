package com.ferreteria.user_service.controller;

import com.ferreteria.user_service.dto.MovimientoCuentaCorrienteResponseDTO;
import com.ferreteria.user_service.dto.PageResponseDTO;
import com.ferreteria.user_service.service.MovimientoCuentaCorrienteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/cuentas-corrientes/{cuentaCorrienteId}/movimientos")
@RequiredArgsConstructor
@Tag(name = "Movimientos Cuenta Corriente", description = "Historial de movimientos de cuentas corrientes")
public class MovimientoCuentaCorrienteController {

    private final MovimientoCuentaCorrienteService movimientoService;

    @GetMapping("/{id}")
    @Operation(summary = "Obtener movimiento por ID")
    public ResponseEntity<MovimientoCuentaCorrienteResponseDTO> obtenerPorId(
            @Parameter(description = "ID del movimiento") @PathVariable UUID id) {
        return ResponseEntity.ok(movimientoService.obtenerPorId(id));
    }

    @GetMapping
    @Operation(summary = "Listar movimientos de una cuenta corriente")
    public ResponseEntity<List<MovimientoCuentaCorrienteResponseDTO>> listar(
            @Parameter(description = "ID de la cuenta corriente") @PathVariable UUID cuentaCorrienteId) {
        return ResponseEntity.ok(movimientoService.listarPorCuentaCorriente(cuentaCorrienteId));
    }

    @GetMapping("/paginado")
    @Operation(summary = "Listar movimientos con paginación")
    public ResponseEntity<PageResponseDTO<MovimientoCuentaCorrienteResponseDTO>> listarPaginado(
            @Parameter(description = "ID de la cuenta corriente") @PathVariable UUID cuentaCorrienteId,
            @Parameter(description = "Número de página") @RequestParam(defaultValue = "0") @Min(0) int page,
            @Parameter(description = "Tamaño de página") @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size,
            @Parameter(description = "Filtrar por tipo") @RequestParam(required = false) String tipo) {
        return ResponseEntity.ok(movimientoService.listarPorCuentaCorrientePaginado(cuentaCorrienteId, page, size, tipo));
    }

    @GetMapping("/periodo")
    @Operation(summary = "Listar movimientos por período")
    public ResponseEntity<PageResponseDTO<MovimientoCuentaCorrienteResponseDTO>> listarPorPeriodo(
            @Parameter(description = "ID de la cuenta corriente") @PathVariable UUID cuentaCorrienteId,
            @Parameter(description = "Fecha inicio (ISO-8601)") @RequestParam Instant inicio,
            @Parameter(description = "Fecha fin (ISO-8601)") @RequestParam Instant fin,
            @Parameter(description = "Número de página") @RequestParam(defaultValue = "0") @Min(0) int page,
            @Parameter(description = "Tamaño de página") @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size) {
        return ResponseEntity.ok(movimientoService.listarPorPeriodo(cuentaCorrienteId, inicio, fin, page, size));
    }

    @GetMapping("/referencia")
    @Operation(summary = "Listar movimientos por referencia")
    public ResponseEntity<List<MovimientoCuentaCorrienteResponseDTO>> listarPorReferencia(
            @Parameter(description = "ID de la cuenta corriente") @PathVariable UUID cuentaCorrienteId,
            @Parameter(description = "ID de referencia") @RequestParam UUID referenciaId,
            @Parameter(description = "Tipo de referencia") @RequestParam String referenciaTipo) {
        return ResponseEntity.ok(movimientoService.listarPorReferencia(referenciaId, referenciaTipo));
    }

    @GetMapping("/totales/periodo")
    @Operation(summary = "Obtener total de movimientos en un período")
    public ResponseEntity<BigDecimal> obtenerTotalPorPeriodo(
            @Parameter(description = "ID de la cuenta corriente") @PathVariable UUID cuentaCorrienteId,
            @Parameter(description = "Fecha inicio (ISO-8601)") @RequestParam Instant inicio,
            @Parameter(description = "Fecha fin (ISO-8601)") @RequestParam Instant fin) {
        return ResponseEntity.ok(movimientoService.obtenerTotalPorPeriodo(cuentaCorrienteId, inicio, fin));
    }
}