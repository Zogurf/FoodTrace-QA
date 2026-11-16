# Actualización de calidad de lotes

Para una base nueva, ejecutar `01-foodtrace.sql`. Docker Compose utiliza ese archivo para inicializar un volumen nuevo.

Para una base creada con la versión anterior, ejecutar **una sola vez** `02-calidad-entrada.sql` antes de arrancar el backend actualizado. No ejecutar esa migración sobre una base ya creada con el nuevo `01-foodtrace.sql`. No eliminar el volumen ni recrear los datos existentes.

El cambio permite controles de calidad sobre un lote de entrada o de producción. Los controles anteriores de producción se conservan.

## Flujo

1. Registrar una recepción: queda en CUARENTENA.
2. Como SUPERVISOR, registrar su control en `POST /api/calidad/controles/entrada` con `loteEntradaId`, `dictamen`, `observacion` y `evidencia`.
3. APROBADO exige evidencia y deja el lote LIBERADO; RECHAZADO impide su consumo. No se permite aprobar una entrada vencida.
4. Producir usando entradas LIBERADAS y no vencidas, con cantidad disponible suficiente.
5. Evaluar la producción mediante `POST /api/calidad/controles`, con `loteProduccionId` y los mismos campos de evaluación.

Una nueva evaluación se agrega al historial. Aprobar actualiza responsable, fecha y evidencia de liberación; rechazar limpia esos campos del estado vigente. No borra los controles anteriores. Reaprobar un lote rechazado requiere otra evaluación con evidencia.

Historial de entrada: `GET /api/calidad/controles/entrada/{id}`.
Historial de producción: `GET /api/calidad/controles/lote/{id}`.
Las respuestas de controles incluyen el nombre de usuario responsable, sin la entidad de usuario.

Los usuarios autenticados pueden leer los catálogos; sus escrituras siguen restringidas a ADMIN. Las evaluaciones siguen restringidas a SUPERVISOR.

## Verificación

Backend (JDK 25): `./mvnw test`.
Frontend: `npm ci` en la plataforma de destino y `npm run build`. Los estilos globales se compilan desde `src/styles.scss` y los componentes nuevos se generan con SCSS.

## Validación, concurrencia y respuestas

La producción valida cada elemento de `consumos`, incluyendo elementos nulos, IDs positivos y cantidades positivas. Los lotes repetidos se rechazan. Angular permite añadir y quitar insumos y avisa si se repite un lote.

Producción bloquea las entradas con `PESSIMISTIC_WRITE`, en orden ascendente de ID, durante la transacción completa. Calidad usa el mismo bloqueo al evaluar una entrada y también bloquea la producción al evaluarla. Así, una operación de calidad y un consumo no actualizan el mismo lote en paralelo. Si falla cualquier consumo, se revierte toda la producción, incluidos sus descuentos y enlaces. Los conflictos de bloqueo o integridad responden HTTP 409; el cliente conserva el formulario para revisar y reintentar manualmente.

Los POST de recepción/producción devuelven únicamente `id`, `codigo`, `estado`, `cantidad` y `cantidadDisponible` (los campos no aplicables son nulos). Los controles devuelven un DTO con el nombre del responsable. `Usuario.password` permanece excluido de JSON como protección adicional. El registro público conserva su comportamiento anterior.

Estos cambios de concurrencia usan bloqueos de base de datos y no requieren columnas adicionales. La única migración pendiente sigue siendo `02-calidad-entrada.sql`, de la mejora anterior. No se ha ejecutado sobre ninguna base desde esta revisión.

Pruebas añadidas: `ConsumosValidationTests`, `StockConcurrencyTests` (dos solicitudes simultáneas y rollback total), `RespuestaSeguraTests` y `CalidadServiceTests`.
