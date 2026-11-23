package com.unsa.taxis.exception;

public class DniPropietarioDuplicadoException extends RuntimeException {

    public DniPropietarioDuplicadoException(String dni) {
        super("Ya existe un propietario registrado con ese DNI.");
    }
}