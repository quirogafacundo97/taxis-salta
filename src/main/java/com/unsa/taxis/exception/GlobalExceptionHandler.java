package com.unsa.taxis.exception;

import com.unsa.taxis.dto.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ViajeNoEncontradoException.class)
    public ResponseEntity<ErrorResponse> manejarViajeNoEncontrado(
            ViajeNoEncontradoException ex) {

        ErrorResponse error = new ErrorResponse(
                HttpStatus.NOT_FOUND.value(),
                ex.getMessage()
        );

        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(error);
    }

    @ExceptionHandler(OfertaNoEncontradaException.class)
    public ResponseEntity<ErrorResponse> manejarOfertaNoEncontrada(
            OfertaNoEncontradaException ex) {

        ErrorResponse error = new ErrorResponse(
                HttpStatus.NOT_FOUND.value(),
                ex.getMessage()
        );

        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(error);
    }
}