package com.unsa.taxis.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.unsa.taxis.config.SecurityConfig;
import com.unsa.taxis.dto.CambiarPasswordRequest;
import com.unsa.taxis.service.AuthService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.security.core.Authentication;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.never;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
@Import(SecurityConfig.class)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AuthService authService;

    @Test
    @WithMockUser(
            username = "12345678",
            authorities = "CAMBIAR_PASSWORD"
    )
    void debeCambiarPasswordCorrectamente() throws Exception {

        CambiarPasswordRequest request = new CambiarPasswordRequest(
                "nuevaPassword123",
                "nuevaPassword123"
        );

        mockMvc.perform(put("/api/auth/cambiar-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNoContent());

        verify(authService).cambiarPassword(
                eq(request),
                any(Authentication.class)
        );
    }

    @Test
    void debeRechazarCambioPasswordSinAutenticacion() throws Exception {

        CambiarPasswordRequest request = new CambiarPasswordRequest(
                "nuevaPassword123",
                "nuevaPassword123"
        );

        mockMvc.perform(put("/api/auth/cambiar-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());

        verify(authService, never()).cambiarPassword(
                any(CambiarPasswordRequest.class),
                any(Authentication.class)
        );
    }

    @Test
    @WithMockUser(
            username = "12345678",
            authorities = "ROLE_ADMIN"
    )
    void debeRechazarCambioPasswordSinAutoridadEspecial() throws Exception {

        CambiarPasswordRequest request = new CambiarPasswordRequest(
                "nuevaPassword123",
                "nuevaPassword123"
        );

        mockMvc.perform(put("/api/auth/cambiar-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());

        verify(authService, never()).cambiarPassword(
                any(CambiarPasswordRequest.class),
                any(Authentication.class)
        );
    }
}