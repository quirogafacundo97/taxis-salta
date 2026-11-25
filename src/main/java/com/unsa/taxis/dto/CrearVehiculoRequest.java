
package com.unsa.taxis.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;

public record CrearVehiculoRequest(

        @NotBlank(message = "La patente es obligatoria")
        @Size(max = 10, message = "La patente no puede superar los 10 caracteres")
        String patente,

        @NotBlank(message = "La licencia es obligatoria")
        @Size(max = 20, message = "La licencia no puede superar los 20 caracteres")
        String licencia,

        @NotBlank(message = "La marca es obligatoria")
        @Size(max = 50, message = "La marca no puede superar los 50 caracteres")
        String marca,

        @NotBlank(message = "El modelo es obligatorio")
        @Size(max = 100, message = "El modelo no puede superar los 100 caracteres")
        String modelo,

        @NotNull(message = "El año es obligatorio")
        @Min(value = 1900, message = "El año no puede ser anterior a 1900")
        @Max(value = 2100, message = "El año no puede ser posterior a 2100")
        Integer anio,

        @NotBlank(message = "El número de reloj Fullmar es obligatorio")
        @Size(max = 50, message = "El número de reloj Fullmar no puede superar los 50 caracteres")
        String nroRelojFullmar,

        @NotNull(message = "El propietario es obligatorio")
        Long propietarioId

) {
}
