package com.unsa.taxis.scheduler;

import com.unsa.taxis.model.EstadoChofer;
import com.unsa.taxis.model.Cliente;
import com.unsa.taxis.model.TipoTarifa;
import com.unsa.taxis.repository.ClienteRepository;
import com.unsa.taxis.repository.ChoferRepository;
import com.unsa.taxis.repository.ViajeRepository;

import com.unsa.taxis.PostgresIntegrationTest;
import com.unsa.taxis.model.Chofer;
import com.unsa.taxis.model.EstadoOferta;
import com.unsa.taxis.model.OfertaViaje;
import com.unsa.taxis.model.Viaje;
import com.unsa.taxis.repository.OfertaViajeRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import java.util.List;

import java.time.OffsetDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
class OfertaViajeSchedulerIntegrationTest extends PostgresIntegrationTest {

    @Autowired
    private OfertaViajeScheduler scheduler;

    @Autowired
    private OfertaViajeRepository ofertaViajeRepository;

    @Autowired
    private ClienteRepository clienteRepository;

    @Autowired
    private ChoferRepository choferRepository;

    @Autowired
    private ViajeRepository viajeRepository;

    @Test
    void debeVencerOfertaYContinuarElDespacho() {

        Cliente cliente = Cliente.builder()
        .whatsappId("5493875551234")
        .nombre("Juan")
        .build();

        cliente = clienteRepository.save(cliente);

        Viaje viaje = Viaje.builder()
                .cliente(cliente)
                .direccionOrigen("Plaza 9 de Julio, Salta")
                .latitudOrigen(-24.7821)
                .longitudOrigen(-65.4122)
                .direccionDestino("Monumento a Güemes, Salta")
                .latitudDestino(-24.7827)
                .longitudDestino(-65.4005)
                .tipoTarifa(TipoTarifa.DIURNA)
                .build();

        viaje = viajeRepository.save(viaje);

        Chofer chofer = Chofer.builder()
                .nombre("Juan")
                .apellido("Quiroga")
                .dni("12345678")
                .telefonoContacto("3875555555")
                .habilitadoAmt(true)
                .build();

        chofer = choferRepository.save(chofer);

        Chofer segundoChofer = Chofer.builder()
        .nombre("Pedro")
        .apellido("Gomez")
        .dni("87654321")
        .telefonoContacto("3875555556")
        .habilitadoAmt(true)
        .estado(EstadoChofer.LIBRE)
        .latitud(-24.7820)
        .longitud(-65.4120)
        .build();

        segundoChofer = choferRepository.save(segundoChofer);

        OfertaViaje oferta = OfertaViaje.builder()
                .viaje(viaje)
                .chofer(chofer)
                .estado(EstadoOferta.PENDIENTE)
                .fechaExpiracion(
                        OffsetDateTime.now().minusSeconds(1)
                )
                .build();

        ofertaViajeRepository.save(oferta);

        scheduler.procesarOfertasExpiradas();

        OfertaViaje ofertaActualizada =
                ofertaViajeRepository.findById(oferta.getId())
                        .orElseThrow();

        assertEquals(
                EstadoOferta.VENCIDA,
                ofertaActualizada.getEstado()
        );

        List<OfertaViaje> ofertas =
        ofertaViajeRepository.findByViajeId(viaje.getId());

        assertEquals(2, ofertas.size());

        assertEquals(
                segundoChofer.getId(),
                ofertas.get(1).getChofer().getId()
        );

        assertEquals(
                EstadoOferta.PENDIENTE,
                ofertas.get(1).getEstado()
        );
    }
}