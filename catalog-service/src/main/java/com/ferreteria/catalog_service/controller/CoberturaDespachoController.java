package com.ferreteria.catalog_service.controller;

import com.ferreteria.catalog_service.dto.ApiResponse;
import com.ferreteria.catalog_service.model.CoberturaDespacho;
import com.ferreteria.catalog_service.service.CoberturaDespachoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/coverage")
@RequiredArgsConstructor
@Tag(name = "Cobertura de Despacho", description = "Gestión de cobertura y costos de despacho por comuna")
public class CoberturaDespachoController {

    private final CoberturaDespachoService service;

    @GetMapping
    @Operation(summary = "Listar todas las comunas con cobertura activa")
    public ResponseEntity<ApiResponse<List<CoberturaDespacho>>> findAllCovered() {
        return ResponseEntity.ok(ApiResponse.ok(service.findAllActiveCovered()));
    }

    @GetMapping("/comuna/{comunaId}")
    @Operation(summary = "Obtener cobertura por comuna")
    public ResponseEntity<ApiResponse<CoberturaDespacho>> findByComuna(@Parameter(description = "ID de la comuna") @PathVariable Long comunaId) {
        Optional<CoberturaDespacho> coverage = service.findByComunaId(comunaId);
        return coverage.map(c -> ResponseEntity.ok(ApiResponse.ok(c)))
                .orElseGet(() -> ResponseEntity.ok(ApiResponse.ok(null)));
    }

    @GetMapping("/check/{comunaId}")
    @Operation(summary = "Verificar si una comuna tiene cobertura")
    public ResponseEntity<ApiResponse<CoverageCheckResponse>> checkCoverage(@Parameter(description = "ID de la comuna") @PathVariable Long comunaId) {
        boolean covered = service.isCovered(comunaId);
        BigDecimal cost = service.getDeliveryCost(comunaId);
        Integer days = service.getEstimatedDays(comunaId);
        return ResponseEntity.ok(ApiResponse.ok(new CoverageCheckResponse(comunaId, covered, cost, days)));
    }

    @PostMapping
    @Operation(summary = "Crear o actualizar cobertura (solo ADMIN)")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<CoberturaDespacho>> createOrUpdate(
            @Parameter(description = "ID de la comuna") @RequestParam Long comunaId,
            @Parameter(description = "Si tiene cobertura") @RequestParam Boolean isCovered,
            @Parameter(description = "Costo de despacho") @RequestParam(required = false) BigDecimal deliveryCost,
            @Parameter(description = "Monto mínimo de orden") @RequestParam(required = false) BigDecimal minOrderAmount,
            @Parameter(description = "Días estimados") @RequestParam(required = false) Integer estimatedDays) {
        return ResponseEntity.ok(ApiResponse.ok(
                service.createOrUpdate(comunaId, isCovered, deliveryCost, minOrderAmount, estimatedDays),
                "Cobertura actualizada"
        ));
    }

    @DeleteMapping("/comuna/{comunaId}")
    @Operation(summary = "Eliminar cobertura (solo ADMIN)")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> delete(@Parameter(description = "ID de la comuna") @PathVariable Long comunaId) {
        service.delete(comunaId);
        return ResponseEntity.ok(ApiResponse.ok(null, "Cobertura eliminada"));
    }

    public record CoverageCheckResponse(
            Long comunaId,
            boolean covered,
            BigDecimal deliveryCost,
            Integer estimatedDays
    ) {}
}