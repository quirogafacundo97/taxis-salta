package com.unsa.taxis.service;

import com.unsa.taxis.model.Tarifa;
import com.unsa.taxis.model.TipoTarifa;
import com.unsa.taxis.repository.FeriadoRepository;
import com.unsa.taxis.repository.TarifaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.OffsetDateTime;
import java.time.LocalDate;
import java.time.LocalTime;

@Service
@RequiredArgsConstructor
public class TarifaService {

    private final TarifaRepository tarifaRepository;
    private final FeriadoRepository feriadoRepository;

    public Tarifa obtenerTarifaParaFechaHora(OffsetDateTime fechaHora) {

        LocalDate fecha = fechaHora.toLocalDate();
        LocalTime hora = fechaHora.toLocalTime();

        TipoTarifa tipo = determinarTipoTarifa(fecha, hora);

        return tarifaRepository.buscarTarifaVigente(tipo, fecha)
                .orElseThrow(() ->
                        new RuntimeException(
                                "No existe una tarifa vigente para el tipo "
                                        + tipo + " en la fecha " + fecha
                        )
                );
    }

    private TipoTarifa determinarTipoTarifa(
            LocalDate fecha,
            LocalTime hora
    ) {

        // Los feriados tienen tarifa nocturna durante todo el día
        if (feriadoRepository.existsByFecha(fecha)) {
            return TipoTarifa.NOCTURNA;
        }

        // Los domingos tienen tarifa nocturna durante todo el día
        if (fecha.getDayOfWeek() == DayOfWeek.SUNDAY) {
            return TipoTarifa.NOCTURNA;
        }

        // Lunes a sábado:
        // Diurna desde las 06:00 hasta antes de las 22:00
        if (!hora.isBefore(LocalTime.of(6, 0))
                && hora.isBefore(LocalTime.of(22, 0))) {

            return TipoTarifa.DIURNA;
        }

        return TipoTarifa.NOCTURNA;
    }
}