package com.ferreteria.product_service.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.server.resource.web.authentication.BearerTokenAuthenticationFilter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.filter.OncePerRequestFilter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;

import java.io.IOException;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;
import java.util.List;

@Configuration
@EnableMethodSecurity // sin esto los @PreAuthorize de los controllers se ignoran en silencio
public class SecurityConfig {
    public static final String INTERNAL_SERVICE_HEADER = "X-Internal-Service";

    // Cadena stateless: la identidad viaja en el JWT que emite auth-service (JWKS en application.yml).
    // Lecturas de productos y categorías públicas; escrituras requieren ADMIN o VENDEDOR.
    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            JwtAuthenticationConverter jwtAuthenticationConverter,
            OncePerRequestFilter internalApiFilter)
            throws Exception {
        return http
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .addFilterBefore(internalApiFilter, BearerTokenAuthenticationFilter.class)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.GET, "/products/**", "/categories/**", "/products/v2/**").permitAll()
                        .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html", "/doc/**").permitAll()
                        .requestMatchers("/actuator/health", "/actuator/info").permitAll()
                        .anyRequest().authenticated())
                .oauth2ResourceServer(oauth2 -> oauth2.jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter)))
                .build();
    }

    @Bean
    public OncePerRequestFilter internalApiFilter(
            @Value("${app.internal.secret:${INTERNAL_API_SECRET:}}") String internalSecret) {
        return new OncePerRequestFilter() {
            @Override
            protected boolean shouldNotFilter(HttpServletRequest request) {
                String path = request.getRequestURI();
                return !(path.contains("/products/code/") &&
                        (path.endsWith("/reserve") || path.endsWith("/release")));
            }

            @Override
            protected void doFilterInternal(HttpServletRequest request,
                                            HttpServletResponse response,
                                            FilterChain filterChain)
                    throws ServletException, IOException {
                if (internalSecret == null || internalSecret.isBlank()
                        || !internalSecret.equals(request.getHeader(INTERNAL_SERVICE_HEADER))) {
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    return;
                }
                // El filtro corre antes del resource server: sin autenticación,
                // /reserve y /release seguirían siendo rechazados con 401.
                SecurityContextHolder.getContext().setAuthentication(
                        new UsernamePasswordAuthenticationToken("internal-service", null,
                                List.of(new SimpleGrantedAuthority("ROLE_INTERNAL"))));
                try {
                    filterChain.doFilter(request, response);
                } finally {
                    SecurityContextHolder.clearContext();
                }
            }
        };
    }

    // auth-service publica los roles en el claim "roles"; Spring por defecto solo mapea "scope"/"scp",
    // así que sin este converter hasRole('ADMIN') nunca coincidiría con los permisos del token.
    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtGrantedAuthoritiesConverter authorities = new JwtGrantedAuthoritiesConverter();
        authorities.setAuthoritiesClaimName("roles");
        authorities.setAuthorityPrefix("ROLE_");
        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(authorities);
        return converter;
    }

    @Bean
    public JwtDecoder jwtDecoder(@Value("${JWT_SECRET:}") String secret) {
        byte[] keyBytes = Base64.getDecoder().decode(secret);
        SecretKeySpec key = new SecretKeySpec(keyBytes, "HmacSHA256");
        return NimbusJwtDecoder.withSecretKey(key).macAlgorithm(MacAlgorithm.HS256).build();
    }
}