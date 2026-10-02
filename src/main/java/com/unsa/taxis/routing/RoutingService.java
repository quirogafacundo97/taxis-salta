package com.unsa.taxis.routing;

public interface RoutingService {

    RutaResponse calcularRuta(
            double latitudOrigen,
            double longitudOrigen,
            double latitudDestino,
            double longitudDestino
    );
}