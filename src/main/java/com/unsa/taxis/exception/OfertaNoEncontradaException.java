package com.unsa.taxis.exception;

public class OfertaNoEncontradaException extends RuntimeException {

    public OfertaNoEncontradaException(Long id) {
        super("No se encontró la oferta con id: " + id);
    }
}