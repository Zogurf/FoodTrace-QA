package com.foodtraceqa.repositories;

import com.foodtraceqa.entities.LoteProduccion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import jakarta.persistence.LockModeType;
import java.util.Optional;

import java.util.List;

public interface LoteProduccionRepository extends JpaRepository<LoteProduccion, Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select l from LoteProduccion l where l.id = :id")
    Optional<LoteProduccion> findByIdForUpdate(@Param("id") Long id);

    Optional<LoteProduccion> findByCodigoAndActivoTrue(String codigo);
    List<LoteProduccion> findByProductoIdAndActivoTrue(Long productoId);
}
