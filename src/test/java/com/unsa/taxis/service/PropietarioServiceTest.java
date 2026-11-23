
package com.unsa.taxis.service;

import com.unsa.taxis.dto.ActualizarPropietarioRequest;
import com.unsa.taxis.dto.CrearPropietarioRequest;
import com.unsa.taxis.dto.PropietarioDTO;
import com.unsa.taxis.exception.DniPropietarioDuplicadoException;
import com.unsa.taxis.exception.PropietarioNoEncontradoException;
import com.unsa.taxis.model.Propietario;
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
class PropietarioServiceTest {

    @Mock
    private PropietarioRepository propietarioRepository;

    @Mock
    private VehiculoRepository vehiculoRepository;

    private PropietarioService propietarioService;

    @BeforeEach
    void setUp() {
        propietarioService = new PropietarioService(
                propietarioRepository,
                vehiculoRepository
        );
    }

    @Test
    void debeListarPropietariosConCantidadDeVehiculos() {

        Propietario juan = Propietario.builder()
                .id(1L)
                .nombre("Juan")
                .apellido("Perez")
                .dni("30123456")
                .telefono("3874000000")
                .build();

        Propietario maria = Propietario.builder()
                .id(2L)
                .nombre("Maria")
                .apellido("Lopez")
                .dni("28987654")
                .telefono("3874111111")
                .build();

        when(propietarioRepository.findAll())
                .thenReturn(List.of(juan, maria));

        when(vehiculoRepository.countByPropietario_Id(1L))
                .thenReturn(2L);

        when(vehiculoRepository.countByPropietario_Id(2L))
                .thenReturn(0L);

        List<PropietarioDTO> resultado =
                propietarioService.listarTodos();

        assertEquals(2, resultado.size());

        assertEquals("Juan", resultado.get(0).getNombre());
        assertEquals(2L, resultado.get(0).getCantidadVehiculos());

        assertEquals("Maria", resultado.get(1).getNombre());
        assertEquals(0L, resultado.get(1).getCantidadVehiculos());

        verify(propietarioRepository).findAll();
        verify(vehiculoRepository).countByPropietario_Id(1L);
        verify(vehiculoRepository).countByPropietario_Id(2L);
    }

    @Test
    void debeDevolverListaVaciaSiNoHayPropietarios() {

        when(propietarioRepository.findAll())
                .thenReturn(List.of());

        List<PropietarioDTO> resultado =
                propietarioService.listarTodos();

        assertTrue(resultado.isEmpty());

        verify(propietarioRepository).findAll();
        verifyNoInteractions(vehiculoRepository);
    }

    @Test
    void debeBuscarPropietarioPorId() {

        Propietario propietario = Propietario.builder()
                .id(1L)
                .nombre("Juan")
                .apellido("Perez")
                .dni("30123456")
                .build();

        when(propietarioRepository.findById(1L))
                .thenReturn(Optional.of(propietario));

        Propietario resultado =
                propietarioService.buscarPorId(1L);

        assertEquals(propietario, resultado);

        verify(propietarioRepository).findById(1L);
    }

    @Test
    void debeLanzarExcepcionCuandoNoExisteElPropietario() {

        when(propietarioRepository.findById(999L))
                .thenReturn(Optional.empty());

        assertThrows(
                PropietarioNoEncontradoException.class,
                () -> propietarioService.buscarPorId(999L)
        );

        verify(propietarioRepository).findById(999L);
    }

    @Test
    void debeCrearPropietarioCorrectamente() {

        CrearPropietarioRequest request =
                new CrearPropietarioRequest(
                        "Juan",
                        "Perez",
                        "30123456",
                        "3874000000"
                );

        when(propietarioRepository.existsByDni("30123456"))
                .thenReturn(false);

        when(propietarioRepository.save(any(Propietario.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Propietario resultado =
                propietarioService.crear(request);

        assertEquals("Juan", resultado.getNombre());
        assertEquals("Perez", resultado.getApellido());
        assertEquals("30123456", resultado.getDni());
        assertEquals("3874000000", resultado.getTelefono());

        verify(propietarioRepository).existsByDni("30123456");
        verify(propietarioRepository).save(any(Propietario.class));
    }

    @Test
    void noDebeCrearPropietarioConDniDuplicado() {

        CrearPropietarioRequest request =
                new CrearPropietarioRequest(
                        "Juan",
                        "Perez",
                        "30123456",
                        "3874000000"
                );

        when(propietarioRepository.existsByDni("30123456"))
                .thenReturn(true);

        assertThrows(
                DniPropietarioDuplicadoException.class,
                () -> propietarioService.crear(request)
        );

        verify(propietarioRepository).existsByDni("30123456");
        verify(propietarioRepository, never()).save(any());
    }

    @Test
    void debeActualizarPropietarioConservandoSuPropioDni() {

        Propietario propietario = Propietario.builder()
                .id(1L)
                .nombre("Juan")
                .apellido("Perez")
                .dni("30123456")
                .telefono("3874000000")
                .build();

        ActualizarPropietarioRequest request =
                new ActualizarPropietarioRequest(
                        "Juan Carlos",
                        "Perez",
                        "30123456",
                        "3874222222"
                );

        when(propietarioRepository.findById(1L))
                .thenReturn(Optional.of(propietario));

        when(propietarioRepository.findByDni("30123456"))
                .thenReturn(Optional.of(propietario));

        when(propietarioRepository.save(propietario))
                .thenReturn(propietario);

        Propietario resultado =
                propietarioService.actualizar(1L, request);

        assertEquals("Juan Carlos", resultado.getNombre());
        assertEquals("Perez", resultado.getApellido());
        assertEquals("30123456", resultado.getDni());
        assertEquals("3874222222", resultado.getTelefono());

        verify(propietarioRepository).findById(1L);
        verify(propietarioRepository).findByDni("30123456");
        verify(propietarioRepository).save(propietario);
    }

    @Test
    void noDebeActualizarPropietarioConDniDeOtroPropietario() {

        Propietario propietario = Propietario.builder()
                .id(1L)
                .nombre("Juan")
                .apellido("Perez")
                .dni("30123456")
                .build();

        Propietario otroPropietario = Propietario.builder()
                .id(2L)
                .nombre("Maria")
                .apellido("Lopez")
                .dni("28987654")
                .build();

        ActualizarPropietarioRequest request =
                new ActualizarPropietarioRequest(
                        "Juan",
                        "Perez",
                        "28987654",
                        "3874000000"
                );

        when(propietarioRepository.findById(1L))
                .thenReturn(Optional.of(propietario));

        when(propietarioRepository.findByDni("28987654"))
                .thenReturn(Optional.of(otroPropietario));

        assertThrows(
                DniPropietarioDuplicadoException.class,
                () -> propietarioService.actualizar(1L, request)
        );

        verify(propietarioRepository).findById(1L);
        verify(propietarioRepository).findByDni("28987654");
        verify(propietarioRepository, never()).save(any());
    }
}
