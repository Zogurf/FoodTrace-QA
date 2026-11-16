package com.foodtraceqa.config.seed;

import com.foodtraceqa.entities.*;
import com.foodtraceqa.enums.*;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.*;
import java.util.*;

@Service
@Profile("seed")
@RequiredArgsConstructor
public class DataSeedService {
    private final EntityManager em;
    private final PasswordEncoder encoder;
    private final SeedProperties config;
    private static final ZoneId ZONE = ZoneId.of("America/Lima");

    @Transactional
    public Map<String, Long> generate() {
        config.validate();
        String prefix = config.getPrefix();
        String adminName = prefix + "_admin";
        if (em.createQuery("select count(u) from Usuario u where u.username = :name", Long.class)
                .setParameter("name", adminName).getSingleResult() != 0)
            throw new IllegalStateException("Ya existe la carga " + prefix + ". No se duplicaron ni modificaron registros.");
        Random random = new Random(config.getRandomSeed());
        List<Long> users = new ArrayList<>(), providers = new ArrayList<>(), materials = new ArrayList<>(), products = new ArrayList<>();
        List<Long> inputs = new ArrayList<>(), outputs = new ArrayList<>();
        // The unique username is also the load marker: concurrent identical loads cannot both commit.
        for (int i = 0; i < config.getUsuarios(); i++) {
            Usuario u = new Usuario();
            u.setNombres("Simulado " + (i + 1)); u.setApellidos("FoodTrace " + prefix);
            u.setUsername(i == 0 ? adminName : prefix + "_sup_" + i);
            u.setPassword(encoder.encode(config.getPassword()));
            u.setRol(i == 0 ? Rol.ADMIN : Rol.SUPERVISOR); u.setActivo(true);
            em.persist(u); users.add(u.getId()); batch(i);
        }
        em.flush(); em.clear();
        for (int i = 0; i < config.getProveedores(); i++) {
            Proveedor p = new Proveedor();
            long ruc = 20000000000L + random.nextLong(1000000000L);
            while (em.createQuery("select count(p) from Proveedor p where p.ruc = :ruc", Long.class)
                    .setParameter("ruc", ruc).getSingleResult() != 0) ruc++;
            p.setRuc(ruc); p.setNombre(prefix + " Proveedor " + (i + 1));
            p.setCalificacion(random.nextInt(6)); p.setActivo(true);
            em.persist(p); providers.add(p.getId()); batch(i);
        }
        for (int i = 0; i < config.getMaterias(); i++) {
            Materia m = new Materia(); m.setNombre(prefix + " Materia " + (i + 1));
            m.setDescripcion("Materia prima simulada para pruebas de trazabilidad");
            m.setUnidadMedida(Medida.values()[i % Medida.values().length]); m.setActivo(true);
            em.persist(m); materials.add(m.getId()); batch(i);
        }
        for (int i = 0; i < config.getProductos(); i++) {
            Producto p = new Producto(); p.setNombre(prefix + " Producto " + (i + 1));
            p.setDescripcion("Producto final simulado"); p.setActivo(true);
            em.persist(p); products.add(p.getId()); batch(i);
        }
        em.flush(); em.clear();
        int released = config.entradasLiberadas();
        int rejectedInputs = Math.min(config.getEntradas() / 10, config.getControles() - released);
        double initialStock = Math.max(100, Math.ceil((double) config.getConsumos() / released) * 10 + 10);
        for (int i = 0; i < config.getEntradas(); i++) {
            LoteEntrada e = new LoteEntrada();
            e.setEnviadoPor(em.getReference(Proveedor.class, providers.get(i % providers.size())));
            e.setMateriaPrima(em.getReference(Materia.class, materials.get(i % materials.size())));
            e.setFechaRecepcion(date(-60, 8)); e.setFechaVencimiento(date(365, 23));
            e.setCantidad(initialStock); e.setCantidadDisponible(initialStock); e.setActivo(true);
            e.setEstado(Estado.CUARENTENA); em.persist(e); inputs.add(e.getId());
            if (i < released) {
                Usuario supervisor = supervisor(users, i);
                Date fecha = date(-59, 10);
                String evidencia = prefix + "/entrada/" + i;
                e.setEstado(Estado.LIBERADO); e.setLiberadoPor(supervisor); e.setFechaLiberacion(fecha); e.setEvidencia(evidencia);
                control(e, null, supervisor, Dictamen.APROBADO, fecha, evidencia);
            }
            if (i >= released && i < released + rejectedInputs) {
                e.setEstado(Estado.RECHAZADO);
                control(e, null, supervisor(users, i), Dictamen.RECHAZADO, date(-59, 11), prefix + "/rechazo-entrada/" + i);
            }
            batch(i);
        }
        em.flush(); em.clear();
        int cursor = 0;
        int productionControls = config.getControles() - released - rejectedInputs;
        int evaluated = Math.min(config.getProducciones(), productionControls - productionControls / 10);
        for (int i = 0; i < config.getProducciones(); i++) {
            LoteProduccion p = new LoteProduccion();
            p.setCodigo(prefix + "-PROD-" + (i + 1));
            p.setProducto(em.getReference(Producto.class, products.get(i % products.size())));
            p.setFechaProduccion(date(-30 + i % 20, 8)); p.setEstado(Estado.CUARENTENA); p.setActivo(true);
            int count = config.getConsumos() / config.getProducciones() + (i < config.getConsumos() % config.getProducciones() ? 1 : 0);
            // Output units are independent of the units used by its recipe inputs.
            p.setCantidad(1); em.persist(p); outputs.add(p.getId());
            for (int j = 0; j < count; j++) {
                LoteEntrada e = em.find(LoteEntrada.class, inputs.get(cursor++ % released));
                double quantity = 1 + random.nextInt(5);
                if (e.getEstado() != Estado.LIBERADO || e.getFechaVencimiento().before(p.getFechaProduccion()) || e.getCantidadDisponible() < quantity)
                    throw new IllegalStateException("Insumo no válido; se revierte la carga");
                e.setCantidadDisponible(e.getCantidadDisponible() - quantity);
                e.setCompletado(e.getCantidadDisponible() == 0);
                LoteConsumo c = new LoteConsumo(); c.setLoteEntrante(e); c.setLoteProducido(p); c.setCantidadConsumida(quantity);
                em.persist(c);
            }
            p.setCantidad(10 + random.nextInt(91));
            if (i < evaluated) evaluate(p, users, i, date(-9, 10), prefix + "/produccion/" + i);
            batch(i);
        }
        em.flush(); em.clear();
        for (int i = 0; i < config.getControles() - released - rejectedInputs - evaluated; i++) {
            LoteProduccion p = em.find(LoteProduccion.class, outputs.get(i % outputs.size()));
            // Later reevaluations preserve earlier controls and update only the current decision.
            evaluate(p, users, i + 1, new Date(date(-8, 10).getTime() + i), prefix + "/reevaluacion/" + i);
            batch(i);
        }
        em.flush(); em.clear();
        Map<String, Long> counts = new LinkedHashMap<>();
        counts.put("usuarios", (long) users.size()); counts.put("proveedores", (long) providers.size());
        counts.put("materias", (long) materials.size()); counts.put("productos", (long) products.size());
        counts.put("entradas", (long) inputs.size()); counts.put("producciones", (long) outputs.size());
        counts.put("consumos", (long) cursor); counts.put("controles", (long) config.getControles());
        counts.put("total", config.total());
        return counts;
    }

    private void evaluate(LoteProduccion p, List<Long> users, int i, Date fecha, String evidencia) {
        Usuario user = supervisor(users, i);
        boolean approved = i % 5 != 0;
        p.setEstado(approved ? Estado.LIBERADO : Estado.RECHAZADO);
        p.setLiberadoPor(approved ? user : null); p.setFechaLiberacion(approved ? fecha : null);
        p.setEvidencia(approved ? evidencia : null);
        control(null, p, user, approved ? Dictamen.APROBADO : Dictamen.RECHAZADO, fecha, evidencia);
    }

    private void control(LoteEntrada entrada, LoteProduccion produccion, Usuario user, Dictamen dictamen, Date fecha, String evidencia) {
        ControlCalidad c = new ControlCalidad(); c.setLoteEntrada(entrada); c.setLoteProduccion(produccion);
        c.setResponsable(user); c.setDictamen(dictamen); c.setFecha(fecha); c.setEvidencia(evidencia);
        c.setObservacion(dictamen == Dictamen.APROBADO ? "Inspección simulada conforme" : "Inspección simulada rechazada");
        em.persist(c);
    }

    private Usuario supervisor(List<Long> users, int i) { return em.getReference(Usuario.class, users.get(1 + i % (users.size() - 1))); }
    private Date date(int days, int hour) { return Date.from(config.getReferenceDate().plusDays(days).atTime(hour, 0).atZone(ZONE).toInstant()); }
    private void batch(int i) { if ((i + 1) % 100 == 0) { em.flush(); em.clear(); } }
}
