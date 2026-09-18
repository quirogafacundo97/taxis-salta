package com.unsa.taxis.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CrearViajeRequest {

    @NotBlank
    private String whatsappId;

    private String nombreCliente;

    @NotBlank
    private String direccionOrigen;

    @NotNull
    private Double latitudOrigen;

    @NotNull
    private Double longitudOrigen;

    @NotBlank
    private String direccionDestino;

    @NotNull
    private Double latitudDestino;

    @NotNull
    private Double longitudDestino;
}