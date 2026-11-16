package com.foodtraceqa.config.seed;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import java.time.LocalDate;
import java.time.ZoneId;

@Getter
@Setter
@ConfigurationProperties("app.seed")
public class SeedProperties {
    private String prefix = "demo20k";
    private String password;
    private long randomSeed = 20261116L;
    private LocalDate referenceDate = LocalDate.now(ZoneId.of("America/Lima"));
    private int usuarios = 100;
    private int proveedores = 200;
    private int materias = 200;
    private int productos = 100;
    private int entradas = 6000;
    private int producciones = 4000;
    private int consumos = 8000;
    private int controles = 6400;

    public int entradasLiberadas() { return entradas * 2 / 3; }
    public long total() {
        return (long) usuarios + proveedores + materias + productos + entradas + producciones + consumos + controles;
    }

    public void validate() {
        if (prefix == null || !prefix.matches("[a-z][a-z0-9]{2,19}"))
            throw new IllegalArgumentException("app.seed.prefix debe tener entre 3 y 20 letras minúsculas/dígitos e iniciar con letra");
        if (password == null || password.length() < 8 || password.length() > 72)
            throw new IllegalArgumentException("Configura SEED_PASSWORD con entre 8 y 72 caracteres ASCII");
        if (!password.chars().allMatch(c -> c >= 32 && c <= 126))
            throw new IllegalArgumentException("SEED_PASSWORD debe ser ASCII para respetar el límite de BCrypt en bytes");
        if (usuarios < 2 || proveedores < 1 || materias < 1 || productos < 1 || entradas < 3 || producciones < 1)
            throw new IllegalArgumentException("Se necesitan al menos 2 usuarios, 1 proveedor/materia/producto, 3 entradas y 1 producción");
        if (total() > 1_000_000 || consumos < 0 || controles < 0)
            throw new IllegalArgumentException("La carga admite como máximo 1 millón de registros y cantidades no negativas");
        if (consumos < producciones || (long) consumos > (long) producciones * entradasLiberadas())
            throw new IllegalArgumentException("Cada producción necesita entre 1 y el total de entradas liberadas consumos únicos");
        if (controles < entradasLiberadas())
            throw new IllegalArgumentException("No hay controles suficientes para liberar las entradas consumibles");
        if (referenceDate == null || referenceDate.isAfter(LocalDate.now(ZoneId.of("America/Lima"))))
            throw new IllegalArgumentException("La fecha de referencia no puede ser futura");
        if (total() > 1_000_000)
            throw new IllegalArgumentException("Esta carga transaccional admite como máximo 1 millón de registros");
    }
}
