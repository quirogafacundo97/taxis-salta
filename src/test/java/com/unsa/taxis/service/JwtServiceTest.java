
package com.unsa.taxis.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import com.nimbusds.jose.jwk.source.ImmutableSecret;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class JwtServiceTest {

    private JwtService jwtService;

    private static final String SECRET =
            "clave-secreta-exclusiva-para-tests-32-bytes";

    @BeforeEach
    void setUp() {

        SecretKey secretKey = new SecretKeySpec(
                SECRET.getBytes(StandardCharsets.UTF_8),
                "HmacSHA256"
        );

        JwtEncoder jwtEncoder = new NimbusJwtEncoder(
                new ImmutableSecret<>(secretKey)
        );

        JwtDecoder jwtDecoder = NimbusJwtDecoder
                .withSecretKey(secretKey)
                .macAlgorithm(MacAlgorithm.HS256)
                .build();

        jwtService = new JwtService(jwtEncoder, jwtDecoder, 3600000);
    }

    @Test
    void debeGenerarTokenConUsuarioYRoles() {

        String token = jwtService.generarToken(
                "12345678",
                List.of("ROLE_ADMIN", "ROLE_AUDITOR_AMT")
        );

        assertNotNull(token);
        assertFalse(token.isBlank());

        Jwt jwt = jwtService.validarToken(token);

        assertEquals("12345678", jwt.getSubject());
        assertEquals(
                List.of("ROLE_ADMIN", "ROLE_AUDITOR_AMT"),
                jwt.getClaimAsStringList("roles")
        );
        assertEquals("https://taxis-salta", jwt.getIssuer().toString());
        assertNotNull(jwt.getExpiresAt());
    }

    @Test
    void debeGenerarTokenRestringidoParaCambioPassword() {

        String token = jwtService.generarTokenCambioPassword("12345678");

        assertNotNull(token);
        assertFalse(token.isBlank());

        Jwt jwt = jwtService.validarToken(token);

        assertEquals("12345678", jwt.getSubject());

        assertEquals(
                List.of("CAMBIAR_PASSWORD"),
                jwt.getClaimAsStringList("roles")
        );

        assertEquals("https://taxis-salta", jwt.getIssuer().toString());
        assertNotNull(jwt.getExpiresAt());
    }

    @Test
    void debeRechazarTokenManipulado() {

        String token = jwtService.generarToken(
                "12345678",
                List.of("ROLE_ADMIN")
        );

        String tokenManipulado = token.substring(0, token.length() - 2)
                + "xx";

        assertThrows(
                Exception.class,
                () -> jwtService.validarToken(tokenManipulado)
        );
    }
}
