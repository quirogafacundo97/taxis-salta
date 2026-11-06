package com.unsa.taxis.exception;

public class CredencialesInvalidasException extends RuntimeException {

    public CredencialesInvalidasException() {
        super("Credenciales inválidas o cuenta inactiva");
    }
}
