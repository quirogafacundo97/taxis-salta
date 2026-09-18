package com.unsa.taxis.repository;

import com.unsa.taxis.model.Propietario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface PropietarioRepository extends JpaRepository<Propietario, Long> {
    Optional<Propietario> findByDni(String dni);
}
