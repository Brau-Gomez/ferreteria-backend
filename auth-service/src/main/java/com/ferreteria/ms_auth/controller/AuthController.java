package com.ferreteria.ms_auth.controller;

import com.ferreteria.ms_auth.dto.AuthResponseDTO;
import com.ferreteria.ms_auth.dto.LoginRequestDTO;
import com.ferreteria.ms_auth.dto.RegistroRequestDTO;
import com.ferreteria.ms_auth.dto.RefreshTokenDTO;
import com.ferreteria.ms_auth.dto.ValidacionTokenDTO;
import com.ferreteria.ms_auth.dto.UsuarioPerfilDTO;
import com.ferreteria.ms_auth.service.AuthService;
import com.ferreteria.ms_auth.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Tag(name = "Autenticación", description = "Endpoints de autenticación y gestión de tokens")
public class AuthController {

    private final AuthService authService;
    private final UserService userService;

    @PostMapping("/login")
    @Operation(summary = "Iniciar sesión")
    public ResponseEntity<AuthResponseDTO> login(@Valid @RequestBody LoginRequestDTO request, HttpServletRequest httpRequest) {
        AuthResponseDTO response = authService.login(request.getEmail(), request.getPassword(), httpRequest.getRemoteAddr());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/register")
    @Operation(summary = "Registrar nuevo usuario")
    public ResponseEntity<AuthResponseDTO> register(@Valid @RequestBody RegistroRequestDTO request) {
        AuthResponseDTO response = authService.register(request.getUsername(), request.getEmail(), request.getPassword(), request.getFullName(), request.getPhone());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/refresh")
    @Operation(summary = "Refrescar access token")
    public ResponseEntity<AuthResponseDTO> refresh(@Valid @RequestBody RefreshTokenDTO request) {
        AuthResponseDTO response = authService.refreshToken(request.getRefreshToken());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/logout")
    @Operation(summary = "Cerrar sesión (revoca refresh token)")
    public ResponseEntity<Void> logout(@Valid @RequestBody RefreshTokenDTO request) {
        authService.logout(request.getRefreshToken());
        return ResponseEntity.ok().build();
    }

    @GetMapping("/validate")
    @Operation(summary = "Validar access token")
    public ResponseEntity<ValidacionTokenDTO> validate(@RequestHeader("Authorization") String authHeader) {
        String token = authHeader != null && authHeader.startsWith("Bearer ") ? authHeader.substring(7) : "";
        ValidacionTokenDTO result = authService.validateToken(token);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/me")
    @Operation(summary = "Obtener perfil del usuario autenticado")
    public ResponseEntity<UsuarioPerfilDTO> me(@AuthenticationPrincipal UserDetails userDetails) {
        var user = userService.findByEmail(userDetails.getUsername());
        return ResponseEntity.ok(UsuarioPerfilDTO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .phone(user.getPhone())
                .roles(user.getRoles())
                .build());
    }

    @PostMapping("/migrate-password")
    @Operation(summary = "Migrar contraseña PBKDF2 a BCrypt (legacy)")
    public ResponseEntity<Void> migratePassword(@AuthenticationPrincipal UserDetails userDetails,
                                                  @RequestBody Map<String, String> body) {
        String plainPassword = body.get("password");
        if (plainPassword == null || plainPassword.isBlank()) {
            return ResponseEntity.badRequest().build();
        }
        authService.migratePassword(userDetails.getUsername(), plainPassword);
        return ResponseEntity.ok().build();
    }
}