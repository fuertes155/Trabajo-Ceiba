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
 * Servicio que encapsula la lógica de negocio para la gestión de bicicletas.
 * Maneja el registro de bicicletas y las consultas de disponibilidad.
 */
@Service
@Transactional
public class BicicletaService {

    private final BicicletaRepository bicicletaRepository;

    public BicicletaService(BicicletaRepository bicicletaRepository) {
        this.bicicletaRepository = bicicletaRepository;
    }

    /**
     * Registra una nueva bicicleta en el sistema (RF-01).
     *
     * @param request DTO con los datos de la bicicleta
     * @return DTO con la bicicleta registrada
     * @throws IllegalArgumentException si ya existe una bicicleta con ese código
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
     * Consulta las bicicletas disponibles, con filtro opcional por tipo (RF-04).
     *
     * @param tipo filtro opcional por tipo de bicicleta
     * @return lista de bicicletas disponibles
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
     * Busca una bicicleta por su código.
     *
     * @param codigo código de la bicicleta
     * @return la entidad Bicicleta encontrada
     * @throws BicicletaNoEncontradaException si no existe
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
