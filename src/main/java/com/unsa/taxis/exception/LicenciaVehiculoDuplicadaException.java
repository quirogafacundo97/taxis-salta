package com.unsa.taxis.exception;

public class LicenciaVehiculoDuplicadaException extends RuntimeException {

    public LicenciaVehiculoDuplicadaException(String licencia) {
        super("Ya existe un vehículo registrado con la licencia: " + licencia);
    }
}
