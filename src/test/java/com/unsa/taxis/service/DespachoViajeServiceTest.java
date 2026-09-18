package com.unsa.taxis.service;

import com.unsa.taxis.dto.ChoferCercanoResponse;
import com.unsa.taxis.model.Chofer;
import com.unsa.taxis.model.EstadoOferta;
import com.unsa.taxis.model.OfertaViaje;
import com.unsa.taxis.model.Viaje;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DespachoViajeServiceTest {

    @Mock
    private ChoferService choferService;

    @Mock
    private OfertaViajeService ofertaViajeService;

    @Mock
    private ViajeService viajeService;

    private DespachoViajeService despachoViajeService;

    private Viaje viaje;

    private Chofer chofer1;
    private Chofer chofer2;
    private Chofer chofer3;
    private Chofer chofer4;
    private Chofer chofer5;
    private Chofer chofer6;

    @BeforeEach
    void setUp() {

        despachoViajeService =
                new DespachoViajeService(
                        choferService,
                        ofertaViajeService,
                        viajeService
                );

        viaje = Viaje.builder()
                .id(1L)
                .latitudOrigen(-24.7829)
                .longitudOrigen(-65.4121)
                .build();

        chofer1 = Chofer.builder()
                .id(1L)
                .nombre("Juan")
                .build();

        chofer2 = Chofer.builder()
                .id(2L)
                .nombre("Pedro")
                .build();

        chofer3 = Chofer.builder()
                .id(3L)
                .nombre("Carlos")
                .build();

        chofer4 = Chofer.builder()
                .id(4L)
                .nombre("Luis")
                .build();

        chofer5 = Chofer.builder()
                .id(5L)
                .nombre("Miguel")
                .build();

        chofer6 = Chofer.builder()
                .id(6L)
                .nombre("Roberto")
                .build();
    }

    @Test
    void debeCrearOfertasParaLosTresChoferesMasCercanos() {

        List<ChoferCercanoResponse> choferesCercanos = List.of(

                ChoferCercanoResponse.builder()
                        .chofer(chofer1)
                        .distanciaMetros(500.0)
                        .build(),

                ChoferCercanoResponse.builder()
                        .chofer(chofer2)
                        .distanciaMetros(800.0)
                        .build(),

                ChoferCercanoResponse.builder()
                        .chofer(chofer3)
                        .distanciaMetros(1200.0)
                        .build(),

                ChoferCercanoResponse.builder()
                        .chofer(chofer4)
                        .distanciaMetros(1800.0)
                        .build()
        );

        when(choferService.buscarChoferesCercanos(
                -24.7829,
                -65.4121,
                2.0
        )).thenReturn(choferesCercanos);

        despachoViajeService.despacharViaje(viaje);

        verify(ofertaViajeService)
                .crearOferta(viaje, chofer1);

        verify(ofertaViajeService)
                .crearOferta(viaje, chofer2);

        verify(ofertaViajeService)
                .crearOferta(viaje, chofer3);

        verify(ofertaViajeService, never())
                .crearOferta(viaje, chofer4);

        verify(ofertaViajeService, times(3))
                .crearOferta(eq(viaje), any(Chofer.class));
    }

    @Test
    void debeCrearOfertasParaLosDosChoferesDisponibles() {

        List<ChoferCercanoResponse> choferesCercanos = List.of(

                ChoferCercanoResponse.builder()
                        .chofer(chofer1)
                        .distanciaMetros(500.0)
                        .build(),

                ChoferCercanoResponse.builder()
                        .chofer(chofer2)
                        .distanciaMetros(800.0)
                        .build()
        );

        when(choferService.buscarChoferesCercanos(
                -24.7829,
                -65.4121,
                2.0
        )).thenReturn(choferesCercanos);

        despachoViajeService.despacharViaje(viaje);

        verify(ofertaViajeService)
                .crearOferta(viaje, chofer1);

        verify(ofertaViajeService)
                .crearOferta(viaje, chofer2);

        verify(ofertaViajeService, never())
                .crearOferta(viaje, chofer3);

        verify(ofertaViajeService, never())
                .crearOferta(viaje, chofer4);

        verify(ofertaViajeService, times(2))
                .crearOferta(eq(viaje), any(Chofer.class));
    }

    @Test
    void noDebeCrearOfertasSiNoHayChoferesDisponibles() {

        when(choferService.buscarChoferesCercanos(
                -24.7829,
                -65.4121,
                2.0
        )).thenReturn(List.of());

        despachoViajeService.despacharViaje(viaje);

        verify(ofertaViajeService, never())
                .crearOferta(any(Viaje.class), any(Chofer.class));
    }

    @Test
    void segundaRondaNoDebeOfrecerChoferesQueYaParticiparon() {

        List<ChoferCercanoResponse> choferesCercanos = List.of(

                ChoferCercanoResponse.builder()
                        .chofer(chofer1)
                        .distanciaMetros(500.0)
                        .build(),

                ChoferCercanoResponse.builder()
                        .chofer(chofer2)
                        .distanciaMetros(800.0)
                        .build(),

                ChoferCercanoResponse.builder()
                        .chofer(chofer3)
                        .distanciaMetros(1200.0)
                        .build(),

                ChoferCercanoResponse.builder()
                        .chofer(chofer4)
                        .distanciaMetros(1800.0)
                        .build(),

                ChoferCercanoResponse.builder()
                        .chofer(chofer5)
                        .distanciaMetros(2000.0)
                        .build()
        );

        when(choferService.buscarChoferesCercanos(
                -24.7829,
                -65.4121,
                2.0
        )).thenReturn(choferesCercanos);

        // Simulamos que los tres primeros ya recibieron una oferta
        OfertaViaje oferta1 = OfertaViaje.builder()
                .id(1L)
                .viaje(viaje)
                .chofer(chofer1)
                .estado(EstadoOferta.RECHAZADA)
                .build();

        OfertaViaje oferta2 = OfertaViaje.builder()
                .id(2L)
                .viaje(viaje)
                .chofer(chofer2)
                .estado(EstadoOferta.VENCIDA)
                .build();

        OfertaViaje oferta3 = OfertaViaje.builder()
                .id(3L)
                .viaje(viaje)
                .chofer(chofer3)
                .estado(EstadoOferta.RECHAZADA)
                .build();

        // aca necesitaremos consultar las ofertas existentes

        when(ofertaViajeService.listarOfertasPorViaje(1L))
                .thenReturn(List.of(oferta1, oferta2, oferta3));

        // y excluir a chofer1, chofer2 y chofer3
        despachoViajeService.despacharViaje(viaje);

        verify(ofertaViajeService)
                .crearOferta(viaje, chofer4);

        verify(ofertaViajeService)
                .crearOferta(viaje, chofer5);

        verify(ofertaViajeService, never())
                .crearOferta(viaje, chofer1);

        verify(ofertaViajeService, never())
                .crearOferta(viaje, chofer2);

        verify(ofertaViajeService, never())
                .crearOferta(viaje, chofer3);
    }

    @Test
    void debeAmpliarRadioSiNoQuedanChoferesDisponiblesEnElRadioInicial() {

        List<ChoferCercanoResponse> choferesEnRadioInicial = List.of(

                ChoferCercanoResponse.builder()
                        .chofer(chofer1)
                        .distanciaMetros(500.0)
                        .build(),

                ChoferCercanoResponse.builder()
                        .chofer(chofer2)
                        .distanciaMetros(800.0)
                        .build(),

                ChoferCercanoResponse.builder()
                        .chofer(chofer3)
                        .distanciaMetros(1200.0)
                        .build(),

                ChoferCercanoResponse.builder()
                        .chofer(chofer4)
                        .distanciaMetros(1800.0)
                        .build()
        );

        List<ChoferCercanoResponse> choferesEnRadioAmpliado = List.of(

                ChoferCercanoResponse.builder()
                        .chofer(chofer5)
                        .distanciaMetros(2500.0)
                        .build()
        );

        when(choferService.buscarChoferesCercanos(
                -24.7829,
                -65.4121,
                2.0
        )).thenReturn(choferesEnRadioInicial);

        when(choferService.buscarChoferesCercanos(
                -24.7829,
                -65.4121,
                4.0
        )).thenReturn(choferesEnRadioAmpliado);

        OfertaViaje oferta1 = OfertaViaje.builder()
                .id(1L)
                .viaje(viaje)
                .chofer(chofer1)
                .estado(EstadoOferta.RECHAZADA)
                .build();

        OfertaViaje oferta2 = OfertaViaje.builder()
                .id(2L)
                .viaje(viaje)
                .chofer(chofer2)
                .estado(EstadoOferta.VENCIDA)
                .build();

        OfertaViaje oferta3 = OfertaViaje.builder()
                .id(3L)
                .viaje(viaje)
                .chofer(chofer3)
                .estado(EstadoOferta.RECHAZADA)
                .build();

        OfertaViaje oferta4 = OfertaViaje.builder()
                .id(4L)
                .viaje(viaje)
                .chofer(chofer4)
                .estado(EstadoOferta.VENCIDA)
                .build();

        when(ofertaViajeService.listarOfertasPorViaje(1L))
                .thenReturn(List.of(
                        oferta1,
                        oferta2,
                        oferta3,
                        oferta4
                ));

        despachoViajeService.despacharViaje(viaje);

        verify(ofertaViajeService)
                .crearOferta(viaje, chofer5);

        verify(ofertaViajeService, never())
                .crearOferta(viaje, chofer1);

        verify(ofertaViajeService, never())
                .crearOferta(viaje, chofer2);

        verify(ofertaViajeService, never())
                .crearOferta(viaje, chofer3);

        verify(ofertaViajeService, never())
                .crearOferta(viaje, chofer4);
    }

    @Test
    void noDebeAmpliarRadioSiHayChoferesDisponiblesEnElRadioInicial() {

        List<ChoferCercanoResponse> choferesEnRadioInicial = List.of(

                ChoferCercanoResponse.builder()
                        .chofer(chofer1)
                        .distanciaMetros(1500.0)
                        .build()
        );

        when(choferService.buscarChoferesCercanos(
                -24.7829,
                -65.4121,
                2.0
        )).thenReturn(choferesEnRadioInicial);

        when(ofertaViajeService.listarOfertasPorViaje(1L))
                .thenReturn(List.of());

        despachoViajeService.despacharViaje(viaje);

        verify(ofertaViajeService)
                .crearOferta(viaje, chofer1);

        verify(choferService, never())
                .buscarChoferesCercanos(
                        -24.7829,
                        -65.4121,
                        4.0
                );

        verify(choferService)
                .buscarChoferesCercanos(
                        -24.7829,
                        -65.4121,
                        2.0
                );
    }

    @Test
    void debeOfrecerHastaTresChoferesNuevosAlAmpliarRadio() {

        List<ChoferCercanoResponse> choferesEnRadioInicial = List.of(
                ChoferCercanoResponse.builder()
                        .chofer(chofer1)
                        .distanciaMetros(500.0)
                        .build(),
                ChoferCercanoResponse.builder()
                        .chofer(chofer2)
                        .distanciaMetros(800.0)
                        .build(),
                ChoferCercanoResponse.builder()
                        .chofer(chofer3)
                        .distanciaMetros(1200.0)
                        .build()
        );

        List<ChoferCercanoResponse> choferesEnRadioAmpliado = List.of(
                ChoferCercanoResponse.builder()
                        .chofer(chofer4)
                        .distanciaMetros(2500.0)
                        .build(),
                ChoferCercanoResponse.builder()
                        .chofer(chofer5)
                        .distanciaMetros(3000.0)
                        .build(),
                ChoferCercanoResponse.builder()
                        .chofer(chofer6)
                        .distanciaMetros(3500.0)
                        .build()
        );

        OfertaViaje oferta1 = OfertaViaje.builder()
                .chofer(chofer1)
                .build();

        OfertaViaje oferta2 = OfertaViaje.builder()
                .chofer(chofer2)
                .build();

        OfertaViaje oferta3 = OfertaViaje.builder()
                .chofer(chofer3)
                .build();

        when(choferService.buscarChoferesCercanos(
                -24.7829,
                -65.4121,
                2.0
        )).thenReturn(choferesEnRadioInicial);

        when(choferService.buscarChoferesCercanos(
                -24.7829,
                -65.4121,
                4.0
        )).thenReturn(choferesEnRadioAmpliado);

        when(ofertaViajeService.listarOfertasPorViaje(1L))
                .thenReturn(List.of(
                        oferta1,
                        oferta2,
                        oferta3
                ));

        despachoViajeService.despacharViaje(viaje);

        verify(ofertaViajeService).crearOferta(viaje, chofer4);
        verify(ofertaViajeService).crearOferta(viaje, chofer5);
        verify(ofertaViajeService).crearOferta(viaje, chofer6);

        verify(ofertaViajeService, never())
                .crearOferta(viaje, chofer1);

        verify(ofertaViajeService, never())
                .crearOferta(viaje, chofer2);

        verify(ofertaViajeService, never())
                .crearOferta(viaje, chofer3);
    }

    @Test
    void debeAmpliarRadioHastaSeisKmSiNoHayChoferesNuevosEnCuatroKm() {

        List<ChoferCercanoResponse> choferesEnRadioInicial = List.of(
                ChoferCercanoResponse.builder()
                        .chofer(chofer1)
                        .distanciaMetros(500.0)
                        .build(),
                ChoferCercanoResponse.builder()
                        .chofer(chofer2)
                        .distanciaMetros(800.0)
                        .build(),
                ChoferCercanoResponse.builder()
                        .chofer(chofer3)
                        .distanciaMetros(1200.0)
                        .build()
        );

        List<ChoferCercanoResponse> choferesEnRadioAmpliado = List.of(
                ChoferCercanoResponse.builder()
                        .chofer(chofer4)
                        .distanciaMetros(2500.0)
                        .build(),
                ChoferCercanoResponse.builder()
                        .chofer(chofer5)
                        .distanciaMetros(3000.0)
                        .build()
        );

        List<ChoferCercanoResponse> choferesEnRadioMaximo = List.of(
                ChoferCercanoResponse.builder()
                        .chofer(chofer6)
                        .distanciaMetros(5000.0)
                        .build()
        );

        OfertaViaje oferta1 = OfertaViaje.builder()
                .chofer(chofer1)
                .build();

        OfertaViaje oferta2 = OfertaViaje.builder()
                .chofer(chofer2)
                .build();

        OfertaViaje oferta3 = OfertaViaje.builder()
                .chofer(chofer3)
                .build();

        OfertaViaje oferta4 = OfertaViaje.builder()
                .chofer(chofer4)
                .build();

        OfertaViaje oferta5 = OfertaViaje.builder()
                .chofer(chofer5)
                .build();

        when(choferService.buscarChoferesCercanos(
                -24.7829,
                -65.4121,
                2.0
        )).thenReturn(choferesEnRadioInicial);

        when(choferService.buscarChoferesCercanos(
                -24.7829,
                -65.4121,
                4.0
        )).thenReturn(choferesEnRadioAmpliado);

        when(choferService.buscarChoferesCercanos(
                -24.7829,
                -65.4121,
                6.0
        )).thenReturn(choferesEnRadioMaximo);

        when(ofertaViajeService.listarOfertasPorViaje(1L))
                .thenReturn(List.of(
                        oferta1,
                        oferta2,
                        oferta3,
                        oferta4,
                        oferta5
                ));

        despachoViajeService.despacharViaje(viaje);

        verify(ofertaViajeService)
                .crearOferta(viaje, chofer6);

        verify(ofertaViajeService, never())
                .crearOferta(viaje, chofer1);

        verify(ofertaViajeService, never())
                .crearOferta(viaje, chofer2);

        verify(ofertaViajeService, never())
                .crearOferta(viaje, chofer3);

        verify(ofertaViajeService, never())
                .crearOferta(viaje, chofer4);

        verify(ofertaViajeService, never())
                .crearOferta(viaje, chofer5);
    }

    @Test
    void debeCrearNuevaRondaCuandoTodasLasOfertasTerminaron() {

        List<ChoferCercanoResponse> choferesCercanos = List.of(

                ChoferCercanoResponse.builder()
                        .chofer(chofer1)
                        .distanciaMetros(500.0)
                        .build(),

                ChoferCercanoResponse.builder()
                        .chofer(chofer2)
                        .distanciaMetros(800.0)
                        .build(),

                ChoferCercanoResponse.builder()
                        .chofer(chofer3)
                        .distanciaMetros(1200.0)
                        .build(),

                ChoferCercanoResponse.builder()
                        .chofer(chofer4)
                        .distanciaMetros(1500.0)
                        .build(),

                ChoferCercanoResponse.builder()
                        .chofer(chofer5)
                        .distanciaMetros(1800.0)
                        .build(),

                ChoferCercanoResponse.builder()
                        .chofer(chofer6)
                        .distanciaMetros(2000.0)
                        .build()
        );

        OfertaViaje oferta1 = OfertaViaje.builder()
                .id(1L)
                .viaje(viaje)
                .chofer(chofer1)
                .estado(EstadoOferta.RECHAZADA)
                .build();

        OfertaViaje oferta2 = OfertaViaje.builder()
                .id(2L)
                .viaje(viaje)
                .chofer(chofer2)
                .estado(EstadoOferta.VENCIDA)
                .build();

        OfertaViaje oferta3 = OfertaViaje.builder()
                .id(3L)
                .viaje(viaje)
                .chofer(chofer3)
                .estado(EstadoOferta.RECHAZADA)
                .build();

        when(ofertaViajeService.listarOfertasPorViaje(1L))
                .thenReturn(List.of(oferta1, oferta2, oferta3));

        when(choferService.buscarChoferesCercanos(
                viaje.getLatitudOrigen(),
                viaje.getLongitudOrigen(),
                2.0
        )).thenReturn(choferesCercanos);

        despachoViajeService.continuarDespacho(viaje);

        verify(ofertaViajeService)
                .crearOferta(viaje, chofer4);

        verify(ofertaViajeService)
                .crearOferta(viaje, chofer5);

        verify(ofertaViajeService)
                .crearOferta(viaje, chofer6);

        verify(ofertaViajeService, never())
                .crearOferta(viaje, chofer1);

        verify(ofertaViajeService, never())
                .crearOferta(viaje, chofer2);

        verify(ofertaViajeService, never())
                .crearOferta(viaje, chofer3);

        verify(ofertaViajeService, times(3))
                .crearOferta(eq(viaje), any(Chofer.class));
    }

    @Test
    void noDebeCrearNuevaRondaSiHayUnaOfertaPendiente() {

        OfertaViaje oferta1 = OfertaViaje.builder()
                .id(1L)
                .viaje(viaje)
                .chofer(chofer1)
                .estado(EstadoOferta.RECHAZADA)
                .build();

        OfertaViaje oferta2 = OfertaViaje.builder()
                .id(2L)
                .viaje(viaje)
                .chofer(chofer2)
                .estado(EstadoOferta.PENDIENTE)
                .build();

        OfertaViaje oferta3 = OfertaViaje.builder()
                .id(3L)
                .viaje(viaje)
                .chofer(chofer3)
                .estado(EstadoOferta.VENCIDA)
                .build();

        when(ofertaViajeService.listarOfertasPorViaje(1L))
                .thenReturn(List.of(oferta1, oferta2, oferta3));

        despachoViajeService.continuarDespacho(viaje);

        verify(ofertaViajeService, never())
                .crearOferta(eq(viaje), any(Chofer.class));

        verifyNoInteractions(choferService);
    }

    @Test
    void continuarDespachoDebeAmpliarRadioDeDosAKuatroKm() {

        OfertaViaje oferta1 = OfertaViaje.builder()
                .id(1L)
                .viaje(viaje)
                .chofer(chofer1)
                .estado(EstadoOferta.RECHAZADA)
                .build();

        OfertaViaje oferta2 = OfertaViaje.builder()
                .id(2L)
                .viaje(viaje)
                .chofer(chofer2)
                .estado(EstadoOferta.VENCIDA)
                .build();

        OfertaViaje oferta3 = OfertaViaje.builder()
                .id(3L)
                .viaje(viaje)
                .chofer(chofer3)
                .estado(EstadoOferta.RECHAZADA)
                .build();

        when(ofertaViajeService.listarOfertasPorViaje(1L))
                .thenReturn(List.of(oferta1, oferta2, oferta3));

        when(choferService.buscarChoferesCercanos(
                viaje.getLatitudOrigen(),
                viaje.getLongitudOrigen(),
                2.0
        )).thenReturn(List.of(
                ChoferCercanoResponse.builder()
                        .chofer(chofer1)
                        .distanciaMetros(500.0)
                        .build(),
                ChoferCercanoResponse.builder()
                        .chofer(chofer2)
                        .distanciaMetros(800.0)
                        .build(),
                ChoferCercanoResponse.builder()
                        .chofer(chofer3)
                        .distanciaMetros(1200.0)
                        .build()
        ));

        when(choferService.buscarChoferesCercanos(
                viaje.getLatitudOrigen(),
                viaje.getLongitudOrigen(),
                4.0
        )).thenReturn(List.of(
                ChoferCercanoResponse.builder()
                        .chofer(chofer4)
                        .distanciaMetros(2500.0)
                        .build(),
                ChoferCercanoResponse.builder()
                        .chofer(chofer5)
                        .distanciaMetros(3000.0)
                        .build()
        ));

        despachoViajeService.continuarDespacho(viaje);

        verify(ofertaViajeService)
                .crearOferta(viaje, chofer4);

        verify(ofertaViajeService)
                .crearOferta(viaje, chofer5);

        verify(ofertaViajeService, never())
                .crearOferta(viaje, chofer1);

        verify(ofertaViajeService, never())
                .crearOferta(viaje, chofer2);

        verify(ofertaViajeService, never())
                .crearOferta(viaje, chofer3);

        verify(choferService)
                .buscarChoferesCercanos(
                        viaje.getLatitudOrigen(),
                        viaje.getLongitudOrigen(),
                        2.0
                );

        verify(choferService)
                .buscarChoferesCercanos(
                        viaje.getLatitudOrigen(),
                        viaje.getLongitudOrigen(),
                        4.0
                );
    }

    @Test
    void continuarDespachoDebeAmpliarRadioHastaSeisKm() {

        OfertaViaje oferta1 = OfertaViaje.builder()
                .id(1L)
                .viaje(viaje)
                .chofer(chofer1)
                .estado(EstadoOferta.RECHAZADA)
                .build();

        OfertaViaje oferta2 = OfertaViaje.builder()
                .id(2L)
                .viaje(viaje)
                .chofer(chofer2)
                .estado(EstadoOferta.VENCIDA)
                .build();

        OfertaViaje oferta3 = OfertaViaje.builder()
                .id(3L)
                .viaje(viaje)
                .chofer(chofer3)
                .estado(EstadoOferta.RECHAZADA)
                .build();

        OfertaViaje oferta4 = OfertaViaje.builder()
                .id(4L)
                .viaje(viaje)
                .chofer(chofer4)
                .estado(EstadoOferta.VENCIDA)
                .build();

        OfertaViaje oferta5 = OfertaViaje.builder()
                .id(5L)
                .viaje(viaje)
                .chofer(chofer5)
                .estado(EstadoOferta.RECHAZADA)
                .build();

        when(ofertaViajeService.listarOfertasPorViaje(1L))
                .thenReturn(List.of(
                        oferta1,
                        oferta2,
                        oferta3,
                        oferta4,
                        oferta5
                ));

        // A 2 km solamente aparecen choferes que ya recibieron oferta.
        when(choferService.buscarChoferesCercanos(
                viaje.getLatitudOrigen(),
                viaje.getLongitudOrigen(),
                2.0
        )).thenReturn(List.of(
                ChoferCercanoResponse.builder()
                        .chofer(chofer1)
                        .distanciaMetros(500.0)
                        .build(),
                ChoferCercanoResponse.builder()
                        .chofer(chofer2)
                        .distanciaMetros(800.0)
                        .build(),
                ChoferCercanoResponse.builder()
                        .chofer(chofer3)
                        .distanciaMetros(1200.0)
                        .build()
        ));

        // A 4 km también solamente aparecen choferes
        // que ya recibieron oferta.
        when(choferService.buscarChoferesCercanos(
                viaje.getLatitudOrigen(),
                viaje.getLongitudOrigen(),
                4.0
        )).thenReturn(List.of(
                ChoferCercanoResponse.builder()
                        .chofer(chofer4)
                        .distanciaMetros(2500.0)
                        .build(),
                ChoferCercanoResponse.builder()
                        .chofer(chofer5)
                        .distanciaMetros(3000.0)
                        .build()
        ));

        // Recién a 6 km aparece un chofer nuevo.
        when(choferService.buscarChoferesCercanos(
                viaje.getLatitudOrigen(),
                viaje.getLongitudOrigen(),
                6.0
        )).thenReturn(List.of(
                ChoferCercanoResponse.builder()
                        .chofer(chofer6)
                        .distanciaMetros(5200.0)
                        .build()
        ));

        despachoViajeService.continuarDespacho(viaje);

        // Solamente el chofer nuevo de 6 km debe recibir la oferta.
        verify(ofertaViajeService)
                .crearOferta(viaje, chofer6);

        verify(ofertaViajeService, never())
                .crearOferta(viaje, chofer1);

        verify(ofertaViajeService, never())
                .crearOferta(viaje, chofer2);

        verify(ofertaViajeService, never())
                .crearOferta(viaje, chofer3);

        verify(ofertaViajeService, never())
                .crearOferta(viaje, chofer4);

        verify(ofertaViajeService, never())
                .crearOferta(viaje, chofer5);

        // Comprobamos que realmente recorrió los tres radios.
        verify(choferService)
                .buscarChoferesCercanos(
                        viaje.getLatitudOrigen(),
                        viaje.getLongitudOrigen(),
                        2.0
                );

        verify(choferService)
                .buscarChoferesCercanos(
                        viaje.getLatitudOrigen(),
                        viaje.getLongitudOrigen(),
                        4.0
                );

        verify(choferService)
                .buscarChoferesCercanos(
                        viaje.getLatitudOrigen(),
                        viaje.getLongitudOrigen(),
                        6.0
                );

        verify(ofertaViajeService)
                .crearOferta(viaje, chofer6);

    }

    @Test
    void noDebeContinuarDespachoSiElViajeYaTieneChofer() {

        viaje.setChofer(chofer1);

        despachoViajeService.continuarDespacho(viaje);

        verifyNoInteractions(ofertaViajeService);
        verifyNoInteractions(choferService);
    }

    @Test
    void noDebeDespacharViajeSiYaTieneChofer() {

        viaje.setChofer(chofer1);

        despachoViajeService.despacharViaje(viaje);

        verifyNoInteractions(ofertaViajeService);
        verifyNoInteractions(choferService);
    }

    @Test
    void debeContinuarDespachoBuscandoElViajePorId() {

        when(viajeService.buscarPorId(1L))
                .thenReturn(viaje);

        when(ofertaViajeService.listarOfertasPorViaje(1L))
                .thenReturn(List.of());

        when(choferService.buscarChoferesCercanos(
                viaje.getLatitudOrigen(),
                viaje.getLongitudOrigen(),
                2.0
        )).thenReturn(List.of(
                ChoferCercanoResponse.builder()
                        .chofer(chofer1)
                        .distanciaMetros(500.0)
                        .build()
        ));

        despachoViajeService.continuarDespacho(1L);

        verify(viajeService)
                .buscarPorId(1L);

        verify(ofertaViajeService)
                .crearOferta(viaje, chofer1);
    }
}

