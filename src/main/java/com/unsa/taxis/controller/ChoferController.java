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
import com.unsa.taxis.dto.OfertaChoferResponse;
import com.unsa.taxis.service.OfertaViajeService;

import java.util.List;

import com.unsa.taxis.service.UsuarioAutenticadoService;

import org.springframework.security.core.Authentication;
import com.unsa.taxis.exception.AccesoNoAutorizadoException;

@RestController
@RequestMapping("/api/choferes")
public class ChoferController {

    private final ChoferService choferService;
    private final OfertaViajeService ofertaViajeService;
    private final UsuarioAutenticadoService usuarioAutenticadoService;

    public ChoferController(
            ChoferService choferService,
            OfertaViajeService ofertaViajeService,
            UsuarioAutenticadoService usuarioAutenticadoService) {

        this.choferService = choferService;
        this.ofertaViajeService = ofertaViajeService;
        this.usuarioAutenticadoService = usuarioAutenticadoService;
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
            @Valid @RequestBody ActualizarUbicacionRequest request,
            Authentication authentication) {

        Chofer choferAutenticado = usuarioAutenticadoService.obtenerChofer(authentication);

        if (!choferAutenticado.getId().equals(id)) {
                throw new AccesoNoAutorizadoException(
                        "No tenés autorización para modificar la ubicación de otro chofer"
                );
        }

        Chofer chofer = choferService.actualizarUbicacion(
                choferAutenticado.getId(),
                request.getLatitud(),
                request.getLongitud()
        );

        return ResponseEntity.ok(chofer);
    }

    @PutMapping("/{id}/estado")
    public ResponseEntity<Chofer> cambiarEstado(
            @PathVariable Long id,
            @Valid @RequestBody CambiarEstadoChoferRequest request,
            Authentication authentication) {

        Chofer choferAutenticado = usuarioAutenticadoService.obtenerChofer(authentication);

        if (!choferAutenticado.getId().equals(id)) {
                throw new AccesoNoAutorizadoException(
                        "No tenés autorización para modificar el estado de otro chofer"
                );
        }

        Chofer chofer = choferService.cambiarEstado(
                choferAutenticado.getId(),
                request.getEstado()
        );

        return ResponseEntity.ok(chofer);
    }

    @GetMapping("/{id}/ofertas")
    public ResponseEntity<List<OfertaChoferResponse>> obtenerOfertasPendientes(
            @PathVariable Long id,
            Authentication authentication) {

        Chofer choferAutenticado = usuarioAutenticadoService.obtenerChofer(authentication);

        if (!choferAutenticado.getId().equals(id)) {
                throw new AccesoNoAutorizadoException(
                        "No tenés autorización para consultar las ofertas de otro chofer"
                );
        }

        return ResponseEntity.ok(
                ofertaViajeService.listarOfertasPendientesResponsePorChofer(choferAutenticado.getId())
        );
    }


}