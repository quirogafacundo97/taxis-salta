package com.unsa.taxis.dto;

import com.unsa.taxis.model.TipoTarifa;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Getter
@Builder
public class OfertaChoferResponse {

    private Long ofertaId;
    private Long viajeId;

    private String clienteNombre;
    private String whatsappId;

    private String direccionOrigen;
    private Double latitudOrigen;
    private Double longitudOrigen;

    private String direccionDestino;
    private Double latitudDestino;
    private Double longitudDestino;

    private TipoTarifa tipoTarifa;
    private BigDecimal costoEstimado;

    private String rutaGeometria;
    private Integer distanciaMetros;
    private Integer duracionSegundos;

    private OffsetDateTime fechaEnvio;
    private OffsetDateTime fechaExpiracion;
}