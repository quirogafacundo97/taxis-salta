package com.unsa.taxis.service;

import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.OffsetDateTime;
import java.util.Base64;
import java.util.HexFormat;

@Service
public class TokenSeguimientoService {

    private static final int BYTES_TOKEN = 32;
    private static final int DURACION_HORAS = 24;

    private final SecureRandom secureRandom = new SecureRandom();

    public DatosTokenSeguimiento generarToken() {

        byte[] bytes = new byte[BYTES_TOKEN];
        secureRandom.nextBytes(bytes);

        String token = Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(bytes);

        String hash = calcularHash(token);

        OffsetDateTime fechaExpiracion =
                OffsetDateTime.now().plusHours(DURACION_HORAS);

        return new DatosTokenSeguimiento(
                token,
                hash,
                fechaExpiracion
        );
    }

    public String calcularHash(String token) {
        try {
            MessageDigest digest =
                    MessageDigest.getInstance("SHA-256");

            byte[] hash = digest.digest(
                    token.getBytes(StandardCharsets.UTF_8)
            );

            return HexFormat.of().formatHex(hash);

        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(
                    "No se pudo calcular el hash del token de seguimiento",
                    e
            );
        }
    }

    public record DatosTokenSeguimiento(
            String token,
            String hash,
            OffsetDateTime fechaExpiracion
    ) {
    }
}