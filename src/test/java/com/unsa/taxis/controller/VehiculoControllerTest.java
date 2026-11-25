
package com.unsa.taxis.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.unsa.taxis.config.SecurityConfig;
import com.unsa.taxis.dto.ActualizarVehiculoRequest;
import com.unsa.taxis.dto.CrearVehiculoRequest;
import com.unsa.taxis.dto.VehiculoDTO;
import com.unsa.taxis.exception.LicenciaVehiculoDuplicadaException;
import com.unsa.taxis.exception.NroRelojFullmarDuplicadoException;
import com.unsa.taxis.exception.PatenteVehiculoDuplicadaException;
import com.unsa.taxis.exception.PropietarioNoEncontradoException;
import com.unsa.taxis.exception.VehiculoNoEncontradoException;
import com.unsa.taxis.service.VehiculoService;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import org.springframework.security.test.context.support.WithMockUser;

@WebMvcTest(VehiculoController.class)
@Import(SecurityConfig.class)
class VehiculoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private VehiculoService vehiculoService;

    private VehiculoDTO crearVehiculoDTO() {
        return VehiculoDTO.builder()
                .id(1L)
                .patente("ABC123")
                .licencia("LIC-001")
                .marca("Toyota")
                .modelo("Etios")
                .anio(2020)
                .nroRelojFullmar("FULL-001")
                .propietarioId(1L)
                .propietarioNombre("Juan")
                .propietarioApellido("Perez")
                .propietarioDni("30123456")
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
    @WithMockUser(username = "admin", authorities = "ROLE_ADMIN")
    void debePermitirAUnAdminListarVehiculos() throws Exception {
        when(vehiculoService.listarTodos())
                .thenReturn(List.of(crearVehiculoDTO()));

        mockMvc.perform(get("/api/vehiculos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].patente").value("ABC123"))
                .andExpect(jsonPath("$[0].licencia").value("LIC-001"))
                .andExpect(jsonPath("$[0].marca").value("Toyota"))
                .andExpect(jsonPath("$[0].propietarioNombre").value("Juan"));

        verify(vehiculoService).listarTodos();
    }

    @Test
    @WithMockUser(username = "admin", authorities = "ROLE_ADMIN")
    void debePermitirAUnAdminBuscarVehiculoPorId() throws Exception {
        when(vehiculoService.buscarPorId(1L))
                .thenReturn(crearVehiculoDTO());

        mockMvc.perform(get("/api/vehiculos/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.patente").value("ABC123"))
                .andExpect(jsonPath("$.marca").value("Toyota"))
                .andExpect(jsonPath("$.propietarioDni").value("30123456"));

        verify(vehiculoService).buscarPorId(1L);
    }

    @Test
    @WithMockUser(username = "admin", authorities = "ROLE_ADMIN")
    void debeRetornar404CuandoNoExisteElVehiculo() throws Exception {
        when(vehiculoService.buscarPorId(999L))
                .thenThrow(new VehiculoNoEncontradoException(999L));

        mockMvc.perform(get("/api/vehiculos/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value(
                        "No se encontró el vehículo con id: 999"
                ));
    }

    @Test
    @WithMockUser(username = "admin", authorities = "ROLE_ADMIN")
    void debePermitirAUnAdminCrearVehiculo() throws Exception {
        when(vehiculoService.crear(any(CrearVehiculoRequest.class)))
                .thenReturn(crearVehiculoDTO());

        mockMvc.perform(post("/api/vehiculos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(crearRequest())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.patente").value("ABC123"))
                .andExpect(jsonPath("$.marca").value("Toyota"))
                .andExpect(jsonPath("$.propietarioId").value(1));

        verify(vehiculoService).crear(any(CrearVehiculoRequest.class));
    }

    @Test
    @WithMockUser(username = "admin", authorities = "ROLE_ADMIN")
    void noDebeCrearVehiculoConDatosInvalidos() throws Exception {
        String request = """
                {
                    "patente": "",
                    "licencia": "LIC-001",
                    "marca": "Toyota",
                    "modelo": "Etios",
                    "anio": 2020,
                    "nroRelojFullmar": "FULL-001",
                    "propietarioId": 1
                }
                """;

        mockMvc.perform(post("/api/vehiculos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").exists());

        verify(vehiculoService, never())
                .crear(any(CrearVehiculoRequest.class));
    }

    @Test
    @WithMockUser(username = "admin", authorities = "ROLE_ADMIN")
    void noDebeCrearVehiculoConPatenteDuplicada() throws Exception {
        when(vehiculoService.crear(any(CrearVehiculoRequest.class)))
                .thenThrow(new PatenteVehiculoDuplicadaException("ABC123"));

        mockMvc.perform(post("/api/vehiculos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(crearRequest())))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    @WithMockUser(username = "admin", authorities = "ROLE_ADMIN")
    void noDebeCrearVehiculoConLicenciaDuplicada() throws Exception {
        when(vehiculoService.crear(any(CrearVehiculoRequest.class)))
                .thenThrow(new LicenciaVehiculoDuplicadaException("LIC-001"));

        mockMvc.perform(post("/api/vehiculos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(crearRequest())))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    @WithMockUser(username = "admin", authorities = "ROLE_ADMIN")
    void noDebeCrearVehiculoConNumeroFullmarDuplicado() throws Exception {
        when(vehiculoService.crear(any(CrearVehiculoRequest.class)))
                .thenThrow(new NroRelojFullmarDuplicadoException("FULL-001"));

        mockMvc.perform(post("/api/vehiculos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(crearRequest())))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    @WithMockUser(username = "admin", authorities = "ROLE_ADMIN")
    void noDebeCrearVehiculoConPropietarioInexistente() throws Exception {
        when(vehiculoService.crear(any(CrearVehiculoRequest.class)))
                .thenThrow(new PropietarioNoEncontradoException(999L));

        mockMvc.perform(post("/api/vehiculos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(crearRequest())))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    @WithMockUser(username = "admin", authorities = "ROLE_ADMIN")
    void debePermitirAUnAdminActualizarVehiculo() throws Exception {
        when(vehiculoService.actualizar(eq(1L),
                any(ActualizarVehiculoRequest.class)))
                .thenReturn(crearVehiculoDTO());

        mockMvc.perform(put("/api/vehiculos/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(actualizarRequest())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.patente").value("ABC123"))
                .andExpect(jsonPath("$.marca").value("Toyota"));

        verify(vehiculoService).actualizar(
                eq(1L), any(ActualizarVehiculoRequest.class));
    }

    @Test
    @WithMockUser(username = "12345678", authorities = "ROLE_CHOFER")
    void noDebePermitirAUnChoferAccederAVehiculos() throws Exception {
        mockMvc.perform(get("/api/vehiculos"))
                .andExpect(status().isForbidden());

        verify(vehiculoService, never()).listarTodos();
    }

    @Test
    void noDebePermitirListarVehiculosSinAutenticacion() throws Exception {
        mockMvc.perform(get("/api/vehiculos"))
                .andExpect(status().isUnauthorized());

        verify(vehiculoService, never()).listarTodos();
    }

    @Test
    void noDebePermitirCrearVehiculosSinAutenticacion() throws Exception {
        mockMvc.perform(post("/api/vehiculos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(crearRequest())))
                .andExpect(status().isUnauthorized());

        verify(vehiculoService, never())
                .crear(any(CrearVehiculoRequest.class));
    }
}
