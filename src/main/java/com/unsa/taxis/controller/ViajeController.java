package com.unsa.taxis.controller;

import com.unsa.taxis.dto.CrearViajeRequest;
import com.unsa.taxis.dto.ViajeResponse;
import com.unsa.taxis.model.Viaje;
import com.unsa.taxis.service.ViajeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/viajes")
@RequiredArgsConstructor
public class ViajeController {

    private final ViajeService viajeService;

    @PostMapping
    public ResponseEntity<ViajeResponse> crearViaje(
            @Valid @RequestBody CrearViajeRequest request) {

        ViajeResponse viaje = viajeService.crearViaje(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(viaje);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ViajeResponse> buscarPorId(@PathVariable Long id) {

        return ResponseEntity.ok(
                viajeService.buscarPorId(id)
        );
    }

    @GetMapping
    public ResponseEntity<List<ViajeResponse>> listarTodos() {

        return ResponseEntity.ok(
                viajeService.listarTodos()
        );
    }
}