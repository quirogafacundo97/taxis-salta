
package com.unsa.taxis.controller;

import com.unsa.taxis.dto.LoginRequest;
import com.unsa.taxis.dto.LoginResponse;
import com.unsa.taxis.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.unsa.taxis.dto.CambiarPasswordRequest;
import org.springframework.security.core.Authentication;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> iniciarSesion(
            @Valid @RequestBody LoginRequest request) {

        LoginResponse response = authService.iniciarSesion(request);

        return ResponseEntity.ok(response);
    }

    @PutMapping("/cambiar-password")
    public ResponseEntity<Void> cambiarPassword(
            @Valid @RequestBody CambiarPasswordRequest request,
            Authentication authentication) {

        authService.cambiarPassword(request, authentication);

        return ResponseEntity.noContent().build();
    }
}
