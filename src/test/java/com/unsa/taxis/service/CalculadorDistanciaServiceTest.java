package com.unsa.taxis.service;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CalculadorDistanciaServiceTest {

    private final CalculadorDistanciaService calculadorDistanciaService =
            new CalculadorDistanciaService();


    @Test
    void mismaUbicacionDebeDarDistanciaCero() {

        double distancia = calculadorDistanciaService.calcularDistanciaMetros(
                -24.7885,
                -65.4100,
                -24.7885,
                -65.4100
        );

        assertEquals(0.0, distancia, 0.001);
    }


    @Test
    void debeCalcularDistanciaAproximadaEntreDosPuntos() {

        double distancia = calculadorDistanciaService.calcularDistanciaMetros(
                -24.7885,
                -65.4100,
                -24.7875,
                -65.4105
        );

        assertTrue(distancia > 100);
        assertTrue(distancia < 200);
    }


    @Test
    void distanciaDebeSerSimetrica() {

        double distanciaAB = calculadorDistanciaService.calcularDistanciaMetros(
                -24.7885,
                -65.4100,
                -24.7875,
                -65.4105
        );

        double distanciaBA = calculadorDistanciaService.calcularDistanciaMetros(
                -24.7875,
                -65.4105,
                -24.7885,
                -65.4100
        );

        assertEquals(distanciaAB, distanciaBA, 0.001);
    }
}