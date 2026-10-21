package com.unsa.taxis.repository;

import com.unsa.taxis.model.Viaje;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ViajeRepository extends JpaRepository<Viaje, Long> {

    // Busca y bloquea el viaje mientras se procesa la operación
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT v FROM Viaje v WHERE v.id = :id")
    Optional<Viaje> buscarPorIdConBloqueo(@Param("id") Long id);
}
