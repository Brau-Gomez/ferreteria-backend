package com.ferreteria.api_gateway.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Base64;
import java.util.List;

@Service
public class JwtService {

    private final String secret;
    private final String issuer;
    private final String audience;
    private SecretKey signingKey;

    public JwtService(
            @Value("${app.jwt.secret}") String secret,
            @Value("${app.jwt.issuer}") String issuer,
            @Value("${app.jwt.audience}") String audience) {
        this.secret = secret;
        this.issuer = issuer;
        this.audience = audience;
    }

    @PostConstruct
    void init() {
        if (secret == null || secret.isBlank()) {
            throw new IllegalStateException("JWT_SECRET no configurado");
        }

        byte[] keyBytes = Base64.getDecoder().decode(secret);
        if (keyBytes.length < 32) {
            throw new IllegalStateException("JWT_SECRET debe tener al menos 32 bytes");
        }
        this.signingKey = Keys.hmacShaKeyFor(keyBytes);
    }

    public Claims obtenerClaimsSiEsValido(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(signingKey)
                    .requireIssuer(issuer)
                    .requireAudience(audience)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            if (!"access".equals(claims.get("type", String.class))) {
                return null;
            }
            return claims;
        } catch (JwtException | IllegalArgumentException exception) {
            return null;
        }
    }

    public List<String> obtenerRoles(Claims claims) {
        Object roles = claims.get("roles");
        if (roles instanceof List<?> roleList) {
            return roleList.stream().map(Object::toString).toList();
        }
        return List.of();
    }
}
