package com.ferreteria.user_service.controller;

import com.ferreteria.user_service.dto.AbonoRequestDTO;
import com.ferreteria.user_service.dto.AbonoResponseDTO;
import com.ferreteria.user_service.dto.PageResponseDTO;
import com.ferreteria.user_service.service.AbonoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
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
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/cuentas-corrientes/{cuentaCorrienteId}/abonos")
@RequiredArgsConstructor
@Tag(name = "Abonos", description = "Gestión de abonos/pagos a cuentas corrientes")
public class AbonoController {

    private final AbonoService abonoService;

    @PostMapping
    @Operation(summary = "Registrar nuevo abono (queda pendiente de validación)")
    public ResponseEntity<AbonoResponseDTO> crear(
            @Parameter(description = "ID de la cuenta corriente") @PathVariable UUID cuentaCorrienteId,
            @Valid @RequestBody AbonoRequestDTO request,
            @AuthenticationPrincipal Jwt jwt) {
        UUID autenticado = UUID.fromString(jwt.getSubject());
        boolean puedeAdministrar = tieneRol(jwt, "ADMIN") || tieneRol(jwt, "VENDEDOR");
        AbonoResponseDTO response = abonoService.crear(
                cuentaCorrienteId, request, autenticado, puedeAdministrar);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener abono por ID")
    public ResponseEntity<AbonoResponseDTO> obtenerPorId(
            @Parameter(description = "ID del abono") @PathVariable UUID id) {
        return ResponseEntity.ok(abonoService.obtenerPorId(id));
    }

    @GetMapping
    @Operation(summary = "Listar abonos de una cuenta corriente")
    public ResponseEntity<List<AbonoResponseDTO>> listar(
            @Parameter(description = "ID de la cuenta corriente") @PathVariable UUID cuentaCorrienteId) {
        return ResponseEntity.ok(abonoService.listarPorCuentaCorriente(cuentaCorrienteId));
    }

    @GetMapping("/paginado")
    @Operation(summary = "Listar abonos con paginación")
    public ResponseEntity<PageResponseDTO<AbonoResponseDTO>> listarPaginado(
            @Parameter(description = "ID de la cuenta corriente") @PathVariable UUID cuentaCorrienteId,
            @Parameter(description = "Número de página") @RequestParam(defaultValue = "0") @Min(0) int page,
            @Parameter(description = "Tamaño de página") @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size,
            @Parameter(description = "Filtrar por estado") @RequestParam(required = false) String estado) {
        return ResponseEntity.ok(abonoService.listarPorCuentaCorrientePaginado(cuentaCorrienteId, page, size, estado));
    }

    @GetMapping("/estado/{estado}")
    @Operation(summary = "Listar abonos de la cuenta corriente por estado")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PageResponseDTO<AbonoResponseDTO>> listarPorEstado(
            @Parameter(description = "ID de la cuenta corriente") @PathVariable UUID cuentaCorrienteId,
            @Parameter(description = "Estado del abono") @PathVariable String estado,
            @Parameter(description = "Número de página") @RequestParam(defaultValue = "0") @Min(0) int page,
            @Parameter(description = "Tamaño de página") @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size) {
        return ResponseEntity.ok(abonoService.listarPorEstado(cuentaCorrienteId, estado, page, size));
    }

    @PostMapping("/{id}/aplicar")
    @Operation(summary = "Aplicar/validar abono (solo admin)")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AbonoResponseDTO> aplicar(
            @Parameter(description = "ID del abono") @PathVariable UUID id,
            @AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok(abonoService.aplicar(id, jwt.getSubject()));
    }

    @PostMapping("/{id}/rechazar")
    @Operation(summary = "Rechazar abono (solo admin)")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AbonoResponseDTO> rechazar(
            @Parameter(description = "ID del abono") @PathVariable UUID id,
            @AuthenticationPrincipal Jwt jwt,
            @Parameter(description = "Motivo del rechazo") @RequestParam String motivo) {
        return ResponseEntity.ok(abonoService.rechazar(id, jwt.getSubject(), motivo));
    }

    @PostMapping("/{id}/anular")
    @Operation(summary = "Anular abono ya aplicado (solo admin)")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AbonoResponseDTO> anular(
            @Parameter(description = "ID del abono") @PathVariable UUID id,
            @AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok(abonoService.anular(id, jwt.getSubject()));
    }

    @GetMapping("/totales/aplicados")
    @Operation(summary = "Obtener total de abonos aplicados")
    public ResponseEntity<BigDecimal> obtenerTotalAplicado(
            @Parameter(description = "ID de la cuenta corriente") @PathVariable UUID cuentaCorrienteId) {
        return ResponseEntity.ok(abonoService.obtenerTotalAplicado(cuentaCorrienteId));
    }

    @GetMapping("/totales/pendientes")
    @Operation(summary = "Obtener total de abonos pendientes")
    public ResponseEntity<BigDecimal> obtenerTotalPendiente(
            @Parameter(description = "ID de la cuenta corriente") @PathVariable UUID cuentaCorrienteId) {
        return ResponseEntity.ok(abonoService.obtenerTotalPendiente(cuentaCorrienteId));
    }

    @GetMapping("/pendientes-antiguos")
    @Operation(summary = "Obtener abonos pendientes antiguos")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<AbonoResponseDTO>> obtenerPendientesAntiguos(
            @Parameter(description = "Días de antigüedad") @RequestParam(defaultValue = "7") int diasAntiguedad) {
        return ResponseEntity.ok(abonoService.obtenerPendientesAntiguos(diasAntiguedad));
    }

    private boolean tieneRol(Jwt jwt, String rol) {
        var roles = jwt.getClaimAsStringList("roles");
        return roles != null && roles.contains(rol);
    }
}