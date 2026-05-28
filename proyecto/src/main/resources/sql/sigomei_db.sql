CREATE DATABASE IF NOT EXISTS sigomei_db
    DEFAULT CHARACTER SET utf8mb4
    DEFAULT COLLATE utf8mb4_unicode_ci;

USE sigomei_db;

SET FOREIGN_KEY_CHECKS = 0;
DROP TABLE IF EXISTS orden_mantenimiento;
DROP TABLE IF EXISTS tecnico;
DROP TABLE IF EXISTS equipo;
DROP TABLE IF EXISTS usuario;
SET FOREIGN_KEY_CHECKS = 1;

CREATE TABLE usuario (
    id_usuario INT AUTO_INCREMENT PRIMARY KEY,
    nombre_usuario VARCHAR(50) NOT NULL UNIQUE,
    contrasena VARCHAR(100) NOT NULL,
    rol ENUM('CONSULTOR', 'ADMINISTRADOR') NOT NULL,
    estatus ENUM('ACTIVO', 'INACTIVO') NOT NULL DEFAULT 'ACTIVO'
);

CREATE TABLE equipo (
    id_equipo INT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(80) NOT NULL,
    tipo ENUM('ELECTRICO', 'MECANICO', 'INSTRUMENTACION', 'HIDRAULICO') NOT NULL,
    marca VARCHAR(60) NOT NULL,
    modelo VARCHAR(60) NOT NULL,
    numero_serie VARCHAR(80) NOT NULL UNIQUE,
    ubicacion_planta VARCHAR(120) NOT NULL,
    fecha_instalacion DATE NOT NULL,
    estado_operativo ENUM('OPERATIVO', 'EN_MANTENIMIENTO', 'FUERA_SERVICIO', 'INACTIVO') NOT NULL DEFAULT 'OPERATIVO',
    criticidad ENUM('BAJA', 'MEDIA', 'ALTA') NOT NULL
);

CREATE TABLE tecnico (
    id_tecnico INT AUTO_INCREMENT PRIMARY KEY,
    nombre_completo VARCHAR(120) NOT NULL,
    rfc VARCHAR(13) NOT NULL UNIQUE,
    telefono VARCHAR(15) NOT NULL,
    correo VARCHAR(100) NOT NULL UNIQUE,
    especialidad ENUM('ELECTRICO', 'MECANICO', 'INSTRUMENTACION', 'HIDRAULICO') NOT NULL,
    nivel_certificacion ENUM('I', 'II', 'III') NOT NULL,
    fecha_ingreso DATE NOT NULL,
    estatus ENUM('ACTIVO', 'INACTIVO') NOT NULL DEFAULT 'ACTIVO'
);

CREATE TABLE orden_mantenimiento (
    id_orden INT AUTO_INCREMENT PRIMARY KEY,
    id_equipo INT NOT NULL,
    id_tecnico INT NOT NULL,
    tipo_mantenimiento ENUM('PREVENTIVO', 'CORRECTIVO') NOT NULL,
    fecha_programada DATE NOT NULL,
    fecha_inicio DATE NULL,
    fecha_cierre DATE NULL,
    descripcion_trabajo TEXT NOT NULL,
    costo_estimado DECIMAL(10,2) NOT NULL,
    costo_real DECIMAL(10,2) NULL,
    estado_orden ENUM('PROGRAMADA', 'EN_EJECUCION', 'FINALIZADA', 'CANCELADA') NOT NULL DEFAULT 'PROGRAMADA',

    CONSTRAINT fk_orden_equipo
        FOREIGN KEY (id_equipo)
        REFERENCES equipo(id_equipo)
        ON UPDATE CASCADE
        ON DELETE RESTRICT,

    CONSTRAINT fk_orden_tecnico
        FOREIGN KEY (id_tecnico)
        REFERENCES tecnico(id_tecnico)
        ON UPDATE CASCADE
        ON DELETE RESTRICT,

    CONSTRAINT chk_orden_costo_estimado
        CHECK (costo_estimado >= 0),

    CONSTRAINT chk_orden_costo_real
        CHECK (costo_real IS NULL OR costo_real >= 0),

    CONSTRAINT chk_orden_fechas
        CHECK (
            (fecha_inicio IS NULL OR fecha_inicio >= fecha_programada)
            AND (fecha_cierre IS NULL OR fecha_inicio IS NULL OR fecha_cierre >= fecha_inicio)
        ),

    CONSTRAINT chk_orden_finalizada
        CHECK (
            (estado_orden = 'FINALIZADA' AND fecha_cierre IS NOT NULL AND costo_real IS NOT NULL)
            OR (estado_orden <> 'FINALIZADA' AND fecha_cierre IS NULL AND costo_real IS NULL)
        )
);

CREATE INDEX idx_equipo_tipo ON equipo(tipo);
CREATE INDEX idx_equipo_criticidad ON equipo(criticidad);
CREATE INDEX idx_equipo_estado ON equipo(estado_operativo);

CREATE INDEX idx_tecnico_especialidad ON tecnico(especialidad);
CREATE INDEX idx_tecnico_certificacion ON tecnico(nivel_certificacion);
CREATE INDEX idx_tecnico_estatus ON tecnico(estatus);

CREATE INDEX idx_orden_estado ON orden_mantenimiento(estado_orden);
CREATE INDEX idx_orden_fecha_programada ON orden_mantenimiento(fecha_programada);
CREATE INDEX idx_orden_equipo ON orden_mantenimiento(id_equipo);
CREATE INDEX idx_orden_tecnico ON orden_mantenimiento(id_tecnico);
CREATE INDEX idx_orden_equipo_fecha_estado
    ON orden_mantenimiento(id_equipo, fecha_programada, estado_orden);

INSERT INTO usuario (id_usuario, nombre_usuario, contrasena, rol, estatus) VALUES
(1, 'admin', 'admin123', 'ADMINISTRADOR', 'ACTIVO'),
(2, 'consulta', 'consulta123', 'CONSULTOR', 'ACTIVO'),
(3, 'bloqueado', 'bloqueado123', 'CONSULTOR', 'INACTIVO');

INSERT INTO equipo (
    id_equipo,
    nombre,
    tipo,
    marca,
    modelo,
    numero_serie,
    ubicacion_planta,
    fecha_instalacion,
    estado_operativo,
    criticidad
) VALUES
(1, 'Compresor electrico', 'ELECTRICO', 'Atlas', 'AX-10', 'EQ-001', 'Planta Norte', '2024-01-10', 'OPERATIVO', 'ALTA'),
(2, 'Bomba mecanica', 'MECANICO', 'Flow', 'BM-22', 'EQ-002', 'Planta Sur', '2024-02-12', 'OPERATIVO', 'MEDIA'),
(3, 'Prensa hidraulica', 'HIDRAULICO', 'Hydra', 'PH-10', 'EQ-010', 'Planta Oeste', '2024-03-01', 'OPERATIVO', 'BAJA');

INSERT INTO tecnico (
    id_tecnico,
    nombre_completo,
    rfc,
    telefono,
    correo,
    especialidad,
    nivel_certificacion,
    fecha_ingreso,
    estatus
) VALUES
(1, 'Ana Lopez', 'LOAA900101AA1', '5551000001', 'ana@example.com', 'ELECTRICO', 'II', '2022-01-10', 'ACTIVO'),
(2, 'Bruno Ruiz', 'RUBB900101BB1', '5551000002', 'bruno@example.com', 'MECANICO', 'II', '2022-02-10', 'ACTIVO'),
(3, 'Carla Soto', 'SOCC900101CC1', '5551000003', 'carla@example.com', 'ELECTRICO', 'II', '2022-03-10', 'INACTIVO'),
(4, 'Diego Mora', 'MODD900101DD1', '5551000004', 'diego@example.com', 'ELECTRICO', 'I', '2022-04-10', 'ACTIVO'),
(5, 'Elena Vera', 'VEEE900101EE1', '5551000010', 'elena@example.com', 'HIDRAULICO', 'III', '2022-05-10', 'ACTIVO');

INSERT INTO orden_mantenimiento (
    id_orden,
    id_equipo,
    id_tecnico,
    tipo_mantenimiento,
    fecha_programada,
    fecha_inicio,
    fecha_cierre,
    descripcion_trabajo,
    costo_estimado,
    costo_real,
    estado_orden
) VALUES
(1, 1, 1, 'PREVENTIVO', '2026-05-20', NULL, NULL, 'Orden programada base', 1500.00, NULL, 'PROGRAMADA'),
(2, 2, 2, 'CORRECTIVO', '2026-05-19', '2026-05-19', NULL, 'Orden en ejecucion base', 1800.00, NULL, 'EN_EJECUCION'),
(3, 2, 2, 'PREVENTIVO', '2026-05-18', '2026-05-18', '2026-05-19', 'Orden finalizada base', 1200.00, 1250.00, 'FINALIZADA');

ALTER TABLE usuario AUTO_INCREMENT = 4;
ALTER TABLE equipo AUTO_INCREMENT = 4;
ALTER TABLE tecnico AUTO_INCREMENT = 6;
ALTER TABLE orden_mantenimiento AUTO_INCREMENT = 4;
