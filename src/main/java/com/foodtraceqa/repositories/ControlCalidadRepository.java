package com.foodtraceqa.repositories;

import com.foodtraceqa.entities.ControlCalidad;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ControlCalidadRepository extends JpaRepository<ControlCalidad, Long> {
    List<ControlCalidad> findByLoteProduccionIdOrderByFechaDesc(Long loteProduccionId);
}
