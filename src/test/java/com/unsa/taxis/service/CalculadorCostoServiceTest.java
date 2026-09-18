package com.unsa.taxis.service;

import com.unsa.taxis.model.Tarifa;
import com.unsa.taxis.model.TipoTarifa;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CalculadorCostoServiceTest {

    private final CalculadorDistanciaService calculadorDistanciaService =
            new CalculadorDistanciaService();

    private final CalculadorCostoService calculadorCostoService =
            new CalculadorCostoService(calculadorDistanciaService);
    @Test
    void mismaUbicacionDebeCobrarSoloBajadaDeBandera() {

        Tarifa tarifa = Tarifa.builder()
                .tipo(TipoTarifa.NOCTURNA)
                .bajadaBandera(new BigDecimal("1176.00"))
                .valorFicha(new BigDecimal("118.00"))
                .build();

        BigDecimal resultado = calculadorCostoService.calcularCosto(
                -24.7885,
                -65.4100,
                -24.7885,
                -65.4100,
                tarifa
        );

        assertEquals(
                new BigDecimal("1176.00"),
                resultado
        );
    }

    @Test
    void distanciaCortaDebeCobrarUnaFicha() {

        Tarifa tarifa = Tarifa.builder()
                .tipo(TipoTarifa.NOCTURNA)
                .bajadaBandera(new BigDecimal("1176.00"))
                .valorFicha(new BigDecimal("118.00"))
                .build();

        BigDecimal resultado = calculadorCostoService.calcularCosto(
                -24.7885,
                -65.4100,
                -24.7890,
                -65.4100,
                tarifa
        );

        assertEquals(
                new BigDecimal("1294.00"),
                resultado
        );
    }

    @Test
    void distanciaMediaDebeCobrarDosFichas() {

        Tarifa tarifa = Tarifa.builder()
                .tipo(TipoTarifa.NOCTURNA)
                .bajadaBandera(new BigDecimal("1176.00"))
                .valorFicha(new BigDecimal("118.00"))
                .build();

        BigDecimal resultado = calculadorCostoService.calcularCosto(
                -24.7885,
                -65.4100,
                -24.7900,
                -65.4100,
                tarifa
        );

        assertEquals(
                new BigDecimal("1412.00"),
                resultado
        );
    }

    @Test
    void distanciaMediaConTarifaDiurnaDebeCalcularCorrectamente() {

        Tarifa tarifa = Tarifa.builder()
                .tipo(TipoTarifa.DIURNA)
                .bajadaBandera(new BigDecimal("980.00"))
                .valorFicha(new BigDecimal("98.00"))
                .build();

        BigDecimal resultado = calculadorCostoService.calcularCosto(
                -24.7885,
                -65.4100,
                -24.7900,
                -65.4100,
                tarifa
        );

        assertEquals(
                new BigDecimal("1176.00"),
                resultado
        );
    }

    @Test
    void distanciaLargaDebeCobrarTresFichas() {

        Tarifa tarifa = Tarifa.builder()
                .tipo(TipoTarifa.DIURNA)
                .bajadaBandera(new BigDecimal("980.00"))
                .valorFicha(new BigDecimal("98.00"))
                .build();

        BigDecimal resultado = calculadorCostoService.calcularCosto(
                -24.7885,
                -65.4100,
                -24.7910,
                -65.4100,
                tarifa
        );

        assertEquals(
                new BigDecimal("1274.00"),
                resultado
        );
    }
}