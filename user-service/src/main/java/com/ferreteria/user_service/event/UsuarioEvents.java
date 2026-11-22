package com.ferreteria.user_service.event;

import com.ferreteria.user_service.model.Usuario;

import java.time.Instant;
import java.util.UUID;

public final class UsuarioEvents {

    private UsuarioEvents() {
    }

    public record UsuarioCreatedEvent(
            String eventType,
            String version,
            UUID usuarioId,
            String email,
            Usuario.Rol rol,
            Instant occurredAt
    ) {
        public static UsuarioCreatedEvent of(Usuario usuario) {
            return new UsuarioCreatedEvent(
                    "usuario.creado",
                    "1",
                    usuario.getId(),
                    usuario.getEmail(),
                    usuario.getRol(),
                    Instant.now()
            );
        }
    }

    public record UsuarioUpdatedEvent(
            String eventType,
            String version,
            UUID usuarioId,
            String email,
            Usuario.Rol rol,
            Usuario.EstadoUsuario estado,
            Instant occurredAt
    ) {
        public static UsuarioUpdatedEvent of(Usuario usuario) {
            return new UsuarioUpdatedEvent(
                    "usuario.actualizado",
                    "1",
                    usuario.getId(),
                    usuario.getEmail(),
                    usuario.getRol(),
                    usuario.getEstado(),
                    Instant.now()
            );
        }
    }

    public record UsuarioDeletedEvent(
            String eventType,
            String version,
            UUID usuarioId,
            Instant occurredAt
    ) {
        public static UsuarioDeletedEvent of(UUID usuarioId) {
            return new UsuarioDeletedEvent(
                    "usuario.eliminado",
                    "1",
                    usuarioId,
                    Instant.now()
            );
        }
    }
}
