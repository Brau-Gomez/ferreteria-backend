package com.ferreteria.api_gateway.security;

import io.jsonwebtoken.Jwts;
import org.junit.jupiter.api.Test;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;

import javax.crypto.SecretKey;
import io.jsonwebtoken.security.Keys;
import java.time.Instant;
import java.util.Base64;
import java.util.Date;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.assertj.core.api.Assertions.assertThat;

class JwtAuthenticationFilterTest {

    private static final String SECRET = "ZGV2LXNlY3JldC1rZXktZm9yLWZlcnJldGVyaWEtbG9jYWwtb25seS0zMmJ5dGVz";

    private final JwtService jwtService = new JwtService(
            SECRET,
            "ferreteria-auth",
            "ferreteria-frontend");

    JwtAuthenticationFilterTest() {
        jwtService.init();
    }

    @Test
    void loginRouteDoesNotRequireToken() {
        JwtAuthenticationFilter filter = new JwtAuthenticationFilter(jwtService);
        MockServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.post("/api/auth/login").build());
        AtomicBoolean chained = new AtomicBoolean();
        GatewayFilterChain chain = ignored -> {
            chained.set(true);
            return reactor.core.publisher.Mono.empty();
        };

        filter.filter(exchange, chain).block();

        assertThat(chained).isTrue();
        assertThat(exchange.getResponse().getStatusCode()).isNull();
    }

    @Test
    void protectedRouteWithoutTokenReturnsUnauthorized() {
        JwtAuthenticationFilter filter = new JwtAuthenticationFilter(jwtService);
        MockServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.get("/api/orders").build());

        filter.filter(exchange, ignored -> reactor.core.publisher.Mono.empty()).block();

        assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void protectedRouteWithValidAccessTokenAddsUserHeaders() {
        JwtAuthenticationFilter filter = new JwtAuthenticationFilter(jwtService);
        String token = crearAccessToken();
        MockServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.get("/api/orders")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .build());
        AtomicBoolean chained = new AtomicBoolean();
        GatewayFilterChain chain = exchangeEnviada -> {
            chained.set(true);
            assertThat(exchangeEnviada.getRequest().getHeaders().getFirst("X-User-Subject"))
                    .isEqualTo("42");
            assertThat(exchangeEnviada.getRequest().getHeaders().getFirst("X-User-Email"))
                    .isEqualTo("cliente@example.com");
            assertThat(exchangeEnviada.getRequest().getHeaders().getFirst("X-User-Roles"))
                    .isEqualTo("CLIENTE");
            return reactor.core.publisher.Mono.empty();
        };

        filter.filter(exchange, chain).block();

        assertThat(chained).isTrue();
    }

    @Test
    void internalRouteIsNotPublishedByGateway() {
        JwtAuthenticationFilter filter = new JwtAuthenticationFilter(jwtService);
        MockServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.post("/api/internal/users/profiles").build());

        filter.filter(exchange, ignored -> reactor.core.publisher.Mono.empty()).block();

        assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void spoofedUserHeadersAreReplacedByValidatedClaims() {
        JwtAuthenticationFilter filter = new JwtAuthenticationFilter(jwtService);
        String token = crearAccessToken();
        MockServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.get("/api/orders")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .header("X-User-Subject", "otro-usuario")
                        .header("X-User-Email", "otro@example.com")
                        .header("X-User-Roles", "ADMIN")
                        .build());

        GatewayFilterChain chain = exchangeEnviada -> {
            assertThat(exchangeEnviada.getRequest().getHeaders().get("X-User-Subject"))
                    .containsExactly("42");
            assertThat(exchangeEnviada.getRequest().getHeaders().get("X-User-Email"))
                    .containsExactly("cliente@example.com");
            assertThat(exchangeEnviada.getRequest().getHeaders().get("X-User-Roles"))
                    .containsExactly("CLIENTE");
            return reactor.core.publisher.Mono.empty();
        };

        filter.filter(exchange, chain).block();
    }

    @Test
    void publicProductGetDoesNotRequireToken() {
        JwtAuthenticationFilter filter = new JwtAuthenticationFilter(jwtService);
        MockServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.method(HttpMethod.GET, "/api/products").build());
        AtomicBoolean chained = new AtomicBoolean();

        filter.filter(exchange, ignored -> {
            chained.set(true);
            return reactor.core.publisher.Mono.empty();
        }).block();

        assertThat(chained).isTrue();
    }

    @Test
    void userListV2WithoutTokenReturnsUnauthorized() {
        JwtAuthenticationFilter filter = new JwtAuthenticationFilter(jwtService);
        MockServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.method(HttpMethod.GET, "/api/users/v2").build());
        AtomicBoolean chained = new AtomicBoolean();

        filter.filter(exchange, ignored -> {
            chained.set(true);
            return reactor.core.publisher.Mono.empty();
        }).block();

        assertThat(chained).isFalse();
        assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void userLookupByEmailWithoutTokenReturnsUnauthorized() {
        JwtAuthenticationFilter filter = new JwtAuthenticationFilter(jwtService);
        MockServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.method(HttpMethod.GET, "/api/users/email/cliente@gmail.com").build());
        AtomicBoolean chained = new AtomicBoolean();

        filter.filter(exchange, ignored -> {
            chained.set(true);
            return reactor.core.publisher.Mono.empty();
        }).block();

        assertThat(chained).isFalse();
        assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    private String crearAccessToken() {
        byte[] keyBytes = Base64.getDecoder().decode(SECRET);
        SecretKey key = Keys.hmacShaKeyFor(keyBytes);
        Instant now = Instant.now();

        return Jwts.builder()
                .subject("42")
                .claim("email", "cliente@example.com")
                .claim("roles", List.of("CLIENTE"))
                .claim("type", "access")
                .issuer("ferreteria-auth")
                .audience().add("ferreteria-frontend").and()
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusSeconds(300)))
                .signWith(key, Jwts.SIG.HS256)
                .compact();
    }
}