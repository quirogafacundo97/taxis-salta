package com.unsa.taxis.exception;

public class PropietarioNoEncontradoException extends RuntimeException {

    public PropietarioNoEncontradoException(Long id) {
        super("No se encontró el propietario con id: " + id);
    }
}