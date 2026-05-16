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
 * Este es el Controlador de Alquileres.
 * Su única función es recibir las peticiones de iniciar o finalizar
 * un alquiler y pasárselas al Servicio para que haga el trabajo pesado.
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
     * Endpoint para iniciar un nuevo alquiler.
     * Recibo un JSON, uso @Valid para revisarlo y luego
     * devuelvo un 201 Created si todo sale bien.
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
     * Endpoint para finalizar un alquiler existente.
     * Usé PUT porque estoy actualizando el estado de un registro
     * que ya existe en la base de datos.
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
