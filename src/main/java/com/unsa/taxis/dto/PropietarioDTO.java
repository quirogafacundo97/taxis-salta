package com.unsa.taxis.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PropietarioDTO {

    private Long id;
    private String nombre;
    private String apellido;
    private String dni;
    private String telefono;
    private Long cantidadVehiculos;
}