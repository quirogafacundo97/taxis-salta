package com.unsa.taxis.controller;

import com.unsa.taxis.dto.PropietarioDTO;
import com.unsa.taxis.dto.CrearPropietarioRequest;
import com.unsa.taxis.dto.ActualizarPropietarioRequest;
import com.unsa.taxis.model.Propietario;
import com.unsa.taxis.service.PropietarioService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/propietarios")
@PreAuthorize("hasAuthority('ROLE_ADMIN')")
public class PropietarioController {

    private final PropietarioService propietarioService;

    public PropietarioController(PropietarioService propietarioService) {
        this.propietarioService = propietarioService;
    }

    @GetMapping
    public ResponseEntity<List<PropietarioDTO>> listarTodos() {
        return ResponseEntity.ok(propietarioService.listarTodos());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Propietario> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(propietarioService.buscarPorId(id));
    }

    @PostMapping
    public ResponseEntity<Propietario> crear(
            @Valid @RequestBody CrearPropietarioRequest request) {

        Propietario propietario = propietarioService.crear(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(propietario);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Propietario> actualizar(
            @PathVariable Long id,
            @Valid @RequestBody ActualizarPropietarioRequest request) {

        return ResponseEntity.ok(
                propietarioService.actualizar(id, request)
        );
    }
}