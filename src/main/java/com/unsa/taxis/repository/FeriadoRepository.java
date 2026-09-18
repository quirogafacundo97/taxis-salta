package com.unsa.taxis.repository;

import com.unsa.taxis.model.Feriado;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;

@Repository
public interface FeriadoRepository extends JpaRepository<Feriado, Long> {

    boolean existsByFecha(LocalDate fecha);
}