package com.unsa.taxis.scheduler;

import com.unsa.taxis.model.OfertaViaje;
import com.unsa.taxis.service.OfertaViajeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class OfertaViajeScheduler {

    private final OfertaViajeService ofertaViajeService;

    @Scheduled(fixedDelay = 1000)
    public void procesarOfertasExpiradas() {

        log.info("Buscando ofertas expiradas...");

        List<OfertaViaje> ofertasExpiradas =
                ofertaViajeService.listarOfertasExpiradas();

        if (!ofertasExpiradas.isEmpty()) {

            log.info(
                    "Se encontraron {} ofertas expiradas",
                    ofertasExpiradas.size()
            );
        }

        for (OfertaViaje oferta : ofertasExpiradas) {

            log.info(
                    "Venciendo oferta {} del viaje {}",
                    oferta.getId(),
                    oferta.getViaje().getId()
            );

            ofertaViajeService.vencerOferta(oferta.getId());
        }
    }
}