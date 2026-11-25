package com.unsa.taxis.repository;

import com.unsa.taxis.PostgresIntegrationTest;
import com.unsa.taxis.model.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_CLASS)
class AsignacionChoferVehiculoRepositoryIntegrationTest
        extends PostgresIntegrationTest {

    @Autowired
    private AsignacionChoferVehiculoRepository asignacionRepository;

    @Autowired
    private ChoferRepository choferRepository;

    @Autowired
    private VehiculoRepository vehiculoRepository;

    @Autowired
    private PropietarioRepository propietarioRepository;

    @Test
    @Transactional
    void unChoferPuedeTenerVariosVehiculosVigentes() {

        Chofer chofer = crearChofer("11111111", "3875551111");

        Vehiculo vehiculo1 = crearVehiculo(
                "AA111AA", "LIC-111", "FULLMAR-111"
        );

        Vehiculo vehiculo2 = crearVehiculo(
                "AA222AA", "LIC-222", "FULLMAR-222"
        );

        AsignacionChoferVehiculo asignacion1 = crearAsignacion(
                chofer, vehiculo1
        );

        AsignacionChoferVehiculo asignacion2 = crearAsignacion(
                chofer, vehiculo2
        );

        asignacionRepository.saveAndFlush(asignacion1);
        asignacionRepository.saveAndFlush(asignacion2);

        assertEquals(2, asignacionRepository.findAll().size());
        assertTrue(asignacion1.getFechaFin() == null);
        assertTrue(asignacion2.getFechaFin() == null);
    }

    @Test
    @Transactional
    void unVehiculoNoPuedeTenerDosChoferesVigentes() {

        Chofer chofer1 = crearChofer("22222221", "3875552221");
        Chofer chofer2 = crearChofer("22222222", "3875552222");

        Vehiculo vehiculo = crearVehiculo(
                "AA333AA", "LIC-333", "FULLMAR-333"
        );

        asignacionRepository.saveAndFlush(
                crearAsignacion(chofer1, vehiculo)
        );

        AsignacionChoferVehiculo segundaAsignacion =
                crearAsignacion(chofer2, vehiculo);

        assertThrows(
                DataIntegrityViolationException.class,
                () -> asignacionRepository.saveAndFlush(segundaAsignacion)
        );
    }

    @Test
    @Transactional
    void unVehiculoPuedeReasignarseCuandoFinalizaLaAsignacionAnterior() {

        Chofer chofer1 = crearChofer("33333331", "3875553331");
        Chofer chofer2 = crearChofer("33333332", "3875553332");

        Vehiculo vehiculo = crearVehiculo(
                "AA444AA", "LIC-444", "FULLMAR-444"
        );

        AsignacionChoferVehiculo asignacionAnterior =
                crearAsignacion(chofer1, vehiculo);

        asignacionAnterior.setFechaFin(OffsetDateTime.now());

        asignacionRepository.saveAndFlush(asignacionAnterior);

        AsignacionChoferVehiculo nuevaAsignacion =
                crearAsignacion(chofer2, vehiculo);

        asignacionRepository.saveAndFlush(nuevaAsignacion);

        assertEquals(2, asignacionRepository.findAll().size());
    }

    private Chofer crearChofer(String dni, String telefono) {

        return choferRepository.save(
                Chofer.builder()
                        .nombre("Chofer")
                        .apellido("Test")
                        .dni(dni)
                        .telefonoContacto(telefono)
                        .habilitadoAmt(true)
                        .estado(EstadoChofer.LIBRE)
                        .latitud(-24.7885)
                        .longitud(-65.4100)
                        .build()
        );
    }

    private Vehiculo crearVehiculo(
            String patente,
            String licencia,
            String nroRelojFullmar
    ) {

        Propietario propietario = propietarioRepository.save(
                Propietario.builder()
                        .nombre("Propietario")
                        .apellido("Test")
                        .dni("30" + licencia.substring(4))
                        .telefono("3875550000")
                        .build()
        );

        return vehiculoRepository.save(
                Vehiculo.builder()
                        .patente(patente)
                        .licencia(licencia)
                        .marca("Toyota")
                        .modelo("Etios")
                        .anio(2020)
                        .nroRelojFullmar(nroRelojFullmar)
                        .propietario(propietario)
                        .build()
        );
    }

    private AsignacionChoferVehiculo crearAsignacion(
            Chofer chofer,
            Vehiculo vehiculo
    ) {

        return AsignacionChoferVehiculo.builder()
                .chofer(chofer)
                .vehiculo(vehiculo)
                .fechaInicio(OffsetDateTime.now())
                .build();
    }
}