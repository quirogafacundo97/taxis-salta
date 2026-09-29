package com.unsa.taxis.service;

import com.unsa.taxis.model.Chofer;
import com.unsa.taxis.model.EstadoChofer;
import com.unsa.taxis.repository.ChoferRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.unsa.taxis.exception.ChoferNoEncontradoException;

import java.util.Optional;

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

    @Test
    void debeBuscarChoferPorId() {

        Chofer chofer = Chofer.builder()
                .id(1L)
                .nombre("Juan")
                .apellido("Perez")
                .estado(EstadoChofer.LIBRE)
                .habilitadoAmt(true)
                .build();

        when(choferRepository.findById(1L))
                .thenReturn(Optional.of(chofer));

        Chofer resultado = choferService.buscarPorId(1L);

        assertEquals(chofer, resultado);

        verify(choferRepository).findById(1L);
    }

    @Test
    void debeLanzarExcepcionCuandoNoExisteElChofer() {

        when(choferRepository.findById(999L))
                .thenReturn(Optional.empty());

        ChoferNoEncontradoException exception =
                assertThrows(
                        ChoferNoEncontradoException.class,
                        () -> choferService.buscarPorId(999L)
                );

        assertEquals(
                "No se encontró el chofer con id: 999",
                exception.getMessage()
        );

        verify(choferRepository).findById(999L);
    }

    @Test
    void debeActualizarUbicacionDelChofer() {

        Chofer chofer = Chofer.builder()
                .id(1L)
                .nombre("Juan")
                .apellido("Perez")
                .estado(EstadoChofer.LIBRE)
                .habilitadoAmt(true)
                .latitud(-24.7885)
                .longitud(-65.4100)
                .build();

        when(choferRepository.findById(1L))
                .thenReturn(Optional.of(chofer));

        when(choferRepository.save(chofer))
                .thenReturn(chofer);

        Chofer resultado = choferService.actualizarUbicacion(
                1L,
                -24.7000,
                -65.3000
        );

        assertEquals(-24.7000, resultado.getLatitud());
        assertEquals(-65.3000, resultado.getLongitud());

        verify(choferRepository).findById(1L);
        verify(choferRepository).save(chofer);
    }

    @Test
    void debeLanzarExcepcionAlActualizarUbicacionDeChoferInexistente() {

        when(choferRepository.findById(999L))
                .thenReturn(Optional.empty());

        ChoferNoEncontradoException exception =
                assertThrows(
                        ChoferNoEncontradoException.class,
                        () -> choferService.actualizarUbicacion(
                                999L,
                                -24.7000,
                                -65.3000
                        )
                );

        assertEquals(
                "No se encontró el chofer con id: 999",
                exception.getMessage()
        );

        verify(choferRepository).findById(999L);
        verify(choferRepository, never()).save(any());
    }

    @Test
    void debeCambiarEstadoDelChofer() {

        Chofer chofer = Chofer.builder()
                .id(1L)
                .nombre("Juan")
                .apellido("Perez")
                .estado(EstadoChofer.LIBRE)
                .habilitadoAmt(true)
                .build();

        when(choferRepository.findById(1L))
                .thenReturn(Optional.of(chofer));

        when(choferRepository.save(chofer))
                .thenReturn(chofer);

        Chofer resultado = choferService.cambiarEstado(
                1L,
                EstadoChofer.OCUPADO
        );

        assertEquals(EstadoChofer.OCUPADO, resultado.getEstado());

        verify(choferRepository).findById(1L);
        verify(choferRepository).save(chofer);
    }

    @Test
    void debeLanzarExcepcionAlCambiarEstadoDeChoferInexistente() {

        when(choferRepository.findById(999L))
                .thenReturn(Optional.empty());

        ChoferNoEncontradoException exception =
                assertThrows(
                        ChoferNoEncontradoException.class,
                        () -> choferService.cambiarEstado(
                                999L,
                                EstadoChofer.OCUPADO
                        )
                );

        assertEquals(
                "No se encontró el chofer con id: 999",
                exception.getMessage()
        );

        verify(choferRepository).findById(999L);
        verify(choferRepository, never()).save(any());
    }

    @Test
    void debeListarSoloChoferesDisponibles() {

        Chofer juan = Chofer.builder()
                .id(1L)
                .nombre("Juan")
                .estado(EstadoChofer.LIBRE)
                .habilitadoAmt(true)
                .latitud(-24.7880)
                .longitud(-65.4120)
                .build();

        Chofer carlos = Chofer.builder()
                .id(2L)
                .nombre("Carlos")
                .estado(EstadoChofer.OCUPADO)
                .habilitadoAmt(true)
                .latitud(-24.7850)
                .longitud(-65.4050)
                .build();

        Chofer pedro = Chofer.builder()
                .id(3L)
                .nombre("Pedro")
                .estado(EstadoChofer.LIBRE)
                .habilitadoAmt(false)
                .latitud(-24.7860)
                .longitud(-65.4070)
                .build();

        Chofer miguel = Chofer.builder()
                .id(4L)
                .nombre("Miguel")
                .estado(EstadoChofer.LIBRE)
                .habilitadoAmt(true)
                .build();

        when(choferRepository.findByEstadoAndHabilitadoAmt(
                EstadoChofer.LIBRE,
                true
        )).thenReturn(List.of(juan, miguel));

        List<Chofer> resultado = choferService.listarDisponibles();

        assertEquals(1, resultado.size());
        assertEquals("Juan", resultado.get(0).getNombre());

        verify(choferRepository)
                .findByEstadoAndHabilitadoAmt(
                        EstadoChofer.LIBRE,
                        true
                );
    }

    @Test
    void debeDevolverListaVaciaSiNoHayChoferesDisponibles() {

        when(choferRepository.findByEstadoAndHabilitadoAmt(
                EstadoChofer.LIBRE,
                true
        )).thenReturn(List.of());

        List<Chofer> resultado = choferService.listarDisponibles();

        assertTrue(resultado.isEmpty());

        verify(choferRepository)
                .findByEstadoAndHabilitadoAmt(
                        EstadoChofer.LIBRE,
                        true
                );
    }
}