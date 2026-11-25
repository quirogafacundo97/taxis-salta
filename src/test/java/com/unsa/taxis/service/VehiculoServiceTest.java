
package com.unsa.taxis.service;

import com.unsa.taxis.dto.ActualizarVehiculoRequest;
import com.unsa.taxis.dto.CrearVehiculoRequest;
import com.unsa.taxis.dto.VehiculoDTO;
import com.unsa.taxis.exception.LicenciaVehiculoDuplicadaException;
import com.unsa.taxis.exception.NroRelojFullmarDuplicadoException;
import com.unsa.taxis.exception.PatenteVehiculoDuplicadaException;
import com.unsa.taxis.exception.PropietarioNoEncontradoException;
import com.unsa.taxis.exception.VehiculoNoEncontradoException;
import com.unsa.taxis.model.Propietario;
import com.unsa.taxis.model.Vehiculo;
import com.unsa.taxis.repository.PropietarioRepository;
import com.unsa.taxis.repository.VehiculoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VehiculoServiceTest {

    @Mock
    private VehiculoRepository vehiculoRepository;

    @Mock
    private PropietarioRepository propietarioRepository;

    private VehiculoService vehiculoService;

    @BeforeEach
    void setUp() {
        vehiculoService = new VehiculoService(
                vehiculoRepository,
                propietarioRepository
        );
    }

    private Propietario crearPropietario() {
        return Propietario.builder()
                .id(1L)
                .nombre("Juan")
                .apellido("Perez")
                .dni("30123456")
                .build();
    }

    private Vehiculo crearVehiculo() {
        return Vehiculo.builder()
                .id(1L)
                .patente("ABC123")
                .licencia("LIC-001")
                .marca("Toyota")
                .modelo("Etios")
                .anio(2020)
                .nroRelojFullmar("FULL-001")
                .propietario(crearPropietario())
                .build();
    }

    private CrearVehiculoRequest crearRequest() {
        return new CrearVehiculoRequest(
                "ABC123",
                "LIC-001",
                "Toyota",
                "Etios",
                2020,
                "FULL-001",
                1L
        );
    }

    private ActualizarVehiculoRequest actualizarRequest() {
        return new ActualizarVehiculoRequest(
                "ABC123",
                "LIC-001",
                "Toyota",
                "Etios",
                2020,
                "FULL-001",
                1L
        );
    }

    @Test
    void debeListarVehiculos() {
        Vehiculo vehiculo = crearVehiculo();

        when(vehiculoRepository.findAll())
                .thenReturn(List.of(vehiculo));

        List<VehiculoDTO> resultado = vehiculoService.listarTodos();

        assertEquals(1, resultado.size());
        assertEquals("ABC123", resultado.get(0).getPatente());
        assertEquals("LIC-001", resultado.get(0).getLicencia());
        assertEquals(1L, resultado.get(0).getPropietarioId());
        assertEquals("Juan", resultado.get(0).getPropietarioNombre());

        verify(vehiculoRepository).findAll();
    }

    @Test
    void debeDevolverListaVaciaSiNoHayVehiculos() {
        when(vehiculoRepository.findAll())
                .thenReturn(List.of());

        List<VehiculoDTO> resultado = vehiculoService.listarTodos();

        assertTrue(resultado.isEmpty());

        verify(vehiculoRepository).findAll();
    }

    @Test
    void debeBuscarVehiculoPorId() {
        Vehiculo vehiculo = crearVehiculo();

        when(vehiculoRepository.findById(1L))
                .thenReturn(Optional.of(vehiculo));

        VehiculoDTO resultado = vehiculoService.buscarPorId(1L);

        assertEquals(1L, resultado.getId());
        assertEquals("ABC123", resultado.getPatente());
        assertEquals("Toyota", resultado.getMarca());
        assertEquals("Juan", resultado.getPropietarioNombre());

        verify(vehiculoRepository).findById(1L);
    }

    @Test
    void debeLanzarExcepcionCuandoNoExisteElVehiculo() {
        when(vehiculoRepository.findById(999L))
                .thenReturn(Optional.empty());

        assertThrows(
                VehiculoNoEncontradoException.class,
                () -> vehiculoService.buscarPorId(999L)
        );

        verify(vehiculoRepository).findById(999L);
    }

    @Test
    void debeCrearVehiculoCorrectamente() {
        CrearVehiculoRequest request = crearRequest();
        Propietario propietario = crearPropietario();

        when(vehiculoRepository.existsByPatente("ABC123"))
                .thenReturn(false);
        when(vehiculoRepository.existsByLicencia("LIC-001"))
                .thenReturn(false);
        when(vehiculoRepository.existsByNroRelojFullmar("FULL-001"))
                .thenReturn(false);
        when(propietarioRepository.findById(1L))
                .thenReturn(Optional.of(propietario));
        when(vehiculoRepository.save(any(Vehiculo.class)))
                .thenAnswer(invocation -> {
                    Vehiculo guardado = invocation.getArgument(0);
                    guardado.setId(1L);
                    return guardado;
                });

        VehiculoDTO resultado = vehiculoService.crear(request);

        assertEquals("ABC123", resultado.getPatente());
        assertEquals("LIC-001", resultado.getLicencia());
        assertEquals("Toyota", resultado.getMarca());
        assertEquals("Etios", resultado.getModelo());
        assertEquals(2020, resultado.getAnio());
        assertEquals("FULL-001", resultado.getNroRelojFullmar());
        assertEquals(1L, resultado.getPropietarioId());

        verify(vehiculoRepository).save(any(Vehiculo.class));
    }

    @Test
    void noDebeCrearVehiculoConPatenteDuplicada() {
        when(vehiculoRepository.existsByPatente("ABC123"))
                .thenReturn(true);

        assertThrows(
                PatenteVehiculoDuplicadaException.class,
                () -> vehiculoService.crear(crearRequest())
        );

        verify(vehiculoRepository, never()).save(any());
        verifyNoInteractions(propietarioRepository);
    }

    @Test
    void noDebeCrearVehiculoConLicenciaDuplicada() {
        when(vehiculoRepository.existsByPatente("ABC123"))
                .thenReturn(false);
        when(vehiculoRepository.existsByLicencia("LIC-001"))
                .thenReturn(true);

        assertThrows(
                LicenciaVehiculoDuplicadaException.class,
                () -> vehiculoService.crear(crearRequest())
        );

        verify(vehiculoRepository, never()).save(any());
        verifyNoInteractions(propietarioRepository);
    }

    @Test
    void noDebeCrearVehiculoConNumeroFullmarDuplicado() {
        when(vehiculoRepository.existsByPatente("ABC123"))
                .thenReturn(false);
        when(vehiculoRepository.existsByLicencia("LIC-001"))
                .thenReturn(false);
        when(vehiculoRepository.existsByNroRelojFullmar("FULL-001"))
                .thenReturn(true);

        assertThrows(
                NroRelojFullmarDuplicadoException.class,
                () -> vehiculoService.crear(crearRequest())
        );

        verify(vehiculoRepository, never()).save(any());
        verifyNoInteractions(propietarioRepository);
    }

    @Test
    void noDebeCrearVehiculoConPropietarioInexistente() {
        when(vehiculoRepository.existsByPatente("ABC123"))
                .thenReturn(false);
        when(vehiculoRepository.existsByLicencia("LIC-001"))
                .thenReturn(false);
        when(vehiculoRepository.existsByNroRelojFullmar("FULL-001"))
                .thenReturn(false);
        when(propietarioRepository.findById(1L))
                .thenReturn(Optional.empty());

        assertThrows(
                PropietarioNoEncontradoException.class,
                () -> vehiculoService.crear(crearRequest())
        );

        verify(vehiculoRepository, never()).save(any());
    }

    @Test
    void debeActualizarVehiculoConservandoSusIdentificadores() {
        Vehiculo vehiculo = crearVehiculo();
        ActualizarVehiculoRequest request = actualizarRequest();

        when(vehiculoRepository.findById(1L))
                .thenReturn(Optional.of(vehiculo));
        when(vehiculoRepository.existsByPatenteAndIdNot("ABC123", 1L))
                .thenReturn(false);
        when(vehiculoRepository.existsByLicenciaAndIdNot("LIC-001", 1L))
                .thenReturn(false);
        when(vehiculoRepository.existsByNroRelojFullmarAndIdNot("FULL-001", 1L))
                .thenReturn(false);
        when(propietarioRepository.findById(1L))
                .thenReturn(Optional.of(crearPropietario()));
        when(vehiculoRepository.save(vehiculo))
                .thenReturn(vehiculo);

        VehiculoDTO resultado = vehiculoService.actualizar(1L, request);

        assertEquals("ABC123", resultado.getPatente());
        assertEquals("LIC-001", resultado.getLicencia());
        assertEquals("FULL-001", resultado.getNroRelojFullmar());

        verify(vehiculoRepository).save(vehiculo);
    }

    @Test
    void noDebeActualizarVehiculoConPatenteDeOtroVehiculo() {
        Vehiculo vehiculo = crearVehiculo();

        when(vehiculoRepository.findById(1L))
                .thenReturn(Optional.of(vehiculo));
        when(vehiculoRepository.existsByPatenteAndIdNot("ABC123", 1L))
                .thenReturn(true);

        assertThrows(
                PatenteVehiculoDuplicadaException.class,
                () -> vehiculoService.actualizar(1L, actualizarRequest())
        );

        verify(vehiculoRepository, never()).save(any());
        verifyNoInteractions(propietarioRepository);
    }

    @Test
    void noDebeActualizarVehiculoConLicenciaDeOtroVehiculo() {
        Vehiculo vehiculo = crearVehiculo();

        when(vehiculoRepository.findById(1L))
                .thenReturn(Optional.of(vehiculo));
        when(vehiculoRepository.existsByPatenteAndIdNot("ABC123", 1L))
                .thenReturn(false);
        when(vehiculoRepository.existsByLicenciaAndIdNot("LIC-001", 1L))
                .thenReturn(true);

        assertThrows(
                LicenciaVehiculoDuplicadaException.class,
                () -> vehiculoService.actualizar(1L, actualizarRequest())
        );

        verify(vehiculoRepository, never()).save(any());
        verifyNoInteractions(propietarioRepository);
    }

    @Test
    void noDebeActualizarVehiculoConNumeroFullmarDeOtroVehiculo() {
        Vehiculo vehiculo = crearVehiculo();

        when(vehiculoRepository.findById(1L))
                .thenReturn(Optional.of(vehiculo));
        when(vehiculoRepository.existsByPatenteAndIdNot("ABC123", 1L))
                .thenReturn(false);
        when(vehiculoRepository.existsByLicenciaAndIdNot("LIC-001", 1L))
                .thenReturn(false);
        when(vehiculoRepository.existsByNroRelojFullmarAndIdNot("FULL-001", 1L))
                .thenReturn(true);

        assertThrows(
                NroRelojFullmarDuplicadoException.class,
                () -> vehiculoService.actualizar(1L, actualizarRequest())
        );

        verify(vehiculoRepository, never()).save(any());
        verifyNoInteractions(propietarioRepository);
    }
}
