package com.unsa.taxis.repository;

import com.unsa.taxis.model.Cliente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface ClienteRepository extends JpaRepository<Cliente, Long> {
    // Permite verificar si el número que escribe ya está registrado en el sistema.
    Optional<Cliente> findByWhatsappId(String whatsappId);
}