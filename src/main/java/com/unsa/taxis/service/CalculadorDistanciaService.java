package com.unsa.taxis.service;

import org.springframework.stereotype.Service;

@Service
public class CalculadorDistanciaService {

    private static final double RADIO_TIERRA_METROS = 6_371_000;

    public double calcularDistanciaMetros(
            Double latitudOrigen,
            Double longitudOrigen,
            Double latitudDestino,
            Double longitudDestino) {

        double diferenciaLatitud = Math.toRadians(
                latitudDestino - latitudOrigen
        );

        double diferenciaLongitud = Math.toRadians(
                longitudDestino - longitudOrigen
        );

        double latitudOrigenRad = Math.toRadians(latitudOrigen);
        double latitudDestinoRad = Math.toRadians(latitudDestino);

        double a = Math.sin(diferenciaLatitud / 2)
                * Math.sin(diferenciaLatitud / 2)
                + Math.cos(latitudOrigenRad)
                * Math.cos(latitudDestinoRad)
                * Math.sin(diferenciaLongitud / 2)
                * Math.sin(diferenciaLongitud / 2);

        double c = 2 * Math.atan2(
                Math.sqrt(a),
                Math.sqrt(1 - a)
        );

        return RADIO_TIERRA_METROS * c;
    }
}