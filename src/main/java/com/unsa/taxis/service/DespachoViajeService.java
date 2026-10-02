package com.unsa.taxis.service;

import com.unsa.taxis.dto.ChoferCercanoResponse;
import com.unsa.taxis.model.Chofer;
import com.unsa.taxis.model.EstadoOferta;
import com.unsa.taxis.model.OfertaViaje;
import com.unsa.taxis.model.Viaje;
import com.unsa.taxis.routing.RoutingException;
import com.unsa.taxis.routing.RoutingService;
import com.unsa.taxis.routing.RutaResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DespachoViajeService {

    private static final double RADIO_INICIAL_KM = 2.0;
    private static final double RADIO_AMPLIADO_KM = 4.0;
    private static final double RADIO_MAXIMO_KM = 6.0;
    private static final int CHOFERES_POR_RONDA = 3;

    private final ChoferService choferService;
    private final OfertaViajeService ofertaViajeService;
    private final ViajeService viajeService;
    private final RoutingService routingService;

    public void despacharViaje(Viaje viaje) {

        if (viaje.getChofer() != null) {
            return;
        }

        despacharNuevaRonda(viaje);
    }

    private List<ChoferConRuta> obtenerCandidatos(Viaje viaje, double radioKm) {

        List<ChoferCercanoResponse> choferesCercanos =
                choferService.buscarChoferesCercanos(
                        viaje.getLatitudOrigen(),
                        viaje.getLongitudOrigen(),
                        radioKm
                );

        List<OfertaViaje> ofertasAnteriores =
                ofertaViajeService.listarOfertasPorViaje(viaje.getId());

        Set<Long> choferesYaOfrecidos =
                ofertasAnteriores.stream()
                        .map(oferta -> oferta.getChofer().getId())
                        .collect(Collectors.toSet());

        List<ChoferConRuta> candidatosConRuta = new ArrayList<>();
        List<ChoferConRuta> candidatosSinRuta = new ArrayList<>();

        for (ChoferCercanoResponse choferCercano : choferesCercanos) {

            Chofer chofer = choferCercano.getChofer();

            if (choferesYaOfrecidos.contains(chofer.getId())) {
                continue;
            }

            try {
                RutaResponse ruta = routingService.calcularRuta(
                        viaje.getLatitudOrigen(),
                        viaje.getLongitudOrigen(),
                        chofer.getLatitud(),
                        chofer.getLongitud()
                );

                candidatosConRuta.add(
                        new ChoferConRuta(
                                chofer,
                                choferCercano.getDistanciaMetros(),
                                ruta.getDuracionSegundos()
                        )
                );

            } catch (RoutingException e) {

                candidatosSinRuta.add(
                        new ChoferConRuta(
                                chofer,
                                choferCercano.getDistanciaMetros(),
                                null
                        )
                );
            }
        }

        // Si al menos un chofer tiene ruta, usamos las rutas calculadas.
        if (!candidatosConRuta.isEmpty()) {
            return candidatosConRuta.stream()
                    .sorted(Comparator.comparingDouble(
                            candidato -> candidato.getDuracionSegundos()
                    ))
                    .toList();
        }

        // Si OSRM falló para todos, usamos Haversine como fallback.
        return candidatosSinRuta.stream()
                .sorted(Comparator.comparingDouble(
                        ChoferConRuta::getDistanciaMetros
                ))
                .toList();
    }

    public void continuarDespacho(Viaje viaje) {

        if (viaje.getChofer() != null) {
            return;
        }

        List<OfertaViaje> ofertas =
                ofertaViajeService.listarOfertasPorViaje(viaje.getId());

        boolean hayOfertasPendientes =
                ofertas.stream()
                        .anyMatch(oferta ->
                                oferta.getEstado() == EstadoOferta.PENDIENTE
                        );

        if (hayOfertasPendientes) {
            return;
        }

        despacharNuevaRonda(viaje);
    }

    public void continuarDespacho(Long viajeId) {

        Viaje viaje = viajeService.buscarEntidadPorId(viajeId);

        continuarDespacho(viaje);
    }

    private void despacharNuevaRonda(Viaje viaje) {

        List<ChoferConRuta> candidatos =
                obtenerCandidatos(viaje, RADIO_INICIAL_KM);

        if (candidatos.isEmpty()) {
            candidatos =
                    obtenerCandidatos(viaje, RADIO_AMPLIADO_KM);
        }

        if (candidatos.isEmpty()) {
            candidatos =
                    obtenerCandidatos(viaje, RADIO_MAXIMO_KM);
        }

        candidatos.stream()
                .limit(CHOFERES_POR_RONDA)
                .map(ChoferConRuta::getChofer)
                .forEach(chofer ->
                        ofertaViajeService.crearOferta(viaje, chofer)
                );
    }
}