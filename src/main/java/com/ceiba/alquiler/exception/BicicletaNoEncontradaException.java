package com.ceiba.alquiler.exception;

/**
 * Excepción lanzada cuando no se encuentra una bicicleta con el código proporcionado.
 */
public class BicicletaNoEncontradaException extends RuntimeException {

    public BicicletaNoEncontradaException(String mensaje) {
        super(mensaje);
    }
}
