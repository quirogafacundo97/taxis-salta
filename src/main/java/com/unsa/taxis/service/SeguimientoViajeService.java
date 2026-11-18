
package com.unsa.taxis.service;

import com.unsa.taxis.dto.SeguimientoViajeResponse;
import com.unsa.taxis.exception.SeguimientoViajeNoDisponibleException;
import com.unsa.taxis.model.AsignacionChoferVehiculo;
import com.unsa.taxis.model.EstadoViaje;
import com.unsa.taxis.model.Vehiculo;
import com.unsa.taxis.model.Viaje;
import com.unsa.taxis.repository.AsignacionChoferVehiculoRepository;
import com.unsa.taxis.repository.ViajeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;

@Service
@RequiredArgsConstructor
public class SeguimientoViajeService {

    private final ViajeRepository viajeRepository;
    private final TokenSeguimientoService tokenSeguimientoService;
    private final AsignacionChoferVehiculoRepository asignacionRepository;

    @Transactional(readOnly = true)
    public SeguimientoViajeResponse consultarSeguimiento(String token) {

        if (token == null || token.isBlank()) {
            throw new SeguimientoViajeNoDisponibleException();
        }

        String hash = tokenSeguimientoService.calcularHash(token);

        Viaje viaje = viajeRepository.findByTokenSeguimientoHash(hash)
                .orElseThrow(SeguimientoViajeNoDisponibleException::new);

        if (viaje.getTokenSeguimientoExpiraEn() == null
                || !OffsetDateTime.now().isBefore(viaje.getTokenSeguimientoExpiraEn())) {
            throw new SeguimientoViajeNoDisponibleException();
        }

        EstadoViaje estado = viaje.getEstado();

        if (estado != EstadoViaje.SOLICITADO
                && estado != EstadoViaje.ACEPTADO
                && estado != EstadoViaje.EN_CURSO) {
            throw new SeguimientoViajeNoDisponibleException();
        }

        if (estado == EstadoViaje.SOLICITADO) {
            return SeguimientoViajeResponse.builder()
                    .estado(estado)
                    .build();
        }

        if (viaje.getChofer() == null) {
            throw new SeguimientoViajeNoDisponibleException();
        }

        AsignacionChoferVehiculo asignacion = asignacionRepository
                .findByChoferAndFechaFinIsNull(viaje.getChofer())
                .orElseThrow(SeguimientoViajeNoDisponibleException::new);

        Vehiculo vehiculo = asignacion.getVehiculo();

        if (vehiculo == null) {
            throw new SeguimientoViajeNoDisponibleException();
        }

        return SeguimientoViajeResponse.builder()
                .estado(estado)
                .choferNombre(viaje.getChofer().getNombre())
                .choferApellido(viaje.getChofer().getApellido())
                .vehiculoMarca(vehiculo.getMarca())
                .vehiculoModelo(vehiculo.getModelo())
                .vehiculoPatente(vehiculo.getPatente())
                .vehiculoLicencia(vehiculo.getLicencia())
                .build();
    }
}
