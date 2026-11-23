
package com.unsa.taxis.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.unsa.taxis.config.SecurityConfig;
import com.unsa.taxis.dto.ActualizarPropietarioRequest;
import com.unsa.taxis.dto.CrearPropietarioRequest;
import com.unsa.taxis.dto.PropietarioDTO;
import com.unsa.taxis.exception.DniPropietarioDuplicadoException;
import com.unsa.taxis.exception.PropietarioNoEncontradoException;
import com.unsa.taxis.model.Propietario;
import com.unsa.taxis.service.PropietarioService;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.springframework.security.test.context.support.WithMockUser;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PropietarioController.class)
@Import(SecurityConfig.class)
class PropietarioControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private PropietarioService propietarioService;

    @Test
    @WithMockUser(username = "admin", authorities = "ROLE_ADMIN")
    void debePermitirAUnAdminListarPropietarios() throws Exception {

        PropietarioDTO propietario = PropietarioDTO.builder()
                .id(1L)
                .nombre("Juan")
                .apellido("Perez")
                .dni("12345678")
                .telefono("3874000000")
                .cantidadVehiculos(2L)
                .build();

        when(propietarioService.listarTodos())
                .thenReturn(List.of(propietario));

        mockMvc.perform(get("/api/propietarios"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].nombre").value("Juan"))
                .andExpect(jsonPath("$[0].apellido").value("Perez"))
                .andExpect(jsonPath("$[0].dni").value("12345678"))
                .andExpect(jsonPath("$[0].cantidadVehiculos").value(2));

        verify(propietarioService).listarTodos();
    }

    @Test
    @WithMockUser(username = "admin", authorities = "ROLE_ADMIN")
    void debePermitirAUnAdminBuscarPropietarioPorId() throws Exception {

        Propietario propietario = Propietario.builder()
                .id(1L)
                .nombre("Juan")
                .apellido("Perez")
                .dni("12345678")
                .telefono("3874000000")
                .build();

        when(propietarioService.buscarPorId(1L))
                .thenReturn(propietario);

        mockMvc.perform(get("/api/propietarios/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.nombre").value("Juan"))
                .andExpect(jsonPath("$.dni").value("12345678"));

        verify(propietarioService).buscarPorId(1L);
    }

    @Test
    @WithMockUser(username = "admin", authorities = "ROLE_ADMIN")
    void debeRetornar404CuandoNoExisteElPropietario() throws Exception {

        when(propietarioService.buscarPorId(999L))
                .thenThrow(new PropietarioNoEncontradoException(999L));

        mockMvc.perform(get("/api/propietarios/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value(
                        "No se encontró el propietario con id: 999"
                ));
    }

    @Test
    @WithMockUser(username = "admin", authorities = "ROLE_ADMIN")
    void debePermitirAUnAdminCrearPropietario() throws Exception {

        CrearPropietarioRequest request = new CrearPropietarioRequest(
                "Juan",
                "Perez",
                "12345678",
                "3874000000"
        );

        Propietario propietario = Propietario.builder()
                .id(1L)
                .nombre("Juan")
                .apellido("Perez")
                .dni("12345678")
                .telefono("3874000000")
                .build();

        when(propietarioService.crear(any(CrearPropietarioRequest.class)))
                .thenReturn(propietario);

        mockMvc.perform(post("/api/propietarios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.nombre").value("Juan"))
                .andExpect(jsonPath("$.dni").value("12345678"));

        verify(propietarioService).crear(any(CrearPropietarioRequest.class));
    }

    @Test
    @WithMockUser(username = "admin", authorities = "ROLE_ADMIN")
    void noDebeCrearPropietarioConDatosInvalidos() throws Exception {

        String request = """
                {
                    "nombre": "",
                    "apellido": "Perez",
                    "dni": "123",
                    "telefono": "3874000000"
                }
                """;

        mockMvc.perform(post("/api/propietarios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").exists());

        verify(propietarioService, never())
                .crear(any(CrearPropietarioRequest.class));
    }

    @Test
    @WithMockUser(username = "admin", authorities = "ROLE_ADMIN")
    void noDebeCrearPropietarioConDniDuplicado() throws Exception {

        CrearPropietarioRequest request = new CrearPropietarioRequest(
                "Juan",
                "Perez",
                "12345678",
                "3874000000"
        );

        when(propietarioService.crear(any(CrearPropietarioRequest.class)))
                .thenThrow(new DniPropietarioDuplicadoException("12345678"));

        mockMvc.perform(post("/api/propietarios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.message").value(
                        "Ya existe un propietario registrado con ese DNI."
                ));
    }

    @Test
    @WithMockUser(username = "admin", authorities = "ROLE_ADMIN")
    void debePermitirAUnAdminActualizarPropietario() throws Exception {

        ActualizarPropietarioRequest request = new ActualizarPropietarioRequest(
                "Carlos",
                "Gomez",
                "12345678",
                "3874111111"
        );

        Propietario propietario = Propietario.builder()
                .id(1L)
                .nombre("Carlos")
                .apellido("Gomez")
                .dni("12345678")
                .telefono("3874111111")
                .build();

        when(propietarioService.actualizar(eq(1L),
                any(ActualizarPropietarioRequest.class)))
                .thenReturn(propietario);

        mockMvc.perform(put("/api/propietarios/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.nombre").value("Carlos"))
                .andExpect(jsonPath("$.apellido").value("Gomez"));

        verify(propietarioService).actualizar(
                eq(1L), any(ActualizarPropietarioRequest.class));
    }

    @Test
    @WithMockUser(username = "12345678", authorities = "ROLE_CHOFER")
    void noDebePermitirAUnChoferAccederAPropietarios() throws Exception {

        mockMvc.perform(get("/api/propietarios"))
                .andExpect(status().isForbidden());

        verify(propietarioService, never()).listarTodos();
    }

    @Test
    void noDebePermitirListarPropietariosSinAutenticacion() throws Exception {

        mockMvc.perform(get("/api/propietarios"))
                .andExpect(status().isUnauthorized());

        verify(propietarioService, never()).listarTodos();
    }

    @Test
    void noDebePermitirCrearPropietariosSinAutenticacion() throws Exception {

        mockMvc.perform(post("/api/propietarios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "nombre": "Juan",
                                    "apellido": "Perez",
                                    "dni": "12345678",
                                    "telefono": "3874000000"
                                }
                                """))
                .andExpect(status().isUnauthorized());

        verify(propietarioService, never())
                .crear(any(CrearPropietarioRequest.class));
    }
}
