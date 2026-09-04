package com.foodtraceqa.services;

import com.foodtraceqa.dto.TrazabilidadDTO.SolicitudCalidad;
import com.foodtraceqa.entities.ControlCalidad;
import com.foodtraceqa.entities.LoteProduccion;
import com.foodtraceqa.entities.Usuario;
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
    private final UsuarioRepository userRepository;

    @Transactional
    public ControlCalidad create(SolicitudCalidad request, String username) {
        Usuario supervisor = userRepository.findByUsernameAndActivoTrue(username)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));
        LoteProduccion lote = productionRepository.findById(request.loteProduccionId())
                .filter(LoteProduccion::isActivo)
                .orElseThrow(() -> new IllegalArgumentException("Lote de producción no encontrado"));
        ControlCalidad control = new ControlCalidad();
        control.setLoteProduccion(lote);
        control.setResponsable(supervisor);
        control.setEvidencia(request.evidencia());
        control.setObservacion(request.observacion());
        control.setFecha(new Date());
        control.setDictamen(request.dictamen());
        lote.setEstado(request.dictamen() == Dictamen.APROBADO ? Estado.LIBERADO : Estado.RECHAZADO);
        if (request.dictamen() == Dictamen.APROBADO) {
            lote.setFechaLiberacion(new Date());
            lote.setLiberadoPor(supervisor);
        }
        productionRepository.save(lote);
        return qualityRepository.save(control);
    }

    @Transactional(readOnly = true)
    public List<ControlCalidad> findByProduction(Long productionId) {
        return qualityRepository.findByLoteProduccionIdOrderByFechaDesc(productionId);
    }
}
