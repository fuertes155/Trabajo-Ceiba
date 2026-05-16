package com.ceiba.alquiler.model.enums;

/**
 * Estados posibles de una bicicleta en el sistema.
 * Solo las bicicletas en estado DISPONIBLE pueden ser alquiladas.
 */
public enum EstadoBicicleta {
    DISPONIBLE,
    ALQUILADA,
    EN_MANTENIMIENTO
}
