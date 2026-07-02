-- V10: Módulos de Producción/Obrador, Trazabilidad Alimentaria, Reparto/Distribución y Devoluciones
-- ERP Panadería Tahona

-- ===================== TABLA BASE (si no existe) =====================

CREATE TABLE IF NOT EXISTS albaranes_venta (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    numero VARCHAR(50),
    fecha DATE,
    total DECIMAL(10,2),
    observaciones TEXT,
    cliente_id BIGINT,
    almacen_id BIGINT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ===================== PRODUCCIÓN / OBRADOR =====================

CREATE TABLE IF NOT EXISTS recetas (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    codigo VARCHAR(50) NOT NULL,
    nombre VARCHAR(255) NOT NULL,
    descripcion VARCHAR(2000),
    tiempo_preparacion INT,
    tiempo_horneado INT,
    temperatura_horneado INT,
    rendimiento_cantidad DECIMAL(10,2),
    unidad_rendimiento VARCHAR(20),
    articulo_resultante_id BIGINT,
    activo BOOLEAN NOT NULL DEFAULT TRUE,
    UNIQUE KEY uk_recetas_codigo (codigo),
    CONSTRAINT fk_recetas_articulo FOREIGN KEY (articulo_resultante_id) REFERENCES articulos(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS receta_ingredientes (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    receta_id BIGINT NOT NULL,
    articulo_id BIGINT NOT NULL,
    cantidad DECIMAL(10,3) NOT NULL,
    unidad VARCHAR(20),
    orden INT DEFAULT 0,
    notas VARCHAR(500),
    CONSTRAINT fk_ingredientes_receta FOREIGN KEY (receta_id) REFERENCES recetas(id) ON DELETE CASCADE,
    CONSTRAINT fk_ingredientes_articulo FOREIGN KEY (articulo_id) REFERENCES articulos(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS ordenes_produccion (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    numero VARCHAR(50) NOT NULL,
    fecha DATE NOT NULL,
    receta_id BIGINT,
    cantidad_planificada DECIMAL(10,2),
    cantidad_producida DECIMAL(10,2),
    merma DECIMAL(10,2),
    estado VARCHAR(30) NOT NULL DEFAULT 'PLANIFICADA',
    fecha_inicio DATETIME,
    fecha_fin DATETIME,
    articulo_id BIGINT,
    almacen_id BIGINT,
    observaciones VARCHAR(2000),
    usuario_id BIGINT,
    fecha_creacion DATETIME,
    UNIQUE KEY uk_ordenes_numero (numero),
    CONSTRAINT fk_ordenes_receta FOREIGN KEY (receta_id) REFERENCES recetas(id),
    CONSTRAINT fk_ordenes_articulo FOREIGN KEY (articulo_id) REFERENCES articulos(id),
    CONSTRAINT fk_ordenes_almacen FOREIGN KEY (almacen_id) REFERENCES almacenes(id),
    CONSTRAINT fk_ordenes_usuario FOREIGN KEY (usuario_id) REFERENCES users(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS horneadas (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    orden_produccion_id BIGINT,
    fecha DATE NOT NULL,
    hora_inicio TIME,
    hora_fin TIME,
    temperatura_inicial INT,
    temperatura_final INT,
    tipo_horneada VARCHAR(30),
    cantidad_producida DECIMAL(10,2),
    merma DECIMAL(10,2),
    resultado VARCHAR(30),
    observaciones VARCHAR(2000),
    humedad_inicial INT,
    usuario_id BIGINT,
    CONSTRAINT fk_horneadas_orden FOREIGN KEY (orden_produccion_id) REFERENCES ordenes_produccion(id),
    CONSTRAINT fk_horneadas_usuario FOREIGN KEY (usuario_id) REFERENCES users(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ===================== TRAZABILIDAD ALIMENTARIA =====================

CREATE TABLE IF NOT EXISTS lotes (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    codigo VARCHAR(50) NOT NULL,
    articulo_id BIGINT NOT NULL,
    fecha_produccion DATE NOT NULL,
    fecha_caducidad DATE NOT NULL,
    cantidad_inicial DECIMAL(10,2),
    cantidad_actual DECIMAL(10,2),
    estado VARCHAR(30) NOT NULL DEFAULT 'ACTIVO',
    origen VARCHAR(100),
    numero_registro_sanitario VARCHAR(50),
    orden_produccion_id BIGINT,
    fecha_creacion DATETIME,
    observaciones VARCHAR(2000),
    almacen_id BIGINT,
    UNIQUE KEY uk_lotes_codigo (codigo),
    CONSTRAINT fk_lotes_articulo FOREIGN KEY (articulo_id) REFERENCES articulos(id),
    CONSTRAINT fk_lotes_orden FOREIGN KEY (orden_produccion_id) REFERENCES ordenes_produccion(id),
    CONSTRAINT fk_lotes_almacen FOREIGN KEY (almacen_id) REFERENCES almacenes(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS lote_insumos (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    lote_producto_id BIGINT NOT NULL,
    lote_insumo_id BIGINT NOT NULL,
    cantidad_usada DECIMAL(10,3) NOT NULL,
    CONSTRAINT fk_loteinsumos_producto FOREIGN KEY (lote_producto_id) REFERENCES lotes(id),
    CONSTRAINT fk_loteinsumos_insumo FOREIGN KEY (lote_insumo_id) REFERENCES lotes(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ===================== REPARTO / DISTRIBUCIÓN =====================

CREATE TABLE IF NOT EXISTS vehiculos (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    matricula VARCHAR(20) NOT NULL,
    marca VARCHAR(100),
    modelo VARCHAR(100),
    tipo VARCHAR(50),
    capacidad_kg DECIMAL(10,2),
    capacidad_volumen DECIMAL(10,2),
    consumo_medio DECIMAL(5,2),
    activo BOOLEAN NOT NULL DEFAULT TRUE,
    observaciones VARCHAR(1000),
    UNIQUE KEY uk_vehiculos_matricula (matricula)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS rutas_reparto (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    codigo VARCHAR(50) NOT NULL,
    nombre VARCHAR(255) NOT NULL,
    descripcion VARCHAR(1000),
    vehiculo_id BIGINT,
    conductor VARCHAR(100),
    activo BOOLEAN NOT NULL DEFAULT TRUE,
    distancia_total_km DECIMAL(10,2),
    tiempo_estimado_minutos INT,
    CONSTRAINT fk_rutas_vehiculo FOREIGN KEY (vehiculo_id) REFERENCES vehiculos(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS rutas_paradas (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    ruta_id BIGINT NOT NULL,
    cliente_id BIGINT NOT NULL,
    orden INT DEFAULT 0,
    hora_estimada TIME,
    tiempo_parada_minutos INT,
    notas VARCHAR(500),
    CONSTRAINT fk_paradas_ruta FOREIGN KEY (ruta_id) REFERENCES rutas_reparto(id) ON DELETE CASCADE,
    CONSTRAINT fk_paradas_cliente FOREIGN KEY (cliente_id) REFERENCES clientes(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS hojas_ruta (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    ruta_id BIGINT NOT NULL,
    fecha DATE NOT NULL,
    vehiculo_id BIGINT,
    conductor VARCHAR(100),
    estado VARCHAR(30) NOT NULL DEFAULT 'PLANIFICADA',
    hora_salida DATETIME,
    hora_llegada DATETIME,
    km_inicio DECIMAL(10,2),
    km_fin DECIMAL(10,2),
    incidencias VARCHAR(2000),
    observaciones VARCHAR(2000),
    usuario_id BIGINT,
    fecha_creacion DATETIME,
    CONSTRAINT fk_hojas_ruta FOREIGN KEY (ruta_id) REFERENCES rutas_reparto(id),
    CONSTRAINT fk_hojas_vehiculo FOREIGN KEY (vehiculo_id) REFERENCES vehiculos(id),
    CONSTRAINT fk_hojas_usuario FOREIGN KEY (usuario_id) REFERENCES users(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS hojas_ruta_entregas (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    hoja_ruta_id BIGINT NOT NULL,
    albaran_id BIGINT,
    cliente_id BIGINT NOT NULL,
    orden INT DEFAULT 0,
    entregado BOOLEAN NOT NULL DEFAULT FALSE,
    fecha_entrega DATETIME,
    estado_entrega VARCHAR(50),
    persona_recepcion VARCHAR(100),
    firma VARCHAR(1000),
    incidencia VARCHAR(500),
    importe_cobrado DECIMAL(10,2),
    medio_cobro VARCHAR(30),
    observaciones VARCHAR(1000),
    latitud DOUBLE,
    longitud DOUBLE,
    CONSTRAINT fk_entregas_hoja FOREIGN KEY (hoja_ruta_id) REFERENCES hojas_ruta(id) ON DELETE CASCADE,
    CONSTRAINT fk_entregas_albaran FOREIGN KEY (albaran_id) REFERENCES albaranes_venta(id),
    CONSTRAINT fk_entregas_cliente FOREIGN KEY (cliente_id) REFERENCES clientes(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ===================== DEVOLUCIONES =====================

CREATE TABLE IF NOT EXISTS devoluciones (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    numero VARCHAR(50) NOT NULL,
    fecha DATE NOT NULL,
    albaran_id BIGINT,
    factura_id BIGINT,
    cliente_id BIGINT NOT NULL,
    estado VARCHAR(50) NOT NULL DEFAULT 'PENDIENTE',
    motivo VARCHAR(200),
    observaciones VARCHAR(2000),
    importe_total DECIMAL(10,2),
    fecha_creacion DATETIME,
    usuario_id BIGINT,
    UNIQUE KEY uk_devoluciones_numero (numero),
    CONSTRAINT fk_devoluciones_albaran FOREIGN KEY (albaran_id) REFERENCES albaranes_venta(id),
    CONSTRAINT fk_devoluciones_factura FOREIGN KEY (factura_id) REFERENCES facturas(id),
    CONSTRAINT fk_devoluciones_cliente FOREIGN KEY (cliente_id) REFERENCES clientes(id),
    CONSTRAINT fk_devoluciones_usuario FOREIGN KEY (usuario_id) REFERENCES users(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS devoluciones_lineas (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    devolucion_id BIGINT NOT NULL,
    articulo_id BIGINT NOT NULL,
    cantidad DECIMAL(10,2) NOT NULL,
    precio_unitario DECIMAL(10,2),
    importe DECIMAL(10,2),
    motivo VARCHAR(200),
    destino VARCHAR(30),
    lote_id BIGINT,
    CONSTRAINT fk_devol_lineas_devolucion FOREIGN KEY (devolucion_id) REFERENCES devoluciones(id) ON DELETE CASCADE,
    CONSTRAINT fk_devol_lineas_articulo FOREIGN KEY (articulo_id) REFERENCES articulos(id),
    CONSTRAINT fk_devol_lineas_lote FOREIGN KEY (lote_id) REFERENCES lotes(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
