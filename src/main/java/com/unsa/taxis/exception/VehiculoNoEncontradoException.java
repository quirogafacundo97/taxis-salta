package com.unsa.taxis.exception;

public class VehiculoNoEncontradoException extends RuntimeException {

    public VehiculoNoEncontradoException(Long id) {
        super("No se encontró el vehículo con id: " + id);
    }
}
