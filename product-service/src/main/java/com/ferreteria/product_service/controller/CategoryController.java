package com.ferreteria.product_service.controller;

import com.ferreteria.product_service.dto.CategoryRequestDTO;
import com.ferreteria.product_service.dto.CategoryResponseDTO;
import com.ferreteria.product_service.dto.PageResponseDTO;
import com.ferreteria.product_service.service.CategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/categories")
@RequiredArgsConstructor
@Tag(name = "Categorías", description = "Gestión de categorías de productos")
public class CategoryController {

    private final CategoryService categoryService;

    @PostMapping
    @Operation(summary = "Crear nueva categoría")
    @PreAuthorize("hasAnyRole('ADMIN', 'VENDEDOR')")
    public ResponseEntity<CategoryResponseDTO> crear(@Valid @RequestBody CategoryRequestDTO request) {
        CategoryResponseDTO response = categoryService.crear(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener categoría por ID")
    public ResponseEntity<CategoryResponseDTO> obtenerPorId(
            @Parameter(description = "ID de la categoría") @PathVariable UUID id) {
        return ResponseEntity.ok(categoryService.obtenerPorId(id));
    }

    @GetMapping
    @Operation(summary = "Listar categorías con paginación y filtros")
    public ResponseEntity<PageResponseDTO<CategoryResponseDTO>> listar(
            @Parameter(description = "Número de página (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Tamaño de página") @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "Campo para ordenar") @RequestParam(defaultValue = "sortOrder") String sortBy,
            @Parameter(description = "Dirección de orden (asc/desc)") @RequestParam(defaultValue = "asc") String sortDir,
            @Parameter(description = "Filtro por nombre") @RequestParam(required = false) String filtro,
            @Parameter(description = "Solo categorías activas") @RequestParam(required = false) Boolean soloActivas) {
        return ResponseEntity.ok(categoryService.listar(page, size, sortBy, sortDir, filtro, soloActivas));
    }

    @GetMapping("/activas")
    @Operation(summary = "Listar solo categorías activas (para selects)")
    public ResponseEntity<List<CategoryResponseDTO>> listarActivas() {
        return ResponseEntity.ok(categoryService.listarActivas());
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar categoría")
    @PreAuthorize("hasAnyRole('ADMIN', 'VENDEDOR')")
    public ResponseEntity<CategoryResponseDTO> actualizar(
            @Parameter(description = "ID de la categoría") @PathVariable UUID id,
            @Valid @RequestBody CategoryRequestDTO request) {
        return ResponseEntity.ok(categoryService.actualizar(id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar categoría")
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminar(@Parameter(description = "ID de la categoría") @PathVariable UUID id) {
        categoryService.eliminar(id);
    }
}