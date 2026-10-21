package com.unsa.taxis.service;

import com.unsa.taxis.model.TokenSolicitudViaje;
import com.unsa.taxis.repository.TokenSolicitudViajeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.OffsetDateTime;
import java.util.Base64;
import java.util.HexFormat;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class TokenSolicitudViajeService {

    private static final int DURACION_MINUTOS = 15;
    private static final int BYTES_TOKEN = 32;

    private final TokenSolicitudViajeRepository tokenRepository;

    private final SecureRandom secureRandom = new SecureRandom();

    @Transactional
    public String generarToken(String whatsappId) {
        if (whatsappId == null || whatsappId.isBlank()) {
            throw new IllegalArgumentException(
                    "El WhatsApp del pasajero es obligatorio"
            );
        }

        byte[] bytes = new byte[BYTES_TOKEN];
        secureRandom.nextBytes(bytes);

        String token = Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(bytes);

        OffsetDateTime ahora = OffsetDateTime.now();

        TokenSolicitudViaje entidad = TokenSolicitudViaje.builder()
                .tokenHash(calcularHash(token))
                .whatsappId(whatsappId)
                .fechaCreacion(ahora)
                .fechaExpiracion(ahora.plusMinutes(DURACION_MINUTOS))
                .build();

        tokenRepository.save(entidad);

        return token;
    }

    @Transactional
    public Optional<String> consumirToken(String token) {
        if (token == null || token.isBlank()) {
            return Optional.empty();
        }

        String hash = calcularHash(token);

        Optional<TokenSolicitudViaje> resultado =
                tokenRepository.buscarPorHashParaActualizar(hash);

        if (resultado.isEmpty()) {
            return Optional.empty();
        }

        TokenSolicitudViaje entidad = resultado.get();
        OffsetDateTime ahora = OffsetDateTime.now();

        if (entidad.getFechaUso() != null
                || !entidad.getFechaExpiracion().isAfter(ahora)) {
            return Optional.empty();
        }

        entidad.setFechaUso(ahora);

        return Optional.of(entidad.getWhatsappId());
    }

    private String calcularHash(String token) {
        try {
            MessageDigest digest =
                    MessageDigest.getInstance("SHA-256");

            byte[] hash = digest.digest(
                    token.getBytes(StandardCharsets.UTF_8)
            );

            return HexFormat.of().formatHex(hash);

        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(
                    "No se pudo calcular el hash del token", e
            );
        }
    }
}
