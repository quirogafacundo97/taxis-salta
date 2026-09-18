package com.unsa.taxis.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;

@Entity
@Table(name = "asignaciones_chofer_vehiculo")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AsignacionChoferVehiculo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "chofer_id", nullable = false)
    private Chofer chofer;

    @ManyToOne
    @JoinColumn(name = "vehiculo_id", nullable = false)
    private Vehiculo vehiculo;

    @Column(name = "fecha_inicio", nullable = false)
    private OffsetDateTime fechaInicio;

    @Column(name = "fecha_fin")
    private OffsetDateTime fechaFin;
}