package com.foodtraceqa.repositories;

import com.foodtraceqa.entities.Proveedor;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProveedorRepository extends JpaRepository<Proveedor, Long> {
    List<Proveedor> findByActivoTrue();
    boolean existsByRuc(Long ruc);
}
