-- Ejecutar UNA VEZ sobre bases creadas antes de esta mejora, antes de arrancar la API.
USE foodtrace_bd;
ALTER TABLE lotes_entrada
    ADD COLUMN liberado_por_id BIGINT NULL,
    ADD COLUMN fecha_liberacion DATETIME NULL,
    ADD COLUMN evidencia VARCHAR(255) NULL,
    ADD CONSTRAINT fk_entrada_liberador FOREIGN KEY (liberado_por_id) REFERENCES usuarios(usuario_id);
ALTER TABLE controles_calidad
    MODIFY COLUMN lote_produccion_id BIGINT NULL,
    ADD COLUMN lote_entrada_id BIGINT NULL,
    ADD CONSTRAINT fk_calidad_entrada FOREIGN KEY (lote_entrada_id) REFERENCES lotes_entrada(lote_entrada_id);
