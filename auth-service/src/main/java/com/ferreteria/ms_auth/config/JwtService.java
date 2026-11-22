package com.ferreteria.ms_auth.config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class JwtService {

    @Value("${app.jwt.secret}")
    private String secret;

    @Value("${app.jwt.access-token-expiration:900000}")
    private long accessTokenExpiration;

    @Value("${app.jwt.refresh-token-expiration:604800000}")
    private long refreshTokenExpiration;

    @Value("${app.jwt.issuer:ferreteria-auth}")
    private String issuer;

    @Value("${app.jwt.audience:ferreteria-frontend}")
    private String audience;

    private SecretKey signingKey;

    @PostConstruct
    public void init() {
        if (secret == null || secret.isBlank()) {
            throw new IllegalStateException("JWT_SECRET no configurado. Define la variable de entorno JWT_SECRET (base64, mínimo 32 bytes para HS256).");
        }
        byte[] keyBytes = java.util.Base64.getDecoder().decode(secret);
        if (keyBytes.length < 32) {
            throw new IllegalStateException("JWT_SECRET debe tener al menos 32 bytes (256 bits) para HS256");
        }
        this.signingKey = Keys.hmacShaKeyFor(keyBytes);
    }

    public String generateAccessToken(String subject, String email, List<String> roles) {
        Instant now = Instant.now();
        Instant expiry = now.plusMillis(accessTokenExpiration);
        return Jwts.builder()
                .subject(subject)
                .claim("email", email)
                .claim("roles", roles)
                .claim("type", "access")
                .issuer(issuer)
                .audience().add(audience).and()
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiry))
                .id(UUID.randomUUID().toString())
                .signWith(signingKey, Jwts.SIG.HS256)
                .compact();
    }

    public String generateRefreshToken(String subject) {
        Instant now = Instant.now();
        Instant expiry = now.plusMillis(refreshTokenExpiration);
        return Jwts.builder()
                .subject(subject)
                .claim("type", "refresh")
                .issuer(issuer)
                .audience().add(audience).and()
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiry))
                .id(UUID.randomUUID().toString())
                .signWith(signingKey, Jwts.SIG.HS256)
                .compact();
    }

    public Jws<Claims> parseToken(String token) {
        return Jwts.parser()
                .verifyWith(signingKey)
                .requireIssuer(issuer)
                .requireAudience(audience)
                .build()
                .parseSignedClaims(token);
    }

    public boolean isAccessToken(String token) {
        try {
            return "access".equals(parseToken(token).getPayload().get("type", String.class));
        } catch (JwtException e) {
            return false;
        }
    }

    public boolean isRefreshToken(String token) {
        try {
            return "refresh".equals(parseToken(token).getPayload().get("type", String.class));
        } catch (JwtException e) {
            return false;
        }
    }

    public String getSubject(String token) {
        return parseToken(token).getPayload().getSubject();
    }

    public List<String> getRoles(String token) {
        Object roles = parseToken(token).getPayload().get("roles");
        if (roles instanceof List<?> list) {
            return list.stream().map(Object::toString).toList();
        }
        return List.of();
    }

    public String getEmail(String token) {
        return parseToken(token).getPayload().get("email", String.class);
    }

    public Instant getExpiration(String token) {
        Date exp = parseToken(token).getPayload().getExpiration();
        return exp != null ? exp.toInstant() : null;
    }

    public boolean isTokenExpired(String token) {
        try {
            Instant exp = getExpiration(token);
            return exp != null && exp.isBefore(Instant.now());
        } catch (JwtException e) {
            return true;
        }
    }

    public Map<String, Object> getAllClaims(String token) {
        return parseToken(token).getPayload();
    }
}