package com.unsa.taxis.controller;

import com.unsa.taxis.exception.ChoferNoEncontradoException;
import com.unsa.taxis.service.ChoferService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import com.unsa.taxis.service.OfertaViajeService;
import com.unsa.taxis.dto.OfertaChoferResponse;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.unsa.taxis.config.SecurityConfig;
import org.springframework.context.annotation.Import;

import com.unsa.taxis.model.Chofer;
import com.unsa.taxis.service.UsuarioAutenticadoService;
import org.springframework.security.core.Authentication;
import org.springframework.security.test.context.support.WithMockUser;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import org.springframework.http.MediaType;

@WebMvcTest(ChoferController.class)
@Import(SecurityConfig.class)
class ChoferControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ChoferService choferService;

    @MockitoBean
    private OfertaViajeService ofertaViajeService;

    @MockitoBean
    private UsuarioAutenticadoService usuarioAutenticadoService;

    @Test
    @WithMockUser(username = "admin", authorities = "ROLE_ADMIN")
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


    @Test
    @WithMockUser(username = "12345678", authorities = "ROLE_CHOFER")
    void debeRetornarOfertasPendientesDelChofer() throws Exception {

                Chofer chofer = Chofer.builder()
                        .id(5L)
                        .nombre("Juan")
                        .apellido("Test")
                        .build();

                when(usuarioAutenticadoService.obtenerChofer(
                        org.mockito.ArgumentMatchers.any(Authentication.class)
                )).thenReturn(chofer);

                OfertaChoferResponse oferta = OfertaChoferResponse.builder()
                        .ofertaId(1L)
                        .viajeId(10L)
                        .clienteNombre("Juan")
                        .direccionOrigen("Centro")
                        .direccionDestino("Zona Sur")
                        .costoEstimado(new java.math.BigDecimal("2500.00"))
                        .distanciaMetros(5000)
                        .duracionSegundos(900)
                        .build();

                when(ofertaViajeService.listarOfertasPendientesResponsePorChofer(5L))
                        .thenReturn(List.of(oferta));

                mockMvc.perform(get("/api/choferes/5/ofertas"))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$[0].ofertaId").value(1))
                        .andExpect(jsonPath("$[0].viajeId").value(10))
                        .andExpect(jsonPath("$[0].clienteNombre").value("Juan"))
                        .andExpect(jsonPath("$[0].direccionOrigen").value("Centro"))
                        .andExpect(jsonPath("$[0].direccionDestino").value("Zona Sur"))
                        .andExpect(jsonPath("$[0].distanciaMetros").value(5000))
                        .andExpect(jsonPath("$[0].duracionSegundos").value(900));
     }


     @Test
     @WithMockUser(username = "12345678", authorities = "ROLE_CHOFER")
     void debeRetornarListaVaciaCuandoElChoferNoTieneOfertasPendientes() throws Exception {

                Chofer chofer = Chofer.builder()
                        .id(5L)
                        .nombre("Juan")
                        .apellido("Test")
                        .build();

                when(usuarioAutenticadoService.obtenerChofer(
                        org.mockito.ArgumentMatchers.any(Authentication.class)
                )).thenReturn(chofer);

                when(ofertaViajeService.listarOfertasPendientesResponsePorChofer(5L))
                        .thenReturn(List.of());

                mockMvc.perform(get("/api/choferes/5/ofertas"))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$").isEmpty());
      }

      @Test
      @WithMockUser(username = "12345678", authorities = "ROLE_CHOFER")
      void noDebePermitirConsultarOfertasDeOtroChofer() throws Exception {

                Chofer chofer = Chofer.builder()
                        .id(5L)
                        .nombre("Juan")
                        .apellido("Test")
                        .build();

                when(usuarioAutenticadoService.obtenerChofer(
                        org.mockito.ArgumentMatchers.any(Authentication.class)
                )).thenReturn(chofer);

                mockMvc.perform(get("/api/choferes/6/ofertas"))
                        .andExpect(status().isForbidden());

                org.mockito.Mockito.verify(
                        ofertaViajeService,
                        org.mockito.Mockito.never()
                ).listarOfertasPendientesResponsePorChofer(6L);
      }

      @Test
      @WithMockUser(username = "12345678", authorities = "ROLE_CHOFER")
      void noDebePermitirActualizarUbicacionDeOtroChofer() throws Exception {

                Chofer chofer = Chofer.builder()
                        .id(5L)
                        .nombre("Juan")
                        .apellido("Test")
                        .build();

                when(usuarioAutenticadoService.obtenerChofer(
                        org.mockito.ArgumentMatchers.any(Authentication.class)
                )).thenReturn(chofer);

                String request = """
                        {
                                "latitud": -24.7885,
                                "longitud": -65.4100
                        }
                        """;

                mockMvc.perform(put("/api/choferes/6/ubicacion")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(request))
                        .andExpect(status().isForbidden());

                org.mockito.Mockito.verify(
                        choferService,
                        org.mockito.Mockito.never()
                ).actualizarUbicacion(
                        org.mockito.ArgumentMatchers.anyLong(),
                        org.mockito.ArgumentMatchers.anyDouble(),
                        org.mockito.ArgumentMatchers.anyDouble()
                );
       }

       @Test
       @WithMockUser(username = "12345678", authorities = "ROLE_CHOFER")
       void noDebePermitirCambiarEstadoDeOtroChofer() throws Exception {

                Chofer chofer = Chofer.builder()
                        .id(5L)
                        .nombre("Juan")
                        .apellido("Test")
                        .build();

                when(usuarioAutenticadoService.obtenerChofer(
                        org.mockito.ArgumentMatchers.any(Authentication.class)
                )).thenReturn(chofer);

                String request = """
                        {
                                "estado": "LIBRE"
                        }
                        """;

                mockMvc.perform(put("/api/choferes/6/estado")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(request))
                        .andExpect(status().isForbidden());

                org.mockito.Mockito.verify(
                        choferService,
                        org.mockito.Mockito.never()
                ).cambiarEstado(
                        org.mockito.ArgumentMatchers.anyLong(),
                        org.mockito.ArgumentMatchers.any()
                );
        }

        @Test
        @WithMockUser(username = "12345678", authorities = "ROLE_CHOFER")
        void noDebePermitirAUnChoferConsultarDatosDeOtroChofer() throws Exception {

                mockMvc.perform(get("/api/choferes/5"))
                        .andExpect(status().isForbidden());

                org.mockito.Mockito.verify(
                        choferService,
                        org.mockito.Mockito.never()
                ).buscarPorId(5L);
        }

        @Test
        void noDebePermitirListarChoferesSinAutenticacion() throws Exception {

                mockMvc.perform(get("/api/choferes"))
                        .andExpect(status().isUnauthorized());

                org.mockito.Mockito.verify(
                        choferService,
                        org.mockito.Mockito.never()
                ).listarTodos();
        }

        @Test
        @WithMockUser(username = "admin", authorities = "ROLE_ADMIN")
                void debePermitirAUnAdminListarChoferes() throws Exception {

                mockMvc.perform(get("/api/choferes"))
                        .andExpect(status().isOk());

                org.mockito.Mockito.verify(choferService).listarTodos();
        }

        @Test
        @WithMockUser(username = "admin", authorities = "ROLE_ADMIN")
        void debePermitirAUnAdminListarChoferesDisponibles() throws Exception {

                mockMvc.perform(get("/api/choferes/disponibles"))
                        .andExpect(status().isOk());

                org.mockito.Mockito.verify(choferService).listarDisponibles();
        }

        @Test
        @WithMockUser(username = "12345678", authorities = "ROLE_CHOFER")
        void noDebePermitirAUnChoferListarChoferesDisponibles() throws Exception {

                mockMvc.perform(get("/api/choferes/disponibles"))
                        .andExpect(status().isForbidden());

                org.mockito.Mockito.verify(
                        choferService,
                        org.mockito.Mockito.never()
                ).listarDisponibles();
        }

        @Test
        @WithMockUser(username = "12345678", authorities = "ROLE_CHOFER")
        void noDebePermitirAUnChoferBuscarChoferesCercanos() throws Exception {

                mockMvc.perform(get("/api/choferes/cercanos")
                                .param("latitud", "-24.7885")
                                .param("longitud", "-65.4100")
                                .param("radioMetros", "5000"))
                        .andExpect(status().isForbidden());

                org.mockito.Mockito.verify(
                        choferService,
                        org.mockito.Mockito.never()
                ).buscarChoferesCercanos(
                        org.mockito.ArgumentMatchers.anyDouble(),
                        org.mockito.ArgumentMatchers.anyDouble(),
                        org.mockito.ArgumentMatchers.anyDouble()
                );
        }

        @Test
        void noDebePermitirListarChoferesDisponiblesSinAutenticacion() throws Exception {

                mockMvc.perform(get("/api/choferes/disponibles"))
                        .andExpect(status().isUnauthorized());

                org.mockito.Mockito.verify(
                        choferService,
                        org.mockito.Mockito.never()
                ).listarDisponibles();
        }

        @Test
        void noDebePermitirBuscarChoferesCercanosSinAutenticacion() throws Exception {

                mockMvc.perform(get("/api/choferes/cercanos")
                                .param("latitud", "-24.7885")
                                .param("longitud", "-65.4100")
                                .param("radioMetros", "5000"))
                        .andExpect(status().isUnauthorized());

                org.mockito.Mockito.verify(
                        choferService,
                        org.mockito.Mockito.never()
                ).buscarChoferesCercanos(
                        org.mockito.ArgumentMatchers.anyDouble(),
                        org.mockito.ArgumentMatchers.anyDouble(),
                        org.mockito.ArgumentMatchers.anyDouble()
                );
        }
}