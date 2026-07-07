-- V36: tablas mapeadas por entidades JPA que faltaban en la cadena de migraciones
-- (historicamente creadas por hibernate ddl-auto=update en desarrollo).
-- DDL generado por Hibernate 6 contra MySQL 8; orden ajustado a las dependencias FK.

CREATE TABLE IF NOT EXISTS `albaran_venta_lineas` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `cantidad` decimal(10,2) DEFAULT NULL,
  `descripcion` varchar(500) DEFAULT NULL,
  `descuento` decimal(10,2) DEFAULT '0.00',
  `iva` decimal(5,2) DEFAULT NULL,
  `precio` decimal(10,2) DEFAULT NULL,
  `albaran_id` bigint DEFAULT NULL,
  `articulo_id` bigint DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKqk0ti0jw6j2fig37248j9yxg7` (`albaran_id`),
  KEY `FKtj5vvmfoli8ihcjlalmwokri` (`articulo_id`),
  CONSTRAINT `FKqk0ti0jw6j2fig37248j9yxg7` FOREIGN KEY (`albaran_id`) REFERENCES `albaranes_venta` (`id`) ON DELETE CASCADE,
  CONSTRAINT `FKtj5vvmfoli8ihcjlalmwokri` FOREIGN KEY (`articulo_id`) REFERENCES `articulos` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `asientos_contables` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `asiento_apertura` bit(1) DEFAULT NULL,
  `asiento_cierre` bit(1) DEFAULT NULL,
  `concepto` varchar(255) NOT NULL,
  `debe` decimal(12,2) NOT NULL,
  `descripcion` text,
  `descuadre` decimal(12,2) DEFAULT NULL,
  `factura_compra_id` bigint DEFAULT NULL,
  `fecha` date NOT NULL,
  `fecha_creacion` datetime(6) DEFAULT NULL,
  `haber` decimal(12,2) NOT NULL,
  `numero` varchar(50) NOT NULL,
  `observaciones` text,
  `tipo` varchar(50) NOT NULL,
  `factura_id` bigint DEFAULT NULL,
  `usuario_id` bigint DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UKr000j4l4dixvlevifvxdj0ilt` (`numero`),
  KEY `FK9elw2d60eh8pjbf0kpmrf9bad` (`factura_id`),
  KEY `FK5rb9tc5ip1otvrtdr6lbhjl21` (`usuario_id`),
  CONSTRAINT `FK5rb9tc5ip1otvrtdr6lbhjl21` FOREIGN KEY (`usuario_id`) REFERENCES `users` (`id`),
  CONSTRAINT `FK9elw2d60eh8pjbf0kpmrf9bad` FOREIGN KEY (`factura_id`) REFERENCES `facturas` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `bancos` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `activo` bit(1) DEFAULT NULL,
  `fecha_creacion` datetime(6) DEFAULT NULL,
  `fecha_modificacion` datetime(6) DEFAULT NULL,
  `iban` varchar(24) DEFAULT NULL,
  `moneda` varchar(3) DEFAULT NULL,
  `nombre` varchar(100) NOT NULL,
  `numero_cuenta` varchar(20) DEFAULT NULL,
  `observaciones` varchar(500) DEFAULT NULL,
  `principal` bit(1) DEFAULT NULL,
  `saldo_actual` decimal(12,2) DEFAULT NULL,
  `swift` varchar(11) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `cajas` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `diferencia` decimal(12,2) DEFAULT NULL,
  `estado` varchar(20) DEFAULT NULL,
  `fecha_apertura` datetime(6) NOT NULL,
  `fecha_cierre` datetime(6) DEFAULT NULL,
  `observaciones` text,
  `saldo_final` decimal(12,2) DEFAULT NULL,
  `saldo_inicial` decimal(12,2) NOT NULL,
  `saldo_teorico` decimal(12,2) DEFAULT NULL,
  `usuario_apertura_id` bigint DEFAULT NULL,
  `usuario_cierre_id` bigint DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKn4rdkanetkc823wucwc7wum6x` (`usuario_apertura_id`),
  KEY `FKinhashnl5ycmk5lgxdy5nmebf` (`usuario_cierre_id`),
  CONSTRAINT `FKinhashnl5ycmk5lgxdy5nmebf` FOREIGN KEY (`usuario_cierre_id`) REFERENCES `users` (`id`),
  CONSTRAINT `FKn4rdkanetkc823wucwc7wum6x` FOREIGN KEY (`usuario_apertura_id`) REFERENCES `users` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `caja_movimientos` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `concepto` varchar(255) NOT NULL,
  `factura_compra_id` bigint DEFAULT NULL,
  `fecha` datetime(6) NOT NULL,
  `forma_pago` varchar(50) DEFAULT NULL,
  `importe` decimal(12,2) NOT NULL,
  `observaciones` text,
  `referencia` varchar(100) DEFAULT NULL,
  `tipo` varchar(20) NOT NULL,
  `caja_id` bigint NOT NULL,
  `factura_id` bigint DEFAULT NULL,
  `usuario_id` bigint DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FK89aupr71kws5431a2j977qs8w` (`caja_id`),
  KEY `FKh2uxearwkn3kg3kfsti4iboft` (`factura_id`),
  KEY `FK2x1pxkj6kqkfybqujd2n7994d` (`usuario_id`),
  CONSTRAINT `FK2x1pxkj6kqkfybqujd2n7994d` FOREIGN KEY (`usuario_id`) REFERENCES `users` (`id`),
  CONSTRAINT `FK89aupr71kws5431a2j977qs8w` FOREIGN KEY (`caja_id`) REFERENCES `cajas` (`id`),
  CONSTRAINT `FKh2uxearwkn3kg3kfsti4iboft` FOREIGN KEY (`factura_id`) REFERENCES `facturas` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `direccionesenvio_new` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `codigo_direccion` int DEFAULT NULL,
  `cp` varchar(50) DEFAULT NULL,
  `direccion` varchar(1024) DEFAULT NULL,
  `direccion2` varchar(1024) DEFAULT NULL,
  `nombre` tinytext,
  `notas` tinytext,
  `poblacion` varchar(255) DEFAULT NULL,
  `provincia` tinytext,
  `telefono` varchar(50) DEFAULT NULL,
  `cliente_id` bigint DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKfyi8i3fh7lrhjwhlkudj8pbxr` (`cliente_id`),
  CONSTRAINT `FKfyi8i3fh7lrhjwhlkudj8pbxr` FOREIGN KEY (`cliente_id`) REFERENCES `clientes` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `factura_albaran` (
  `albaran_id` bigint NOT NULL,
  `factura_id` bigint NOT NULL,
  PRIMARY KEY (`albaran_id`,`factura_id`),
  KEY `FK2tnf6pq0q4cxtmnb6hmu3vs21` (`factura_id`),
  CONSTRAINT `FK2tnf6pq0q4cxtmnb6hmu3vs21` FOREIGN KEY (`factura_id`) REFERENCES `facturas` (`id`) ON DELETE CASCADE,
  CONSTRAINT `FK8d2v4ltvsxf431y3qtl79oc3i` FOREIGN KEY (`albaran_id`) REFERENCES `albaranes_venta` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `facturas_compra` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `base_imponible` decimal(12,2) NOT NULL,
  `contabilizada` bit(1) DEFAULT b'0',
  `estado` varchar(20) DEFAULT 'PENDIENTE',
  `fecha` date NOT NULL,
  `fecha_contabilizacion` datetime(6) DEFAULT NULL,
  `fecha_creacion` datetime(6) DEFAULT NULL,
  `fecha_modificacion` datetime(6) DEFAULT NULL,
  `fecha_pago` date DEFAULT NULL,
  `fecha_vencimiento` date DEFAULT NULL,
  `forma_pago` varchar(50) DEFAULT NULL,
  `importe_iva` decimal(12,2) NOT NULL,
  `importe_recargo` decimal(12,2) DEFAULT '0.00',
  `importe_retencion` decimal(12,2) DEFAULT '0.00',
  `notas_internas` tinytext,
  `numero` varchar(100) NOT NULL,
  `numero_serie` varchar(100) DEFAULT NULL,
  `observaciones` tinytext,
  `pagada` bit(1) DEFAULT b'0',
  `tipo_iva` decimal(5,2) DEFAULT NULL,
  `tipo_recargo` decimal(5,2) DEFAULT NULL,
  `tipo_retencion` decimal(5,2) DEFAULT NULL,
  `total` decimal(12,2) NOT NULL,
  `usuario_creacion` varchar(255) DEFAULT NULL,
  `pedido_compra_id` bigint DEFAULT NULL,
  `proveedor_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UKihq04wne79a767nrj01b44w58` (`numero`),
  KEY `FKl8imgps8swwt0bwpmv3iyxa5g` (`pedido_compra_id`),
  KEY `FKa4p975ckwciooaue8yj6tok5y` (`proveedor_id`),
  CONSTRAINT `FKa4p975ckwciooaue8yj6tok5y` FOREIGN KEY (`proveedor_id`) REFERENCES `proveedores` (`id`),
  CONSTRAINT `FKl8imgps8swwt0bwpmv3iyxa5g` FOREIGN KEY (`pedido_compra_id`) REFERENCES `pedidos_compra` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `facturas_compra_lineas` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `cantidad` decimal(10,2) NOT NULL,
  `descripcion` varchar(500) DEFAULT NULL,
  `descuento` decimal(5,2) DEFAULT NULL,
  `importe` decimal(12,2) NOT NULL,
  `orden` int DEFAULT NULL,
  `precio_unitario` decimal(10,4) NOT NULL,
  `tipo_iva` decimal(5,2) DEFAULT NULL,
  `articulo_id` bigint DEFAULT NULL,
  `factura_compra_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FKftdfqpjfdb6coufavr56f5f3` (`articulo_id`),
  KEY `FK71v2yq59kk6s7tm6a6xnn9lt3` (`factura_compra_id`),
  CONSTRAINT `FK71v2yq59kk6s7tm6a6xnn9lt3` FOREIGN KEY (`factura_compra_id`) REFERENCES `facturas_compra` (`id`),
  CONSTRAINT `FKftdfqpjfdb6coufavr56f5f3` FOREIGN KEY (`articulo_id`) REFERENCES `articulos` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `plan_contable` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `activa` bit(1) DEFAULT NULL,
  `codigo` varchar(20) NOT NULL,
  `nivel` int DEFAULT NULL,
  `nombre` varchar(255) NOT NULL,
  `tipo` varchar(20) DEFAULT NULL,
  `padre_id` bigint DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK55cn1hdnx6rc5hbe8t304933` (`codigo`),
  KEY `FK3o9vus4eigr7rv2mmg6dgivws` (`padre_id`),
  CONSTRAINT `FK3o9vus4eigr7rv2mmg6dgivws` FOREIGN KEY (`padre_id`) REFERENCES `plan_contable` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `plan_cuentas` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `activo` bit(1) DEFAULT NULL,
  `codigo` varchar(20) NOT NULL,
  `descripcion` text,
  `nivel` int NOT NULL,
  `nombre` varchar(255) NOT NULL,
  `tipo` varchar(50) NOT NULL,
  `cuenta_padre_id` bigint DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK7l5sxde6mnt7sewrg6qbk905o` (`codigo`),
  KEY `FK16410jeb5milerot3b7oo1svs` (`cuenta_padre_id`),
  CONSTRAINT `FK16410jeb5milerot3b7oo1svs` FOREIGN KEY (`cuenta_padre_id`) REFERENCES `plan_cuentas` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `lineas_asiento` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `concepto` varchar(255) DEFAULT NULL,
  `debe` decimal(12,2) DEFAULT NULL,
  `haber` decimal(12,2) DEFAULT NULL,
  `orden` int DEFAULT NULL,
  `asiento_id` bigint NOT NULL,
  `cuenta_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FKjtc8ljchbjvsf3ghdt46huhw3` (`asiento_id`),
  KEY `FKluff5qrdb0mtdnpuok1a1px41` (`cuenta_id`),
  CONSTRAINT `FKjtc8ljchbjvsf3ghdt46huhw3` FOREIGN KEY (`asiento_id`) REFERENCES `asientos_contables` (`id`),
  CONSTRAINT `FKluff5qrdb0mtdnpuok1a1px41` FOREIGN KEY (`cuenta_id`) REFERENCES `plan_cuentas` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `modelo347_registros` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `clave_operacion` varchar(1) DEFAULT NULL,
  `ejercicio` int NOT NULL,
  `es_cliente` bit(1) DEFAULT NULL,
  `es_proveedor` bit(1) DEFAULT NULL,
  `generado` bit(1) DEFAULT NULL,
  `importe_anual` decimal(13,2) DEFAULT NULL,
  `importe_metalico_t1` decimal(13,2) DEFAULT NULL,
  `importe_metalico_t2` decimal(13,2) DEFAULT NULL,
  `importe_metalico_t3` decimal(13,2) DEFAULT NULL,
  `importe_metalico_t4` decimal(13,2) DEFAULT NULL,
  `importe_metalico_total` decimal(13,2) DEFAULT NULL,
  `importe_t1` decimal(13,2) DEFAULT NULL,
  `importe_t2` decimal(13,2) DEFAULT NULL,
  `importe_t3` decimal(13,2) DEFAULT NULL,
  `importe_t4` decimal(13,2) DEFAULT NULL,
  `importe_total` decimal(13,2) DEFAULT NULL,
  `nif_declarado` varchar(20) NOT NULL,
  `nombre_declarado` varchar(255) NOT NULL,
  `numero_operaciones` int DEFAULT NULL,
  `observaciones` varchar(500) DEFAULT NULL,
  `pais` varchar(2) DEFAULT NULL,
  `provincia` varchar(2) DEFAULT NULL,
  `tipo_operacion` varchar(1) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `movimientos_banco` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `concepto` varchar(500) DEFAULT NULL,
  `conciliado` bit(1) DEFAULT NULL,
  `fecha` date NOT NULL,
  `fecha_conciliacion` date DEFAULT NULL,
  `fecha_creacion` datetime(6) DEFAULT NULL,
  `importe` decimal(12,2) NOT NULL,
  `observaciones` tinytext,
  `saldo_resultante` decimal(12,2) DEFAULT NULL,
  `tipo` varchar(20) DEFAULT NULL,
  `banco_id` bigint NOT NULL,
  `factura_id` bigint DEFAULT NULL,
  `factura_compra_id` bigint DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FK3mc65hma8we3ck4l964g1o1vj` (`banco_id`),
  KEY `FKpmx7lecjkj3aedgqo4x1wbmj` (`factura_id`),
  KEY `FK78ri0y5352h0hiwfmfvmxgaw6` (`factura_compra_id`),
  CONSTRAINT `FK3mc65hma8we3ck4l964g1o1vj` FOREIGN KEY (`banco_id`) REFERENCES `bancos` (`id`),
  CONSTRAINT `FK78ri0y5352h0hiwfmfvmxgaw6` FOREIGN KEY (`factura_compra_id`) REFERENCES `facturas_compra` (`id`),
  CONSTRAINT `FKpmx7lecjkj3aedgqo4x1wbmj` FOREIGN KEY (`factura_id`) REFERENCES `facturas` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `movimientos_caja` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `categoria` varchar(100) DEFAULT NULL,
  `concepto` varchar(200) NOT NULL,
  `documento` varchar(100) DEFAULT NULL,
  `fecha` date NOT NULL,
  `fecha_creacion` datetime(6) DEFAULT NULL,
  `importe` decimal(12,2) NOT NULL,
  `observaciones` varchar(500) DEFAULT NULL,
  `tipo` varchar(10) NOT NULL,
  `factura_id` bigint DEFAULT NULL,
  `usuario_id` bigint DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKqneki9mkkv7wlphsrh5c5ltof` (`factura_id`),
  KEY `FK66kce9rup8nls14imf823alrg` (`usuario_id`),
  CONSTRAINT `FK66kce9rup8nls14imf823alrg` FOREIGN KEY (`usuario_id`) REFERENCES `users` (`id`),
  CONSTRAINT `FKqneki9mkkv7wlphsrh5c5ltof` FOREIGN KEY (`factura_id`) REFERENCES `facturas` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `movimientos_stock` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `cantidad` decimal(10,2) NOT NULL,
  `concepto` varchar(500) DEFAULT NULL,
  `fecha` date NOT NULL,
  `fecha_creacion` datetime(6) DEFAULT NULL,
  `importe` decimal(12,2) DEFAULT NULL,
  `observaciones` tinytext,
  `precio_unitario` decimal(10,4) DEFAULT NULL,
  `tipo` varchar(20) NOT NULL,
  `usuario_creacion` varchar(255) DEFAULT NULL,
  `albaran_id` bigint DEFAULT NULL,
  `almacen_destino_id` bigint DEFAULT NULL,
  `almacen_origen_id` bigint DEFAULT NULL,
  `articulo_id` bigint NOT NULL,
  `factura_compra_id` bigint DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FK7w7i78teh03padryfk7nnvdq3` (`albaran_id`),
  KEY `FKermktl9v69a5013wqf11s6noi` (`almacen_destino_id`),
  KEY `FKojdqj2gqb29obc6mu2tcmp8wd` (`almacen_origen_id`),
  KEY `FKc7dxi3nywiqn1hmyl5l323b2x` (`articulo_id`),
  KEY `FK98dtjn21jh88hs6uyditxl3e8` (`factura_compra_id`),
  CONSTRAINT `FK7w7i78teh03padryfk7nnvdq3` FOREIGN KEY (`albaran_id`) REFERENCES `albaranes_venta` (`id`),
  CONSTRAINT `FK98dtjn21jh88hs6uyditxl3e8` FOREIGN KEY (`factura_compra_id`) REFERENCES `facturas_compra` (`id`),
  CONSTRAINT `FKc7dxi3nywiqn1hmyl5l323b2x` FOREIGN KEY (`articulo_id`) REFERENCES `articulos` (`id`),
  CONSTRAINT `FKermktl9v69a5013wqf11s6noi` FOREIGN KEY (`almacen_destino_id`) REFERENCES `almacenes` (`id`),
  CONSTRAINT `FKojdqj2gqb29obc6mu2tcmp8wd` FOREIGN KEY (`almacen_origen_id`) REFERENCES `almacenes` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `pedidos_compra_lineas` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `cantidad` decimal(10,2) NOT NULL,
  `cantidad_recibida` decimal(10,2) DEFAULT NULL,
  `descripcion` varchar(500) DEFAULT NULL,
  `descuento` decimal(5,2) DEFAULT NULL,
  `importe` decimal(12,2) NOT NULL,
  `orden` int DEFAULT NULL,
  `precio_unitario` decimal(10,4) NOT NULL,
  `tipo_iva` decimal(5,2) DEFAULT NULL,
  `articulo_id` bigint DEFAULT NULL,
  `pedido_compra_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FK4dt04cb5ip4ufahaq9ixdo6jm` (`articulo_id`),
  KEY `FKsbem0p77wspl5qxdwmu7b654x` (`pedido_compra_id`),
  CONSTRAINT `FK4dt04cb5ip4ufahaq9ixdo6jm` FOREIGN KEY (`articulo_id`) REFERENCES `articulos` (`id`),
  CONSTRAINT `FKsbem0p77wspl5qxdwmu7b654x` FOREIGN KEY (`pedido_compra_id`) REFERENCES `pedidos_compra` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `rgpd_accesos_datos` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `campos_accedidos` json DEFAULT NULL,
  `fecha_acceso` datetime(6) NOT NULL,
  `ip` varchar(45) DEFAULT NULL,
  `metadata` json DEFAULT NULL,
  `modulo` varchar(100) DEFAULT NULL,
  `motivo` text,
  `tipo_acceso` varchar(50) NOT NULL,
  `cliente_id` bigint DEFAULT NULL,
  `usuario_id` bigint DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_acceso_fecha` (`fecha_acceso`),
  KEY `idx_acceso_usuario` (`usuario_id`),
  KEY `idx_acceso_cliente` (`cliente_id`),
  CONSTRAINT `FKbiovxuxh9plhusu4buuugjip5` FOREIGN KEY (`usuario_id`) REFERENCES `users` (`id`),
  CONSTRAINT `FKopctw8m19yln8p5nnbkgu2447` FOREIGN KEY (`cliente_id`) REFERENCES `clientes` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `rgpd_consentimientos` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `activo` bit(1) NOT NULL DEFAULT b'1',
  `canal` varchar(50) DEFAULT NULL,
  `email` varchar(100) DEFAULT NULL,
  `fecha_consentimiento` datetime(6) NOT NULL,
  `fecha_revocacion` datetime(6) DEFAULT NULL,
  `ip_origen` varchar(45) DEFAULT NULL,
  `metadata` json DEFAULT NULL,
  `nombre` varchar(200) DEFAULT NULL,
  `otorgado` bit(1) NOT NULL DEFAULT b'0',
  `texto_consentimiento` text,
  `tipo_consentimiento` varchar(50) NOT NULL,
  `version_politica` varchar(20) DEFAULT NULL,
  `cliente_id` bigint DEFAULT NULL,
  `usuario_registro_id` bigint DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKu4q2q39jgvtlue9k14v5bx40` (`cliente_id`),
  KEY `FK7be6d37b6lww89wtaagoh9kgj` (`usuario_registro_id`),
  CONSTRAINT `FK7be6d37b6lww89wtaagoh9kgj` FOREIGN KEY (`usuario_registro_id`) REFERENCES `users` (`id`),
  CONSTRAINT `FKu4q2q39jgvtlue9k14v5bx40` FOREIGN KEY (`cliente_id`) REFERENCES `clientes` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `rgpd_solicitudes` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `canal` varchar(50) DEFAULT NULL,
  `descripcion` text,
  `email_solicitante` varchar(100) NOT NULL,
  `estado` varchar(30) NOT NULL DEFAULT 'PENDIENTE',
  `fecha_limite_respuesta` datetime(6) NOT NULL,
  `fecha_respuesta` datetime(6) DEFAULT NULL,
  `fecha_solicitud` datetime(6) NOT NULL,
  `identidad_verificada` bit(1) DEFAULT b'0',
  `ip_origen` varchar(45) DEFAULT NULL,
  `nombre_solicitante` varchar(200) NOT NULL,
  `notas_internas` text,
  `respuesta` text,
  `ruta_archivo_respuesta` varchar(500) DEFAULT NULL,
  `tipo_derecho` varchar(50) NOT NULL,
  `cliente_id` bigint DEFAULT NULL,
  `usuario_responsable_id` bigint DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FK78km7lk6a56ndp9ap9uvrn5y8` (`cliente_id`),
  KEY `FKpba939d6fidix6kjtldj9d75o` (`usuario_responsable_id`),
  CONSTRAINT `FK78km7lk6a56ndp9ap9uvrn5y8` FOREIGN KEY (`cliente_id`) REFERENCES `clientes` (`id`),
  CONSTRAINT `FKpba939d6fidix6kjtldj9d75o` FOREIGN KEY (`usuario_responsable_id`) REFERENCES `users` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Alineación de tipos de columna con lo que esperan las entidades JPA
-- (ddl-auto=validate compara tipos exactos; tipos generados por Hibernate 6).

ALTER TABLE `albaranes_venta` MODIFY COLUMN `observaciones` tinytext;
ALTER TABLE `clientes` MODIFY COLUMN `notas` tinytext;
ALTER TABLE `empresa_config` MODIFY COLUMN `direccion` tinytext;
ALTER TABLE `factura_lineas` MODIFY COLUMN `descripcion` varchar(500) DEFAULT NULL;
ALTER TABLE `factura_lineas` MODIFY COLUMN `precio_unitario` decimal(10,4) DEFAULT NULL;
ALTER TABLE `factura_lineas` MODIFY COLUMN `total` decimal(12,2) DEFAULT NULL;
ALTER TABLE `facturas` MODIFY COLUMN `observaciones_revision` tinytext;
ALTER TABLE `hojas_ruta` MODIFY COLUMN `km_inicio` decimal(38,2) DEFAULT NULL;
ALTER TABLE `hojas_ruta` MODIFY COLUMN `km_fin` decimal(38,2) DEFAULT NULL;
ALTER TABLE `pedidos_compra` MODIFY COLUMN `observaciones` tinytext;
ALTER TABLE `presupuestos` MODIFY COLUMN `observaciones` tinytext;
ALTER TABLE `proveedores` MODIFY COLUMN `notas` tinytext;
ALTER TABLE `verifactu_evidence` MODIFY COLUMN `error_message` varchar(255) DEFAULT NULL;

-- Columnas de FacturaLinea ausentes en la cadena de migraciones
ALTER TABLE `factura_lineas` ADD COLUMN `descuento` decimal(5,2) DEFAULT NULL;
ALTER TABLE `factura_lineas` ADD COLUMN `precio` decimal(10,2) DEFAULT NULL;
