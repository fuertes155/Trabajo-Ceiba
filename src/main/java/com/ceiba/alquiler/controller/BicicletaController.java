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
 * Este es el Controlador para las Bicicletas.
 * Aquí expongo las URLs (endpoints) para que el frontend o Postman
 * puedan comunicarse con el sistema.
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
     * Endpoint para guardar una bicicleta nueva en la base de datos.
     * Usé @Valid para asegurarme de que el JSON que envían cumpla con las reglas
     * del DTO.
     */
    @PostMapping
    @Operation(summary = "Registrar una bicicleta", description = "Registra una nueva bicicleta con código único, tipo y estado")
    public ResponseEntity<BicicletaResponseDTO> registrarBicicleta(
            @Valid @RequestBody BicicletaRequestDTO request) {
        BicicletaResponseDTO response = bicicletaService.registrarBicicleta(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Endpoint para ver el catálogo de bicicletas disponibles.
     * Le puse un @RequestParam opcional por si el usuario quiere filtrar solo las
     * de Montaña, etc.
     */
    @GetMapping("/disponibles")
    @Operation(summary = "Consultar bicicletas disponibles", description = "Lista las bicicletas en estado DISPONIBLE, con filtro opcional por tipo")
    public ResponseEntity<List<BicicletaResponseDTO>> consultarDisponibles(
            @Parameter(description = "Tipo de bicicleta para filtrar (URBANA, MONTANA, ELECTRICA)") @RequestParam(required = false) TipoBicicleta tipo) {
        List<BicicletaResponseDTO> disponibles = bicicletaService.consultarDisponibles(tipo);
        return ResponseEntity.ok(disponibles);
    }

    /**
     * Endpoint para ver todo el historial de una bicicleta específica.
     * Recibo el código por la URL (ej: /api/bicicletas/BIC-001/historial).
     */
    @GetMapping("/{codigo}/historial")
    @Operation(summary = "Historial de alquileres", description = "Consulta todos los alquileres de una bicicleta, mostrando cliente, tiempos, costos y multas")
    public ResponseEntity<List<AlquilerResponseDTO>> consultarHistorial(
            @Parameter(description = "Código de la bicicleta (ej: BIC-001)") @PathVariable String codigo) {
        List<AlquilerResponseDTO> historial = alquilerService.consultarHistorial(codigo);
        return ResponseEntity.ok(historial);
    }
}
