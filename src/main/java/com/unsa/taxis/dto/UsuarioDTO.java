package com.unsa.taxis.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UsuarioDTO {

    private Long id;
    private String username;
    private String nombreCompleto;
    private String email;
    private List<String> roles;
    private Boolean activo;
}
