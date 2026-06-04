-- V13: APPCC - Registro de puntos críticos de control
CREATE TABLE IF NOT EXISTS appcc_controles (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    fecha DATE NOT NULL,
    hora DATETIME NOT NULL,
    punto_critico VARCHAR(50) NOT NULL,
    descripcion VARCHAR(100),
    temperatura DECIMAL(5,1),
    unidad_temperatura VARCHAR(20) DEFAULT '°C',
    limite_critico DECIMAL(5,1),
    resultado VARCHAR(30) DEFAULT 'CONFORME',
    accion_correctiva VARCHAR(500),
    responsable VARCHAR(100),
    lote_id BIGINT,
    usuario_id BIGINT,
    fecha_creacion DATETIME,
    CONSTRAINT fk_appcc_lote FOREIGN KEY (lote_id) REFERENCES lotes(id),
    CONSTRAINT fk_appcc_usuario FOREIGN KEY (usuario_id) REFERENCES users(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
