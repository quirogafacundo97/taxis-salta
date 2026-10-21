package com.unsa.taxis.controller;

import com.unsa.taxis.dto.ViajeResponse;
import com.unsa.taxis.exception.ViajeNoEncontradoException;
import com.unsa.taxis.service.ViajeService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.unsa.taxis.dto.CrearViajeRequest;
import com.unsa.taxis.exception.TokenSolicitudViajeInvalidoException;
import org.springframework.http.MediaType;

import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

@WebMvcTest(ViajeController.class)
class ViajeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ViajeService viajeService;

    @Test
    void debeRetornar404CuandoNoExisteElViaje() throws Exception {

        when(viajeService.buscarPorId(999L))
                .thenThrow(new ViajeNoEncontradoException(999L));

        mockMvc.perform(get("/api/viajes/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value(
                        "No se encontró el viaje con id: 999"
                ));
    }

    @Test
    void debeIniciarViajeCorrectamente() throws Exception {

        mockMvc.perform(put("/api/viajes/1/iniciar"))
                .andExpect(status().isNoContent());

        verify(viajeService).iniciarViaje(1L);
    }

    @Test
    void debeFinalizarViajeCorrectamente() throws Exception {

        mockMvc.perform(
                        put("/api/viajes/1/finalizar")
                )
                .andExpect(status().isNoContent());

        verify(viajeService)
                .finalizarViaje(1L);
    }

    @Test
    void debeCancelarViajeCorrectamente() throws Exception {

        mockMvc.perform(
                        put("/api/viajes/1/cancelar")
                )
                .andExpect(status().isNoContent());

        verify(viajeService)
                .cancelarViaje(1L);
    }
    
    @Test
    void debeRetornar400CuandoElTokenEsInvalido() throws Exception {

        CrearViajeRequest request = CrearViajeRequest.builder()
                .token("tokenInvalido")
                .nombreCliente("Juan")
                .direccionOrigen("Av. Belgrano 1234, Salta")
                .latitudOrigen(-24.7885)
                .longitudOrigen(-65.4100)
                .direccionDestino("Plaza 9 de Julio, Salta")
                .latitudDestino(-24.7875)
                .longitudDestino(-65.4105)
                .build();

        when(viajeService.crearViaje(any(CrearViajeRequest.class)))
                .thenThrow(new TokenSolicitudViajeInvalidoException());

        mockMvc.perform(post("/api/viajes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value(
                        "El enlace de solicitud de viaje es inválido, venció o ya fue utilizado"
                ));
     }
     
     @Test
     void debeRetornar400CuandoFaltaElToken() throws Exception {
        
        String requestSinToken = """
            {
                "nombreCliente": "Juan",
                "direccionOrigen": "Av. Belgrano 1234, Salta",
                "latitudOrigen": -24.7885,
                "longitudOrigen": -65.4100,
                "direccionDestino": "Plaza 9 de Julio, Salta",
                "latitudDestino": -24.7875,
                "longitudDestino": -65.4105
            }
            """;

        mockMvc.perform(post("/api/viajes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestSinToken))
                .andExpect(status().isBadRequest());

        verify(viajeService, org.mockito.Mockito.never())
                .crearViaje(any(CrearViajeRequest.class));
      }
      
     @Test
     void debeCrearViajeCorrectamente() throws Exception {

        CrearViajeRequest request = CrearViajeRequest.builder()
                .token("tokenValido")
                .nombreCliente("Juan")
                .direccionOrigen("Av. Belgrano 1234, Salta")
                .latitudOrigen(-24.7885)
                .longitudOrigen(-65.4100)
                .direccionDestino("Plaza 9 de Julio, Salta")
                .latitudDestino(-24.7875)
                .longitudDestino(-65.4105)
                .build();

        ViajeResponse respuesta = ViajeResponse.builder()
                .id(1L)
                .clienteNombre("Juan")
                .direccionOrigen("Av. Belgrano 1234, Salta")
                .direccionDestino("Plaza 9 de Julio, Salta")
                .estado(com.unsa.taxis.model.EstadoViaje.SOLICITADO)
                .tipoTarifa(com.unsa.taxis.model.TipoTarifa.NOCTURNA)
                .costoEstimado(new java.math.BigDecimal("1176.00"))
                .build();

        when(viajeService.crearViaje(any(CrearViajeRequest.class)))
                .thenReturn(respuesta);

        mockMvc.perform(post("/api/viajes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.clienteNombre").value("Juan"))
                .andExpect(jsonPath("$.estado").value("SOLICITADO"))
                .andExpect(jsonPath("$.direccionDestino")
                        .value("Plaza 9 de Julio, Salta"));
       }

}