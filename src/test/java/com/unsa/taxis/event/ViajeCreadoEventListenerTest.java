package com.unsa.taxis.event;

import com.unsa.taxis.service.DespachoViajeService;
import com.unsa.taxis.service.ViajeService;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import com.unsa.taxis.model.Viaje;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
class ViajeCreadoEventListenerTest {

    @Mock
    private ViajeService viajeService;

    @Mock
    private DespachoViajeService despachoViajeService;

    @InjectMocks
    private ViajeCreadoEventListener listener;

    @Test
    void alCrearViaje_deberiaDespacharElViajeRecibido() {
        Long viajeId = 10L;
        Viaje viaje = new Viaje();

        when(viajeService.buscarEntidadPorId(viajeId))
                .thenReturn(viaje);

        listener.alCrearViaje(new ViajeCreadoEvent(viajeId));

        verify(viajeService).buscarEntidadPorId(viajeId);
        verify(despachoViajeService).despacharViaje(viaje);
    }

    @Test
    void alCrearViaje_siFallaLaBusqueda_noDebeDespachar() {
        Long viajeId = 10L;

        when(viajeService.buscarEntidadPorId(viajeId))
                .thenThrow(new RuntimeException("Error al buscar el viaje"));

        listener.alCrearViaje(new ViajeCreadoEvent(viajeId));

        verify(viajeService).buscarEntidadPorId(viajeId);
        verify(despachoViajeService, never()).despacharViaje(any());
    }
}