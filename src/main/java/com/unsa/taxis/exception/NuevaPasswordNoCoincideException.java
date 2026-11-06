package com.unsa.taxis.exception;

public class NuevaPasswordNoCoincideException extends RuntimeException {

    public NuevaPasswordNoCoincideException() {
        super("La nueva contraseña y su confirmación no coinciden");
    }
}