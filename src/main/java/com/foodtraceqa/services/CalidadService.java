package com.foodtraceqa.services;

import com.foodtraceqa.dto.TrazabilidadDTO.*;
import com.foodtraceqa.entities.*;
import com.foodtraceqa.enums.Dictamen;
import com.foodtraceqa.enums.Estado;
import com.foodtraceqa.repositories.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.Date;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CalidadService {
    private final ControlCalidadRepository qualityRepository;
    private final LoteProduccionRepository productionRepository;
    private final LoteEntradaRepository entradaRepository;
    private final UsuarioRepository userRepository;

    @Transactional
    public ControlRespuesta create(SolicitudCalidad request, String username) {
        ControlCalidad control = preparar(request.dictamen(), request.evidencia(), request.observacion(), username);
        LoteProduccion lote = productionRepository.findByIdForUpdate(request.loteProduccionId())
                .filter(LoteProduccion::isActivo)
                .orElseThrow(() -> new IllegalArgumentException("Lote de producción no encontrado"));
        control.setLoteProduccion(lote);
        boolean aprobado = request.dictamen() == Dictamen.APROBADO;
        lote.setEstado(aprobado ? Estado.LIBERADO : Estado.RECHAZADO);
        lote.setFechaLiberacion(aprobado ? control.getFecha() : null);
        lote.setLiberadoPor(aprobado ? control.getResponsable() : null);
        lote.setEvidencia(aprobado ? control.getEvidencia() : null);
        productionRepository.save(lote);
        return respuesta(qualityRepository.save(control));
    }

    @Transactional
    public ControlRespuesta createEntrada(SolicitudCalidadEntrada request, String username) {
        ControlCalidad control = preparar(request.dictamen(), request.evidencia(), request.observacion(), username);
        LoteEntrada lote = entradaRepository.findByIdForUpdate(request.loteEntradaId())
                .filter(LoteEntrada::isActivo)
                .orElseThrow(() -> new IllegalArgumentException("Lote de entrada no encontrado"));
        boolean aprobado = request.dictamen() == Dictamen.APROBADO;
        if (aprobado && (lote.getFechaVencimiento() == null || !lote.getFechaVencimiento().after(control.getFecha())))
            throw new IllegalArgumentException("No se puede liberar un lote de entrada vencido");
        control.setLoteEntrada(lote);
        lote.setEstado(aprobado ? Estado.LIBERADO : Estado.RECHAZADO);
        lote.setFechaLiberacion(aprobado ? control.getFecha() : null);
        lote.setLiberadoPor(aprobado ? control.getResponsable() : null);
        lote.setEvidencia(aprobado ? control.getEvidencia() : null);
        entradaRepository.save(lote);
        return respuesta(qualityRepository.save(control));
    }

    private ControlCalidad preparar(Dictamen dictamen, String evidencia, String observacion, String username) {
        if (dictamen == null) throw new IllegalArgumentException("El dictamen es obligatorio");
        if (observacion == null || observacion.isBlank()) throw new IllegalArgumentException("La observación es obligatoria");
        if (dictamen == Dictamen.APROBADO && (evidencia == null || evidencia.isBlank()))
            throw new IllegalArgumentException("La evidencia es obligatoria para liberar un lote");
        ControlCalidad control = new ControlCalidad();
        control.setResponsable(userRepository.findByUsernameAndActivoTrue(username)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado")));
        control.setEvidencia(evidencia == null ? null : evidencia.trim());
        control.setObservacion(observacion.trim());
        control.setFecha(new Date());
        control.setDictamen(dictamen);
        return control;
    }

    @Transactional(readOnly = true)
    public List<ControlRespuesta> findByProduction(Long id) {
        return qualityRepository.findByLoteProduccionIdOrderByFechaDesc(id).stream().map(this::respuesta).toList();
    }

    @Transactional(readOnly = true)
    public List<ControlRespuesta> findByEntrada(Long id) {
        return qualityRepository.findByLoteEntradaIdOrderByFechaDesc(id).stream().map(this::respuesta).toList();
    }

    private ControlRespuesta respuesta(ControlCalidad c) {
        return new ControlRespuesta(c.getId(), c.getLoteProduccion() == null ? null : c.getLoteProduccion().getId(),
                c.getLoteEntrada() == null ? null : c.getLoteEntrada().getId(), c.getResponsable().getUsername(),
                c.getEvidencia(), c.getObservacion(), c.getFecha(), c.getDictamen());
    }
}
