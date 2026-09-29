package com.unsa.taxis.controller;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import com.unsa.taxis.exception.ViajeNoEncontradoException;
import com.unsa.taxis.service.ViajeService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ViajeController.class)
class ViajeControllerTest {

    @Autowired
    private MockMvc mockMvc;

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
}