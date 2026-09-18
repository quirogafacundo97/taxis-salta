package com.unsa.taxis.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.OffsetDateTime;

@Entity
@Table(name = "choferes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Chofer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50)
    private String nombre;

    @Column(nullable = false, length = 50)
    private String apellido;

    @Column(nullable = false, unique = true, length = 20)
    private String dni;

    @Column(name = "telefono_contacto", nullable = false, unique = true, length = 20)
    private String telefonoContacto;

    @Builder.Default
    @Column(name = "habilitado_amt", nullable = false)
    private Boolean habilitadoAmt = true;

    // Con esta anotación, Hibernate guarda el texto 'LIBRE' en Postgres en lugar de un número (0, 1, 2)
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private EstadoChofer estado = EstadoChofer.DESCONECTADO;

    @Column
    private Double latitud;

    @Column
    private Double longitud;

    @Column(name = "fecha_creacion", updatable = false)
    private OffsetDateTime fechaCreacion;

    @PrePersist
    protected void onCreate() {
        this.fechaCreacion = OffsetDateTime.now();
    }
}