
package com.unsa.taxis.service;

import com.unsa.taxis.dto.LoginRequest;
import com.unsa.taxis.dto.LoginResponse;
import com.unsa.taxis.exception.CredencialesInvalidasException;
import com.unsa.taxis.model.Usuario;
import com.unsa.taxis.repository.UsuarioRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.unsa.taxis.dto.CambiarPasswordRequest;
import com.unsa.taxis.exception.AccesoNoAutorizadoException;
import org.springframework.security.core.Authentication;
import com.unsa.taxis.exception.NuevaPasswordNoCoincideException;

import java.util.List;

@Service
public class AuthService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(
            UsuarioRepository usuarioRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService) {

        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @Transactional(readOnly = true)
    public LoginResponse iniciarSesion(LoginRequest request) {

        Usuario usuario = usuarioRepository
                .findByUsername(request.username())
                .orElseThrow(CredencialesInvalidasException::new);

        if (!usuario.getActivo()
                || !passwordEncoder.matches(
                        request.password(),
                        usuario.getPassword())) {

            throw new CredencialesInvalidasException();
        }

        List<String> roles = usuario.getRoles()
                .stream()
                .map(rol -> rol.getNombre())
                .toList();

        String token;

        if (usuario.getDebeCambiarPassword()) {

                token = jwtService.generarTokenCambioPassword(
                        usuario.getUsername()
                );

        } else {

                token = jwtService.generarToken(
                        usuario.getUsername(),
                        roles
                );
        }

        return new LoginResponse(
                token,
                usuario.getUsername(),
                usuario.getNombreCompleto(),
                roles,
                usuario.getDebeCambiarPassword()
        );
    }

    @Transactional
    public void cambiarPassword(CambiarPasswordRequest request, Authentication authentication) {

        if (authentication == null
                || !authentication.isAuthenticated()
                || authentication.getName() == null) {

                throw new CredencialesInvalidasException();
        }

        Usuario usuario = usuarioRepository
                .findByUsername(authentication.getName())
                .orElseThrow(CredencialesInvalidasException::new);

        if (!usuario.getActivo()) {
                throw new AccesoNoAutorizadoException(
                        "La cuenta se encuentra desactivada"
                );
        }

        if (!usuario.getDebeCambiarPassword()) {
                throw new AccesoNoAutorizadoException(
                        "La cuenta no requiere cambiar la contraseña"
                );
        }

        if (!request.nuevaPassword().equals(
                request.confirmarNuevaPassword())) {

                throw new NuevaPasswordNoCoincideException();
        }

        usuario.setPassword(
                passwordEncoder.encode(request.nuevaPassword())
        );

        usuario.setDebeCambiarPassword(false);

        usuarioRepository.save(usuario);
    }
}
