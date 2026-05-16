package com.ceiba.alquiler.model.enums;

import java.math.BigDecimal;

/**
 * Tipos de bicicleta disponibles en el sistema con su tarifa por hora asociada.
 * Las tarifas están definidas en pesos colombianos (COP).
 */
public enum TipoBicicleta {

    URBANA(new BigDecimal("3500")),
    MONTANA(new BigDecimal("5000")),
    ELECTRICA(new BigDecimal("7500"));

    private final BigDecimal tarifaPorHora;

    TipoBicicleta(BigDecimal tarifaPorHora) {
        this.tarifaPorHora = tarifaPorHora;
    }

    public BigDecimal getTarifaPorHora() {
        return tarifaPorHora;
    }
}
