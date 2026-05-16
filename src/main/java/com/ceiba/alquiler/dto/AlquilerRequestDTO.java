package com.ceiba.alquiler.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Este es el JSON que me envían cuando alguien quiere iniciar un alquiler.
 * Le puse anotaciones de validación para asegurar que no manden datos vacíos.
 */
public class AlquilerRequestDTO {

    @NotBlank(message = "El código de la bicicleta es obligatorio")
    private String codigoBicicleta;

    @NotBlank(message = "El nombre del cliente es obligatorio")
    @Size(min = 2, max = 100, message = "El nombre del cliente debe tener entre 2 y 100 caracteres")
    private String nombreCliente;

    @Min(value = 1, message = "La duración estimada debe ser al menos 1 hora")
    private int duracionEstimadaHoras;

    public AlquilerRequestDTO() {
    }

    public AlquilerRequestDTO(String codigoBicicleta, String nombreCliente, int duracionEstimadaHoras) {
        this.codigoBicicleta = codigoBicicleta;
        this.nombreCliente = nombreCliente;
        this.duracionEstimadaHoras = duracionEstimadaHoras;
    }

    // --- Getters y Setters ---

    public String getCodigoBicicleta() {
        return codigoBicicleta;
    }

    public void setCodigoBicicleta(String codigoBicicleta) {
        this.codigoBicicleta = codigoBicicleta;
    }

    public String getNombreCliente() {
        return nombreCliente;
    }

    public void setNombreCliente(String nombreCliente) {
        this.nombreCliente = nombreCliente;
    }

    public int getDuracionEstimadaHoras() {
        return duracionEstimadaHoras;
    }

    public void setDuracionEstimadaHoras(int duracionEstimadaHoras) {
        this.duracionEstimadaHoras = duracionEstimadaHoras;
    }
}
