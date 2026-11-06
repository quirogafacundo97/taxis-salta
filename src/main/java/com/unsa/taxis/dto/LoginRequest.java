
package com.unsa.taxis.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record LoginRequest(

        @NotBlank(message = "El DNI es obligatorio")
        @Pattern(
                regexp = "^[0-9]{7,8}$",
                message = "El DNI debe contener entre 7 y 8 dígitos"
        )
        String username,

        @NotBlank(message = "La contraseña es obligatoria")
        String password

) {
}
