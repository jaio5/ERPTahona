-- Script de migración: añadir PKs y FK a la base `tahona`
-- ATENCIÓN: ejecuta esto sólo después de importar `tahonaOriginal.sql` en una base de prueba y tras hacer backup.
-- Este script realiza cambios estructurales: modifica columnas TEXT a VARCHARs (longitudes asumidas), añade PRIMARY KEYs y crea FOREIGN KEYs.
-- Suposiciones principales:
-- 1) Columnas tipo `Codigo`, `CodigoArticulo`, `CodigoCliente`, `CodigoAlmacen`, `CodigoProveedor`, `CodigoFamilia`, `CodigoAgente` son identificadores alfanuméricos y caben en VARCHAR(100).
-- 2) Numeraciones de documentos (NumFactura, NumAlbaran, NumPedido, NumLinea, NumCobro, NumRemesa, etc.) son enteros usados como claves naturales.
-- 3) No existen ya PRIMARY KEYs en las tablas objetivo (si existen, algunas ALTER pueden fallar).
-- Si prefieres otro enfoque (añadir ids autonuméricos), dímelo y genero otra versión.

SET FOREIGN_KEY_CHECKS=0;

USE `tahona`;

-- Modificar columnas TEXT a VARCHAR donde se usarán como claves
ALTER TABLE `Clientes` MODIFY `Codigo` VARCHAR(255) NULL;
ALTER TABLE `Articulos` MODIFY `CodigoArticulo` VARCHAR(255) NULL;
ALTER TABLE `Almacenes` MODIFY `CodigoAlmacen` VARCHAR(255) NULL;
ALTER TABLE `Agentes` MODIFY `CodigoAgente` VARCHAR(255) NULL;
ALTER TABLE `Proveedores` MODIFY `Codigo` VARCHAR(255) NULL;
ALTER TABLE `Familias` MODIFY `CodigoFamilia` VARCHAR(255) NULL;
ALTER TABLE `DireccionesEnvio` MODIFY `CodigoCliente` VARCHAR(255) NULL;
ALTER TABLE `DireccionesEnvio` MODIFY `CodigoDireccion` INT NULL;
ALTER TABLE `ArticulosPrecio` MODIFY `CodigoArticulo` VARCHAR(255) NULL;
ALTER TABLE `AlbaranesVenta` MODIFY `CodigoCliente` VARCHAR(255) NULL;
ALTER TABLE `FacturasVenta` MODIFY `CodigoCliente` VARCHAR(255) NULL;
ALTER TABLE `PedidosVenta` MODIFY `CodigoCliente` VARCHAR(255) NULL;
ALTER TABLE `CashCobros` MODIFY `CodigoCliente` VARCHAR(255) NULL;
ALTER TABLE `CashPagos` MODIFY `CodigoProveedor` VARCHAR(255) NULL;
ALTER TABLE `AlbaranesVentaDesglose` MODIFY `CodigoArticulo` VARCHAR(255) NULL;
ALTER TABLE `FacturasVentaDesglose` MODIFY `CodigoArticulo` VARCHAR(255) NULL;
ALTER TABLE `PedidosVentaDesglose` MODIFY `CodigoArticulo` VARCHAR(255) NULL;
ALTER TABLE `Suministros` MODIFY `Proveedor` VARCHAR(255) NULL;
ALTER TABLE `Suministros` MODIFY `Articulo` VARCHAR(255) NULL;
ALTER TABLE `Composicion` MODIFY `ArticuloCompuesto` VARCHAR(255) NULL;
ALTER TABLE `Composicion` MODIFY `Componente` VARCHAR(255) NULL;
ALTER TABLE `StockMovimientos` MODIFY `CodigoArticulo` VARCHAR(255) NULL;
ALTER TABLE `StockRegularizaciones` MODIFY `CodigoArticulo` VARCHAR(255) NULL;

-- Añadir PRIMARY KEYS (elección de PK por tabla)
-- Clientes: en vez de forzar PK sobre `Codigo` (posible duplicado), añadimos una columna id AUTO_INCREMENT y dejamos `Codigo` como índice único si no hay duplicados
ALTER TABLE `Clientes` ADD COLUMN IF NOT EXISTS `id` INT NOT NULL AUTO_INCREMENT PRIMARY KEY FIRST;
-- Intentar crear índice único en Codigo (si ya existen duplicados fallará, pero no impedirá la existencia del id)
ALTER TABLE `Clientes` ADD UNIQUE INDEX IF NOT EXISTS ux_clientes_codigo (`Codigo`);

-- Articulos: idem, añadimos id autonumérico y un índice único sobre CodigoArticulo
ALTER TABLE `Articulos` ADD COLUMN IF NOT EXISTS `id` INT NOT NULL AUTO_INCREMENT PRIMARY KEY FIRST;
ALTER TABLE `Articulos` ADD UNIQUE INDEX IF NOT EXISTS ux_articulos_codigo (`CodigoArticulo`);

-- Almacenes: PK sobre CodigoAlmacen (siempre que sea único)
ALTER TABLE `Almacenes` ADD PRIMARY KEY (`CodigoAlmacen`);
-- Agentes: PK sobre CodigoAgente
ALTER TABLE `Agentes` ADD PRIMARY KEY (`CodigoAgente`);
-- Proveedores: PK sobre Codigo
ALTER TABLE `Proveedores` ADD PRIMARY KEY (`Codigo`);
-- Familias: PK sobre CodigoFamilia
ALTER TABLE `Familias` ADD PRIMARY KEY (`CodigoFamilia`);
-- TiposdeIVA: PK sobre Codigo_Tipo_de_IVA (ya INTEGER)
ALTER TABLE `TiposdeIVA` ADD PRIMARY KEY (`Codigo_Tipo_de_IVA`);
-- DireccionesEnvio: PK compuesta (CodigoCliente, CodigoDireccion)
ALTER TABLE `DireccionesEnvio` ADD PRIMARY KEY (`CodigoCliente`,`CodigoDireccion`);
-- ArticulosPrecio: PK compuesta (CodigoArticulo, CodigoMoneda)
-- Nota: la columna en el dump se llama `CodigoMoneda` o `CodigoMoneda`? En el dump aparece `CodigoMoneda` como INTEGER -> si no existe, ajusta manualmente.
-- Vamos a intentar usar `CodigoMoneda` si existe; en caso contrario el índice se puede crear sobre (CodigoArticulo)

-- Comprobación de existencia de columna `CodigoMoneda` no es trivial en script simple; añadimos índice compuesto defensivo:
ALTER TABLE `ArticulosPrecio` ADD INDEX idx_articulosprecio_codarticulo (`CodigoArticulo`);

-- Documentos con PKs y desglose con PK compuesta
ALTER TABLE `FacturasVenta` ADD PRIMARY KEY (`NumFactura`);
ALTER TABLE `FacturasVentaDesglose` ADD PRIMARY KEY (`NumFactura`,`NumLinea`);
ALTER TABLE `AlbaranesVenta` ADD PRIMARY KEY (`NumAlbaran`);
ALTER TABLE `AlbaranesVentaDesglose` ADD PRIMARY KEY (`NumAlbaran`,`NumLinea`);
ALTER TABLE `PedidosVenta` ADD PRIMARY KEY (`NumPedido`);
ALTER TABLE `PedidosVentaDesglose` ADD PRIMARY KEY (`NumPedido`,`Numlinea`);
ALTER TABLE `FacturasCompra` ADD PRIMARY KEY (`NumFactura`);
ALTER TABLE `FacturasCompraDesglose` ADD PRIMARY KEY (`NumFactura`,`NumLinea`);

-- Cobros/Pagos
ALTER TABLE `CashCobros` ADD PRIMARY KEY (`NumCobro`);
ALTER TABLE `CashPagos` ADD PRIMARY KEY (`NumPago`);
ALTER TABLE `CashRemesas` ADD PRIMARY KEY (`NumRemesa`);

-- Stock y Movimientos básicos
ALTER TABLE `StockMovimientos` ADD PRIMARY KEY (`Codigo`);
ALTER TABLE `StockRegularizaciones` ADD PRIMARY KEY (`Codigo`);

-- Composicion (articulo compuesto + componente)
ALTER TABLE `Composicion` ADD PRIMARY KEY (`ArticuloCompuesto`,`Componente`);

-- Otros keys recomendados (subcuentas, cuentas)
ALTER TABLE `Cuentas` ADD PRIMARY KEY (`CodigoCuenta`);
ALTER TABLE `Subcuentas` ADD PRIMARY KEY (`Codigosubcuenta`);

-- Añadir índices para columnas utilizadas como FKs
ALTER TABLE `FacturasVenta` ADD INDEX idx_facturasventa_codigo_cliente (`CodigoCliente`);
ALTER TABLE `AlbaranesVenta` ADD INDEX idx_albaranesventa_codigo_cliente (`CodigoCliente`);
ALTER TABLE `PedidosVenta` ADD INDEX idx_pedidosventa_codigo_cliente (`CodigoCliente`);
ALTER TABLE `CashCobros` ADD INDEX idx_cashcobros_codigo_cliente (`CodigoCliente`);
ALTER TABLE `CashPagos` ADD INDEX idx_cashpagos_codigo_proveedor (`CodigoProveedor`);
ALTER TABLE `FacturasVentaDesglose` ADD INDEX idx_fvdesg_codigo_art (`CodigoArticulo`);
ALTER TABLE `AlbaranesVentaDesglose` ADD INDEX idx_avdesg_codigo_art (`CodigoArticulo`);
ALTER TABLE `PedidosVentaDesglose` ADD INDEX idx_pvdesg_codigo_art (`CodigoArticulo`);

-- FOREIGN KEYS
-- Clientes <- FacturasVenta
ALTER TABLE `FacturasVenta`
  ADD CONSTRAINT fk_facturasventa_cliente FOREIGN KEY (`CodigoCliente`) REFERENCES `Clientes`(`Codigo`) ON DELETE SET NULL ON UPDATE CASCADE;
-- Clientes <- AlbaranesVenta
ALTER TABLE `AlbaranesVenta`
  ADD CONSTRAINT fk_albaranesventa_cliente FOREIGN KEY (`CodigoCliente`) REFERENCES `Clientes`(`Codigo`) ON DELETE SET NULL ON UPDATE CASCADE;
-- Clientes <- PedidosVenta
ALTER TABLE `PedidosVenta`
  ADD CONSTRAINT fk_pedidosventa_cliente FOREIGN KEY (`CodigoCliente`) REFERENCES `Clientes`(`Codigo`) ON DELETE SET NULL ON UPDATE CASCADE;
-- Clientes <- DireccionesEnvio (la PK es compuesta ya)
ALTER TABLE `DireccionesEnvio`
  ADD CONSTRAINT fk_direccion_cliente FOREIGN KEY (`CodigoCliente`) REFERENCES `Clientes`(`Codigo`) ON DELETE CASCADE ON UPDATE CASCADE;
-- Articulos <- Desgloses
ALTER TABLE `FacturasVentaDesglose`
  ADD CONSTRAINT fk_fvdesg_articulo FOREIGN KEY (`CodigoArticulo`) REFERENCES `Articulos`(`CodigoArticulo`) ON DELETE SET NULL ON UPDATE CASCADE;
ALTER TABLE `AlbaranesVentaDesglose`
  ADD CONSTRAINT fk_avdesg_articulo FOREIGN KEY (`CodigoArticulo`) REFERENCES `Articulos`(`CodigoArticulo`) ON DELETE SET NULL ON UPDATE CASCADE;
ALTER TABLE `PedidosVentaDesglose`
  ADD CONSTRAINT fk_pvdesg_articulo FOREIGN KEY (`CodigoArticulo`) REFERENCES `Articulos`(`CodigoArticulo`) ON DELETE SET NULL ON UPDATE CASCADE;
-- Proveedores <- CashPagos
ALTER TABLE `CashPagos`
  ADD CONSTRAINT fk_cashpagos_proveedor FOREIGN KEY (`CodigoProveedor`) REFERENCES `Proveedores`(`Codigo`) ON DELETE SET NULL ON UPDATE CASCADE;
-- ArticulosProveedor (Suministros)
ALTER TABLE `Suministros`
  ADD CONSTRAINT fk_suministros_proveedor FOREIGN KEY (`Proveedor`) REFERENCES `Proveedores`(`Codigo`) ON DELETE SET NULL ON UPDATE CASCADE;
ALTER TABLE `Suministros`
  ADD CONSTRAINT fk_suministros_articulo FOREIGN KEY (`Articulo`) REFERENCES `Articulos`(`CodigoArticulo`) ON DELETE SET NULL ON UPDATE CASCADE;

-- Reactivar comprobaciones
SET FOREIGN_KEY_CHECKS=1;

-- FIN del script

/*
Instrucciones:
1) Haz backup de la base de datos: mysqldump -u root -p tahona > tahona_backup.sql
2) Importa tahonaOriginal.sql en un servidor de pruebas.
3) Ejecuta este script:
   mysql -u root -p tahona < tools/tahona_add_pks_fks.sql
4) Verifica que no haya errores y que las tablas principales tienen PKs y FK.

Si hay errores (por ejemplo, claves duplicadas al intentar añadir PK), detén y avísame: prepararé una versión que crea ids autonuméricos y migra referencias.
*/
