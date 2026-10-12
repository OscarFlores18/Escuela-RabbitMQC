package com.app.Auth.exception;

public class UsuarioYaExisteException extends RuntimeException {

    public UsuarioYaExisteException(String mensaje) {
        super(mensaje);
    }
}
