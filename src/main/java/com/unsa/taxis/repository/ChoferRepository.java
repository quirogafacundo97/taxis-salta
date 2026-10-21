package com.unsa.taxis.repository;

import com.unsa.taxis.model.Chofer;
import com.unsa.taxis.model.EstadoChofer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.Query;

@Repository
public interface ChoferRepository extends JpaRepository<Chofer, Long> {

    // Spring Data JPA creará la consulta SQL automáticamente en segundo plano
    Optional<Chofer> findByDni(String dni);

    Optional<Chofer> findByTelefonoContacto(String telefonoContacto);

    // Busca todos los choferes que estén en un estado específico (ej: LIBRE)
    List<Chofer> findByEstado(EstadoChofer estado);

    List<Chofer> findByEstadoAndHabilitadoAmt(EstadoChofer estado, Boolean habilitadoAmt);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT c FROM Chofer c WHERE c.id = :id")
    Optional<Chofer> buscarPorIdConBloqueo(@Param("id") Long id);
}