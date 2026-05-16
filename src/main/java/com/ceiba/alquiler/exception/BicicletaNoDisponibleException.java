package com.ceiba.alquiler.exception;

/**
 * Excepción lanzada cuando se intenta alquilar una bicicleta
 * que no está en estado DISPONIBLE (RN-04).
 */
public class BicicletaNoDisponibleException extends RuntimeException {

    public BicicletaNoDisponibleException(String mensaje) {
        super(mensaje);
    }
}
