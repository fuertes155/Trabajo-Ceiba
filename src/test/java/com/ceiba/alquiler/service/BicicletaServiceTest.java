package com.ceiba.alquiler.service;

import com.ceiba.alquiler.dto.BicicletaRequestDTO;
import com.ceiba.alquiler.dto.BicicletaResponseDTO;
import com.ceiba.alquiler.exception.BicicletaNoEncontradaException;
import com.ceiba.alquiler.model.Bicicleta;
import com.ceiba.alquiler.model.enums.EstadoBicicleta;
import com.ceiba.alquiler.model.enums.TipoBicicleta;
import com.ceiba.alquiler.repository.BicicletaRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BicicletaServiceTest {

    @Mock
    private BicicletaRepository bicicletaRepository;

    @InjectMocks
    private BicicletaService bicicletaService;

    @Test
    @DisplayName("RF-01: Registrar bicicleta exitosamente")
    void registrarBicicleta_exitoso() {
        BicicletaRequestDTO request = new BicicletaRequestDTO("BIC-010", TipoBicicleta.URBANA, EstadoBicicleta.DISPONIBLE);
        when(bicicletaRepository.existsByCodigo("BIC-010")).thenReturn(false);
        when(bicicletaRepository.save(any(Bicicleta.class))).thenAnswer(i -> { Bicicleta b = i.getArgument(0); b.setId(10L); return b; });

        BicicletaResponseDTO response = bicicletaService.registrarBicicleta(request);

        assertNotNull(response);
        assertEquals("BIC-010", response.getCodigo());
        assertEquals(TipoBicicleta.URBANA, response.getTipo());
        verify(bicicletaRepository).save(any(Bicicleta.class));
    }

    @Test
    @DisplayName("RF-01: No permite código duplicado")
    void registrarBicicleta_codigoDuplicado() {
        BicicletaRequestDTO request = new BicicletaRequestDTO("BIC-001", TipoBicicleta.URBANA, EstadoBicicleta.DISPONIBLE);
        when(bicicletaRepository.existsByCodigo("BIC-001")).thenReturn(true);
        assertThrows(IllegalArgumentException.class, () -> bicicletaService.registrarBicicleta(request));
        verify(bicicletaRepository, never()).save(any());
    }

    @Test
    @DisplayName("RF-04: Consultar disponibles sin filtro")
    void consultarDisponibles_sinFiltro() {
        List<Bicicleta> disponibles = List.of(
                new Bicicleta("BIC-001", TipoBicicleta.URBANA, EstadoBicicleta.DISPONIBLE),
                new Bicicleta("BIC-002", TipoBicicleta.MONTANA, EstadoBicicleta.DISPONIBLE));
        when(bicicletaRepository.findByEstado(EstadoBicicleta.DISPONIBLE)).thenReturn(disponibles);
        List<BicicletaResponseDTO> resultado = bicicletaService.consultarDisponibles(null);
        assertEquals(2, resultado.size());
    }

    @Test
    @DisplayName("RF-04: Filtrar por tipo MONTANA")
    void consultarDisponibles_filtrarPorTipo() {
        List<Bicicleta> montanas = List.of(new Bicicleta("BIC-002", TipoBicicleta.MONTANA, EstadoBicicleta.DISPONIBLE));
        when(bicicletaRepository.findByEstadoAndTipo(EstadoBicicleta.DISPONIBLE, TipoBicicleta.MONTANA)).thenReturn(montanas);
        List<BicicletaResponseDTO> resultado = bicicletaService.consultarDisponibles(TipoBicicleta.MONTANA);
        assertEquals(1, resultado.size());
        assertEquals(TipoBicicleta.MONTANA, resultado.get(0).getTipo());
    }

    @Test
    @DisplayName("Buscar por código - no encontrada")
    void buscarPorCodigo_noEncontrada() {
        when(bicicletaRepository.findByCodigo("BIC-999")).thenReturn(Optional.empty());
        assertThrows(BicicletaNoEncontradaException.class, () -> bicicletaService.buscarPorCodigo("BIC-999"));
    }
}
