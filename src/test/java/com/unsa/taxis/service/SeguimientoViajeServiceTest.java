
package com.unsa.taxis.service;

import com.unsa.taxis.exception.SeguimientoViajeNoDisponibleException;
import com.unsa.taxis.model.*;
import com.unsa.taxis.repository.AsignacionChoferVehiculoRepository;
import com.unsa.taxis.repository.ViajeRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.OffsetDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SeguimientoViajeServiceTest {

    @Mock
    private ViajeRepository viajeRepository;

    @Mock
    private TokenSeguimientoService tokenSeguimientoService;

    @Mock
    private AsignacionChoferVehiculoRepository asignacionRepository;

    @InjectMocks
    private SeguimientoViajeService seguimientoViajeService;

    private static final String TOKEN = "tokenSeguimientoPrueba";
    private static final String HASH = "hashSeguimientoPrueba";

    private Viaje crearViaje(EstadoViaje estado) {
        return Viaje.builder()
                .id(1L)
                .estado(estado)
                .tokenSeguimientoExpiraEn(OffsetDateTime.now().plusHours(1))
                .build();
    }

    private void configurarTokenValido(Viaje viaje) {
        when(tokenSeguimientoService.calcularHash(TOKEN))
                .thenReturn(HASH);

        when(viajeRepository.findByTokenSeguimientoHash(HASH))
                .thenReturn(Optional.of(viaje));
    }

    @Test
    void debeDevolverSoloEstadoCuandoViajeEstaSolicitado() {
        Viaje viaje = crearViaje(EstadoViaje.SOLICITADO);
        configurarTokenValido(viaje);

        var resultado = seguimientoViajeService.consultarSeguimiento(TOKEN);

        assertEquals(EstadoViaje.SOLICITADO, resultado.getEstado());
        assertNull(resultado.getChoferNombre());
        assertNull(resultado.getVehiculoPatente());

        verifyNoInteractions(asignacionRepository);
    }

    @ParameterizedTest
    @EnumSource(value = EstadoViaje.class, names = {"ACEPTADO", "EN_CURSO"})
    void debeDevolverDatosDeChoferYVehiculoCuandoViajeEstaAsignado(
            EstadoViaje estado) {

        Chofer chofer = Chofer.builder()
                .id(10L)
                .nombre("Juan")
                .apellido("Pérez")
                .build();

        Vehiculo vehiculo = Vehiculo.builder()
                .id(20L)
                .marca("Fiat")
                .modelo("Cronos")
                .patente("AB123CD")
                .licencia("1234")
                .build();

        AsignacionChoferVehiculo asignacion =
                AsignacionChoferVehiculo.builder()
                        .id(30L)
                        .chofer(chofer)
                        .vehiculo(vehiculo)
                        .build();

        Viaje viaje = crearViaje(estado);
        viaje.setChofer(chofer);

        configurarTokenValido(viaje);

        when(asignacionRepository.findByChoferAndFechaFinIsNull(chofer))
                .thenReturn(Optional.of(asignacion));

        var resultado = seguimientoViajeService.consultarSeguimiento(TOKEN);

        assertEquals(estado, resultado.getEstado());
        assertEquals("Juan", resultado.getChoferNombre());
        assertEquals("Pérez", resultado.getChoferApellido());
        assertEquals("Fiat", resultado.getVehiculoMarca());
        assertEquals("Cronos", resultado.getVehiculoModelo());
        assertEquals("AB123CD", resultado.getVehiculoPatente());
        assertEquals("1234", resultado.getVehiculoLicencia());
    }

    @Test
    void debeRechazarTokenNulo() {
        assertThrows(
                SeguimientoViajeNoDisponibleException.class,
                () -> seguimientoViajeService.consultarSeguimiento(null)
        );

        verifyNoInteractions(tokenSeguimientoService, viajeRepository);
    }

    @Test
    void debeRechazarTokenVacio() {
        assertThrows(
                SeguimientoViajeNoDisponibleException.class,
                () -> seguimientoViajeService.consultarSeguimiento(" ")
        );
    }

    @Test
    void debeRechazarTokenInexistente() {
        when(tokenSeguimientoService.calcularHash(TOKEN))
                .thenReturn(HASH);

        when(viajeRepository.findByTokenSeguimientoHash(HASH))
                .thenReturn(Optional.empty());

        assertThrows(
                SeguimientoViajeNoDisponibleException.class,
                () -> seguimientoViajeService.consultarSeguimiento(TOKEN)
        );
    }

    @Test
    void debeRechazarTokenVencido() {
        Viaje viaje = crearViaje(EstadoViaje.SOLICITADO);
        viaje.setTokenSeguimientoExpiraEn(OffsetDateTime.now().minusMinutes(1));

        configurarTokenValido(viaje);

        assertThrows(
                SeguimientoViajeNoDisponibleException.class,
                () -> seguimientoViajeService.consultarSeguimiento(TOKEN)
        );
    }

    @ParameterizedTest
    @EnumSource(value = EstadoViaje.class, names = {"FINALIZADO", "CANCELADO"})
    void debeRechazarViajesFinalizadosOCancelados(EstadoViaje estado) {
        Viaje viaje = crearViaje(estado);
        configurarTokenValido(viaje);

        assertThrows(
                SeguimientoViajeNoDisponibleException.class,
                () -> seguimientoViajeService.consultarSeguimiento(TOKEN)
        );
    }

    @Test
    void debeRechazarViajeAceptadoSinChofer() {
        Viaje viaje = crearViaje(EstadoViaje.ACEPTADO);
        configurarTokenValido(viaje);

        assertThrows(
                SeguimientoViajeNoDisponibleException.class,
                () -> seguimientoViajeService.consultarSeguimiento(TOKEN)
        );

        verifyNoInteractions(asignacionRepository);
    }

    @Test
    void debeRechazarChoferSinAsignacionVigente() {
        Chofer chofer = Chofer.builder()
                .id(10L)
                .nombre("Juan")
                .apellido("Pérez")
                .build();

        Viaje viaje = crearViaje(EstadoViaje.ACEPTADO);
        viaje.setChofer(chofer);

        configurarTokenValido(viaje);

        when(asignacionRepository.findByChoferAndFechaFinIsNull(chofer))
                .thenReturn(Optional.empty());

        assertThrows(
                SeguimientoViajeNoDisponibleException.class,
                () -> seguimientoViajeService.consultarSeguimiento(TOKEN)
        );
    }
}
