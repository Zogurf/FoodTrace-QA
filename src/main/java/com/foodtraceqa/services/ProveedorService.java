package com.foodtraceqa.services;

import com.foodtraceqa.dto.CatalogoDTO.ProveedorRequest;
import com.foodtraceqa.entities.Proveedor;
import com.foodtraceqa.repositories.ProveedorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProveedorService {
    private final ProveedorRepository repository;

    public List<Proveedor> findAll() { return repository.findByActivoTrue(); }
    public Proveedor findById(Long id) {
        return repository.findById(id).filter(Proveedor::isActivo)
                .orElseThrow(() -> new IllegalArgumentException("Proveedor no encontrado"));
    }
    @Transactional
    public Proveedor create(ProveedorRequest request) {
        if (repository.existsByRuc(request.ruc())) throw new IllegalArgumentException("El RUC ya está registrado");
        Proveedor p = new Proveedor();
        apply(p, request);
        return repository.save(p);
    }
    @Transactional
    public Proveedor update(Long id, ProveedorRequest request) {
        Proveedor p = findById(id);
        if (!p.getRuc().equals(request.ruc()) && repository.existsByRuc(request.ruc()))
            throw new IllegalArgumentException("El RUC ya está registrado");
        apply(p, request);
        return repository.save(p);
    }
    @Transactional
    public void delete(Long id) { Proveedor p = findById(id); p.setActivo(false); repository.save(p); }
    private void apply(Proveedor p, ProveedorRequest r) {
        p.setRuc(r.ruc()); p.setNombre(r.nombre()); p.setCalificacion(r.calificacion()); p.setActivo(true);
    }
}
