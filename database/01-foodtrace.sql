CREATE DATABASE IF NOT EXISTS foodtrace_bd
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;

USE foodtrace_bd;

CREATE TABLE IF NOT EXISTS usuarios (
    usuario_id BIGINT NOT NULL AUTO_INCREMENT,
    nombres VARCHAR(255) NOT NULL,
    apellidos VARCHAR(255) NOT NULL,
    username VARCHAR(80) NOT NULL,
    password VARCHAR(255) NOT NULL,
    activo BIT(1) NOT NULL DEFAULT b'1',
    rol ENUM('SUPERVISOR', 'ADMIN') NOT NULL,
    PRIMARY KEY (usuario_id),
    UNIQUE KEY uk_usuarios_username (username),
    UNIQUE KEY idx_usuario_username (username)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS proveedores (
    proveedor_id BIGINT NOT NULL AUTO_INCREMENT,
    ruc BIGINT NOT NULL,
    nombre VARCHAR(150) NOT NULL,
    calificacion INT NOT NULL DEFAULT 0,
    activo BIT(1) NOT NULL DEFAULT b'1',
    PRIMARY KEY (proveedor_id),
    UNIQUE KEY uk_proveedores_ruc (ruc),
    UNIQUE KEY idx_proveedor_ruc (ruc)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS materias_prima (
    materia_prima_id BIGINT NOT NULL AUTO_INCREMENT,
    nombre VARCHAR(120) NOT NULL,
    descripcion VARCHAR(500),
    activo BIT(1) NOT NULL DEFAULT b'1',
    unidad_medida ENUM('KG', 'L', 'UNIDAD') NOT NULL,
    PRIMARY KEY (materia_prima_id),
    KEY idx_materia_nombre (nombre)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS productos (
    producto_id BIGINT NOT NULL AUTO_INCREMENT,
    nombre VARCHAR(120) NOT NULL,
    descripcion VARCHAR(500),
    activo BIT(1) NOT NULL DEFAULT b'1',
    PRIMARY KEY (producto_id),
    KEY idx_producto_nombre (nombre)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS lotes_entrada (
    lote_entrada_id BIGINT NOT NULL AUTO_INCREMENT,
    proveedor_id BIGINT NOT NULL,
    materia_prima_id BIGINT NOT NULL,
    liberado_por_id BIGINT,
    fecha_liberacion DATETIME,
    evidencia VARCHAR(255),
    fecha_recepcion DATETIME,
    fecha_vencimiento DATETIME,
    cantidad DOUBLE NOT NULL,
    cantidad_disponible DOUBLE NOT NULL,
    completado BIT(1) NOT NULL DEFAULT b'0',
    activo BIT(1) NOT NULL DEFAULT b'1',
    estado ENUM('CUARENTENA', 'LIBERADO', 'RECHAZADO') NOT NULL,
    PRIMARY KEY (lote_entrada_id),
    KEY idx_entrada_proveedor (proveedor_id),
    KEY idx_entrada_materia (materia_prima_id),
    CONSTRAINT fk_entrada_liberador FOREIGN KEY (liberado_por_id) REFERENCES usuarios (usuario_id),
    CONSTRAINT fk_entrada_proveedor FOREIGN KEY (proveedor_id) REFERENCES proveedores (proveedor_id),
    CONSTRAINT fk_entrada_materia FOREIGN KEY (materia_prima_id) REFERENCES materias_prima (materia_prima_id)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS lotes_produccion (
    lote_produccion_id BIGINT NOT NULL AUTO_INCREMENT,
    producto_id BIGINT NOT NULL,
    usuario_id BIGINT,
    codigo VARCHAR(80) NOT NULL,
    cantidad DOUBLE NOT NULL,
    evidencia VARCHAR(255),
    fecha_produccion DATETIME,
    fecha_liberacion DATETIME,
    estado ENUM('CUARENTENA', 'LIBERADO', 'RECHAZADO') NOT NULL,
    activo BIT(1) NOT NULL DEFAULT b'1',
    PRIMARY KEY (lote_produccion_id),
    UNIQUE KEY uk_lotes_produccion_codigo (codigo),
    UNIQUE KEY idx_produccion_codigo (codigo),
    KEY idx_produccion_producto (producto_id),
    CONSTRAINT fk_produccion_producto FOREIGN KEY (producto_id) REFERENCES productos (producto_id),
    CONSTRAINT fk_produccion_usuario FOREIGN KEY (usuario_id) REFERENCES usuarios (usuario_id)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS lotes_consumo (
    lote_consumo_id BIGINT NOT NULL AUTO_INCREMENT,
    lote_producido_id BIGINT NOT NULL,
    lote_entrante_id BIGINT NOT NULL,
    cantidad_consumida DOUBLE NOT NULL,
    PRIMARY KEY (lote_consumo_id),
    KEY idx_consumo_produccion (lote_producido_id),
    KEY idx_consumo_entrada (lote_entrante_id),
    CONSTRAINT fk_consumo_produccion FOREIGN KEY (lote_producido_id) REFERENCES lotes_produccion (lote_produccion_id),
    CONSTRAINT fk_consumo_entrada FOREIGN KEY (lote_entrante_id) REFERENCES lotes_entrada (lote_entrada_id)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS controles_calidad (
    control_calidad_id BIGINT NOT NULL AUTO_INCREMENT,
    lote_produccion_id BIGINT,
    lote_entrada_id BIGINT,
    usuario_id BIGINT NOT NULL,
    evidencia VARCHAR(255),
    observacion VARCHAR(1000) NOT NULL,
    fecha DATETIME,
    dictamen ENUM('APROBADO', 'RECHAZADO') NOT NULL,
    PRIMARY KEY (control_calidad_id),
    KEY idx_calidad_lote (lote_produccion_id),
    CONSTRAINT fk_calidad_entrada FOREIGN KEY (lote_entrada_id) REFERENCES lotes_entrada (lote_entrada_id),
    CONSTRAINT fk_calidad_lote FOREIGN KEY (lote_produccion_id) REFERENCES lotes_produccion (lote_produccion_id),
    CONSTRAINT fk_calidad_usuario FOREIGN KEY (usuario_id) REFERENCES usuarios (usuario_id)
) ENGINE=InnoDB;

-- Usuario de prueba: admin / password
INSERT INTO usuarios (nombres, apellidos, username, password, activo, rol)
SELECT 'Administrador', 'FoodTrace', 'admin',
       '$2a$10$L.ej/2nI450zNbvd99Itp.UTy6/SgsirVDCZNYa8KICSHF32Hwgb6',
       b'1', 'ADMIN'
WHERE NOT EXISTS (SELECT 1 FROM usuarios WHERE username = 'admin');

INSERT INTO proveedores (ruc, nombre, calificacion, activo)
SELECT 20123456789, 'Proveedor Demo S.A.C.', 5, b'1'
WHERE NOT EXISTS (SELECT 1 FROM proveedores WHERE ruc = 20123456789);

INSERT INTO materias_prima (nombre, descripcion, activo, unidad_medida)
SELECT 'Harina de trigo', 'Materia prima para productos de panificación', b'1', 'KG'
WHERE NOT EXISTS (SELECT 1 FROM materias_prima WHERE nombre = 'Harina de trigo');

INSERT INTO materias_prima (nombre, descripcion, activo, unidad_medida)
SELECT 'Aceite vegetal', 'Aceite vegetal de prueba', b'1', 'L'
WHERE NOT EXISTS (SELECT 1 FROM materias_prima WHERE nombre = 'Aceite vegetal');

INSERT INTO productos (nombre, descripcion, activo)
SELECT 'Pan integral demo', 'Producto de prueba para trazabilidad', b'1'
WHERE NOT EXISTS (SELECT 1 FROM productos WHERE nombre = 'Pan integral demo');

INSERT INTO lotes_entrada (
    proveedor_id, materia_prima_id, fecha_recepcion, fecha_vencimiento,
    cantidad, cantidad_disponible, completado, activo, estado
)
SELECT p.proveedor_id, m.materia_prima_id, '2026-10-01 08:00:00', '2027-03-31 23:59:59',
       100, 100, b'0', b'1', 'LIBERADO'
FROM proveedores p
JOIN materias_prima m ON m.nombre = 'Harina de trigo'
WHERE p.ruc = 20123456789
  AND NOT EXISTS (
      SELECT 1 FROM lotes_entrada e
      WHERE e.proveedor_id = p.proveedor_id AND e.materia_prima_id = m.materia_prima_id
  );

INSERT INTO lotes_produccion (
    producto_id, usuario_id, codigo, cantidad, evidencia,
    fecha_produccion, fecha_liberacion, estado, activo
)
SELECT p.producto_id, u.usuario_id, 'LOTE-DEMO-001', 50, NULL,
       '2026-10-02 09:00:00', '2026-10-02 15:00:00', 'LIBERADO', b'1'
FROM productos p
JOIN usuarios u ON u.username = 'admin'
WHERE p.nombre = 'Pan integral demo'
  AND NOT EXISTS (SELECT 1 FROM lotes_produccion WHERE codigo = 'LOTE-DEMO-001');

INSERT INTO lotes_consumo (lote_producido_id, lote_entrante_id, cantidad_consumida)
SELECT lp.lote_produccion_id, le.lote_entrada_id, 25
FROM lotes_produccion lp
JOIN lotes_entrada le ON le.cantidad_disponible = 100
WHERE lp.codigo = 'LOTE-DEMO-001'
  AND NOT EXISTS (
      SELECT 1 FROM lotes_consumo c
      WHERE c.lote_producido_id = lp.lote_produccion_id
  );

INSERT INTO controles_calidad (
    lote_produccion_id, usuario_id, evidencia, observacion, fecha, dictamen
)
SELECT lp.lote_produccion_id, u.usuario_id, NULL,
       'Control inicial aprobado para pruebas', '2026-10-02 16:00:00', 'APROBADO'
FROM lotes_produccion lp
JOIN usuarios u ON u.username = 'admin'
WHERE lp.codigo = 'LOTE-DEMO-001'
  AND NOT EXISTS (
      SELECT 1 FROM controles_calidad c
      WHERE c.lote_produccion_id = lp.lote_produccion_id
  );
