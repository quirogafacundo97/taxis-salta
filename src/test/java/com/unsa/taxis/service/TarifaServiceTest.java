
package com.unsa.taxis.service;

import com.unsa.taxis.model.Tarifa;
import com.unsa.taxis.model.TipoTarifa;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
class TarifaServiceTest {

    @Autowired
    private TarifaService tarifaService;

    @Test
    void debeUsarTarifaDiurnaExactamenteALas6() {

        OffsetDateTime fechaHora = OffsetDateTime.of(
                2026, 8, 25,
                6, 0,
                0, 0,
                ZoneOffset.of("-03:00")
        );

        Tarifa tarifa = tarifaService.obtenerTarifaParaFechaHora(fechaHora);

        assertEquals(TipoTarifa.DIURNA, tarifa.getTipo());
    }

    @Test
    void debeUsarTarifaNocturnaExactamenteALas22() {

        OffsetDateTime fechaHora = OffsetDateTime.of(
                2026, 8, 25,
                22, 0,
                0, 0,
                ZoneOffset.of("-03:00")
        );

        Tarifa tarifa = tarifaService.obtenerTarifaParaFechaHora(fechaHora);

        assertEquals(TipoTarifa.NOCTURNA, tarifa.getTipo());
    }

    @Test
    void debeUsarTarifaDiurnaUnDiaLaborable() {

        OffsetDateTime fechaHora = OffsetDateTime.of(
                2026, 8, 25,
                15, 0,
                0, 0,
                ZoneOffset.of("-03:00")
        );

        Tarifa tarifa = tarifaService.obtenerTarifaParaFechaHora(fechaHora);

        assertEquals(TipoTarifa.DIURNA, tarifa.getTipo());
    }

    @Test
    void debeUsarTarifaNocturnaDespuesDeLas22() {

        OffsetDateTime fechaHora = OffsetDateTime.of(
                2026, 8, 25,
                22, 30,
                0, 0,
                ZoneOffset.of("-03:00")
        );

        Tarifa tarifa = tarifaService.obtenerTarifaParaFechaHora(fechaHora);

        assertEquals(TipoTarifa.NOCTURNA, tarifa.getTipo());
    }

    @Test
    void debeUsarTarifaNocturnaAntesDeLas6() {

        OffsetDateTime fechaHora = OffsetDateTime.of(
                2026, 8, 25,
                5, 30,
                0, 0,
                ZoneOffset.of("-03:00")
        );

        Tarifa tarifa = tarifaService.obtenerTarifaParaFechaHora(fechaHora);

        assertEquals(TipoTarifa.NOCTURNA, tarifa.getTipo());
    }

    @Test
    void debeUsarTarifaNocturnaLosDomingos() {

        OffsetDateTime fechaHora = OffsetDateTime.of(
                2026, 8, 30,
                14, 0,
                0, 0,
                ZoneOffset.of("-03:00")
        );

        Tarifa tarifa = tarifaService.obtenerTarifaParaFechaHora(fechaHora);

        assertEquals(TipoTarifa.NOCTURNA, tarifa.getTipo());
    }

    @Test
    void debeUsarTarifaNocturnaEnFeriados() {

        // IMPORTANTE:
        // Esta fecha debe existir en la tabla feriados.
        OffsetDateTime fechaHora = OffsetDateTime.of(
                2026, 8, 17,
                14, 0,
                0, 0,
                ZoneOffset.of("-03:00")
        );

        Tarifa tarifa = tarifaService.obtenerTarifaParaFechaHora(fechaHora);

        assertEquals(TipoTarifa.NOCTURNA, tarifa.getTipo());
    }
}

