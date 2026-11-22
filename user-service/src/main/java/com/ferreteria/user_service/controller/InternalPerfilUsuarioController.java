package com.ferreteria.user_service.controller;

import com.ferreteria.user_service.dto.CrearPerfilUsuarioRequestDTO;
import com.ferreteria.user_service.dto.UsuarioResponseDTO;
import com.ferreteria.user_service.service.UsuarioService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/internal/users")
@RequiredArgsConstructor
public class InternalPerfilUsuarioController {

    private final UsuarioService usuarioService;

    @PostMapping("/profiles")
    public ResponseEntity<UsuarioResponseDTO> crearPerfil(
            @Valid @RequestBody CrearPerfilUsuarioRequestDTO request) {
        UsuarioResponseDTO response = usuarioService.crearPerfilDesdeAuth(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
