
package com.unsa.taxis.service;

import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.unsa.taxis.exception.TransicionEstadoViajeException;

import static org.mockito.Mockito.doThrow;

class WhatsAppConversacionServiceTest {

    @Test
    void debeEnviarEnlaceAlMapaCuandoElPasajeroSolicitaUnTaxi() {
        WhatsAppService whatsAppService = mock(WhatsAppService.class);
        TokenSolicitudViajeService tokenService =
                mock(TokenSolicitudViajeService.class);

        ViajeService viajeService = mock(ViajeService.class);

        when(tokenService.generarToken("5493870000000"))
                .thenReturn("tokenPrueba123");

        WhatsAppConversacionService conversacionService =
                new WhatsAppConversacionService(
                        whatsAppService,
                        viajeService,
                        tokenService,
                        "http://localhost:3000"
                );

        WhatsAppWebhookService.MensajeEntrante mensaje =
                new WhatsAppWebhookService.MensajeEntrante(
                        "5493870000000",
                        "wamid.TEST123",
                        "text",
                        "Quiero un taxi"
                );

        conversacionService.procesarMensaje(mensaje);

        verify(tokenService).generarToken("5493870000000");

        verify(whatsAppService).enviarMensajeTexto(
                "5493870000000",
                "Para solicitar tu taxi, abrí este enlace y marcá el origen y el destino:\n"
                        + "http://localhost:3000?token=tokenPrueba123"
        );
    }

    @Test
    void debeResponderConInstruccionesCuandoElMensajeNoEsUnaSolicitud() {
        WhatsAppService whatsAppService = mock(WhatsAppService.class);
        TokenSolicitudViajeService tokenService =
                mock(TokenSolicitudViajeService.class);

        ViajeService viajeService = mock(ViajeService.class);

        WhatsAppConversacionService conversacionService =
                new WhatsAppConversacionService(
                        whatsAppService,
                        viajeService,
                        tokenService,
                        "http://localhost:3000"
                );

        WhatsAppWebhookService.MensajeEntrante mensaje =
                new WhatsAppWebhookService.MensajeEntrante(
                        "5493870000000",
                        "wamid.TEST124",
                        "text",
                        "¿Cómo funciona el servicio?"
                );

        conversacionService.procesarMensaje(mensaje);

        verify(whatsAppService).enviarMensajeTexto(
                "5493870000000",
                "Para solicitar un viaje, escribí «Quiero un taxi» y te comparto el enlace para indicar el origen y el destino."
        );
    }

    @Test
    void debeSolicitarTextoCuandoElMensajeNoEsDeTipoTexto() {
        WhatsAppService whatsAppService = mock(WhatsAppService.class);
        TokenSolicitudViajeService tokenService =
                mock(TokenSolicitudViajeService.class);

        ViajeService viajeService = mock(ViajeService.class);

        WhatsAppConversacionService conversacionService =
                new WhatsAppConversacionService(
                        whatsAppService,
                        viajeService,
                        tokenService,
                        "http://localhost:3000"
                );

        WhatsAppWebhookService.MensajeEntrante mensaje =
                new WhatsAppWebhookService.MensajeEntrante(
                        "5493870000000",
                        "wamid.TEST125",
                        "image",
                        null
                );

        conversacionService.procesarMensaje(mensaje);

        verify(whatsAppService).enviarMensajeTexto(
                "5493870000000",
                "Por ahora, escribí «Quiero un taxi» para iniciar una solicitud."
        );
    }

    @Test
    void debeCancelarElViajeCuandoElPasajeroLoSolicitaPorWhatsApp() {

        WhatsAppService whatsAppService = mock(WhatsAppService.class);
        TokenSolicitudViajeService tokenService =
                mock(TokenSolicitudViajeService.class);
        ViajeService viajeService = mock(ViajeService.class);

        WhatsAppConversacionService conversacionService =
                new WhatsAppConversacionService(
                        whatsAppService,
                        viajeService,
                        tokenService,
                        "http://localhost:3000"
                );

        WhatsAppWebhookService.MensajeEntrante mensaje =
                new WhatsAppWebhookService.MensajeEntrante(
                        "5493870000000",
                        "wamid.TEST126",
                        "text",
                        "Cancelar"
                );

        conversacionService.procesarMensaje(mensaje);

        verify(viajeService).cancelarViajePorWhatsapp("5493870000000");

        verify(whatsAppService).enviarMensajeTexto(
                "5493870000000",
                "Tu viaje fue cancelado correctamente."
        );
     }

     @Test
     void debeInformarCuandoNoSePuedeCancelarElViaje() {

        WhatsAppService whatsAppService = mock(WhatsAppService.class);
        TokenSolicitudViajeService tokenService =
                mock(TokenSolicitudViajeService.class);
        ViajeService viajeService = mock(ViajeService.class);

        doThrow(new TransicionEstadoViajeException(
                "No se puede cancelar el viaje porque ya fue aceptado o está en curso."
        )).when(viajeService).cancelarViajePorWhatsapp("5493870000000");

        WhatsAppConversacionService conversacionService =
                new WhatsAppConversacionService(
                        whatsAppService,
                        viajeService,
                        tokenService,
                        "http://localhost:3000"
                );

        WhatsAppWebhookService.MensajeEntrante mensaje =
                new WhatsAppWebhookService.MensajeEntrante(
                        "5493870000000",
                        "wamid.TEST127",
                        "text",
                        "Cancelar"
                );

        conversacionService.procesarMensaje(mensaje);

        verify(viajeService).cancelarViajePorWhatsapp("5493870000000");

        verify(whatsAppService).enviarMensajeTexto(
                "5493870000000",
                "No se puede cancelar el viaje porque ya fue aceptado o está en curso."
        );
     }
}
