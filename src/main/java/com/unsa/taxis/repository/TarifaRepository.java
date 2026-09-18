package com.unsa.taxis.repository;

import com.unsa.taxis.model.Tarifa;
import com.unsa.taxis.model.TipoTarifa;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;

@Repository
public interface TarifaRepository extends JpaRepository<Tarifa, Long> {

    @Query("""
        SELECT t
        FROM Tarifa t
        WHERE t.tipo = :tipo
          AND t.fechaDesde <= :fecha
          AND (t.fechaHasta IS NULL OR t.fechaHasta >= :fecha)
        """)
    Optional<Tarifa> buscarTarifaVigente(
            @Param("tipo") TipoTarifa tipo,
            @Param("fecha") LocalDate fecha
    );
}