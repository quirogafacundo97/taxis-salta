package com.unsa.taxis.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record ActualizarPropietarioRequest(

        @NotBlank(message = "El nombre es obligatorio")
        @Size(max = 50, message = "El nombre no puede superar los 50 caracteres")
        String nombre,

        @NotBlank(message = "El apellido es obligatorio")
        @Size(max = 50, message = "El apellido no puede superar los 50 caracteres")
        String apellido,

        @NotBlank(message = "El DNI es obligatorio")
        @Pattern(
                regexp = "^[0-9]{7,8}$",
                message = "El DNI debe contener entre 7 y 8 dígitos"
        )
        String dni,

        @Size(max = 20, message = "El teléfono no puede superar los 20 caracteres")
        String telefono

) {
}