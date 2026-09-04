package com.foodtraceqa.repositories;

import com.foodtraceqa.entities.LoteEntrada;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface LoteEntradaRepository extends JpaRepository<LoteEntrada, Long> {
    List<LoteEntrada> findByActivoTrue();
}
