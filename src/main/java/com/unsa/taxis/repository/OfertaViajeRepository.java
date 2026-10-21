package com.unsa.taxis.repository;

import com.unsa.taxis.model.EstadoOferta;
import com.unsa.taxis.model.OfertaViaje;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.List;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.Optional;

@Repository
public interface OfertaViajeRepository
        extends JpaRepository<OfertaViaje, Long> {

    List<OfertaViaje> findByViajeIdAndEstado(
            Long viajeId,
            EstadoOferta estado
    );

    List<OfertaViaje> findByViajeId(Long viajeId);

    List<OfertaViaje> findByEstadoAndFechaExpiracionLessThanEqual(
            EstadoOferta estado,
            OffsetDateTime fecha
    );

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT o FROM OfertaViaje o WHERE o.id = :id")
    Optional<OfertaViaje> buscarPorIdConBloqueo(@Param("id") Long id);
}