# Generador de datos simulados

El perfil `seed` añade 25.000 registros relacionados: 100 usuarios (1 ADMIN y 99 SUPERVISOR), 200 proveedores, 200 materias, 100 productos, 6.000 entradas, 4.000 producciones, 8.000 consumos y 6.400 controles. No incluye despachos porque el modelo todavía no tiene esa entidad.

## Preparación

Usar una base de pruebas MySQL con el esquema actual. Si tu base es anterior a las mejoras de calidad, aplicar una sola vez `02-calidad-entrada.sql` como indica el README; para una base nueva usar el `01-foodtrace.sql` actualizado. No ejecutar ambos sobre una base nueva. El generador valida el esquema y no ejecuta migraciones.

Requiere JDK 25 y las dependencias Maven del proyecto. Detener temporalmente las escrituras de otras instancias durante la carga. Los datos existentes se conservan.

## Ejecutar desde FoodTrace-QA

```bash
# Leer la contraseña sin incluirla en el historial de comandos.
read -r -s -p 'Contraseña para cuentas simuladas: ' SEED_PASSWORD
export SEED_PASSWORD
bash mvnw spring-boot:run -Dspring-boot.run.profiles=seed
unset SEED_PASSWORD
```

La contraseña debe tener entre 8 y 72 caracteres ASCII. Se guarda con BCrypt y no se imprime. Todas las cuentas simuladas comparten esa contraseña, solo para pruebas. El usuario ADMIN es `demo20k_admin` y los supervisores son `demo20k_sup_1` a `demo20k_sup_99`.

Esperar el mensaje `Carga terminada y confirmada` y el resumen por tabla; después detener con Ctrl+C. Arrancar normalmente sin `seed` para usar Angular/API en el puerto habitual. El proceso seed usa un puerto aleatorio mientras está activo.

## Repetición y configuración

La carga completa se ejecuta en una transacción: un error revierte todas sus inserciones y descuentos. El username del administrador identifica la carga y su restricción única impide confirmar dos cargas simultáneas con el mismo prefijo. Ejecutar otra vez `demo20k` produce un error sin duplicar datos. No borrar ese usuario marcador. No existe una opción de borrado o reinicio del historial.

Para crear otra carga intencionalmente, usar otro prefijo alfanumérico en minúscula (3–20 caracteres, empieza por letra):

```bash
bash mvnw spring-boot:run -Dspring-boot.run.profiles=seed \
  -Dspring-boot.run.arguments="--app.seed.prefix=demoextra --app.seed.random-seed=42"
```

Las cantidades se configuran con `--app.seed.usuarios`, `proveedores`, `materias`, `productos`, `entradas`, `producciones`, `consumos` y `controles`. Se validan antes de insertar. El total incluye también los controles de las entradas. La distribución por defecto suma exactamente 25.000 registros **nuevos**, además de los ya existentes.

La semilla fija reproduce la distribución sobre una base equivalente. IDs y hashes BCrypt no serán idénticos. La fecha de referencia usa el día actual de Lima; para reproducir las fechas pasar `--app.seed.reference-date=2026-11-16` (no puede ser futura). No cambiar la fecha de referencia en una repetición con el mismo prefijo esperando otra carga.

## Coherencia del escenario

- Dos tercios de las entradas se aprueban antes de producir; algunas de las restantes se rechazan y otras quedan en cuarentena.
- Cada producción consume al menos una entrada y no repite una entrada dentro de su receta. Solo usa entradas liberadas; las cantidades se descuentan y se comprueba su disponibilidad y vencimiento a la fecha histórica de producción.
- Las fechas de recepción preceden a los controles de entrada; estos preceden a producción; los controles de producción son posteriores a ella y no futuros.
- Las aprobaciones tienen responsable SUPERVISOR, evidencia y fecha; los rechazos no conservan una liberación vigente. Se reserva parte de los controles de producción para reevaluaciones posteriores sin borrar las anteriores.
- Se usan EntityManager y referencias por ID para limitar la memoria: se hace flush/clear cada 100 entidades principales. Con IDs IDENTITY los inserts pueden seguir siendo individuales; no se promete un tiempo fijo de carga.
- Este generador no llama a los servicios operativos. Implementa sus invariantes explícitamente para permitir datos históricos. No sustituye las pruebas de esos servicios.

## Verificación

El resumen se imprime después del commit. Para comprobar las cantidades totales actuales:

```sql
SELECT 'usuarios' tabla, COUNT(*) registros FROM usuarios
UNION ALL SELECT 'proveedores', COUNT(*) FROM proveedores
UNION ALL SELECT 'materias', COUNT(*) FROM materias_prima
UNION ALL SELECT 'productos', COUNT(*) FROM productos
UNION ALL SELECT 'entradas', COUNT(*) FROM lotes_entrada
UNION ALL SELECT 'producciones', COUNT(*) FROM lotes_produccion
UNION ALL SELECT 'consumos', COUNT(*) FROM lotes_consumo
UNION ALL SELECT 'controles', COUNT(*) FROM controles_calidad;
```

Las pruebas `DataSeedTests` usan una carga reducida en H2 para verificar conteos, stock, roles, relaciones, duplicados y rollback. Ejecutar `bash mvnw test` antes de cargar MySQL. Las mediciones de rendimiento para RNF03 se hacen después sobre MySQL con la carga completa.
