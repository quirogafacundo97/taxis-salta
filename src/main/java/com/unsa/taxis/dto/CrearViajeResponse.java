package com.unsa.taxis.dto;

import com.unsa.taxis.model.EstadoViaje;
import com.unsa.taxis.model.TipoTarifa;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Getter
@Builder
public class CrearViajeResponse {

    private Long id;
    private String clienteNombre;
    private Long choferId;
    private String choferNombre;
    private String choferApellido;
    private String direccionOrigen;
    private String direccionDestino;
    private EstadoViaje estado;
    private TipoTarifa tipoTarifa;
    private BigDecimal costoEstimado;
    private String rutaGeometria;
    private Integer distanciaMetros;
    private Integer duracionSegundos;
    private OffsetDateTime fechaCreacion;

    private String tokenSeguimiento;
}