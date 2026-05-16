package com.ceiba.alquiler.controller;

import com.ceiba.alquiler.dto.BicicletaRequestDTO;
import com.ceiba.alquiler.dto.BicicletaResponseDTO;
import com.ceiba.alquiler.dto.AlquilerResponseDTO;
import com.ceiba.alquiler.model.enums.TipoBicicleta;
import com.ceiba.alquiler.service.AlquilerService;
import com.ceiba.alquiler.service.BicicletaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controlador REST para la gestión de bicicletas.
 * Expone endpoints para registrar bicicletas, consultar disponibilidad e historial.
 */
@RestController
@RequestMapping("/api/bicicletas")
@Tag(name = "Bicicletas", description = "Operaciones de gestión de bicicletas")
public class BicicletaController {

    private final BicicletaService bicicletaService;
    private final AlquilerService alquilerService;

    public BicicletaController(BicicletaService bicicletaService, AlquilerService alquilerService) {
        this.bicicletaService = bicicletaService;
        this.alquilerService = alquilerService;
    }

    /**
     * Registra una nueva bicicleta en el sistema (RF-01).
     */
    @PostMapping
    @Operation(summary = "Registrar una bicicleta", description = "Registra una nueva bicicleta con código único, tipo y estado")
    public ResponseEntity<BicicletaResponseDTO> registrarBicicleta(
            @Valid @RequestBody BicicletaRequestDTO request) {
        BicicletaResponseDTO response = bicicletaService.registrarBicicleta(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Consulta las bicicletas disponibles con filtro opcional por tipo (RF-04).
     */
    @GetMapping("/disponibles")
    @Operation(summary = "Consultar bicicletas disponibles",
            description = "Lista las bicicletas en estado DISPONIBLE, con filtro opcional por tipo")
    public ResponseEntity<List<BicicletaResponseDTO>> consultarDisponibles(
            @Parameter(description = "Tipo de bicicleta para filtrar (URBANA, MONTANA, ELECTRICA)")
            @RequestParam(required = false) TipoBicicleta tipo) {
        List<BicicletaResponseDTO> disponibles = bicicletaService.consultarDisponibles(tipo);
        return ResponseEntity.ok(disponibles);
    }

    /**
     * Consulta el historial de alquileres de una bicicleta por su código (RF-05).
     */
    @GetMapping("/{codigo}/historial")
    @Operation(summary = "Historial de alquileres",
            description = "Consulta todos los alquileres de una bicicleta, mostrando cliente, tiempos, costos y multas")
    public ResponseEntity<List<AlquilerResponseDTO>> consultarHistorial(
            @Parameter(description = "Código de la bicicleta (ej: BIC-001)")
            @PathVariable String codigo) {
        List<AlquilerResponseDTO> historial = alquilerService.consultarHistorial(codigo);
        return ResponseEntity.ok(historial);
    }
}
