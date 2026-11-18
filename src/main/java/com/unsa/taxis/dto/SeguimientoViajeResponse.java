package com.unsa.taxis.dto;

import com.unsa.taxis.model.EstadoViaje;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class SeguimientoViajeResponse {

    private EstadoViaje estado;

    private String choferNombre;
    private String choferApellido;

    private String vehiculoMarca;
    private String vehiculoModelo;
    private String vehiculoPatente;
    private String vehiculoLicencia;
}