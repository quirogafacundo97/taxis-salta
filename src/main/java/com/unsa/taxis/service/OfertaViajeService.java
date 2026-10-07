package com.unsa.taxis.service;

import com.unsa.taxis.exception.OfertaNoDisponibleException;
import com.unsa.taxis.exception.OfertaNoEncontradaException;
import com.unsa.taxis.model.*;
import com.unsa.taxis.repository.OfertaViajeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.unsa.taxis.event.OfertaRechazadaEvent;
import org.springframework.context.ApplicationEventPublisher;
import com.unsa.taxis.event.OfertaVencidaEvent;

import java.time.OffsetDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OfertaViajeService {

    private static final long TIEMPO_EXPIRACION_SEGUNDOS = 10;

    private final OfertaViajeRepository ofertaViajeRepository;
    private final ChoferService choferService;
    private final ApplicationEventPublisher eventPublisher;

    public OfertaViaje crearOferta(Viaje viaje, Chofer chofer) {

        OfertaViaje oferta = OfertaViaje.builder()
                .viaje(viaje)
                .chofer(chofer)
                .estado(EstadoOferta.PENDIENTE)
                .fechaExpiracion(
                        OffsetDateTime.now()
                                .plusSeconds(TIEMPO_EXPIRACION_SEGUNDOS)
                )
                .build();

        return ofertaViajeRepository.save(oferta);
    }

    private void validarOfertaDisponible(OfertaViaje oferta) {

        if (oferta.getEstado() != EstadoOferta.PENDIENTE) {
            throw new OfertaNoDisponibleException(
                    "La oferta no está disponible"
            );
        }

        if (!OffsetDateTime.now().isBefore(oferta.getFechaExpiracion())) {

            oferta.setEstado(EstadoOferta.VENCIDA);
            ofertaViajeRepository.save(oferta);

            throw new OfertaNoDisponibleException(
                    "La oferta ha vencido"
            );
        }
    }

    private void cancelarOtrasOfertasPendientes(
            Viaje viaje,
            Long ofertaAceptadaId) {

        List<OfertaViaje> ofertasPendientes =
                ofertaViajeRepository.findByViajeIdAndEstado(
                        viaje.getId(),
                        EstadoOferta.PENDIENTE
                );

        for (OfertaViaje otraOferta : ofertasPendientes) {

            if (!otraOferta.getId().equals(ofertaAceptadaId)) {
                otraOferta.setEstado(EstadoOferta.CANCELADA);
                ofertaViajeRepository.save(otraOferta);
            }
        }
    }

    @Transactional
    public void aceptarOferta(Long ofertaId) {

        OfertaViaje oferta = ofertaViajeRepository.findById(ofertaId)
                .orElseThrow(() ->
                        new OfertaNoEncontradaException(ofertaId)
                );

        validarOfertaDisponible(oferta);

        Viaje viaje = oferta.getViaje();

        if (viaje.getChofer() != null) {
            throw new OfertaNoDisponibleException(
                    "El viaje ya tiene un chofer asignado"
            );
        }

        oferta.setEstado(EstadoOferta.ACEPTADA);

        viaje.setChofer(oferta.getChofer());
        viaje.setEstado(EstadoViaje.ACEPTADO);

        choferService.cambiarEstado(
                oferta.getChofer().getId(),
                EstadoChofer.OCUPADO
        );

        cancelarOtrasOfertasPendientes(
                viaje,
                oferta.getId()
        );

        ofertaViajeRepository.save(oferta);
    }

    public void rechazarOferta(Long ofertaId) {

        OfertaViaje oferta = ofertaViajeRepository.findById(ofertaId)
                .orElseThrow(() ->
                        new OfertaNoEncontradaException(ofertaId)
                );

        validarOfertaDisponible(oferta);

        oferta.setEstado(EstadoOferta.RECHAZADA);

        ofertaViajeRepository.save(oferta);

        eventPublisher.publishEvent(
                new OfertaRechazadaEvent(oferta.getId())
        );
    }

    public void vencerOferta(Long ofertaId) {

        OfertaViaje oferta = ofertaViajeRepository.findById(ofertaId)
                .orElseThrow(() ->
                        new OfertaNoEncontradaException(ofertaId)
                );

        if (oferta.getEstado() != EstadoOferta.PENDIENTE) {
            throw new OfertaNoDisponibleException(
                    "La oferta no está disponible para vencer"
            );
        }

        if (OffsetDateTime.now().isBefore(oferta.getFechaExpiracion())) {
            throw new OfertaNoDisponibleException(
                    "La oferta todavía no ha vencido"
            );
        }

        oferta.setEstado(EstadoOferta.VENCIDA);

        ofertaViajeRepository.save(oferta);

        eventPublisher.publishEvent(
                new OfertaVencidaEvent(oferta.getId())
        );
    }

    public OfertaViaje buscarPorId(Long ofertaId) {
        return ofertaViajeRepository.findById(ofertaId)
                .orElseThrow(() ->
                        new OfertaNoEncontradaException(ofertaId)
                );
    }

    public List<OfertaViaje> listarOfertasPorViaje(Long viajeId) {
        return ofertaViajeRepository.findByViajeId(viajeId);
    }

    public List<OfertaViaje> listarOfertasExpiradas() {

        return ofertaViajeRepository
                .findByEstadoAndFechaExpiracionLessThanEqual(
                        EstadoOferta.PENDIENTE,
                        OffsetDateTime.now()
                );
    }
}