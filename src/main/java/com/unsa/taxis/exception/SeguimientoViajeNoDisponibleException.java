package com.unsa.taxis.exception;

public class SeguimientoViajeNoDisponibleException extends RuntimeException {

    public SeguimientoViajeNoDisponibleException() {
        super("El seguimiento de este viaje no está disponible");
    }
}