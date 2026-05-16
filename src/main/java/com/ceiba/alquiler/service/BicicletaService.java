package com.ceiba.alquiler.service;

import com.ceiba.alquiler.dto.BicicletaRequestDTO;
import com.ceiba.alquiler.dto.BicicletaResponseDTO;
import com.ceiba.alquiler.exception.BicicletaNoEncontradaException;
import com.ceiba.alquiler.model.Bicicleta;
import com.ceiba.alquiler.model.enums.EstadoBicicleta;
import com.ceiba.alquiler.model.enums.TipoBicicleta;
import com.ceiba.alquiler.repository.BicicletaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Servicio para gestionar todo lo relacionado con las bicicletas.
 * Lo mantuve simple: solo registro y consultas.
 */
@Service
@Transactional
public class BicicletaService {

    private final BicicletaRepository bicicletaRepository;

    public BicicletaService(BicicletaRepository bicicletaRepository) {
        this.bicicletaRepository = bicicletaRepository;
    }

    /**
     * Guardo la bicicleta nueva, pero primero verifico que
     * no me intenten meter un código repetido.
     */
    public BicicletaResponseDTO registrarBicicleta(BicicletaRequestDTO request) {
        if (bicicletaRepository.existsByCodigo(request.getCodigo())) {
            throw new IllegalArgumentException(
                    "Ya existe una bicicleta registrada con el código: " + request.getCodigo()
            );
        }

        Bicicleta bicicleta = new Bicicleta(
                request.getCodigo(),
                request.getTipo(),
                request.getEstado()
        );

        Bicicleta guardada = bicicletaRepository.save(bicicleta);
        return convertirAResponseDTO(guardada);
    }

    /**
     * Busco las bicicletas disponibles. Si me pasan un tipo (ej. URBANA),
     * filtro por ese tipo. Si no, las devuelvo todas.
     */
    @Transactional(readOnly = true)
    public List<BicicletaResponseDTO> consultarDisponibles(TipoBicicleta tipo) {
        List<Bicicleta> bicicletas;

        if (tipo != null) {
            bicicletas = bicicletaRepository.findByEstadoAndTipo(EstadoBicicleta.DISPONIBLE, tipo);
        } else {
            bicicletas = bicicletaRepository.findByEstado(EstadoBicicleta.DISPONIBLE);
        }

        return bicicletas.stream()
                .map(this::convertirAResponseDTO)
                .collect(Collectors.toList());
    }

    /**
     * Función utilitaria para buscar una bici. Lanza un error 404 si no existe.
     */
    @Transactional(readOnly = true)
    public Bicicleta buscarPorCodigo(String codigo) {
        return bicicletaRepository.findByCodigo(codigo)
                .orElseThrow(() -> new BicicletaNoEncontradaException(
                        "No se encontró una bicicleta con el código: " + codigo
                ));
    }

    /**
     * Convierte una entidad Bicicleta a su DTO de respuesta.
     */
    private BicicletaResponseDTO convertirAResponseDTO(Bicicleta bicicleta) {
        return new BicicletaResponseDTO(
                bicicleta.getId(),
                bicicleta.getCodigo(),
                bicicleta.getTipo(),
                bicicleta.getEstado()
        );
    }
}
