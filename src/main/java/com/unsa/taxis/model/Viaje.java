package com.unsa.taxis.model;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Entity
@Table(name = "viajes")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Viaje {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // RELACIÓN CON CLIENTE: Muchos viajes pertenecen a un cliente
    @ManyToOne(optional = false)
    @JoinColumn(name = "cliente_id", nullable = false)
    private Cliente cliente;

    // RELACIÓN CON CHOFER: Muchos viajes pueden ser asignados a un chofer
    // Es opcional (null) cuando el cliente recién solicita el viaje y aún no tiene chofer
    @ManyToOne
    @JoinColumn(name = "chofer_id")
    private Chofer chofer;

    @Column(name = "direccion_origen", nullable = false)
    private String direccionOrigen;

    @Column(name = "latitud_origen", nullable = false)
    private Double latitudOrigen;

    @Column(name = "longitud_origen", nullable = false)
    private Double longitudOrigen;

    @Column(name = "direccion_destino", nullable = false)
    private String direccionDestino;

    @Column(name = "latitud_destino", nullable = false)
    private Double latitudDestino;

    @Column(name = "longitud_destino", nullable = false)
    private Double longitudDestino;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false, length = 20)
    @Builder.Default
    private EstadoViaje estado = EstadoViaje.SOLICITADO;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_tarifa", nullable = false, length = 20)
    private TipoTarifa tipoTarifa;

    // Usamos BigDecimal para montos de dinero (es mucho más preciso que Double o Float)
    @Column(name = "costo_estimado", precision = 10, scale = 2)
    private BigDecimal costoEstimado;

    @Column(name = "fecha_creacion", updatable = false)
    private OffsetDateTime fechaCreacion;

    @PrePersist
    protected void onCreate() {
        this.fechaCreacion = OffsetDateTime.now();
    }
}