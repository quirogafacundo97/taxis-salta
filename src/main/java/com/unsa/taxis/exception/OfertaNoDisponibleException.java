package com.unsa.taxis.exception;

public class OfertaNoDisponibleException extends RuntimeException {

    public OfertaNoDisponibleException(String mensaje) {
        super(mensaje);
    }
}