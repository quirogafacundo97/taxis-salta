
package com.unsa.taxis.service;

import com.unsa.taxis.exception.TransicionEstadoViajeException;
import com.unsa.taxis.model.Cliente;
import com.unsa.taxis.repository.ClienteRepository;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;

class WhatsAppConversacionServiceTest {

    @Test
    void debeEnviarEnlaceAlMapaCuandoElPasajeroSolicitaUnTaxi() {
        WhatsAppService whatsAppService = mock(WhatsAppService.class);
        TokenSolicitudViajeService tokenService =
                mock(TokenSolicitudViajeService.class);
        ViajeService viajeService = mock(ViajeService.class);
        ClienteRepository clienteRepository = mock(ClienteRepository.class);

        when(tokenService.generarToken("5493870000000"))
                .thenReturn("tokenPrueba123");

        WhatsAppConversacionService conversacionService =
                new WhatsAppConversacionService(
                        whatsAppService,
                        viajeService,
                        tokenService,
                        clienteRepository,
                        "http://localhost:3000"
                );

        WhatsAppWebhookService.MensajeEntrante mensaje =
                new WhatsAppWebhookService.MensajeEntrante(
                        "5493870000000",
                        "wamid.TEST123",
                        "text",
                        "Quiero un taxi",
                        null
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
        ClienteRepository clienteRepository = mock(ClienteRepository.class);

        WhatsAppConversacionService conversacionService =
                new WhatsAppConversacionService(
                        whatsAppService,
                        viajeService,
                        tokenService,
                        clienteRepository,
                        "http://localhost:3000"
                );

        WhatsAppWebhookService.MensajeEntrante mensaje =
                new WhatsAppWebhookService.MensajeEntrante(
                        "5493870000000",
                        "wamid.TEST124",
                        "text",
                        "¿Cómo funciona el servicio?",
                        null
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
        ClienteRepository clienteRepository = mock(ClienteRepository.class);

        WhatsAppConversacionService conversacionService =
                new WhatsAppConversacionService(
                        whatsAppService,
                        viajeService,
                        tokenService,
                        clienteRepository,
                        "http://localhost:3000"
                );

        WhatsAppWebhookService.MensajeEntrante mensaje =
                new WhatsAppWebhookService.MensajeEntrante(
                        "5493870000000",
                        "wamid.TEST125",
                        "image",
                        null,
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
        ClienteRepository clienteRepository = mock(ClienteRepository.class);

        WhatsAppConversacionService conversacionService =
                new WhatsAppConversacionService(
                        whatsAppService,
                        viajeService,
                        tokenService,
                        clienteRepository,
                        "http://localhost:3000"
                );

        WhatsAppWebhookService.MensajeEntrante mensaje =
                new WhatsAppWebhookService.MensajeEntrante(
                        "5493870000000",
                        "wamid.TEST126",
                        "text",
                        "Cancelar",
                        null
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
        ClienteRepository clienteRepository = mock(ClienteRepository.class);

        doThrow(new TransicionEstadoViajeException(
                "No se puede cancelar el viaje porque ya fue aceptado o está en curso."
        )).when(viajeService).cancelarViajePorWhatsapp("5493870000000");

        WhatsAppConversacionService conversacionService =
                new WhatsAppConversacionService(
                        whatsAppService,
                        viajeService,
                        tokenService,
                        clienteRepository,
                        "http://localhost:3000"
                );

        WhatsAppWebhookService.MensajeEntrante mensaje =
                new WhatsAppWebhookService.MensajeEntrante(
                        "5493870000000",
                        "wamid.TEST127",
                        "text",
                        "Cancelar",
                        null
                );

        conversacionService.procesarMensaje(mensaje);

        verify(viajeService).cancelarViajePorWhatsapp("5493870000000");

        verify(whatsAppService).enviarMensajeTexto(
                "5493870000000",
                "No se puede cancelar el viaje porque ya fue aceptado o está en curso."
        );
    }

    @Test
    void debeGuardarElNombreDelPerfilCuandoElClienteNoExiste() {
        WhatsAppService whatsAppService = mock(WhatsAppService.class);
        TokenSolicitudViajeService tokenService =
                mock(TokenSolicitudViajeService.class);
        ViajeService viajeService = mock(ViajeService.class);
        ClienteRepository clienteRepository = mock(ClienteRepository.class);

        when(clienteRepository.findByWhatsappId("5493870000000"))
                .thenReturn(Optional.empty());

        WhatsAppConversacionService conversacionService =
                new WhatsAppConversacionService(
                        whatsAppService,
                        viajeService,
                        tokenService,
                        clienteRepository,
                        "http://localhost:3000"
                );

        WhatsAppWebhookService.MensajeEntrante mensaje =
                new WhatsAppWebhookService.MensajeEntrante(
                        "5493870000000",
                        "wamid.TEST128",
                        "text",
                        "Hola",
                        "Juan Pérez"
                );

        conversacionService.procesarMensaje(mensaje);

        ArgumentCaptor<Cliente> clienteCaptor =
                ArgumentCaptor.forClass(Cliente.class);

        verify(clienteRepository).save(clienteCaptor.capture());

        assertEquals(
                "5493870000000",
                clienteCaptor.getValue().getWhatsappId()
        );
        assertEquals(
                "Juan Pérez",
                clienteCaptor.getValue().getNombre()
        );
        }
        
        @Test
        void debeConservarElNombreExistenteDelCliente() {
                WhatsAppService whatsAppService = mock(WhatsAppService.class);
                TokenSolicitudViajeService tokenService =
                        mock(TokenSolicitudViajeService.class);
                ViajeService viajeService = mock(ViajeService.class);
                ClienteRepository clienteRepository = mock(ClienteRepository.class);

                Cliente clienteExistente = Cliente.builder()
                        .whatsappId("5493870000000")
                        .nombre("Juan Quiroga")
                        .build();

                when(clienteRepository.findByWhatsappId("5493870000000"))
                        .thenReturn(Optional.of(clienteExistente));

                WhatsAppConversacionService conversacionService =
                        new WhatsAppConversacionService(
                                whatsAppService,
                                viajeService,
                                tokenService,
                                clienteRepository,
                                "http://localhost:3000"
                        );

                WhatsAppWebhookService.MensajeEntrante mensaje =
                        new WhatsAppWebhookService.MensajeEntrante(
                                "5493870000000",
                                "wamid.TEST129",
                                "text",
                                "Hola",
                                "Juan Pérez"
                        );

                conversacionService.procesarMensaje(mensaje);

                assertEquals("Juan Quiroga", clienteExistente.getNombre());

                verify(clienteRepository, never()).save(any(Cliente.class));
        }

        @Test
        void debeCompletarElNombreCuandoElClienteExistenteNoLoTiene() {
                WhatsAppService whatsAppService = mock(WhatsAppService.class);
                TokenSolicitudViajeService tokenService =
                        mock(TokenSolicitudViajeService.class);
                ViajeService viajeService = mock(ViajeService.class);
                ClienteRepository clienteRepository = mock(ClienteRepository.class);

                Cliente clienteExistente = Cliente.builder()
                        .whatsappId("5493870000000")
                        .nombre(null)
                        .build();

                when(clienteRepository.findByWhatsappId("5493870000000"))
                        .thenReturn(Optional.of(clienteExistente));

                WhatsAppConversacionService conversacionService =
                        new WhatsAppConversacionService(
                                whatsAppService,
                                viajeService,
                                tokenService,
                                clienteRepository,
                                "http://localhost:3000"
                        );

                WhatsAppWebhookService.MensajeEntrante mensaje =
                        new WhatsAppWebhookService.MensajeEntrante(
                                "5493870000000",
                                "wamid.TEST130",
                                "text",
                                "Hola",
                                "Juan Pérez"
                        );

                conversacionService.procesarMensaje(mensaje);

                assertEquals("Juan Pérez", clienteExistente.getNombre());

                verify(clienteRepository).save(clienteExistente);
   }
}
