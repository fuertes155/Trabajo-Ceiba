package com.ceiba.alquiler.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuración de OpenAPI/Swagger para la documentación interactiva de la API.
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("API Alquiler de Bicicletas Urbanas")
                        .version("1.0.0")
                        .description("API REST para gestionar el alquiler de bicicletas en una empresa de turismo urbano. "
                                + "Permite registrar bicicletas, iniciar/finalizar alquileres, consultar disponibilidad "
                                + "e historial, y calcular costos con multas por devolución tardía.")
                        .contact(new Contact()
                                .name("Ceiba Software")
                                .email("desarrollo@ceiba.com"))
                );
    }
}
