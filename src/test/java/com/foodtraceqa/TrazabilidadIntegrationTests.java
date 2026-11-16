package com.foodtraceqa;

import com.foodtraceqa.dto.TrazabilidadDTO.SolicitudConsumo;
import com.foodtraceqa.dto.TrazabilidadDTO.SolicitudProduccion;
import com.foodtraceqa.dto.TrazabilidadDTO.SolicitudRecepcion;
import com.foodtraceqa.entities.LoteEntrada;
import com.foodtraceqa.entities.LoteProduccion;
import com.foodtraceqa.entities.Materia;
import com.foodtraceqa.entities.Producto;
import com.foodtraceqa.entities.Proveedor;
import com.foodtraceqa.enums.Estado;
import com.foodtraceqa.enums.Medida;
import com.foodtraceqa.repositories.MateriaRepository;
import com.foodtraceqa.repositories.LoteEntradaRepository;
import com.foodtraceqa.repositories.ProductoRepository;
import com.foodtraceqa.repositories.ProveedorRepository;
import com.foodtraceqa.services.TrazabilidadService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class TrazabilidadIntegrationTests {

    @Autowired
    private TrazabilidadService trazabilidadService;

    @Autowired
    private ProveedorRepository proveedorRepository;

    @Autowired
    private MateriaRepository materiaRepository;

        @Autowired
        private LoteEntradaRepository loteEntradaRepository;

    @Autowired
    private ProductoRepository productoRepository;

    private Proveedor proveedor;
    private Materia materia;
    private Producto producto;

    @BeforeEach
    void prepararDatos() {
        proveedor = new Proveedor();
        proveedor.setRuc(90000001L);
        proveedor.setNombre("Proveedor de prueba");
        proveedor.setCalificacion(5);
        proveedor.setActivo(true);
        proveedor = proveedorRepository.save(proveedor);

        materia = new Materia();
        materia.setNombre("Harina de prueba");
        materia.setDescripcion("Materia para pruebas de integración");
        materia.setUnidadMedida(Medida.KG);
        materia.setActivo(true);
        materia = materiaRepository.save(materia);

        producto = new Producto();
        producto.setNombre("Producto de prueba");
        producto.setDescripcion("Producto para pruebas de integración");
        producto.setActivo(true);
        producto = productoRepository.save(producto);
    }

    @Test
    void deberiaRegistrarRecepcionYDescontarMateriaAlProducir() {
        Date vencimiento = new Date(System.currentTimeMillis() + 86_400_000L);
        LoteEntrada entrada = trazabilidadService.recibir(
                new SolicitudRecepcion(proveedor.getId(), materia.getId(), vencimiento, 10));

        entrada.setEstado(Estado.LIBERADO);
        loteEntradaRepository.save(entrada);

        LoteProduccion produccion = trazabilidadService.producir(
                new SolicitudProduccion(
                        producto.getId(),
                        "LOTE-001",
                        8,
                        new Date(),
                        List.of(new SolicitudConsumo(entrada.getId(), 4))));

        LoteEntrada entradaActualizada = loteEntradaRepository.findById(entrada.getId()).orElseThrow();

        assertThat(produccion.getEstado()).isEqualTo(Estado.CUARENTENA);
        assertThat(entradaActualizada.getCantidadDisponible()).isEqualTo(6);
        assertThat(entradaActualizada.isCompletado()).isFalse();
    }

    @Test
    void deberiaRechazarCantidadInsuficiente() {
        Date vencimiento = new Date(System.currentTimeMillis() + 86_400_000L);
        LoteEntrada entrada = trazabilidadService.recibir(
                new SolicitudRecepcion(proveedor.getId(), materia.getId(), vencimiento, 2));

        entrada.setEstado(Estado.LIBERADO);
        loteEntradaRepository.save(entrada);

        assertThatThrownBy(() -> trazabilidadService.producir(
                new SolicitudProduccion(
                        producto.getId(),
                        "LOTE-002",
                        8,
                        new Date(),
                        List.of(new SolicitudConsumo(entrada.getId(), 3)))))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Cantidad disponible insuficiente");
    }
    @Test
    void deberiaRechazarInsumosEnCuarentenaRechazadosOVencidos() {
        LoteEntrada entrada = trazabilidadService.recibir(new SolicitudRecepcion(
                proveedor.getId(), materia.getId(), new Date(System.currentTimeMillis() + 86_400_000L), 10));
        for (Estado estado : List.of(Estado.CUARENTENA, Estado.RECHAZADO, Estado.LIBERADO)) {
            entrada.setEstado(estado);
            if (estado == Estado.LIBERADO) entrada.setFechaVencimiento(new Date(0));
            loteEntradaRepository.save(entrada);
            assertThatThrownBy(() -> trazabilidadService.producir(new SolicitudProduccion(
                    producto.getId(), "INVALIDO-" + estado, 1, new Date(),
                    List.of(new SolicitudConsumo(entrada.getId(), 1)))))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining(estado == Estado.LIBERADO ? "vencido" : "LIBERADOS");
            assertThat(entrada.getCantidadDisponible()).isEqualTo(10);
        }
    }

}
