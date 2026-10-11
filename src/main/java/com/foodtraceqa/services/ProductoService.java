package com.foodtraceqa.services;

import com.foodtraceqa.dto.CatalogoDTO.ProductoRequest;
import com.foodtraceqa.entities.Producto;
import com.foodtraceqa.repositories.ProductoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductoService {
    private final ProductoRepository repository;

    public List<Producto> findAll() {
        return repository.findByActivoTrue();
    }

    public Producto findById(Long id) {
        return repository.findById(id).filter(Producto::isActivo)
                .orElseThrow(() -> new IllegalArgumentException("Producto no encontrado"));
    }

    @Transactional
    public Producto create(ProductoRequest r) {
        Producto p = new Producto();
        apply(p, r);
        return repository.save(p);
    }

    @Transactional
    public Producto update(Long id, ProductoRequest r) {
        Producto p = findById(id);
        apply(p, r);
        return repository.save(p);
    }

    @Transactional
    public void delete(Long id) {
        Producto p = findById(id);
        p.setActivo(false);
        repository.save(p);
    }

    private void apply(Producto p, ProductoRequest r) {
        p.setNombre(r.nombre());
        p.setDescripcion(r.descripcion());
        p.setActivo(true);
    }
}
