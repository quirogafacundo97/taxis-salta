package com.unsa.taxis.repository;

import com.unsa.taxis.PostgresIntegrationTest;
import com.unsa.taxis.model.Propietario;
import com.unsa.taxis.model.Vehiculo;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_CLASS)
class VehiculoRepositoryIntegrationTest extends PostgresIntegrationTest {

    @Autowired
    private VehiculoRepository vehiculoRepository;

    @Autowired
    private PropietarioRepository propietarioRepository;

    @Test
    @Transactional
    void debeGuardarYRecuperarVehiculoConSuPropietario() {

        // 1. Crear propietario
        Propietario propietario = Propietario.builder()
                .nombre("Juan")
                .apellido("Perez")
                .dni("30123456")
                .telefono("3875551234")
                .build();

        propietario = propietarioRepository.save(propietario);

        // 2. Crear vehículo
        Vehiculo vehiculo = Vehiculo.builder()
                .patente("AB123CD")
                .licencia("LIC-001")
                .marca("Toyota")
                .modelo("Etios")
                .anio(2020)
                .nroRelojFullmar("FULLMAR-001")
                .propietario(propietario)
                .build();

        // 3. Guardar vehículo
        Vehiculo guardado = vehiculoRepository.save(vehiculo);

        assertNotNull(guardado.getId());
        assertNotNull(guardado.getFechaCreacion());

        // 4. Recuperar vehículo desde PostgreSQL
        Vehiculo recuperado = vehiculoRepository.findById(guardado.getId())
                .orElseThrow();

        // 5. Verificar datos del vehículo
        assertEquals("AB123CD", recuperado.getPatente());
        assertEquals("LIC-001", recuperado.getLicencia());
        assertEquals("Toyota", recuperado.getMarca());
        assertEquals("Etios", recuperado.getModelo());
        assertEquals(2020, recuperado.getAnio());
        assertEquals("FULLMAR-001", recuperado.getNroRelojFullmar());

        // 6. Verificar propietario
        assertNotNull(recuperado.getPropietario());
        assertEquals(
                propietario.getId(),
                recuperado.getPropietario().getId()
        );
        assertEquals("Juan", recuperado.getPropietario().getNombre());
        assertEquals("Perez", recuperado.getPropietario().getApellido());
        assertEquals("30123456", recuperado.getPropietario().getDni());
    }
}