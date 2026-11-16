package com.foodtraceqa.repositories;

import com.foodtraceqa.entities.LoteEntrada;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import jakarta.persistence.LockModeType;
import java.util.Optional;
import java.util.List;

public interface LoteEntradaRepository extends JpaRepository<LoteEntrada, Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select l from LoteEntrada l where l.id = :id")
    Optional<LoteEntrada> findByIdForUpdate(@Param("id") Long id);

    List<LoteEntrada> findByActivoTrue();
}
