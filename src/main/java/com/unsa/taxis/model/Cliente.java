package com.unsa.taxis.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.OffsetDateTime;

@Entity
@Table(name = "clientes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Cliente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // El ID internacional de WhatsApp (ej: "5493875551234") es único y obligatorio
    @Column(name = "whatsapp_id", nullable = false, unique = true, length = 30)
    private String whatsappId;

    // El nombre puede ser nulo al principio (mientras el bot le pregunta cómo se llama)
    @Column(length = 100)
    private String nombre;

    @Column(name = "fecha_registro", updatable = false)
    private OffsetDateTime fechaRegistro;

    @PrePersist
    protected void onCreate() {
        this.fechaRegistro = OffsetDateTime.now();
    }
}