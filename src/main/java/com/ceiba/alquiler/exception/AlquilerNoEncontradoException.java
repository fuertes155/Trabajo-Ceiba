package com.ceiba.alquiler.exception;

/**
 * Excepción lanzada cuando no se encuentra un alquiler con el ID proporcionado (RN-05).
 */
public class AlquilerNoEncontradoException extends RuntimeException {

    public AlquilerNoEncontradoException(String mensaje) {
        super(mensaje);
    }
}
