package com.sigomei.api.excepciones;

public class RegistroNoEncontradoException extends Exception {

    public RegistroNoEncontradoException(String mensaje) {
        super(mensaje);
    }
}