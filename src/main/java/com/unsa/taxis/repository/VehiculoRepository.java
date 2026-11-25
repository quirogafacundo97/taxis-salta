package com.unsa.taxis.repository;

import com.unsa.taxis.model.Vehiculo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface VehiculoRepository extends JpaRepository<Vehiculo, Long> {
    long countByPropietario_Id(Long propietarioId);

    boolean existsByPatente(String patente);
    boolean existsByLicencia(String licencia);
    boolean existsByNroRelojFullmar(String nroRelojFullmar);

    boolean existsByPatenteAndIdNot(String patente, Long id);
    boolean existsByLicenciaAndIdNot(String licencia, Long id);
    boolean existsByNroRelojFullmarAndIdNot(String nroRelojFullmar, Long id);
}
