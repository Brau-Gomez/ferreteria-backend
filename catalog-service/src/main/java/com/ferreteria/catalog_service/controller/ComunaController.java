package com.ferreteria.catalog_service.controller;

import com.ferreteria.catalog_service.dto.ApiResponse;
import com.ferreteria.catalog_service.model.Comuna;
import com.ferreteria.catalog_service.service.ComunaService;
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
@RequestMapping("/communes")
@RequiredArgsConstructor
@Tag(name = "Comunas", description = "Gestión de comunas")
public class ComunaController {

    private final ComunaService service;

    @GetMapping
    @Operation(summary = "Listar todas las comunas activas")
    public ResponseEntity<ApiResponse<List<Comuna>>> findAll() {
        return ResponseEntity.ok(ApiResponse.ok(service.findAllActive()));
    }

    @GetMapping("/region/{regionId}")
    @Operation(summary = "Listar comunas por región")
    public ResponseEntity<ApiResponse<List<Comuna>>> findByRegion(@Parameter(description = "ID de la región") @PathVariable Long regionId) {
        return ResponseEntity.ok(ApiResponse.ok(service.findByRegionId(regionId)));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener comuna por ID")
    public ResponseEntity<ApiResponse<Comuna>> findById(@Parameter(description = "ID de la comuna") @PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(service.findById(id)));
    }

    @GetMapping("/{id}/exists")
    @Operation(summary = "Verificar si existe una comuna")
    public ResponseEntity<Boolean> exists(@Parameter(description = "ID de la comuna") @PathVariable Long id) {
        return ResponseEntity.ok(service.existsById(id));
    }

    @PostMapping
    @Operation(summary = "Crear comuna (solo ADMIN)")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Comuna>> create(@Valid @RequestBody Comuna comuna) {
        return ResponseEntity.ok(ApiResponse.ok(service.create(comuna), "Comuna creada"));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar comuna (solo ADMIN)")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Comuna>> update(@Parameter(description = "ID de la comuna") @PathVariable Long id, @Valid @RequestBody Comuna comuna) {
        return ResponseEntity.ok(ApiResponse.ok(service.update(id, comuna), "Comuna actualizada"));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar comuna (solo ADMIN)")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> delete(@Parameter(description = "ID de la comuna") @PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.ok(ApiResponse.ok(null, "Comuna eliminada"));
    }
}