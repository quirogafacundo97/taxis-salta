package com.unsa.taxis.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChoferDTO {
    private Long id;
    private String nombre;
    private String apellido;
    private String dni;
    private String telefonoContacto;
    private boolean habilitadoAmt;
    private String estado;
    private String zonaActual;

    // Simplificamos el vehículo y su propietario en lugar de anidar objetos gigantes
    private String vehiculoPatente;
    private String vehiculoModelo;
    private String vehiculoLicencia;
    private String propietarioNombreCompleto; // Ej: "Ana Maria Valdez"
}