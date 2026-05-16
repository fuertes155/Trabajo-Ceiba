package com.ceiba.alquiler.service;

import com.ceiba.alquiler.dto.AlquilerRequestDTO;
import com.ceiba.alquiler.dto.AlquilerResponseDTO;
import com.ceiba.alquiler.exception.AlquilerNoEncontradoException;
import com.ceiba.alquiler.exception.AlquilerYaFinalizadoException;
import com.ceiba.alquiler.exception.BicicletaNoDisponibleException;
import com.ceiba.alquiler.model.Alquiler;
import com.ceiba.alquiler.model.Bicicleta;
import com.ceiba.alquiler.model.enums.EstadoBicicleta;
import com.ceiba.alquiler.repository.AlquilerRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Aquí centralicé toda la lógica de negocio de los alquileres.
 * Decidí poner aquí los cálculos de las horas, los costos y las multas
 * para que los Controladores queden limpios y solo se encarguen de la web.
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
     * Este método se encarga de iniciar un alquiler nuevo.
     * Primero verifico si la bicicleta está disponible, si no, lanzo un error.
     * Luego guardo la hora exacta en la que el cliente se la lleva.
     */
    public AlquilerResponseDTO iniciarAlquiler(AlquilerRequestDTO request) {
        Bicicleta bicicleta = bicicletaService.buscarPorCodigo(request.getCodigoBicicleta());

        // RN-04: Validar que la bicicleta esté disponible
        if (bicicleta.getEstado() != EstadoBicicleta.DISPONIBLE) {
            throw new BicicletaNoDisponibleException(
                    String.format("La bicicleta %s no está disponible para alquiler. Estado actual: %s",
                            bicicleta.getCodigo(), bicicleta.getEstado()));
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
     * Este es el método más importante. Aquí finalizo el alquiler y hago toda la
     * matemática.
     * Calculo cuánto tiempo pasó realmente, cuánto cobrarle de base, y si se pasó
     * de la hora,
     * le aplico la multa del 50%.
     */
    public AlquilerResponseDTO finalizarAlquiler(Long alquilerId) {
        Alquiler alquiler = alquilerRepository.findById(alquilerId)
                .orElseThrow(() -> new AlquilerNoEncontradoException(
                        "No se encontró un alquiler con el ID: " + alquilerId));

        // RN-05: Validar que el alquiler no esté ya finalizado
        if (!alquiler.isActivo()) {
            throw new AlquilerYaFinalizadoException(
                    String.format("El alquiler con ID %d ya fue finalizado previamente", alquilerId));
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
                tarifaPorHora);
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
     * Un método sencillo para buscar todos los alquileres que ha tenido una
     * bicicleta,
     * ordenados del más reciente al más antiguo.
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

    // ==================== Métodos de Cálculo (Reglas de Negocio)
    // ====================

    /**
     * Creé esta pequeña función de ayuda para la Regla de Negocio 02.
     * Simplemente usa Math.ceil para redondear cualquier fracción de hora hacia
     * arriba.
     * Por ejemplo, si pasaron 65 minutos, devuelve 2 horas.
     */
    protected long calcularHorasRedondeadas(long minutos) {
        return (long) Math.ceil((double) minutos / 60.0);
    }

    /**
     * Esta es mi implementación de la Regla de Negocio 03 (La Multa).
     * Verifico si el tiempo real fue mayor al estimado. Si es así, cobro
     * la mitad de la tarifa (50%) por cada hora extra, usando el mismo redondeo de
     * arriba.
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
