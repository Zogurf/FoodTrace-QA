package com.foodtraceqa;

import com.foodtraceqa.config.seed.*;
import com.foodtraceqa.entities.*;
import com.foodtraceqa.enums.*;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import java.time.LocalDate;
import java.util.*;
import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
class DataSeedTests {
    @Autowired EntityManager em;
    @Autowired PasswordEncoder encoder;
    @Autowired PlatformTransactionManager transactions;

    private SeedProperties config(String prefix) {
        SeedProperties p = new SeedProperties();
        p.setPrefix(prefix); p.setPassword("SeedTest123!");
        p.setReferenceDate(LocalDate.of(2026, 1, 1));
        p.setUsuarios(3); p.setProveedores(2); p.setMaterias(3); p.setProductos(2);
        p.setEntradas(12); p.setProducciones(4); p.setConsumos(8); p.setControles(15);
        return p;
    }

    private long count(String entity) {
        return em.createQuery("select count(e) from " + entity + " e", Long.class).getSingleResult();
    }

    @Test
    void cargaCoherenteConConteosExactosYReejecucionSinDuplicados() {
        SeedProperties p = config("testseed");
        TransactionTemplate tx = new TransactionTemplate(transactions);
        String[] entities = { "Usuario", "Proveedor", "Materia", "Producto", "LoteEntrada", "LoteProduccion", "LoteConsumo", "ControlCalidad" };
        long[] expected = { 3, 2, 3, 2, 12, 4, 8, 15 };
        long[] before = tx.execute(status -> Arrays.stream(entities).mapToLong(this::count).toArray());
        Map<String, Long> summary = tx.execute(status -> new DataSeedService(em, encoder, p).generate());
        assertThat(summary.get("total")).isEqualTo(49L);
        tx.executeWithoutResult(status -> {
            for (int i = 0; i < entities.length; i++) assertThat(count(entities[i]) - before[i]).isEqualTo(expected[i]);
            Usuario admin = em.createQuery("select u from Usuario u where u.username = 'testseed_admin'", Usuario.class).getSingleResult();
            assertThat(admin.getRol()).isEqualTo(Rol.ADMIN);
            assertThat(encoder.matches(p.getPassword(), admin.getPassword())).isTrue();
            List<LoteEntrada> inputs = em.createQuery("select e from LoteEntrada e where e.enviadoPor.nombre like 'testseed %'", LoteEntrada.class).getResultList();
            for (LoteEntrada input : inputs) {
                Double consumed = em.createQuery("select sum(c.cantidadConsumida) from LoteConsumo c where c.loteEntrante.id = :id", Double.class)
                        .setParameter("id", input.getId()).getSingleResult();
                assertThat(input.getCantidadDisponible()).isEqualTo(input.getCantidad() - (consumed == null ? 0 : consumed));
                assertThat(input.getCantidadDisponible()).isGreaterThanOrEqualTo(0);
            }
            List<LoteProduccion> outputs = em.createQuery("select p from LoteProduccion p where p.codigo like 'testseed-%'", LoteProduccion.class).getResultList();
            for (LoteProduccion output : outputs) {
                List<LoteConsumo> links = em.createQuery("select c from LoteConsumo c where c.loteProducido.id = :id", LoteConsumo.class)
                        .setParameter("id", output.getId()).getResultList();
                assertThat(links).hasSize(2);
                assertThat(links.stream().map(c -> c.getLoteEntrante().getId()).distinct().count()).isEqualTo(2);
                for (LoteConsumo link : links) {
                    assertThat(link.getLoteEntrante().getEstado()).isEqualTo(Estado.LIBERADO);
                    assertThat(link.getLoteEntrante().getFechaLiberacion()).isBefore(output.getFechaProduccion());
                    assertThat(link.getLoteEntrante().getFechaVencimiento()).isAfter(output.getFechaProduccion());
                }
                if (output.getEstado() == Estado.LIBERADO) {
                    assertThat(output.getLiberadoPor().getRol()).isEqualTo(Rol.SUPERVISOR);
                    assertThat(output.getFechaLiberacion()).isAfter(output.getFechaProduccion());
                    assertThat(output.getEvidencia()).isNotBlank();
                } else {
                    assertThat(output.getLiberadoPor()).isNull();
                    assertThat(output.getFechaLiberacion()).isNull();
                }
            }
            List<ControlCalidad> controls = em.createQuery("select c from ControlCalidad c where c.evidencia like 'testseed/%'", ControlCalidad.class).getResultList();
            assertThat(controls).hasSize(15);
            for (ControlCalidad c : controls) {
                assertThat((c.getLoteEntrada() == null) != (c.getLoteProduccion() == null)).isTrue();
                assertThat(c.getResponsable().getRol()).isEqualTo(Rol.SUPERVISOR);
            }
        });
        assertThatThrownBy(() -> tx.execute(status -> new DataSeedService(em, encoder, p).generate()))
                .isInstanceOf(IllegalStateException.class).hasMessageContaining("Ya existe");
        tx.executeWithoutResult(status -> {
            for (int i = 0; i < entities.length; i++) assertThat(count(entities[i]) - before[i]).isEqualTo(expected[i]);
        });
    }

    @Test
    void errorDuranteLaCargaRevierteLasInsercionesAnteriores() {
        SeedProperties p = config("rollbackseed");
        PasswordEncoder failing = new PasswordEncoder() {
            private int calls;
            public String encode(CharSequence raw) {
                if (++calls == 2) throw new IllegalStateException("Fallo simulado");
                return encoder.encode(raw);
            }
            public boolean matches(CharSequence raw, String encoded) { return false; }
        };
        TransactionTemplate tx = new TransactionTemplate(transactions);
        long before = tx.execute(status -> count("Usuario"));
        assertThatThrownBy(() -> tx.execute(status -> new DataSeedService(em, failing, p).generate()))
                .isInstanceOf(IllegalStateException.class).hasMessageContaining("Fallo simulado");
        long after = tx.execute(status -> count("Usuario"));
        assertThat(after).isEqualTo(before);
    }

    @Test
    void validaConfiguracionAntesDeInsertar() {
        SeedProperties defaults = new SeedProperties();
        defaults.setPassword("SeedTest123!");
        assertThat(defaults.total()).isEqualTo(25000L);
        defaults.validate();
        defaults.setConsumos(1);
        assertThatThrownBy(defaults::validate).isInstanceOf(IllegalArgumentException.class);
        SeedProperties invalid = config("invalidseed");
        invalid.setControles(1);
        assertThatThrownBy(invalid::validate).isInstanceOf(IllegalArgumentException.class);
        invalid = config("bad prefix");
        assertThatThrownBy(invalid::validate).isInstanceOf(IllegalArgumentException.class);
    }
}
