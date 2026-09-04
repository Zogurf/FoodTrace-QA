package com.foodtraceqa.repositories;

import com.foodtraceqa.entities.Materia;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface MateriaRepository extends JpaRepository<Materia, Long> {
    List<Materia> findByActivoTrue();
}
