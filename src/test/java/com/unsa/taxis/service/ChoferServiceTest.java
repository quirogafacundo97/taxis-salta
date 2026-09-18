package com.unsa.taxis.service;

import com.unsa.taxis.model.Chofer;
import com.unsa.taxis.model.EstadoChofer;
import com.unsa.taxis.repository.ChoferRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ChoferServiceTest {

    @Mock
    private ChoferRepository choferRepository;

    private final CalculadorDistanciaService calculadorDistanciaService =
            new CalculadorDistanciaService();

    private ChoferService choferService;

    @BeforeEach
    void setUp() {
        choferService = new ChoferService(
                choferRepository,
                calculadorDistanciaService
        );
    }

    @Test
    void debeOrdenarChoferesPorDistanciaAscendente() {

        Chofer juan = Chofer.builder()
                .id(1L)
                .nombre("Juan")
                .apellido("Perez")
                .estado(EstadoChofer.LIBRE)
                .habilitadoAmt(true)
                .latitud(-24.7880)
                .longitud(-65.4120)
                .build();

        Chofer carlos = Chofer.builder()
                .id(2L)
                .nombre("Carlos")
                .apellido("Gomez")
                .estado(EstadoChofer.LIBRE)
                .habilitadoAmt(true)
                .latitud(-24.7850)
                .longitud(-65.4050)
                .build();

        when(choferRepository.findByEstadoAndHabilitadoAmt(
                EstadoChofer.LIBRE,
                true
        )).thenReturn(List.of(carlos, juan));

        var resultado = choferService.buscarChoferesCercanos(
                -24.7885,
                -65.4100,
                5.0
        );

        assertEquals(2, resultado.size());

        assertEquals("Juan", resultado.get(0).getChofer().getNombre());
        assertEquals("Carlos", resultado.get(1).getChofer().getNombre());

        assertTrue(
                resultado.get(0).getDistanciaMetros()
                        < resultado.get(1).getDistanciaMetros()
        );
    }

    @Test
    void noDebeDevolverChoferesFueraDelRadio() {

        Chofer juan = Chofer.builder()
                .id(1L)
                .nombre("Juan")
                .apellido("Perez")
                .estado(EstadoChofer.LIBRE)
                .habilitadoAmt(true)
                .latitud(-24.7880)
                .longitud(-65.4120)
                .build();

        Chofer carlos = Chofer.builder()
                .id(2L)
                .nombre("Carlos")
                .apellido("Gomez")
                .estado(EstadoChofer.LIBRE)
                .habilitadoAmt(true)
                .latitud(-24.7000)
                .longitud(-65.3000)
                .build();

        when(choferRepository.findByEstadoAndHabilitadoAmt(
                EstadoChofer.LIBRE,
                true
        )).thenReturn(List.of(juan, carlos));

        var resultado = choferService.buscarChoferesCercanos(
                -24.7885,
                -65.4100,
                1.0
        );

        assertEquals(1, resultado.size());
        assertEquals("Juan", resultado.get(0).getChofer().getNombre());
    }

    @Test
    void noDebeDevolverChoferesSinUbicacion() {

        Chofer juan = Chofer.builder()
                .id(1L)
                .nombre("Juan")
                .apellido("Perez")
                .estado(EstadoChofer.LIBRE)
                .habilitadoAmt(true)
                .latitud(-24.7880)
                .longitud(-65.4120)
                .build();

        Chofer carlos = Chofer.builder()
                .id(2L)
                .nombre("Carlos")
                .apellido("Gomez")
                .estado(EstadoChofer.LIBRE)
                .habilitadoAmt(true)
                .build();

        when(choferRepository.findByEstadoAndHabilitadoAmt(
                EstadoChofer.LIBRE,
                true
        )).thenReturn(List.of(juan, carlos));

        var resultado = choferService.buscarChoferesCercanos(
                -24.7885,
                -65.4100,
                5.0
        );

        assertEquals(1, resultado.size());
        assertEquals("Juan", resultado.get(0).getChofer().getNombre());
    }

    @Test
    void debeDevolverListaVaciaSiNoHayChoferesEnElRadio() {

        Chofer juan = Chofer.builder()
                .id(1L)
                .nombre("Juan")
                .apellido("Perez")
                .estado(EstadoChofer.LIBRE)
                .habilitadoAmt(true)
                .latitud(-24.7880)
                .longitud(-65.4120)
                .build();

        when(choferRepository.findByEstadoAndHabilitadoAmt(
                EstadoChofer.LIBRE,
                true
        )).thenReturn(List.of(juan));

        var resultado = choferService.buscarChoferesCercanos(
                -24.7885,
                -65.4100,
                0.05
        );

        assertTrue(resultado.isEmpty());
    }
}