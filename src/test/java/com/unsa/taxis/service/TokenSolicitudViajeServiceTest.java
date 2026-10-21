package com.unsa.taxis.service;

import com.unsa.taxis.PostgresIntegrationTest;
import com.unsa.taxis.model.TokenSolicitudViaje;
import com.unsa.taxis.repository.TokenSolicitudViajeRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.OffsetDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class TokenSolicitudViajeServiceTest extends PostgresIntegrationTest {

    @Autowired
    private TokenSolicitudViajeService tokenService;

    @Autowired
    private TokenSolicitudViajeRepository tokenRepository;

    @Test
    void debeGenerarTokenYGuardarSoloSuHash() {
        String whatsappId = "5493815550101";

        String token = tokenService.generarToken(whatsappId);

        assertNotNull(token);
        assertFalse(token.isBlank());
        assertEquals(43, token.length());

        String hashEsperado = calcularHash(token);

        TokenSolicitudViaje entidad = tokenRepository.findAll()
                .stream()
                .filter(t -> t.getTokenHash().equals(hashEsperado))
                .findFirst()
                .orElseThrow();

        assertEquals(whatsappId, entidad.getWhatsappId());
        assertNull(entidad.getFechaUso());
        assertTrue(entidad.getFechaExpiracion()
                .isAfter(entidad.getFechaCreacion()));
    }

    @Test
    void debeConsumirTokenValidoYDevolverWhatsAppAsociado() {
        String whatsappId = "5493815550102";

        String token = tokenService.generarToken(whatsappId);

        Optional<String> resultado = tokenService.consumirToken(token);

        assertTrue(resultado.isPresent());
        assertEquals(whatsappId, resultado.get());

        TokenSolicitudViaje entidad = tokenRepository.findAll()
                .stream()
                .filter(t -> t.getTokenHash().equals(calcularHash(token)))
                .findFirst()
                .orElseThrow();

        assertNotNull(entidad.getFechaUso());
    }

    @Test
    void noDebePermitirConsumirTokenDosVeces() {
        String token = tokenService.generarToken("5493815550103");

        assertTrue(tokenService.consumirToken(token).isPresent());
        assertTrue(tokenService.consumirToken(token).isEmpty());
    }

    
    @Test
    void noDebeConsumirTokenVencido() throws InterruptedException {
        String token = tokenService.generarToken("5493815550104");
        String hash = calcularHash(token);

        TokenSolicitudViaje entidad = tokenRepository.findAll()
                .stream()
                .filter(t -> t.getTokenHash().equals(hash))
                .findFirst()
                .orElseThrow();

        // La expiración sigue siendo posterior a la creación.
        entidad.setFechaExpiracion(
                entidad.getFechaCreacion().plusSeconds(1)
        );

        tokenRepository.saveAndFlush(entidad);

        // Esperamos a que venza el token.
        Thread.sleep(1100);

        assertTrue(tokenService.consumirToken(token).isEmpty());

        assertNull(tokenRepository.findById(entidad.getId())
                .orElseThrow()
                .getFechaUso());
    }


    @Test
    void debeRechazarTokenInexistente() {
        assertTrue(tokenService.consumirToken("token-inexistente").isEmpty());
    }

    private String calcularHash(String token) {
        try {
            byte[] hash = java.security.MessageDigest
                    .getInstance("SHA-256")
                    .digest(token.getBytes(java.nio.charset.StandardCharsets.UTF_8));

            return java.util.HexFormat.of().formatHex(hash);
        } catch (java.security.NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }
}
