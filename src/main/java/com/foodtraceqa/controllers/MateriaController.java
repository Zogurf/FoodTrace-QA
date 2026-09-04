package com.foodtraceqa.controllers;

import com.foodtraceqa.dto.CatalogoDTO.MateriaRequest;
import com.foodtraceqa.entities.Materia;
import com.foodtraceqa.services.MateriaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/catalogos/materias-primas")
@RequiredArgsConstructor
public class MateriaController {
    private final MateriaService service;
    @GetMapping public List<Materia> findAll() { return service.findAll(); }
    @GetMapping("/{id}") public Materia findById(@PathVariable Long id) { return service.findById(id); }
    @PostMapping @ResponseStatus(HttpStatus.CREATED) public Materia create(@Valid @RequestBody MateriaRequest r) { return service.create(r); }
    @PutMapping("/{id}") public Materia update(@PathVariable Long id, @Valid @RequestBody MateriaRequest r) { return service.update(id, r); }
    @DeleteMapping("/{id}") @ResponseStatus(HttpStatus.NO_CONTENT) public void delete(@PathVariable Long id) { service.delete(id); }
}
