
package com.unsa.taxis.repository;

import com.unsa.taxis.PostgresIntegrationTest;
import com.unsa.taxis.model.Rol;
import com.unsa.taxis.model.Usuario;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class UsuarioRepositoryIntegrationTest extends PostgresIntegrationTest {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private RolRepository rolRepository;

    @Test
    @Transactional
    void debeGuardarYRecuperarUsuarioConMultiplesRoles() {

        // 1. Recuperamos los roles existentes
        Rol rolAdmin = rolRepository.findByNombre("ROLE_ADMIN")
                .orElseThrow();

        Rol rolAuditor = rolRepository.findByNombre("ROLE_AUDITOR_AMT")
                .orElseThrow();

        // 2. Creamos un usuario administrador
        Usuario usuario = Usuario.builder()
                .username("12345678")
                .password("$2a$10$password-encriptado-test")
                .nombreCompleto("Administrador Test")
                .activo(true)
                .debeCambiarPassword(true)
                .roles(Set.of(rolAdmin, rolAuditor))
                .build();

        // 3. Guardamos el usuario
        Usuario guardado = usuarioRepository.save(usuario);

        assertNotNull(guardado.getId());

        // 4. Recuperamos el usuario por username
        Usuario recuperado = usuarioRepository
                .findByUsername("12345678")
                .orElseThrow();

        // 5. Verificamos sus datos
        assertEquals("12345678", recuperado.getUsername());
        assertTrue(recuperado.getActivo());
        assertTrue(recuperado.getDebeCambiarPassword());
        assertNull(recuperado.getChofer());

        // 6. Verificamos que conserve ambos roles
        assertEquals(2, recuperado.getRoles().size());

        assertTrue(
                recuperado.getRoles().stream()
                        .anyMatch(rol -> rol.getNombre().equals("ROLE_ADMIN"))
        );

        assertTrue(
                recuperado.getRoles().stream()
                        .anyMatch(rol -> rol.getNombre().equals("ROLE_AUDITOR_AMT"))
        );
    }
}
