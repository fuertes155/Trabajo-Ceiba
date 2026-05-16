package com.ceiba.alquiler.exception;

/**
 * Excepción lanzada cuando se intenta finalizar un alquiler
 * que ya fue finalizado previamente (RN-05).
 */
public class AlquilerYaFinalizadoException extends RuntimeException {

    public AlquilerYaFinalizadoException(String mensaje) {
        super(mensaje);
    }
}
