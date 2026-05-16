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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Pruebas unitarias para AlquilerService.
 * Valida las reglas de negocio principales:
 *  - Cálculo de costo base con redondeo al alza (RN-02)
 *  - Cálculo de multas por devolución tardía (RN-03)
 *  - Validación de disponibilidad de bicicleta (RN-04)
 *  - Validación de estado de alquiler (RN-05)
 */
@ExtendWith(MockitoExtension.class)
class AlquilerServiceTest {

    @Mock
    private AlquilerRepository alquilerRepository;

    @Mock
    private BicicletaService bicicletaService;

    @InjectMocks
    private AlquilerService alquilerService;

    private Bicicleta bicicletaUrbana;
    private Bicicleta bicicletaMontana;
    private Bicicleta bicicletaElectrica;
    private Bicicleta bicicletaEnMantenimiento;

    @BeforeEach
    void setUp() {
        bicicletaUrbana = new Bicicleta("BIC-001", TipoBicicleta.URBANA, EstadoBicicleta.DISPONIBLE);
        bicicletaUrbana.setId(1L);

        bicicletaMontana = new Bicicleta("BIC-002", TipoBicicleta.MONTANA, EstadoBicicleta.DISPONIBLE);
        bicicletaMontana.setId(2L);

        bicicletaElectrica = new Bicicleta("BIC-003", TipoBicicleta.ELECTRICA, EstadoBicicleta.DISPONIBLE);
        bicicletaElectrica.setId(3L);

        bicicletaEnMantenimiento = new Bicicleta("BIC-004", TipoBicicleta.MONTANA, EstadoBicicleta.EN_MANTENIMIENTO);
        bicicletaEnMantenimiento.setId(4L);
    }

    // ==================== RN-02: Cálculo de horas redondeadas ====================

    @Nested
    @DisplayName("RN-02: Cálculo de horas redondeadas al alza")
    class CalculoHorasRedondeadas {

        @Test
        @DisplayName("60 minutos exactos → 1 hora")
        void horasExactas_noRedondea() {
            assertEquals(1, alquilerService.calcularHorasRedondeadas(60));
        }

        @Test
        @DisplayName("120 minutos exactos → 2 horas")
        void dosHorasExactas_noRedondea() {
            assertEquals(2, alquilerService.calcularHorasRedondeadas(120));
        }

        @Test
        @DisplayName("70 minutos (1h 10min) → 2 horas")
        void unaHoraDiezMinutos_redondeaADos() {
            assertEquals(2, alquilerService.calcularHorasRedondeadas(70));
        }

        @Test
        @DisplayName("1 minuto → 1 hora")
        void unMinuto_redondeaAUnaHora() {
            assertEquals(1, alquilerService.calcularHorasRedondeadas(1));
        }

        @Test
        @DisplayName("200 minutos (3h 20min) → 4 horas")
        void tresHorasVeinteMinutos_redondeaACuatro() {
            assertEquals(4, alquilerService.calcularHorasRedondeadas(200));
        }

        @Test
        @DisplayName("181 minutos (3h 1min) → 4 horas")
        void tresHorasUnMinuto_redondeaACuatro() {
            assertEquals(4, alquilerService.calcularHorasRedondeadas(181));
        }
    }

    // ==================== RN-03: Cálculo de multas ====================

    @Nested
    @DisplayName("RN-03: Cálculo de multas por devolución tardía")
    class CalculoMultas {

        @Test
        @DisplayName("Sin retraso: devuelve a tiempo → multa = $0")
        void sinRetraso_multaCero() {
            // Duración real: 100 min, estimada: 2h (120 min) → sin retraso
            BigDecimal multa = alquilerService.calcularMulta(100, 2, new BigDecimal("5000"));
            assertEquals(BigDecimal.ZERO, multa);
        }

        @Test
        @DisplayName("Sin retraso: devuelve exacto → multa = $0")
        void devolucionExacta_multaCero() {
            // Duración real: 120 min, estimada: 2h (120 min) → sin retraso
            BigDecimal multa = alquilerService.calcularMulta(120, 2, new BigDecimal("5000"));
            assertEquals(BigDecimal.ZERO, multa);
        }

        @Test
        @DisplayName("Ejemplo del enunciado: MONTAÑA, estimada 2h, devuelta 3h20min → multa $5.000")
        void ejemploEnunciado_multaCorrecta() {
            // Duración real: 200 min, estimada: 2h (120 min)
            // Retraso: 80 min → ceil(80/60) = 2 horas de retraso
            // Multa: 2 × (5000 × 0.50) = 2 × 2500 = $5.000
            BigDecimal multa = alquilerService.calcularMulta(200, 2, new BigDecimal("5000"));
            assertEquals(new BigDecimal("5000.00"), multa);
        }

        @Test
        @DisplayName("Retraso de 1 minuto → 1 hora de retraso")
        void retrasoMinimo_unaHora() {
            // Estimada: 1h (60 min), real: 61 min → 1 min retraso → ceil = 1h
            // Multa: 1 × (3500 × 0.50) = $1.750
            BigDecimal multa = alquilerService.calcularMulta(61, 1, new BigDecimal("3500"));
            assertEquals(new BigDecimal("1750.00"), multa);
        }

        @Test
        @DisplayName("Retraso de 60 minutos exactos → 1 hora de retraso")
        void retrasoUnaHoraExacta() {
            // Estimada: 1h (60 min), real: 120 min → 60 min retraso → ceil = 1h
            // Multa: 1 × (7500 × 0.50) = $3.750
            BigDecimal multa = alquilerService.calcularMulta(120, 1, new BigDecimal("7500"));
            assertEquals(new BigDecimal("3750.00"), multa);
        }

        @Test
        @DisplayName("Retraso de 61 minutos → 2 horas de retraso")
        void retrasoSesentaYUnMinutos_dosHoras() {
            // Estimada: 1h (60 min), real: 121 min → 61 min retraso → ceil = 2h
            // Multa: 2 × (5000 × 0.50) = $5.000
            BigDecimal multa = alquilerService.calcularMulta(121, 1, new BigDecimal("5000"));
            assertEquals(new BigDecimal("5000.00"), multa);
        }
    }

    // ==================== Costo total completo (ejemplo del enunciado) ====================

    @Nested
    @DisplayName("Cálculo de costo total completo")
    class CalculoCostoTotal {

        @Test
        @DisplayName("Ejemplo del enunciado: MONTAÑA, estimada 2h, devuelta 3h20min → total $25.000")
        void ejemploEnunciado_costoTotalCorrecto() {
            // Datos: MONTAÑA ($5.000/h), estimada 2h, real 200 min (3h 20min)
            BigDecimal tarifaPorHora = new BigDecimal("5000");

            // Costo base: ceil(200/60) = 4h × $5.000 = $20.000
            long horasReales = alquilerService.calcularHorasRedondeadas(200);
            BigDecimal costoBase = tarifaPorHora.multiply(BigDecimal.valueOf(horasReales));
            assertEquals(new BigDecimal("20000"), costoBase);

            // Multa: 2 horas retraso × $2.500 = $5.000
            BigDecimal multa = alquilerService.calcularMulta(200, 2, tarifaPorHora);
            assertEquals(new BigDecimal("5000.00"), multa);

            // Total: $20.000 + $5.000 = $25.000
            BigDecimal total = costoBase.add(multa);
            assertEquals(new BigDecimal("25000.00"), total);
        }

        @Test
        @DisplayName("URBANA, estimada 3h, devuelta a 3h exactas → total $10.500 sin multa")
        void urbanaSinRetraso_sinMulta() {
            BigDecimal tarifaPorHora = new BigDecimal("3500");

            long horasReales = alquilerService.calcularHorasRedondeadas(180);
            BigDecimal costoBase = tarifaPorHora.multiply(BigDecimal.valueOf(horasReales));
            assertEquals(new BigDecimal("10500"), costoBase);

            BigDecimal multa = alquilerService.calcularMulta(180, 3, tarifaPorHora);
            assertEquals(BigDecimal.ZERO, multa);

            BigDecimal total = costoBase.add(multa);
            assertEquals(new BigDecimal("10500"), total);
        }

        @Test
        @DisplayName("ELÉCTRICA, estimada 1h, devuelta a 1h30min → total $15.000 + $3.750 multa")
        void electricaConRetraso() {
            BigDecimal tarifaPorHora = new BigDecimal("7500");

            // Costo base: ceil(90/60) = 2h × $7.500 = $15.000
            long horasReales = alquilerService.calcularHorasRedondeadas(90);
            assertEquals(2, horasReales);
            BigDecimal costoBase = tarifaPorHora.multiply(BigDecimal.valueOf(horasReales));
            assertEquals(new BigDecimal("15000"), costoBase);

            // Multa: retraso 30 min → ceil = 1h → 1 × ($7.500 × 0.50) = $3.750
            BigDecimal multa = alquilerService.calcularMulta(90, 1, tarifaPorHora);
            assertEquals(new BigDecimal("3750.00"), multa);

            // Total: $15.000 + $3.750 = $18.750
            BigDecimal total = costoBase.add(multa);
            assertEquals(new BigDecimal("18750.00"), total);
        }
    }

    // ==================== RF-02: Iniciar alquiler ====================

    @Nested
    @DisplayName("RF-02: Iniciar alquiler")
    class IniciarAlquiler {

        @Test
        @DisplayName("Iniciar alquiler exitoso: bicicleta pasa a ALQUILADA")
        void iniciarAlquiler_exitoso() {
            AlquilerRequestDTO request = new AlquilerRequestDTO("BIC-001", "Juan Pérez", 2);

            when(bicicletaService.buscarPorCodigo("BIC-001")).thenReturn(bicicletaUrbana);
            when(alquilerRepository.save(any(Alquiler.class))).thenAnswer(invocation -> {
                Alquiler alquiler = invocation.getArgument(0);
                alquiler.setId(1L);
                return alquiler;
            });

            AlquilerResponseDTO response = alquilerService.iniciarAlquiler(request);

            assertNotNull(response);
            assertEquals("BIC-001", response.getCodigoBicicleta());
            assertEquals("Juan Pérez", response.getNombreCliente());
            assertTrue(response.isActivo());
            assertEquals(EstadoBicicleta.ALQUILADA, bicicletaUrbana.getEstado());
        }

        @Test
        @DisplayName("RN-04: No permite alquilar bicicleta EN_MANTENIMIENTO")
        void noPermiteAlquilar_enMantenimiento() {
            AlquilerRequestDTO request = new AlquilerRequestDTO("BIC-004", "María López", 1);

            when(bicicletaService.buscarPorCodigo("BIC-004")).thenReturn(bicicletaEnMantenimiento);

            BicicletaNoDisponibleException exception = assertThrows(
                    BicicletaNoDisponibleException.class,
                    () -> alquilerService.iniciarAlquiler(request)
            );

            assertTrue(exception.getMessage().contains("BIC-004"));
            assertTrue(exception.getMessage().contains("EN_MANTENIMIENTO"));
        }

        @Test
        @DisplayName("RN-04: No permite alquilar bicicleta ALQUILADA")
        void noPermiteAlquilar_yaAlquilada() {
            bicicletaUrbana.setEstado(EstadoBicicleta.ALQUILADA);
            AlquilerRequestDTO request = new AlquilerRequestDTO("BIC-001", "Carlos Gómez", 3);

            when(bicicletaService.buscarPorCodigo("BIC-001")).thenReturn(bicicletaUrbana);

            assertThrows(BicicletaNoDisponibleException.class,
                    () -> alquilerService.iniciarAlquiler(request));
        }
    }

    // ==================== RF-03: Finalizar alquiler ====================

    @Nested
    @DisplayName("RF-03 & RN-05: Finalizar alquiler")
    class FinalizarAlquiler {

        @Test
        @DisplayName("RN-05: No permite finalizar alquiler inexistente")
        void noPermiteFinalizarAlquiler_inexistente() {
            when(alquilerRepository.findById(999L)).thenReturn(Optional.empty());

            assertThrows(AlquilerNoEncontradoException.class,
                    () -> alquilerService.finalizarAlquiler(999L));
        }

        @Test
        @DisplayName("RN-05: No permite finalizar alquiler ya finalizado")
        void noPermiteFinalizarAlquiler_yaFinalizado() {
            Alquiler alquilerFinalizado = new Alquiler();
            alquilerFinalizado.setId(1L);
            alquilerFinalizado.setActivo(false);
            alquilerFinalizado.setBicicleta(bicicletaUrbana);

            when(alquilerRepository.findById(1L)).thenReturn(Optional.of(alquilerFinalizado));

            AlquilerYaFinalizadoException exception = assertThrows(
                    AlquilerYaFinalizadoException.class,
                    () -> alquilerService.finalizarAlquiler(1L)
            );

            assertTrue(exception.getMessage().contains("1"));
        }

        @Test
        @DisplayName("Finalizar alquiler exitoso: bicicleta vuelve a DISPONIBLE")
        void finalizarAlquiler_exitoso_cambiaEstado() {
            bicicletaMontana.setEstado(EstadoBicicleta.ALQUILADA);

            Alquiler alquilerActivo = new Alquiler();
            alquilerActivo.setId(1L);
            alquilerActivo.setBicicleta(bicicletaMontana);
            alquilerActivo.setNombreCliente("Ana García");
            alquilerActivo.setHoraInicio(LocalDateTime.now().minusHours(2));
            alquilerActivo.setDuracionEstimadaHoras(3);
            alquilerActivo.setActivo(true);

            when(alquilerRepository.findById(1L)).thenReturn(Optional.of(alquilerActivo));
            when(alquilerRepository.save(any(Alquiler.class))).thenAnswer(i -> i.getArgument(0));

            AlquilerResponseDTO response = alquilerService.finalizarAlquiler(1L);

            assertFalse(response.isActivo());
            assertNotNull(response.getHoraFin());
            assertNotNull(response.getCostoTotal());
            assertEquals(EstadoBicicleta.DISPONIBLE, bicicletaMontana.getEstado());
        }
    }

    // ==================== RF-05: Historial de alquileres ====================

    @Nested
    @DisplayName("RF-05: Historial de alquileres")
    class HistorialAlquileres {

        @Test
        @DisplayName("Consultar historial retorna lista de alquileres")
        void consultarHistorial_retornaLista() {
            Alquiler alquiler1 = new Alquiler();
            alquiler1.setId(1L);
            alquiler1.setBicicleta(bicicletaUrbana);
            alquiler1.setNombreCliente("Cliente 1");
            alquiler1.setHoraInicio(LocalDateTime.now().minusDays(2));
            alquiler1.setDuracionEstimadaHoras(2);
            alquiler1.setActivo(false);
            alquiler1.setHoraFin(LocalDateTime.now().minusDays(2).plusHours(2));
            alquiler1.setCostoBase(new BigDecimal("7000"));
            alquiler1.setMulta(BigDecimal.ZERO);
            alquiler1.setCostoTotal(new BigDecimal("7000"));
            alquiler1.setDuracionRealMinutos(120L);

            when(bicicletaService.buscarPorCodigo("BIC-001")).thenReturn(bicicletaUrbana);
            when(alquilerRepository.findByBicicletaCodigoOrderByHoraInicioDesc("BIC-001"))
                    .thenReturn(List.of(alquiler1));

            List<AlquilerResponseDTO> historial = alquilerService.consultarHistorial("BIC-001");

            assertEquals(1, historial.size());
            assertEquals("Cliente 1", historial.get(0).getNombreCliente());
            assertFalse(historial.get(0).isTuvoMulta());
        }

        @Test
        @DisplayName("Historial vacío retorna lista vacía")
        void consultarHistorial_vacio() {
            when(bicicletaService.buscarPorCodigo("BIC-005")).thenReturn(
                    new Bicicleta("BIC-005", TipoBicicleta.URBANA, EstadoBicicleta.DISPONIBLE));
            when(alquilerRepository.findByBicicletaCodigoOrderByHoraInicioDesc("BIC-005"))
                    .thenReturn(List.of());

            List<AlquilerResponseDTO> historial = alquilerService.consultarHistorial("BIC-005");

            assertTrue(historial.isEmpty());
        }
    }
}
