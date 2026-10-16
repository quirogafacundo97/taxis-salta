package com.unsa.taxis.service;

import com.unsa.taxis.model.Tarifa;
import com.unsa.taxis.routing.RoutingException;
import com.unsa.taxis.routing.RoutingService;
import com.unsa.taxis.routing.RutaResponse;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class CalculadorCostoServiceTest {

    private final CalculadorDistanciaService calculadorDistanciaService =
            new CalculadorDistanciaService();

    private final RoutingService routingService = mock(RoutingService.class);

    private final CalculadorCostoService calculadorCostoService =
            new CalculadorCostoService(
                    calculadorDistanciaService,
                    routingService
            );

    @Test
    void mismaUbicacionDebeCobrarSoloBajadaDeBandera() {
        when(routingService.calcularRuta(
                -24.7885, -65.4100, -24.7885, -65.4100
        )).thenReturn(new RutaResponse(0, 0));

        Tarifa tarifa = crearTarifa("1176", "118");

        BigDecimal costo = calculadorCostoService.calcularCosto(
                -24.7885, -65.4100,
                -24.7885, -65.4100,
                tarifa
        );

        assertEquals(new BigDecimal("1176.00"), costo);
    }

    @Test
    void distanciaDeOsrmDebeUsarseParaCalcularElCosto() {
        // 250 metros por ruta: se cobran 3 fichas.
        when(routingService.calcularRuta(
                -24.7885, -65.4100, -24.7900, -65.4100
        )).thenReturn(new RutaResponse(250, 300));

        Tarifa tarifa = crearTarifa("1176", "118");

        BigDecimal costo = calculadorCostoService.calcularCosto(
                -24.7885, -65.4100,
                -24.7900, -65.4100,
                tarifa
        );

        assertEquals(new BigDecimal("1530.00"), costo);
    }

    @Test
    void siOsrmFallaDebeUsarLaDistanciaHaversine() {
        when(routingService.calcularRuta(
                -24.7885, -65.4100, -24.7900, -65.4100
        )).thenThrow(new RoutingException(
                "No se pudo calcular la ruta con OSRM"
        ));

        Tarifa tarifa = crearTarifa("1176", "118");

        BigDecimal costo = calculadorCostoService.calcularCosto(
                -24.7885, -65.4100,
                -24.7900, -65.4100,
                tarifa
        );

        // La distancia en línea recta es de aproximadamente 167 metros.
        assertEquals(new BigDecimal("1412.00"), costo);
    }

    @Test
    void distanciaCortaConRespaldoDebeCobrarUnaFicha() {
        when(routingService.calcularRuta(
                -24.7885, -65.4100, -24.7890, -65.4100
        )).thenThrow(new RoutingException(
                "No se pudo calcular la ruta con OSRM"
        ));

        Tarifa tarifa = crearTarifa("1176", "118");

        BigDecimal costo = calculadorCostoService.calcularCosto(
                -24.7885, -65.4100,
                -24.7890, -65.4100,
                tarifa
        );

        assertEquals(new BigDecimal("1294.00"), costo);
    }

    @Test
    void distanciaMediaConTarifaDiurnaDebeCalcularCorrectamente() {
        when(routingService.calcularRuta(
                -24.7885, -65.4100, -24.7900, -65.4100
        )).thenReturn(new RutaResponse(150, 200));

        Tarifa tarifa = crearTarifa("980", "98");

        BigDecimal costo = calculadorCostoService.calcularCosto(
                -24.7885, -65.4100,
                -24.7900, -65.4100,
                tarifa
        );

        assertEquals(new BigDecimal("1176.00"), costo);
    }

    @Test
    void distanciaLargaConRespaldoDebeCobrarTresFichas() {
        when(routingService.calcularRuta(
                -24.7885, -65.4100, -24.7910, -65.4100
        )).thenThrow(new RoutingException(
                "No se pudo calcular la ruta con OSRM"
        ));

       Tarifa tarifa = crearTarifa("980", "98");

        BigDecimal costo = calculadorCostoService.calcularCosto(
                -24.7885, -65.4100,
                -24.7910, -65.4100,
                tarifa
        );

        assertEquals(new BigDecimal("1274.00"), costo);
    }

       private Tarifa crearTarifa(
            String bajadaBandera,
            String valorFicha
        ) {
                Tarifa tarifa = new Tarifa();
                tarifa.setBajadaBandera(new BigDecimal(bajadaBandera));
                tarifa.setValorFicha(new BigDecimal(valorFicha));
                return tarifa;
        }

}
