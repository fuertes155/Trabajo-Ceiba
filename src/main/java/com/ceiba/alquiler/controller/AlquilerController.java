package com.ceiba.alquiler.controller;

import com.ceiba.alquiler.dto.AlquilerRequestDTO;
import com.ceiba.alquiler.dto.AlquilerResponseDTO;
import com.ceiba.alquiler.service.AlquilerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controlador REST para la gestión de alquileres.
 * Expone endpoints para iniciar y finalizar alquileres de bicicletas.
 */
@RestController
@RequestMapping("/api/alquileres")
@Tag(name = "Alquileres", description = "Operaciones de alquiler de bicicletas")
public class AlquilerController {

    private final AlquilerService alquilerService;

    public AlquilerController(AlquilerService alquilerService) {
        this.alquilerService = alquilerService;
    }

    /**
     * Inicia un nuevo alquiler de bicicleta (RF-02).
     */
    @PostMapping
    @Operation(summary = "Iniciar un alquiler",
            description = "Inicia el alquiler de una bicicleta disponible, registrando cliente y duración estimada")
    public ResponseEntity<AlquilerResponseDTO> iniciarAlquiler(
            @Valid @RequestBody AlquilerRequestDTO request) {
        AlquilerResponseDTO response = alquilerService.iniciarAlquiler(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Finaliza un alquiler activo (RF-03).
     */
    @PutMapping("/{id}/finalizar")
    @Operation(summary = "Finalizar un alquiler",
            description = "Finaliza un alquiler activo, calcula costo total y aplica multa si corresponde")
    public ResponseEntity<AlquilerResponseDTO> finalizarAlquiler(
            @Parameter(description = "ID del alquiler a finalizar")
            @PathVariable Long id) {
        AlquilerResponseDTO response = alquilerService.finalizarAlquiler(id);
        return ResponseEntity.ok(response);
    }
}
