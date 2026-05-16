package com.ceiba.alquiler.repository;

import com.ceiba.alquiler.model.Alquiler;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repositorio JPA para la entidad Alquiler.
 */
@Repository
public interface AlquilerRepository extends JpaRepository<Alquiler, Long> {

    List<Alquiler> findByBicicletaCodigoOrderByHoraInicioDesc(String codigo);
}
