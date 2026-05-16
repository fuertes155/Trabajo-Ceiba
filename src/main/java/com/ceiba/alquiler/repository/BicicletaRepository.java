package com.ceiba.alquiler.repository;

import com.ceiba.alquiler.model.Bicicleta;
import com.ceiba.alquiler.model.enums.EstadoBicicleta;
import com.ceiba.alquiler.model.enums.TipoBicicleta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio JPA para la entidad Bicicleta.
 */
@Repository
public interface BicicletaRepository extends JpaRepository<Bicicleta, Long> {

    Optional<Bicicleta> findByCodigo(String codigo);

    List<Bicicleta> findByEstado(EstadoBicicleta estado);

    List<Bicicleta> findByEstadoAndTipo(EstadoBicicleta estado, TipoBicicleta tipo);

    boolean existsByCodigo(String codigo);
}
