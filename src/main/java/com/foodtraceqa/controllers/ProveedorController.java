package com.foodtraceqa.controllers;

import com.foodtraceqa.dto.CatalogoDTO.ProveedorRequest;
import com.foodtraceqa.entities.Proveedor;
import com.foodtraceqa.services.ProveedorService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/catalogos/proveedores")
@RequiredArgsConstructor
public class ProveedorController {
    private final ProveedorService service;
    @GetMapping public List<Proveedor> findAll() { return service.findAll(); }
    @GetMapping("/{id}") public Proveedor findById(@PathVariable Long id) { return service.findById(id); }
    @PostMapping @ResponseStatus(HttpStatus.CREATED) public Proveedor create(@Valid @RequestBody ProveedorRequest r) { return service.create(r); }
    @PutMapping("/{id}") public Proveedor update(@PathVariable Long id, @Valid @RequestBody ProveedorRequest r) { return service.update(id, r); }
    @DeleteMapping("/{id}") @ResponseStatus(HttpStatus.NO_CONTENT) public void delete(@PathVariable Long id) { service.delete(id); }
}
