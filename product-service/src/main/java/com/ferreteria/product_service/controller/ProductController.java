package com.ferreteria.product_service.controller;

import com.ferreteria.product_service.dto.PageResponseDTO;
import com.ferreteria.product_service.dto.ProductImageRequestDTO;
import com.ferreteria.product_service.dto.ProductImageResponseDTO;
import com.ferreteria.product_service.dto.ProductRequestDTO;
import com.ferreteria.product_service.dto.ProductResponseDTO;
import com.ferreteria.product_service.service.ProductService;
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
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/products")
@RequiredArgsConstructor
@Tag(name = "Productos", description = "Gestión de productos, stock e imágenes")
public class ProductController {

    private final ProductService productService;

    @PostMapping
    @Operation(summary = "Crear nuevo producto")
    @PreAuthorize("hasAnyRole('ADMIN', 'VENDEDOR')")
    public ResponseEntity<ProductResponseDTO> crear(@Valid @RequestBody ProductRequestDTO request) {
        ProductResponseDTO response = productService.crear(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener producto por ID")
    public ResponseEntity<ProductResponseDTO> obtenerPorId(
            @Parameter(description = "ID del producto") @PathVariable UUID id) {
        return ResponseEntity.ok(productService.obtenerPorId(id));
    }

    @GetMapping("/code/{code}")
    @Operation(summary = "Obtener producto por código")
    public ResponseEntity<ProductResponseDTO> obtenerPorCode(
            @Parameter(description = "Código del producto") @PathVariable String code) {
        return ResponseEntity.ok(productService.obtenerPorCode(code));
    }

    @GetMapping("/{id}/exists")
    @Operation(summary = "Verificar si existe un producto por ID")
    public ResponseEntity<Boolean> existePorId(
            @Parameter(description = "ID del producto") @PathVariable UUID id) {
        return ResponseEntity.ok(productService.existePorId(id));
    }

    @GetMapping
    @Operation(summary = "Listar productos con paginación y filtros")
    public ResponseEntity<PageResponseDTO<ProductResponseDTO>> listar(
            @Parameter(description = "Número de página (0-based)") @RequestParam(defaultValue = "0") @Min(0) int page,
            @Parameter(description = "Tamaño de página") @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size,
            @Parameter(description = "Campo para ordenar") @RequestParam(defaultValue = "createdAt") String sortBy,
            @Parameter(description = "Dirección de orden (asc/desc)") @RequestParam(defaultValue = "desc") String sortDir,
            @Parameter(description = "Filtro por nombre o código") @RequestParam(required = false) String filtro,
            @Parameter(description = "Filtrar por categoría") @RequestParam(required = false) UUID categoryId,
            @Parameter(description = "Solo productos activos") @RequestParam(required = false) Boolean soloActivas,
            @Parameter(description = "Precio mínimo") @RequestParam(required = false) BigDecimal minPrice,
            @Parameter(description = "Precio máximo") @RequestParam(required = false) BigDecimal maxPrice) {
        return ResponseEntity.ok(productService.listar(page, size, sortBy, sortDir, filtro, categoryId, soloActivas, minPrice, maxPrice));
    }

    @GetMapping("/featured")
    @Operation(summary = "Listar productos destacados")
    public ResponseEntity<List<ProductResponseDTO>> listarDestacados() {
        return ResponseEntity.ok(productService.listarDestacados());
    }

    @GetMapping("/low-stock")
    @Operation(summary = "Listar productos con stock bajo")
    @PreAuthorize("hasAnyRole('ADMIN', 'VENDEDOR')")
    public ResponseEntity<List<ProductResponseDTO>> listarStockBajo() {
        return ResponseEntity.ok(productService.listarStockBajo());
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar producto")
    @PreAuthorize("hasAnyRole('ADMIN', 'VENDEDOR')")
    public ResponseEntity<ProductResponseDTO> actualizar(
            @Parameter(description = "ID del producto") @PathVariable UUID id,
            @Valid @RequestBody ProductRequestDTO request) {
        return ResponseEntity.ok(productService.actualizar(id, request));
    }

    @PatchMapping("/{id}/stock")
    @Operation(summary = "Actualizar stock (suma o resta cantidad)")
    @PreAuthorize("hasAnyRole('ADMIN', 'VENDEDOR')")
    public ResponseEntity<ProductResponseDTO> actualizarStock(
            @Parameter(description = "ID del producto") @PathVariable UUID id,
            @Parameter(description = "Cantidad a sumar (positivo) o restar (negativo)") @RequestParam int cantidad) {
        return ResponseEntity.ok(productService.actualizarStock(id, cantidad));
    }

    @PatchMapping("/{id}/estado")
    @Operation(summary = "Actualizar estado activo/inactivo")
    @PreAuthorize("hasAnyRole('ADMIN', 'VENDEDOR')")
    public ResponseEntity<ProductResponseDTO> actualizarEstado(
            @Parameter(description = "ID del producto") @PathVariable UUID id,
            @Parameter(description = "Nuevo estado") @RequestParam boolean active) {
        return ResponseEntity.ok(productService.actualizarEstado(id, active));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar producto")
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminar(@Parameter(description = "ID del producto") @PathVariable UUID id) {
        productService.eliminar(id);
    }

    // Imágenes
    @PostMapping("/{id}/images")
    @Operation(summary = "Agregar imagen a un producto")
    @PreAuthorize("hasAnyRole('ADMIN', 'VENDEDOR')")
    public ResponseEntity<ProductImageResponseDTO> agregarImagen(
            @Parameter(description = "ID del producto") @PathVariable UUID id,
            @Valid @RequestBody ProductImageRequestDTO request) {
        ProductImageResponseDTO response = productService.agregarImagen(id, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @DeleteMapping("/images/{imageId}")
    @Operation(summary = "Eliminar imagen de un producto")
    @PreAuthorize("hasAnyRole('ADMIN', 'VENDEDOR')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminarImagen(@Parameter(description = "ID de la imagen") @PathVariable UUID imageId) {
        productService.eliminarImagen(imageId);
    }
}