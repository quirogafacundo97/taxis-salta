
package com.unsa.taxis.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.text.Normalizer;
import java.util.Locale;

@Service
public class WhatsAppConversacionService {

    private final WhatsAppService whatsAppService;
    private final String urlPaginaPasajero;
    private final TokenSolicitudViajeService tokenSolicitudViajeService;

    public WhatsAppConversacionService(
            WhatsAppService whatsAppService,
            TokenSolicitudViajeService tokenSolicitudViajeService,
            @Value("${app.pasajero.url}") String urlPaginaPasajero) {

        this.whatsAppService = whatsAppService;
        this.tokenSolicitudViajeService = tokenSolicitudViajeService;
        this.urlPaginaPasajero = urlPaginaPasajero;
    }

    public void procesarMensaje(
            WhatsAppWebhookService.MensajeEntrante mensaje) {

        if (mensaje == null || mensaje.remitente() == null) {
            return;
        }

        if (!"text".equals(mensaje.tipo())
                || mensaje.texto() == null
                || mensaje.texto().isBlank()) {

            whatsAppService.enviarMensajeTexto(
                    mensaje.remitente(),
                    "Por ahora, escribí «Quiero un taxi» para iniciar una solicitud."
            );
            return;
        }

        String textoNormalizado = normalizarTexto(mensaje.texto());

        if (textoNormalizado.contains("taxi")
                || textoNormalizado.contains("hola")
                || textoNormalizado.contains("buen dia")
                || textoNormalizado.contains("buenas")) {

            String token = tokenSolicitudViajeService.generarToken(
                    mensaje.remitente()
            );

            String separador = urlPaginaPasajero.contains("?") ? "&" : "?";
            String enlace = urlPaginaPasajero + separador + "token=" + token;

            whatsAppService.enviarMensajeTexto(
                    mensaje.remitente(),
                    "Para solicitar tu taxi, abrí este enlace y marcá el origen y el destino:\n"
                            + enlace
            );
            return;
        }

        whatsAppService.enviarMensajeTexto(
                mensaje.remitente(),
                "Para solicitar un viaje, escribí «Quiero un taxi» y te comparto el enlace para indicar el origen y el destino."
        );
    }

    private String normalizarTexto(String texto) {
        return Normalizer.normalize(texto, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .toLowerCase(Locale.ROOT)
                .trim();
    }
}
