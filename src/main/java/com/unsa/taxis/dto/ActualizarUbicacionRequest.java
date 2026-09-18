package com.unsa.taxis.dto;

import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ActualizarUbicacionRequest {

    @NotNull
    private Double latitud;

    @NotNull
    private Double longitud;
}