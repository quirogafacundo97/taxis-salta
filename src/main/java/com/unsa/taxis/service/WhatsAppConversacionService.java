
package com.unsa.taxis.service;

import com.unsa.taxis.exception.TransicionEstadoViajeException;
import com.unsa.taxis.model.Cliente;
import com.unsa.taxis.repository.ClienteRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.text.Normalizer;
import java.util.Locale;

@Service
public class WhatsAppConversacionService {

    private final WhatsAppService whatsAppService;
    private final ViajeService viajeService;
    private final String urlPaginaPasajero;
    private final TokenSolicitudViajeService tokenSolicitudViajeService;
    private final ClienteRepository clienteRepository;

    public WhatsAppConversacionService(
            WhatsAppService whatsAppService,
            ViajeService viajeService,
            TokenSolicitudViajeService tokenSolicitudViajeService,
            ClienteRepository clienteRepository,
            @Value("${app.pasajero.url}") String urlPaginaPasajero) {

        this.whatsAppService = whatsAppService;
        this.viajeService = viajeService;
        this.tokenSolicitudViajeService = tokenSolicitudViajeService;
        this.clienteRepository = clienteRepository;
        this.urlPaginaPasajero = urlPaginaPasajero;
    }

    public void procesarMensaje(
            WhatsAppWebhookService.MensajeEntrante mensaje) {

        if (mensaje == null || mensaje.remitente() == null) {
            return;
        }

        guardarNombrePerfil(mensaje);

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

        if (textoNormalizado.equals("cancelar")
                || textoNormalizado.equals("cancela")
                || textoNormalizado.equals("cancelar mi viaje")
                || textoNormalizado.equals("quiero cancelar")
                || textoNormalizado.equals("quiero cancelar mi viaje")) {

            try {
                viajeService.cancelarViajePorWhatsapp(mensaje.remitente());

                whatsAppService.enviarMensajeTexto(
                        mensaje.remitente(),
                        "Tu viaje fue cancelado correctamente."
                );

            } catch (TransicionEstadoViajeException e) {

                whatsAppService.enviarMensajeTexto(
                        mensaje.remitente(),
                        e.getMessage()
                );
            }

            return;
        }

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

    private void guardarNombrePerfil(
            WhatsAppWebhookService.MensajeEntrante mensaje) {

        String nombre = mensaje.nombrePerfil();

        if (nombre == null || nombre.isBlank()) {
            return;
        }

        nombre = nombre.trim();

        // La columna Cliente.nombre admite hasta 100 caracteres.
        if (nombre.length() > 100) {
            nombre = nombre.substring(0, 100);
        }

        Cliente cliente = clienteRepository
                .findByWhatsappId(mensaje.remitente())
                .orElseGet(() -> Cliente.builder()
                        .whatsappId(mensaje.remitente())
                        .build());

        if (cliente.getNombre() == null || cliente.getNombre().isBlank()) {
            cliente.setNombre(nombre);
            clienteRepository.save(cliente);
        }
    }

    private String normalizarTexto(String texto) {
        return Normalizer.normalize(texto, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .toLowerCase(Locale.ROOT)
                .trim();
    }
}
