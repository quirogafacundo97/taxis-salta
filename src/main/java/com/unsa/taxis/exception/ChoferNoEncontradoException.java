package com.unsa.taxis.exception;

public class ChoferNoEncontradoException extends RuntimeException {

    public ChoferNoEncontradoException(Long id) {
        super("No se encontró el chofer con id: " + id);
    }
}