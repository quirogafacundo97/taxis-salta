package com.unsa.taxis.repository;

import com.unsa.taxis.model.AsignacionChoferVehiculo;
import com.unsa.taxis.model.Chofer;
import com.unsa.taxis.model.Vehiculo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AsignacionChoferVehiculoRepository
        extends JpaRepository<AsignacionChoferVehiculo, Long> {

    // Busca la asignación actualmente activa de un chofer
    Optional<AsignacionChoferVehiculo> findByChoferAndFechaFinIsNull(Chofer chofer);

    // Busca la asignación actualmente activa de un vehículo
    Optional<AsignacionChoferVehiculo> findByVehiculoAndFechaFinIsNull(Vehiculo vehiculo);
}