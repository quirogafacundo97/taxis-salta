package com.unsa.taxis.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.OffsetDateTime;

@Entity
@Table(name = "vehiculos")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Vehiculo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 10)
    private String patente;

    @Column(nullable = false, unique = true, length = 20)
    private String licencia;

    @Column(nullable = false, length = 50)
    private String marca;

    @Column(nullable = false, length = 100)
    private String modelo;

    @Column(nullable = false)
    private Integer anio;

    @Column(name = "nro_reloj_fullmar", nullable = false, unique = true, length = 50)
    private String nroRelojFullmar;

    // RELACIÓN: Muchos vehículos pueden pertenecer a un mismo propietario
    @ManyToOne
    @JoinColumn(name = "propietario_id")
    private Propietario propietario;

    @Column(name = "fecha_creacion", updatable = false)
    private OffsetDateTime fechaCreacion;

    @PrePersist
    protected void onCreate() {
        this.fechaCreacion = OffsetDateTime.now();
    }
}