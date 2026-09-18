package com.unsa.taxis.scheduler;

import com.unsa.taxis.model.Chofer;
import com.unsa.taxis.model.EstadoOferta;
import com.unsa.taxis.model.OfertaViaje;
import com.unsa.taxis.model.Viaje;
import com.unsa.taxis.service.DespachoViajeService;
import com.unsa.taxis.service.OfertaViajeService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OfertaViajeSchedulerTest {

    @Mock
    private OfertaViajeService ofertaViajeService;

    @Mock
    private DespachoViajeService despachoViajeService;

    private OfertaViajeScheduler scheduler;

    private Viaje viaje;

    private Chofer chofer;

    @BeforeEach
    void setUp() {

        scheduler = new OfertaViajeScheduler(
                ofertaViajeService,
                despachoViajeService
        );

        viaje = Viaje.builder()
                .id(1L)
                .build();

        chofer = Chofer.builder()
                .id(1L)
                .build();
    }

    @Test
    void noDebeHacerNadaSiNoHayOfertasExpiradas() {

        when(ofertaViajeService.listarOfertasExpiradas())
                .thenReturn(List.of());

        scheduler.procesarOfertasExpiradas();

        verify(ofertaViajeService)
                .listarOfertasExpiradas();

        verify(ofertaViajeService, never())
                .vencerOferta(anyLong());

        verify(despachoViajeService, never())
                .continuarDespacho(anyLong());
    }

    @Test
    void debeVencerLaOfertaYContinuarElDespacho() {

        OfertaViaje oferta = OfertaViaje.builder()
                .id(1L)
                .viaje(viaje)
                .chofer(chofer)
                .estado(EstadoOferta.PENDIENTE)
                .build();

        when(ofertaViajeService.listarOfertasExpiradas())
                .thenReturn(List.of(oferta));

        scheduler.procesarOfertasExpiradas();

        verify(ofertaViajeService)
                .vencerOferta(1L);

        verify(despachoViajeService)
                .continuarDespacho(1L);
    }

    @Test
    void debeContinuarDespachoUnaSolaVezSiVariasOfertasPertenecenAlMismoViaje() {

        OfertaViaje oferta1 = OfertaViaje.builder()
                .id(1L)
                .viaje(viaje)
                .chofer(chofer)
                .estado(EstadoOferta.PENDIENTE)
                .build();

        OfertaViaje oferta2 = OfertaViaje.builder()
                .id(2L)
                .viaje(viaje)
                .chofer(Chofer.builder().id(2L).build())
                .estado(EstadoOferta.PENDIENTE)
                .build();

        OfertaViaje oferta3 = OfertaViaje.builder()
                .id(3L)
                .viaje(viaje)
                .chofer(Chofer.builder().id(3L).build())
                .estado(EstadoOferta.PENDIENTE)
                .build();

        when(ofertaViajeService.listarOfertasExpiradas())
                .thenReturn(List.of(
                        oferta1,
                        oferta2,
                        oferta3
                ));

        scheduler.procesarOfertasExpiradas();

        verify(ofertaViajeService)
                .vencerOferta(1L);

        verify(ofertaViajeService)
                .vencerOferta(2L);

        verify(ofertaViajeService)
                .vencerOferta(3L);

        verify(despachoViajeService, times(1))
                .continuarDespacho(1L);
    }

    @Test
    void debeContinuarElDespachoDeCadaViajeUnaVez() {

        Viaje viaje2 = Viaje.builder()
                .id(2L)
                .build();

        OfertaViaje oferta1 = OfertaViaje.builder()
                .id(1L)
                .viaje(viaje)
                .chofer(chofer)
                .estado(EstadoOferta.PENDIENTE)
                .build();

        OfertaViaje oferta2 = OfertaViaje.builder()
                .id(2L)
                .viaje(viaje2)
                .chofer(chofer)
                .estado(EstadoOferta.PENDIENTE)
                .build();

        when(ofertaViajeService.listarOfertasExpiradas())
                .thenReturn(List.of(oferta1, oferta2));

        scheduler.procesarOfertasExpiradas();

        verify(ofertaViajeService)
                .vencerOferta(1L);

        verify(ofertaViajeService)
                .vencerOferta(2L);

        verify(despachoViajeService)
                .continuarDespacho(1L);

        verify(despachoViajeService)
                .continuarDespacho(2L);
    }
}