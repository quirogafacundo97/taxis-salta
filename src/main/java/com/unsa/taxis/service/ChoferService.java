package com.unsa.taxis.service;

import com.unsa.taxis.dto.ChoferCercanoResponse;
import com.unsa.taxis.exception.ChoferNoEncontradoException;
import com.unsa.taxis.model.Chofer;
import com.unsa.taxis.model.EstadoChofer;
import com.unsa.taxis.repository.ChoferRepository;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;

@Service
public class ChoferService {

    private final ChoferRepository choferRepository;
    private final CalculadorDistanciaService calculadorDistanciaService;

    public ChoferService(ChoferRepository choferRepository, CalculadorDistanciaService calculadorDistanciaService) {
        this.choferRepository = choferRepository;
        this.calculadorDistanciaService = calculadorDistanciaService;
    }

    // 1. Listar absolutamente todos los choferes
    public List<Chofer> listarTodos() {
        return choferRepository.findAll();
    }

    public Chofer buscarPorId(Long id) {
        return choferRepository.findById(id)
                .orElseThrow(() -> new ChoferNoEncontradoException(id));
    }

    public Chofer actualizarUbicacion(
            Long id,
            Double latitud,
            Double longitud) {

        Chofer chofer = choferRepository.findById(id)
                .orElseThrow(() -> new ChoferNoEncontradoException(id));

        chofer.setLatitud(latitud);
        chofer.setLongitud(longitud);

        return choferRepository.save(chofer);
    }

    public Chofer cambiarEstado(Long id, EstadoChofer nuevoEstado) {

        Chofer chofer = choferRepository.findById(id)
                .orElseThrow(() -> new ChoferNoEncontradoException(id));

        chofer.setEstado(nuevoEstado);

        return choferRepository.save(chofer);
    }

    public List<Chofer> listarDisponibles() {

        return choferRepository
                .findByEstadoAndHabilitadoAmt(
                        EstadoChofer.LIBRE,
                        true
                )
                .stream()
                .filter(chofer ->
                        chofer.getLatitud() != null
                                && chofer.getLongitud() != null
                )
                .toList();
    }

    public List<ChoferCercanoResponse> buscarChoferesCercanos(
            Double latitud,
            Double longitud,
            Double radioKm) {

        double radioMetros = radioKm * 1000;

        return listarDisponibles().stream()
                .map(chofer -> {

                    double distancia = calculadorDistanciaService
                            .calcularDistanciaMetros(
                                    latitud,
                                    longitud,
                                    chofer.getLatitud(),
                                    chofer.getLongitud()
                            );

                    return ChoferCercanoResponse.builder()
                            .chofer(chofer)
                            .distanciaMetros(distancia)
                            .build();
                })
                .filter(resultado ->
                        resultado.getDistanciaMetros() <= radioMetros
                )
                .sorted(Comparator.comparing(
                        ChoferCercanoResponse::getDistanciaMetros
                ))
                .toList();
    }

}