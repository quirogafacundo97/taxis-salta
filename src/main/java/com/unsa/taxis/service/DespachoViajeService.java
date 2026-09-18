package com.unsa.taxis.service;

import com.unsa.taxis.dto.ChoferCercanoResponse;
import com.unsa.taxis.model.EstadoOferta;
import com.unsa.taxis.model.OfertaViaje;
import com.unsa.taxis.model.Viaje;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

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

    public void despacharViaje(Viaje viaje) {

        if (viaje.getChofer() != null) {
            return;
        }

        despacharNuevaRonda(viaje);
    }

    private List<ChoferCercanoResponse> obtenerCandidatos(
            Viaje viaje,
            double radioKm) {

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

        return choferesCercanos.stream()
                .filter(chofer ->
                        !choferesYaOfrecidos.contains(
                                chofer.getChofer().getId()
                        )
                )
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

        Viaje viaje = viajeService.buscarPorId(viajeId);

        continuarDespacho(viaje);
    }

    private void despacharNuevaRonda(Viaje viaje) {

        List<ChoferCercanoResponse> candidatos =
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
                .map(ChoferCercanoResponse::getChofer)
                .forEach(chofer ->
                        ofertaViajeService.crearOferta(viaje, chofer)
                );
    }
}