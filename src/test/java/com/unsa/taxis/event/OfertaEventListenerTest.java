package com.unsa.taxis.event;

import com.unsa.taxis.model.OfertaViaje;
import com.unsa.taxis.model.Viaje;
import com.unsa.taxis.service.DespachoViajeService;
import com.unsa.taxis.service.OfertaViajeService;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class OfertaEventListenerTest {

    @Mock
    private DespachoViajeService despachoViajeService;

    @Mock
    private OfertaViajeService ofertaViajeService;

    @InjectMocks
    private OfertaEventListener ofertaEventListener;

    @Test
    void debeProcesarOfertaRechazada() {
        Viaje viaje = Viaje.builder()
                .id(10L)
                .build();

        OfertaViaje oferta = OfertaViaje.builder()
                .id(1L)
                .viaje(viaje)
                .build();

        when(ofertaViajeService.buscarPorId(1L))
                .thenReturn(oferta);

        ofertaEventListener.manejarOfertaRechazada(
                new OfertaRechazadaEvent(1L)
        );

        verify(despachoViajeService).continuarDespacho(viaje);
    }

    @Test
    void debeProcesarOfertaVencida() {
        Viaje viaje = Viaje.builder()
                .id(20L)
                .build();

        OfertaViaje oferta = OfertaViaje.builder()
                .id(2L)
                .viaje(viaje)
                .build();

        when(ofertaViajeService.buscarPorId(2L))
                .thenReturn(oferta);

        ofertaEventListener.manejarOfertaVencida(
                new OfertaVencidaEvent(2L)
        );

        verify(despachoViajeService).continuarDespacho(viaje);
    }
}