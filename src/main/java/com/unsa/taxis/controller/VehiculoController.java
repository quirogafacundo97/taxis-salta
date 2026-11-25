package com.unsa.taxis.controller;

import com.unsa.taxis.dto.ActualizarVehiculoRequest;
import com.unsa.taxis.dto.CrearVehiculoRequest;
import com.unsa.taxis.dto.VehiculoDTO;
import com.unsa.taxis.service.VehiculoService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/vehiculos")
@PreAuthorize("hasAuthority('ROLE_ADMIN')")
public class VehiculoController {

    private final VehiculoService vehiculoService;

    public VehiculoController(VehiculoService vehiculoService) {
        this.vehiculoService = vehiculoService;
    }

    @GetMapping
    public ResponseEntity<List<VehiculoDTO>> listarTodos() {
        return ResponseEntity.ok(vehiculoService.listarTodos());
    }

    @GetMapping("/{id}")
    public ResponseEntity<VehiculoDTO> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(vehiculoService.buscarPorId(id));
    }

    @PostMapping
    public ResponseEntity<VehiculoDTO> crear(
            @Valid @RequestBody CrearVehiculoRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(vehiculoService.crear(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<VehiculoDTO> actualizar(
            @PathVariable Long id,
            @Valid @RequestBody ActualizarVehiculoRequest request
    ) {
        return ResponseEntity.ok(vehiculoService.actualizar(id, request));
    }
}