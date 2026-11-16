package com.foodtraceqa;

import com.foodtraceqa.dto.TrazabilidadDTO.*;
import com.foodtraceqa.entities.*;
import com.foodtraceqa.enums.*;
import com.foodtraceqa.repositories.*;
import com.foodtraceqa.services.TrazabilidadService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import java.util.*;
import java.util.concurrent.*;
import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
class StockConcurrencyTests {
    @Autowired TrazabilidadService service;
    @Autowired ProveedorRepository proveedores;
    @Autowired MateriaRepository materias;
    @Autowired ProductoRepository productos;
    @Autowired LoteEntradaRepository entradas;
    @Autowired LoteProduccionRepository producciones;
    @Autowired LoteConsumoRepository consumos;

    // No test transaction: each service call commits/rolls back independently.
    @Test
    void dosProduccionesNoPuedenConsumirElMismoStock() throws Exception {
        String key = UUID.randomUUID().toString();
        Proveedor proveedor = new Proveedor();
        proveedor.setRuc(80000000000L + Math.floorMod(System.nanoTime(), 1000000000L));
        proveedor.setNombre("Concurrente " + key);
        proveedor.setActivo(true);
        proveedor = proveedores.save(proveedor);
        Materia materia = new Materia();
        materia.setNombre("Materia " + key);
        materia.setUnidadMedida(Medida.KG);
        materia.setActivo(true);
        materia = materias.save(materia);
        Producto producto = new Producto();
        producto.setNombre("Producto " + key);
        producto.setActivo(true);
        producto = productos.save(producto);
        LoteEntrada entrada = service.recibir(new SolicitudRecepcion(proveedor.getId(), materia.getId(),
                new Date(System.currentTimeMillis() + 86400000L), 10));
        entrada.setEstado(Estado.LIBERADO);
        entradas.save(entrada);
        Long entradaId = entrada.getId();
        Long productoId = producto.getId();
        CountDownLatch ready = new CountDownLatch(2);
        CountDownLatch start = new CountDownLatch(1);
        ExecutorService executor = Executors.newFixedThreadPool(2);
        try {
            List<Future<Boolean>> results = new ArrayList<>();
            for (int i = 0; i < 2; i++) {
                String code = key + "-" + i;
                results.add(executor.submit(() -> {
                    ready.countDown();
                    if (!start.await(10, TimeUnit.SECONDS)) throw new IllegalStateException("Sin inicio");
                    try {
                        service.producir(new SolicitudProduccion(productoId, code, 8, new Date(),
                                List.of(new SolicitudConsumo(entradaId, 8))));
                        return true;
                    } catch (IllegalArgumentException ex) {
                        assertThat(ex).hasMessageContaining("Cantidad disponible insuficiente");
                        return false;
                    }
                }));
            }
            assertThat(ready.await(10, TimeUnit.SECONDS)).isTrue();
            start.countDown();
            int successes = 0;
            for (Future<Boolean> result : results) if (result.get(20, TimeUnit.SECONDS)) successes++;
            assertThat(successes).isEqualTo(1);
            assertThat(entradas.findById(entradaId).orElseThrow().getCantidadDisponible()).isEqualTo(2);
            assertThat(consumos.findByLoteEntranteId(entradaId)).hasSize(1);
            assertThat(producciones.findByProductoIdAndActivoTrue(productoId)).hasSize(1);

            // A failure on a later input must roll back the earlier deduction too.
            LoteEntrada cuarentena = service.recibir(new SolicitudRecepcion(entrada.getEnviadoPor().getId(),
                    entrada.getMateriaPrima().getId(), new Date(System.currentTimeMillis() + 86400000L), 10));
            assertThatThrownBy(() -> service.producir(new SolicitudProduccion(productoId, key + "-rollback", 1,
                    new Date(), List.of(new SolicitudConsumo(entradaId, 1), new SolicitudConsumo(cuarentena.getId(), 1)))))
                    .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("LIBERADOS");
            assertThat(entradas.findById(entradaId).orElseThrow().getCantidadDisponible()).isEqualTo(2);
            assertThat(consumos.findByLoteEntranteId(entradaId)).hasSize(1);
            assertThat(producciones.findByProductoIdAndActivoTrue(productoId)).hasSize(1);
        } finally {
            start.countDown();
            executor.shutdownNow();
        }
    }
}
