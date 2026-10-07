package com.unsa.taxis.service;

import com.unsa.taxis.exception.OfertaNoDisponibleException;
import com.unsa.taxis.exception.OfertaNoEncontradaException;
import com.unsa.taxis.model.*;
import com.unsa.taxis.repository.OfertaViajeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import com.unsa.taxis.event.OfertaRechazadaEvent;
import com.unsa.taxis.event.OfertaVencidaEvent;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OfertaViajeServiceTest {

    @Mock
    private OfertaViajeRepository ofertaViajeRepository;

    @Mock
    private ChoferService choferService;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    private OfertaViajeService ofertaViajeService;

    private Viaje viaje;
    private Chofer chofer;
    private Chofer otroChofer;

    @BeforeEach
    void setUp() {

        ofertaViajeService = new OfertaViajeService(
                ofertaViajeRepository,
                choferService,
                eventPublisher
        );

        chofer = Chofer.builder()
                .id(1L)
                .nombre("Juan")
                .apellido("Pérez")
                .dni("30111222")
                .telefonoContacto("3875551111")
                .habilitadoAmt(true)
                .estado(EstadoChofer.LIBRE)
                .latitud(-24.7885)
                .longitud(-65.4100)
                .build();

        otroChofer = Chofer.builder()
                .id(2L)
                .nombre("Carlos")
                .apellido("Gómez")
                .dni("30222333")
                .telefonoContacto("3875552222")
                .habilitadoAmt(true)
                .estado(EstadoChofer.LIBRE)
                .latitud(-24.7890)
                .longitud(-65.4110)
                .build();

        viaje = Viaje.builder()
                .id(100L)
                .estado(EstadoViaje.SOLICITADO)
                .chofer(null)
                .build();
    }

    @Test
    void debeCrearOfertaPendienteConFechaDeExpiracion() {

        when(ofertaViajeRepository.save(any(OfertaViaje.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        OffsetDateTime antes = OffsetDateTime.now();

        OfertaViaje oferta =
                ofertaViajeService.crearOferta(viaje, chofer);

        OffsetDateTime despues = OffsetDateTime.now();

        assertNotNull(oferta);
        assertEquals(viaje, oferta.getViaje());
        assertEquals(chofer, oferta.getChofer());
        assertEquals(EstadoOferta.PENDIENTE, oferta.getEstado());
        assertNotNull(oferta.getFechaExpiracion());

        assertTrue(!oferta.getFechaExpiracion().isBefore(
                antes.plusSeconds(10)
        ));

        assertTrue(!oferta.getFechaExpiracion().isAfter(
                despues.plusSeconds(10)
        ));

        verify(ofertaViajeRepository).save(oferta);
    }

    @Test
    void debeAceptarOfertaYAsignarChoferAlViaje() {

        OfertaViaje oferta = OfertaViaje.builder()
                .id(1L)
                .viaje(viaje)
                .chofer(chofer)
                .estado(EstadoOferta.PENDIENTE)
                .fechaEnvio(OffsetDateTime.now())
                .fechaExpiracion(OffsetDateTime.now().plusSeconds(10))
                .build();

        when(ofertaViajeRepository.findById(1L))
                .thenReturn(Optional.of(oferta));

        when(ofertaViajeRepository.findByViajeIdAndEstado(
                viaje.getId(),
                EstadoOferta.PENDIENTE
        )).thenReturn(List.of(oferta));

        ofertaViajeService.aceptarOferta(1L);

        assertEquals(EstadoOferta.ACEPTADA, oferta.getEstado());
        assertEquals(EstadoViaje.ACEPTADO, viaje.getEstado());
        assertEquals(chofer, viaje.getChofer());

        verify(ofertaViajeRepository).findById(1L);

        verify(choferService).cambiarEstado(
                chofer.getId(),
                EstadoChofer.OCUPADO
        );

        verify(ofertaViajeRepository).findByViajeIdAndEstado(
                viaje.getId(),
                EstadoOferta.PENDIENTE
        );

        verify(ofertaViajeRepository, atLeastOnce())
                .save(oferta);
    }

    @Test
    void alAceptarUnaOfertaDebeCancelarLasDemasOfertasPendientes() {

        OfertaViaje ofertaAceptada = OfertaViaje.builder()
                .id(1L)
                .viaje(viaje)
                .chofer(chofer)
                .estado(EstadoOferta.PENDIENTE)
                .fechaEnvio(OffsetDateTime.now())
                .fechaExpiracion(OffsetDateTime.now().plusSeconds(10))
                .build();

        OfertaViaje otraOferta = OfertaViaje.builder()
                .id(2L)
                .viaje(viaje)
                .chofer(otroChofer)
                .estado(EstadoOferta.PENDIENTE)
                .fechaEnvio(OffsetDateTime.now())
                .fechaExpiracion(OffsetDateTime.now().plusSeconds(10))
                .build();

        when(ofertaViajeRepository.findById(1L))
                .thenReturn(Optional.of(ofertaAceptada));

        when(ofertaViajeRepository.findByViajeIdAndEstado(
                viaje.getId(),
                EstadoOferta.PENDIENTE
        )).thenReturn(List.of(
                otraOferta
        ));

        ofertaViajeService.aceptarOferta(1L);

        assertEquals(
                EstadoOferta.ACEPTADA,
                ofertaAceptada.getEstado()
        );

        assertEquals(
                EstadoOferta.CANCELADA,
                otraOferta.getEstado()
        );

        assertEquals(chofer, viaje.getChofer());
        assertEquals(EstadoViaje.ACEPTADO, viaje.getEstado());

        verify(choferService).cambiarEstado(
                chofer.getId(),
                EstadoChofer.OCUPADO
        );

        verify(ofertaViajeRepository).save(ofertaAceptada);
        verify(ofertaViajeRepository).save(otraOferta);
    }

    @Test
    void noDebeAceptarUnaOfertaRechazada() {

        OfertaViaje oferta = OfertaViaje.builder()
                .id(1L)
                .viaje(viaje)
                .chofer(chofer)
                .estado(EstadoOferta.RECHAZADA)
                .fechaEnvio(OffsetDateTime.now())
                .fechaExpiracion(OffsetDateTime.now().plusSeconds(10))
                .build();

        when(ofertaViajeRepository.findById(1L))
                .thenReturn(Optional.of(oferta));

        assertThrows(
                OfertaNoDisponibleException.class,
                () -> ofertaViajeService.aceptarOferta(1L)
        );

        assertEquals(
                EstadoOferta.RECHAZADA,
                oferta.getEstado()
        );

        assertNull(viaje.getChofer());

        assertEquals(
                EstadoViaje.SOLICITADO,
                viaje.getEstado()
        );

        verify(ofertaViajeRepository, never())
                .save(any(OfertaViaje.class));
    }

    @Test
    void noDebeAceptarUnaOfertaVencida() {

        OfertaViaje oferta = OfertaViaje.builder()
                .id(1L)
                .viaje(viaje)
                .chofer(chofer)
                .estado(EstadoOferta.PENDIENTE)
                .fechaEnvio(OffsetDateTime.now().minusSeconds(20))
                .fechaExpiracion(OffsetDateTime.now().minusSeconds(10))
                .build();

        when(ofertaViajeRepository.findById(1L))
                .thenReturn(Optional.of(oferta));

        assertThrows(
                OfertaNoDisponibleException.class,
                () -> ofertaViajeService.aceptarOferta(1L)
        );

        assertEquals(
                EstadoOferta.VENCIDA,
                oferta.getEstado()
        );

        assertNull(viaje.getChofer());

        assertEquals(
                EstadoViaje.SOLICITADO,
                viaje.getEstado()
        );

        verify(ofertaViajeRepository).save(oferta);
    }

    @Test
    void noDebeAceptarOfertaDeUnViajeYaAceptado() {

        Chofer choferAsignado = Chofer.builder()
                .id(3L)
                .nombre("Pedro")
                .apellido("Test")
                .build();

        viaje.setChofer(choferAsignado);
        viaje.setEstado(EstadoViaje.ACEPTADO);

        OfertaViaje oferta = OfertaViaje.builder()
                .id(1L)
                .viaje(viaje)
                .chofer(chofer)
                .estado(EstadoOferta.PENDIENTE)
                .fechaEnvio(OffsetDateTime.now())
                .fechaExpiracion(OffsetDateTime.now().plusSeconds(10))
                .build();

        when(ofertaViajeRepository.findById(1L))
                .thenReturn(Optional.of(oferta));

        assertThrows(
                OfertaNoDisponibleException.class,
                () -> ofertaViajeService.aceptarOferta(1L)
        );

        assertEquals(
                EstadoOferta.PENDIENTE,
                oferta.getEstado()
        );

        assertEquals(
                choferAsignado,
                viaje.getChofer()
        );

        verify(ofertaViajeRepository, never())
                .save(any(OfertaViaje.class));
    }

    @Test
    void debeRechazarOfertaPendiente() {

        OfertaViaje oferta = OfertaViaje.builder()
                .id(1L)
                .viaje(viaje)
                .chofer(chofer)
                .estado(EstadoOferta.PENDIENTE)
                .fechaEnvio(OffsetDateTime.now())
                .fechaExpiracion(OffsetDateTime.now().plusSeconds(10))
                .build();

        when(ofertaViajeRepository.findById(1L))
                .thenReturn(Optional.of(oferta));

        ofertaViajeService.rechazarOferta(1L);

        assertEquals(
                EstadoOferta.RECHAZADA,
                oferta.getEstado()
        );

        verify(ofertaViajeRepository).save(oferta);

        verify(eventPublisher).publishEvent(
                new OfertaRechazadaEvent(oferta.getId())
        );
    }

    @Test
    void noDebeRechazarUnaOfertaCancelada() {

        OfertaViaje oferta = OfertaViaje.builder()
                .id(1L)
                .viaje(viaje)
                .chofer(chofer)
                .estado(EstadoOferta.CANCELADA)
                .fechaEnvio(OffsetDateTime.now())
                .fechaExpiracion(OffsetDateTime.now().plusSeconds(10))
                .build();

        when(ofertaViajeRepository.findById(1L))
                .thenReturn(Optional.of(oferta));

        assertThrows(
                OfertaNoDisponibleException.class,
                () -> ofertaViajeService.rechazarOferta(1L)
        );

        assertEquals(
                EstadoOferta.CANCELADA,
                oferta.getEstado()
        );

        verify(ofertaViajeRepository, never())
                .save(any(OfertaViaje.class));
    }

    @Test
    void noDebeRechazarUnaOfertaVencida()  {
        OfertaViaje oferta = OfertaViaje.builder()
                .id(1L)
                .viaje(viaje)
                .chofer(chofer)
                .estado(EstadoOferta.PENDIENTE)
                .fechaEnvio(OffsetDateTime.now().minusSeconds(20))
                .fechaExpiracion(OffsetDateTime.now().minusSeconds(10))
                .build();

        when(ofertaViajeRepository.findById(1L))
                .thenReturn(Optional.of(oferta));

        assertThrows(
                OfertaNoDisponibleException.class,
                () -> ofertaViajeService.rechazarOferta(1L)
        );

        assertEquals(
                EstadoOferta.VENCIDA,
                oferta.getEstado()
        );

        verify(ofertaViajeRepository)
                .save(oferta);
    }

    @Test
    void debeVencerUnaOfertaExpirada() {

        OfertaViaje oferta = OfertaViaje.builder()
                .id(1L)
                .viaje(viaje)
                .chofer(chofer)
                .estado(EstadoOferta.PENDIENTE)
                .fechaEnvio(OffsetDateTime.now().minusSeconds(20))
                .fechaExpiracion(OffsetDateTime.now().minusSeconds(10))
                .build();

        when(ofertaViajeRepository.findById(1L))
                .thenReturn(Optional.of(oferta));

        ofertaViajeService.vencerOferta(1L);

        assertEquals(
                EstadoOferta.VENCIDA,
                oferta.getEstado()
        );

        verify(ofertaViajeRepository).save(oferta);

        verify(eventPublisher).publishEvent(
                new OfertaVencidaEvent(oferta.getId())
        );
    }

    @Test
    void noDebeVencerUnaOfertaQueTodaviaEstaVigente() {

        OfertaViaje oferta = OfertaViaje.builder()
                .id(1L)
                .viaje(viaje)
                .chofer(chofer)
                .estado(EstadoOferta.PENDIENTE)
                .fechaEnvio(OffsetDateTime.now())
                .fechaExpiracion(OffsetDateTime.now().plusSeconds(10))
                .build();

        when(ofertaViajeRepository.findById(1L))
                .thenReturn(Optional.of(oferta));

        assertThrows(
                OfertaNoDisponibleException.class,
                () -> ofertaViajeService.vencerOferta(1L)
        );

        assertEquals(
                EstadoOferta.PENDIENTE,
                oferta.getEstado()
        );

        verify(ofertaViajeRepository, never())
                .save(any(OfertaViaje.class));
    }

    @Test
    void debeLanzarExcepcionSiLaOfertaNoExiste() {

        when(ofertaViajeRepository.findById(999L))
                .thenReturn(Optional.empty());

        OfertaNoEncontradaException excepcion =
                assertThrows(
                        OfertaNoEncontradaException.class,
                        () -> ofertaViajeService.aceptarOferta(999L)
                );

        assertEquals(
                "No se encontró la oferta con id: 999",
                excepcion.getMessage()
        );

        verify(ofertaViajeRepository)
                .findById(999L);
    }

    @Test
    void debeListarOfertasExpiradas() {

        OfertaViaje ofertaExpirada = OfertaViaje.builder()
                .id(1L)
                .viaje(viaje)
                .chofer(chofer)
                .estado(EstadoOferta.PENDIENTE)
                .fechaExpiracion(
                        OffsetDateTime.now().minusSeconds(10)
                )
                .build();

        when(ofertaViajeRepository
                .findByEstadoAndFechaExpiracionLessThanEqual(
                        eq(EstadoOferta.PENDIENTE),
                        any(OffsetDateTime.class)
                ))
                .thenReturn(List.of(ofertaExpirada));

        List<OfertaViaje> resultado =
                ofertaViajeService.listarOfertasExpiradas();

        assertEquals(1, resultado.size());
        assertEquals(ofertaExpirada, resultado.get(0));

        verify(ofertaViajeRepository)
                .findByEstadoAndFechaExpiracionLessThanEqual(
                        eq(EstadoOferta.PENDIENTE),
                        any(OffsetDateTime.class)
                );
    }


}