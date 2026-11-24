package com.ferreteria.user_service.controller;

import com.ferreteria.user_service.dto.PageResponseDTO;
import com.ferreteria.user_service.dto.UsuarioRequestDTO;
import com.ferreteria.user_service.dto.UsuarioResponseDTO;
import com.ferreteria.user_service.model.Usuario;
import com.ferreteria.user_service.service.UsuarioService;
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

import java.util.UUID;

@RestController
@RequestMapping({"/usuarios", "/users"})
@RequiredArgsConstructor
@Tag(name = "Usuarios", description = "Gestión de usuarios del sistema")
public class UsuarioController {

    private final UsuarioService usuarioService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Crear usuario (solo ADMIN)")
    public ResponseEntity<UsuarioResponseDTO> crear(@Valid @RequestBody UsuarioRequestDTO request) {
        UsuarioResponseDTO response = usuarioService.crear(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or #id.toString() == authentication.name")
    @Operation(summary = "Obtener usuario por ID")
    public ResponseEntity<UsuarioResponseDTO> obtenerPorId(
            @Parameter(description = "ID del usuario") @PathVariable UUID id,
            @AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok(usuarioService.obtenerPorId(id));
    }

    @GetMapping("/{id}/exists")
    @Operation(summary = "Verificar si existe un usuario por ID")
    public ResponseEntity<Boolean> existePorId(
            @Parameter(description = "ID del usuario") @PathVariable UUID id) {
        return ResponseEntity.ok(usuarioService.existePorId(id));
    }

    @GetMapping("/email/{email}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Obtener usuario por email (solo ADMIN)")
    public ResponseEntity<UsuarioResponseDTO> obtenerPorEmail(
            @Parameter(description = "Email del usuario") @PathVariable String email) {
        return ResponseEntity.ok(usuarioService.obtenerPorEmail(email));
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('VENDEDOR')")
    @Operation(summary = "Listar usuarios con paginación y filtros")
    public ResponseEntity<PageResponseDTO<UsuarioResponseDTO>> listar(
            @Parameter(description = "Número de página (0-based)") @RequestParam(defaultValue = "0") @Min(0) int page,
            @Parameter(description = "Tamaño de página") @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size,
            @Parameter(description = "Campo para ordenar") @RequestParam(defaultValue = "nombreCompleto") String sortBy,
            @Parameter(description = "Dirección de orden (asc/desc)") @RequestParam(defaultValue = "asc") String sortDir,
            @Parameter(description = "Filtro por nombre o email") @RequestParam(required = false) String filtro) {
        return ResponseEntity.ok(usuarioService.listar(page, size, sortBy, sortDir, filtro));
    }

    @GetMapping("/rol/{rol}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('VENDEDOR')")
    @Operation(summary = "Listar usuarios por rol")
    public ResponseEntity<PageResponseDTO<UsuarioResponseDTO>> listarPorRol(
            @Parameter(description = "Rol del usuario") @PathVariable Usuario.Rol rol,
            @Parameter(description = "Número de página") @RequestParam(defaultValue = "0") @Min(0) int page,
            @Parameter(description = "Tamaño de página") @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size) {
        return ResponseEntity.ok(usuarioService.listarPorRol(rol, page, size));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or #id.toString() == authentication.name")
    @Operation(summary = "Actualizar usuario")
    public ResponseEntity<UsuarioResponseDTO> actualizar(
            @Parameter(description = "ID del usuario") @PathVariable UUID id,
            @Valid @RequestBody UsuarioRequestDTO request) {
        return ResponseEntity.ok(usuarioService.actualizar(id, request));
    }

    @PatchMapping("/{id}/estado")
    @Operation(summary = "Actualizar estado del usuario")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UsuarioResponseDTO> actualizarEstado(
            @Parameter(description = "ID del usuario") @PathVariable UUID id,
            @Parameter(description = "Nuevo estado") @RequestParam Usuario.EstadoUsuario estado) {
        return ResponseEntity.ok(usuarioService.actualizarEstado(id, estado));
    }

    @PatchMapping("/{id}/rol")
    @Operation(summary = "Actualizar rol del usuario")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UsuarioResponseDTO> actualizarRol(
            @Parameter(description = "ID del usuario") @PathVariable UUID id,
            @Parameter(description = "Nuevo rol") @RequestParam Usuario.Rol rol) {
        return ResponseEntity.ok(usuarioService.actualizarRol(id, rol));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar usuario")
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminar(@Parameter(description = "ID del usuario") @PathVariable UUID id) {
        usuarioService.eliminar(id);
    }
}