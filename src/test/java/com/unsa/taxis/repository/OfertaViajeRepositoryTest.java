package com.unsa.taxis.repository;

import org.springframework.transaction.annotation.Transactional;
import com.unsa.taxis.model.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class OfertaViajeRepositoryTest {

    @Autowired
    private OfertaViajeRepository ofertaViajeRepository;

    @Autowired
    private ClienteRepository clienteRepository;

    @Autowired
    private ChoferRepository choferRepository;

    @Autowired
    private ViajeRepository viajeRepository;

    @Test
    @Transactional
    void debeGuardarYRecuperarOfertaViaje() {

        // 1. Creamos un cliente
        Cliente cliente = Cliente.builder()
                .whatsappId("5493875559999")
                .nombre("Cliente Test")
                .build();

        cliente = clienteRepository.save(cliente);

        // 2. Creamos un chofer
        Chofer chofer = Chofer.builder()
                .nombre("Chofer")
                .apellido("Test")
                .dni("99999999")
                .telefonoContacto("3875559999")
                .habilitadoAmt(true)
                .estado(EstadoChofer.LIBRE)
                .latitud(-24.7885)
                .longitud(-65.4100)
                .build();

        chofer = choferRepository.save(chofer);

        // 3. Creamos un viaje
        Viaje viaje = Viaje.builder()
                .cliente(cliente)
                .direccionOrigen("Plaza 9 de Julio")
                .latitudOrigen(-24.7885)
                .longitudOrigen(-65.4100)
                .direccionDestino("Universidad Nacional de Salta")
                .latitudDestino(-24.7280)
                .longitudDestino(-65.4080)
                .estado(EstadoViaje.SOLICITADO)
                .tipoTarifa(TipoTarifa.DIURNA)
                .costoEstimado(new BigDecimal("1500.00"))
                .build();

        viaje = viajeRepository.save(viaje);

        // 4. Creamos la oferta
        OffsetDateTime fechaExpiracion =
                OffsetDateTime.now().plusSeconds(10);

        OfertaViaje oferta = OfertaViaje.builder()
                .viaje(viaje)
                .chofer(chofer)
                .estado(EstadoOferta.PENDIENTE)
                .fechaExpiracion(fechaExpiracion)
                .build();

        // 5. Guardamos la oferta
        OfertaViaje guardada =
                ofertaViajeRepository.save(oferta);

        // 6. Verificamos que Hibernate haya generado el ID
        assertNotNull(guardada.getId());

        // 7. Verificamos que fechaEnvio haya sido generada automáticamente
        assertNotNull(guardada.getFechaEnvio());

        // 8. Recuperamos la oferta desde PostgreSQL
        OfertaViaje recuperada =
                ofertaViajeRepository
                        .findById(guardada.getId())
                        .orElseThrow();

        // 9. Verificamos las relaciones
        assertEquals(
                viaje.getId(),
                recuperada.getViaje().getId()
        );

        assertEquals(
                chofer.getId(),
                recuperada.getChofer().getId()
        );

        // 10. Verificamos el estado
        assertEquals(
                EstadoOferta.PENDIENTE,
                recuperada.getEstado()
        );

        // 11. Verificamos las fechas
        assertNotNull(recuperada.getFechaEnvio());

        assertEquals(
                fechaExpiracion.toInstant().truncatedTo(ChronoUnit.MICROS),
                recuperada.getFechaExpiracion().toInstant().truncatedTo(ChronoUnit.MICROS)
        );
    }

    @Test
    @Transactional
    void debeBuscarOfertasPendientesDeUnViaje() {

        // 1. Crear cliente
        Cliente cliente = Cliente.builder()
                .whatsappId("5493875559999")
                .nombre("Cliente Test")
                .build();

        cliente = clienteRepository.save(cliente);

        // 2. Crear tres choferes
        Chofer chofer1 = choferRepository.save(
                Chofer.builder()
                        .nombre("Juan")
                        .apellido("Test")
                        .dni("99999991")
                        .telefonoContacto("3875559991")
                        .habilitadoAmt(true)
                        .estado(EstadoChofer.LIBRE)
                        .latitud(-24.7885)
                        .longitud(-65.4100)
                        .build()
        );

        Chofer chofer2 = choferRepository.save(
                Chofer.builder()
                        .nombre("Carlos")
                        .apellido("Test")
                        .dni("99999992")
                        .telefonoContacto("3875559992")
                        .habilitadoAmt(true)
                        .estado(EstadoChofer.LIBRE)
                        .latitud(-24.7890)
                        .longitud(-65.4110)
                        .build()
        );

        Chofer chofer3 = choferRepository.save(
                Chofer.builder()
                        .nombre("Mario")
                        .apellido("Test")
                        .dni("99999993")
                        .telefonoContacto("3875559993")
                        .habilitadoAmt(true)
                        .estado(EstadoChofer.LIBRE)
                        .latitud(-24.7900)
                        .longitud(-65.4120)
                        .build()
        );

        // 3. Crear viaje
        Viaje viaje = Viaje.builder()
                .cliente(cliente)
                .direccionOrigen("Plaza 9 de Julio")
                .latitudOrigen(-24.7885)
                .longitudOrigen(-65.4100)
                .direccionDestino("Universidad Nacional de Salta")
                .latitudDestino(-24.7280)
                .longitudDestino(-65.4080)
                .estado(EstadoViaje.SOLICITADO)
                .tipoTarifa(TipoTarifa.DIURNA)
                .costoEstimado(new BigDecimal("1500.00"))
                .build();

        viaje = viajeRepository.save(viaje);

        // 4. Crear tres ofertas para el mismo viaje
        OfertaViaje oferta1 = OfertaViaje.builder()
                .viaje(viaje)
                .chofer(chofer1)
                .estado(EstadoOferta.PENDIENTE)
                .fechaExpiracion(OffsetDateTime.now().plusSeconds(10))
                .build();

        OfertaViaje oferta2 = OfertaViaje.builder()
                .viaje(viaje)
                .chofer(chofer2)
                .estado(EstadoOferta.RECHAZADA)
                .fechaExpiracion(OffsetDateTime.now().plusSeconds(10))
                .build();

        OfertaViaje oferta3 = OfertaViaje.builder()
                .viaje(viaje)
                .chofer(chofer3)
                .estado(EstadoOferta.PENDIENTE)
                .fechaExpiracion(OffsetDateTime.now().plusSeconds(10))
                .build();

        ofertaViajeRepository.save(oferta1);
        ofertaViajeRepository.save(oferta2);
        ofertaViajeRepository.save(oferta3);

        // 5. Ejecutar el metodo que estamos probando
        var ofertasPendientes =
                ofertaViajeRepository.findByViajeIdAndEstado(
                        viaje.getId(),
                        EstadoOferta.PENDIENTE
                );

        // 6. Verificar resultado
        assertEquals(2, ofertasPendientes.size());

        assertTrue(
                ofertasPendientes.stream()
                        .allMatch(oferta ->
                                oferta.getEstado() == EstadoOferta.PENDIENTE)
        );

        assertTrue(
                ofertasPendientes.stream()
                        .anyMatch(oferta ->
                                oferta.getChofer().getId().equals(chofer1.getId()))
        );

        assertTrue(
                ofertasPendientes.stream()
                        .anyMatch(oferta ->
                                oferta.getChofer().getId().equals(chofer3.getId()))
        );

        assertFalse(
                ofertasPendientes.stream()
                        .anyMatch(oferta ->
                                oferta.getChofer().getId().equals(chofer2.getId()))
        );
    }
}

