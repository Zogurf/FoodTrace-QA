package com.foodtraceqa.controllers;

import com.foodtraceqa.dto.TrazabilidadDTO.*;
import com.foodtraceqa.entities.LoteEntrada;
import com.foodtraceqa.entities.LoteProduccion;
import com.foodtraceqa.services.TrazabilidadService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/trazabilidad")
@RequiredArgsConstructor
public class TrazabilidadController {
    private final TrazabilidadService service;

    @PostMapping("/recepciones")
    @ResponseStatus(HttpStatus.CREATED)
    public LoteEntrada recibir(@Valid @RequestBody SolicitudRecepcion solicitud) {
        return service.recibir(solicitud);
    }

    @PostMapping("/producciones")
    @ResponseStatus(HttpStatus.CREATED)
    public LoteProduccion producir(@Valid @RequestBody SolicitudProduccion solicitud) {
        return service.producir(solicitud);
    }

    @GetMapping("/hacia-atras/{loteProduccionId}")
    public TrazabilidadHaciaAtras haciaAtras(@PathVariable Long loteProduccionId) {
        return service.haciaAtras(loteProduccionId);
    }

    @GetMapping("/hacia-adelante/{loteEntradaId}")
    public TrazabilidadHaciaAdelante haciaAdelante(@PathVariable Long loteEntradaId) {
        return service.haciaAdelante(loteEntradaId);
    }
}
