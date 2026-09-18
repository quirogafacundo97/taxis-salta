package com.unsa.taxis.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "tarifas")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Tarifa {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TipoTarifa tipo;

    @Column(name = "bajada_bandera", nullable = false, precision = 10, scale = 2)
    private BigDecimal bajadaBandera;

    @Column(name = "valor_ficha", nullable = false, precision = 10, scale = 2)
    private BigDecimal valorFicha;

    @Column(name = "fecha_desde", nullable = false)
    private LocalDate fechaDesde;

    @Column(name = "fecha_hasta")
    private LocalDate fechaHasta;
}