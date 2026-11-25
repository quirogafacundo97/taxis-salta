package com.unsa.taxis.exception;

public class NroRelojFullmarDuplicadoException extends RuntimeException {

    public NroRelojFullmarDuplicadoException(String nroRelojFullmar) {
        super("Ya existe un vehículo registrado con el número de reloj Fullmar: "
                + nroRelojFullmar);
    }
}
