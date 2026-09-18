package com.unsa.taxis.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "feriados")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Feriado {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private LocalDate fecha;

    @Column(length = 100)
    private String descripcion;
}