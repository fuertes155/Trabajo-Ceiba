package com.ceiba.alquiler.config;

import com.ceiba.alquiler.model.Bicicleta;
import com.ceiba.alquiler.model.enums.EstadoBicicleta;
import com.ceiba.alquiler.model.enums.TipoBicicleta;
import com.ceiba.alquiler.repository.BicicletaRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/**
 * Componente que carga datos iniciales de bicicletas al arrancar la aplicación.
 * Solo se ejecuta cuando NO está activo el perfil "test" para no interferir con las pruebas.
 */
@Component
@Profile("!test")
public class DataLoader implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataLoader.class);

    private final BicicletaRepository bicicletaRepository;

    public DataLoader(BicicletaRepository bicicletaRepository) {
        this.bicicletaRepository = bicicletaRepository;
    }

    @Override
    public void run(String... args) {
        if (bicicletaRepository.count() > 0) {
            log.info("Los datos iniciales ya existen. Omitiendo carga.");
            return;
        }

        log.info("Cargando datos iniciales de bicicletas...");

        bicicletaRepository.save(new Bicicleta("BIC-001", TipoBicicleta.URBANA, EstadoBicicleta.DISPONIBLE));
        bicicletaRepository.save(new Bicicleta("BIC-002", TipoBicicleta.MONTANA, EstadoBicicleta.DISPONIBLE));
        bicicletaRepository.save(new Bicicleta("BIC-003", TipoBicicleta.ELECTRICA, EstadoBicicleta.DISPONIBLE));
        bicicletaRepository.save(new Bicicleta("BIC-004", TipoBicicleta.MONTANA, EstadoBicicleta.EN_MANTENIMIENTO));
        bicicletaRepository.save(new Bicicleta("BIC-005", TipoBicicleta.URBANA, EstadoBicicleta.DISPONIBLE));

        log.info("✅ Se cargaron {} bicicletas iniciales.", bicicletaRepository.count());
    }
}
