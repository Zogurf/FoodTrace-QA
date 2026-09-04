package com.foodtraceqa.repositories;

import com.foodtraceqa.entities.LoteProduccion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.List;

public interface LoteProduccionRepository extends JpaRepository<LoteProduccion, Long> {
    Optional<LoteProduccion> findByCodigoAndActivoTrue(String codigo);
    List<LoteProduccion> findByProductoIdAndActivoTrue(Long productoId);
}
