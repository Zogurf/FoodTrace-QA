package com.foodtraceqa.services;

import com.foodtraceqa.dto.CatalogoDTO.MateriaRequest;
import com.foodtraceqa.entities.Materia;
import com.foodtraceqa.repositories.MateriaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MateriaService {
    private final MateriaRepository repository;

    public List<Materia> findAll() {
        return repository.findByActivoTrue();
    }

    public Materia findById(Long id) {
        return repository.findById(id).filter(Materia::isActivo)
                .orElseThrow(() -> new IllegalArgumentException("Materia prima no encontrada"));
    }

    @Transactional
    public Materia create(MateriaRequest r) {
        Materia m = new Materia();
        apply(m, r);
        return repository.save(m);
    }

    @Transactional
    public Materia update(Long id, MateriaRequest r) {
        Materia m = findById(id);
        apply(m, r);
        return repository.save(m);
    }

    @Transactional
    public void delete(Long id) {
        Materia m = findById(id);
        m.setActivo(false);
        repository.save(m);
    }

    private void apply(Materia m, MateriaRequest r) {
        m.setNombre(r.nombre());
        m.setDescripcion(r.descripcion());
        m.setUnidadMedida(r.unidadMedida());
        m.setActivo(true);
    }
}
