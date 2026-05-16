package com.ceiba.alquiler.dto;

import com.ceiba.alquiler.model.enums.EstadoBicicleta;
import com.ceiba.alquiler.model.enums.TipoBicicleta;

import java.math.BigDecimal;

/**
 * DTO de respuesta para información de bicicleta.
 */
public class BicicletaResponseDTO {

    private Long id;
    private String codigo;
    private TipoBicicleta tipo;
    private EstadoBicicleta estado;
    private BigDecimal tarifaPorHora;

    public BicicletaResponseDTO() {
    }

    public BicicletaResponseDTO(Long id, String codigo, TipoBicicleta tipo, EstadoBicicleta estado) {
        this.id = id;
        this.codigo = codigo;
        this.tipo = tipo;
        this.estado = estado;
        this.tarifaPorHora = tipo.getTarifaPorHora();
    }

    // --- Getters y Setters ---

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }

    public TipoBicicleta getTipo() {
        return tipo;
    }

    public void setTipo(TipoBicicleta tipo) {
        this.tipo = tipo;
    }

    public EstadoBicicleta getEstado() {
        return estado;
    }

    public void setEstado(EstadoBicicleta estado) {
        this.estado = estado;
    }

    public BigDecimal getTarifaPorHora() {
        return tarifaPorHora;
    }

    public void setTarifaPorHora(BigDecimal tarifaPorHora) {
        this.tarifaPorHora = tarifaPorHora;
    }
}
