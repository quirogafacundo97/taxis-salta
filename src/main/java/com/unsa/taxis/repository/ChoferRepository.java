package com.unsa.taxis.repository;

import com.unsa.taxis.model.Chofer;
import com.unsa.taxis.model.EstadoChofer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface ChoferRepository extends JpaRepository<Chofer, Long> {

    // Spring Data JPA creará la consulta SQL automáticamente en segundo plano
    Optional<Chofer> findByDni(String dni);

    Optional<Chofer> findByTelefonoContacto(String telefonoContacto);

    // Busca todos los choferes que estén en un estado específico (ej: LIBRE)
    List<Chofer> findByEstado(EstadoChofer estado);

    List<Chofer> findByEstadoAndHabilitadoAmt(EstadoChofer estado, Boolean habilitadoAmt);
}