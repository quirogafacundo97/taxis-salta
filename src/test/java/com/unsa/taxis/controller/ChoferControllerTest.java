package com.unsa.taxis.controller;

import com.unsa.taxis.exception.ChoferNoEncontradoException;
import com.unsa.taxis.service.ChoferService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ChoferController.class)
class ChoferControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ChoferService choferService;

    @Test
    void debeRetornar404CuandoNoExisteElChofer() throws Exception {

        when(choferService.buscarPorId(999L))
                .thenThrow(new ChoferNoEncontradoException(999L));

        mockMvc.perform(get("/api/choferes/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value(
                        "No se encontró el chofer con id: 999"
                ));
    }
}