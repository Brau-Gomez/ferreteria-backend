package com.ferreteria.ms_auth.event;

import java.time.Instant;
import java.util.Set;

public final class AuthEvents {

    private AuthEvents() {
    }

    public record AuthUserLoginEvent(
            String eventType,
            String version,
            Long userId,
            String email,
            Instant occurredAt
    ) {
        public static AuthUserLoginEvent of(com.ferreteria.ms_auth.model.User user) {
            return new AuthUserLoginEvent("auth.usuario_login", "1", user.getId(), user.getEmail(), Instant.now());
        }
    }

    public record AuthUserRegisteredEvent(
            String eventType,
            String version,
            Long userId,
            String email,
            Set<String> roles,
            Instant occurredAt
    ) {
        public static AuthUserRegisteredEvent of(com.ferreteria.ms_auth.model.User user) {
            return new AuthUserRegisteredEvent(
                    "auth.usuario_registrado",
                    "1",
                    user.getId(),
                    user.getEmail(),
                    Set.copyOf(user.getRoles()),
                    Instant.now()
            );
        }
    }

    public record AuthTokenRefreshedEvent(
            String eventType,
            String version,
            Long userId,
            Instant occurredAt
    ) {
        public static AuthTokenRefreshedEvent of(Long userId) {
            return new AuthTokenRefreshedEvent("auth.token_actualizado", "1", userId, Instant.now());
        }
    }

    public record AuthUserLogoutEvent(
            String eventType,
            String version,
            Instant occurredAt
    ) {
        public static AuthUserLogoutEvent now() {
            return new AuthUserLogoutEvent("auth.usuario_logout", "1", Instant.now());
        }
    }
}
