package com.unsa.taxis.service;

import com.unsa.taxis.dto.CrearViajeRequest;
import com.unsa.taxis.dto.ViajeResponse;
import com.unsa.taxis.mapper.ViajeMapper;
import com.unsa.taxis.model.Cliente;
import com.unsa.taxis.model.Tarifa;
import com.unsa.taxis.model.TipoTarifa;
import com.unsa.taxis.model.Viaje;
import com.unsa.taxis.repository.ClienteRepository;
import com.unsa.taxis.repository.ViajeRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.ArgumentCaptor;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

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

    @InjectMocks
    private ViajeService viajeService;


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
                .whatsappId("5493875551234")
                .nombreCliente("Juan")
                .direccionOrigen("Av. Belgrano 1234, Salta")
                .latitudOrigen(-24.7885)
                .longitudOrigen(-65.4100)
                .direccionDestino("Plaza 9 de Julio, Salta")
                .latitudDestino(-24.7875)
                .longitudDestino(-65.4105)
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
                .thenAnswer(invocation -> invocation.getArgument(0));

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

        verify(viajeRepository)
                .save(any(Viaje.class));

        verify(viajeMapper).toResponse(any(Viaje.class));

        verify(clienteRepository, never())
                .save(any(Cliente.class));
    }

    @Test
    void debeCrearClienteNuevoAlCrearViaje() {

        CrearViajeRequest request = CrearViajeRequest.builder()
                .whatsappId("5493875999999")
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
}