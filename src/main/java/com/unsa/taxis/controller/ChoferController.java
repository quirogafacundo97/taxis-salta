package com.unsa.taxis.controller;

import com.unsa.taxis.dto.CambiarEstadoChoferRequest;
import com.unsa.taxis.dto.ChoferCercanoResponse;
import com.unsa.taxis.model.Chofer;
import com.unsa.taxis.service.ChoferService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.unsa.taxis.dto.ActualizarUbicacionRequest;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@RestController
@RequestMapping("/api/choferes")
public class ChoferController {

    private final ChoferService choferService;

    public ChoferController(ChoferService choferService) {
        this.choferService = choferService;
    }

    @GetMapping
    public ResponseEntity<List<Chofer>> obtenerTodos() {
        List<Chofer> choferes = choferService.listarTodos();
        return ResponseEntity.ok(choferes);
    }

    @GetMapping("/disponibles")
    public ResponseEntity<List<Chofer>> obtenerDisponibles() {

        return ResponseEntity.ok(
                choferService.listarDisponibles()
        );
    }

    @GetMapping("/cercanos")
    public ResponseEntity<List<ChoferCercanoResponse>> buscarChoferesCercanos(
            @RequestParam Double latitud,
            @RequestParam Double longitud,
            @RequestParam(defaultValue = "5") Double radioKm) {

        return ResponseEntity.ok(
                choferService.buscarChoferesCercanos(
                        latitud,
                        longitud,
                        radioKm
                )
        );
    }


    @GetMapping("/{id}")
    public ResponseEntity<Chofer> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(
                choferService.buscarPorId(id)
        );
    }

    @PutMapping("/{id}/ubicacion")
    public ResponseEntity<Chofer> actualizarUbicacion(
            @PathVariable Long id,
            @Valid @RequestBody ActualizarUbicacionRequest request) {

        Chofer chofer = choferService.actualizarUbicacion(
                id,
                request.getLatitud(),
                request.getLongitud()
        );

        return ResponseEntity.ok(chofer);
    }

    @PutMapping("/{id}/estado")
    public ResponseEntity<Chofer> cambiarEstado(
            @PathVariable Long id,
            @Valid @RequestBody CambiarEstadoChoferRequest request) {

        Chofer chofer = choferService.cambiarEstado(
                id,
                request.getEstado()
        );

        return ResponseEntity.ok(chofer);
    }




}