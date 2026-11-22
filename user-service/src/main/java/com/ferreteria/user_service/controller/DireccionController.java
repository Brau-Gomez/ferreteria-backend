package com.ferreteria.user_service.controller;

import com.ferreteria.user_service.dto.DireccionRequestDTO;
import com.ferreteria.user_service.dto.DireccionResponseDTO;
import com.ferreteria.user_service.dto.PageResponseDTO;
import com.ferreteria.user_service.service.DireccionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/usuarios/{usuarioId}/direcciones")
@RequiredArgsConstructor
@Tag(name = "Direcciones", description = "Gestión de direcciones de usuarios")
public class DireccionController {

    private final DireccionService direccionService;

    @PostMapping
    @Operation(summary = "Crear dirección para un usuario")
    public ResponseEntity<DireccionResponseDTO> crear(
            @Parameter(description = "ID del usuario") @PathVariable UUID usuarioId,
            @Valid @RequestBody DireccionRequestDTO request) {
        DireccionResponseDTO response = direccionService.crear(usuarioId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener dirección por ID")
    public ResponseEntity<DireccionResponseDTO> obtenerPorId(
            @Parameter(description = "ID del usuario") @PathVariable UUID usuarioId,
            @Parameter(description = "ID de la dirección") @PathVariable UUID id) {
        return ResponseEntity.ok(direccionService.obtenerPorUsuario(usuarioId, id));
    }

    @GetMapping
    @Operation(summary = "Listar direcciones de un usuario")
    public ResponseEntity<List<DireccionResponseDTO>> listar(
            @Parameter(description = "ID del usuario") @PathVariable UUID usuarioId) {
        return ResponseEntity.ok(direccionService.listarPorUsuario(usuarioId));
    }

    @GetMapping("/paginado")
    @Operation(summary = "Listar direcciones de un usuario con paginación")
    public ResponseEntity<PageResponseDTO<DireccionResponseDTO>> listarPaginado(
            @Parameter(description = "ID del usuario") @PathVariable UUID usuarioId,
            @Parameter(description = "Número de página") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Tamaño de página") @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(direccionService.listarPorUsuarioPaginado(usuarioId, page, size));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar dirección")
    public ResponseEntity<DireccionResponseDTO> actualizar(
            @Parameter(description = "ID de la dirección") @PathVariable UUID id,
            @Valid @RequestBody DireccionRequestDTO request) {
        return ResponseEntity.ok(direccionService.actualizar(id, request));
    }

    @PatchMapping("/{id}/principal")
    @Operation(summary = "Establecer dirección como principal")
    public ResponseEntity<DireccionResponseDTO> establecerPrincipal(
            @Parameter(description = "ID de la dirección") @PathVariable UUID id) {
        return ResponseEntity.ok(direccionService.establecerPrincipal(id));
    }

    @PatchMapping("/{id}/facturacion")
    @Operation(summary = "Establecer dirección para facturación")
    public ResponseEntity<DireccionResponseDTO> establecerFacturacion(
            @Parameter(description = "ID de la dirección") @PathVariable UUID id) {
        return ResponseEntity.ok(direccionService.establecerFacturacion(id));
    }

    @PatchMapping("/{id}/despacho")
    @Operation(summary = "Establecer dirección para despacho")
    public ResponseEntity<DireccionResponseDTO> establecerDespacho(
            @Parameter(description = "ID de la dirección") @PathVariable UUID id) {
        return ResponseEntity.ok(direccionService.establecerDespacho(id));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar dirección")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminar(@Parameter(description = "ID de la dirección") @PathVariable UUID id) {
        direccionService.eliminar(id);
    }
}