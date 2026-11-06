package com.unsa.taxis.service;

import com.unsa.taxis.exception.CredencialesInvalidasException;
import com.unsa.taxis.model.Chofer;
import com.unsa.taxis.model.Usuario;
import com.unsa.taxis.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.unsa.taxis.exception.AccesoNoAutorizadoException;

@Service
@RequiredArgsConstructor
public class UsuarioAutenticadoService {

    private final UsuarioRepository usuarioRepository;

    @Transactional(readOnly = true)
    public Usuario obtenerUsuario(Authentication authentication) {

        if (authentication == null
                || !authentication.isAuthenticated()
                || authentication.getName() == null) {
            throw new CredencialesInvalidasException();
        }

        return usuarioRepository
                .findWithChoferByUsername(authentication.getName())
                .filter(Usuario::getActivo)
                .orElseThrow(CredencialesInvalidasException::new);
    }

    @Transactional(readOnly = true)
    public Chofer obtenerChofer(Authentication authentication) {

        Usuario usuario = obtenerUsuario(authentication);

        if (usuario.getChofer() == null) {
            throw new AccesoNoAutorizadoException(
                "El usuario autenticado no tiene un chofer asociado"
            );
        }

        return usuario.getChofer();
    }
}