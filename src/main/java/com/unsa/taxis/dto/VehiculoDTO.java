
package com.unsa.taxis.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VehiculoDTO {

    private Long id;
    private String patente;
    private String licencia;
    private String marca;
    private String modelo;
    private Integer anio;
    private String nroRelojFullmar;

    private Long propietarioId;
    private String propietarioNombre;
    private String propietarioApellido;
    private String propietarioDni;
}
