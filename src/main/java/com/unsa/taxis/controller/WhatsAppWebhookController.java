package com.unsa.taxis.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.unsa.taxis.service.WhatsAppWebhookService;

import java.util.Map;

@RestController
@RequestMapping("/api/whatsapp")
public class WhatsAppWebhookController {

    private static final Logger log =
            LoggerFactory.getLogger(WhatsAppWebhookController.class);

     
    private final WhatsAppWebhookService whatsAppWebhookService;

    public WhatsAppWebhookController(
            WhatsAppWebhookService whatsAppWebhookService) {
        this.whatsAppWebhookService = whatsAppWebhookService;
    }

    @Value("${whatsapp.verify-token}")
    private String verifyToken;

    @GetMapping("/webhook")
    public ResponseEntity<String> verificarWebhook(
            @RequestParam(name = "hub.mode", required = false) String mode,
            @RequestParam(name = "hub.verify_token", required = false) String token,
            @RequestParam(name = "hub.challenge", required = false) String challenge) {

        if ("subscribe".equals(mode)
                && verifyToken.equals(token)
                && challenge != null) {

            log.info("Webhook de WhatsApp verificado correctamente");
            return ResponseEntity.ok(challenge);
        }

        log.warn("Solicitud de verificación del webhook rechazada");
        return ResponseEntity.status(403)
                .body("Verificación rechazada");
    }

    @PostMapping("/webhook")
    public ResponseEntity<Void> recibirMensaje(
            @RequestBody Map<String, Object> payload) {

        log.info("Notificación recibida desde WhatsApp");

    WhatsAppWebhookService.ResultadoNotificacion resultado =
            whatsAppWebhookService.procesarNotificacion(payload);

            log.info(
                    "Notificación procesada: {} mensajes entrantes y {} actualizaciones de estado",
                    resultado.mensajesEntrantes().size(),
                    resultado.notificacionesEstado()
            );

            return ResponseEntity.ok().build();
    }
}