package com.unsa.taxis.routing;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OsrmRoutingServiceTest {

    @Test
    void deberiaCalcularRutaConOsrm() {

        OsrmRoutingService routingService =
                new OsrmRoutingService("https://router.project-osrm.org");

        RutaResponse ruta = routingService.calcularRuta(
                -24.782,
                -65.411,
                -24.795,
                -65.400
        );

        assertNotNull(ruta);
        assertTrue(ruta.getDistanciaMetros() > 0);
        assertTrue(ruta.getDuracionSegundos() > 0);
    }
}