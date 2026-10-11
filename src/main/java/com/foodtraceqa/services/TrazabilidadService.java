package com.foodtraceqa.services;

import com.foodtraceqa.dto.TrazabilidadDTO.*;
import com.foodtraceqa.entities.*;
import com.foodtraceqa.enums.Estado;
import com.foodtraceqa.repositories.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TrazabilidadService {
    private final LoteEntradaRepository entradaRepository;
    private final LoteProduccionRepository produccionRepository;
    private final LoteConsumoRepository consumoRepository;
    private final ProveedorRepository proveedorRepository;
    private final MateriaRepository materiaRepository;
    private final ProductoRepository productoRepository;

    @Transactional
    public LoteEntrada recibir(SolicitudRecepcion solicitud) {
        if (solicitud.fechaVencimiento().before(new Date())) {
            throw new IllegalArgumentException("La fecha de vencimiento debe ser futura");
        }
        Proveedor proveedor = proveedorRepository.findById(solicitud.proveedorId())
                .filter(Proveedor::isActivo).orElseThrow(() -> new IllegalArgumentException("Proveedor no encontrado"));
        Materia materia = materiaRepository.findById(solicitud.materiaPrimaId())
                .filter(Materia::isActivo)
                .orElseThrow(() -> new IllegalArgumentException("Materia prima no encontrada"));
        LoteEntrada lote = new LoteEntrada();
        lote.setEnviadoPor(proveedor);
        lote.setMateriaPrima(materia);
        lote.setFechaRecepcion(new Date());
        lote.setFechaVencimiento(solicitud.fechaVencimiento());
        lote.setCantidad(solicitud.cantidad());
        lote.setCantidadDisponible(solicitud.cantidad());
        lote.setCompletado(false);
        lote.setActivo(true);
        lote.setEstado(Estado.CUARENTENA);
        return entradaRepository.save(lote);
    }

    @Transactional
    public LoteProduccion producir(SolicitudProduccion solicitud) {
        if (solicitud.fechaProduccion().after(new Date())) {
            throw new IllegalArgumentException("La fecha de producción no puede ser futura");
        }
        if (solicitud.consumos().stream().map(SolicitudConsumo::loteEntradaId).distinct().count() != solicitud
                .consumos().size()) {
            throw new IllegalArgumentException("No se puede repetir un lote de entrada en los consumos");
        }
        Producto producto = productoRepository.findById(solicitud.productoId())
                .filter(Producto::isActivo).orElseThrow(() -> new IllegalArgumentException("Producto no encontrado"));
        if (produccionRepository.findByCodigoAndActivoTrue(solicitud.codigo()).isPresent())
            throw new IllegalArgumentException("El código del lote ya está registrado");
        LoteProduccion lote = new LoteProduccion();
        lote.setProducto(producto);
        lote.setCodigo(solicitud.codigo());
        lote.setCantidad(solicitud.cantidad());
        lote.setFechaProduccion(solicitud.fechaProduccion());
        lote.setEstado(Estado.CUARENTENA);
        lote.setActivo(true);
        LoteProduccion guardado = produccionRepository.save(lote);
        for (SolicitudConsumo consumo : solicitud.consumos()) {
            LoteEntrada entrada = entradaRepository.findById(consumo.loteEntradaId())
                    .filter(LoteEntrada::isActivo)
                    .orElseThrow(() -> new IllegalArgumentException("Lote de entrada no encontrado"));
            if (entrada.getCantidadDisponible() < consumo.cantidadConsumida())
                throw new IllegalArgumentException("Cantidad disponible insuficiente en lote " + entrada.getId());
            entrada.setCantidadDisponible(entrada.getCantidadDisponible() - consumo.cantidadConsumida());
            entrada.setCompletado(entrada.getCantidadDisponible() == 0);
            entradaRepository.save(entrada);
            LoteConsumo enlace = new LoteConsumo();
            enlace.setLoteProducido(guardado);
            enlace.setLoteEntrante(entrada);
            enlace.setCantidadConsumida(consumo.cantidadConsumida());
            consumoRepository.save(enlace);
        }
        return guardado;
    }

    @Transactional(readOnly = true)
    public TrazabilidadHaciaAtras haciaAtras(Long loteProduccionId) {
        LoteProduccion produccion = buscarProduccionActiva(loteProduccionId);
        List<TrazabilidadEntrada> entradas = consumoRepository.findByLoteProducidoId(loteProduccionId).stream()
                .map(c -> new TrazabilidadEntrada(c.getLoteEntrante().getId(),
                        c.getLoteEntrante().getMateriaPrima().getNombre(),
                        c.getLoteEntrante().getEnviadoPor().getNombre(),
                        c.getLoteEntrante().getFechaRecepcion(), c.getLoteEntrante().getFechaVencimiento(),
                        c.getCantidadConsumida()))
                .toList();
        return new TrazabilidadHaciaAtras(produccion.getId(), produccion.getCodigo(),
                produccion.getProducto().getNombre(), produccion.getEstado(), entradas);
    }

    @Transactional(readOnly = true)
    public TrazabilidadHaciaAdelante haciaAdelante(Long loteEntradaId) {
        LoteEntrada entrada = entradaRepository.findById(loteEntradaId).filter(LoteEntrada::isActivo)
                .orElseThrow(() -> new IllegalArgumentException("Lote de entrada no encontrado"));
        List<TrazabilidadSalida> salidas = consumoRepository.findByLoteEntranteId(loteEntradaId).stream()
                .map(c -> new TrazabilidadSalida(c.getLoteProducido().getId(), c.getLoteProducido().getCodigo(),
                        c.getLoteProducido().getProducto().getNombre(), c.getLoteProducido().getCantidad(),
                        c.getLoteProducido().getEstado(), c.getCantidadConsumida()))
                .toList();
        return new TrazabilidadHaciaAdelante(entrada.getId(), entrada.getMateriaPrima().getNombre(),
                entrada.getEnviadoPor().getNombre(), salidas);
    }

    private LoteProduccion buscarProduccionActiva(Long id) {
        return produccionRepository.findById(id).filter(LoteProduccion::isActivo)
                .orElseThrow(() -> new IllegalArgumentException("Lote de producción no encontrado"));
    }
}
