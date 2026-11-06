
package com.unsa.taxis.repository;

import com.unsa.taxis.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.EntityGraph;

import java.util.Optional;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    Optional<Usuario> findByUsername(String username);

    @EntityGraph(attributePaths = "chofer")
    Optional<Usuario> findWithChoferByUsername(String username);

    boolean existsByUsername(String username);

    Optional<Usuario> findByChoferId(Long choferId);
}
