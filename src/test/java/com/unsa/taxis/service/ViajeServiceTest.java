package com.unsa.taxis.service;

import com.unsa.taxis.exception.TransicionEstadoViajeException;
import com.unsa.taxis.exception.ViajeNoEncontradoException;
import com.unsa.taxis.dto.CrearViajeRequest;
import com.unsa.taxis.dto.ViajeResponse;
import com.unsa.taxis.mapper.ViajeMapper;
import com.unsa.taxis.model.*;
import com.unsa.taxis.repository.ClienteRepository;
import com.unsa.taxis.repository.ViajeRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.ArgumentCaptor;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.context.ApplicationEventPublisher;
import com.unsa.taxis.event.ViajeCreadoEvent;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.unsa.taxis.exception.AccesoNoAutorizadoException;
import org.springframework.security.core.Authentication;

import org.springframework.security.core.authority.SimpleGrantedAuthority;

@ExtendWith(MockitoExtension.class)
class ViajeServiceTest {

    @Mock
    private ViajeRepository viajeRepository;

    @Mock
    private ClienteRepository clienteRepository;

    @Mock
    private TarifaService tarifaService;

    @Mock
    private CalculadorCostoService calculadorCostoService;

    @Mock
    private ViajeMapper viajeMapper;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @Mock
    private TokenSolicitudViajeService tokenSolicitudViajeService;

    @InjectMocks
    private ViajeService viajeService;

    @Mock
    private ChoferService choferService;

    @Mock
    private OfertaViajeService ofertaViajeService;

    @Mock
    private UsuarioAutenticadoService usuarioAutenticadoService;

    @Mock
    private Authentication authentication;

    @Test
    void debeCrearViajeConClienteExistente() {

        // Cliente que ya existe
        Cliente cliente = Cliente.builder()
                .id(1L)
                .whatsappId("5493875551234")
                .nombre("Juan")
                .build();

        // Request recibido
        CrearViajeRequest request = CrearViajeRequest.builder()
                .token("tokenPrueba")
                .nombreCliente("Juan")
                .direccionOrigen("Av. Belgrano 1234, Salta")
                .latitudOrigen(-24.7885)
                .longitudOrigen(-65.4100)
                .direccionDestino("Plaza 9 de Julio, Salta")
                .latitudDestino(-24.7875)
                .longitudDestino(-65.4105)
                .rutaGeometria("{\"type\":\"LineString\",\"coordinates\":[[-65.4100,-24.7885],[-65.4105,-24.7875]]}")
                .distanciaMetros(1500)
                .duracionSegundos(300)
                .build();

        // Tarifa que queremos simular
        Tarifa tarifa = Tarifa.builder()
                .tipo(TipoTarifa.NOCTURNA)
                .bajadaBandera(new BigDecimal("1176.00"))
                .valorFicha(new BigDecimal("118.00"))
                .build();

        // Simulamos que el cálculo dio $1412
        BigDecimal costoEstimado = new BigDecimal("1412.00");

        // Configuramos los mocks
        when(clienteRepository.findByWhatsappId("5493875551234"))
                .thenReturn(Optional.of(cliente));

        when(tarifaService.obtenerTarifaParaFechaHora(any()))
                .thenReturn(tarifa);

        when(calculadorCostoService.calcularCosto(
                -24.7885,
                -65.4100,
                -24.7875,
                -65.4105,
                tarifa
        )).thenReturn(costoEstimado);

        // Simulamos el guardado del viaje
        when(viajeRepository.save(any(Viaje.class)))
                .thenAnswer(invocation -> {
                    Viaje viaje = invocation.getArgument(0);
                    viaje.setId(1L);
                    return viaje;
                });

        ViajeResponse respuesta = ViajeResponse.builder()
                .id(1L)
                .clienteNombre("Juan")
                .direccionOrigen("Av. Belgrano 1234, Salta")
                .direccionDestino("Plaza 9 de Julio, Salta")
                .tipoTarifa(TipoTarifa.NOCTURNA)
                .costoEstimado(costoEstimado)
                .build();

        when(viajeMapper.toResponse(any(Viaje.class)))
                .thenReturn(respuesta);

        when(tokenSolicitudViajeService.consumirToken("tokenPrueba"))
                .thenReturn(Optional.of("5493875551234"));

        // Ejecutamos el metodo REAL que estamos probando
        ViajeResponse resultado = viajeService.crearViaje(request);

        // Verificaciones
        assertNotNull(resultado);
        assertEquals(1L, resultado.getId());
        assertEquals("Juan", resultado.getClienteNombre());
        assertEquals("Av. Belgrano 1234, Salta", resultado.getDireccionOrigen());
        assertEquals("Plaza 9 de Julio, Salta", resultado.getDireccionDestino());
        assertEquals(TipoTarifa.NOCTURNA, resultado.getTipoTarifa());
        assertEquals(new BigDecimal("1412.00"), resultado.getCostoEstimado());

        verify(clienteRepository)
                .findByWhatsappId("5493875551234");

        verify(tarifaService)
                .obtenerTarifaParaFechaHora(any());

        verify(calculadorCostoService)
                .calcularCosto(
                        -24.7885,
                        -65.4100,
                        -24.7875,
                        -65.4105,
                        tarifa
                );

        ArgumentCaptor<Viaje> viajeCaptor =
                ArgumentCaptor.forClass(Viaje.class);

        verify(viajeRepository).save(viajeCaptor.capture());

        Viaje viajeGuardado = viajeCaptor.getValue();

        assertEquals(
                "{\"type\":\"LineString\",\"coordinates\":[[-65.4100,-24.7885],[-65.4105,-24.7875]]}",
                viajeGuardado.getRutaGeometria()
        );
        assertEquals(1500, viajeGuardado.getDistanciaMetros());
        assertEquals(300, viajeGuardado.getDuracionSegundos());

        verify(viajeMapper).toResponse(any(Viaje.class));

        verify(clienteRepository, never())
                .save(any(Cliente.class));

        ArgumentCaptor<ViajeCreadoEvent> eventoCaptor =
                ArgumentCaptor.forClass(ViajeCreadoEvent.class);

        verify(eventPublisher).publishEvent(eventoCaptor.capture());

        assertEquals(1L, eventoCaptor.getValue().viajeId());
    }

    @Test
    void debeCrearClienteNuevoAlCrearViaje() {

        CrearViajeRequest request = CrearViajeRequest.builder()
                .token("tokenPrueba")
                .nombreCliente("Carlos")
                .direccionOrigen("Av. Belgrano 1234, Salta")
                .latitudOrigen(-24.7885)
                .longitudOrigen(-65.4100)
                .direccionDestino("Plaza 9 de Julio, Salta")
                .latitudDestino(-24.7875)
                .longitudDestino(-65.4105)
                .build();

        Tarifa tarifa = Tarifa.builder()
                .tipo(TipoTarifa.NOCTURNA)
                .bajadaBandera(new BigDecimal("1176.00"))
                .valorFicha(new BigDecimal("118.00"))
                .build();

        BigDecimal costoEstimado = new BigDecimal("1412.00");

        // El cliente NO existe
        when(clienteRepository.findByWhatsappId("5493875999999"))
                .thenReturn(Optional.empty());

        // Simulamos el guardado del nuevo cliente
        when(clienteRepository.save(any(Cliente.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // Simulamos la tarifa
        when(tarifaService.obtenerTarifaParaFechaHora(any()))
                .thenReturn(tarifa);

        // Simulamos el cálculo del costo
        when(calculadorCostoService.calcularCosto(
                -24.7885,
                -65.4100,
                -24.7875,
                -65.4105,
                tarifa
        )).thenReturn(costoEstimado);

        // Simulamos el guardado del viaje
        when(viajeRepository.save(any(Viaje.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // Simulamos la respuesta del mapper
        ViajeResponse respuesta = ViajeResponse.builder()
                .id(1L)
                .clienteNombre("Carlos")
                .direccionOrigen("Av. Belgrano 1234, Salta")
                .direccionDestino("Plaza 9 de Julio, Salta")
                .tipoTarifa(TipoTarifa.NOCTURNA)
                .costoEstimado(costoEstimado)
                .build();

        when(viajeMapper.toResponse(any(Viaje.class)))
                .thenReturn(respuesta);

        when(tokenSolicitudViajeService.consumirToken("tokenPrueba"))
                .thenReturn(Optional.of("5493875999999"));

        // Ejecutamos el servicio REAL
        ViajeResponse resultado = viajeService.crearViaje(request);

        // Verificamos el resultado
        assertNotNull(resultado);
        assertEquals(1L, resultado.getId());
        assertEquals("Carlos", resultado.getClienteNombre());
        assertEquals(new BigDecimal("1412.00"),
                resultado.getCostoEstimado());

        // Verificamos que se buscó al cliente
        verify(clienteRepository)
                .findByWhatsappId("5493875999999");

        // Verificamos que se creó y guardó un cliente
        verify(clienteRepository)
                .save(any(Cliente.class));

        // Verificamos que el viaje se guardó con el cliente correcto
        ArgumentCaptor<Viaje> viajeCaptor =
                ArgumentCaptor.forClass(Viaje.class);

        verify(viajeRepository)
                .save(viajeCaptor.capture());

        Viaje viajeGuardado = viajeCaptor.getValue();

        assertNotNull(viajeGuardado.getCliente());
        assertEquals("5493875999999",
                viajeGuardado.getCliente().getWhatsappId());
        assertEquals("Carlos",
                viajeGuardado.getCliente().getNombre());

        // Verificamos la conversión a DTO
        verify(viajeMapper)
                .toResponse(any(Viaje.class));
    }

    @Test
    void debeLanzarExcepcionCuandoNoExisteElViaje() {

        org.mockito.Mockito.doReturn(
                List.of(new SimpleGrantedAuthority("ROLE_ADMIN"))
        ).when(authentication).getAuthorities();

        // Simulamos que el viaje no existe
        when(viajeRepository.findById(999L))
                .thenReturn(Optional.empty());

        // Ejecutamos el metodo y verificamos la excepción
        ViajeNoEncontradoException excepcion =
                assertThrows(
                        ViajeNoEncontradoException.class,
                        () -> viajeService.buscarPorId(999L, authentication)
                );

        // Verificamos el mensaje
        assertEquals(
                "No se encontró el viaje con id: 999",
                excepcion.getMessage()
        );

        // Verificamos que efectivamente se consultó el repositorio
        verify(viajeRepository)
                .findById(999L);

        // El mapper nunca debería ejecutarse
        verifyNoInteractions(viajeMapper);
    }

    @Test
    void debePermitirAlAdministradorConsultarCualquierViaje() {

        Chofer chofer = Chofer.builder()
                .id(1L)
                .build();

        Viaje viaje = Viaje.builder()
                .id(1L)
                .chofer(chofer)
                .build();

        ViajeResponse respuesta = ViajeResponse.builder()
                .id(1L)
                .build();

        org.mockito.Mockito.doReturn(
                List.of(new SimpleGrantedAuthority("ROLE_ADMIN"))
        ).when(authentication).getAuthorities();

        when(viajeRepository.findById(1L))
                .thenReturn(Optional.of(viaje));

        when(viajeMapper.toResponse(viaje))
                .thenReturn(respuesta);

        ViajeResponse resultado =
                viajeService.buscarPorId(1L, authentication);

        assertEquals(1L, resultado.getId());

        verify(viajeRepository).findById(1L);
        verify(viajeMapper).toResponse(viaje);
     }


     @Test
     void debePermitirAlChoferConsultarSuPropioViaje() {

        Chofer chofer = Chofer.builder()
                .id(1L)
                .build();

        Viaje viaje = Viaje.builder()
                .id(1L)
                .chofer(chofer)
                .build();

        ViajeResponse respuesta = ViajeResponse.builder()
                .id(1L)
                .build();

        when(usuarioAutenticadoService.obtenerChofer(authentication))
                .thenReturn(chofer);

        when(viajeRepository.findById(1L))
                .thenReturn(Optional.of(viaje));

        when(viajeMapper.toResponse(viaje))
                .thenReturn(respuesta);

        ViajeResponse resultado =
                viajeService.buscarPorId(1L, authentication);

        assertEquals(1L, resultado.getId());

        verify(viajeRepository).findById(1L);
        verify(viajeMapper).toResponse(viaje);
     }


     @Test
     void noDebePermitirAlChoferConsultarElViajeDeOtroChofer() {

        Chofer choferAsignado = Chofer.builder()
                .id(1L)
                .build();

        Chofer choferAutenticado = Chofer.builder()
                .id(2L)
                .build();

        Viaje viaje = Viaje.builder()
                .id(1L)
                .chofer(choferAsignado)
                .build();

        when(usuarioAutenticadoService.obtenerChofer(authentication))
                .thenReturn(choferAutenticado);

        when(viajeRepository.findById(1L))
                .thenReturn(Optional.of(viaje));

        assertThrows(
                AccesoNoAutorizadoException.class,
                () -> viajeService.buscarPorId(1L, authentication)
        );

        verify(viajeRepository).findById(1L);
        verifyNoInteractions(viajeMapper);
    }

    @Test
    void debeIniciarViajeCuandoEstaAceptado() {

        Chofer chofer = Chofer.builder()
                .id(1L)
                .build();

        Viaje viaje = Viaje.builder()
                .id(1L)
                .estado(EstadoViaje.ACEPTADO)
                .chofer(chofer)
                .build();

        when(viajeRepository.findById(1L))
                .thenReturn(Optional.of(viaje));

        when(usuarioAutenticadoService.obtenerChofer(authentication))
                .thenReturn(chofer);

        viajeService.iniciarViaje(1L, authentication);

        assertEquals(
                EstadoViaje.EN_CURSO,
                viaje.getEstado()
        );

        verify(viajeRepository)
                .findById(1L);

        verify(viajeRepository)
                .save(viaje);
    }

    @Test
    void noDebeIniciarViajeSiPerteneceAOtroChofer() {

        Chofer choferAsignado = Chofer.builder()
                .id(1L)
                .build();

        Chofer choferAutenticado = Chofer.builder()
                .id(2L)
                .build();

        Viaje viaje = Viaje.builder()
                .id(1L)
                .estado(EstadoViaje.ACEPTADO)
                .chofer(choferAsignado)
                .build();

        when(viajeRepository.findById(1L))
                .thenReturn(Optional.of(viaje));

        when(usuarioAutenticadoService.obtenerChofer(authentication))
                .thenReturn(choferAutenticado);

        assertThrows(
                AccesoNoAutorizadoException.class,
                () -> viajeService.iniciarViaje(1L, authentication)
        );

        assertEquals(EstadoViaje.ACEPTADO, viaje.getEstado());

        verify(viajeRepository, never())
                .save(any(Viaje.class));
    }

    @Test
    void noDebeIniciarViajeCuandoNoEstaAceptado() {

        Viaje viaje = Viaje.builder()
                .id(1L)
                .estado(EstadoViaje.SOLICITADO)
                .build();

        when(viajeRepository.findById(1L))
                .thenReturn(Optional.of(viaje));

        TransicionEstadoViajeException excepcion =
                assertThrows(
                        TransicionEstadoViajeException.class,
                        () -> viajeService.iniciarViaje(1L, authentication)
                );

        assertEquals(
                "No se puede iniciar el viaje porque no está aceptado",
                excepcion.getMessage()
        );

        assertEquals(
                EstadoViaje.SOLICITADO,
                viaje.getEstado()
        );

        verify(viajeRepository)
                .findById(1L);

        verify(viajeRepository, never())
                .save(any(Viaje.class));
    }

    @Test
    void debeFinalizarViajeYLiberarChofer() {

        Chofer chofer = Chofer.builder()
                .id(1L)
                .estado(EstadoChofer.OCUPADO)
                .build();

        Viaje viaje = Viaje.builder()
                .id(1L)
                .estado(EstadoViaje.EN_CURSO)
                .chofer(chofer)
                .build();

        when(viajeRepository.findById(1L))
                .thenReturn(Optional.of(viaje));

        when(usuarioAutenticadoService.obtenerChofer(authentication))
                .thenReturn(chofer);

        viajeService.finalizarViaje(1L, authentication);

        assertEquals(
                EstadoViaje.FINALIZADO,
                viaje.getEstado()
        );

        assertEquals(
                chofer,
                viaje.getChofer()
        );

        verify(choferService)
                .cambiarEstado(
                        chofer.getId(),
                        EstadoChofer.LIBRE
                );

        verify(viajeRepository)
                .save(viaje);
    }

    @Test
    void noDebeFinalizarViajeSiPerteneceAOtroChofer() {

        Chofer choferAsignado = Chofer.builder()
                .id(1L)
                .estado(EstadoChofer.OCUPADO)
                .build();

        Chofer choferAutenticado = Chofer.builder()
                .id(2L)
                .build();

        Viaje viaje = Viaje.builder()
                .id(1L)
                .estado(EstadoViaje.EN_CURSO)
                .chofer(choferAsignado)
                .build();

        when(viajeRepository.findById(1L))
                .thenReturn(Optional.of(viaje));

        when(usuarioAutenticadoService.obtenerChofer(authentication))
                .thenReturn(choferAutenticado);

        assertThrows(
                AccesoNoAutorizadoException.class,
                () -> viajeService.finalizarViaje(1L, authentication)
        );

        assertEquals(EstadoViaje.EN_CURSO, viaje.getEstado());

        verify(choferService, never())
                .cambiarEstado(anyLong(), any(EstadoChofer.class));

        verify(viajeRepository, never())
                .save(any(Viaje.class));
    }

    @Test
    void noDebeFinalizarViajeCuandoNoEstaEnCurso() {

        Chofer chofer = Chofer.builder()
                .id(1L)
                .estado(EstadoChofer.OCUPADO)
                .build();

        Viaje viaje = Viaje.builder()
                .id(1L)
                .estado(EstadoViaje.ACEPTADO)
                .chofer(chofer)
                .build();

        when(viajeRepository.findById(1L))
                .thenReturn(Optional.of(viaje));

        TransicionEstadoViajeException excepcion =
                assertThrows(
                        TransicionEstadoViajeException.class,
                        () -> viajeService.finalizarViaje(1L, authentication)
                );

        assertEquals(
                "No se puede finalizar el viaje porque no está en curso",
                excepcion.getMessage()
        );

        assertEquals(
                EstadoViaje.ACEPTADO,
                viaje.getEstado()
        );

        verify(choferService, never())
                .cambiarEstado(
                        anyLong(),
                        any(EstadoChofer.class)
                );

        verify(viajeRepository, never())
                .save(any(Viaje.class));
    }

    @Test
    void debeCancelarViajeCuandoEstaSolicitado() {

        String whatsappId = "5493871234567";

        Cliente cliente = Cliente.builder()
                .id(1L)
                .whatsappId(whatsappId)
                .build();

        Viaje viaje = Viaje.builder()
                .id(1L)
                .cliente(cliente)
                .estado(EstadoViaje.SOLICITADO)
                .build();

        when(viajeRepository
                .findByCliente_WhatsappIdAndEstadoInOrderByFechaCreacionDesc(
                        eq(whatsappId),
                        anyList()
                ))
                .thenReturn(List.of(viaje));

        when(viajeRepository.buscarPorIdConBloqueo(1L))
                .thenReturn(Optional.of(viaje));

        viajeService.cancelarViajePorWhatsapp(whatsappId);

        assertEquals(EstadoViaje.CANCELADO, viaje.getEstado());

        verify(ofertaViajeService).cancelarTodasLasOfertasPendientes(viaje);
        verify(viajeRepository).save(viaje);
     }

   @ParameterizedTest
   @EnumSource(
        value = EstadoViaje.class,
        names = {"ACEPTADO", "EN_CURSO", "FINALIZADO", "CANCELADO"}
   )
   void noDebeCancelarViajeCuandoEstadoNoLoPermite(EstadoViaje estado) {

        String whatsappId = "5493871234567";

        Cliente cliente = Cliente.builder()
                .id(1L)
                .whatsappId(whatsappId)
                .build();

        Viaje viaje = Viaje.builder()
                .id(1L)
                .cliente(cliente)
                .estado(estado)
                .build();

        when(viajeRepository
                .findByCliente_WhatsappIdAndEstadoInOrderByFechaCreacionDesc(
                        eq(whatsappId),
                        anyList()
                ))
                .thenReturn(List.of(viaje));

        when(viajeRepository.buscarPorIdConBloqueo(1L))
                .thenReturn(Optional.of(viaje));

        assertThrows(
                TransicionEstadoViajeException.class,
                () -> viajeService.cancelarViajePorWhatsapp(whatsappId)
        );

        assertEquals(estado, viaje.getEstado());

        verify(ofertaViajeService, never())
                .cancelarTodasLasOfertasPendientes(any());

        verify(viajeRepository, never()).save(any());
    }

    @Test
    void noDebeCancelarCuandoElClienteNoTieneViajesActivos() {

        String whatsappId = "5493871234567";

        when(viajeRepository
                .findByCliente_WhatsappIdAndEstadoInOrderByFechaCreacionDesc(
                        eq(whatsappId),
                        anyList()
                ))
                .thenReturn(List.of());

        assertThrows(
                TransicionEstadoViajeException.class,
                () -> viajeService.cancelarViajePorWhatsapp(whatsappId)
        );

        verify(viajeRepository, never()).buscarPorIdConBloqueo(anyLong());
        verify(ofertaViajeService, never())
                .cancelarTodasLasOfertasPendientes(any());
        verify(viajeRepository, never()).save(any());
    }
}