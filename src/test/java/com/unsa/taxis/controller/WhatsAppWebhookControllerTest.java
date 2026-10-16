package com.unsa.taxis.controller;

import com.unsa.taxis.service.WhatsAppWebhookService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(WhatsAppWebhookController.class)
class WhatsAppWebhookControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private WhatsAppWebhookService whatsAppWebhookService;

    @Test
    void debeAceptarUnaNotificacionDeWhatsApp() throws Exception {

        when(whatsAppWebhookService.procesarNotificacion(anyMap()))
                .thenReturn(
                        new WhatsAppWebhookService.ResultadoNotificacion(
                                java.util.List.of(),
                                0
                        )
                );

        mockMvc.perform(post("/api/whatsapp/webhook")
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {
                                  "object": "whatsapp_business_account"
                                }
                                """))
                .andExpect(status().isOk());

        verify(whatsAppWebhookService).procesarNotificacion(anyMap());
    }
}