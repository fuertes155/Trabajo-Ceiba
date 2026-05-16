package com.ceiba.alquiler.controller;

import com.ceiba.alquiler.model.Alquiler;
import com.ceiba.alquiler.model.Bicicleta;
import com.ceiba.alquiler.model.enums.EstadoBicicleta;
import com.ceiba.alquiler.model.enums.TipoBicicleta;
import com.ceiba.alquiler.repository.AlquilerRepository;
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

import java.time.LocalDateTime;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AlquilerControllerIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private BicicletaRepository bicicletaRepository;
    @Autowired private AlquilerRepository alquilerRepository;

    private Bicicleta bicicletaDisponible;
    private Bicicleta bicicletaEnMantenimiento;

    @BeforeEach
    void setUp() {
        alquilerRepository.deleteAll();
        bicicletaRepository.deleteAll();
        bicicletaDisponible = bicicletaRepository.save(new Bicicleta("BIC-001", TipoBicicleta.URBANA, EstadoBicicleta.DISPONIBLE));
        bicicletaEnMantenimiento = bicicletaRepository.save(new Bicicleta("BIC-004", TipoBicicleta.MONTANA, EstadoBicicleta.EN_MANTENIMIENTO));
    }

    @Test
    @DisplayName("POST /api/alquileres - Iniciar alquiler exitoso")
    void iniciarAlquiler_exitoso() throws Exception {
        String json = """
                {"codigoBicicleta":"BIC-001","nombreCliente":"Juan Pérez","duracionEstimadaHoras":2}
                """;
        mockMvc.perform(post("/api/alquileres").contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.codigoBicicleta").value("BIC-001"))
                .andExpect(jsonPath("$.nombreCliente").value("Juan Pérez"))
                .andExpect(jsonPath("$.activo").value(true));
    }

    @Test
    @DisplayName("POST /api/alquileres - Bicicleta en mantenimiento retorna 409")
    void iniciarAlquiler_noDisponible() throws Exception {
        String json = """
                {"codigoBicicleta":"BIC-004","nombreCliente":"María López","duracionEstimadaHoras":1}
                """;
        mockMvc.perform(post("/api/alquileres").contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.mensaje", containsString("EN_MANTENIMIENTO")));
    }

    @Test
    @DisplayName("POST /api/alquileres - Validación: nombre vacío retorna 400")
    void iniciarAlquiler_validacionFalla() throws Exception {
        String json = """
                {"codigoBicicleta":"BIC-001","nombreCliente":"","duracionEstimadaHoras":2}
                """;
        mockMvc.perform(post("/api/alquileres").contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("PUT /api/alquileres/{id}/finalizar - Finalizar alquiler exitoso")
    void finalizarAlquiler_exitoso() throws Exception {
        // Crear un alquiler activo directamente
        Alquiler alquiler = new Alquiler();
        alquiler.setBicicleta(bicicletaDisponible);
        alquiler.setNombreCliente("Test Cliente");
        alquiler.setHoraInicio(LocalDateTime.now().minusHours(2));
        alquiler.setDuracionEstimadaHoras(3);
        alquiler.setActivo(true);
        bicicletaDisponible.setEstado(EstadoBicicleta.ALQUILADA);
        bicicletaRepository.save(bicicletaDisponible);
        Alquiler guardado = alquilerRepository.save(alquiler);

        mockMvc.perform(put("/api/alquileres/" + guardado.getId() + "/finalizar"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.activo").value(false))
                .andExpect(jsonPath("$.costoTotal").isNotEmpty())
                .andExpect(jsonPath("$.horaFin").isNotEmpty());
    }

    @Test
    @DisplayName("PUT /api/alquileres/{id}/finalizar - Alquiler inexistente retorna 404")
    void finalizarAlquiler_noExiste() throws Exception {
        mockMvc.perform(put("/api/alquileres/999/finalizar"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("PUT /api/alquileres/{id}/finalizar - Alquiler ya finalizado retorna 409")
    void finalizarAlquiler_yaFinalizado() throws Exception {
        Alquiler alquiler = new Alquiler();
        alquiler.setBicicleta(bicicletaDisponible);
        alquiler.setNombreCliente("Test");
        alquiler.setHoraInicio(LocalDateTime.now().minusHours(2));
        alquiler.setDuracionEstimadaHoras(2);
        alquiler.setActivo(false); // Ya finalizado
        Alquiler guardado = alquilerRepository.save(alquiler);

        mockMvc.perform(put("/api/alquileres/" + guardado.getId() + "/finalizar"))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("Flujo completo: iniciar → finalizar → historial")
    void flujoCompleto() throws Exception {
        // 1. Iniciar alquiler
        String json = """
                {"codigoBicicleta":"BIC-001","nombreCliente":"Carlos Ruiz","duracionEstimadaHoras":1}
                """;
        String response = mockMvc.perform(post("/api/alquileres").contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        Long alquilerId = objectMapper.readTree(response).get("id").asLong();

        // 2. Verificar bicicleta no disponible
        mockMvc.perform(get("/api/bicicletas/disponibles"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.codigo=='BIC-001')]").doesNotExist());

        // 3. Finalizar alquiler
        mockMvc.perform(put("/api/alquileres/" + alquilerId + "/finalizar"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.activo").value(false))
                .andExpect(jsonPath("$.costoTotal").isNotEmpty());

        // 4. Verificar historial
        mockMvc.perform(get("/api/bicicletas/BIC-001/historial"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].nombreCliente").value("Carlos Ruiz"));
    }
}
