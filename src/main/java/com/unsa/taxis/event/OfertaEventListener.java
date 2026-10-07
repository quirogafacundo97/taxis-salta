package com.unsa.taxis.event;

import com.unsa.taxis.model.OfertaViaje;
import com.unsa.taxis.model.Viaje;
import com.unsa.taxis.service.DespachoViajeService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import com.unsa.taxis.service.OfertaViajeService;

@Component
@RequiredArgsConstructor
public class OfertaEventListener {

    private final DespachoViajeService despachoViajeService;
    private final OfertaViajeService ofertaViajeService;

    @EventListener
    public void manejarOfertaRechazada(OfertaRechazadaEvent event) {
        OfertaViaje oferta =
                ofertaViajeService.buscarPorId(event.ofertaId());

        Viaje viaje = oferta.getViaje();

        despachoViajeService.continuarDespacho(viaje);
    }

    @EventListener
    public void manejarOfertaVencida(OfertaVencidaEvent event) {
        OfertaViaje oferta =
                ofertaViajeService.buscarPorId(event.ofertaId());

        Viaje viaje = oferta.getViaje();

        despachoViajeService.continuarDespacho(viaje);
    }

}