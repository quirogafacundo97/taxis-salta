
package com.unsa.taxis.service;

import com.unsa.taxis.dto.LoginRequest;
import com.unsa.taxis.dto.LoginResponse;
import com.unsa.taxis.exception.CredencialesInvalidasException;
import com.unsa.taxis.model.Rol;
import com.unsa.taxis.model.Usuario;
import com.unsa.taxis.repository.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.Set;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.unsa.taxis.dto.CambiarPasswordRequest;
import com.unsa.taxis.exception.AccesoNoAutorizadoException;
import com.unsa.taxis.exception.NuevaPasswordNoCoincideException;
import org.springframework.security.core.Authentication;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private AuthService authService;

    @Test
    void debeIniciarSesionCorrectamente() {

        Usuario usuario = Usuario.builder()
                .username("12345678")
                .password("hash-password")
                .nombreCompleto("Administrador Test")
                .activo(true)
                .debeCambiarPassword(true)
                .roles(Set.of(
                        Rol.builder()
                                .id(1L)
                                .nombre("ROLE_ADMIN")
                                .build()
                ))
                .build();

        when(usuarioRepository.findByUsername("12345678"))
                .thenReturn(Optional.of(usuario));

        when(passwordEncoder.matches("password123", "hash-password"))
                .thenReturn(true);

        when(jwtService.generarTokenCambioPassword("12345678"))
                .thenReturn("jwt-de-prueba");

        LoginResponse response = authService.iniciarSesion(
                new LoginRequest("12345678", "password123")
        );

        assertEquals("jwt-de-prueba", response.token());
        assertEquals("12345678", response.username());
        assertEquals("Administrador Test", response.nombreCompleto());
        assertEquals(List.of("ROLE_ADMIN"), response.roles());
        assertTrue(response.debeCambiarPassword());

        verify(jwtService).generarTokenCambioPassword("12345678");
        verify(jwtService, never()).generarToken(anyString(), anyList());
    }

    @Test
    void debeIniciarSesionConTokenNormalCuandoNoDebeCambiarPassword() {

        Usuario usuario = Usuario.builder()
                .username("12345678")
                .password("hash-password")
                .nombreCompleto("Administrador Test")
                .activo(true)
                .debeCambiarPassword(false)
                .roles(Set.of(
                        Rol.builder()
                                .id(1L)
                                .nombre("ROLE_ADMIN")
                                .build()
                ))
                .build();

        when(usuarioRepository.findByUsername("12345678"))
                .thenReturn(Optional.of(usuario));

        when(passwordEncoder.matches("password123", "hash-password"))
                .thenReturn(true);

        when(jwtService.generarToken("12345678", List.of("ROLE_ADMIN")))
                .thenReturn("jwt-normal");

        LoginResponse response = authService.iniciarSesion(
                new LoginRequest("12345678", "password123")
        );

        assertEquals("jwt-normal", response.token());
        assertFalse(response.debeCambiarPassword());

        verify(jwtService).generarToken("12345678", List.of("ROLE_ADMIN"));
        verify(jwtService, never()).generarTokenCambioPassword(anyString());
    }

    @Test
    void debeCambiarPasswordCorrectamente() {

        Usuario usuario = Usuario.builder()
                .username("12345678")
                .password("hash-temporal")
                .activo(true)
                .debeCambiarPassword(true)
                .build();

        Authentication authentication = mock(Authentication.class);

        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn("12345678");

        when(usuarioRepository.findByUsername("12345678"))
                .thenReturn(Optional.of(usuario));

        when(passwordEncoder.encode("nuevaPassword123"))
                .thenReturn("nuevo-hash");

        authService.cambiarPassword(
                new CambiarPasswordRequest("nuevaPassword123", "nuevaPassword123"),
                authentication
        );

        assertEquals("nuevo-hash", usuario.getPassword());
        assertFalse(usuario.getDebeCambiarPassword());

        verify(usuarioRepository).save(usuario);
    }

    @Test
    void debeRechazarPasswordQueNoCoincide() {

        Usuario usuario = Usuario.builder()
                .username("12345678")
                .password("hash-temporal")
                .activo(true)
                .debeCambiarPassword(true)
                .build();

        Authentication authentication = mock(Authentication.class);

        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn("12345678");

        when(usuarioRepository.findByUsername("12345678"))
                .thenReturn(Optional.of(usuario));

        assertThrows(
                NuevaPasswordNoCoincideException.class,
                () -> authService.cambiarPassword(
                        new CambiarPasswordRequest("nuevaPassword123", "otraPassword123"),
                        authentication
                )
        );

        verify(passwordEncoder, never()).encode(anyString());
        verify(usuarioRepository, never()).save(any());
    }

    @Test
    void debeRechazarCambioSiLaCuentaNoLoRequiere() {

        Usuario usuario = Usuario.builder()
                .username("12345678")
                .password("hash-actual")
                .activo(true)
                .debeCambiarPassword(false)
                .build();

        Authentication authentication = mock(Authentication.class);

        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn("12345678");

        when(usuarioRepository.findByUsername("12345678"))
                .thenReturn(Optional.of(usuario));

        assertThrows(
                AccesoNoAutorizadoException.class,
                () -> authService.cambiarPassword(
                        new CambiarPasswordRequest("nuevaPassword123", "nuevaPassword123"),
                        authentication
                )
        );

        verify(passwordEncoder, never()).encode(anyString());
        verify(usuarioRepository, never()).save(any());
    }

    @Test
    void debeRechazarCambioPasswordSiLaCuentaEstaInactiva() {

        Usuario usuario = Usuario.builder()
                .username("12345678")
                .password("hash-temporal")
                .activo(false)
                .debeCambiarPassword(true)
                .build();

        Authentication authentication = mock(Authentication.class);

        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn("12345678");

        when(usuarioRepository.findByUsername("12345678"))
                .thenReturn(Optional.of(usuario));

        assertThrows(
                AccesoNoAutorizadoException.class,
                () -> authService.cambiarPassword(
                        new CambiarPasswordRequest("nuevaPassword123", "nuevaPassword123"),
                        authentication
                )
        );

        verify(passwordEncoder, never()).encode(anyString());
        verify(usuarioRepository, never()).save(any());
    }

    @Test
    void debeRechazarDniInexistente() {

        when(usuarioRepository.findByUsername("12345678"))
                .thenReturn(Optional.empty());

        assertThrows(
                CredencialesInvalidasException.class,
                () -> authService.iniciarSesion(
                        new LoginRequest("12345678", "password123")
                )
        );

        verifyNoInteractions(passwordEncoder, jwtService);
    }

    @Test
    void debeRechazarPasswordIncorrecta() {

        Usuario usuario = Usuario.builder()
                .username("12345678")
                .password("hash-password")
                .activo(true)
                .build();

        when(usuarioRepository.findByUsername("12345678"))
                .thenReturn(Optional.of(usuario));

        when(passwordEncoder.matches("incorrecta", "hash-password"))
                .thenReturn(false);

        assertThrows(
                CredencialesInvalidasException.class,
                () -> authService.iniciarSesion(
                        new LoginRequest("12345678", "incorrecta")
                )
        );

        verifyNoInteractions(jwtService);
    }

    @Test
    void debeRechazarCuentaInactiva() {

        Usuario usuario = Usuario.builder()
                .username("12345678")
                .password("hash-password")
                .activo(false)
                .build();

        when(usuarioRepository.findByUsername("12345678"))
                .thenReturn(Optional.of(usuario));

        assertThrows(
                CredencialesInvalidasException.class,
                () -> authService.iniciarSesion(
                        new LoginRequest("12345678", "password123")
                )
        );

        verifyNoInteractions(jwtService);
    }
}
