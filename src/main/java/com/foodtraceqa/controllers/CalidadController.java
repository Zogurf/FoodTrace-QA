package com.foodtraceqa.controllers;

import com.foodtraceqa.dto.TrazabilidadDTO.*;
import com.foodtraceqa.services.CalidadService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/calidad")
@RequiredArgsConstructor
public class CalidadController {
    private final CalidadService service;

    @PostMapping("/controles")
    @ResponseStatus(HttpStatus.CREATED)
    @org.springframework.security.access.prepost.PreAuthorize("hasRole('SUPERVISOR')")
    public ControlRespuesta create(@Valid @RequestBody SolicitudCalidad request, Authentication authentication) {
        return service.create(request, authentication.getName());
    }

    @PostMapping("/controles/entrada")
    @ResponseStatus(HttpStatus.CREATED)
    @org.springframework.security.access.prepost.PreAuthorize("hasRole('SUPERVISOR')")
    public ControlRespuesta createEntrada(@Valid @RequestBody SolicitudCalidadEntrada request, Authentication authentication) {
        return service.createEntrada(request, authentication.getName());
    }

    @GetMapping("/controles/entrada/{id}")
    public List<ControlRespuesta> findByEntrada(@PathVariable Long id) {
        return service.findByEntrada(id);
    }

    @GetMapping("/controles/lote/{loteProduccionId}")
    public List<ControlRespuesta> findByProduction(@PathVariable Long loteProduccionId) {
        return service.findByProduction(loteProduccionId);
    }
}
