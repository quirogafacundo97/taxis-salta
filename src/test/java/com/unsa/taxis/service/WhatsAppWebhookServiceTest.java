package com.unsa.taxis.service;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class WhatsAppWebhookServiceTest {

    private final WhatsAppWebhookService service =
            new WhatsAppWebhookService();

    @Test
    void debeReconocerUnMensajeDeTextoEntrante() {

        Map<String, Object> payload = Map.of(
                "entry", java.util.List.of(
                        Map.of(
                                "changes", java.util.List.of(
                                        Map.of(
                                                "field", "messages",
                                                "value", Map.of(
                                                        "messages", java.util.List.of(
                                                                Map.of(
                                                                        "from", "5493870000000",
                                                                        "id", "wamid.TEST123",
                                                                        "type", "text",
                                                                        "text", Map.of(
                                                                                "body", "Hola, necesito un taxi"
                                                                        )
                                                                )
                                                        )
                                                )
                                        )
                                )
                        )
                )
        );

        var resultado = service.procesarNotificacion(payload);

        assertEquals(1, resultado.mensajesEntrantes().size());
        assertEquals(
                "5493870000000",
                resultado.mensajesEntrantes().get(0).remitente()
        );
        assertEquals(
                "Hola, necesito un taxi",
                resultado.mensajesEntrantes().get(0).texto()
        );
        assertEquals(0, resultado.notificacionesEstado());
    }

    @Test
    void debeReconocerUnaNotificacionDeEstadoSinTratarlaComoMensaje() {

        Map<String, Object> payload = Map.of(
                "entry", java.util.List.of(
                        Map.of(
                                "changes", java.util.List.of(
                                        Map.of(
                                                "field", "messages",
                                                "value", Map.of(
                                                        "statuses", java.util.List.of(
                                                                Map.of(
                                                                        "id", "wamid.TEST123",
                                                                        "status", "delivered",
                                                                        "recipient_id", "5493870000000"
                                                                )
                                                        )
                                                )
                                        )
                                )
                        )
                )
        );

        var resultado = service.procesarNotificacion(payload);

        assertTrue(resultado.mensajesEntrantes().isEmpty());
        assertEquals(1, resultado.notificacionesEstado());
    }

    @Test
    void debeIgnorarUnPayloadVacio() {

        var resultado = service.procesarNotificacion(Map.of());

        assertTrue(resultado.mensajesEntrantes().isEmpty());
        assertEquals(0, resultado.notificacionesEstado());
    }
}