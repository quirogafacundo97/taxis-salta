package com.unsa.taxis.dto;

import com.unsa.taxis.model.EstadoChofer;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CambiarEstadoChoferRequest {

    @NotNull
    private EstadoChofer estado;
}