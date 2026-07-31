package com.ecommerce.security;

import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;

class SecurityConfigTest {

    @Test
    void jwtDecoderShouldFallbackWhenIssuerIsUnavailable() {
        SecurityConfig config = new SecurityConfig(
                mock(KeycloakJwtAuthenticationConverter.class),
                "http://localhost:5173"
        );

        JwtDecoder decoder = config.jwtDecoder("http://localhost:8081/realms/ecommerce-realm");

        assertThat(decoder).isNotNull();
        assertThatThrownBy(() -> decoder.decode("header.payload.signature"))
                .isInstanceOf(JwtException.class)
                .hasMessageContaining("JWT decoder is not configured");
    }
}
