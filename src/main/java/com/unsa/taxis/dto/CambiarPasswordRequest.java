package com.unsa.taxis.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CambiarPasswordRequest(

        @NotBlank(message = "La nueva contraseña es obligatoria")
        @Size(min = 8, max = 72,
                message = "La contraseña debe tener entre 8 y 72 caracteres")
        String nuevaPassword,

        @NotBlank(message = "La confirmación de contraseña es obligatoria")
        String confirmarNuevaPassword

) {
}