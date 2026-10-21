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
import com.unsa.taxis.repository.ChoferRepository;
import com.unsa.taxis.repository.ViajeRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import jakarta.persistence.PersistenceContext;
import com.unsa.taxis.exception.OfertaVencidaException;
import org.springframework.transaction.annotation.Propagation;

import java.time.OffsetDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OfertaViajeService {

    private static final long TIEMPO_EXPIRACION_SEGUNDOS = 10;

    private final OfertaViajeRepository ofertaViajeRepository;
    private final ChoferService choferService;
    private final ApplicationEventPublisher eventPublisher;
    private final ChoferRepository choferRepository;
    private final ViajeRepository viajeRepository;
    
    @PersistenceContext
    private EntityManager entityManager;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
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
            
            eventPublisher.publishEvent(
                new OfertaVencidaEvent(oferta.getId())
            );

            throw new OfertaVencidaException("La oferta ha vencido");
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


    @Transactional(noRollbackFor = OfertaVencidaException.class)
    public void aceptarOferta(Long ofertaId) {

    // Lectura inicial: necesitamos conocer el viaje y el chofer.
    // Todavía no bloqueamos la oferta para evitar interbloqueos.
    OfertaViaje ofertaInicial = ofertaViajeRepository.findById(ofertaId)
            .orElseThrow(() ->
                    new OfertaNoEncontradaException(ofertaId)
            );

    Long viajeId = ofertaInicial.getViaje().getId();
    Long choferId = ofertaInicial.getChofer().getId();

    // 1. Primero bloqueamos el viaje.
    Viaje viaje = viajeRepository.buscarPorIdConBloqueo(viajeId)
            .orElseThrow(() ->
                    new IllegalStateException("El viaje asociado a la oferta no existe")
           );

    // 2. Después bloqueamos el chofer.
    Chofer chofer = choferRepository.buscarPorIdConBloqueo(choferId)
            .orElseThrow(() ->
                   new IllegalStateException("El chofer asociado a la oferta no existe")
            );

    // 3. Finalmente bloqueamos y refrescamos la oferta.
    // Así comprobamos su estado actual, no el de la lectura inicial.
    OfertaViaje oferta = ofertaViajeRepository
            .buscarPorIdConBloqueo(ofertaId)
            .orElseThrow(() ->
                   new OfertaNoEncontradaException(ofertaId)
            );

    entityManager.refresh(oferta, LockModeType.PESSIMISTIC_WRITE);

    validarOfertaDisponible(oferta);

    // Verificamos que el viaje siga sin asignar.
    if (viaje.getChofer() != null) {
            throw new OfertaNoDisponibleException(
                    "El viaje ya tiene un chofer asignado"
            );
    }

    // El chofer debe seguir libre y habilitado.
    if (chofer.getEstado() != EstadoChofer.LIBRE) {
            throw new OfertaNoDisponibleException(
                    "El chofer ya no está disponible"
            );
    }

    if (!Boolean.TRUE.equals(chofer.getHabilitadoAmt())) {
            throw new OfertaNoDisponibleException(
                    "El chofer no está habilitado por la AMT"
            );
    }

    // La oferta debe corresponder al viaje y al chofer bloqueados.
    if (!oferta.getViaje().getId().equals(viajeId)
            || !oferta.getChofer().getId().equals(choferId)) {
            throw new OfertaNoDisponibleException(
                    "La oferta ya no corresponde a los datos esperados"
            );
    }

    // Asignamos el viaje y actualizamos ambos estados.
    oferta.setEstado(EstadoOferta.ACEPTADA);

    viaje.setChofer(chofer);
    viaje.setEstado(EstadoViaje.ACEPTADO);

    chofer.setEstado(EstadoChofer.OCUPADO);
    choferRepository.save(chofer);

    // Cancelamos las ofertas pendientes del mismo viaje.
    cancelarOtrasOfertasPendientes(viaje, oferta.getId());

    ofertaViajeRepository.save(oferta);
    }

    @Transactional(noRollbackFor = OfertaVencidaException.class)
    public void rechazarOferta(Long ofertaId) {

        OfertaViaje oferta = ofertaViajeRepository
                .buscarPorIdConBloqueo(ofertaId)
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

    @Transactional
    public void vencerOferta(Long ofertaId) {

        OfertaViaje oferta = ofertaViajeRepository
                .buscarPorIdConBloqueo(ofertaId)
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