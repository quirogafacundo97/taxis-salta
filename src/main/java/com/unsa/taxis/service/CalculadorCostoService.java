package com.unsa.taxis.service;

import com.unsa.taxis.model.Tarifa;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
public class CalculadorCostoService {

    private static final double METROS_POR_FICHA = 100.0;

    private final CalculadorDistanciaService calculadorDistanciaService;

    public CalculadorCostoService(
            CalculadorDistanciaService calculadorDistanciaService) {

        this.calculadorDistanciaService = calculadorDistanciaService;
    }

    public BigDecimal calcularCosto(
            Double latitudOrigen,
            Double longitudOrigen,
            Double latitudDestino,
            Double longitudDestino,
            Tarifa tarifa
    ) {

        double distanciaMetros =
                calculadorDistanciaService.calcularDistanciaMetros(
                        latitudOrigen,
                        longitudOrigen,
                        latitudDestino,
                        longitudDestino
                );

        long cantidadFichas = (long) Math.ceil(
                distanciaMetros / METROS_POR_FICHA
        );

        BigDecimal costoFichas = tarifa.getValorFicha()
                .multiply(BigDecimal.valueOf(cantidadFichas));

        return tarifa.getBajadaBandera()
                .add(costoFichas)
                .setScale(2, RoundingMode.HALF_UP);
    }
}