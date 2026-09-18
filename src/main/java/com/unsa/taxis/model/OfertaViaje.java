package com.unsa.taxis.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;

@Entity
@Table(name = "ofertas_viaje")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OfertaViaje {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "viaje_id", nullable = false)
    private Viaje viaje;

    @ManyToOne(optional = false)
    @JoinColumn(name = "chofer_id", nullable = false)
    private Chofer chofer;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private EstadoOferta estado = EstadoOferta.PENDIENTE;

    @Column(name = "fecha_envio", nullable = false)
    private OffsetDateTime fechaEnvio;

    @Column(name = "fecha_expiracion", nullable = false)
    private OffsetDateTime fechaExpiracion;

    @PrePersist
    protected void onCreate() {
        this.fechaEnvio = OffsetDateTime.now();
    }
}
