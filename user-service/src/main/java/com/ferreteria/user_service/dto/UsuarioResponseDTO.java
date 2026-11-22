package com.ferreteria.user_service.dto;

import com.ferreteria.user_service.model.Usuario;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UsuarioResponseDTO {

    private UUID id;
    private String email;
    private String nombreCompleto;
    private String telefono;
    private String rut;
    private Usuario.Rol rol;
    private Usuario.EstadoUsuario estado;
    private Boolean emailVerificado;
    private Instant createdAt;
    private Instant updatedAt;

    public static UsuarioResponseDTO fromEntity(Usuario usuario) {
        return UsuarioResponseDTO.builder()
                .id(usuario.getId())
                .email(usuario.getEmail())
                .nombreCompleto(usuario.getNombreCompleto())
                .telefono(usuario.getTelefono())
                .rut(usuario.getRut())
                .rol(usuario.getRol())
                .estado(usuario.getEstado())
                .emailVerificado(usuario.getEmailVerificado())
                .createdAt(usuario.getCreatedAt())
                .updatedAt(usuario.getUpdatedAt())
                .build();
    }
}