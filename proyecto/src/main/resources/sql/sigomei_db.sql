CREATE DATABASE IF NOT EXISTS sigomei_db;
USE sigomei_db;

CREATE TABLE usuario (
    id_usuario INT AUTO_INCREMENT PRIMARY KEY,
    nombre_usuario VARCHAR(50) NOT NULL UNIQUE,
    contrasena VARCHAR(100) NOT NULL,
    rol ENUM('COORDINADOR', 'SUPERVISOR') NOT NULL,
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
        ON DELETE RESTRICT
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