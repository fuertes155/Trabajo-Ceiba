package com.ceiba.alquiler.controller;

import com.ceiba.alquiler.model.Bicicleta;
import com.ceiba.alquiler.model.enums.EstadoBicicleta;
import com.ceiba.alquiler.model.enums.TipoBicicleta;
import com.ceiba.alquiler.repository.BicicletaRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class BicicletaControllerIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private BicicletaRepository bicicletaRepository;

    @BeforeEach
    void setUp() {
        bicicletaRepository.deleteAll();
        bicicletaRepository.save(new Bicicleta("BIC-001", TipoBicicleta.URBANA, EstadoBicicleta.DISPONIBLE));
        bicicletaRepository.save(new Bicicleta("BIC-002", TipoBicicleta.MONTANA, EstadoBicicleta.DISPONIBLE));
        bicicletaRepository.save(new Bicicleta("BIC-003", TipoBicicleta.ELECTRICA, EstadoBicicleta.DISPONIBLE));
        bicicletaRepository.save(new Bicicleta("BIC-004", TipoBicicleta.MONTANA, EstadoBicicleta.EN_MANTENIMIENTO));
    }

    @Test
    @DisplayName("POST /api/bicicletas - Registrar bicicleta nueva")
    void registrarBicicleta() throws Exception {
        String json = """
                {"codigo":"BIC-010","tipo":"URBANA","estado":"DISPONIBLE"}
                """;
        mockMvc.perform(post("/api/bicicletas").contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.codigo").value("BIC-010"))
                .andExpect(jsonPath("$.tipo").value("URBANA"));
    }

    @Test
    @DisplayName("POST /api/bicicletas - Código duplicado retorna 400")
    void registrarBicicleta_duplicado() throws Exception {
        String json = """
                {"codigo":"BIC-001","tipo":"URBANA","estado":"DISPONIBLE"}
                """;
        mockMvc.perform(post("/api/bicicletas").contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("GET /api/bicicletas/disponibles - Lista todas las disponibles")
    void consultarDisponibles() throws Exception {
        mockMvc.perform(get("/api/bicicletas/disponibles"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(3)));
    }

    @Test
    @DisplayName("GET /api/bicicletas/disponibles?tipo=MONTANA - Filtra por tipo")
    void consultarDisponibles_filtro() throws Exception {
        mockMvc.perform(get("/api/bicicletas/disponibles").param("tipo", "MONTANA"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].tipo").value("MONTANA"));
    }

    @Test
    @DisplayName("GET /api/bicicletas/{codigo}/historial - Historial vacío")
    void consultarHistorial_vacio() throws Exception {
        mockMvc.perform(get("/api/bicicletas/BIC-001/historial"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    @DisplayName("GET /api/bicicletas/{codigo}/historial - Bicicleta inexistente retorna 404")
    void consultarHistorial_noExiste() throws Exception {
        mockMvc.perform(get("/api/bicicletas/BIC-999/historial"))
                .andExpect(status().isNotFound());
    }
}
