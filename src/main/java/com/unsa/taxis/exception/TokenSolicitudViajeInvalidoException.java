package com.unsa.taxis.exception;

public class TokenSolicitudViajeInvalidoException extends RuntimeException {

    public TokenSolicitudViajeInvalidoException() {
        super("El enlace de solicitud de viaje es inválido, venció o ya fue utilizado");
    }
}
