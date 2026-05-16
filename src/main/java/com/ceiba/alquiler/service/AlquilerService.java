package com.ceiba.alquiler.service;

import com.ceiba.alquiler.dto.AlquilerRequestDTO;
import com.ceiba.alquiler.dto.AlquilerResponseDTO;
import com.ceiba.alquiler.exception.AlquilerNoEncontradoException;
import com.ceiba.alquiler.exception.AlquilerYaFinalizadoException;
import com.ceiba.alquiler.exception.BicicletaNoDisponibleException;
import com.ceiba.alquiler.model.Alquiler;
import com.ceiba.alquiler.model.Bicicleta;
import com.ceiba.alquiler.model.enums.EstadoBicicleta;
import com.ceiba.alquiler.model.enums.TipoBicicleta;
import com.ceiba.alquiler.repository.AlquilerRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Servicio que encapsula la lógica de negocio para la gestión de alquileres.
 * Implementa las reglas de negocio RN-01 a RN-05:
 *  - Cálculo de costo base con redondeo al alza (RN-02)
 *  - Cálculo de multa por devolución tardía (RN-03)
 *  - Validación de disponibilidad de bicicleta (RN-04)
 *  - Validación de estado de alquiler (RN-05)
 */
@Service
@Transactional
public class AlquilerService {

    private static final BigDecimal PORCENTAJE_MULTA = new BigDecimal("0.50");

    private final AlquilerRepository alquilerRepository;
    private final BicicletaService bicicletaService;

    public AlquilerService(AlquilerRepository alquilerRepository, BicicletaService bicicletaService) {
        this.alquilerRepository = alquilerRepository;
        this.bicicletaService = bicicletaService;
    }

    /**
     * Inicia un nuevo alquiler de bicicleta (RF-02).
     * Valida que la bicicleta esté disponible (RN-04) y cambia su estado a ALQUILADA.
     *
     * @param request DTO con los datos del alquiler
     * @return DTO con el alquiler creado
     * @throws BicicletaNoDisponibleException si la bicicleta no está disponible
     */
    public AlquilerResponseDTO iniciarAlquiler(AlquilerRequestDTO request) {
        Bicicleta bicicleta = bicicletaService.buscarPorCodigo(request.getCodigoBicicleta());

        // RN-04: Validar que la bicicleta esté disponible
        if (bicicleta.getEstado() != EstadoBicicleta.DISPONIBLE) {
            throw new BicicletaNoDisponibleException(
                    String.format("La bicicleta %s no está disponible para alquiler. Estado actual: %s",
                            bicicleta.getCodigo(), bicicleta.getEstado())
            );
        }

        // Crear el alquiler
        Alquiler alquiler = new Alquiler();
        alquiler.setBicicleta(bicicleta);
        alquiler.setNombreCliente(request.getNombreCliente());
        alquiler.setHoraInicio(LocalDateTime.now());
        alquiler.setDuracionEstimadaHoras(request.getDuracionEstimadaHoras());
        alquiler.setActivo(true);

        // Cambiar estado de la bicicleta a ALQUILADA
        bicicleta.setEstado(EstadoBicicleta.ALQUILADA);

        Alquiler guardado = alquilerRepository.save(alquiler);
        return convertirAResponseDTO(guardado);
    }

    /**
     * Finaliza un alquiler activo (RF-03).
     * Calcula el costo total aplicando las reglas de negocio RN-01, RN-02 y RN-03.
     * Valida que el alquiler exista y no esté ya finalizado (RN-05).
     *
     * @param alquilerId ID del alquiler a finalizar
     * @return DTO con el alquiler finalizado, incluyendo costos calculados
     * @throws AlquilerNoEncontradoException si el alquiler no existe
     * @throws AlquilerYaFinalizadoException si el alquiler ya fue finalizado
     */
    public AlquilerResponseDTO finalizarAlquiler(Long alquilerId) {
        Alquiler alquiler = alquilerRepository.findById(alquilerId)
                .orElseThrow(() -> new AlquilerNoEncontradoException(
                        "No se encontró un alquiler con el ID: " + alquilerId
                ));

        // RN-05: Validar que el alquiler no esté ya finalizado
        if (!alquiler.isActivo()) {
            throw new AlquilerYaFinalizadoException(
                    String.format("El alquiler con ID %d ya fue finalizado previamente", alquilerId)
            );
        }

        // Registrar hora de devolución
        LocalDateTime horaFin = LocalDateTime.now();
        alquiler.setHoraFin(horaFin);

        // Calcular duración real en minutos
        long duracionRealMinutos = Duration.between(alquiler.getHoraInicio(), horaFin).toMinutes();
        if (duracionRealMinutos < 1) {
            duracionRealMinutos = 1; // Mínimo 1 minuto para evitar cobro de 0
        }
        alquiler.setDuracionRealMinutos(duracionRealMinutos);

        // Obtener tarifa por hora del tipo de bicicleta (RN-01)
        BigDecimal tarifaPorHora = alquiler.getBicicleta().getTipo().getTarifaPorHora();

        // RN-02: Calcular costo base (redondeo al alza a hora completa)
        long horasReales = calcularHorasRedondeadas(duracionRealMinutos);
        BigDecimal costoBase = tarifaPorHora.multiply(BigDecimal.valueOf(horasReales));
        alquiler.setCostoBase(costoBase);

        // RN-03: Calcular multa por devolución tardía
        BigDecimal multa = calcularMulta(
                duracionRealMinutos,
                alquiler.getDuracionEstimadaHoras(),
                tarifaPorHora
        );
        alquiler.setMulta(multa);

        // Costo total = costo base + multa
        alquiler.setCostoTotal(costoBase.add(multa));

        // Cambiar estado de la bicicleta a DISPONIBLE
        alquiler.getBicicleta().setEstado(EstadoBicicleta.DISPONIBLE);
        alquiler.setActivo(false);

        Alquiler finalizado = alquilerRepository.save(alquiler);
        return convertirAResponseDTO(finalizado);
    }

    /**
     * Consulta el historial de alquileres de una bicicleta por su código (RF-05).
     *
     * @param codigoBicicleta código de la bicicleta
     * @return lista de alquileres ordenados por fecha de inicio descendente
     */
    @Transactional(readOnly = true)
    public List<AlquilerResponseDTO> consultarHistorial(String codigoBicicleta) {
        // Verificar que la bicicleta existe
        bicicletaService.buscarPorCodigo(codigoBicicleta);

        return alquilerRepository.findByBicicletaCodigoOrderByHoraInicioDesc(codigoBicicleta)
                .stream()
                .map(this::convertirAResponseDTO)
                .collect(Collectors.toList());
    }

    // ==================== Métodos de Cálculo (Reglas de Negocio) ====================

    /**
     * Calcula las horas redondeadas al alza a la hora completa más cercana (RN-02).
     * Ejemplo: 70 min → 2 horas, 120 min → 2 horas, 121 min → 3 horas.
     *
     * @param minutos duración en minutos
     * @return horas redondeadas al alza
     */
    protected long calcularHorasRedondeadas(long minutos) {
        return (long) Math.ceil((double) minutos / 60.0);
    }

    /**
     * Calcula la multa por devolución tardía (RN-03).
     * Si el tiempo real supera la duración estimada, se cobra 50% de la tarifa/hora
     * por cada hora de retraso (redondeada al alza).
     *
     * @param duracionRealMinutos duración real del alquiler en minutos
     * @param duracionEstimadaHoras duración estimada en horas
     * @param tarifaPorHora tarifa por hora del tipo de bicicleta
     * @return monto de la multa (BigDecimal.ZERO si no hubo retraso)
     */
    protected BigDecimal calcularMulta(long duracionRealMinutos, int duracionEstimadaHoras, BigDecimal tarifaPorHora) {
        long minutosEstimados = (long) duracionEstimadaHoras * 60;
        long minutosRetraso = duracionRealMinutos - minutosEstimados;

        if (minutosRetraso <= 0) {
            return BigDecimal.ZERO;
        }

        long horasRetraso = calcularHorasRedondeadas(minutosRetraso);
        BigDecimal multaPorHora = tarifaPorHora.multiply(PORCENTAJE_MULTA);

        return multaPorHora.multiply(BigDecimal.valueOf(horasRetraso));
    }

    /**
     * Convierte una entidad Alquiler a su DTO de respuesta.
     */
    private AlquilerResponseDTO convertirAResponseDTO(Alquiler alquiler) {
        AlquilerResponseDTO dto = new AlquilerResponseDTO();
        dto.setId(alquiler.getId());
        dto.setCodigoBicicleta(alquiler.getBicicleta().getCodigo());
        dto.setTipoBicicleta(alquiler.getBicicleta().getTipo().name());
        dto.setNombreCliente(alquiler.getNombreCliente());
        dto.setHoraInicio(alquiler.getHoraInicio());
        dto.setDuracionEstimadaHoras(alquiler.getDuracionEstimadaHoras());
        dto.setHoraFin(alquiler.getHoraFin());
        dto.setDuracionRealMinutos(alquiler.getDuracionRealMinutos());
        dto.setCostoBase(alquiler.getCostoBase());
        dto.setMulta(alquiler.getMulta());
        dto.setCostoTotal(alquiler.getCostoTotal());
        dto.setActivo(alquiler.isActivo());
        dto.setTuvoMulta(alquiler.getMulta() != null && alquiler.getMulta().compareTo(BigDecimal.ZERO) > 0);
        return dto;
    }
}
