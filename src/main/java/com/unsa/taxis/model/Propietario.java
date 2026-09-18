package com.unsa.taxis.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.OffsetDateTime;

@Entity
@Table(name = "propietarios")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Propietario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50)
    private String nombre;

    @Column(nullable = false, length = 50)
    private String apellido;

    @Column(nullable = false, unique = true, length = 20)
    private String dni;

    @Column(length = 20)
    private String telefono;

    @Column(name = "fecha_creacion", updatable = false)
    private OffsetDateTime fechaCreacion;

    @PrePersist
    protected void onCreate() {
        this.fechaCreacion = OffsetDateTime.now();
    }
}

