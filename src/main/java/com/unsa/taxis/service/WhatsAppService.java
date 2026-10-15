
package com.unsa.taxis.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.Map;

@Service
public class WhatsAppService {

    private final RestClient restClient;
    private final String apiVersion;
    private final String phoneNumberId;
    private final String accessToken;

    public WhatsAppService(
            @Value("${whatsapp.api.url}") String apiUrl,
            @Value("${whatsapp.api.version}") String apiVersion,
            @Value("${whatsapp.phone-number-id}") String phoneNumberId,
            @Value("${whatsapp.access-token}") String accessToken) {

        this.restClient = RestClient.builder()
                .baseUrl(apiUrl)
                .build();

        this.apiVersion = apiVersion;
        this.phoneNumberId = phoneNumberId;
        this.accessToken = accessToken;
    }

    public void enviarMensajeTexto(String destinatario, String mensaje) {

        Map<String, Object> cuerpo = Map.of(
                "messaging_product", "whatsapp",
                "recipient_type", "individual",
                "to", destinatario,
                "type", "text",
                "text", Map.of(
                        "preview_url", false,
                        "body", mensaje
                )
        );

        restClient.post()
                .uri("/{version}/{phoneNumberId}/messages",
                        apiVersion, phoneNumberId)
                .contentType(MediaType.APPLICATION_JSON)
                .headers(headers ->
                        headers.setBearerAuth(accessToken))
                .body(cuerpo)
                .retrieve()
                .toBodilessEntity();
    }
}