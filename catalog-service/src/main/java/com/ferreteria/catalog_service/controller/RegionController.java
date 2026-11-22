package com.ferreteria.catalog_service.controller;

import com.ferreteria.catalog_service.dto.ApiResponse;
import com.ferreteria.catalog_service.model.Region;
import com.ferreteria.catalog_service.service.RegionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/regions")
@RequiredArgsConstructor
@Tag(name = "Regiones", description = "Gestión de regiones")
public class RegionController {

    private final RegionService service;

    @GetMapping
    @Operation(summary = "Listar todas las regiones activas")
    public ResponseEntity<ApiResponse<List<Region>>> findAll() {
        return ResponseEntity.ok(ApiResponse.ok(service.findAllActive()));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener región por ID")
    public ResponseEntity<ApiResponse<Region>> findById(@Parameter(description = "ID de la región") @PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(service.findById(id)));
    }

    @GetMapping("/{id}/exists")
    @Operation(summary = "Verificar si existe una región")
    public ResponseEntity<Boolean> exists(@Parameter(description = "ID de la región") @PathVariable Long id) {
        return ResponseEntity.ok(service.existsById(id));
    }

    @PostMapping
    @Operation(summary = "Crear región (solo ADMIN)")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Region>> create(@Valid @RequestBody Region region) {
        return ResponseEntity.ok(ApiResponse.ok(service.create(region), "Región creada"));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar región (solo ADMIN)")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Region>> update(@Parameter(description = "ID de la región") @PathVariable Long id, @Valid @RequestBody Region region) {
        return ResponseEntity.ok(ApiResponse.ok(service.update(id, region), "Región actualizada"));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar región (solo ADMIN)")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> delete(@Parameter(description = "ID de la región") @PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.ok(ApiResponse.ok(null, "Región eliminada"));
    }
}