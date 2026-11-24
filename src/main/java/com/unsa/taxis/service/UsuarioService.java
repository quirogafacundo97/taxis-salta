package com.unsa.taxis.service;

import com.unsa.taxis.dto.UsuarioDTO;
import com.unsa.taxis.model.Usuario;
import com.unsa.taxis.repository.UsuarioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;

    public UsuarioService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    @Transactional(readOnly = true)
    public List<UsuarioDTO> listarTodos() {

        return usuarioRepository.findAll()
                .stream()
                .map(this::convertirADTO)
                .toList();
    }

    private UsuarioDTO convertirADTO(Usuario usuario) {

        List<String> roles = usuario.getRoles()
                .stream()
                .map(rol -> rol.getNombre())
                .toList();

        return UsuarioDTO.builder()
                .id(usuario.getId())
                .username(usuario.getUsername())
                .nombreCompleto(usuario.getNombreCompleto())
                .email(usuario.getEmail())
                .roles(roles)
                .activo(usuario.getActivo())
                .build();
    }
}
