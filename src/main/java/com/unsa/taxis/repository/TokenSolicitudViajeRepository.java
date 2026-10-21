package com.unsa.taxis.repository;

import com.unsa.taxis.model.TokenSolicitudViaje;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface TokenSolicitudViajeRepository
        extends JpaRepository<TokenSolicitudViaje, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            SELECT t
            FROM TokenSolicitudViaje t
            WHERE t.tokenHash = :tokenHash
            """)
    Optional<TokenSolicitudViaje> buscarPorHashParaActualizar(
            @Param("tokenHash") String tokenHash
    );
}