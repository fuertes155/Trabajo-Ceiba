package com.ceiba.alquiler.dto;

import com.ceiba.alquiler.model.enums.EstadoBicicleta;
import com.ceiba.alquiler.model.enums.TipoBicicleta;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

/**
 * DTO para la solicitud de registro de una nueva bicicleta.
 */
public class BicicletaRequestDTO {

    @NotBlank(message = "El código de la bicicleta es obligatorio")
    @Pattern(regexp = "^BIC-\\d{3}$", message = "El código debe seguir el formato BIC-XXX (ej: BIC-001)")
    private String codigo;

    @NotNull(message = "El tipo de bicicleta es obligatorio")
    private TipoBicicleta tipo;

    @NotNull(message = "El estado de la bicicleta es obligatorio")
    private EstadoBicicleta estado;

    public BicicletaRequestDTO() {
    }

    public BicicletaRequestDTO(String codigo, TipoBicicleta tipo, EstadoBicicleta estado) {
        this.codigo = codigo;
        this.tipo = tipo;
        this.estado = estado;
    }

    // --- Getters y Setters ---

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
}
