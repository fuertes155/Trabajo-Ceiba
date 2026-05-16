package com.ceiba.alquiler.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO de respuesta para información de alquiler.
 * Incluye todos los datos del ciclo de vida: inicio, fin, costos y multa.
 */
public class AlquilerResponseDTO {

    private Long id;
    private String codigoBicicleta;
    private String tipoBicicleta;
    private String nombreCliente;
    private LocalDateTime horaInicio;
    private int duracionEstimadaHoras;
    private LocalDateTime horaFin;
    private Long duracionRealMinutos;
    private BigDecimal costoBase;
    private BigDecimal multa;
    private BigDecimal costoTotal;
    private boolean activo;
    private boolean tuvoMulta;

    public AlquilerResponseDTO() {
    }

    // --- Getters y Setters ---

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCodigoBicicleta() {
        return codigoBicicleta;
    }

    public void setCodigoBicicleta(String codigoBicicleta) {
        this.codigoBicicleta = codigoBicicleta;
    }

    public String getTipoBicicleta() {
        return tipoBicicleta;
    }

    public void setTipoBicicleta(String tipoBicicleta) {
        this.tipoBicicleta = tipoBicicleta;
    }

    public String getNombreCliente() {
        return nombreCliente;
    }

    public void setNombreCliente(String nombreCliente) {
        this.nombreCliente = nombreCliente;
    }

    public LocalDateTime getHoraInicio() {
        return horaInicio;
    }

    public void setHoraInicio(LocalDateTime horaInicio) {
        this.horaInicio = horaInicio;
    }

    public int getDuracionEstimadaHoras() {
        return duracionEstimadaHoras;
    }

    public void setDuracionEstimadaHoras(int duracionEstimadaHoras) {
        this.duracionEstimadaHoras = duracionEstimadaHoras;
    }

    public LocalDateTime getHoraFin() {
        return horaFin;
    }

    public void setHoraFin(LocalDateTime horaFin) {
        this.horaFin = horaFin;
    }

    public Long getDuracionRealMinutos() {
        return duracionRealMinutos;
    }

    public void setDuracionRealMinutos(Long duracionRealMinutos) {
        this.duracionRealMinutos = duracionRealMinutos;
    }

    public BigDecimal getCostoBase() {
        return costoBase;
    }

    public void setCostoBase(BigDecimal costoBase) {
        this.costoBase = costoBase;
    }

    public BigDecimal getMulta() {
        return multa;
    }

    public void setMulta(BigDecimal multa) {
        this.multa = multa;
    }

    public BigDecimal getCostoTotal() {
        return costoTotal;
    }

    public void setCostoTotal(BigDecimal costoTotal) {
        this.costoTotal = costoTotal;
    }

    public boolean isActivo() {
        return activo;
    }

    public void setActivo(boolean activo) {
        this.activo = activo;
    }

    public boolean isTuvoMulta() {
        return tuvoMulta;
    }

    public void setTuvoMulta(boolean tuvoMulta) {
        this.tuvoMulta = tuvoMulta;
    }
}
