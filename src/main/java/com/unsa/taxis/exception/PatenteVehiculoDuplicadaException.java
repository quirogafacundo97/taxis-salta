package com.unsa.taxis.exception;

public class PatenteVehiculoDuplicadaException extends RuntimeException {

    public PatenteVehiculoDuplicadaException(String patente) {
        super("Ya existe un vehículo registrado con la patente: " + patente);
    }
}
