package com.ferreteria.cart_service.config;

import org.junit.jupiter.api.Test;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class JwtAuthenticationConverterTest {

    @Test
    void elClaimRolesSeConvierteEnRolConPrefijoUnico() {
        Jwt jwt = Jwt.withTokenValue("token")
                .header("alg", "none")
                .claim("roles", List.of("ADMIN"))
                .build();

        Authentication authentication = new SecurityConfig().jwtAuthenticationConverter().convert(jwt);

        assertThat(authentication.getAuthorities())
                .extracting(GrantedAuthority::getAuthority)
                .containsExactly("ROLE_ADMIN")
                .doesNotContain("ROLE_ROLE_ADMIN");
    }
}
