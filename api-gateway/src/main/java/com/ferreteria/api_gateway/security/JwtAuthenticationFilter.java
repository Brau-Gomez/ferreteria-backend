package com.ferreteria.api_gateway.security;

import io.jsonwebtoken.Claims;
import java.util.List;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
public class JwtAuthenticationFilter implements GlobalFilter, Ordered {

    private final JwtService jwtService;

    public JwtAuthenticationFilter(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();

        if (esRutaPublica(request)) {
            return chain.filter(exchange);
        }

        String authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return unauthorized(exchange);
        }

        Claims claims = jwtService.obtenerClaimsSiEsValido(authHeader.substring(7));
        if (claims == null) {
            return unauthorized(exchange);
        }

        ServerHttpRequest requestConUsuario = agregarHeadersInternos(request, claims);
        return chain.filter(exchange.mutate().request(requestConUsuario).build());
    }

    @Override
    public int getOrder() {
        return -1;
    }

    private boolean esRutaPublica(ServerHttpRequest request) {
        String path = request.getURI().getPath();

        if (HttpMethod.OPTIONS.equals(request.getMethod())
                || path.startsWith("/actuator/")
                || path.equals("/actuator")
                || path.startsWith("/v3/api-docs")
                || path.startsWith("/swagger-ui")
                || path.equals("/swagger-ui.html")
                || path.equals("/api/auth/login")
                || path.startsWith("/api/auth/login/")
                || path.equals("/api/auth/register")
                || path.startsWith("/api/auth/register/")
                || path.equals("/api/auth/refresh")
                || path.startsWith("/api/auth/refresh/")
                || path.equals("/api/auth/validate")
                || path.startsWith("/api/auth/validate/")
                || path.startsWith("/api/internal/users")
                || path.equals("/api/users/v2")
                || path.startsWith("/api/users/v2/")
                || path.equals("/api/usuarios/v2")
                || path.startsWith("/api/usuarios/v2/")) {
            return true;
        }

        if (HttpMethod.GET.equals(request.getMethod())) {
            return path.startsWith("/api/products/")
                    || path.equals("/api/products")
                    || path.startsWith("/api/categories/")
                    || path.equals("/api/categories")
                    || path.startsWith("/api/images/")
                    || path.startsWith("/api/regions/")
                    || path.equals("/api/regions")
                    || path.startsWith("/api/communes/")
                    || path.equals("/api/communes")
                    || path.startsWith("/api/coverage/")
                    || path.equals("/api/coverage")
                    || path.matches("^/api/users/(?:[^/]+)/exists$")
                    || path.matches("^/api/usuarios/(?:[^/]+)/exists$")
                    || path.startsWith("/api/users/email/")
                    || path.startsWith("/api/usuarios/email/");
        }

        return false;
    }

    private Mono<Void> unauthorized(ServerWebExchange exchange) {
        exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
        return exchange.getResponse().setComplete();
    }

    private ServerHttpRequest agregarHeadersInternos(ServerHttpRequest request, Claims claims) {
        ServerHttpRequest.Builder builder = request.mutate();

        if (claims.getSubject() != null) {
            builder.header("X-User-Subject", claims.getSubject());
        }

        String email = claims.get("email", String.class);
        if (email != null) {
            builder.header("X-User-Email", email);
        }

        List<String> roles = jwtService.obtenerRoles(claims);
        if (!roles.isEmpty()) {
            builder.header("X-User-Roles", String.join(",", roles));
        }

        return builder.build();
    }
}
