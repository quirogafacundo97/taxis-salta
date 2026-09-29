package com.unsa.taxis.exception;

public class ViajeNoEncontradoException extends RuntimeException {

    public ViajeNoEncontradoException(Long id) {
        super("No se encontró el viaje con id: " + id);
    }
}
