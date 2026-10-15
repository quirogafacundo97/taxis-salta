package com.unsa.taxis.event;

import com.unsa.taxis.model.Viaje;
import com.unsa.taxis.service.DespachoViajeService;
import com.unsa.taxis.service.ViajeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.transaction.event.TransactionPhase;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class ViajeCreadoEventListener {

    private final ViajeService viajeService;
    private final DespachoViajeService despachoViajeService;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void alCrearViaje(ViajeCreadoEvent event) {
        try {
            Viaje viaje = viajeService.buscarEntidadPorId(event.viajeId());
            despachoViajeService.despacharViaje(viaje);
        } catch (Exception e) {
            log.error(
                    "Error al iniciar el despacho del viaje con ID {}",
                    event.viajeId(),
                    e
            );
        }
    }

}

