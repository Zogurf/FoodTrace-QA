package com.foodtraceqa.controllers;

import com.foodtraceqa.dto.CatalogoDTO.ProductoRequest;
import com.foodtraceqa.entities.Producto;
import com.foodtraceqa.services.ProductoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/catalogos/productos")
@RequiredArgsConstructor
public class ProductoController {
    private final ProductoService service;
    @GetMapping public List<Producto> findAll() { return service.findAll(); }
    @GetMapping("/{id}") public Producto findById(@PathVariable Long id) { return service.findById(id); }
    @PostMapping @ResponseStatus(HttpStatus.CREATED) public Producto create(@Valid @RequestBody ProductoRequest r) { return service.create(r); }
    @PutMapping("/{id}") public Producto update(@PathVariable Long id, @Valid @RequestBody ProductoRequest r) { return service.update(id, r); }
    @DeleteMapping("/{id}") @ResponseStatus(HttpStatus.NO_CONTENT) public void delete(@PathVariable Long id) { service.delete(id); }
}
