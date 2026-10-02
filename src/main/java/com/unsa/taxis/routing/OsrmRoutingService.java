package com.unsa.taxis.routing;

import org.springframework.web.client.RestClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class OsrmRoutingService implements RoutingService {

    private final RestClient restClient;

    public OsrmRoutingService(
            @Value("${routing.osrm.url}") String osrmUrl
    ) {
        this.restClient = RestClient.builder()
                .baseUrl(osrmUrl)
                .build();
    }

    @Override
    public RutaResponse calcularRuta(
            double latitudOrigen,
            double longitudOrigen,
            double latitudDestino,
            double longitudDestino
    ) {
        String coordenadas = longitudOrigen + "," + latitudOrigen
                + ";" + longitudDestino + "," + latitudDestino;

        try {
            OsrmResponse respuesta = restClient.get()
                    .uri("/route/v1/driving/" + coordenadas + "?overview=false")
                    .retrieve()
                    .body(OsrmResponse.class);

            if (respuesta == null
                    || respuesta.routes() == null
                    || respuesta.routes().isEmpty()) {
                throw new RoutingException("OSRM no devolvió una ruta");
            }

            OsrmRoute ruta = respuesta.routes().get(0);

            return new RutaResponse(
                    ruta.distance(),
                    ruta.duration()
            );

        } catch (RoutingException e) {
            throw e;
        } catch (Exception e) {
            throw new RoutingException(
                    "No se pudo calcular la ruta con OSRM",
                    e
            );
        }
    }

    private record OsrmResponse(
            List<OsrmRoute> routes
    ) {
    }

    private record OsrmRoute(
            double distance,
            double duration
    ) {
    }
}