package com.unsa.taxis.repository;

import com.unsa.taxis.model.EstadoOferta;
import com.unsa.taxis.model.OfertaViaje;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.List;

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
}