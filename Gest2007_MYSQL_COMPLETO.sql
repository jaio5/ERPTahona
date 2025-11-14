-- ================================================================
-- SCRIPT MYSQL COMPLETO - Base de Datos Gest2007
-- Convertido desde SQLite
-- ================================================================

SET FOREIGN_KEY_CHECKS=0;
SET NAMES utf8mb4;
SET CHARACTER_SET_CLIENT=utf8mb4;
SET CHARACTER_SET_CONNECTION=utf8mb4;
SET SQL_MODE='NO_AUTO_VALUE_ON_ZERO,ALLOW_INVALID_DATES';

DROP DATABASE IF EXISTS Gest2007;
CREATE DATABASE Gest2007 CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE Gest2007;



CREATE TABLE IF NOT EXISTS `Agentes` (
	`CodigoAgente`	TEXT,
	`NombreAgente`	TEXT,
	`Notas`	TEXT,
	`_Comision`	REAL,
	`Domicilio`	TEXT,
	`Poblacion`	TEXT,
	`CodigoPostal`	TEXT,
	`Provincia`	TEXT,
	`CIF`	TEXT
);
CREATE TABLE IF NOT EXISTS `AlbaranesCompra` (
	`NumAlbaran`	INTEGER,
	`NumFactura`	INTEGER,
	`NumPedido`	INTEGER,
	`Fecha`	TEXT,
	`IVASN`	TEXT,
	`REQSN`	TEXT,
	`CodigoProveedor`	TEXT,
	`CodigoAlmacen`	TEXT,
	`Observaciones`	TEXT,
	`Sumadesglose`	REAL,
	`_DescuentoPP`	REAL,
	`Descuentopp`	REAL,
	`SumaIVA`	REAL,
	`SumaREQ`	REAL,
	`TotalFactura`	REAL,
	`BaseIVA1`	REAL,
	`IVA1`	REAL,
	`REQ1`	REAL,
	`ImporteIVA1`	REAL,
	`BaseIVA2`	REAL,
	`IVA2`	REAL,
	`REQ2`	REAL,
	`ImporteIVA2`	REAL,
	`BaseIVA3`	REAL,
	`IVA3`	REAL,
	`REQ3`	REAL,
	`ImporteIVA3`	REAL
);
CREATE TABLE IF NOT EXISTS `AlbaranesCompraDesglose` (
	`NumAlbaran`	INTEGER,
	`NumLinea`	INTEGER,
	`CodigoArticulo`	TEXT,
	`DescripcionArticulo`	TEXT,
	`Cantidad`	REAL,
	`PVP`	REAL,
	`Descuento`	REAL,
	`PrecioUnitario`	REAL,
	`Subtotal`	REAL,
	`TIVA`	REAL
);
CREATE TABLE IF NOT EXISTS `AlbaranesVenta` (
	`NumAlbaran`	INTEGER,
	`NumFactura`	INTEGER,
	`NumPedido`	INTEGER,
	`Fecha`	TEXT,
	`IVASN`	TEXT,
	`REQSN`	TEXT,
	`CodigoCliente`	TEXT,
	`CodigoAlmacen`	TEXT,
	`Observaciones`	TEXT,
	`Sumadesglose`	REAL,
	`_DescuentoPP`	REAL,
	`Descuentopp`	REAL,
	`SumaIVA`	REAL,
	`SumaREQ`	REAL,
	`TotalFactura`	REAL,
	`BaseIVA1`	REAL,
	`IVA1`	REAL,
	`REQ1`	REAL,
	`ImporteIVA1`	REAL,
	`BaseIVA2`	REAL,
	`IVA2`	REAL,
	`REQ2`	REAL,
	`ImporteIVA2`	REAL,
	`BaseIVA3`	REAL,
	`IVA3`	REAL,
	`REQ3`	REAL,
	`ImporteIVA3`	REAL,
	`DireccionEnvio`	INTEGER
);
CREATE TABLE IF NOT EXISTS `AlbaranesVentaDesglose` (
	`NumAlbaran`	INTEGER,
	`NumLinea`	INTEGER,
	`CodigoArticulo`	TEXT,
	`DescripcionArticulo`	TEXT,
	`Cantidad`	REAL,
	`PVP`	REAL,
	`Descuento`	REAL,
	`PrecioUnitario`	REAL,
	`Subtotal`	REAL,
	`TIVA`	INTEGER
);
CREATE TABLE IF NOT EXISTS `Almacenes` (
	`CodigoAlmacen`	TEXT,
	`NombreAlmacen`	TEXT
);
CREATE TABLE IF NOT EXISTS `Apuntes` (
	`Asiento`	INTEGER,
	`Fila`	INTEGER,
	`Fecha`	TEXT,
	`Subcuenta`	INTEGER,
	`Concepto`	TEXT,
	`DebePesetas`	REAL,
	`HaberPesetas`	REAL,
	`Documento`	TEXT,
	`Punteo`	TEXT,
	`Renumerado`	INTEGER,
	`Saldo`	REAL
);
CREATE TABLE IF NOT EXISTS `Articulos` (
	`CodigoArticulo`	TEXT,
	`DescripcionCorta`	TEXT,
	`DescripcionArticulo`	TEXT,
	`Unidad`	TEXT,
	`FamiliaArticulo`	TEXT,
	`Tipo_de_IVA`	INTEGER,
	`SubCuentaVentas`	INTEGER,
	`SubCuentaCompras`	INTEGER,
	`UsoInterno`	TEXT,
	`Compuesto`	TEXT,
	`Servicio`	TEXT,
	`CosteArticulo`	REAL,
	`CosteMedio`	REAL,
	`UltimoCoste`	REAL,
	`PVP1`	REAL,
	`PVP2`	REAL,
	`PVP3`	REAL,
	`PVP4`	REAL,
	`PVP5`	REAL,
	`MinimoStock`	INTEGER,
	`MaximoStock`	INTEGER,
	`MinStockPorAlmacen`	INTEGER,
	`MaxStockPorAlmacen`	INTEGER,
	`Descuento1`	REAL,
	`Descuento2`	REAL,
	`Descuento3`	REAL,
	`Descuento4`	REAL,
	`Descuento5`	REAL,
	`ProveedorDefecto`	TEXT,
	`DiasFabricacion`	REAL,
	`CodigoTablaArticulo`	TEXT,
	`ValorFila`	TEXT,
	`ValorColumna`	TEXT
);
CREATE TABLE IF NOT EXISTS `ArticulosPrecio` (
	`CodigoArticulo`	TEXT,
	`CodigoMoneda`	INTEGER,
	`PVP1`	REAL,
	`PVP2`	REAL,
	`PVP3`	REAL,
	`PVP4`	REAL,
	`PVP5`	REAL,
	`Coste`	REAL
);
CREATE TABLE IF NOT EXISTS `Asientos` (
	`Asiento`	INTEGER,
	`Fila`	INTEGER,
	`Fecha`	TEXT,
	`Subcuenta`	INTEGER,
	`Concepto`	TEXT,
	`DebePesetas`	REAL,
	`HaberPesetas`	REAL,
	`Documento`	TEXT,
	`Punteo`	TEXT,
	`Renumerado`	INTEGER,
	`Saldo`	REAL
);
CREATE TABLE IF NOT EXISTS `AsientosPatron` (
	`Codigo`	INTEGER,
	`Descripcion`	TEXT,
	`ProveedorOCliente`	TEXT
);
CREATE TABLE IF NOT EXISTS `BalancesConImporte` (
	`Agrupacion`	INTEGER,
	`Titulo`	TEXT,
	`SumadeImporte`	REAL,
	`SaldoA_oAnterior`	REAL
);
CREATE TABLE IF NOT EXISTS `Bancos` (
	`CodigoBanco`	INTEGER,
	`Nombre`	TEXT,
	`Direccion`	TEXT,
	`Poblacion`	TEXT,
	`CP`	TEXT,
	`Telefono`	TEXT,
	`FAX`	TEXT,
	`Director`	TEXT,
	`Banco`	TEXT,
	`Oficina`	TEXT,
	`DC`	TEXT,
	`Cuenta`	TEXT,
	`CuentaContable`	INTEGER,
	`Provincia`	TEXT,
	`SufijoOrdenante`	TEXT,
	`SufijoPresentador`	TEXT,
	`CodigoCedente`	TEXT
);
CREATE TABLE IF NOT EXISTS `CashCobros` (
	`NumCobro`	INTEGER,
	`CodigoCliente`	TEXT,
	`FechaExpedicion`	TEXT,
	`FechaVencimiento`	TEXT,
	`FechaCobro`	TEXT,
	`Concepto`	TEXT,
	`Importe`	REAL,
	`Presentado`	TEXT,
	`Cobrado`	TEXT,
	`Impagado`	TEXT,
	`BancoCliente`	TEXT,
	`PoblacionBanco`	TEXT,
	`DireccionBanco`	TEXT,
	`Banco`	TEXT,
	`AgenciaBanco`	TEXT,
	`CuentaBanco`	TEXT,
	`DC`	TEXT,
	`Asiento`	INTEGER,
	`Factura`	INTEGER,
	`Devuelto`	TEXT
);
CREATE TABLE IF NOT EXISTS `CashPagos` (
	`NumPago`	INTEGER,
	`CodigoProveedor`	TEXT,
	`Vencimiento`	TEXT,
	`Concepto`	TEXT,
	`Importe`	REAL,
	`CodigoBanco`	INTEGER,
	`Pagado`	TEXT,
	`Asiento`	INTEGER,
	`Factura`	INTEGER
);
CREATE TABLE IF NOT EXISTS `CashRemesas` (
	`NumRemesa`	INTEGER,
	`Fecha_Ingreso`	TEXT,
	`Banco`	INTEGER,
	`NumCobros`	INTEGER,
	`ImporteTotal`	REAL,
	`NumAsiento`	INTEGER,
	`SubcuentaEfectos`	INTEGER,
	`Norma`	INTEGER
);
CREATE TABLE IF NOT EXISTS `CashRemesasDesglose` (
	`NumRemesa`	INTEGER,
	`NumCobro`	INTEGER,
	`Linea`	INTEGER
);
CREATE TABLE IF NOT EXISTS `Clientes` (
	`Codigo`	TEXT,
	`CodigoDeReferencia`	TEXT,
	`Nombre`	TEXT,
	`Domicilio`	TEXT,
	`Domicilio2`	TEXT,
	`Poblacion`	TEXT,
	`CodigoPostal`	TEXT,
	`Provincia`	TEXT,
	`PersonaContacto`	TEXT,
	`Telefono`	TEXT,
	`Fax`	TEXT,
	`CIF`	TEXT,
	`IVASN`	TEXT,
	`REQSN`	TEXT,
	`SubcuentaCliente`	INTEGER,
	`SubcuentaProveedor`	INTEGER,
	`TipoDescuento`	INTEGER,
	`Tarifa`	INTEGER,
	`SectoresActividad`	TEXT,
	`FormaPago`	TEXT,
	`Agente`	TEXT,
	`Riesgo`	REAL,
	`Banco`	TEXT,
	`DireccionBanco`	TEXT,
	`PoblacionBanco`	TEXT,
	`ProvinciaBanco`	TEXT,
	`AgenciaBanco`	TEXT,
	`CuentaBanco`	TEXT,
	`Notas`	TEXT
);
CREATE TABLE IF NOT EXISTS `ClientesDiaPago` (
	`CodigoCliente`	TEXT,
	`DiaPago`	INTEGER
);
CREATE TABLE IF NOT EXISTS `CobrosPagosPendientes` (
	`Fecha`	TEXT,
	`Numero`	INTEGER,
	`Nombre`	TEXT,
	`Codigo`	TEXT,
	`Cobro`	REAL,
	`Pago`	REAL,
	`Saldo`	REAL
);
CREATE TABLE IF NOT EXISTS `Composicion` (
	`ArticuloCompuesto`	TEXT,
	`Componente`	TEXT,
	`Cantidad`	REAL
);
CREATE TABLE IF NOT EXISTS `Cuentas` (
	`CodigoCuenta`	INTEGER,
	`Titulo`	TEXT,
	`SaldoDebe`	REAL,
	`SaldoHaber`	REAL,
	`SaldoDebe2`	REAL,
	`SaldoHaber2`	REAL,
	`Filtro0`	INTEGER,
	`Filtro9`	INTEGER
);
CREATE TABLE IF NOT EXISTS `DatosEmpresa` (
	`Codigo`	INTEGER,
	`Nombre`	TEXT,
	`Direcci_n`	TEXT,
	`Poblaci_n`	TEXT,
	`Prov_ncia`	TEXT,
	`CP`	TEXT,
	`CIF`	TEXT,
	`SufijoClienteOrdenante`	TEXT,
	`SufijoClientePresentador`	TEXT,
	`Directorio`	TEXT,
	`DirectorioEA`	INTEGER,
	`Defecto`	TEXT,
	`CodigoImpPed`	INTEGER,
	`CodigoImpAlb`	INTEGER,
	`CodigoImpFac`	INTEGER,
	`CodigoImpRec`	REAL,
	`CodigoImpPago`	INTEGER,
	`CodigoImpPresupuesto`	INTEGER,
	`VentasIngresoFin`	INTEGER,
	`VentasGastoFin`	INTEGER,
	`ComprasIngresoFin`	INTEGER,
	`ComprasGastoFin`	INTEGER,
	`DevolucionVentas`	INTEGER,
	`DevolucionCompras`	INTEGER,
	`Vigilar_stock`	TEXT,
	`FechaUltimoInventario`	TEXT,
	`TipoValoracion`	INTEGER,
	`Moneda`	INTEGER,
	`DecimalesPrecios`	INTEGER,
	`DecimalesCantidad`	INTEGER,
	`DecimalesCantidadPresentes`	TEXT,
	`DecimalesPreciosPresentes`	TEXT,
	`CabListado`	TEXT
);
CREATE TABLE IF NOT EXISTS `DireccionesEnvio` (
	`CodigoCliente`	TEXT,
	`CodigoDireccion`	INTEGER,
	`Nombre`	TEXT,
	`Direccion`	TEXT,
	`Direccion2`	TEXT,
	`Poblacion`	TEXT,
	`Provincia`	TEXT,
	`CP`	TEXT,
	`Telefono`	TEXT,
	`Notas`	TEXT
);
CREATE TABLE IF NOT EXISTS `FacturasCompra` (
	`NumFactura`	INTEGER,
	`NumAsiento`	INTEGER,
	`NumIVA`	INTEGER,
	`Fecha`	TEXT,
	`IVASN`	TEXT,
	`REQSN`	TEXT,
	`CodigoProveedor`	TEXT,
	`FormaPago`	TEXT,
	`Observaciones`	TEXT,
	`Sumadesglose`	REAL,
	`_DescuentoPP`	REAL,
	`Descuentopp`	REAL,
	`SumaIVA`	REAL,
	`SumaREQ`	REAL,
	`TotalFactura`	REAL,
	`Agente`	TEXT,
	`_Comision`	REAL,
	`Importe_Comision`	REAL,
	`BaseIVA1`	REAL,
	`IVA1`	REAL,
	`REQ1`	REAL,
	`ImporteIVA1`	REAL,
	`BaseIVA2`	REAL,
	`IVA2`	REAL,
	`REQ2`	REAL,
	`ImporteIVA2`	REAL,
	`BaseIVA3`	REAL,
	`IVA3`	REAL,
	`REQ3`	REAL,
	`ImporteIVA3`	REAL,
	`Referencia`	TEXT
);
CREATE TABLE IF NOT EXISTS `FacturasCompraDesglose` (
	`NumFactura`	INTEGER,
	`NumLinea`	INTEGER,
	`CodigoArticulo`	TEXT,
	`DescripcionArticulo`	TEXT,
	`Cantidad`	REAL,
	`PVP`	REAL,
	`Descuento`	REAL,
	`PrecioUnitario`	REAL,
	`Subtotal`	REAL,
	`TIVA`	INTEGER,
	`NumAlbaran`	INTEGER,
	`Fecha`	TEXT
);
CREATE TABLE IF NOT EXISTS `FacturasVenta` (
	`NumFactura`	INTEGER,
	`NumAsiento`	INTEGER,
	`NumIVA`	INTEGER,
	`Fecha`	TEXT,
	`IVASN`	TEXT,
	`REQSN`	TEXT,
	`CodigoCliente`	TEXT,
	`FormaPago`	TEXT,
	`Observaciones`	TEXT,
	`Sumadesglose`	REAL,
	`_DescuentoPP`	REAL,
	`Descuentopp`	REAL,
	`SumaIVA`	REAL,
	`SumaREQ`	REAL,
	`TotalFactura`	REAL,
	`Agente`	TEXT,
	`_Comision`	REAL,
	`Importe_Comision`	REAL,
	`BaseIVA1`	REAL,
	`IVA1`	REAL,
	`REQ1`	REAL,
	`ImporteIVA1`	REAL,
	`BaseIVA2`	REAL,
	`IVA2`	REAL,
	`REQ2`	REAL,
	`ImporteIVA2`	REAL,
	`BaseIVA3`	REAL,
	`IVA3`	REAL,
	`REQ3`	REAL,
	`ImporteIVA3`	REAL,
	`Referencia`	TEXT
);
CREATE TABLE IF NOT EXISTS `FacturasVentaDesglose` (
	`NumFactura`	INTEGER,
	`NumLinea`	INTEGER,
	`CodigoArticulo`	TEXT,
	`DescripcionArticulo`	TEXT,
	`Cantidad`	REAL,
	`PVP`	REAL,
	`Descuento`	REAL,
	`PrecioUnitario`	REAL,
	`Subtotal`	REAL,
	`TIVA`	INTEGER,
	`NumAlbaran`	INTEGER,
	`Fecha`	TEXT
);
CREATE TABLE IF NOT EXISTS `Familias` (
	`CodigoFamilia`	TEXT,
	`DescripcionFamilia`	TEXT
);
CREATE TABLE IF NOT EXISTS `FormasdePago` (
	`CodigoFormaPago`	TEXT,
	`Descripcion`	TEXT
);
CREATE TABLE IF NOT EXISTS `FormasdePagoDesglose` (
	`CodigoFormaPago`	TEXT,
	`D_asFechaFactura`	INTEGER,
	`D_adePago`	INTEGER,
	`PorcentajeFactura`	REAL
);
CREATE TABLE IF NOT EXISTS `IVArep` (
	`NumFactura`	INTEGER,
	`TipoIVA`	INTEGER,
	`Documento`	TEXT,
	`CodigoEmpresa`	TEXT,
	`Fecha`	TEXT,
	`BaseImponible`	REAL,
	`IVA_`	REAL,
	`CuotaIVA`	REAL,
	`REQ_`	REAL,
	`CuotaREQ`	REAL,
	`ImporteIVA`	REAL,
	`Total_Factura`	REAL,
	`UINumFact`	INTEGER
);
CREATE TABLE IF NOT EXISTS `IVAsop` (
	`NumFactura`	INTEGER,
	`TipoIVA`	INTEGER,
	`Documento`	TEXT,
	`CodigoEmpresa`	TEXT,
	`Fecha`	TEXT,
	`BaseImponible`	REAL,
	`IVA_`	REAL,
	`CuotaIVA`	REAL,
	`REQ_`	REAL,
	`CuotaREQ`	REAL,
	`ImporteIVA`	REAL,
	`Total_Factura`	REAL,
	`UINumFact`	INTEGER
);
CREATE TABLE IF NOT EXISTS `OrdenesProduccion` (
	`NumOrden`	INTEGER,
	`Articulo`	TEXT,
	`Cantidad`	REAL,
	`Almacen`	TEXT,
	`FechaInicioPrevista`	TEXT,
	`FechaFinPrevista`	TEXT,
	`Notas`	TEXT,
	`Coste`	REAL
);
CREATE TABLE IF NOT EXISTS `OrdenesProduccionDesglose` (
	`NumOrden`	INTEGER,
	`NumLinea`	INTEGER,
	`CodigoArticulo`	TEXT,
	`DescripcionArticulo`	TEXT,
	`Cantidad`	REAL,
	`Almacen`	TEXT
);
CREATE TABLE IF NOT EXISTS `PedidosCompra` (
	`NumPedido`	INTEGER,
	`Fecha`	TEXT,
	`CodigoProveedor`	TEXT,
	`Observaciones`	TEXT,
	`Servido`	TEXT,
	`FechaEntrega`	TEXT,
	`IVASN`	TEXT,
	`REQSN`	TEXT,
	`Sumadesglose`	REAL,
	`_DescuentoPP`	REAL,
	`Descuentopp`	REAL,
	`SumaIVA`	REAL,
	`SumaREQ`	REAL,
	`TotalFactura`	REAL,
	`BaseIVA1`	REAL,
	`IVA1`	REAL,
	`REQ1`	REAL,
	`ImporteIVA1`	REAL,
	`BaseIVA2`	REAL,
	`IVA2`	REAL,
	`REQ2`	REAL,
	`ImporteIVA2`	REAL,
	`BaseIVA3`	REAL,
	`IVA3`	REAL,
	`REQ3`	REAL,
	`ImporteIVA3`	REAL
);
CREATE TABLE IF NOT EXISTS `PedidosCompraDesglose` (
	`NumPedido`	INTEGER,
	`Numlinea`	INTEGER,
	`CodigoArticulo`	TEXT,
	`DescripcionArticulo`	TEXT,
	`Cantidad`	REAL,
	`PVP`	REAL,
	`Descuento`	REAL,
	`PrecioUnitario`	REAL,
	`Subtotal`	REAL,
	`TIVA`	INTEGER
);
CREATE TABLE IF NOT EXISTS `PedidosVenta` (
	`NumPedido`	INTEGER,
	`Fecha`	TEXT,
	`CodigoCliente`	TEXT,
	`Observaciones`	TEXT,
	`Servido`	TEXT,
	`FechaEntrega`	TEXT,
	`NumPresupuesto`	TEXT,
	`IVASN`	TEXT,
	`REQSN`	TEXT,
	`Sumadesglose`	TEXT,
	`_DescuentoPP`	TEXT,
	`Descuentopp`	TEXT,
	`SumaIVA`	TEXT,
	`SumaREQ`	TEXT,
	`TotalFactura`	TEXT,
	`BaseIVA1`	TEXT,
	`IVA1`	TEXT,
	`REQ1`	TEXT,
	`ImporteIVA1`	TEXT,
	`BaseIVA2`	TEXT,
	`IVA2`	TEXT,
	`REQ2`	TEXT,
	`ImporteIVA2`	TEXT,
	`BaseIVA3`	TEXT,
	`IVA3`	TEXT,
	`REQ3`	TEXT,
	`ImporteIVA3`	TEXT
);
CREATE TABLE IF NOT EXISTS `PedidosVentaDesglose` (
	`NumPedido`	INTEGER,
	`Numlinea`	INTEGER,
	`CodigoArticulo`	TEXT,
	`DescripcionArticulo`	TEXT,
	`Cantidad`	REAL,
	`PVP`	REAL,
	`Descuento`	REAL,
	`PrecioUnitario`	REAL,
	`Subtotal`	REAL,
	`TIVA`	INTEGER
);
CREATE TABLE IF NOT EXISTS `Presupuestos` (
	`NumPresupuesto`	INTEGER,
	`Fecha`	TEXT,
	`IVASN`	TEXT,
	`REQSN`	TEXT,
	`Observaciones`	TEXT,
	`Sumadesglose`	REAL,
	`_DescuentoPP`	REAL,
	`Descuentopp`	REAL,
	`SumaIVA`	REAL,
	`SumaREQ`	REAL,
	`TotalPresupuesto`	REAL,
	`Agente`	TEXT,
	`BaseIVA1`	REAL,
	`IVA1`	REAL,
	`REQ1`	REAL,
	`ImporteIVA1`	REAL,
	`BaseIVA2`	REAL,
	`IVA2`	REAL,
	`REQ2`	REAL,
	`ImporteIVA2`	REAL,
	`BaseIVA3`	REAL,
	`IVA3`	REAL,
	`REQ3`	REAL,
	`ImporteIVA3`	REAL,
	`Aceptado`	TEXT,
	`CodigoCliente`	TEXT
);
CREATE TABLE IF NOT EXISTS `PresupuestosDesglose` (
	`NumPresupuesto`	INTEGER,
	`NumLinea`	INTEGER,
	`DescripcionArticulo`	TEXT,
	`Cantidad`	REAL,
	`PVP`	REAL,
	`Descuento`	REAL,
	`PrecioUnitario`	REAL,
	`Subtotal`	REAL,
	`TIVA`	INTEGER,
	`CodigoArticulo`	TEXT
);
CREATE TABLE IF NOT EXISTS `Proveedores` (
	`Codigo`	TEXT,
	`CodigoDeReferencia`	TEXT,
	`Nombre`	TEXT,
	`Domicilio`	TEXT,
	`Domicilio2`	TEXT,
	`Poblacion`	TEXT,
	`CodigoPostal`	TEXT,
	`Provincia`	TEXT,
	`PersonaContacto`	TEXT,
	`Telefono`	TEXT,
	`Fax`	TEXT,
	`CIF`	TEXT,
	`IVASN`	TEXT,
	`REQSN`	TEXT,
	`SubcuentaCliente`	INTEGER,
	`SubcuentaProveedor`	INTEGER,
	`TipoDescuento`	INTEGER,
	`Tarifa`	INTEGER,
	`SectoresActividad`	TEXT,
	`FormaPago`	TEXT,
	`Agente`	TEXT,
	`Riesgo`	REAL,
	`Banco`	TEXT,
	`DireccionBanco`	TEXT,
	`PoblacionBanco`	TEXT,
	`ProvinciaBanco`	TEXT,
	`AgenciaBanco`	TEXT,
	`CuentaBanco`	TEXT,
	`Notas`	TEXT
);
CREATE TABLE IF NOT EXISTS `Provincias` (
	`CodigoProvincia`	TEXT,
	`NombreProvincia`	TEXT,
	`Zona`	TEXT
);
CREATE TABLE IF NOT EXISTS `Sectores` (
	`CodigoSector`	TEXT,
	`DescripcionSector`	TEXT
);
CREATE TABLE IF NOT EXISTS `StockMovimientos` (
	`Codigo`	INTEGER,
	`Fecha`	TEXT,
	`CodigoArticulo`	TEXT,
	`Cantidad`	REAL,
	`AlmacenOrigen`	TEXT,
	`AlmacenDestino`	TEXT,
	`Observaciones`	TEXT
);
CREATE TABLE IF NOT EXISTS `StockRegularizaciones` (
	`Codigo`	INTEGER,
	`Fecha`	TEXT,
	`CodigoArticulo`	TEXT,
	`Cantidad`	REAL,
	`Almacen`	TEXT,
	`Observaciones`	TEXT
);
CREATE TABLE IF NOT EXISTS `Subcuentas` (
	`Codigosubcuenta`	INTEGER,
	`TITULO`	TEXT,
	`SaldoDebe`	REAL,
	`SaldoHaber`	REAL,
	`SaldoDebe2`	REAL,
	`SaldoHaber2`	REAL
);
CREATE TABLE IF NOT EXISTS `Suministros` (
	`Proveedor`	TEXT,
	`Articulo`	TEXT,
	`Coste`	REAL,
	`Descuento`	REAL,
	`PlazoEntrega`	INTEGER
);
CREATE TABLE IF NOT EXISTS `TablaAuxiliar` (
	`CodigoTabla`	TEXT,
	`Descripcion`	TEXT,
	`DescripcionFilas`	TEXT,
	`DescripcionColumnas`	TEXT,
	`CodigoArticulo`	TEXT,
	`DescripcionArticulo`	TEXT,
	`Unidad`	TEXT,
	`FamiliaArticulo`	TEXT,
	`TipodeIVA`	INTEGER,
	`SubCuentaVentas`	INTEGER,
	`SubCuentaCompras`	INTEGER,
	`UsoInterno`	TEXT,
	`Compuesto`	TEXT,
	`Servicio`	TEXT,
	`CosteArticulo`	REAL,
	`PVP1`	REAL,
	`PVP2`	REAL,
	`PVP3`	REAL,
	`PVP4`	REAL,
	`PVP5`	REAL,
	`MinimoStock`	INTEGER,
	`MaximoStock`	INTEGER,
	`MinStockPorAlmacen`	INTEGER,
	`MaxStockPorAlmacen`	INTEGER,
	`Descuento1`	REAL,
	`Descuento2`	REAL,
	`Descuento3`	REAL,
	`Descuento4`	REAL,
	`Descuento5`	REAL,
	`ProveedorDefecto`	TEXT,
	`DiasFabricacion`	REAL
);
CREATE TABLE IF NOT EXISTS `TablaAuxiliarColumna` (
	`CodigoTabla`	TEXT,
	`NumeroColumna`	INTEGER,
	`Descripcion`	TEXT,
	`DigitosGeneracion`	TEXT
);
CREATE TABLE IF NOT EXISTS `TablaAuxiliarFila` (
	`CodigoTabla`	TEXT,
	`NumeroFila`	INTEGER,
	`Descripcion`	TEXT,
	`DigitosGeneracion`	TEXT
);
CREATE TABLE IF NOT EXISTS `TablaSaldosAgrupaciones` (
	`Agrupacion`	INTEGER,
	`Signo`	INTEGER,
	`Importe`	REAL
);
CREATE TABLE IF NOT EXISTS `TiposdeIVA` (
	`Codigo_Tipo_de_IVA`	INTEGER,
	`Porcentaje_de_IVA`	REAL,
	`Porcentaje_de_REQ`	REAL,
	`SubcuentaIVASoportado`	INTEGER,
	`SubcuentaIVARepercutido`	INTEGER,
	`SubcuentaREQRepercutido`	INTEGER
);
CREATE TABLE IF NOT EXISTS `WkAlbaranesCompra` (
	`NumAlbaran`	INTEGER,
	`NumLinea`	INTEGER,
	`CodigoArticulo`	TEXT,
	`DescripcionArticulo`	TEXT,
	`Cantidad`	REAL,
	`PVP`	REAL,
	`Descuento`	REAL,
	`PrecioUnitario`	REAL,
	`Subtotal`	REAL,
	`TIVA`	REAL
);
CREATE TABLE IF NOT EXISTS `WkAlbaranesVenta` (
	`NumAlbaran`	INTEGER,
	`NumLinea`	INTEGER,
	`CodigoArticulo`	TEXT,
	`DescripcionArticulo`	TEXT,
	`Cantidad`	REAL,
	`PVP`	REAL,
	`Descuento`	REAL,
	`PrecioUnitario`	REAL,
	`Subtotal`	REAL,
	`TIVA`	INTEGER
);
CREATE TABLE IF NOT EXISTS `WkAlbaranesVentaFacturar` (
	`NumAlbaran`	INTEGER,
	`NumFactura`	INTEGER,
	`NumPedido`	INTEGER,
	`Fecha`	TEXT,
	`IVASN`	TEXT,
	`REQSN`	TEXT,
	`CodigoCliente`	TEXT,
	`CodigoAlmacen`	TEXT,
	`Observaciones`	TEXT,
	`Sumadesglose`	REAL,
	`_DescuentoPP`	REAL,
	`Descuentopp`	REAL,
	`SumaIVA`	REAL,
	`SumaREQ`	REAL,
	`TotalFactura`	REAL,
	`BaseIVA1`	REAL,
	`IVA1`	REAL,
	`REQ1`	REAL,
	`ImporteIVA1`	REAL,
	`BaseIVA2`	REAL,
	`IVA2`	REAL,
	`REQ2`	REAL,
	`ImporteIVA2`	REAL,
	`BaseIVA3`	REAL,
	`IVA3`	REAL,
	`REQ3`	REAL,
	`ImporteIVA3`	REAL,
	`DireccionEnvio`	INTEGER
);
CREATE TABLE IF NOT EXISTS `WkAlbaranesVentaSinFacturar` (
	`NumAlbaran`	INTEGER,
	`NumFactura`	INTEGER,
	`NumPedido`	INTEGER,
	`Fecha`	TEXT,
	`IVASN`	TEXT,
	`REQSN`	TEXT,
	`CodigoCliente`	TEXT,
	`CodigoAlmacen`	TEXT,
	`Observaciones`	TEXT,
	`Sumadesglose`	REAL,
	`_DescuentoPP`	INTEGER,
	`Descuentopp`	REAL,
	`SumaIVA`	REAL,
	`SumaREQ`	REAL,
	`TotalFactura`	REAL,
	`BaseIVA1`	REAL,
	`IVA1`	REAL,
	`REQ1`	REAL,
	`ImporteIVA1`	REAL,
	`BaseIVA2`	REAL,
	`IVA2`	REAL,
	`REQ2`	REAL,
	`ImporteIVA2`	REAL,
	`BaseIVA3`	REAL,
	`IVA3`	REAL,
	`REQ3`	REAL,
	`ImporteIVA3`	REAL,
	`DireccionEnvio`	INTEGER
);
CREATE TABLE IF NOT EXISTS `WkCobrosARemesar` (
	`NumCobro`	INTEGER,
	`CodigoCliente`	TEXT,
	`FechaVencimiento`	TEXT,
	`Importe`	REAL,
	`Devuelto`	TEXT
);
CREATE TABLE IF NOT EXISTS `WkCobrosSinRemesar` (
	`NumCobro`	INTEGER,
	`CodigoCliente`	TEXT,
	`FechaVencimiento`	TEXT,
	`Importe`	REAL,
	`Devuelto`	TEXT
);
CREATE TABLE IF NOT EXISTS `WkDevoluciones` (
	`NumLote`	INTEGER,
	`TipoOperacion`	TEXT,
	`FechaSoporte`	TEXT,
	`FechaDevolucion`	TEXT,
	`NumEfecto`	REAL,
	`NumCobro`	REAL,
	`FechaPresentacion`	TEXT,
	`NumRemesa`	REAL,
	`ImporteImpagado`	REAL,
	`Importe`	REAL,
	`Vencimiento`	TEXT,
	`IdentificativoEfecto`	INTEGER
);
CREATE TABLE IF NOT EXISTS `WkDevoluciones19` (
	`NumOrden`	INTEGER,
	`CodigoCliente`	TEXT,
	`NombreCliente`	TEXT,
	`Importe`	REAL,
	`CodigoRemesa`	INTEGER,
	`NumCobro`	INTEGER,
	`Concepto`	TEXT,
	`Motivo`	TEXT,
	`Entidad`	TEXT,
	`Oficina`	TEXT,
	`DC`	TEXT,
	`Cuenta`	TEXT
);
CREATE TABLE IF NOT EXISTS `WkDevoluciones58` (
	`NumOrden`	INTEGER,
	`CodigoCliente`	TEXT,
	`NombreCliente`	TEXT,
	`Entidad`	TEXT,
	`Oficina`	TEXT,
	`DC`	TEXT,
	`Cuenta`	TEXT,
	`Importe`	REAL,
	`CodigoRemesa`	INTEGER,
	`NumCobro`	INTEGER,
	`Concepto`	TEXT,
	`Motivo`	TEXT,
	`Vencimiento`	TEXT
);
CREATE TABLE IF NOT EXISTS `WkEntregasyRecepcionesPendientes` (
	`NumPedido`	INTEGER,
	`Fecha`	TEXT,
	`Empresa`	TEXT,
	`FechaEntrega`	TEXT,
	`CodigoArticulo`	TEXT,
	`CantidadPedido`	REAL,
	`CantidadRecibir`	REAL,
	`CantidadEntregar`	REAL,
	`Stock`	REAL
);
CREATE TABLE IF NOT EXISTS `WkFacturasCompra` (
	`NumFactura`	INTEGER,
	`NumLinea`	INTEGER,
	`CodigoArticulo`	TEXT,
	`DescripcionArticulo`	TEXT,
	`Cantidad`	REAL,
	`PVP`	REAL,
	`Descuento`	REAL,
	`PrecioUnitario`	REAL,
	`Subtotal`	REAL,
	`TIVA`	INTEGER,
	`NumAlbaran`	INTEGER,
	`Fecha`	TEXT
);
CREATE TABLE IF NOT EXISTS `WkFacturasVenta` (
	`NumFactura`	INTEGER,
	`NumLinea`	INTEGER,
	`CodigoArticulo`	TEXT,
	`DescripcionArticulo`	TEXT,
	`Cantidad`	REAL,
	`PVP`	REAL,
	`Descuento`	REAL,
	`PrecioUnitario`	REAL,
	`Subtotal`	REAL,
	`TIVA`	INTEGER,
	`NumAlbaran`	INTEGER,
	`Fecha`	TEXT
);
CREATE TABLE IF NOT EXISTS `WkMayorCuentas` (
	`Asiento`	INTEGER,
	`Fila`	INTEGER,
	`Fecha`	TEXT,
	`Subcuenta`	INTEGER,
	`Concepto`	TEXT,
	`DebePesetas`	REAL,
	`HaberPesetas`	REAL,
	`Documento`	TEXT,
	`Punteo`	TEXT,
	`Renumerado`	INTEGER,
	`Saldo`	REAL
);
CREATE TABLE IF NOT EXISTS `WkOPComponentesSinStock` (
	`CodigoArticulo`	TEXT,
	`DescripcionArticulo`	TEXT,
	`CantidadNecesaria`	REAL,
	`Almacen`	TEXT,
	`Producir`	TEXT,
	`Proveedor`	TEXT,
	`CantidadPedir`	REAL,
	`MinimoStock`	REAL
);
CREATE TABLE IF NOT EXISTS `WkOPEstadoMateriales` (
	`CodigoArticulo`	TEXT,
	`DescripcionArticulo`	TEXT,
	`Cantidad`	REAL,
	`Almacen`	TEXT,
	`StockAlmacen`	REAL,
	`StockTotal`	REAL,
	`MinimoStock`	REAL,
	`CantidadRecibir`	REAL,
	`CantidadEntregar`	REAL
);
CREATE TABLE IF NOT EXISTS `WkOrdenantes` (
	`NumLote`	INTEGER,
	`FechaSoporte`	TEXT,
	`IdentificacionCedente`	TEXT,
	`ImportesDevueltos`	REAL,
	`ImportesNominales`	REAL,
	`NumeroRegistros`	REAL,
	`NumeroRecibos`	REAL,
	`EntidadAdeudo`	TEXT,
	`OficinaAdeudo`	TEXT,
	`DCAdeudo`	TEXT,
	`CuentaAdeudo`	TEXT
);
CREATE TABLE IF NOT EXISTS `WkOrdenantes19` (
	`NumOrden`	INTEGER,
	`FechaAdeudo`	TEXT,
	`NombreCliente`	TEXT,
	`SumaImportes`	REAL,
	`TotalDevoluciones`	INTEGER,
	`CodigoOrdenante`	TEXT,
	`Entidad`	TEXT,
	`Oficina`	TEXT,
	`DC`	TEXT,
	`Cuenta`	TEXT
);
CREATE TABLE IF NOT EXISTS `WkOrdenantes58` (
	`NumOrden`	INTEGER,
	`CodigoOrdenante`	TEXT,
	`NombreCliente`	TEXT,
	`Entidad`	TEXT,
	`Oficina`	TEXT,
	`DC`	TEXT,
	`Cuenta`	TEXT,
	`SumaImportes`	REAL,
	`TotalDevoluciones`	INTEGER
);
CREATE TABLE IF NOT EXISTS `WkOrdenesProduccion` (
	`NumOrden`	INTEGER,
	`NumLinea`	INTEGER,
	`CodigoArticulo`	TEXT,
	`DescripcionArticulo`	TEXT,
	`Cantidad`	REAL,
	`Almacen`	TEXT
);
CREATE TABLE IF NOT EXISTS `WkPedidosCompra` (
	`NumPedido`	INTEGER,
	`Numlinea`	INTEGER,
	`CodigoArticulo`	TEXT,
	`DescripcionArticulo`	TEXT,
	`Cantidad`	REAL,
	`PVP`	REAL,
	`Descuento`	REAL,
	`PrecioUnitario`	REAL,
	`Subtotal`	REAL,
	`TIVA`	INTEGER
);
CREATE TABLE IF NOT EXISTS `WkPedidosCompraOtraMoneda` (
	`NumPedido`	INTEGER,
	`Fecha`	TEXT,
	`CodigoProveedor`	TEXT,
	`Observaciones`	TEXT,
	`Servido`	TEXT,
	`FechaEntrega`	TEXT,
	`IVASN`	TEXT,
	`REQSN`	TEXT,
	`Sumadesglose`	REAL,
	`_DescuentoPP`	REAL,
	`Descuentopp`	REAL,
	`SumaIVA`	REAL,
	`SumaREQ`	REAL,
	`TotalFactura`	REAL,
	`BaseIVA1`	REAL,
	`IVA1`	REAL,
	`REQ1`	REAL,
	`ImporteIVA1`	REAL,
	`BaseIVA2`	REAL,
	`IVA2`	REAL,
	`REQ2`	REAL,
	`ImporteIVA2`	REAL,
	`BaseIVA3`	REAL,
	`IVA3`	REAL,
	`REQ3`	REAL,
	`ImporteIVA3`	REAL
);
CREATE TABLE IF NOT EXISTS `WkPedidosCompraServidos` (
	`NumPedido`	INTEGER,
	`CodigoArticulo`	TEXT,
	`DescripcionArticulo`	TEXT,
	`CantidadPedido`	REAL,
	`PVP`	REAL,
	`Descuento`	REAL,
	`CantidadEntregada`	REAL,
	`CantidadPendiente`	REAL
);
CREATE TABLE IF NOT EXISTS `WkPedidosCompraServir` (
	`NumPedido`	INTEGER,
	`CodigoArticulo`	TEXT,
	`DescripcionArticulo`	TEXT,
	`Cantidad`	REAL,
	`PVP`	REAL,
	`Descuento`	REAL,
	`PrecioUnitario`	REAL,
	`Subtotal`	REAL
);
CREATE TABLE IF NOT EXISTS `WkPedidosVenta` (
	`NumPedido`	INTEGER,
	`Numlinea`	TEXT,
	`CodigoArticulo`	TEXT,
	`DescripcionArticulo`	TEXT,
	`Cantidad`	TEXT,
	`PVP`	REAL,
	`Descuento`	REAL,
	`PrecioUnitario`	TEXT,
	`Subtotal`	TEXT,
	`TIVA`	TEXT
);
CREATE TABLE IF NOT EXISTS `WkPedidosVentaOtraMoneda` (
	`NumPedido`	INTEGER,
	`Fecha`	TEXT,
	`CodigoCliente`	TEXT,
	`Observaciones`	TEXT,
	`Servido`	TEXT,
	`FechaEntrega`	TEXT,
	`NumPresupuesto`	INTEGER,
	`IVASN`	TEXT,
	`REQSN`	TEXT,
	`Sumadesglose`	REAL,
	`_DescuentoPP`	REAL,
	`Descuentopp`	REAL,
	`SumaIVA`	REAL,
	`SumaREQ`	REAL,
	`TotalFactura`	REAL,
	`BaseIVA1`	REAL,
	`IVA1`	REAL,
	`REQ1`	REAL,
	`ImporteIVA1`	REAL,
	`BaseIVA2`	REAL,
	`IVA2`	REAL,
	`REQ2`	REAL,
	`ImporteIVA2`	REAL,
	`BaseIVA3`	REAL,
	`IVA3`	REAL,
	`REQ3`	REAL,
	`ImporteIVA3`	REAL
);
CREATE TABLE IF NOT EXISTS `WkPedidosVentaServidos` (
	`NumPedido`	INTEGER,
	`CodigoArticulo`	TEXT,
	`DescripcionArticulo`	TEXT,
	`CantidadPedido`	REAL,
	`PVP`	REAL,
	`Descuento`	REAL,
	`CantidadEntregada`	REAL,
	`CantidadPendiente`	REAL
);
CREATE TABLE IF NOT EXISTS `WkPedidosVentaServir` (
	`NumPedido`	INTEGER,
	`CodigoArticulo`	TEXT,
	`DescripcionArticulo`	TEXT,
	`Cantidad`	REAL,
	`PVP`	REAL,
	`Descuento`	REAL,
	`PrecioUnitario`	REAL,
	`Subtotal`	REAL
);
CREATE TABLE IF NOT EXISTS `WkPresupuestos` (
	`NumPresupuesto`	INTEGER,
	`NumLinea`	INTEGER,
	`DescripcionArticulo`	TEXT,
	`Cantidad`	REAL,
	`PVP`	REAL,
	`Descuento`	REAL,
	`PrecioUnitario`	REAL,
	`Subtotal`	REAL,
	`TIVA`	INTEGER,
	`CodigoArticulo`	TEXT
);
CREATE TABLE IF NOT EXISTS `WkPresupuestosOtraMoneda` (
	`NumPresupuesto`	INTEGER,
	`Fecha`	TEXT,
	`IVASN`	TEXT,
	`REQSN`	TEXT,
	`CodigoCliente`	TEXT,
	`Observaciones`	TEXT,
	`Sumadesglose`	REAL,
	`_DescuentoPP`	REAL,
	`Descuentopp`	REAL,
	`SumaIVA`	REAL,
	`SumaREQ`	REAL,
	`TotalPresupuesto`	REAL,
	`Agente`	TEXT,
	`BaseIVA1`	REAL,
	`IVA1`	REAL,
	`REQ1`	REAL,
	`ImporteIVA1`	REAL,
	`BaseIVA2`	REAL,
	`IVA2`	REAL,
	`REQ2`	REAL,
	`ImporteIVA2`	REAL,
	`BaseIVA3`	REAL,
	`IVA3`	REAL,
	`REQ3`	REAL,
	`ImporteIVA3`	REAL,
	`Aceptado`	TEXT
);
CREATE TABLE IF NOT EXISTS `WkRechazados` (
	`NumCobro`	INTEGER,
	`FechaSoporte`	TEXT,
	`NumRemesa`	INTEGER,
	`NumProvinciaLibramiento`	TEXT,
	`CodigoINEPlazaLibramiento`	TEXT,
	`PlazaLibramiento`	TEXT,
	`Importe`	REAL,
	`Vencimiento`	TEXT,
	`TipoDocumento`	INTEGER,
	`FechaExpedicion`	TEXT,
	`Nombre`	TEXT,
	`CodigoAcepto`	INTEGER,
	`ClausulaGastos`	INTEGER,
	`Entidad`	TEXT,
	`Oficina`	TEXT,
	`DC`	TEXT,
	`Cuenta`	TEXT,
	`TipoError`	TEXT,
	`Domicilio`	TEXT,
	`CPPlazaLibrada`	TEXT,
	`PlazaLibrada`	TEXT,
	`NumProvinciaPlazaLibrada`	TEXT,
	`CodigoINEPlazaLibrada`	TEXT,
	`Motivo`	TEXT
);
CREATE TABLE IF NOT EXISTS `WkStockDetallado` (
	`Fecha`	TEXT,
	`CodigoArticulo`	TEXT,
	`Coste`	REAL,
	`Cantidad`	REAL,
	`CodigoAlmacen`	TEXT,
	`Tipo`	TEXT
);
CREATE TABLE IF NOT EXISTS `Zonas` (
	`Codigo_de_zona`	TEXT,
	`Descripcion_Zona`	TEXT
);
CREATE TABLE IF NOT EXISTS `balances` (
	`Agrupacion`	INTEGER,
	`Titulo`	TEXT,
	`SaldoN`	REAL,
	`SaldoN_1`	REAL
);
CREATE TABLE IF NOT EXISTS `balancescuentas` (
	`Agrupacion`	INTEGER,
	`CodigoCuenta`	INTEGER,
	`Signo`	INTEGER
);
CREATE TABLE IF NOT EXISTS `querysaldos` (
	`Subcuenta`	INTEGER,
	`SumaDebe`	REAL,
	`SumaHaber`	REAL
);
CREATE TABLE IF NOT EXISTS `wKFormasDePago` (
	`CodigoFormaPago`	TEXT,
	`D_asFechaFactura`	INTEGER,
	`D_adePago`	INTEGER,
	`PorcentajeFactura`	REAL
);
CREATE TABLE IF NOT EXISTS `wkAlbaranesCompraFacturar` (
	`NumAlbaran`	INTEGER,
	`NumFactura`	INTEGER,
	`NumPedido`	INTEGER,
	`Fecha`	TEXT,
	`IVASN`	TEXT,
	`REQSN`	TEXT,
	`CodigoProveedor`	TEXT,
	`CodigoAlmacen`	TEXT,
	`Observaciones`	TEXT,
	`Sumadesglose`	REAL,
	`_DescuentoPP`	REAL,
	`Descuentopp`	REAL,
	`SumaIVA`	REAL,
	`SumaREQ`	REAL,
	`TotalFactura`	REAL,
	`BaseIVA1`	REAL,
	`IVA1`	REAL,
	`REQ1`	REAL,
	`ImporteIVA1`	REAL,
	`BaseIVA2`	REAL,
	`IVA2`	REAL,
	`REQ2`	REAL,
	`ImporteIVA2`	REAL,
	`BaseIVA3`	REAL,
	`IVA3`	REAL,
	`REQ3`	REAL,
	`ImporteIVA3`	REAL,
	`DireccionEnvio`	INTEGER
);
CREATE TABLE IF NOT EXISTS `wkAlbaranesCompraOtraMoneda` (
	`NumAlbaran`	INTEGER,
	`NumFactura`	INTEGER,
	`NumPedido`	INTEGER,
	`Fecha`	TEXT,
	`IVASN`	TEXT,
	`REQSN`	TEXT,
	`CodigoProveedor`	TEXT,
	`CodigoAlmacen`	TEXT,
	`Observaciones`	TEXT,
	`Sumadesglose`	REAL,
	`_DescuentoPP`	REAL,
	`Descuentopp`	REAL,
	`SumaIVA`	REAL,
	`SumaREQ`	REAL,
	`TotalFactura`	REAL,
	`BaseIVA1`	REAL,
	`IVA1`	REAL,
	`REQ1`	REAL,
	`ImporteIVA1`	REAL,
	`BaseIVA2`	REAL,
	`IVA2`	REAL,
	`REQ2`	REAL,
	`ImporteIVA2`	REAL,
	`BaseIVA3`	REAL,
	`IVA3`	REAL,
	`REQ3`	REAL,
	`ImporteIVA3`	REAL
);
CREATE TABLE IF NOT EXISTS `wkAlbaranesCompraSinFacturar` (
	`NumAlbaran`	INTEGER,
	`NumFactura`	INTEGER,
	`NumPedido`	INTEGER,
	`Fecha`	TEXT,
	`IVASN`	TEXT,
	`REQSN`	TEXT,
	`CodigoProveedor`	TEXT,
	`CodigoAlmacen`	TEXT,
	`Observaciones`	TEXT,
	`Sumadesglose`	REAL,
	`_DescuentoPP`	INTEGER,
	`Descuentopp`	REAL,
	`SumaIVA`	REAL,
	`SumaREQ`	REAL,
	`TotalFactura`	REAL,
	`BaseIVA1`	REAL,
	`IVA1`	REAL,
	`REQ1`	REAL,
	`ImporteIVA1`	REAL,
	`BaseIVA2`	REAL,
	`IVA2`	REAL,
	`REQ2`	REAL,
	`ImporteIVA2`	REAL,
	`BaseIVA3`	REAL,
	`IVA3`	REAL,
	`REQ3`	REAL,
	`ImporteIVA3`	REAL,
	`DireccionEnvio`	INTEGER
);
CREATE TABLE IF NOT EXISTS `wkAlbaranesVentaOtraMoneda` (
	`NumAlbaran`	INTEGER,
	`NumFactura`	INTEGER,
	`NumPedido`	INTEGER,
	`Fecha`	TEXT,
	`IVASN`	TEXT,
	`REQSN`	TEXT,
	`CodigoCliente`	TEXT,
	`CodigoAlmacen`	TEXT,
	`Observaciones`	TEXT,
	`Sumadesglose`	REAL,
	`_DescuentoPP`	REAL,
	`Descuentopp`	REAL,
	`SumaIVA`	REAL,
	`SumaREQ`	REAL,
	`TotalFactura`	REAL,
	`BaseIVA1`	REAL,
	`IVA1`	REAL,
	`REQ1`	REAL,
	`ImporteIVA1`	REAL,
	`BaseIVA2`	REAL,
	`IVA2`	REAL,
	`REQ2`	REAL,
	`ImporteIVA2`	REAL,
	`BaseIVA3`	REAL,
	`IVA3`	REAL,
	`REQ3`	REAL,
	`ImporteIVA3`	REAL,
	`DireccionEnvio`	INTEGER
);
CREATE TABLE IF NOT EXISTS `wkFacturasCompraOtraMoneda` (
	`NumFactura`	INTEGER,
	`NumAsiento`	INTEGER,
	`NumIVA`	INTEGER,
	`Fecha`	TEXT,
	`IVASN`	TEXT,
	`REQSN`	TEXT,
	`CodigoProveedor`	TEXT,
	`FormaPago`	TEXT,
	`Observaciones`	TEXT,
	`Sumadesglose`	REAL,
	`_DescuentoPP`	REAL,
	`Descuentopp`	REAL,
	`SumaIVA`	REAL,
	`SumaREQ`	REAL,
	`TotalFactura`	REAL,
	`Agente`	TEXT,
	`_Comision`	REAL,
	`Importe_Comision`	REAL,
	`BaseIVA1`	REAL,
	`IVA1`	REAL,
	`REQ1`	REAL,
	`ImporteIVA1`	REAL,
	`BaseIVA2`	REAL,
	`IVA2`	REAL,
	`REQ2`	REAL,
	`ImporteIVA2`	REAL,
	`BaseIVA3`	REAL,
	`IVA3`	REAL,
	`REQ3`	REAL,
	`ImporteIVA3`	REAL,
	`Referencia`	TEXT
);
CREATE TABLE IF NOT EXISTS `wkFacturasVentaOtraMoneda` (
	`NumFactura`	INTEGER,
	`NumAsiento`	INTEGER,
	`NumIVA`	INTEGER,
	`Fecha`	TEXT,
	`IVASN`	TEXT,
	`REQSN`	TEXT,
	`CodigoCliente`	TEXT,
	`FormaPago`	TEXT,
	`Observaciones`	TEXT,
	`Sumadesglose`	REAL,
	`_DescuentoPP`	REAL,
	`Descuentopp`	REAL,
	`SumaIVA`	REAL,
	`SumaREQ`	REAL,
	`TotalFactura`	REAL,
	`Agente`	TEXT,
	`_Comision`	REAL,
	`Importe_Comision`	REAL,
	`BaseIVA1`	REAL,
	`IVA1`	REAL,
	`REQ1`	REAL,
	`ImporteIVA1`	REAL,
	`BaseIVA2`	REAL,
	`IVA2`	REAL,
	`REQ2`	REAL,
	`ImporteIVA2`	REAL,
	`BaseIVA3`	REAL,
	`IVA3`	REAL,
	`REQ3`	REAL,
	`ImporteIVA3`	REAL,
	`Referencia`	TEXT
);
INSERT INTO `Almacenes` VALUES ('1','ARMADA ESPAÑOLA');
INSERT INTO `Articulos` VALUES ('0105','PAN REDONDO CASERO 1/2','PAN REDONDO CASERO 1/2','','02',1,'','','0','0','0',0.0,0.0,0.0,2.07,0.0,0.0,0.0,0.0,0,0,0,0,0.0,0.0,0.0,0.0,0.0,'',0.0,'','','');
INSERT INTO `Articulos` VALUES ('0109','BOCADILLO INTEGRAL','BOCADILLO INTEGRAL','','03',2,'','','0','0','0',0.0,0.0,0.0,0.58,0.0,0.0,0.0,0.0,0,0,0,0,0.0,0.0,0.0,0.0,0.0,'',0.0,'','','');
INSERT INTO `Articulos` VALUES ('0112','MEDIA DE BOMBON','MEDIA DE BOMBON','','03',2,'','','0','0','0',0.0,0.0,0.0,0.34,0.0,0.0,0.0,0.0,0,0,0,0,0.0,0.0,0.0,0.0,0.0,'',0.0,'','','');
INSERT INTO `Articulos` VALUES ('0113','MONTADITO DE BOMBON','MONTADITO DE BOMBON','','03',2,'','','0','0','0',0.0,0.0,0.0,0.25,0.0,0.0,0.0,0.0,0,0,0,0,0.0,0.0,0.0,0.0,0.0,'',0.0,'','','');
INSERT INTO `Articulos` VALUES ('0114','PULGUITA DE BOMBON','PULGUITA DE BOMBON','','03',2,'','','0','0','0',0.0,0.0,0.0,0.18,0.0,0.0,0.0,0.0,0,0,0,0,0.0,0.0,0.0,0.0,0.0,'',0.0,'','','');
INSERT INTO `Articulos` VALUES ('0121','PANECITO SOBADO 1/4','PANECITO SOBADO 1/4','','01',1,'','','0','0','0',0.0,0.0,0.0,1.25,0.0,0.0,0.0,0.0,0,0,0,0,0.0,0.0,0.0,0.0,0.0,'',0.0,'','','');
INSERT INTO `Articulos` VALUES ('0122','ROLLO SOBADO 1/4','ROLLO SOBADO 1/4','','01',1,'','','0','0','0',0.0,0.0,0.0,1.25,0.0,0.0,0.0,0.0,0,0,0,0,0.0,0.0,0.0,0.0,0.0,'',0.0,'','','');
INSERT INTO `Articulos` VALUES ('0123','BOCADILLO SOBADO','BOCADILLO SOBADO','','01',1,'','','0','0','0',0.0,0.0,0.0,0.63,0.0,0.0,0.0,0.0,0,0,0,0,0.0,0.0,0.0,0.0,0.0,'',0.0,'','','');
INSERT INTO `Articulos` VALUES ('0124','PAN REDONDO SOBADO MEDIO','PAN REDONDO SOBADO MEDIO','','01',1,'','','0','0','0',0.0,0.0,0.0,2.5,0.0,0.0,0.0,0.0,0,0,0,0,0.0,0.0,0.0,0.0,0.0,'',0.0,'','','');
INSERT INTO `Articulos` VALUES ('0125','BARRA SOBADO MEDIO','BARRA SOBADO MEDIO','','01',1,'','','0','0','0',0.0,0.0,0.0,2.5,0.0,0.0,0.0,0.0,0,0,0,0,0.0,0.0,0.0,0.0,0.0,'',0.0,'','','');
INSERT INTO `Articulos` VALUES ('0126','PIEZA SOBADA CON FORMA €/Kg','PIEZA SOBADA CON FORMA €/Kg','','01',1,'','','0','0','0',0.0,0.0,0.0,6.0,0.0,0.0,0.0,0.0,0,0,0,0,0.0,0.0,0.0,0.0,0.0,'',0.0,'','','');
INSERT INTO `Articulos` VALUES ('0129','PULGUITA DE PAN ','PULGUITA DE PAN ','','01',1,'','','0','0','0',0.0,0.0,0.0,0.24,0.0,0.0,0.0,0.0,0,0,0,0,0.0,0.0,0.0,0.0,0.0,'',0.0,'','','');
INSERT INTO `Articulos` VALUES ('0130','BAGUETTINA','BAGUETTINA','','01',1,'','','0','0','0',0.0,0.0,0.0,0.48,0.0,0.0,0.0,0.0,0,0,0,0,0.0,0.0,0.0,0.0,0.0,'',0.0,'','','');
INSERT INTO `Articulos` VALUES ('0132','MOLDE BOMBON','MOLDE BOMBON','','03',2,'','','0','0','0',0.0,0.0,0.0,2.4,0.0,0.0,0.0,0.0,0,0,0,0,0.0,0.0,0.0,0.0,0.0,'',0.0,'','','');
INSERT INTO `Articulos` VALUES ('0140','BARRA GALLEGA ','BARRA GALLEGA ','','02',1,'','','0','0','0',0.0,0.0,0.0,1.06,0.0,0.0,0.0,0.0,0,0,0,0,0.0,0.0,0.0,0.0,0.0,'',0.0,'','','');
INSERT INTO `Articulos` VALUES ('0110','BOCADILLO NORMAL','BOCADILLO NORMAL','','01',1,'','','0','0','0',0.0,0.0,0.0,0.48,0.0,0.0,0.0,0.0,0,0,0,0,0.0,0.0,0.0,0.0,0.0,'',0.0,'','','');
INSERT INTO `Articulos` VALUES ('0117','CHURRO PAN','CHURRO PAN','','01',1,'','','0','0','0',0.0,0.0,0.0,0.63,0.0,0.0,0.0,0.0,0,0,0,0,0.0,0.0,0.0,0.0,0.0,'',0.0,'','','');
INSERT INTO `Articulos` VALUES ('0118','CHURRO CASERO','CHURRO CASERO','','02',1,'','','0','0','0',0.0,0.0,0.0,0.67,0.0,0.0,0.0,0.0,0,0,0,0,0.0,0.0,0.0,0.0,0.0,'',0.0,'','','');
INSERT INTO `Articulos` VALUES ('0145','BARRA PAN CRISTAL','BARRA PAN CRISTAL','','03',2,'','','0','0','0',0.0,0.0,0.0,1.4,0.0,0.0,0.0,0.0,0,0,0,0,0.0,0.0,0.0,0.0,0.0,'',0.0,'','','');
INSERT INTO `Articulos` VALUES ('0146','CHAPATA CRISTAL PESO','CHAPATA CRISTAL PESO','','03',2,'','','0','0','0',0.0,0.0,0.0,7.0,0.0,0.0,0.0,0.0,0,0,0,0,0.0,0.0,0.0,0.0,0.0,'',0.0,'','','');
INSERT INTO `Articulos` VALUES ('0147','PAN REDONDO CRISTAL MEDIO','PAN REDONDO CRISTAL MEDIO','','03',2,'','','0','0','0',0.0,0.0,0.0,2.6,0.0,0.0,0.0,0.0,0,0,0,0,0.0,0.0,0.0,0.0,0.0,'',0.0,'','','');
INSERT INTO `Articulos` VALUES ('0148','PAN REDONDO CRISTAL KILO','PAN REDONDO CRISTAL KILO','','03',2,'','','0','0','0',0.0,0.0,0.0,6.0,0.0,0.0,0.0,0.0,0,0,0,0,0.0,0.0,0.0,0.0,0.0,'',0.0,'','','');
INSERT INTO `Articulos` VALUES ('0116','PAN DE HAMBURGUESA','PAN DE HAMBURGUESA','','03',2,'','','0','0','0',0.0,0.0,0.0,0.45,0.0,0.0,0.0,0.0,0,0,0,0,0.0,0.0,0.0,0.0,0.0,'',0.0,'','','');
INSERT INTO `Articulos` VALUES ('0115','BOMBON BOC.','BOMBON BOC.','','03',2,'','','0','0','0',0.0,0.0,0.0,0.48,0.0,0.0,0.0,0.0,0,0,0,0,0.0,0.0,0.0,0.0,0.0,'',0.0,'','','');
INSERT INTO `Articulos` VALUES ('0219','BOLLO SUIZO','BOLLO SUIZO','','04',2,'','','0','0','0',0.0,0.0,0.0,0.75,0.0,0.0,0.0,0.0,0,0,0,0,0.0,0.0,0.0,0.0,0.0,'',0.0,'','','');
INSERT INTO `Articulos` VALUES ('0213','KILO MADALENAS CHOCOLATE','KILO MADALENAS CHOCOLATE','','04',2,'','','0','0','0',0.0,0.0,0.0,7.5,0.0,0.0,0.0,0.0,0,0,0,0,0.0,0.0,0.0,0.0,0.0,'',0.0,'','','');
INSERT INTO `Articulos` VALUES ('0214','KILO MADALENAS MANZANA','KILO MADALENAS MANZANA','','04',2,'','','0','0','0',0.0,0.0,0.0,7.5,0.0,0.0,0.0,0.0,0,0,0,0,0.0,0.0,0.0,0.0,0.0,'',0.0,'','','');
INSERT INTO `Articulos` VALUES ('0206','FARTONS CASEROS','FARTONS CASEROS','','04',2,'','','0','0','0',0.0,0.0,0.0,0.45,0.0,0.0,0.0,0.0,0,0,0,0,0.0,0.0,0.0,0.0,0.0,'',0.0,'','','');
INSERT INTO `Articulos` VALUES ('1001','HARINA SACO `BUFORT`','HARINA SACO `BUFORT`','','10',1,'','','0','0','0',0.0,0.0,0.0,24.16,0.0,0.0,0.0,0.0,0,0,0,0,0.0,0.0,0.0,0.0,0.0,'',0.0,'','','');
INSERT INTO `Articulos` VALUES ('1002','LEVADURA PASTILLA 500G','LEVADURA PASTILLA 500G','','10',2,'','','0','0','0',0.0,0.0,0.0,2.97,0.0,0.0,0.0,0.0,0,0,0,0,0.0,0.0,0.0,0.0,0.0,'',0.0,'','','');
INSERT INTO `Articulos` VALUES ('0706','ROLLOS MORENOS 9 uds.','ROLLOS MORENOS 9 uds.','','07',2,'','','0','0','0',0.0,0.0,0.0,2.2,0.0,0.0,0.0,0.0,0,0,0,0,0.0,0.0,0.0,0.0,0.0,'',0.0,'','','');
INSERT INTO `Articulos` VALUES ('0705','ROLLOS MORENOS 6 uds.','ROLLOS MORENOS 6 uds.','','07',2,'','','0','0','0',0.0,0.0,0.0,1.5,0.0,0.0,0.0,0.0,0,0,0,0,0.0,0.0,0.0,0.0,0.0,'',0.0,'','','');
INSERT INTO `Articulos` VALUES ('0704','ROLLOS VINO-OLIVA KILO','ROLLOS VINO-OLIVA KILO','','07',2,'','','0','0','0',0.0,0.0,0.0,12.0,0.0,0.0,0.0,0.0,0,0,0,0,0.0,0.0,0.0,0.0,0.0,'',0.0,'','','');
INSERT INTO `Articulos` VALUES ('0703','ROLLOS INTEGRALES KILO','ROLLOS INTEGRALES KILO','','07',2,'','','0','0','0',0.0,0.0,0.0,10.0,0.0,0.0,0.0,0.0,0,0,0,0,0.0,0.0,0.0,0.0,0.0,'',0.0,'','','');
INSERT INTO `Articulos` VALUES ('0710','COOKIES CHOCOLATE KILO','COOKIES CHOCOLATE KILO','','07',2,'','','0','0','0',0.0,0.0,0.0,12.0,0.0,0.0,0.0,0.0,0,0,0,0,0.0,0.0,0.0,0.0,0.0,'',0.0,'','','');
INSERT INTO `Articulos` VALUES ('0205','TORTELL','TORTELL','','04',2,'','','0','0','0',0.0,0.0,0.0,1.36,0.0,0.0,0.0,0.0,0,0,0,0,0.0,0.0,0.0,0.0,0.0,'',0.0,'','','');
INSERT INTO `Articulos` VALUES ('0331','NAPOLITANA YORK/QUESO','NAPOLITANA YORK/QUESO','','05',2,'','','0','0','0',0.0,0.0,0.0,1.5,0.0,0.0,0.0,0.0,0,0,0,0,0.0,0.0,0.0,0.0,0.0,'',0.0,'','','');
INSERT INTO `Articulos` VALUES ('0332','REJILLAS POLLO','REJILLAS POLLO','','05',2,'','','0','0','0',0.0,0.0,0.0,1.5,0.0,0.0,0.0,0.0,0,0,0,0,0.0,0.0,0.0,0.0,0.0,'',0.0,'','','');
INSERT INTO `Articulos` VALUES ('0333','REJILLAS ESPINACAS','REJILLAS ESPINACAS','','05',2,'','','0','0','0',0.0,0.0,0.0,1.5,0.0,0.0,0.0,0.0,0,0,0,0,0.0,0.0,0.0,0.0,0.0,'',0.0,'','','');
INSERT INTO `Articulos` VALUES ('0334','REJILLAS JAMON Y QUESO','REJILLAS JAMON Y QUESO','','05',2,'','','0','0','0',0.0,0.0,0.0,1.5,0.0,0.0,0.0,0.0,0,0,0,0,0.0,0.0,0.0,0.0,0.0,'',0.0,'','','');
INSERT INTO `Articulos` VALUES ('0335','AGUJAS DE ATUN','AGUJAS DE ATUN','','05',2,'','','0','0','0',0.0,0.0,0.0,1.5,0.0,0.0,0.0,0.0,0,0,0,0,0.0,0.0,0.0,0.0,0.0,'',0.0,'','','');
INSERT INTO `Articulos` VALUES ('0160','BARRA CENTENO','BARRA CENTENO','','03',2,'','','0','0','0',0.0,0.0,0.0,1.2,0.0,0.0,0.0,0.0,0,0,0,0,0.0,0.0,0.0,0.0,0.0,'',0.0,'','','');
INSERT INTO `Articulos` VALUES ('0161','BARRA CEREALES','BARRA CEREALES','','03',2,'','','0','0','0',0.0,0.0,0.0,1.4,0.0,0.0,0.0,0.0,0,0,0,0,0.0,0.0,0.0,0.0,0.0,'',0.0,'','','');
INSERT INTO `Articulos` VALUES ('0165','PAN ESPELTA','PAN ESPELTA','','03',2,'','','0','0','0',0.0,0.0,0.0,4.2,0.0,0.0,0.0,0.0,0,0,0,0,0.0,0.0,0.0,0.0,0.0,'',0.0,'','','');
INSERT INTO `Articulos` VALUES ('0166','PAN TRIGO SARRACENO','PAN TRIGO SARRACENO','','03',2,'','','0','0','0',0.0,0.0,0.0,4.2,0.0,0.0,0.0,0.0,0,0,0,0,0.0,0.0,0.0,0.0,0.0,'',0.0,'','','');
INSERT INTO `Articulos` VALUES ('0230','PORCION PLANCHA CABELLO','PORCION PLANCHA CABELLO','','04',2,'','','0','0','0',0.0,0.0,0.0,1.2,0.0,0.0,0.0,0.0,0,0,0,0,0.0,0.0,0.0,0.0,0.0,'',0.0,'','','');
INSERT INTO `Articulos` VALUES ('0218','DONUT CHOCO ','DONUT CHOCO ','','04',2,'','','0','0','0',0.0,0.0,0.0,1.1,0.0,0.0,0.0,0.0,0,0,0,0,0.0,0.0,0.0,0.0,0.0,'',0.0,'','','');
INSERT INTO `Articulos` VALUES ('0217','DONUT BLANCO ','DONUT BLANCO ','','04',2,'','','0','0','0',0.0,0.0,0.0,0.8,0.0,0.0,0.0,0.0,0,0,0,0,0.0,0.0,0.0,0.0,0.0,'',0.0,'','','');
INSERT INTO `Articulos` VALUES ('0150','PAN DE MAIZ ','PAN DE MAIZ ','','03',2,'','','0','0','0',0.0,0.0,0.0,4.5,0.0,0.0,0.0,0.0,0,0,0,0,0.0,0.0,0.0,0.0,0.0,'',0.0,'','','');
INSERT INTO `Articulos` VALUES ('0302','PORCION COCA MOLLITAS','PORCION COCA MOLLITAS','','05',2,'','','0','0','0',0.0,0.0,0.0,0.91,1.0,0.0,0.0,0.0,0,0,0,0,0.0,0.0,0.0,0.0,0.0,'',0.0,'','','');
INSERT INTO `Articulos` VALUES ('0301','COCA MOLLITAS ENTERA','COCA MOLLITAS ENTERA','','05',2,'','','0','0','0',0.0,0.0,0.0,11.0,0.0,0.0,0.0,0.0,0,0,0,0,0.0,0.0,0.0,0.0,0.0,'',0.0,'','','');
INSERT INTO `Articulos` VALUES ('0304','PORCION COCA PISTO EMPANADA','PORCION COCA PISTO EMPANADA','','05',2,'','','0','0','0',0.0,0.0,0.0,1.18,1.3,0.0,0.0,0.0,0,0,0,0,0.0,0.0,0.0,0.0,0.0,'',0.0,'','','');
INSERT INTO `Articulos` VALUES ('0320','EMPANADILLA GRANDE PISTO','EMPANADILLA GRANDE PISTO','','05',2,'','','0','0','0',0.0,0.0,0.0,1.0,1.1,0.0,0.0,0.0,0,0,0,0,0.0,0.0,0.0,0.0,0.0,'',0.0,'','','');
INSERT INTO `Articulos` VALUES ('0330','KILO SALADITOS VARIADOS','KILO SALADITOS VARIADOS','','05',2,'','','0','0','0',0.0,0.0,0.0,14.0,0.0,0.0,0.0,0.0,0,0,0,0,0.0,0.0,0.0,0.0,0.0,'',0.0,'','','');
INSERT INTO `Articulos` VALUES ('0315','PIZZA BERENJENA BACON ENTERA','PIZZA BERENJENA BACON ENTERA','','05',2,'','','0','0','0',0.0,0.0,0.0,16.0,0.0,0.0,0.0,0.0,0,0,0,0,0.0,0.0,0.0,0.0,0.0,'',0.0,'','','');
INSERT INTO `Articulos` VALUES ('0317','COQUITAS VERDURA','COQUITAS VERDURA','','05',2,'','','0','0','0',0.0,0.0,0.0,1.7,0.0,0.0,0.0,0.0,0,0,0,0,0.0,0.0,0.0,0.0,0.0,'',0.0,'','','');
INSERT INTO `Articulos` VALUES ('0321','MINI EMPANADILLA ATUN/CEBOLLA','MINI EMPANADILLA ATUN/CEBOLLA','','05',2,'','','0','0','0',0.0,0.0,0.0,0.55,0.6,0.0,0.0,0.0,0,0,0,0,0.0,0.0,0.0,0.0,0.0,'',0.0,'','','');
INSERT INTO `Articulos` VALUES ('0322','MINI EMPANADILLA QUESO/BACON','MINI EMPANADILLA QUESO/BACON','','05',2,'','','0','0','0',0.0,0.0,0.0,0.55,0.6,0.0,0.0,0.0,0,0,0,0,0.0,0.0,0.0,0.0,0.0,'',0.0,'','','');
INSERT INTO `Articulos` VALUES ('0323','MINI EMPANADILLA Q.CABRA/CEB.CARAM.','MINI EMPANADILLA Q.CABRA/CEB.CARAM.','','05',2,'','','0','0','0',0.0,0.0,0.0,0.55,0.6,0.0,0.0,0.0,0,0,0,0,0.0,0.0,0.0,0.0,0.0,'',0.0,'','','');
INSERT INTO `Articulos` VALUES ('0324','MINI EMPANADILLA ESPINACAS','MINI EMPANADILLA ESPINACAS','','05',2,'','','0','0','0',0.0,0.0,0.0,0.55,0.6,0.0,0.0,0.0,0,0,0,0,0.0,0.0,0.0,0.0,0.0,'',0.0,'','','');
INSERT INTO `Articulos` VALUES ('0325','MINI EMPANADILLA QUESO','MINI EMPANADILLA QUESO','','05',2,'','','0','0','0',0.0,0.0,0.0,0.55,0.6,0.0,0.0,0.0,0,0,0,0,0.0,0.0,0.0,0.0,0.0,'',0.0,'','','');
INSERT INTO `Articulos` VALUES ('0326','MINI EMPANADILLA SOBRASADA','MINI EMPANADILLA SOBRASADA','','05',2,'','','0','0','0',0.0,0.0,0.0,0.55,0.6,0.0,0.0,0.0,0,0,0,0,0.0,0.0,0.0,0.0,0.0,'',0.0,'','','');
INSERT INTO `Articulos` VALUES ('0327','MINI EMPANADILLA MORCILLA','MINI EMPANADILLA MORCILLA','','05',2,'','','0','0','0',0.0,0.0,0.0,0.55,0.6,0.0,0.0,0.0,0,0,0,0,0.0,0.0,0.0,0.0,0.0,'',0.0,'','','');
INSERT INTO `Articulos` VALUES ('0138','PANECITO CASERO COMEDOR','PANECITO CASERO COMEDOR','','02',1,'','','0','0','0',0.0,0.0,0.0,0.72,0.0,0.0,0.0,0.0,0,0,0,0,0.0,0.0,0.0,0.0,0.0,'',0.0,'','','');
INSERT INTO `Articulos` VALUES ('0139','PANECITO CASERO COMED.PEQUEÑO','PANECITO CASERO COMED.PEQUEÑO','','02',1,'','','0','0','0',0.0,0.0,0.0,0.48,0.0,0.0,0.0,0.0,0,0,0,0,0.0,0.0,0.0,0.0,0.0,'',0.0,'','','');
INSERT INTO `Articulos` VALUES ('0135','BARRA ACEITE 1/4','BARRA ACEITE 1/4','','03',2,'','','0','0','0',0.0,0.0,0.0,1.3,0.0,0.0,0.0,0.0,0,0,0,0,0.0,0.0,0.0,0.0,0.0,'',0.0,'','','');
INSERT INTO `Articulos` VALUES ('0136','PAN ACEITE 1/2','PAN ACEITE 1/2','','03',2,'','','0','0','0',0.0,0.0,0.0,2.6,0.0,0.0,0.0,0.0,0,0,0,0,0.0,0.0,0.0,0.0,0.0,'',0.0,'','','');
INSERT INTO `Articulos` VALUES ('0231','ROSCON PEQUEÑO','ROSCON PEQUEÑO','','04',2,'','','0','0','0',0.0,0.0,0.0,11.82,13.0,0.0,0.0,0.0,0,0,0,0,0.0,0.0,0.0,0.0,0.0,'',0.0,'','','');
INSERT INTO `Articulos` VALUES ('0232','ROSCON MEDIANO','ROSCON MEDIANO','','04',2,'','','0','0','0',0.0,0.0,0.0,15.45,17.0,0.0,0.0,0.0,0,0,0,0,0.0,0.0,0.0,0.0,0.0,'',0.0,'','','');
INSERT INTO `Articulos` VALUES ('0233','ROSCON GRANDE','ROSCON GRANDE','','04',2,'','','0','0','0',0.0,0.0,0.0,20.45,22.5,0.0,0.0,0.0,0,0,0,0,0.0,0.0,0.0,0.0,0.0,'',0.0,'','','');
INSERT INTO `Articulos` VALUES ('0234','ROSCON PEQUEÑO RELLENO','ROSCON PEQUEÑO RELLENO','','04',2,'','','0','0','0',0.0,0.0,0.0,13.64,15.0,0.0,0.0,0.0,0,0,0,0,0.0,0.0,0.0,0.0,0.0,'',0.0,'','','');
INSERT INTO `Articulos` VALUES ('0235','ROSCON MEDIANO RELLENO','ROSCON MEDIANO RELLENO','','04',2,'','','0','0','0',0.0,0.0,0.0,20.0,22.0,0.0,0.0,0.0,0,0,0,0,0.0,0.0,0.0,0.0,0.0,'',0.0,'','','');
INSERT INTO `Articulos` VALUES ('0236','ROSCON GRANDE RELLENO','ROSCON GRANDE RELLENO','','04',2,'','','0','0','0',0.0,0.0,0.0,24.55,27.0,0.0,0.0,0.0,0,0,0,0,0.0,0.0,0.0,0.0,0.0,'',0.0,'','','');
INSERT INTO `Articulos` VALUES ('0170','BARRA INTEGRAL 1 KILO.','BARRA INTEGRAL 1 KILO.','','03',2,'','','0','0','0',0.0,0.0,0.0,4.33,0.0,0.0,0.0,0.0,0,0,0,0,0.0,0.0,0.0,0.0,0.0,'',0.0,'','','');
INSERT INTO `Articulos` VALUES ('0308','PORCION COCA TOÑINA','PORCION COCA TOÑINA','','05',2,'','','0','0','0',0.0,0.0,0.0,1.64,1.8,0.0,0.0,0.0,0,0,0,0,0.0,0.0,0.0,0.0,0.0,'',0.0,'','','');
INSERT INTO `Articulos` VALUES ('0309','COCA VERDURA SARDINA ENTERA','COCA VERDURA SARDINA ENTERA','','05',2,'','','0','0','0',0.0,0.0,0.0,12.0,0.0,0.0,0.0,0.0,0,0,0,0,0.0,0.0,0.0,0.0,0.0,'',0.0,'','','');
INSERT INTO `Articulos` VALUES ('0310','PORCION COCA VERDURA SARDINA','PORCION COCA VERDURA SARDINA','','05',2,'','','0','0','0',0.0,0.0,0.0,1.09,1.2,0.0,0.0,0.0,0,0,0,0,0.0,0.0,0.0,0.0,0.0,'',0.0,'','','');
INSERT INTO `Articulos` VALUES ('0311','COCA TOMATE ANCHOAS ENTERA','COCA TOMATE ANCHOAS ENTERA','','05',2,'','','0','0','0',0.0,0.0,0.0,12.0,0.0,0.0,0.0,0.0,0,0,0,0,0.0,0.0,0.0,0.0,0.0,'',0.0,'','','');
INSERT INTO `Articulos` VALUES ('0312','PORCION COCA TOMATE ANCHOAS','PORCION COCA TOMATE ANCHOAS','','05',2,'','','0','0','0',0.0,0.0,0.0,1.2,0.0,0.0,0.0,0.0,0,0,0,0,0.0,0.0,0.0,0.0,0.0,'',0.0,'','','');
INSERT INTO `Articulos` VALUES ('0313','PIZZA JAMON QUESO ENTERA','PIZZA JAMON QUESO ENTERA','','05',2,'','','0','0','0',0.0,0.0,0.0,15.0,0.0,0.0,0.0,0.0,0,0,0,0,0.0,0.0,0.0,0.0,0.0,'',0.0,'','','');
INSERT INTO `Articulos` VALUES ('0314','PORCION PIZZA JAMON QUESO','PORCION PIZZA JAMON QUESO','','05',2,'','','0','0','0',0.0,0.0,0.0,1.9,0.0,0.0,0.0,0.0,0,0,0,0,0.0,0.0,0.0,0.0,0.0,'',0.0,'','','');
INSERT INTO `Articulos` VALUES ('0316','PORCION PIZZA BERENJENA BACON','PORCION PIZZA BERENJENA BACON','','05',2,'','','0','0','0',0.0,0.0,0.0,2.1,0.0,0.0,0.0,0.0,0,0,0,0,0.0,0.0,0.0,0.0,0.0,'',0.0,'','','');
INSERT INTO `Articulos` VALUES ('0212','KILO MADALENAS ALMENDRA','KILO MADALENAS ALMENDRA','','04',2,'','','0','0','0',0.0,0.0,0.0,8.0,0.0,0.0,0.0,0.0,0,0,0,0,0.0,0.0,0.0,0.0,0.0,'',0.0,'','','');
INSERT INTO `Articulos` VALUES ('0220','TOÑA GRANDE','TOÑA GRANDE','','04',2,'','','0','0','0',0.0,0.0,0.0,3.32,0.0,0.0,0.0,0.0,0,0,0,0,0.0,0.0,0.0,0.0,0.0,'',0.0,'','','');
INSERT INTO `Articulos` VALUES ('0221','MONA GR. SIN/HUE.','MONA GR. SIN/HUE.','','04',2,'','','0','0','0',0.0,0.0,0.0,2.55,0.0,0.0,0.0,0.0,0,0,0,0,0.0,0.0,0.0,0.0,0.0,'',0.0,'','','');
INSERT INTO `Articulos` VALUES ('0222','MONA GR. CON/HUE.','MONA GR. CON/HUE.','','04',2,'','','0','0','0',0.0,0.0,0.0,2.77,0.0,0.0,0.0,0.0,0,0,0,0,0.0,0.0,0.0,0.0,0.0,'',0.0,'','','');
INSERT INTO `Articulos` VALUES ('0223','MONA PEQ. SIN/HUE.','MONA PEQ. SIN/HUE.','','04',2,'','','0','0','0',0.0,0.0,0.0,1.82,0.0,0.0,0.0,0.0,0,0,0,0,0.0,0.0,0.0,0.0,0.0,'',0.0,'','','');
INSERT INTO `Articulos` VALUES ('0224','MONA PEQ. CON/HUE.','MONA PEQ. CON/HUE.','','04',2,'','','0','0','0',0.0,0.0,0.0,2.14,0.0,0.0,0.0,0.0,0,0,0,0,0.0,0.0,0.0,0.0,0.0,'',0.0,'','','');
INSERT INTO `Articulos` VALUES ('0225','MONA CODORNIZ CON/HUE.','MONA CODORNIZ CON/HUE.','','04',2,'','','0','0','0',0.0,0.0,0.0,1.45,0.0,0.0,0.0,0.0,0,0,0,0,0.0,0.0,0.0,0.0,0.0,'',0.0,'','','');
INSERT INTO `Articulos` VALUES ('0101','BARRA NORMAL 1/4','BARRA NORMAL 1/4','','01',1,'','','0','0','0',0.0,0.0,0.0,0.96,0.0,0.0,0.0,0.0,0,0,0,0,0.0,0.0,0.0,0.0,0.0,'',0.0,'','','');
INSERT INTO `Articulos` VALUES ('0106','PAN CASERO KILO.','PAN CASERO KILO.','','02',1,'','','0','0','0',0.0,0.0,0.0,4.04,0.0,0.0,0.0,0.0,0,0,0,0,0.0,0.0,0.0,0.0,0.0,'',0.0,'','','');
INSERT INTO `Articulos` VALUES ('0128','MEDIA DE PAN ','MEDIA DE PAN ','','01',1,'','','0','0','0',0.0,0.0,0.0,0.34,0.0,0.0,0.0,0.0,0,0,0,0,0.0,0.0,0.0,0.0,0.0,'',0.0,'','','');
INSERT INTO `Articulos` VALUES ('0103','BARRA CASERA 1/4','BARRA CASERA 1/4','','02',1,'','','0','0','0',0.0,0.0,0.0,1.06,0.0,0.0,0.0,0.0,0,0,0,0,0.0,0.0,0.0,0.0,0.0,'',0.0,'','','');
INSERT INTO `Articulos` VALUES ('0108','BARRA INTEGRAL','BARRA INTEGRAL','','03',2,'','','0','0','0',0.0,0.0,0.0,1.06,0.0,0.0,0.0,0.0,0,0,0,0,0.0,0.0,0.0,0.0,0.0,'',0.0,'','','');
INSERT INTO `Articulos` VALUES ('0107','BARRA CASERA KILO.','BARRA CASERA KILO.','','02',1,'','','0','0','0',0.0,0.0,0.0,4.04,0.0,0.0,0.0,0.0,0,0,0,0,0.0,0.0,0.0,0.0,0.0,'',0.0,'','','');
INSERT INTO `Articulos` VALUES ('0111','BOCADILLO CASERO','BOCADILLO CASERO','','02',1,'','','0','0','0',0.0,0.0,0.0,0.53,0.0,0.0,0.0,0.0,0,0,0,0,0.0,0.0,0.0,0.0,0.0,'',0.0,'','','');
INSERT INTO `Articulos` VALUES ('0202','CROISANT CURVO GRANDE','CROISANT CURVO GRANDE','','04',2,'','','0','0','0',0.0,0.0,0.0,1.09,0.0,0.0,0.0,0.0,0,0,0,0,0.0,0.0,0.0,0.0,0.0,'',0.0,'','','');
INSERT INTO `Articulos` VALUES ('0300','BOLLERIA SALADA','BOLLERIA SALADA','','05',2,'','','0','0','0',0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0,0,0,0,0.0,0.0,0.0,0.0,0.0,'',0.0,'','','');
INSERT INTO `Articulos` VALUES ('0131','MOLDE INTEGRAL','MOLDE INTEGRAL','','03',2,'','','0','0','0',0.0,0.0,0.0,2.4,0.0,0.0,0.0,0.0,0,0,0,0,0.0,0.0,0.0,0.0,0.0,'',0.0,'','','');
INSERT INTO `Articulos` VALUES ('0701','ROLLOS DE HUEVO KILO','ROLLOS DE HUEVO KILO','','07',2,'','','0','0','0',0.0,0.0,0.0,10.0,0.0,0.0,0.0,0.0,0,0,0,0,0.0,0.0,0.0,0.0,0.0,'',0.0,'','','');
INSERT INTO `Articulos` VALUES ('0702','ROLLOS DE NARANJA KILO','ROLLOS DE NARANJA KILO','','07',2,'','','0','0','0',0.0,0.0,0.0,10.0,0.0,0.0,0.0,0.0,0,0,0,0,0.0,0.0,0.0,0.0,0.0,'',0.0,'','','');
INSERT INTO `Articulos` VALUES ('0307','COCA TOÑINA ENTERA','COCA TOÑINA ENTERA','','05',2,'','','0','0','0',0.0,0.0,0.0,18.18,20.0,0.0,0.0,0.0,0,0,0,0,0.0,0.0,0.0,0.0,0.0,'',0.0,'','','');
INSERT INTO `Articulos` VALUES ('0201','ENSEIMADA GRANDE','ENSEIMADA GRANDE','','04',2,'','','0','0','0',0.0,0.0,0.0,0.91,0.0,0.0,0.0,0.0,0,0,0,0,0.0,0.0,0.0,0.0,0.0,'',0.0,'','','');
INSERT INTO `Articulos` VALUES ('0207','MINI CROISANT MANTEQUILLA','MINI CROISANT MANTEQUILLA','','04',2,'','','0','0','0',0.0,0.0,0.0,0.54,0.0,0.0,0.0,0.0,0,0,0,0,0.0,0.0,0.0,0.0,0.0,'',0.0,'','','');
INSERT INTO `Articulos` VALUES ('0210','MINI CROISANT CHOCO','MINI CROISANT CHOCO','','04',2,'','','0','0','0',0.0,0.0,0.0,0.54,0.0,0.0,0.0,0.0,0,0,0,0,0.0,0.0,0.0,0.0,0.0,'',0.0,'','','');
INSERT INTO `Articulos` VALUES ('0208','MINI ENSEIMADA','MINI ENSEIMADA','','04',2,'','','0','0','0',0.0,0.0,0.0,0.45,0.0,0.0,0.0,0.0,0,0,0,0,0.0,0.0,0.0,0.0,0.0,'',0.0,'','','');
INSERT INTO `Articulos` VALUES ('0303','COCA PISTO EMPANADA ENTERA','COCA PISTO EMPANADA ENTERA','','05',2,'','','0','0','0',0.0,0.0,0.0,13.63,15.0,0.0,0.0,0.0,0,0,0,0,0.0,0.0,0.0,0.0,0.0,'',0.0,'','','');
INSERT INTO `Articulos` VALUES ('0305','COCA GUISANTES ENTERA','COCA GUISANTES ENTERA','','05',2,'','','0','0','0',0.0,0.0,0.0,18.18,20.0,0.0,0.0,0.0,0,0,0,0,0.0,0.0,0.0,0.0,0.0,'',0.0,'','','');
INSERT INTO `Articulos` VALUES ('0102','BARRA CASERA LARGA 1/4','BARRA CASERA LARGA 1/4','','02',1,'','','0','0','0',0.0,0.0,0.0,1.01,0.0,0.0,0.0,0.0,0,0,0,0,0.0,0.0,0.0,0.0,0.0,'',0.0,'','','');
INSERT INTO `Articulos` VALUES ('0120','BARRA SOBADA 1/4','BARRA SOBADA 1/4','','01',1,'','','0','0','0',0.0,0.0,0.0,1.25,0.0,0.0,0.0,0.0,0,0,0,0,0.0,0.0,0.0,0.0,0.0,'',0.0,'','','');
INSERT INTO `Articulos` VALUES ('0104','BARRA CASERA 1/2 KG.','BARRA CASERA 1/2 KG.','','02',1,'','','0','0','0',0.0,0.0,0.0,2.07,0.0,0.0,0.0,0.0,0,0,0,0,0.0,0.0,0.0,0.0,0.0,'',0.0,'','','');
INSERT INTO `Articulos` VALUES ('0209','NAPOLITANA CHOCO','NAPOLITANA CHOCO','','04',2,'','','0','0','0',0.0,0.0,0.0,1.09,0.0,0.0,0.0,0.0,0,0,0,0,0.0,0.0,0.0,0.0,0.0,'',0.0,'','','');
INSERT INTO `Articulos` VALUES ('0211','KILO MADALENAS NORMALES','KILO MADALENAS NORMALES','','04',2,'','','0','0','0',0.0,0.0,0.0,7.0,0.0,0.0,0.0,0.0,0,0,0,0,0.0,0.0,0.0,0.0,0.0,'',0.0,'','','');
INSERT INTO `Articulos` VALUES ('0306','PORCION COCA GUISANTES','PORCION COCA GUISANTES','','05',2,'','','0','0','0',0.0,0.0,0.0,1.64,1.8,0.0,0.0,0.0,0,0,0,0,0.0,0.0,0.0,0.0,0.0,'',0.0,'','','');
INSERT INTO `Articulos` VALUES ('0200','BOLLERIA DULCE','BOLLERIA DULCE','','04',2,'','','0','0','0',0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0,0,0,0,0.0,0.0,0.0,0.0,0.0,'',0.0,'','','');
INSERT INTO `Articulos` VALUES ('0100','PAN COMUN','PAN COMUN','','01',1,'','','0','0','0',0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0,0,0,0,0.0,0.0,0.0,0.0,0.0,'',0.0,'','','');
INSERT INTO `Articulos` VALUES ('0900','SERVICIO ESPECIAL','SERVICIO ESPECIAL','','09',3,'','','0','0','1',0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0,0,0,0,0.0,0.0,0.0,0.0,0.0,'',0.0,'','','');
INSERT INTO `Articulos` VALUES ('0901','ALQUILER DE UTILLAJE PROPIO','ALQUILER DE UTILLAJE PROPIO','','09',3,'','','0','0','1',0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0,0,0,0,0.0,0.0,0.0,0.0,0.0,'',0.0,'','','');
INSERT INTO `Articulos` VALUES ('1010','SACO PAN DURO/AYER','SACO PAN DURO/AYER','','10',1,'','','0','0','0',0.0,0.0,0.0,2.1,0.0,0.0,0.0,0.0,0,0,0,0,0.0,0.0,0.0,0.0,0.0,'',0.0,'','','');
INSERT INTO `Articulos` VALUES ('0800','VARIOS PASTELERIA ','VARIOS PASTELERIA ','','06',2,'','','0','0','0',0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0,0,0,0,0.0,0.0,0.0,0.0,0.0,'',0.0,'','','');
INSERT INTO `Articulos` VALUES ('0801','TARTA DE ELCHE KG. ','TARTA DE ELCHE KG. ','','06',2,'','','0','0','0',0.0,0.0,0.0,18.2,0.0,0.0,0.0,0.0,0,0,0,0,0.0,0.0,0.0,0.0,0.0,'',0.0,'','','');
INSERT INTO `Articulos` VALUES ('0203','CROISSANT RECTO MARGARINA 90','CROISSANT RECTO MARGARINA 90','','04',2,'','','0','0','0',0.0,0.0,0.0,0.91,0.0,0.0,0.0,0.0,0,0,0,0,0.0,0.0,0.0,0.0,0.0,'',0.0,'','','');
INSERT INTO `Articulos` VALUES ('0204','CROISSANT RECTO CHOCO INYE. 90','CROISSANT RECTO CHOCO INYE. 90','','04',2,'','','0','0','0',0.0,0.0,0.0,1.09,0.0,0.0,0.0,0.0,0,0,0,0,0.0,0.0,0.0,0.0,0.0,'',0.0,'','','');
INSERT INTO `BalancesConImporte` VALUES (100,'A) Accionistas por desembolsos no exigidos',0.0,0.0);
INSERT INTO `BalancesConImporte` VALUES (200,'B) Inmovilizado',0.0,0.0);
INSERT INTO `BalancesConImporte` VALUES (210,'    I. Gastos de establecimiento',0.0,0.0);
INSERT INTO `BalancesConImporte` VALUES (220,'    II. Inmovilizaciones inmateriales',0.0,0.0);
INSERT INTO `BalancesConImporte` VALUES (221,'        1. Gastos de investigación y desarrollo',0.0,0.0);
INSERT INTO `BalancesConImporte` VALUES (222,'        2. Concesiones, patentes, licencias, marcas y similares',0.0,0.0);
INSERT INTO `BalancesConImporte` VALUES (223,'        3. Fondo de comercio',0.0,0.0);
INSERT INTO `BalancesConImporte` VALUES (224,'        4. Derechos de traspaso',0.0,0.0);
INSERT INTO `BalancesConImporte` VALUES (225,'        5. Aplicaciones informáticas',0.0,0.0);
INSERT INTO `BalancesConImporte` VALUES (226,'        6. Anticipos',0.0,0.0);
INSERT INTO `BalancesConImporte` VALUES (227,'        7. Provisiones',0.0,0.0);
INSERT INTO `BalancesConImporte` VALUES (228,'        8. Amortizaciones',0.0,0.0);
INSERT INTO `BalancesConImporte` VALUES (230,'    III. Inmovilizaciones materiales',0.0,0.0);
INSERT INTO `BalancesConImporte` VALUES (231,'        1. Terrenos y construcciones',0.0,0.0);
INSERT INTO `BalancesConImporte` VALUES (232,'        2. Instalaciones técnicas y maquinaria',0.0,0.0);
INSERT INTO `BalancesConImporte` VALUES (233,'        3. Otras instalaciones, utillaje y mobiliario',0.0,0.0);
INSERT INTO `BalancesConImporte` VALUES (234,'        4. Anticipos e inmovilizaciones materiales en curso',0.0,0.0);
INSERT INTO `BalancesConImporte` VALUES (235,'        5. Otro inmovilizado',0.0,0.0);
INSERT INTO `BalancesConImporte` VALUES (236,'        6. Provisiones',0.0,0.0);
INSERT INTO `BalancesConImporte` VALUES (237,'        7. Amortizaciones',0.0,0.0);
INSERT INTO `BalancesConImporte` VALUES (240,'    IV. Inmovilizaciones financieras',0.0,0.0);
INSERT INTO `BalancesConImporte` VALUES (241,'        1. Participaciones en empresas del grupo',0.0,0.0);
INSERT INTO `BalancesConImporte` VALUES (242,'        2. Créditos a empresas del grupo',0.0,0.0);
INSERT INTO `BalancesConImporte` VALUES (243,'        3. Participaciones en empresas asociadas',0.0,0.0);
INSERT INTO `BalancesConImporte` VALUES (244,'        4. Créditos a empresas asociadas',0.0,0.0);
INSERT INTO `BalancesConImporte` VALUES (245,'        5. Valores que tengan carácter de inmovilizaciones',0.0,0.0);
INSERT INTO `BalancesConImporte` VALUES (246,'        6. Otros créditos',0.0,0.0);
INSERT INTO `BalancesConImporte` VALUES (247,'        7. Depósitos y fianzas entregados a largo plazo',0.0,0.0);
INSERT INTO `BalancesConImporte` VALUES (248,'        8. Provisiones',0.0,0.0);
INSERT INTO `BalancesConImporte` VALUES (250,'    V. Acciones propias',0.0,0.0);
INSERT INTO `BalancesConImporte` VALUES (300,'C) Gastos a distribuir en varios ejercicios',0.0,0.0);
INSERT INTO `BalancesConImporte` VALUES (400,'D) Activo circulante',394.0,0.0);
INSERT INTO `BalancesConImporte` VALUES (410,'    I. Accionistas por desembolsos exigidos',0.0,0.0);
INSERT INTO `BalancesConImporte` VALUES (420,'    II. Existencias',0.0,0.0);
INSERT INTO `BalancesConImporte` VALUES (421,'        1. Comerciales',0.0,0.0);
INSERT INTO `BalancesConImporte` VALUES (422,'        2. Materias primas y otros aprovisionamientos',0.0,0.0);
INSERT INTO `BalancesConImporte` VALUES (423,'        3. Productos en curso y semiterminados',0.0,0.0);
INSERT INTO `BalancesConImporte` VALUES (424,'        4. Productos terminados',0.0,0.0);
INSERT INTO `BalancesConImporte` VALUES (425,'        5. Subproductos, residuos y materiales recuperados',0.0,0.0);
INSERT INTO `BalancesConImporte` VALUES (426,'        6. Anticipos',0.0,0.0);
INSERT INTO `BalancesConImporte` VALUES (427,'        7. Provisiones',0.0,0.0);
INSERT INTO `BalancesConImporte` VALUES (430,'    III. Deudores',394.0,0.0);
INSERT INTO `BalancesConImporte` VALUES (431,'        1. Clientes por ventas y prestaciones de servicios',394.0,0.0);
INSERT INTO `BalancesConImporte` VALUES (432,'        2. Sociedades del grupo, deudores',0.0,0.0);
INSERT INTO `BalancesConImporte` VALUES (433,'        3. Sociedades asociadas, deudores',0.0,0.0);
INSERT INTO `BalancesConImporte` VALUES (434,'        4. Deudores varios',0.0,0.0);
INSERT INTO `BalancesConImporte` VALUES (435,'        5. Personal',0.0,0.0);
INSERT INTO `BalancesConImporte` VALUES (436,'        6. Administraciones Públicas',0.0,0.0);
INSERT INTO `BalancesConImporte` VALUES (437,'        7. Provisiones',0.0,0.0);
INSERT INTO `BalancesConImporte` VALUES (440,'    IV. Inversiones financieras temporales',0.0,0.0);
INSERT INTO `BalancesConImporte` VALUES (441,'        1. Participaciones en empresas del grupo',0.0,0.0);
INSERT INTO `BalancesConImporte` VALUES (442,'        2. Créditos a empresas del grupo',0.0,0.0);
INSERT INTO `BalancesConImporte` VALUES (443,'        3. Participaciones en empresas asociadas',0.0,0.0);
INSERT INTO `BalancesConImporte` VALUES (444,'        4. Créditos a empresas asociadas',0.0,0.0);
INSERT INTO `BalancesConImporte` VALUES (445,'        5. Cartera de valores a corto plazo',0.0,0.0);
INSERT INTO `BalancesConImporte` VALUES (446,'        6. Créditos',0.0,0.0);
INSERT INTO `BalancesConImporte` VALUES (447,'        7. Depósitos y fianzas entregados a corto plazo',0.0,0.0);
INSERT INTO `BalancesConImporte` VALUES (448,'        8. Provisiones',0.0,0.0);
INSERT INTO `BalancesConImporte` VALUES (450,'    V. Acciones propias a corto plazo',0.0,0.0);
INSERT INTO `BalancesConImporte` VALUES (460,'    VI. Tesorería',0.0,0.0);
INSERT INTO `BalancesConImporte` VALUES (470,'    VII. Ajustes por periodificación',0.0,0.0);
INSERT INTO `BalancesConImporte` VALUES (1100,'A) Fondos propios',0.0,0.0);
INSERT INTO `BalancesConImporte` VALUES (1110,'    I. Capital suscrito',0.0,0.0);
INSERT INTO `BalancesConImporte` VALUES (1120,'    II. Prima de emisión',0.0,0.0);
INSERT INTO `BalancesConImporte` VALUES (1130,'    III. Reserva de revalorización',0.0,0.0);
INSERT INTO `BalancesConImporte` VALUES (1140,'    IV. Reservas',0.0,0.0);
INSERT INTO `BalancesConImporte` VALUES (1141,'        1. Reserva legal',0.0,0.0);
INSERT INTO `BalancesConImporte` VALUES (1142,'        2. Reservas para acciones propias',0.0,0.0);
INSERT INTO `BalancesConImporte` VALUES (1143,'        3. Reservas para acciones de la sociedad dominante',0.0,0.0);
INSERT INTO `BalancesConImporte` VALUES (1144,'        4. Reservas estatutarias',0.0,0.0);
INSERT INTO `BalancesConImporte` VALUES (1145,'        5. Otras reservas',0.0,0.0);
INSERT INTO `BalancesConImporte` VALUES (1150,'    V. Resultados de ejercicios anteriores',0.0,0.0);
INSERT INTO `BalancesConImporte` VALUES (1151,'        1. Remanente',0.0,0.0);
INSERT INTO `BalancesConImporte` VALUES (1152,'        2. Resultados negatigos de ejercicios anteriores',0.0,0.0);
INSERT INTO `BalancesConImporte` VALUES (1153,'        3. Aportaciones socios para compensación pérdidas',0.0,0.0);
INSERT INTO `BalancesConImporte` VALUES (1160,'    VI.Pérdidas y ganancias (beneficio o pérdida)',0.0,0.0);
INSERT INTO `BalancesConImporte` VALUES (1170,'    VII. Dividendo a cuenta entregado en el ejercicio',0.0,0.0);
INSERT INTO `BalancesConImporte` VALUES (1200,'B) Ingresos a distribuir en varios ejercicios',0.0,0.0);
INSERT INTO `BalancesConImporte` VALUES (1201,'        1. Subvenciones de capital',0.0,0.0);
INSERT INTO `BalancesConImporte` VALUES (1202,'        2. Diferencias positivas de cambio',0.0,0.0);
INSERT INTO `BalancesConImporte` VALUES (1203,'        3.Otros ingresos a distribuir en varios ejercicios',0.0,0.0);
INSERT INTO `BalancesConImporte` VALUES (1300,'C) Provisiones para riesgos y gastos',0.0,0.0);
INSERT INTO `BalancesConImporte` VALUES (1301,'        1. Provisiones pensiones y obligaciones similares',0.0,0.0);
INSERT INTO `BalancesConImporte` VALUES (1302,'        2. Provisiones para impuestos',0.0,0.0);
INSERT INTO `BalancesConImporte` VALUES (1303,'        3. Otras provisiones',0.0,0.0);
INSERT INTO `BalancesConImporte` VALUES (1304,'        4. Fondos de reversión',0.0,0.0);
INSERT INTO `BalancesConImporte` VALUES (1400,'D) Acreedores a largo plazo',0.0,0.0);
INSERT INTO `BalancesConImporte` VALUES (1410,'    I. Emisiones de obligaciones',0.0,0.0);
INSERT INTO `BalancesConImporte` VALUES (1411,'        1. Obligaciones no convertibles',0.0,0.0);
INSERT INTO `BalancesConImporte` VALUES (1412,'        2. Obligaciones convertibles',0.0,0.0);
INSERT INTO `BalancesConImporte` VALUES (1413,'        3. Otras deudas representadas en valores negociab.',0.0,0.0);
INSERT INTO `BalancesConImporte` VALUES (1420,'    II. Deudas con entidades de crédito',0.0,0.0);
INSERT INTO `BalancesConImporte` VALUES (1430,'    III. Deudas con empresas del grupo y asociadas',0.0,0.0);
INSERT INTO `BalancesConImporte` VALUES (1431,'        1. Deudas con empresas del grupo',0.0,0.0);
INSERT INTO `BalancesConImporte` VALUES (1432,'        2. Deudas con empresas asociadas',0.0,0.0);
INSERT INTO `BalancesConImporte` VALUES (1440,'    IV.Otros acreedores',0.0,0.0);
INSERT INTO `BalancesConImporte` VALUES (1441,'        1. Deudas representadas por efectos a pagar',0.0,0.0);
INSERT INTO `BalancesConImporte` VALUES (1442,'        2. Otras deudas',0.0,0.0);
INSERT INTO `BalancesConImporte` VALUES (1443,'        3. Fianzas y depósitos recibidos a largo plazo',0.0,0.0);
INSERT INTO `BalancesConImporte` VALUES (1450,'    V. Desembolsos pendientes sobre acciones no exigidos',0.0,0.0);
INSERT INTO `BalancesConImporte` VALUES (1451,'        1. De empresas del grupo',0.0,0.0);
INSERT INTO `BalancesConImporte` VALUES (1452,'        2. De empresas asociadas',0.0,0.0);
INSERT INTO `BalancesConImporte` VALUES (1453,'        3. De otras empresas',0.0,0.0);
INSERT INTO `BalancesConImporte` VALUES (1500,'E) Acreedores a corto plazo',54.0,0.0);
INSERT INTO `BalancesConImporte` VALUES (1510,'    I. Emisiones de obligaciones',0.0,0.0);
INSERT INTO `BalancesConImporte` VALUES (1511,'        1. Obligaciones no convertibles',0.0,0.0);
INSERT INTO `BalancesConImporte` VALUES (1512,'        2. Obligaciones convertibles',0.0,0.0);
INSERT INTO `BalancesConImporte` VALUES (1513,'        3. Otras deudas representadas valores negociables',0.0,0.0);
INSERT INTO `BalancesConImporte` VALUES (1514,'        4. Intereses de obligaciones y otros valores',0.0,0.0);
INSERT INTO `BalancesConImporte` VALUES (1520,'    II. Deudas con entidades decrédito',0.0,0.0);
INSERT INTO `BalancesConImporte` VALUES (1521,'        1. Préstamos y otras deudas',0.0,0.0);
INSERT INTO `BalancesConImporte` VALUES (1522,'        2. Deudas por intereses',0.0,0.0);
INSERT INTO `BalancesConImporte` VALUES (1530,'    III. Deudas con empresas del grupo y asociadas a corto plazo',0.0,0.0);
INSERT INTO `BalancesConImporte` VALUES (1531,'        1. Deudas con empresas del grupo',0.0,0.0);
INSERT INTO `BalancesConImporte` VALUES (1532,'        2. Deudas con empresas asociadas',0.0,0.0);
INSERT INTO `BalancesConImporte` VALUES (1540,'    IV. Acreedores comerciales',0.0,0.0);
INSERT INTO `BalancesConImporte` VALUES (1541,'        1. Anticipos recibidos por pedidos',0.0,0.0);
INSERT INTO `BalancesConImporte` VALUES (1542,'        2. Deudas por compras o prestaciones de servicios',0.0,0.0);
INSERT INTO `BalancesConImporte` VALUES (1543,'        3. Deudas representadas por efectos a pagar',0.0,0.0);
INSERT INTO `BalancesConImporte` VALUES (1550,'    V. Otras deudas no comerciales',54.0,0.0);
INSERT INTO `BalancesConImporte` VALUES (1551,'        1. Administraciones Públicas',54.0,0.0);
INSERT INTO `BalancesConImporte` VALUES (1552,'        2. Deudas representadas por efectos a pagar',0.0,0.0);
INSERT INTO `BalancesConImporte` VALUES (1553,'        3. Otras deudas',0.0,0.0);
INSERT INTO `BalancesConImporte` VALUES (1554,'        4. Remuneraciones pendientes de pago',0.0,0.0);
INSERT INTO `BalancesConImporte` VALUES (1555,'        5. Fianzas y depósitos recibidos a corto plazo',0.0,0.0);
INSERT INTO `BalancesConImporte` VALUES (1560,'    VI. Provisiones para operaciones de tráfico',0.0,0.0);
INSERT INTO `BalancesConImporte` VALUES (1570,'    VII. Ajustes por periodificación',0.0,0.0);
INSERT INTO `BalancesConImporte` VALUES (2000,'A) GASTOS',0.0,0.0);
INSERT INTO `BalancesConImporte` VALUES (2010,'      1. Reducción de existencias productos terminados y en curso de fabricación',0.0,0.0);
INSERT INTO `BalancesConImporte` VALUES (2020,'      2. Aprovisionamientos',0.0,0.0);
INSERT INTO `BalancesConImporte` VALUES (2021,'          a) Consumo de mercaderías',0.0,0.0);
INSERT INTO `BalancesConImporte` VALUES (2022,'          b) Consumo de materias primas y otras materias consumibles',0.0,0.0);
INSERT INTO `BalancesConImporte` VALUES (2023,'          c) Otros gastos externos',0.0,0.0);
INSERT INTO `BalancesConImporte` VALUES (2030,'      3. Gastos de personal',0.0,0.0);
INSERT INTO `BalancesConImporte` VALUES (2031,'          a) Sueldos, salarios y asimilados',0.0,0.0);
INSERT INTO `BalancesConImporte` VALUES (2032,'          b) Cargas sociales',0.0,0.0);
INSERT INTO `BalancesConImporte` VALUES (2040,'      4. Dotaciones para amortizaciones de inmovilizado',0.0,0.0);
INSERT INTO `BalancesConImporte` VALUES (2050,'      5. Variación de las provisiones de tráfico',0.0,0.0);
INSERT INTO `BalancesConImporte` VALUES (2051,'          a) Variación de provisiones de existencias',0.0,0.0);
INSERT INTO `BalancesConImporte` VALUES (2052,'          b) Variación de provisiones y pérdidas de créditos incobrables',0.0,0.0);
INSERT INTO `BalancesConImporte` VALUES (2053,'          c) Variación de otras provisiones de tráfico',0.0,0.0);
INSERT INTO `BalancesConImporte` VALUES (2060,'      6. Otros gastos de explotación',0.0,0.0);
INSERT INTO `BalancesConImporte` VALUES (2061,'          a) Servicios exteriores',0.0,0.0);
INSERT INTO `BalancesConImporte` VALUES (2062,'          b) Tributos',0.0,0.0);
INSERT INTO `BalancesConImporte` VALUES (2063,'          c) Otros gastos de gestión corriente',0.0,0.0);
INSERT INTO `BalancesConImporte` VALUES (2064,'          d) Dotación al fondo de reversión',0.0,0.0);
INSERT INTO `BalancesConImporte` VALUES (2065,'    I. Beneficios de Explotación',340.0,0.0);
INSERT INTO `BalancesConImporte` VALUES (2070,'      7. Gastos financieros y gastos asimilados',0.0,0.0);
INSERT INTO `BalancesConImporte` VALUES (2071,'          a) Por deudas con empresas del grupo',0.0,0.0);
INSERT INTO `BalancesConImporte` VALUES (2072,'          b) Por deudas con empresas asociadas',0.0,0.0);
INSERT INTO `BalancesConImporte` VALUES (2073,'          c) Por deudas con terceros y gastos asimilados',0.0,0.0);
INSERT INTO `BalancesConImporte` VALUES (2074,'          d) Pérdidas de inversiones financieras',0.0,0.0);
INSERT INTO `BalancesConImporte` VALUES (2080,'      8. Variación de las provisiones de inversiones financieras',0.0,0.0);
INSERT INTO `BalancesConImporte` VALUES (2090,'      9. Diferencias negativas de cambio',0.0,0.0);
INSERT INTO `BalancesConImporte` VALUES (2091,'    II. Resultados financieros positivos',0.0,0.0);
INSERT INTO `BalancesConImporte` VALUES (2092,'    III. Beneficios actividades ordinarias',340.0,0.0);
INSERT INTO `BalancesConImporte` VALUES (2100,'      10. Var. previsiones inmovilizado inmaterial, material y cartera de control',0.0,0.0);
INSERT INTO `BalancesConImporte` VALUES (2110,'      11. Pérds.procedentes inmovilizado inmaterial,material y cartera de control',0.0,0.0);
INSERT INTO `BalancesConImporte` VALUES (2120,'      12. Pérdidas por operaciones con acciones y obligaciones propias',0.0,0.0);
INSERT INTO `BalancesConImporte` VALUES (2130,'      13. Gastos extraordinarios',0.0,0.0);
INSERT INTO `BalancesConImporte` VALUES (2140,'      14. Gastos y pérdidas de otros ejercicios',0.0,0.0);
INSERT INTO `BalancesConImporte` VALUES (2141,'    IV. Resultados extraordinarios positivos ',0.0,0.0);
INSERT INTO `BalancesConImporte` VALUES (2142,'    V. Beneficios antes de impuestos',340.0,0.0);
INSERT INTO `BalancesConImporte` VALUES (2150,'      15. Impuesto sobre sociedades',0.0,0.0);
INSERT INTO `BalancesConImporte` VALUES (2160,'      16. Otros impuestos',0.0,0.0);
INSERT INTO `BalancesConImporte` VALUES (2161,'    VI. Resultado del ejercicio (beneficios)',340.0,0.0);
INSERT INTO `BalancesConImporte` VALUES (3000,'B) INGRESOS',340.0,0.0);
INSERT INTO `BalancesConImporte` VALUES (3010,'      1. Importe neto de la cifra de negocio',340.0,0.0);
INSERT INTO `BalancesConImporte` VALUES (3011,'          a) Ventas',340.0,0.0);
INSERT INTO `BalancesConImporte` VALUES (3012,'          b) Prestaciones de servicios',0.0,0.0);
INSERT INTO `BalancesConImporte` VALUES (3013,'          c) Devoluciones y `rappels` sobre ventas',0.0,0.0);
INSERT INTO `BalancesConImporte` VALUES (3020,'      2.Aumento de las existencias productos terminados y en curso de fabricación',0.0,0.0);
INSERT INTO `BalancesConImporte` VALUES (3030,'      3. Trabajos efectuados por la empresa para el inmovilizado',0.0,0.0);
INSERT INTO `BalancesConImporte` VALUES (3040,'      4. Otros ingresos de explotación',0.0,0.0);
INSERT INTO `BalancesConImporte` VALUES (3041,'          a) Ingresos accesorios y otros de gestión corriente',0.0,0.0);
INSERT INTO `BalancesConImporte` VALUES (3042,'          b) Subvenciones',0.0,0.0);
INSERT INTO `BalancesConImporte` VALUES (3043,'          c) Exceso de provisiones de riesgos y gastos',0.0,0.0);
INSERT INTO `BalancesConImporte` VALUES (3044,'    I. Pérdidas de explotación',0.0,0.0);
INSERT INTO `BalancesConImporte` VALUES (3050,'      5. Ingresos de participaciones en capital',0.0,0.0);
INSERT INTO `BalancesConImporte` VALUES (3051,'          a) En empresas del grupo',0.0,0.0);
INSERT INTO `BalancesConImporte` VALUES (3052,'          b) En empresas asociadas',0.0,0.0);
INSERT INTO `BalancesConImporte` VALUES (3053,'          c) En empresas fuera del grupo',0.0,0.0);
INSERT INTO `BalancesConImporte` VALUES (3060,'      6. Ingresos de otros valores negociables y créditos del activo inmovilizado',0.0,0.0);
INSERT INTO `BalancesConImporte` VALUES (3061,'          a) De empresas del grupo',0.0,0.0);
INSERT INTO `BalancesConImporte` VALUES (3062,'          b) De empresas asociadas',0.0,0.0);
INSERT INTO `BalancesConImporte` VALUES (3063,'          c) De empresas del grupo',0.0,0.0);
INSERT INTO `BalancesConImporte` VALUES (3070,'      7. Otros intereses e ingresos asimilados',0.0,0.0);
INSERT INTO `BalancesConImporte` VALUES (3071,'          a) De empresas del grupo',0.0,0.0);
INSERT INTO `BalancesConImporte` VALUES (3072,'          b) De empresas asociadas',0.0,0.0);
INSERT INTO `BalancesConImporte` VALUES (3073,'          c) Otros intereses',0.0,0.0);
INSERT INTO `BalancesConImporte` VALUES (3074,'          d) Beneficios en inversiones financieras',0.0,0.0);
INSERT INTO `BalancesConImporte` VALUES (3080,'      8. Diferencias positivas de cambio',0.0,0.0);
INSERT INTO `BalancesConImporte` VALUES (3081,'    II. Resultados financieros negativos',0.0,0.0);
INSERT INTO `BalancesConImporte` VALUES (3082,'    III. Pérdidas actividades ordinarias',0.0,0.0);
INSERT INTO `BalancesConImporte` VALUES (3090,'      9. Beneficios enajenación inmovilizado inmaterial,material y cartera contro',0.0,0.0);
INSERT INTO `BalancesConImporte` VALUES (3100,'      10. Beneficios por operaciones con acciones y obligaciones propias',0.0,0.0);
INSERT INTO `BalancesConImporte` VALUES (3110,'      11. Subvenciones de capital transferidas al resultado del ejercicio',0.0,0.0);
INSERT INTO `BalancesConImporte` VALUES (3120,'      12. Ingresos extraordinarios',0.0,0.0);
INSERT INTO `BalancesConImporte` VALUES (3130,'      13. Ingresos y beneficios de otros ejercicios',0.0,0.0);
INSERT INTO `BalancesConImporte` VALUES (3160,'    IV. Resultados extraordinarios negativos',0.0,0.0);
INSERT INTO `BalancesConImporte` VALUES (3170,'    V. Pérdidas antes de impuestos',0.0,0.0);
INSERT INTO `BalancesConImporte` VALUES (3180,'    VI. Resultado del ejercicio (pérdidas)',0.0,0.0);
INSERT INTO `BalancesConImporte` VALUES (4000,'A) GASTOS',0.0,0.0);
INSERT INTO `BalancesConImporte` VALUES (4010,'         1.  Consumos de explotación',0.0,0.0);
INSERT INTO `BalancesConImporte` VALUES (4020,'         2.  Gastos de personal',0.0,0.0);
INSERT INTO `BalancesConImporte` VALUES (4021,'              a) Sueldos, salarios y asimilados',0.0,0.0);
INSERT INTO `BalancesConImporte` VALUES (4022,'              b) Cargas sociales',0.0,0.0);
INSERT INTO `BalancesConImporte` VALUES (4030,'         3.  Dotaciones para amortizaciones de inmovilizado',0.0,0.0);
INSERT INTO `BalancesConImporte` VALUES (4040,'         4.  Variación de las provisiones de tráfico y pérdidas de créditos incobrables',0.0,0.0);
INSERT INTO `BalancesConImporte` VALUES (4050,'         5.  Otros gastos de explotación',0.0,0.0);
INSERT INTO `BalancesConImporte` VALUES (4051,'    I.   BENEFICIOS DE EXPLOTACIÓN',0.0,0.0);
INSERT INTO `BalancesConImporte` VALUES (4060,'         6.  Gastos financieros y gastos asimilados',0.0,0.0);
INSERT INTO `BalancesConImporte` VALUES (4061,'              a) Por deudas con empresas del grupo',0.0,0.0);
INSERT INTO `BalancesConImporte` VALUES (4062,'              b) Por deudas con empresas asociadas',0.0,0.0);
INSERT INTO `BalancesConImporte` VALUES (4063,'              c) Por otras deudas',0.0,0.0);
INSERT INTO `BalancesConImporte` VALUES (4064,'              d) Pérdidas de inversiones financieras',0.0,0.0);
INSERT INTO `BalancesConImporte` VALUES (4070,'         7.  Variación de las provisiones de inversiones financieras',0.0,0.0);
INSERT INTO `BalancesConImporte` VALUES (4080,'         8.  Diferencias negativas de cambio',0.0,0.0);
INSERT INTO `BalancesConImporte` VALUES (4081,'    II.  RESULTADOS FINANCIEROS POSITIVOS',0.0,0.0);
INSERT INTO `BalancesConImporte` VALUES (4082,'    III. BENEFICIOS DE LAS ACTIVIDADES ORDINARIAS',0.0,0.0);
INSERT INTO `BalancesConImporte` VALUES (4090,'         9.  Variación de las provisiones de inmovilizado inmaterial, material y cartera de control',0.0,0.0);
INSERT INTO `BalancesConImporte` VALUES (4100,'        10. Pérdidas procedentes del inmovilizado inmaterial, material y cartera de control',0.0,0.0);
INSERT INTO `BalancesConImporte` VALUES (4110,'        11. Pérdidas por operaciones con acciones y obligaciones propias',0.0,0.0);
INSERT INTO `BalancesConImporte` VALUES (4120,'        12. Gastos extraordinarios',0.0,0.0);
INSERT INTO `BalancesConImporte` VALUES (4130,'        13. Gastos y pérdidas de otros ejercicios',0.0,0.0);
INSERT INTO `BalancesConImporte` VALUES (4131,'   IV. RESULTADOS EXTRAORDINARIOS POSITIVOS',0.0,0.0);
INSERT INTO `BalancesConImporte` VALUES (4132,'   V.  BENEFICIOS ANTES DE IMPUESTOS',0.0,0.0);
INSERT INTO `BalancesConImporte` VALUES (4140,'        14. Impuesto sobre sociedades',0.0,0.0);
INSERT INTO `BalancesConImporte` VALUES (4150,'        15. Otros impuestos',0.0,0.0);
INSERT INTO `BalancesConImporte` VALUES (4151,'   VI. RESULTADO DEL EJERCICIO (BENEFICIOS)',0.0,0.0);
INSERT INTO `BalancesConImporte` VALUES (5000,'B) INGRESOS',340.0,0.0);
INSERT INTO `BalancesConImporte` VALUES (5010,'        1. Ingresos de explotación',340.0,0.0);
INSERT INTO `BalancesConImporte` VALUES (5011,'            a) Importe neto de la cifra de negocios',340.0,0.0);
INSERT INTO `BalancesConImporte` VALUES (5012,'            b) Otros ingresos de explotación',0.0,0.0);
INSERT INTO `BalancesConImporte` VALUES (5013,'    I.  PERDIDAS DE EXPLOTACION',0.0,0.0);
INSERT INTO `BalancesConImporte` VALUES (5020,'        2. Ingresos financieros',0.0,0.0);
INSERT INTO `BalancesConImporte` VALUES (5021,'            a) En empresas del grupo',0.0,0.0);
INSERT INTO `BalancesConImporte` VALUES (5022,'            b) En empresas asociadas',0.0,0.0);
INSERT INTO `BalancesConImporte` VALUES (5023,'            c) Otros',0.0,0.0);
INSERT INTO `BalancesConImporte` VALUES (5024,'            d) Beneficios en inversiones financieras',0.0,0.0);
INSERT INTO `BalancesConImporte` VALUES (5030,'        3. Diferencias positivas de cambio',0.0,0.0);
INSERT INTO `BalancesConImporte` VALUES (5031,'   II.  RESULTADOS FINANCIEROS NEGATIVOS',0.0,0.0);
INSERT INTO `BalancesConImporte` VALUES (5032,'   III. PERDIDAS DE LAS ACTIVIDADES ORDINARIAS',0.0,0.0);
INSERT INTO `BalancesConImporte` VALUES (5040,'       4. Beneficios en enajenación de inmovilizado inmaterial, material y cartera de control',0.0,0.0);
INSERT INTO `BalancesConImporte` VALUES (5050,'       5. Beneficios por operaciones con acciones y obligaciones propias',0.0,0.0);
INSERT INTO `BalancesConImporte` VALUES (5060,'       6. Subvenciones de capital transferidas al resultado del ejercicio',0.0,0.0);
INSERT INTO `BalancesConImporte` VALUES (5070,'       7. Ingresos extraordinarios',0.0,0.0);
INSERT INTO `BalancesConImporte` VALUES (5080,'       8. Ingresos y beneficios de otros ejercicios',0.0,0.0);
INSERT INTO `BalancesConImporte` VALUES (5081,'   IV. RESULTADOS EXTRAORDINARIOS NEGATIVOS',0.0,0.0);
INSERT INTO `BalancesConImporte` VALUES (5082,'   V.  PÉRDIDAS ANTES DE IMPUESTOS',0.0,0.0);
INSERT INTO `BalancesConImporte` VALUES (5083,'   VI. RESULTADO DEL EJERCICIO (PÉRDIDAS)',0.0,0.0);
INSERT INTO `Clientes` VALUES ('0143','','PASCUAL RUSO GIMENEZ `La Bodeguita`','Av. Mediterraneo 68, Local 1','','Santa Pola','03133','03','','','','74386240-P','1','0','','',0,0,'','','',0.0,'','','','','','','');
INSERT INTO `Clientes` VALUES ('0144','','**baja**PETRONILA FELIZ RUIZ ','Partida El Altet, Pol. 1º, nº 45','RTE. LOS OLIVOS','El Altet','03195','03','','','','74.386.240-P','1','0','','',0,0,'','','',0.0,'','','','','','','');
INSERT INTO `Clientes` VALUES ('0145','','**baja**JUANI HITA MESEGUER','Servido en Rte. `Maestral`','','ALICANTE','','03','','','','21500386-D','1','0','','',0,0,'','CONTADO','',0.0,'','','','','','','cliente particular - encargo -');
INSERT INTO `Clientes` VALUES ('0141','','SANTIAGO GUIJARRO JORNET, S.L.','`RINCON DE SANTI`','AVDA. San Bartolome de Tirajana, 45','ARENALES DEL SOL','03195','03','YOLANDA Y SANTI','','','B-42555953','1','0','','',0,0,'','','',0.0,'','','','','','','');
INSERT INTO `Clientes` VALUES ('0142','','ASOCIACION DE VECINOS AGUA AMARGA','C/. TORMOS, 6 1A','','ALICANTE','03008','03','PILI  615 097 192','','','G-53299319','1','0','','',0,0,'','CONTADO','',0.0,'','','','','','','');
INSERT INTO `Clientes` VALUES ('0133','','**baja**MONICA QUILACHAMIN - ','Urbabanova Local 5','`El Mar Restaurante','Alicante','','03','','','','51299831-X','1','0','','',0,0,'','CONTADO','',0.0,'','','','','','','');
INSERT INTO `Clientes` VALUES ('0134','','CARDASI S,A,','C/ Galileo Galilei, 2','','03203 ELCHE - Parque Industrial','','03','','','','A-53183901','1','0','','',0,0,'','CONTADO','',0.0,'','','','','','','');
INSERT INTO `Clientes` VALUES ('0129','','VERONICA KUBICEK SILVEIRA `Nou Yoyos`','C/ Mar 25','','El Altet','03195','03','','','','X-4584270-W','1','0','','',0,0,'','','',0.0,'','','','','','','');
INSERT INTO `Clientes` VALUES ('0114','','**BAJA**LIVIA DANUTA MECHITA','AV. CARTAGENA','','EL ALTET','','03','','','','X3274500J','1','0','','',0,0,'','CONTADO','',0.0,'','','','','','','');
INSERT INTO `Clientes` VALUES ('0115','','**baja**NOEMI FERNANDEZ TORRES','AV. SAN BARTOLOM DE TIRAJANA 20','','ARENALES DEL SOL','03195','03','','','','44766621-L','1','0','','',0,0,'','CONTADO','',0.0,'','','','','','','');
INSERT INTO `Clientes` VALUES ('0116','','Mª Del Mar Bautista Pelaez - R. de Pili','Av. Elche 144','','Alicante','','03','','965113125','','52523521P','1','0','','',0,0,'','CONTADO','',0.0,'','','','','','','');
INSERT INTO `Clientes` VALUES ('0117','','**baja**BEATRIZ BERNÁRDEZ IZQUIERDO','Av. Dama de Elche 19',' `Botijo`','El Altet','03195','03','','','','47046881J','1','0','','',0,0,'','CONTADO','',0.0,'','','','','','','');
INSERT INTO `Clientes` VALUES ('0118','','**baja**ANTONIA GARCIA MARTINEZ','Rincon de Montemar','C/ Holanda 13','Gran Alacant - Santa Pola','03130','03','','','','22450750J','1','0','','',0,0,'','CONTADO','',0.0,'','','','','','','');
INSERT INTO `Clientes` VALUES ('0121','','**baja**ELISABET MARTINEZ GARCIA','Rte. Vista Alegre','Ctra. Alicante-Cartagena Km. 17','Santa Pola','03130','03','','','','74006630J','1','0','','',0,0,'','CONTADO','',0.0,'','','','','','','');
INSERT INTO `Clientes` VALUES ('0103','','**baja**José Luis Herrera Payán ','Pza. San Crispín 6','','TORRELLANO','03320','03','José Luis','965680098','','21999139F','1','0','','',0,0,'','CONTADO','',0.0,'','','','','','','');
INSERT INTO `Clientes` VALUES ('0102','','**baja**ALFONSA TROYA FERNANDEZ','Ubanova - Local 5','  `LA RUEDA`','Alicante','','03','Jose','','','21448520P','1','0','','',0,0,'','','',0.0,'','','','','','','');
INSERT INTO `Clientes` VALUES ('0119','','**baja**ELOINA SELVAS FRIAS ','Av. Ciudad Deportiva 2','','El Altet','','03','','','','21414585','1','0','','',0,0,'','','',0.0,'','','','','','','');
INSERT INTO `Clientes` VALUES ('0124','','**baja**PEDRO ASENCIO AZNAR','C/ ALICANTE s/n',' `Centro Social`','EL ALTET','03195','03','','','','21985860-E','1','0','','',0,0,'','','',0.0,'','','','','','','');
INSERT INTO `Clientes` VALUES ('0127','','**baja**BASILIO FERNANDEZ LATORRE','SAN BARTOLOME DE TIRAJANA 20, LOCAL 14','','ARENALES DEL SOL','','03','','','','02078739-E','1','0','','',0,0,'','','',0.0,'','','','','','','');
INSERT INTO `Clientes` VALUES ('0200','','BEGOÑA PILAR BAEZA BONMATI','AV. SAN FRANCISCO DE ASIS 64','','EL ALTET','03195','03','','','','48350693-R','1','1','','',0,0,'','','',0.0,'','','','','','','');
INSERT INTO `Clientes` VALUES ('0202','','TERRAMAR, S.A.','C/ ZARANDIETA 7, BAJO','','ALICANTE','03010','03','','','','A-03284296','1','0','','',0,0,'','','',0.0,'','','','','','','');
INSERT INTO `Clientes` VALUES ('0207','','**baja**HIJOS DE FABRIZZIO C.B.','ETELLA 6',' `Pizz. Provenzal','ALICANTE','','03','','','','53152138-E','1','0','','',0,0,'','','',0.0,'','','','','','','****BAJA**** PASA A SER -----PIZZERIA PROVENZAL, S.L.');
INSERT INTO `Clientes` VALUES ('0211','','**baja**MARCO ESPIN S.L.','CONCEJAL LORENZO LLANERAS 9',' `Pollo Salsero`','ALICANTE','','03','','','','B-53540043','1','0','','',0,0,'','','',0.0,'','','','','','','');
INSERT INTO `Clientes` VALUES ('0217','','RAQUEL ORTOLA BONMATI','PASEO TOMAS DURA','','URBANOVA - ALICANTE','','03','','','','52765150-E','1','0','','',0,0,'','','',0.0,'','','','','','','');
INSERT INTO `Clientes` VALUES ('0222','','GRAN BAR POMARES S.L.','AV. CARTAGENA 17','','EL ALTET','03195','03','','','','B-54547492','1','0','','',0,0,'','','',0.0,'','','','','','','');
INSERT INTO `Clientes` VALUES ('0299','','**baja**ANTONIO JESUS MARTINEZ','PLAZA MAYOR 12-13',' `Cafet. Montemar`','GRN ALACANT','03130','03','','','','74229758-H','1','0','','',0,0,'','','',0.0,'','','','','','','');
INSERT INTO `Clientes` VALUES ('0300','','FUNDACION NORAY `Proyecto Hombre`','PTDA. AGUA AMARGA S/N','','ALICANTE','03008','03','','','','G-53363131','1','0','','',0,0,'','','',0.0,'','','','','','','');
INSERT INTO `Clientes` VALUES ('0128','','**baja**ANTONIA GARCIA','HOLANDA 5','','GRAN ALACANT','03130','03','','','','22450750-J','1','0','','',0,0,'','','',0.0,'','','','','','','');
INSERT INTO `Clientes` VALUES ('0104','','RODRIGUEZ CHARCUTEROS C.B.','C/ LA IGLESIA 8','','EL ALTET','03195','03','','','','E-53472304','1','0','','',0,0,'','CONTADO','',0.0,'','','','','','','');
INSERT INTO `Clientes` VALUES ('0105','','**baja**ROSA Mª NAVARO RIERA','C/ AN JUAN BOSCO 3, BAJO',' `El Pollo Frutero`','ALICANTE','03005','03','','','','21433729-Y','1','0','','',0,0,'','CONTADO','',0.0,'','','','','','','');
INSERT INTO `Clientes` VALUES ('0106','','**baja**DAVID CERDA MARTINEZ','C/ LA TORRE 95','','ELCHE','03204','03','','','','74239960-P','1','0','','',0,0,'','CONTADO','',0.0,'','','','','','','');
INSERT INTO `Clientes` VALUES ('0107','','**baja**MARGARITA MOLLA AGULLO ','C/ LIBERTAD 57','`El Chambao`','TORRELLANO','','03','','','','34485334-W','1','0','','',0,0,'','CONTADO','',0.0,'','','','','','','');
INSERT INTO `Clientes` VALUES ('0108','','CARMELO ESCOLANO MENDEZ','ALICANTE 2','','EL ALTET','','03','','','','21413415-R','1','0','','',0,0,'','CONTADO','',0.0,'','','','','','','');
INSERT INTO `Clientes` VALUES ('0109','','**baja**MARIA DEL CARMEN TORRES','C/ SAN BARTOLOME DE TIRAJANA 20','','AENALES DEL SOL','03195','03','','','','221151796D','1','0','','',0,0,'','','',0.0,'','','','','','','');
INSERT INTO `Clientes` VALUES ('0101','','**baja**OCIO HOTELERO SANTA POLA S.L.','C/ HOLANDA 15','','SANTA POLA','03130','03','','966697897','','B-54747951','1','0','','',0,0,'','CONTADO','',0.0,'','','','','','','');
INSERT INTO `Clientes` VALUES ('0110','','**baja**VENTA VISTALEGRE S.C.','Ctra. Alicante-Cartagena N-332 Km. 88','','Santa Pola','','03','','','','J-54746581','1','0','','',0,0,'','','',0.0,'','','','','','','');
INSERT INTO `Clientes` VALUES ('0111','','**baja**CANTINA AEROPUERTO ALICANTE','C/ Jaime I s/n','','Monforte','03670','03','','','','B53415162','1','0','','',0,0,'','','',0.0,'','','','','','','');
INSERT INTO `Clientes` VALUES ('0112','','MECHEDA S.L. ','Av Dama de Elche 17','','El Altt','','03','','965689218','º','B-54854914','1','0','','',0,0,'','CONTADO','',0.0,'','','','','','','');
INSERT INTO `Clientes` VALUES ('0113','','**baja**Juan Fanisco Snchez Campos','Paseo Tomas Dura 17','Ubanova `Don Arroz`','Alicante','','03','','965188527','','50528635G','1','0','','',0,0,'','CONTADO','',0.0,'','','','','','','');
INSERT INTO `Clientes` VALUES ('0120','','MARIA TERESA RODRIGUEZ BLASCO','AV. CIUDAD DEPORTIVA S/N','','EL ALTET','03195','03','','','','22004031T','1','0','','',0,0,'','CONTADO','',0.0,'','','','','','','');
INSERT INTO `Clientes` VALUES ('0122','','SANSEL C.V.','Av. Cartagena 48','','El Altet','03195','03','','','','F54984141','1','0','','',0,0,'','CONTADO','',0.0,'','','','','','','');
INSERT INTO `Clientes` VALUES ('0130','','**BAJA**BRABEL RESTAURACION SL','REST. NOU ARCOS','ISLAS CANARIAS 2','ARENALES DEL SOL ','','03','','','','B-54776844','1','0','','',0,0,'','','',0.0,'','','','','','','');
INSERT INTO `Clientes` VALUES ('0002','','CLIENTES CONTADO','','','','','','','','','','1','0','','',0,0,'','','',0.0,'','','','','','','');
INSERT INTO `Clientes` VALUES ('0001','','**** FACTURA ANULADA ****','','','','','','','','','','1','0','','',0,0,'','','',0.0,'','','','','','','');
INSERT INTO `Clientes` VALUES ('0131','','CAPS  CUIDADORES S.L.','CEIP RODOLFO TOMAS i SAMPER - L´ALTET ','C/ SEQUIA DE MESTALLA, 2','PICANYA','46210','46','Mª Jose','963976520','','B-97320378','1','0','','',0,0,'','','',0.0,'','','','','','','');
INSERT INTO `Clientes` VALUES ('0132','','RESTAURANTE LA BODEGA S.L.','LIBERTAD 55','','TORRELLANO','','03','','965680395','','B54989827','1','0','','',0,0,'','CONTADO','',0.0,'','','','','','','');
INSERT INTO `Clientes` VALUES ('0135','','PIKOLINO´S INTERCONTINENTAL, S.A.','C/ Galileo Galilei, 2','Elche Parque Industrial','ELCHE','03203','03','Miguel Angel Justicia','966915150','','A-53238713','1','0','','',0,0,'','90DIAS','',0.0,'','','','','','','');
INSERT INTO `Clientes` VALUES ('0136','','PIKOSTORE, S.L.','C/ GALILEO GALILEI, 2','ELCHE PARQUE INDUSTRIAL','TORRELLANO','03203','03','','','','B-53906590','1','0','','',0,0,'','90DIAS','',0.0,'','','','','','','');
INSERT INTO `Clientes` VALUES ('0126','','**baja**KALIFRO, CB  ','CTRA. ELCHE-EL ALTET Km. 10','Pol. 1 Nº 48 Letra A   --- `BRASERIA LOS','El Altet','03195','03','Rogelio Juan','965 26 70 74','','E-42552000','1','0','','',0,0,'','','',0.0,'','','','','','','');
INSERT INTO `Clientes` VALUES ('0297','','**baja**JAVIER JOSÉ SEMPERE GOMIS','Ptda. L´Altet Pol.1 Nº48','Rte. LOS OLIVOS','EL ALTET','03195','03','JAVI','966366910','','48365627-P','1','0','','',0,0,'','','',0.0,'','','','','','','');
INSERT INTO `Clientes` VALUES ('0137','','MOLICOPI, S.L.','Galileo Galilei, 2','Elche Parque Industrial','ELCHE','03320','03','M.A. JUSTICIA - MARIOLA','965681234','','B-03875952','1','0','','',0,0,'','','',0.0,'','','','','','','');
INSERT INTO `Clientes` VALUES ('0203','','LIMENCOP, S.L.','Avda. Salamanca, 27','','Alicante','03005','03','SANTOS','','','B-53212619','1','0','','',0,0,'','','',0.0,'','','','','','','');
INSERT INTO `Clientes` VALUES ('0138','','**baja*RESTAURANTE PRESTIGE SPORT S.L.U.','AV. CARTAGENA','','EL ALTET','','03','','','','B-42606889','1','0','','',0,0,'','CONTADO','',0.0,'','','','','','','');
INSERT INTO `Clientes` VALUES ('0139','','FUNDACION JUAN PERAN - PIKOLINOS','GALILEO GALILEI, 2','ELCHE PARQUE EMPRESARIAL','ELCHE','03203','03','','965681234','','G-54265301','1','0','','',0,0,'','','',0.0,'','','','','','','');
INSERT INTO `Clientes` VALUES ('0140','','**baja**ROSA MARIA MONTEVERDE FERNANDEZ','AV. DAMA DE ELCHE','','EL ALTET','03195','03','ROSA','','','21451754-E','1','0','','',0,0,'','','',0.0,'','','','','','','');
INSERT INTO `Clientes` VALUES ('0218','','** BAJA**PIZZERIA PROVENZAL, S.L.','PASEO TOMAS DURÁ','URBANOVA','ALICANTE','','03','ALAN','','','B-02776797','1','0','','',0,0,'','','',0.0,'','','','','','','ALTA DESDE 2021 ------ANTES HIJOS DE FABRIZZIO');
INSERT INTO `Clientes` VALUES ('0146','','**BAJA**PERLA ARENALES, S.L.','RESTAURANTE LA PERLA','SAN BARTOLOME DE TIRAJANA, 24','ARENALES DEL SOL','03195','03','ANTONIO Y PEDRO','','','B-01628957','1','0','','',0,0,'','','',0.0,'','','','','','','BAJA EN 2022  **DEJA A DEBER**');
INSERT INTO `Clientes` VALUES ('0296','','**BAJA**TALLER DE EDITORES, S.A. ','REVISTA MUJER HOY','C/.JOSEFA VALCARCEL, 40 BIS','MADRID','28027','28','BEGOÑA BAEZA BONMATI','','','A-78509130','1','0','','',0,0,'','','',0.0,'','','','','','','CLIENTE DE BEGOÑA BAEZA    DIRECCION DE ARTE');
INSERT INTO `Clientes` VALUES ('0147','','ALBA VIDAL GUILL - ICE & VICE','Paseo Tomás Durá, 5-6 Local 1','','URBANOVA - ALICANTE','03008','03','','','','2168970-H','1','0','','',0,0,'','CONTADO','',0.0,'','','','','','','');
INSERT INTO `Clientes` VALUES ('0123','','RESTAURANTE VISTA ALEGRE SLU','Ptda. Valverde Bajo, 13','','SANTA POLA','03130','03','','','','B-42560375','1','0','','',0,0,'','','',0.0,'','','','','','','');
INSERT INTO `Clientes` VALUES ('0148','','SERVICIOS INTEGRALES MRH, S.L.','Fragata Almansa, 6','','El Altet','03195','03','JOSE','','','B-04973079','1','0','','',0,0,'','','',0.0,'','','','','','','');
INSERT INTO `Clientes` VALUES ('0149','','PIZZERIA URBANOVA, S.L.','Paseo Tomás Durá, 15 Urbanova','**PROVENZAL**','ALICANTE','03008','03','SERGIO','','','B-72675549','1','0','','',0,0,'','','',0.0,'','','','','','','CLIENE NUEVO DESDE ENERO 2023');
INSERT INTO `Clientes` VALUES ('0150','','NICOLAS Y MORAGUES, ESPJ','LA TENDETA DE JUANI','PTDA. TORRELLANO BAJO, POL.1 NUM.25','TORRELLANO BAJO','03320','03','JUANFRAN Y JUANI - SEVE','622301065','','E-42733030','1','0','','',0,0,'','30DIAS','',0.0,'CAIXABANC','ALICANTE','ALICANTE','03','ES18','21008843220200032551','');
INSERT INTO `Clientes` VALUES ('0295','','LOBO AGENCIA DIGITAL, S.L.','C/Travessia 15E La Marina Ed. BioHub','','','46024','46','','','','B-97845929','1','0','','',0,0,'','','',0.0,'','','','','','','CLIENTE TRABAJO BEGOÑA BAEZA');
INSERT INTO `Clientes` VALUES ('0151','','ADA CILA RUSO ESPINOSA','GABRIEL MIRO, 67-1-4','La Tahona de Ada','SANTA POLA','03130','03','ADA','633684034','','20519123-H','1','0','','',0,1,'','','',0.0,'','','','','','','');
INSERT INTO `Clientes` VALUES ('0152','','JOSE M. MANCHON LLEDO','SAN BARTOLOME DE TIRAJANA, 67 BAJO','`RESTAURANTE ESTRELLA DE MAR`','LOS ARENALES DEL SOL','03195','03','JOSE-MERCE','966 911 331','','74230036-C','1','0','','',0,2,'','CONTADO','',0.0,'','','','','','','');
INSERT INTO `Clientes` VALUES ('0153','','BERNARDO PEREZ VALERA',' `BURGUER BERNA`','SAN BARTOLOME DE TIRAJANA, 11','ARENALES DEL SOL','03195','03','BERNA Y DANI','','','48374021-F','1','0','','',0,0,'','','',0.0,'','','','','','','');
INSERT INTO `Clientes` VALUES ('0294','','AMPA CEIP RODOLFO TOMAS SAMPER','AVDA. CIUDAD DEPORTIVA','','EL ALTET','03195','03','','','','G-03244464','1','0','','',0,0,'','CONTADO','',0.0,'','','','','','','');
INSERT INTO `Clientes` VALUES ('0293','','IES GRAN ALACANT','MUNTANYA DE SANTA POLA, S/N','GRAN ALACANT','SANTA POLA','03130','03','','','','50300141-I','1','0','','',0,0,'','CONTADO','',0.0,'','','','','','','676243976-606515128');
INSERT INTO `Clientes` VALUES ('0201','','ARENALES PLAYA - DESPACHO PROPIO','SAN BARTOLOME DE TIRAJANA','','LOS ARENALES DEL SOL','03195','03','','','','F-54059985','1','0','','',0,0,'','','',0.0,'','','','','','','');
INSERT INTO `Clientes` VALUES ('0154','','HECTOR FABIAN GONZALEZ','`CAMINITO TAPAS BAR`','Avda. Cartagena, 48','EL ALTET','03195','03','FABIAN','','','X-3579041-B','1','0','','',0,0,'','CONTADO','',0.0,'','','','','','','ANTES `SANSEL`, ROGELIO Y ELO');
INSERT INTO `Cuentas` VALUES (2824,'Amortización acumulada de utillaje','','','','',282400000,282499999);
INSERT INTO `Cuentas` VALUES (2825,'Amortización acumulada de otras instalaciones','','','','',282500000,282599999);
INSERT INTO `Cuentas` VALUES (2826,'Amortización acumulada de mobiliario','','','','',282600000,282699999);
INSERT INTO `Cuentas` VALUES (2827,'Amt. acum. de equipos para proceso de información','','','','',282700000,282799999);
INSERT INTO `Cuentas` VALUES (2828,'Amortización acumulada de elementos de transporte','','','','',282800000,282899999);
INSERT INTO `Cuentas` VALUES (2829,'Amortización acumulada otro inmovilizado material','','','','',282900000,282999999);
INSERT INTO `Cuentas` VALUES (2930,'Provisión deprec. partic.capital a largo emp.grupo','','','','',293000000,293099999);
INSERT INTO `Cuentas` VALUES (2935,'Provisión deprec. val renta fija a largo emp.grupo','','','','',293500000,293599999);
INSERT INTO `Cuentas` VALUES (2941,'Provisión deprec. partic.capital a largo emp.asoc.','','','','',294100000,294199999);
INSERT INTO `Cuentas` VALUES (2946,'Provisión deprec. val renta fija a largo emp.asoc.','','','','',294600000,294699999);
INSERT INTO `Cuentas` VALUES (4000,'Proveedores (Pesetas)','','','','',400000000,400099999);
INSERT INTO `Cuentas` VALUES (4004,'Proveedores (Moneda extranjera)','','','','',400400000,400499999);
INSERT INTO `Cuentas` VALUES (4009,'Proveedores facturas pendientes de formalizar','','','','',400900000,400999999);
INSERT INTO `Cuentas` VALUES (4020,'Proveedores empresas del grupo (pesetas)','','','','',402000000,402099999);
INSERT INTO `Cuentas` VALUES (4021,'Efectos comerciales a pagar, empresas del grupo','','','','',402100000,402199999);
INSERT INTO `Cuentas` VALUES (4024,'Proveedores, empresas del grupo(moneda extranjera)','','','','',402400000,402499999);
INSERT INTO `Cuentas` VALUES (4026,'Envases y embalajes a devolver a prov. emp.grupo','','','','',402600000,402699999);
INSERT INTO `Cuentas` VALUES (4029,'Proveedores emp.grupo facturas pendientes','','','','',402900000,402999999);
INSERT INTO `Cuentas` VALUES (4100,'Acreedores prestaciones de servicios (pesetas)','','','','',410000000,410099999);
INSERT INTO `Cuentas` VALUES (4104,'Acreedores prestaciones de servicios (moneda ext.)','','','','',410400000,410499999);
INSERT INTO `Cuentas` VALUES (4109,'Acreedores prestaciones de servicios, facturas','','','','',410900000,410999999);
INSERT INTO `Cuentas` VALUES (4300,'Clientes (pesetas)','','','','',430000000,430099999);
INSERT INTO `Cuentas` VALUES (4304,'Clientes (moneda extranjera)','','','','',430400000,430499999);
INSERT INTO `Cuentas` VALUES (4309,'Clientes, facturas pendientes de formalizar','','','','',430900000,430999999);
INSERT INTO `Cuentas` VALUES (4310,'Efectos comerciales en cartera','','','','',431000000,431099999);
INSERT INTO `Cuentas` VALUES (4311,'Efectos comerciales descontados','','','','',431100000,431199999);
INSERT INTO `Cuentas` VALUES (4312,'Efectos comerciales en gestión de cobro','','','','',431200000,431299999);
INSERT INTO `Cuentas` VALUES (4315,'Efectos comerciales impagados','','','','',431500000,431599999);
INSERT INTO `Cuentas` VALUES (4320,'Clientes, empresas del grupo (pesetas)','','','','',432000000,432099999);
INSERT INTO `Cuentas` VALUES (4321,'Efectos comerciales a cobrar, empresas del grupo','','','','',432100000,432199999);
INSERT INTO `Cuentas` VALUES (4324,'Clientes, empresas del grupo (moneda extranjera)','','','','',432400000,432499999);
INSERT INTO `Cuentas` VALUES (4326,'Envases y embalajes a devolver clientes, emp.grupo','','','','',432600000,432699999);
INSERT INTO `Cuentas` VALUES (4329,'Clientes, empresas del grupo, facturas pendientes ','','','','',432900000,432999999);
INSERT INTO `Cuentas` VALUES (4400,'Deudores (pesetas)','','','','',440000000,440099999);
INSERT INTO `Cuentas` VALUES (4404,'Deudores (moneda extranjera)','','','','',440400000,440499999);
INSERT INTO `Cuentas` VALUES (4409,'Deudores, facturas pendientes de formalizar','','','','',440900000,440999999);
INSERT INTO `Cuentas` VALUES (4410,'Deudores, efectos comerciales en cartera','','','','',441000000,441099999);
INSERT INTO `Cuentas` VALUES (4411,'Deudores, efectos comerciales descontados','','','','',441100000,441199999);
INSERT INTO `Cuentas` VALUES (4412,'Deudores, efectos comerciales en gestión de cobro','','','','',441200000,441299999);
INSERT INTO `Cuentas` VALUES (4415,'Deudores, efectos comerciales impagados','','','','',441500000,441599999);
INSERT INTO `Cuentas` VALUES (4700,'Hacienda Pública, deudor por IVA','','','','',470000000,470099999);
INSERT INTO `Cuentas` VALUES (4708,'Hacienda Pública, deudor subvenciones concedidas','','','','',470800000,470899999);
INSERT INTO `Cuentas` VALUES (4709,'Hacienda Pública, deudor devolución de impuestos','','','','',470900000,470999999);
INSERT INTO `Cuentas` VALUES (4740,'Impuesto sobre beneficios anticipado','','','','',474000000,474099999);
INSERT INTO `Cuentas` VALUES (4745,'Crédito por pérdidas a compensar del ejercicio...','','','','',474500000,474599999);
INSERT INTO `Cuentas` VALUES (4750,'Hacienda Pública, acreedor por IVA.','','','','',475000000,475099999);
INSERT INTO `Cuentas` VALUES (4751,'Hacienda Pública, acreedor retenciones practicadas','','','','',475100000,475199999);
INSERT INTO `Cuentas` VALUES (4752,'Hacienda Pública, acreedor impuesto sociedades','','','','',475200000,475299999);
INSERT INTO `Cuentas` VALUES (4758,'Hacienda Pública, acreedor subvenciones reintegrar','','','','',475800000,475899999);
INSERT INTO `Cuentas` VALUES (5090,'Obligaciones y bonos amortizados','','','','',509000000,509099999);
INSERT INTO `Cuentas` VALUES (5091,'Obligaciones y bonos convertibles amortizados','','','','',509100000,509199999);
INSERT INTO `Cuentas` VALUES (5095,'Otros valores negociables amortizados','','','','',509500000,509599999);
INSERT INTO `Cuentas` VALUES (5100,'Préstamos a corto plazo de empresas del grupo','','','','',510000000,510099999);
INSERT INTO `Cuentas` VALUES (5109,'Otras deudas a corto plazo con empresas del grupo','','','','',510900000,510999999);
INSERT INTO `Cuentas` VALUES (5120,'Préstamos a corto de entidades crédito del grupo','','','','',512000000,512099999);
INSERT INTO `Cuentas` VALUES (5128,'Deudas efectos descontados entidades crédito grupo','','','','',512800000,512899999);
INSERT INTO `Cuentas` VALUES (5129,'Otras deudas a corto entidades crédito del grupo','','','','',512900000,512999999);
INSERT INTO `Cuentas` VALUES (5200,'Préstamos a corto plazo de entidades de crédito','','','','',520000000,520099999);
INSERT INTO `Cuentas` VALUES (5201,'Deudas a corto plazo por crédito dispuesto','','','','',520100000,520199999);
INSERT INTO `Cuentas` VALUES (5208,'Deudas por efectos descontados','','','','',520800000,520899999);
INSERT INTO `Cuentas` VALUES (5360,'Intereses a corto plazo de valores de renta fija','','','','',536000000,536099999);
INSERT INTO `Cuentas` VALUES (5361,'Intereses a corto plazo de créditos a emp. grupo','','','','',536100000,536199999);
INSERT INTO `Cuentas` VALUES (5400,'Inversiones finan. temp. acciones con cotización','','','','',540000000,540099999);
INSERT INTO `Cuentas` VALUES (5401,'Inversiones finan. temp. acciones sin cotización','','','','',540100000,540199999);
INSERT INTO `Cuentas` VALUES (7961,'Exceso provis.particip.capital a largo emp.asoc.','','','','',796100000,796199999);
INSERT INTO `Cuentas` VALUES (7963,'Exceso provis.valores negociables largo otras emp.','','','','',796300000,796399999);
INSERT INTO `Cuentas` VALUES (7965,'Exceso provis.valores r.fija a largo emp.grupo','','','','',796500000,796599999);
INSERT INTO `Cuentas` VALUES (7966,'Exceso provis.valores r.fija a largo emp asoc.','','','','',796600000,796699999);
INSERT INTO `Cuentas` VALUES (7970,'Exceso provis.insolv. créditos a largo emp. grupo','','','','',797000000,797099999);
INSERT INTO `Cuentas` VALUES (7971,'Exceso provis.insolv. créditos a largo emp. asoc.','','','','',797100000,797199999);
INSERT INTO `Cuentas` VALUES (7973,'Exceso provis.insolv. créditos a largo otras emp.','','','','',797300000,797399999);
INSERT INTO `Cuentas` VALUES (7980,'Exceso provis.valores neg. a corto emp. grupo','','','','',798000000,798099999);
INSERT INTO `Cuentas` VALUES (7981,'Exceso provis.valores neg. a corto emp. asoc.','','','','',798100000,798199999);
INSERT INTO `Cuentas` VALUES (7983,'Exceso provis.valores neg. a corto otras emp.','','','','',798300000,798399999);
INSERT INTO `Cuentas` VALUES (7990,'Exceso provis.insolv. créditos a corto emp. grupo','','','','',799000000,799099999);
INSERT INTO `Cuentas` VALUES (7991,'Exceso provis.insolv. créditos a corto emp. asoc.','','','','',799100000,799199999);
INSERT INTO `Cuentas` VALUES (7993,'Exceso provis.insolv. créditos a corto otras emp.','','','','',799300000,799399999);
INSERT INTO `Cuentas` VALUES (10,'Capital','','','','',100000000,109999999);
INSERT INTO `Cuentas` VALUES (11,'Reservas','','','','',110000000,119999999);
INSERT INTO `Cuentas` VALUES (12,'Resultados pendientes de aplicación','','','','',120000000,129999999);
INSERT INTO `Cuentas` VALUES (13,'Ingresos a distribuir en varios ejercicios','','','','',130000000,139999999);
INSERT INTO `Cuentas` VALUES (14,'Provisiones para riesgos y gastos','','','','',140000000,149999999);
INSERT INTO `Cuentas` VALUES (15,'Empréstitos y otras obligaciones análogas','','','','',150000000,159999999);
INSERT INTO `Cuentas` VALUES (16,'Deudas largo plazo con empresas grupo y asociadas','','','','',160000000,169999999);
INSERT INTO `Cuentas` VALUES (17,'Deudas largo plazo préstamos recibidos y otros','','','','',170000000,179999999);
INSERT INTO `Cuentas` VALUES (18,'Fianzas y depósitos recibidos a largo plazo','','','','',180000000,189999999);
INSERT INTO `Cuentas` VALUES (19,'Situaciones transitorias de financiación','','','','',190000000,199999999);
INSERT INTO `Cuentas` VALUES (20,'Gastos de establecimiento','','','','',200000000,209999999);
INSERT INTO `Cuentas` VALUES (21,'Inmovilizaciones inmateriales','','','','',210000000,219999999);
INSERT INTO `Cuentas` VALUES (22,'Inmovilizaciones materiales','','','','',220000000,229999999);
INSERT INTO `Cuentas` VALUES (23,'Inmovilizaciones materiales en curso','','','','',230000000,239999999);
INSERT INTO `Cuentas` VALUES (24,'Inversiones financieras en empresas grupo y asoc.','','','','',240000000,249999999);
INSERT INTO `Cuentas` VALUES (25,'Otras inversiones financieras permanentes','','','','',250000000,259999999);
INSERT INTO `Cuentas` VALUES (26,'Fianzas y depósitos constituidos a largo plazo','','','','',260000000,269999999);
INSERT INTO `Cuentas` VALUES (27,'Gastos a distribuir en varios ejercicios','','','','',270000000,279999999);
INSERT INTO `Cuentas` VALUES (28,'Amortización acumulada del inmovilizado','','','','',280000000,289999999);
INSERT INTO `Cuentas` VALUES (29,'Provisiones de inmovilizado','','','','',290000000,299999999);
INSERT INTO `Cuentas` VALUES (30,'Comerciales','','','','',300000000,309999999);
INSERT INTO `Cuentas` VALUES (31,'Materias primas','','','','',310000000,319999999);
INSERT INTO `Cuentas` VALUES (32,'Otros aprovisionamientos','','','','',320000000,329999999);
INSERT INTO `Cuentas` VALUES (33,'Productos en curso','','','','',330000000,339999999);
INSERT INTO `Cuentas` VALUES (34,'Productos semiterminados','','','','',340000000,349999999);
INSERT INTO `Cuentas` VALUES (35,'Productos terminados','','','','',350000000,359999999);
INSERT INTO `Cuentas` VALUES (36,'Subproductos, residuos y materiales recuperados','','','','',360000000,369999999);
INSERT INTO `Cuentas` VALUES (39,'Provisiones por depreciación de existencias','','','','',390000000,399999999);
INSERT INTO `Cuentas` VALUES (40,'Proveedores','','','','',400000000,409999999);
INSERT INTO `Cuentas` VALUES (41,'Acreedores varios','','','','',410000000,419999999);
INSERT INTO `Cuentas` VALUES (43,'Clientes','','','','',430000000,439999999);
INSERT INTO `Cuentas` VALUES (44,'Deudores varios','','','','',440000000,449999999);
INSERT INTO `Cuentas` VALUES (46,'Personal','','','','',460000000,469999999);
INSERT INTO `Cuentas` VALUES (47,'Administraciones Públicas','','','','',470000000,479999999);
INSERT INTO `Cuentas` VALUES (48,'Ajustes por periodificación','','','','',480000000,489999999);
INSERT INTO `Cuentas` VALUES (49,'Provisiones por operaciones de tráfico','','','','',490000000,499999999);
INSERT INTO `Cuentas` VALUES (50,'Empréstitos y emisiones análogas a corto plazo','','','','',500000000,509999999);
INSERT INTO `Cuentas` VALUES (51,'Deudas a corto plazo empresas grupo y asociadas','','','','',510000000,519999999);
INSERT INTO `Cuentas` VALUES (52,'Deudas corto plazo préstamos recibidos','','','','',520000000,529999999);
INSERT INTO `Cuentas` VALUES (53,'Inversiones financieras corto plazo emp. grupo','','','','',530000000,539999999);
INSERT INTO `Cuentas` VALUES (54,'Otras inversiones financieras temporales','','','','',540000000,549999999);
INSERT INTO `Cuentas` VALUES (55,'Otras cuentas no bancarias','','','','',550000000,559999999);
INSERT INTO `Cuentas` VALUES (56,'Fianzas y depósitos a corto plazo','','','','',560000000,569999999);
INSERT INTO `Cuentas` VALUES (57,'Tesorería','','','','',570000000,579999999);
INSERT INTO `Cuentas` VALUES (58,'Ajustes por periodificación','','','','',580000000,589999999);
INSERT INTO `Cuentas` VALUES (59,'Provisiones financieras','','','','',590000000,599999999);
INSERT INTO `Cuentas` VALUES (60,'Compras','','','','',600000000,609999999);
INSERT INTO `Cuentas` VALUES (61,'Variación de existencias','','','','',610000000,619999999);
INSERT INTO `Cuentas` VALUES (62,'Servicios exteriores','','','','',620000000,629999999);
INSERT INTO `Cuentas` VALUES (63,'Tributos','','','','',630000000,639999999);
INSERT INTO `Cuentas` VALUES (64,'Gastos de personal','','','','',640000000,649999999);
INSERT INTO `Cuentas` VALUES (65,'Otros gastos de gestión','','','','',650000000,659999999);
INSERT INTO `Cuentas` VALUES (66,'Gastos financieros','','','','',660000000,669999999);
INSERT INTO `Cuentas` VALUES (67,'Pérdidas procedentes del inmovilizado y gastos exc','','','','',670000000,679999999);
INSERT INTO `Cuentas` VALUES (68,'Dotaciones para amortizaciones','','','','',680000000,689999999);
INSERT INTO `Cuentas` VALUES (69,'Dotaciones a las provisiones','','','','',690000000,699999999);
INSERT INTO `Cuentas` VALUES (70,'Venta mercaderías producción propia, servicios...','','','','',700000000,709999999);
INSERT INTO `Cuentas` VALUES (71,'Variación de existencias','','','','',710000000,719999999);
INSERT INTO `Cuentas` VALUES (73,'Trabajos realizados para la empresa','','','','',730000000,739999999);
INSERT INTO `Cuentas` VALUES (74,'Subvenciones a la explotación','','','','',740000000,749999999);
INSERT INTO `Cuentas` VALUES (75,'Otros ingresos de gestión','','','','',750000000,759999999);
INSERT INTO `Cuentas` VALUES (76,'Ingresos financieros','','','','',760000000,769999999);
INSERT INTO `Cuentas` VALUES (77,'Beneficios procedentes del inmovilizado e ingresos','','','','',770000000,779999999);
INSERT INTO `Cuentas` VALUES (79,'Excesos y aplicaciones de provisiones','','','','',790000000,799999999);
INSERT INTO `Cuentas` VALUES (100,'Capital social','','','','',100000000,100999999);
INSERT INTO `Cuentas` VALUES (101,'Fondo social','','','','',101000000,101999999);
INSERT INTO `Cuentas` VALUES (102,'Capital','','','','',102000000,102999999);
INSERT INTO `Cuentas` VALUES (110,'Prima de emisión de acciones','','','','',110000000,110999999);
INSERT INTO `Cuentas` VALUES (111,'Reservas de revalorización','','','','',111000000,111999999);
INSERT INTO `Cuentas` VALUES (112,'Reserva legal','','','','',112000000,112999999);
INSERT INTO `Cuentas` VALUES (113,'Reservas especiales','','','','',113000000,113999999);
INSERT INTO `Cuentas` VALUES (114,'Reservas para acciones de la sociedad dominante','','','','',114000000,114999999);
INSERT INTO `Cuentas` VALUES (115,'Reservas para acciones propias','','','','',115000000,115999999);
INSERT INTO `Cuentas` VALUES (116,'Reservas estatutarias','','','','',116000000,116999999);
INSERT INTO `Cuentas` VALUES (117,'Reservas voluntarias','','','','',117000000,117999999);
INSERT INTO `Cuentas` VALUES (118,'Reserva por capital amortizado','','','','',118000000,118999999);
INSERT INTO `Cuentas` VALUES (120,'Remanente','','','','',120000000,120999999);
INSERT INTO `Cuentas` VALUES (121,'Resultados negativos de ejercicios anteriores','','','','',121000000,121999999);
INSERT INTO `Cuentas` VALUES (122,'Aportaciones de socios para compensación pérdidas','','','','',122000000,122999999);
INSERT INTO `Cuentas` VALUES (129,'Pérdidas y ganancias','','','','',129000000,129999999);
INSERT INTO `Cuentas` VALUES (130,'Subvenciones oficiales de capital','','','','',130000000,130999999);
INSERT INTO `Cuentas` VALUES (131,'Subvenciones de capital','','','','',131000000,131999999);
INSERT INTO `Cuentas` VALUES (135,'Ingresos por intereses diferidos','','','','',135000000,135999999);
INSERT INTO `Cuentas` VALUES (136,'Diferencias positivas en moneda extranjera','','','','',136000000,136999999);
INSERT INTO `Cuentas` VALUES (140,'Provisión para pensiones y obligaciones similares','','','','',140000000,140999999);
INSERT INTO `Cuentas` VALUES (141,'Provisión para impuestos','','','','',141000000,141999999);
INSERT INTO `Cuentas` VALUES (142,'Provisión para responsabilidades','','','','',142000000,142999999);
INSERT INTO `Cuentas` VALUES (143,'Provisión para grandes reparaciones','','','','',143000000,143999999);
INSERT INTO `Cuentas` VALUES (144,'Fondo de reversión','','','','',144000000,144999999);
INSERT INTO `Cuentas` VALUES (150,'Obligaciones y bonos','','','','',150000000,150999999);
INSERT INTO `Cuentas` VALUES (151,'Obligaciones y bonos convertibles','','','','',151000000,151999999);
INSERT INTO `Cuentas` VALUES (155,'Deudas representadas en otros valores negociables','','','','',155000000,155999999);
INSERT INTO `Cuentas` VALUES (160,'Deudas a largo plazo con empresas del grupo','','','','',160000000,160999999);
INSERT INTO `Cuentas` VALUES (161,'Deudas a largo plazo con empresas asociadas','','','','',161000000,161999999);
INSERT INTO `Cuentas` VALUES (162,'Deudas largo plazo con entidades crédito del grupo','','','','',162000000,162999999);
INSERT INTO `Cuentas` VALUES (163,'Deudas a largo plazo entidades de crédito asoc.','','','','',163000000,163999999);
INSERT INTO `Cuentas` VALUES (164,'Proveedores de inmov. largo plazo empresas grupo','','','','',164000000,164999999);
INSERT INTO `Cuentas` VALUES (165,'Proveedores de inmov. largo plazo empresas asoc.','','','','',165000000,165999999);
INSERT INTO `Cuentas` VALUES (170,'Deudas largo plazo con entidades de crédito','','','','',170000000,170999999);
INSERT INTO `Cuentas` VALUES (171,'Deudas largo plazo','','','','',171000000,171999999);
INSERT INTO `Cuentas` VALUES (172,'Deudas largo plazo transformables en subvenciones','','','','',172000000,172999999);
INSERT INTO `Cuentas` VALUES (173,'Proveedores de inmovilizado a largo plazo','','','','',173000000,173999999);
INSERT INTO `Cuentas` VALUES (174,'Efectos a pagar a largo plazo','','','','',174000000,174999999);
INSERT INTO `Cuentas` VALUES (180,'Fianzas recibidas a largo plazo','','','','',180000000,180999999);
INSERT INTO `Cuentas` VALUES (185,'Depósitos recibidos a largo plazo','','','','',185000000,185999999);
INSERT INTO `Cuentas` VALUES (190,'Accionistas por desembolsos no exigidos','','','','',190000000,190999999);
INSERT INTO `Cuentas` VALUES (191,'Accionistas desembolsos no exigidos emp.grupo','','','','',191000000,191999999);
INSERT INTO `Cuentas` VALUES (192,'Accionistas desembolsos no exigidos emp. asoc.','','','','',192000000,192999999);
INSERT INTO `Cuentas` VALUES (193,'Accionistas aportaciones no dinerarias pendientes','','','','',193000000,193999999);
INSERT INTO `Cuentas` VALUES (194,'Accionistas aport. no dinerarias ptes. emp. grupo','','','','',194000000,194999999);
INSERT INTO `Cuentas` VALUES (195,'Accionistas aport. no dinerarias ptes. emp. asoc.','','','','',195000000,195999999);
INSERT INTO `Cuentas` VALUES (196,'Socios parte no desembolsada','','','','',196000000,196999999);
INSERT INTO `Cuentas` VALUES (198,'Acciones propias en situaciones especiales','','','','',198000000,198999999);
INSERT INTO `Cuentas` VALUES (200,'Gastos de constitución','','','','',200000000,200999999);
INSERT INTO `Cuentas` VALUES (201,'Gastos de primer establecimiento','','','','',201000000,201999999);
INSERT INTO `Cuentas` VALUES (202,'Gastos ampliación de capital','','','','',202000000,202999999);
INSERT INTO `Cuentas` VALUES (210,'Gastos de investigación y desarrollo','','','','',210000000,210999999);
INSERT INTO `Cuentas` VALUES (211,'Concesiones administrativas','','','','',211000000,211999999);
INSERT INTO `Cuentas` VALUES (212,'Propiedad industrial','','','','',212000000,212999999);
INSERT INTO `Cuentas` VALUES (213,'Fondo de comercio','','','','',213000000,213999999);
INSERT INTO `Cuentas` VALUES (214,'Derechos de transpaso','','','','',214000000,214999999);
INSERT INTO `Cuentas` VALUES (215,'Aplicaciones informáticas','','','','',215000000,215999999);
INSERT INTO `Cuentas` VALUES (217,'Derechos sobre bienes arrendamientos financieros','','','','',217000000,217999999);
INSERT INTO `Cuentas` VALUES (219,'Anticipos de inmovilizaciones inmateriales','','','','',219000000,219999999);
INSERT INTO `Cuentas` VALUES (220,'Terrenos y bienes naturales','','','','',220000000,220999999);
INSERT INTO `Cuentas` VALUES (221,'Construcciones','','','','',221000000,221999999);
INSERT INTO `Cuentas` VALUES (222,'Instalaciones técnicas','','','','',222000000,222999999);
INSERT INTO `Cuentas` VALUES (223,'Maquinaria','','','','',223000000,223999999);
INSERT INTO `Cuentas` VALUES (224,'Utillaje','','','','',224000000,224999999);
INSERT INTO `Cuentas` VALUES (225,'Otras instalaciones','','','','',225000000,225999999);
INSERT INTO `Cuentas` VALUES (226,'Mobiliario','','','','',226000000,226999999);
INSERT INTO `Cuentas` VALUES (227,'Equipos para proceso de información','','','','',227000000,227999999);
INSERT INTO `Cuentas` VALUES (228,'Elementos de transporte','','','','',228000000,228999999);
INSERT INTO `Cuentas` VALUES (229,'Otro inmovilizado material','','','','',229000000,229999999);
INSERT INTO `Cuentas` VALUES (230,'Adaptación de terrenos y bienes naturales','','','','',230000000,230999999);
INSERT INTO `Cuentas` VALUES (231,'Construcciones en curso','','','','',231000000,231999999);
INSERT INTO `Cuentas` VALUES (232,'Instalaciones técnicas en montaje','','','','',232000000,232999999);
INSERT INTO `Cuentas` VALUES (233,'Maquinaria en montaje','','','','',233000000,233999999);
INSERT INTO `Cuentas` VALUES (237,'Equipos para procesos de información en montaje','','','','',237000000,237999999);
INSERT INTO `Cuentas` VALUES (239,'Anticipos para inmovilizaciones materiales','','','','',239000000,239999999);
INSERT INTO `Cuentas` VALUES (240,'Participación en empresas del grupo','','','','',240000000,240999999);
INSERT INTO `Cuentas` VALUES (241,'Participación en empresas asociadas','','','','',241000000,241999999);
INSERT INTO `Cuentas` VALUES (242,'Valores de renta fija empresas del grupo','','','','',242000000,242999999);
INSERT INTO `Cuentas` VALUES (243,'Valores de renta fija empresas asociadas','','','','',243000000,243999999);
INSERT INTO `Cuentas` VALUES (244,'Créditos largo plazo empresas grupo','','','','',244000000,244999999);
INSERT INTO `Cuentas` VALUES (245,'Créditos largo plazo empresas asociadas','','','','',245000000,245999999);
INSERT INTO `Cuentas` VALUES (246,'Intereses largo plazo inversiones financ.emp.grupo','','','','',246000000,246999999);
INSERT INTO `Cuentas` VALUES (247,'Intereses largo plazo inversiones financ.emp.asoc.','','','','',247000000,247999999);
INSERT INTO `Cuentas` VALUES (248,'Desembolsos pendientes sobre acciones emp. grupo','','','','',248000000,248999999);
INSERT INTO `Cuentas` VALUES (249,'Desembolsos pendientes sobre acciones emp.asoc.','','','','',249000000,249999999);
INSERT INTO `Cuentas` VALUES (250,'Inversiones financieras permanentes en capital','','','','',250000000,250999999);
INSERT INTO `Cuentas` VALUES (251,'Valores de renta fija','','','','',251000000,251999999);
INSERT INTO `Cuentas` VALUES (252,'Créditos a largo plazo','','','','',252000000,252999999);
INSERT INTO `Cuentas` VALUES (253,'Créditos largo plazo enajenacion de inmovilizado','','','','',253000000,253999999);
INSERT INTO `Cuentas` VALUES (254,'Créditos a largo plazo al personal','','','','',254000000,254999999);
INSERT INTO `Cuentas` VALUES (256,'Intereses a largo plazo valores de renta fija','','','','',256000000,256999999);
INSERT INTO `Cuentas` VALUES (257,'Intereses largo plazo créditos','','','','',257000000,257999999);
INSERT INTO `Cuentas` VALUES (258,'Imposiciones a largo plazo','','','','',258000000,258999999);
INSERT INTO `Cuentas` VALUES (259,'Desembolsos pendientes sobre acciones','','','','',259000000,259999999);
INSERT INTO `Cuentas` VALUES (260,'Fianzas constituidas a largo plazo','','','','',260000000,260999999);
INSERT INTO `Cuentas` VALUES (265,'Depósitos constituidos a largo plazo','','','','',265000000,265999999);
INSERT INTO `Cuentas` VALUES (270,'Gastos de formalización de deudas','','','','',270000000,270999999);
INSERT INTO `Cuentas` VALUES (271,'Gastos por intereses diferidos valores negociables','','','','',271000000,271999999);
INSERT INTO `Cuentas` VALUES (272,'Gastos por intereses diferidos','','','','',272000000,272999999);
INSERT INTO `Cuentas` VALUES (281,'Amortización acumulada del inmovilizado inmaterial','','','','',281000000,281999999);
INSERT INTO `Cuentas` VALUES (282,'Amortización acumulada del inmovilizado material','','','','',282000000,282999999);
INSERT INTO `Cuentas` VALUES (291,'Provisión depreciación inmovilizado inmaterial','','','','',291000000,291999999);
INSERT INTO `Cuentas` VALUES (292,'Provisión depreciación inmovilizado material','','','','',292000000,292999999);
INSERT INTO `Cuentas` VALUES (293,'Provisión deprec. valores neg. a largo emp.grupo','','','','',293000000,293999999);
INSERT INTO `Cuentas` VALUES (294,'Provisión deprec. valores neg. a largo emp.asoc.','','','','',294000000,294999999);
INSERT INTO `Cuentas` VALUES (295,'Provisión insolvencias créditos a largo emp. grupo','','','','',295000000,295999999);
INSERT INTO `Cuentas` VALUES (296,'Provisión insolvencias créditos a largo emp.asoc.','','','','',296000000,296999999);
INSERT INTO `Cuentas` VALUES (297,'Provisión depreciación valores neg. largo plazo','','','','',297000000,297999999);
INSERT INTO `Cuentas` VALUES (298,'Provisión insolvencias créditos a largo plazo','','','','',298000000,298999999);
INSERT INTO `Cuentas` VALUES (300,'Mercaderías A','','','','',300000000,300999999);
INSERT INTO `Cuentas` VALUES (301,'Mercaderías B','','','','',301000000,301999999);
INSERT INTO `Cuentas` VALUES (310,'Materias primas A','','','','',310000000,310999999);
INSERT INTO `Cuentas` VALUES (311,'Materias primas B','','','','',311000000,311999999);
INSERT INTO `Cuentas` VALUES (320,'Elementos y conjuntos incorporables','','','','',320000000,320999999);
INSERT INTO `Cuentas` VALUES (321,'Combustibles','','','','',321000000,321999999);
INSERT INTO `Cuentas` VALUES (322,'Repuestos','','','','',322000000,322999999);
INSERT INTO `Cuentas` VALUES (325,'Materiales diversos','','','','',325000000,325999999);
INSERT INTO `Cuentas` VALUES (326,'Embalajes','','','','',326000000,326999999);
INSERT INTO `Cuentas` VALUES (327,'Envases','','','','',327000000,327999999);
INSERT INTO `Cuentas` VALUES (328,'Material de oficina','','','','',328000000,328999999);
INSERT INTO `Cuentas` VALUES (330,'Productos en curso A','','','','',330000000,330999999);
INSERT INTO `Cuentas` VALUES (331,'Productos en curso B','','','','',331000000,331999999);
INSERT INTO `Cuentas` VALUES (340,'Productos semiterminados A','','','','',340000000,340999999);
INSERT INTO `Cuentas` VALUES (341,'Productos semiterminados B','','','','',341000000,341999999);
INSERT INTO `Cuentas` VALUES (350,'Productos terminados A','','','','',350000000,350999999);
INSERT INTO `Cuentas` VALUES (351,'Productos terminados B','','','','',351000000,351999999);
INSERT INTO `Cuentas` VALUES (360,'Subproductos A','','','','',360000000,360999999);
INSERT INTO `Cuentas` VALUES (361,'Subproductos B','','','','',361000000,361999999);
INSERT INTO `Cuentas` VALUES (365,'Residuos A','','','','',365000000,365999999);
INSERT INTO `Cuentas` VALUES (366,'Residuos B','','','','',366000000,366999999);
INSERT INTO `Cuentas` VALUES (368,'Materiales recuperados A','','','','',368000000,368999999);
INSERT INTO `Cuentas` VALUES (369,'Materiales recuperados B','','','','',369000000,369999999);
INSERT INTO `Cuentas` VALUES (390,'Provisión depreciación de mercaderias','','','','',390000000,390999999);
INSERT INTO `Cuentas` VALUES (391,'Provisión depreciación de materias primas','','','','',391000000,391999999);
INSERT INTO `Cuentas` VALUES (392,'Provisión depreciación otros aprovisionamientos','','','','',392000000,392999999);
INSERT INTO `Cuentas` VALUES (393,'Provisión depreciación productos en curso','','','','',393000000,393999999);
INSERT INTO `Cuentas` VALUES (394,'Provisión depreciación productos semiterminados','','','','',394000000,394999999);
INSERT INTO `Cuentas` VALUES (395,'Provisión depreciación productos terminados','','','','',395000000,395999999);
INSERT INTO `Cuentas` VALUES (396,'Provisión deprec. subproductos residuos mat.recup.','','','','',396000000,396999999);
INSERT INTO `Cuentas` VALUES (400,'Proveedores','','','','',400000000,400999999);
INSERT INTO `Cuentas` VALUES (401,'Proveedores efectos comerciales a pagar','','','','',401000000,401999999);
INSERT INTO `Cuentas` VALUES (402,'Proveedores empresas del grupo','','','','',402000000,402999999);
INSERT INTO `Cuentas` VALUES (403,'Proveedores empresas asociadas','','','','',403000000,403999999);
INSERT INTO `Cuentas` VALUES (406,'Envases y embalajes a devolver a proveedores','','','','',406000000,406999999);
INSERT INTO `Cuentas` VALUES (407,'Anticipos a proveedores','','','','',407000000,407999999);
INSERT INTO `Cuentas` VALUES (410,'Acreedores por prestaciones de servicios','','','','',410000000,410999999);
INSERT INTO `Cuentas` VALUES (411,'Acreedores efectos comerciales a pagar','','','','',411000000,411999999);
INSERT INTO `Cuentas` VALUES (419,'Acreedores por operaciones en común','','','','',419000000,419999999);
INSERT INTO `Cuentas` VALUES (430,'Clientes','','','','',430000000,430999999);
INSERT INTO `Cuentas` VALUES (431,'Clientes efectos comerciales a cobrar','','','','',431000000,431999999);
INSERT INTO `Cuentas` VALUES (432,'Clientes empresas del grupo','','','','',432000000,432999999);
INSERT INTO `Cuentas` VALUES (433,'Clientes empresas asociadas','','','','',433000000,433999999);
INSERT INTO `Cuentas` VALUES (435,'Clientes de dudoso cobro','','','','',435000000,435999999);
INSERT INTO `Cuentas` VALUES (436,'Envases y embalajes a devolver por clientes','','','','',436000000,436999999);
INSERT INTO `Cuentas` VALUES (437,'Anticipos de clientes','','','','',437000000,437999999);
INSERT INTO `Cuentas` VALUES (440,'Deudores','','','','',440000000,440999999);
INSERT INTO `Cuentas` VALUES (441,'Deudores efectos comerciales a cobrar','','','','',441000000,441999999);
INSERT INTO `Cuentas` VALUES (445,'Deudores de dudoso cobro','','','','',445000000,445999999);
INSERT INTO `Cuentas` VALUES (449,'Deudores por operaciones en común','','','','',449000000,449999999);
INSERT INTO `Cuentas` VALUES (460,'Anticipos de remuneraciones','','','','',460000000,460999999);
INSERT INTO `Cuentas` VALUES (465,'Remuneraciones pendientes de pago','','','','',465000000,465999999);
INSERT INTO `Cuentas` VALUES (470,'Hacienda pública deudor por diversos conceptos','','','','',470000000,470999999);
INSERT INTO `Cuentas` VALUES (471,'Organismos de la seguridad social, deudores','','','','',471000000,471999999);
INSERT INTO `Cuentas` VALUES (472,'Hacienda pública iva soportado','','','','',472000000,472999999);
INSERT INTO `Cuentas` VALUES (473,'Hacienda pública, retenciones y pagos a cuenta','','','','',473000000,473999999);
INSERT INTO `Cuentas` VALUES (474,'Impuesto beneficios anticipados y comp. pérdidas','','','','',474000000,474999999);
INSERT INTO `Cuentas` VALUES (475,'Hacienda pública acreedor por conceptos fiscales','','','','',475000000,475999999);
INSERT INTO `Cuentas` VALUES (476,'Organismos de la seguridad social acreedores','','','','',476000000,476999999);
INSERT INTO `Cuentas` VALUES (477,'Hacienda pública iva repercutido','','','','',477000000,477999999);
INSERT INTO `Cuentas` VALUES (479,'Impuesto sobre beneficios diferido','','','','',479000000,479999999);
INSERT INTO `Cuentas` VALUES (480,'Gastos anticipados','','','','',480000000,480999999);
INSERT INTO `Cuentas` VALUES (485,'Ingresos anticipados','','','','',485000000,485999999);
INSERT INTO `Cuentas` VALUES (490,'Provisión para insolvencias de tráfico','','','','',490000000,490999999);
INSERT INTO `Cuentas` VALUES (493,'Provisión insolvencias tráfico empresas del grupo','','','','',493000000,493999999);
INSERT INTO `Cuentas` VALUES (494,'Provisión insolvencias tráfico empresas asociadas','','','','',494000000,494999999);
INSERT INTO `Cuentas` VALUES (499,'Provisión para otras operaciones de tráfico','','','','',499000000,499999999);
INSERT INTO `Cuentas` VALUES (500,'Obligaciones y bonos a corto plazo','','','','',500000000,500999999);
INSERT INTO `Cuentas` VALUES (501,'Obligaciones y bonos convertibles a corto plazo','','','','',501000000,501999999);
INSERT INTO `Cuentas` VALUES (505,'Deudas representadas en otros valores neg. a corto','','','','',505000000,505999999);
INSERT INTO `Cuentas` VALUES (506,'Intereses empréstitos y otras emisiones análogas','','','','',506000000,506999999);
INSERT INTO `Cuentas` VALUES (509,'Valores negociables amortizados','','','','',509000000,509999999);
INSERT INTO `Cuentas` VALUES (510,'Deudas a corto plazo con empresas del grupo','','','','',510000000,510999999);
INSERT INTO `Cuentas` VALUES (511,'Deudas a corto plazo con empresas asociadas','','','','',511000000,511999999);
INSERT INTO `Cuentas` VALUES (512,'Deudas a corto plazo entidades crédito del grupo','','','','',512000000,512999999);
INSERT INTO `Cuentas` VALUES (513,'Deudas a corto plazo entidades crédito asociadas','','','','',513000000,513999999);
INSERT INTO `Cuentas` VALUES (514,'Proveedores de inmovilizado a corto emp. grupo','','','','',514000000,514999999);
INSERT INTO `Cuentas` VALUES (515,'Proveedores de inmovilizado a corto emp. asociadas','','','','',515000000,515999999);
INSERT INTO `Cuentas` VALUES (516,'Intereses a corto plazo de deudas com emp. grupo','','','','',516000000,516999999);
INSERT INTO `Cuentas` VALUES (517,'Intereses a corto plazo de deudas con emp. asoc.','','','','',517000000,517999999);
INSERT INTO `Cuentas` VALUES (520,'Deudas a corto plazo con entidades de crédito','','','','',520000000,520999999);
INSERT INTO `Cuentas` VALUES (521,'Deudas a corto plazo','','','','',521000000,521999999);
INSERT INTO `Cuentas` VALUES (523,'Proveedores de inmovilizado a corto plazo','','','','',523000000,523999999);
INSERT INTO `Cuentas` VALUES (524,'Efectos comerciales a pagar a corto plazo','','','','',524000000,524999999);
INSERT INTO `Cuentas` VALUES (525,'Dividendo activo a pagar','','','','',525000000,525999999);
INSERT INTO `Cuentas` VALUES (526,'Intereses a corto plazo deudas entidades crédito','','','','',526000000,526999999);
INSERT INTO `Cuentas` VALUES (527,'Intereses a corto plazo de deudas','','','','',527000000,527999999);
INSERT INTO `Cuentas` VALUES (530,'Participaciones a corto plazo en empresas grupo','','','','',530000000,530999999);
INSERT INTO `Cuentas` VALUES (531,'Participaciones a corto plazo en empresas asoc.','','','','',531000000,531999999);
INSERT INTO `Cuentas` VALUES (532,'Valores de renta fija a corto plazo empresas grupo','','','','',532000000,532999999);
INSERT INTO `Cuentas` VALUES (533,'Valores de renta fija corto plazo empresas asoc.','','','','',533000000,533999999);
INSERT INTO `Cuentas` VALUES (534,'Créditos a corto plazo a empresas del grupo','','','','',534000000,534999999);
INSERT INTO `Cuentas` VALUES (535,'Créditos a corto plazo a empresas asociadas','','','','',535000000,535999999);
INSERT INTO `Cuentas` VALUES (536,'Intereses a corto inversiones financ. emp. grupo','','','','',536000000,536999999);
INSERT INTO `Cuentas` VALUES (537,'Intereses a corto inversiones financ. emp. asoc.','','','','',537000000,537999999);
INSERT INTO `Cuentas` VALUES (538,'Desembolsos pendientes acciones a corto emp.grup.','','','','',538000000,538999999);
INSERT INTO `Cuentas` VALUES (539,'Desembolsos pendientes.acciones a corto emp.asoc.','','','','',539000000,539999999);
INSERT INTO `Cuentas` VALUES (540,'Inversiones financieras temporales en capital','','','','',540000000,540999999);
INSERT INTO `Cuentas` VALUES (541,'Valores de renta fija a corto plazo','','','','',541000000,541999999);
INSERT INTO `Cuentas` VALUES (542,'Créditos a corto plazo','','','','',542000000,542999999);
INSERT INTO `Cuentas` VALUES (543,'Créditos a corto por enajenación del inmovilizado','','','','',543000000,543999999);
INSERT INTO `Cuentas` VALUES (544,'Créditos a corto plazo al personal','','','','',544000000,544999999);
INSERT INTO `Cuentas` VALUES (545,'Dividendo a cobrar','','','','',545000000,545999999);
INSERT INTO `Cuentas` VALUES (546,'Intereses a corto plazo de valores renta fija','','','','',546000000,546999999);
INSERT INTO `Cuentas` VALUES (547,'Intereses a corto plazo de créditos','','','','',547000000,547999999);
INSERT INTO `Cuentas` VALUES (548,'Imposiciones a corto plazo','','','','',548000000,548999999);
INSERT INTO `Cuentas` VALUES (549,'Desembolsos pendientes sobre acciones a corto','','','','',549000000,549999999);
INSERT INTO `Cuentas` VALUES (550,'Titular de la explotación','','','','',550000000,550999999);
INSERT INTO `Cuentas` VALUES (551,'Cuenta corriente con empresas del grupo','','','','',551000000,551999999);
INSERT INTO `Cuentas` VALUES (552,'Cuenta corriente con empresas asociadas','','','','',552000000,552999999);
INSERT INTO `Cuentas` VALUES (553,'Cuenta corriente con socios y administradores','','','','',553000000,553999999);
INSERT INTO `Cuentas` VALUES (555,'Partidas pendientes de aplicación','','','','',555000000,555999999);
INSERT INTO `Cuentas` VALUES (556,'Desembolsos exigidos sobre acciones','','','','',556000000,556999999);
INSERT INTO `Cuentas` VALUES (557,'Dividendo activo a cuenta','','','','',557000000,557999999);
INSERT INTO `Cuentas` VALUES (558,'Accionistas por desembolsos exigidos','','','','',558000000,558999999);
INSERT INTO `Cuentas` VALUES (560,'Fianzas recibidas a corto plazo','','','','',560000000,560999999);
INSERT INTO `Cuentas` VALUES (561,'Depósitos recibidos a corto plazo','','','','',561000000,561999999);
INSERT INTO `Cuentas` VALUES (565,'Fianzas constituidas a corto plazo','','','','',565000000,565999999);
INSERT INTO `Cuentas` VALUES (566,'Depósitos constituidos a corto plazo','','','','',566000000,566999999);
INSERT INTO `Cuentas` VALUES (570,'Caja pesetas','','','','',570000000,570999999);
INSERT INTO `Cuentas` VALUES (571,'Caja moneda extranjera','','','','',571000000,571999999);
INSERT INTO `Cuentas` VALUES (572,'Bancos e inst. crédito c/c vista, pesetas','','','','',572000000,572999999);
INSERT INTO `Cuentas` VALUES (573,'Bancos e inst. crédito c/c vista, moneda extr.','','','','',573000000,573999999);
INSERT INTO `Cuentas` VALUES (574,'Bancos e inst. crédito cuentas ahorro pesetas','','','','',574000000,574999999);
INSERT INTO `Cuentas` VALUES (575,'Bancos e inst. crédito cuentas ahorro moneda extr.','','','','',575000000,575999999);
INSERT INTO `Cuentas` VALUES (580,'Intereses pagados por anticipado','','','','',580000000,580999999);
INSERT INTO `Cuentas` VALUES (585,'Intereses cobrados por anticipado','','','','',585000000,585999999);
INSERT INTO `Cuentas` VALUES (593,'Provisión deprec. valores neg. a corto emp. grupo','','','','',593000000,593999999);
INSERT INTO `Cuentas` VALUES (594,'Provisión deprec. valores neg. a corto emp. asoc.','','','','',594000000,594999999);
INSERT INTO `Cuentas` VALUES (595,'Provisión insolvencias créditos a corto emp. grupo','','','','',595000000,595999999);
INSERT INTO `Cuentas` VALUES (596,'Provisión insolvencias créditos corto emp. asoc.','','','','',596000000,596999999);
INSERT INTO `Cuentas` VALUES (597,'Provisión depreciación valores negociables a corto','','','','',597000000,597999999);
INSERT INTO `Cuentas` VALUES (598,'Provisión insolvencias créditos a corto plazo','','','','',598000000,598999999);
INSERT INTO `Cuentas` VALUES (600,'Compras de mercaderias','','','','',600000000,600999999);
INSERT INTO `Cuentas` VALUES (601,'Compras de materias primas','','','','',601000000,601999999);
INSERT INTO `Cuentas` VALUES (602,'Compras de otros aprovisionamientos','','','','',602000000,602999999);
INSERT INTO `Cuentas` VALUES (607,'Trabajos realizados por otras empresas','','','','',607000000,607999999);
INSERT INTO `Cuentas` VALUES (608,'Devoluciones de compras y operaciones similares','','','','',608000000,608999999);
INSERT INTO `Cuentas` VALUES (609,'`rappels` por compras','','','','',609000000,609999999);
INSERT INTO `Cuentas` VALUES (610,'Variación de existencias de mercaderias','','','','',610000000,610999999);
INSERT INTO `Cuentas` VALUES (611,'Variación de existencias de materias primas','','','','',611000000,611999999);
INSERT INTO `Cuentas` VALUES (612,'Variación de existencias otros aprovisionamientos','','','','',612000000,612999999);
INSERT INTO `Cuentas` VALUES (620,'Investigación y desarrollo','','','','',620000000,620999999);
INSERT INTO `Cuentas` VALUES (621,'Arrendamientos y canones','','','','',621000000,621999999);
INSERT INTO `Cuentas` VALUES (622,'Reparaciones y conservación','','','','',622000000,622999999);
INSERT INTO `Cuentas` VALUES (623,'Servicios de profesionales independientes','','','','',623000000,623999999);
INSERT INTO `Cuentas` VALUES (624,'Transportes','','','','',624000000,624999999);
INSERT INTO `Cuentas` VALUES (625,'Primas de seguros','','','','',625000000,625999999);
INSERT INTO `Cuentas` VALUES (626,'Servicios bancarios y similares','','','','',626000000,626999999);
INSERT INTO `Cuentas` VALUES (627,'Publicidad, propaganda y relaciones públicas','','','','',627000000,627999999);
INSERT INTO `Cuentas` VALUES (628,'Suministros','','','','',628000000,628999999);
INSERT INTO `Cuentas` VALUES (629,'Otros servicios','','','','',629000000,629999999);
INSERT INTO `Cuentas` VALUES (630,'Impuesto sobre beneficios','','','','',630000000,630999999);
INSERT INTO `Cuentas` VALUES (631,'Otros tributos','','','','',631000000,631999999);
INSERT INTO `Cuentas` VALUES (633,'Ajustes negativos en la imposición sobre benefic.','','','','',633000000,633999999);
INSERT INTO `Cuentas` VALUES (634,'Ajustes negativos en la imposición indirecta','','','','',634000000,634999999);
INSERT INTO `Cuentas` VALUES (636,'Devolución de impuestos','','','','',636000000,636999999);
INSERT INTO `Cuentas` VALUES (638,'Ajustes positivos en la imposición sobre benefic.','','','','',638000000,638999999);
INSERT INTO `Cuentas` VALUES (639,'Ajustes positivos en la imposición indirecta','','','','',639000000,639999999);
INSERT INTO `Cuentas` VALUES (640,'Sueldos y salarios','','','','',640000000,640999999);
INSERT INTO `Cuentas` VALUES (641,'Indemnizaciones','','','','',641000000,641999999);
INSERT INTO `Cuentas` VALUES (642,'Seguridad social a cargo de la empresa','','','','',642000000,642999999);
INSERT INTO `Cuentas` VALUES (643,'Aportaciones a sistemas complement.de pensiones','','','','',643000000,643999999);
INSERT INTO `Cuentas` VALUES (649,'Otros gastos sociales','','','','',649000000,649999999);
INSERT INTO `Cuentas` VALUES (650,'Pérdidas de créditos comerciales incobrables','','','','',650000000,650999999);
INSERT INTO `Cuentas` VALUES (651,'Resultados de operaciones en común','','','','',651000000,651999999);
INSERT INTO `Cuentas` VALUES (659,'Otras pérdidas en gestión corriente','','','','',659000000,659999999);
INSERT INTO `Cuentas` VALUES (661,'Intereses de obligaciones y bonos','','','','',661000000,661999999);
INSERT INTO `Cuentas` VALUES (662,'Intereses de deudas a largo plazo','','','','',662000000,662999999);
INSERT INTO `Cuentas` VALUES (663,'Intereses de deudas a corto plazo','','','','',663000000,663999999);
INSERT INTO `Cuentas` VALUES (664,'Intereses por descuento de efectos','','','','',664000000,664999999);
INSERT INTO `Cuentas` VALUES (665,'Descuentos sobre ventas por pronto pago','','','','',665000000,665999999);
INSERT INTO `Cuentas` VALUES (666,'Pérdidas en valores mobiliarios','','','','',666000000,666999999);
INSERT INTO `Cuentas` VALUES (667,'Pérdidas de créditos','','','','',667000000,667999999);
INSERT INTO `Cuentas` VALUES (668,'Diferencias negativas de cambio','','','','',668000000,668999999);
INSERT INTO `Cuentas` VALUES (669,'Otros gastos financieros','','','','',669000000,669999999);
INSERT INTO `Cuentas` VALUES (670,'Pérdidas procedentes de inmovilizado inmaterial','','','','',670000000,670999999);
INSERT INTO `Cuentas` VALUES (671,'Pérdidas procedentes del inmovilizado material','','','','',671000000,671999999);
INSERT INTO `Cuentas` VALUES (672,'Pérdidas procedentes de valores a largo plazo','','','','',672000000,672999999);
INSERT INTO `Cuentas` VALUES (673,'Pérdidas de créditos a largo plazo','','','','',673000000,673999999);
INSERT INTO `Cuentas` VALUES (674,'Pérdidas operaciones acciones y oblig. propias','','','','',674000000,674999999);
INSERT INTO `Cuentas` VALUES (678,'Gastos extraordinarios','','','','',678000000,678999999);
INSERT INTO `Cuentas` VALUES (679,'Gastos y pérdidas de ejercicios anteriores','','','','',679000000,679999999);
INSERT INTO `Cuentas` VALUES (680,'Amortización de gastos de establecimiento','','','','',680000000,680999999);
INSERT INTO `Cuentas` VALUES (681,'Amortización del inmovilizado inmaterial','','','','',681000000,681999999);
INSERT INTO `Cuentas` VALUES (682,'Amortización del inmovilizado material','','','','',682000000,682999999);
INSERT INTO `Cuentas` VALUES (690,'Dotación al fondo de reversión','','','','',690000000,690999999);
INSERT INTO `Cuentas` VALUES (691,'Dotación provisión del inmovilizado inmaterial','','','','',691000000,691999999);
INSERT INTO `Cuentas` VALUES (692,'Dotación provisión del inmovilizado material','','','','',692000000,692999999);
INSERT INTO `Cuentas` VALUES (693,'Dotación provisión de existencias','','','','',693000000,693999999);
INSERT INTO `Cuentas` VALUES (694,'Dotación provisión para insolvencias de tráfico','','','','',694000000,694999999);
INSERT INTO `Cuentas` VALUES (695,'Dotación provisión otras operaciones de tráfico','','','','',695000000,695999999);
INSERT INTO `Cuentas` VALUES (696,'Dotación provisión valores negociables a largo','','','','',696000000,696999999);
INSERT INTO `Cuentas` VALUES (697,'Dotación provisión insolvencias créditos a largo','','','','',697000000,697999999);
INSERT INTO `Cuentas` VALUES (698,'Dotacion provisión valores negociables a corto','','','','',698000000,698999999);
INSERT INTO `Cuentas` VALUES (699,'Dtoacion provisión insolvencias créditos a corto','','','','',699000000,699999999);
INSERT INTO `Cuentas` VALUES (700,'Ventas de mercaderias','','','','',700000000,700999999);
INSERT INTO `Cuentas` VALUES (701,'Ventas de productos terminados','','','','',701000000,701999999);
INSERT INTO `Cuentas` VALUES (702,'Ventas de productos semiterminados','','','','',702000000,702999999);
INSERT INTO `Cuentas` VALUES (703,'Ventas de subproductos y residuos','','','','',703000000,703999999);
INSERT INTO `Cuentas` VALUES (704,'Ventas de envases y embalajes','','','','',704000000,704999999);
INSERT INTO `Cuentas` VALUES (705,'Prestaciones de servicios','','','','',705000000,705999999);
INSERT INTO `Cuentas` VALUES (708,'Devoluciones de ventas y operaciones similares','','','','',708000000,708999999);
INSERT INTO `Cuentas` VALUES (709,'`rappels` sobre ventas','','','','',709000000,709999999);
INSERT INTO `Cuentas` VALUES (710,'Variación de existencias de productos en curso','','','','',710000000,710999999);
INSERT INTO `Cuentas` VALUES (711,'Variación de existencias de productos semiterminad','','','','',711000000,711999999);
INSERT INTO `Cuentas` VALUES (712,'Variación existencias de productos terminados','','','','',712000000,712999999);
INSERT INTO `Cuentas` VALUES (713,'Variación exist.subprod.residuos y mat.recuperados','','','','',713000000,713999999);
INSERT INTO `Cuentas` VALUES (730,'Incorporacion al activo de gastos establecimiento','','','','',730000000,730999999);
INSERT INTO `Cuentas` VALUES (731,'Trabajos realizados inmovilizado inmaterial','','','','',731000000,731999999);
INSERT INTO `Cuentas` VALUES (732,'Trabajos realizados inmovilizado material','','','','',732000000,732999999);
INSERT INTO `Cuentas` VALUES (733,'Trabajos realizados inmovilizado material en curso','','','','',733000000,733999999);
INSERT INTO `Cuentas` VALUES (737,'Incorporación activo gastos formalización deudas','','','','',737000000,737999999);
INSERT INTO `Cuentas` VALUES (740,'Subvenciones oficiales a la explotación','','','','',740000000,740999999);
INSERT INTO `Cuentas` VALUES (741,'Otras subvenciones a la explotación','','','','',741000000,741999999);
INSERT INTO `Cuentas` VALUES (751,'Resultados de operaciones en común','','','','',751000000,751999999);
INSERT INTO `Cuentas` VALUES (752,'Ingresos por arrendamientos','','','','',752000000,752999999);
INSERT INTO `Cuentas` VALUES (753,'Ingresos propiedad industrial cedida explotación','','','','',753000000,753999999);
INSERT INTO `Cuentas` VALUES (754,'Ingresos por comisiones','','','','',754000000,754999999);
INSERT INTO `Cuentas` VALUES (755,'Ingresos por servicios al personal','','','','',755000000,755999999);
INSERT INTO `Cuentas` VALUES (759,'Ingresos por servicios diversos','','','','',759000000,759999999);
INSERT INTO `Cuentas` VALUES (760,'Ingresos en participaciones de capital','','','','',760000000,760999999);
INSERT INTO `Cuentas` VALUES (761,'Ingresos de valores de renta fija','','','','',761000000,761999999);
INSERT INTO `Cuentas` VALUES (762,'Ingresos de créditos a largo plazo','','','','',762000000,762999999);
INSERT INTO `Cuentas` VALUES (763,'Ingresos de créditos a corto plazo','','','','',763000000,763999999);
INSERT INTO `Cuentas` VALUES (765,'Descuento sobre compras por pronto pago','','','','',765000000,765999999);
INSERT INTO `Cuentas` VALUES (766,'Beneficios en valores negociables','','','','',766000000,766999999);
INSERT INTO `Cuentas` VALUES (768,'Diferencias positivas de cambio','','','','',768000000,768999999);
INSERT INTO `Cuentas` VALUES (769,'Otros ingresos financieros','','','','',769000000,769999999);
INSERT INTO `Cuentas` VALUES (770,'Beneficios procedentes de inmovilizado inmaterial','','','','',770000000,770999999);
INSERT INTO `Cuentas` VALUES (771,'Beneficios procedentes de inmovilizado material','','','','',771000000,771999999);
INSERT INTO `Cuentas` VALUES (772,'Benef. procedentes part. capital a largo emp.grupo','','','','',772000000,772999999);
INSERT INTO `Cuentas` VALUES (774,'Benef. operaciones acciones y obligaciones propias','','','','',774000000,774999999);
INSERT INTO `Cuentas` VALUES (775,'Subvenciones de capital transp.resultado ejercicio','','','','',775000000,775999999);
INSERT INTO `Cuentas` VALUES (778,'Ingresos extraordinarios','','','','',778000000,778999999);
INSERT INTO `Cuentas` VALUES (779,'Ingresos y beneficios de ejercicios anteriores','','','','',779000000,779999999);
INSERT INTO `Cuentas` VALUES (790,'Excesos de provisión para riesgos y gastos','','','','',790000000,790999999);
INSERT INTO `Cuentas` VALUES (791,'Exceso de provisión del inmovilizado inmaterial','','','','',791000000,791999999);
INSERT INTO `Cuentas` VALUES (792,'Exceso de provisión del inmovilizado material','','','','',792000000,792999999);
INSERT INTO `Cuentas` VALUES (793,'Provisión de existencias aplicadas','','','','',793000000,793999999);
INSERT INTO `Cuentas` VALUES (794,'Provisión para insolvencias de trafico aplicadas','','','','',794000000,794999999);
INSERT INTO `Cuentas` VALUES (795,'Provisión para otras operaciones de tráfico aplic.','','','','',795000000,795999999);
INSERT INTO `Cuentas` VALUES (796,'Exceso provisión valores negociables a largo','','','','',796000000,796999999);
INSERT INTO `Cuentas` VALUES (797,'Exceso provisión insolvencias créditos a largo','','','','',797000000,797999999);
INSERT INTO `Cuentas` VALUES (798,'Exceso provisión valores negociables a corto','','','','',798000000,798999999);
INSERT INTO `Cuentas` VALUES (799,'Exceso provisión insolvencias créditos a corto','','','','',799000000,799999999);
INSERT INTO `Cuentas` VALUES (1000,'Capital ordinario','','','','',100000000,100099999);
INSERT INTO `Cuentas` VALUES (1001,'Capital privilegiado','','','','',100100000,100199999);
INSERT INTO `Cuentas` VALUES (1002,'Capital sin derecho a voto','','','','',100200000,100299999);
INSERT INTO `Cuentas` VALUES (1003,'Capital con derechos restringidos','','','','',100300000,100399999);
INSERT INTO `Cuentas` VALUES (1300,'Subvenciones del Estado','','','','',130000000,130099999);
INSERT INTO `Cuentas` VALUES (1301,'Subvenciones de otras Administraciones Públicas','','','','',130100000,130199999);
INSERT INTO `Cuentas` VALUES (1500,'Obligaciones y bonos simples','','','','',150000000,150099999);
INSERT INTO `Cuentas` VALUES (1501,'Obligaciones y bonos garantizados','','','','',150100000,150199999);
INSERT INTO `Cuentas` VALUES (1502,'Obligaciones y bonos subordinados','','','','',150200000,150299999);
INSERT INTO `Cuentas` VALUES (1503,'Obligaciones y bonos cupón cero','','','','',150300000,150399999);
INSERT INTO `Cuentas` VALUES (1504,'Obligaciones y bonos c/opción suscripción acciones','','','','',150400000,150499999);
INSERT INTO `Cuentas` VALUES (1505,'Obligaciones y bonos c/participación en beneficios','','','','',150500000,150599999);
INSERT INTO `Cuentas` VALUES (1600,'Préstamos a largo plazo de empresas del grupo','','','','',160000000,160099999);
INSERT INTO `Cuentas` VALUES (1609,'Otras deudas a largo plazo con empresas del grupo','','','','',160900000,160999999);
INSERT INTO `Cuentas` VALUES (1700,'Préstamos a largo plazo de entidades de crédito','','','','',170000000,170099999);
INSERT INTO `Cuentas` VALUES (1709,'Otras deudas a largo plazo de entidades de crédito','','','','',170900000,170999999);
INSERT INTO `Cuentas` VALUES (2100,'Gastos de I + D en proyectos no terminados','','','','',210000000,210099999);
INSERT INTO `Cuentas` VALUES (2101,'Gastos de I + D en proyectos terminados','','','','',210100000,210199999);
INSERT INTO `Cuentas` VALUES (2500,'Inv. finan. perm. acciones con cotización mercado','','','','',250000000,250099999);
INSERT INTO `Cuentas` VALUES (2501,'Inv. finan. perm. acciones sin cotización mercado','','','','',250100000,250199999);
INSERT INTO `Cuentas` VALUES (2502,'Otras inversiones financieras en capital','','','','',250200000,250299999);
INSERT INTO `Cuentas` VALUES (2810,'Amortización acumulada de gastos de I + D','','','','',281000000,281099999);
INSERT INTO `Cuentas` VALUES (2811,'Amortización acumulada concesiones administrativas','','','','',281100000,281199999);
INSERT INTO `Cuentas` VALUES (2812,'Amortización acumulada de propiedad industrial','','','','',281200000,281299999);
INSERT INTO `Cuentas` VALUES (2813,'Amortización acumulada de fondo de comercio','','','','',281300000,281399999);
INSERT INTO `Cuentas` VALUES (2814,'Amortización acumulada de derechos de traspaso','','','','',281400000,281499999);
INSERT INTO `Cuentas` VALUES (2815,'Amortización acumulada aplicaciones informáticas','','','','',281500000,281599999);
INSERT INTO `Cuentas` VALUES (2817,'Amt. acum. derechos bienes régimen arrend. financ.','','','','',281700000,281799999);
INSERT INTO `Cuentas` VALUES (2821,'Amortización acumulada de construcciones','','','','',282100000,282199999);
INSERT INTO `Cuentas` VALUES (2822,'Amortización acumulada de instalaciones técnicas','','','','',282200000,282299999);
INSERT INTO `Cuentas` VALUES (2823,'Amortización acumulada de maquinaria','','','','',282300000,282399999);
INSERT INTO `Cuentas` VALUES (5409,'Otras inversiones finan. temporales en capital','','','','',540900000,540999999);
INSERT INTO `Cuentas` VALUES (5560,'Desembolsos exigidos sobre acciones de emp. grupo','','','','',556000000,556099999);
INSERT INTO `Cuentas` VALUES (5561,'Desembolsos exigidos sobre acciones de emp. asoc.','','','','',556100000,556199999);
INSERT INTO `Cuentas` VALUES (5562,'Desembolsos exigidos sobre acciones de otras emp.','','','','',556200000,556299999);
INSERT INTO `Cuentas` VALUES (6080,'Devoluciones de compras de mercaderías','','','','',608000000,608099999);
INSERT INTO `Cuentas` VALUES (6081,'Devoluciones de compras de materias primas','','','','',608100000,608199999);
INSERT INTO `Cuentas` VALUES (6082,'Devoluciones de compras otros aprovisionamientos','','','','',608200000,608299999);
INSERT INTO `Cuentas` VALUES (6090,'`Rappels` por compras de mercaderías','','','','',609000000,609099999);
INSERT INTO `Cuentas` VALUES (6091,'`Rappels` por compras de materias primas','','','','',609100000,609199999);
INSERT INTO `Cuentas` VALUES (6092,'`Rappels` por compras de otros aprovisionamientos','','','','',609200000,609299999);
INSERT INTO `Cuentas` VALUES (6341,'Ajustes negativos en I.V.A. de circulante','','','','',634100000,634199999);
INSERT INTO `Cuentas` VALUES (6342,'Ajustes negativos  en I.V.A. de inversiones','','','','',634200000,634299999);
INSERT INTO `Cuentas` VALUES (6391,'Ajustes positivos en I.V.A. de circulante','','','','',639100000,639199999);
INSERT INTO `Cuentas` VALUES (6392,'Ajustes positivos en I.V.A. de inversiones','','','','',639200000,639299999);
INSERT INTO `Cuentas` VALUES (6510,'Beneficio transferido (gestor)','','','','',651000000,651099999);
INSERT INTO `Cuentas` VALUES (6511,'Pérdida soportada (partícipe o asociado no gestor)','','','','',651100000,651199999);
INSERT INTO `Cuentas` VALUES (6610,'Intereses obligaciones y bonos a largo emp.grupo','','','','',661000000,661099999);
INSERT INTO `Cuentas` VALUES (6611,'Intereses obligaciones y bonos a largo emp.asoc.','','','','',661100000,661199999);
INSERT INTO `Cuentas` VALUES (6613,'Intereses obligaciones y bonos a largo otras emp.','','','','',661300000,661399999);
INSERT INTO `Cuentas` VALUES (6615,'Intereses obligaciones y bonos a corto emp.grupo','','','','',661500000,661599999);
INSERT INTO `Cuentas` VALUES (6616,'Intereses obligaciones y bonos a corto emp.asoc.','','','','',661600000,661699999);
INSERT INTO `Cuentas` VALUES (6618,'Intereses obligaciones y bonos a corto otras emp.','','','','',661800000,661899999);
INSERT INTO `Cuentas` VALUES (6620,'Intereses deudas a largo plazo con emp. grupo','','','','',662000000,662099999);
INSERT INTO `Cuentas` VALUES (6621,'Intereses deudas a largo plazo con emp. asoc.','','','','',662100000,662199999);
INSERT INTO `Cuentas` VALUES (6622,'Intereses deudas a largo con entidades de crédito','','','','',662200000,662299999);
INSERT INTO `Cuentas` VALUES (6623,'Intereses deudas largo con otras empresas','','','','',662300000,662399999);
INSERT INTO `Cuentas` VALUES (6630,'Intereses deudas a corto plazo con emp. grupo','','','','',663000000,663099999);
INSERT INTO `Cuentas` VALUES (6631,'Intereses deudas a corto plazo con emp. asoc.','','','','',663100000,663199999);
INSERT INTO `Cuentas` VALUES (6632,'Intereses deudas a corto con entidades de crédito','','','','',663200000,663299999);
INSERT INTO `Cuentas` VALUES (6633,'Intereses de deudas a corto con otras empresas','','','','',663300000,663399999);
INSERT INTO `Cuentas` VALUES (6640,'Intereses descuento efectos ent. crédito grupo','','','','',664000000,664099999);
INSERT INTO `Cuentas` VALUES (6641,'Intereses descuento efectos ent. crédito asoc.','','','','',664100000,664199999);
INSERT INTO `Cuentas` VALUES (6642,'Intereses descuento efectos en otras ent. crédito','','','','',664200000,664299999);
INSERT INTO `Cuentas` VALUES (6650,'Descuentos ventas por pronto pago emp. grupo','','','','',665000000,665099999);
INSERT INTO `Cuentas` VALUES (6651,'Descuentos ventas por pronto pago emp. asoc.','','','','',665100000,665199999);
INSERT INTO `Cuentas` VALUES (6653,'Descuentos ventas por pronto pago otras emp.','','','','',665300000,665399999);
INSERT INTO `Cuentas` VALUES (6660,'Pérdidas valores negociables a largo emp. grupo','','','','',666000000,666099999);
INSERT INTO `Cuentas` VALUES (6661,'Pérdidas valores negociables a largo emp. asoc.','','','','',666100000,666199999);
INSERT INTO `Cuentas` VALUES (6663,'Pérdidas valores negociables a largo otras emp.','','','','',666300000,666399999);
INSERT INTO `Cuentas` VALUES (6665,'Pérdidas valores negociables a corto emp. grupo','','','','',666500000,666599999);
INSERT INTO `Cuentas` VALUES (6666,'Pérdidas valores negociables a corto emp. asoc.','','','','',666600000,666699999);
INSERT INTO `Cuentas` VALUES (6668,'Pérdidas valores negociables a corto otras emp.','','','','',666800000,666899999);
INSERT INTO `Cuentas` VALUES (6670,'Pérdidas créditos a largo plazo a emp. grupo','','','','',667000000,667099999);
INSERT INTO `Cuentas` VALUES (6671,'Pérdidas créditos a largo plazo a emp. asociadas','','','','',667100000,667199999);
INSERT INTO `Cuentas` VALUES (6673,'Pérdidas créditos a largo plazo a otras empresas','','','','',667300000,667399999);
INSERT INTO `Cuentas` VALUES (6675,'Pérdidas créditos a corto plazo a emp. grupo','','','','',667500000,667599999);
INSERT INTO `Cuentas` VALUES (6676,'Pérdidas créditos a corto plazo a emp. asociadas','','','','',667600000,667699999);
INSERT INTO `Cuentas` VALUES (6678,'Pérdidas créditos a corto plazo a otras empresas','','','','',667800000,667899999);
INSERT INTO `Cuentas` VALUES (6960,'Dotac.provis.particip.capital a largo emp.grupo','','','','',696000000,696099999);
INSERT INTO `Cuentas` VALUES (6961,'Dotac.provis.particip.capital a largo emp.asoc.','','','','',696100000,696199999);
INSERT INTO `Cuentas` VALUES (6963,'Dotac.provis. valores r.fija a largo otras emp','','','','',696300000,696399999);
INSERT INTO `Cuentas` VALUES (6965,'Dotac.provis. valores r.fija a largo emp.grupo','','','','',696500000,696599999);
INSERT INTO `Cuentas` VALUES (6966,'Dotac.provis. valores r.fija a largo emp asoc.','','','','',696600000,696699999);
INSERT INTO `Cuentas` VALUES (6970,'Dotac.provis. insolv. crédito a largo emp.grupo','','','','',697000000,697099999);
INSERT INTO `Cuentas` VALUES (6971,'Dotac.provis. insolv. crédito a largo emp.asoc.','','','','',697100000,697199999);
INSERT INTO `Cuentas` VALUES (6973,'Dotac.provis. insolv. crédito a largo otras emp.','','','','',697300000,697399999);
INSERT INTO `Cuentas` VALUES (6980,'Dotac.provis. valores neg. a corto emp. grupo','','','','',698000000,698099999);
INSERT INTO `Cuentas` VALUES (6981,'Dotac.provis. valores neg. a corto emp. asoc.','','','','',698100000,698199999);
INSERT INTO `Cuentas` VALUES (6983,'Dotac.provis. valores neg. a corto otras emp.','','','','',698300000,698399999);
INSERT INTO `Cuentas` VALUES (6990,'Dotac.provis. insolv. crédito a corto emp. grupo','','','','',699000000,699099999);
INSERT INTO `Cuentas` VALUES (6991,'Dotac.provis. insolv. crédito a corto emp. asoc.','','','','',699100000,699199999);
INSERT INTO `Cuentas` VALUES (6993,'Dotac.provis. insolv. crédito a corto otras emp.','','','','',699300000,699399999);
INSERT INTO `Cuentas` VALUES (7080,'Devoluciones de ventas de mercaderías','','','','',708000000,708099999);
INSERT INTO `Cuentas` VALUES (7081,'Devoluciones de ventas de productos terminados','','','','',708100000,708199999);
INSERT INTO `Cuentas` VALUES (7082,'Devoluciones de ventas de productos semiterminados','','','','',708200000,708299999);
INSERT INTO `Cuentas` VALUES (7083,'Devoluciones de ventas de subproductos y residuos','','','','',708300000,708399999);
INSERT INTO `Cuentas` VALUES (7084,'Devoluciones de ventas de envases y embalajes','','','','',708400000,708499999);
INSERT INTO `Cuentas` VALUES (7090,'Rappels sobre ventas de mercaderías','','','','',709000000,709099999);
INSERT INTO `Cuentas` VALUES (7091,'Rappels sobre ventas de productos terminados','','','','',709100000,709199999);
INSERT INTO `Cuentas` VALUES (7092,'Rappels sobre ventas de productos semiterminados','','','','',709200000,709299999);
INSERT INTO `Cuentas` VALUES (7093,'Rappels sobre ventas de subproductos y residuos','','','','',709300000,709399999);
INSERT INTO `Cuentas` VALUES (7094,'Rappels sobre ventas de envases y embalajes','','','','',709400000,709499999);
INSERT INTO `Cuentas` VALUES (7510,'Pérdida transferencia (gestor)','','','','',751000000,751099999);
INSERT INTO `Cuentas` VALUES (7511,'Beneficio atribuido (partícipe asociado no gestor)','','','','',751100000,751199999);
INSERT INTO `Cuentas` VALUES (7600,'Ingresos participaciones en capital emp.grupo','','','','',760000000,760099999);
INSERT INTO `Cuentas` VALUES (7601,'Ingresos participaciones en capital emp.asociadas','','','','',760100000,760199999);
INSERT INTO `Cuentas` VALUES (7603,'Ingresos participaciones capital otras empresas','','','','',760300000,760399999);
INSERT INTO `Cuentas` VALUES (7610,'Ingresos en valores renta fija empresas del grupo','','','','',761000000,761099999);
INSERT INTO `Cuentas` VALUES (7611,'Ingresos en valores renta fija empresas asociadas','','','','',761100000,761199999);
INSERT INTO `Cuentas` VALUES (7613,'Ingresos en valores renta fija otras empresas','','','','',761300000,761399999);
INSERT INTO `Cuentas` VALUES (7620,'Ingresos créditos a largo plazo a emp. grupo','','','','',762000000,762099999);
INSERT INTO `Cuentas` VALUES (7621,'Ingresos créditos a largo plazo emp.asociadas','','','','',762100000,762199999);
INSERT INTO `Cuentas` VALUES (7623,'Ingresos créditos a argo plazo a otras empresas','','','','',762300000,762399999);
INSERT INTO `Cuentas` VALUES (7630,'Ingresos créditos a corto plazo a emp. grupo','','','','',763000000,763099999);
INSERT INTO `Cuentas` VALUES (7631,'Ingresos créditos a corto plazo a emp. asociadas','','','','',763100000,763199999);
INSERT INTO `Cuentas` VALUES (7633,'Ingresos créditos a corto plazo a otras empresas','','','','',763300000,763399999);
INSERT INTO `Cuentas` VALUES (7650,'Descuentos sobre compras por pronto pago emp.grupo','','','','',765000000,765099999);
INSERT INTO `Cuentas` VALUES (7651,'Descuentos sobre compras por pronto pago emp.asoc.','','','','',765100000,765199999);
INSERT INTO `Cuentas` VALUES (7653,'Descuentos sobre compras por pronto pago otras emp','','','','',765300000,765399999);
INSERT INTO `Cuentas` VALUES (7660,'Beneficios valores negociables a largo emp. grupo','','','','',766000000,766099999);
INSERT INTO `Cuentas` VALUES (7661,'Beneficios valores negociables a largo emp. asoc.','','','','',766100000,766199999);
INSERT INTO `Cuentas` VALUES (7663,'Beneficios valores negociables a largo otras emp.','','','','',766300000,766399999);
INSERT INTO `Cuentas` VALUES (7665,'Beneficios valores negociables a corto emp. grupo','','','','',766500000,766599999);
INSERT INTO `Cuentas` VALUES (7666,'Beneficios valores negociables a corto emp. asoc.','','','','',766600000,766699999);
INSERT INTO `Cuentas` VALUES (7668,'Beneficios valores negociables a corto otras emp.','','','','',766800000,766899999);
INSERT INTO `Cuentas` VALUES (7960,'Exceso provis.particip.capital a largo emp.grupo','','','','',796000000,796099999);
INSERT INTO `DatosEmpresa` VALUES (18,'GRUPO BABO, S.Coop.V.L. 2025','ARMADA ESPAÑOLA, P.2 Nº23','EL ALTET','ALICANTE','03195','F54059985','','','C:\Documents and Settings\Mamen\My Documents\PC FUJITSU SIEMENS Panaderia oct2024\GTELITE',17,'0',1,1,1,1.0,1,1,769000000,665000000,765000000,669000000,708000000,608000000,'0','',0,1,2,2,'1','1','0');
INSERT INTO `Familias` VALUES ('01','PAN COMUN');
INSERT INTO `Familias` VALUES ('02','PAN CASERO');
INSERT INTO `Familias` VALUES ('03','PAN ESPECIAL');
INSERT INTO `Familias` VALUES ('04','BOLLERIA DULCE');
INSERT INTO `Familias` VALUES ('05','BOLLERIA SALADA');
INSERT INTO `Familias` VALUES ('06','PASTELERIA');
INSERT INTO `Familias` VALUES ('07','PASTAS Y ROLLOS');
INSERT INTO `Familias` VALUES ('09','SERVICIOS ESPECIALES');
INSERT INTO `Familias` VALUES ('08','BEBIDAS Y SNACKS');
INSERT INTO `Familias` VALUES ('10','MATERIAS PRIMAS Y ENVASES');
INSERT INTO `FormasdePago` VALUES ('30DIAS','A 30 días');
INSERT INTO `FormasdePago` VALUES ('60DIAS','A 60 días');
INSERT INTO `FormasdePago` VALUES ('90DIAS','A 90 días');
INSERT INTO `FormasdePago` VALUES ('CONTADO','Al contado');
INSERT INTO `FormasdePagoDesglose` VALUES ('CONTADO',0,0,100.0);
INSERT INTO `FormasdePagoDesglose` VALUES ('30DIAS',30,0,100.0);
INSERT INTO `FormasdePagoDesglose` VALUES ('60DIAS',60,0,100.0);
INSERT INTO `FormasdePagoDesglose` VALUES ('90DIAS',90,0,100.0);
INSERT INTO `Provincias` VALUES ('01','ALAVA','PAV');
INSERT INTO `Provincias` VALUES ('02','ALBACETE','CAM');
INSERT INTO `Provincias` VALUES ('03','ALICANTE','VAL');
INSERT INTO `Provincias` VALUES ('04','ALMERIA','AND');
INSERT INTO `Provincias` VALUES ('05','AVILA','CAL');
INSERT INTO `Provincias` VALUES ('06','BADAJOZ','EXT');
INSERT INTO `Provincias` VALUES ('07','BALEARES','BAL');
INSERT INTO `Provincias` VALUES ('08','BARCELONA','CAT');
INSERT INTO `Provincias` VALUES ('09','BURGOS','CAL');
INSERT INTO `Provincias` VALUES ('10','CACERES','EXT');
INSERT INTO `Provincias` VALUES ('11','CADIZ','AND');
INSERT INTO `Provincias` VALUES ('12','CASTELLON','VAL');
INSERT INTO `Provincias` VALUES ('13','CIUDAD DECIMAL(15,2)','CAM');
INSERT INTO `Provincias` VALUES ('14','CORDOBA','AND');
INSERT INTO `Provincias` VALUES ('15','LA CORUÑA','GAL');
INSERT INTO `Provincias` VALUES ('16','CUENCA','CAM');
INSERT INTO `Provincias` VALUES ('17','GERONA','CAT');
INSERT INTO `Provincias` VALUES ('18','GRANADA','AND');
INSERT INTO `Provincias` VALUES ('19','GUADALAJARA','CAM');
INSERT INTO `Provincias` VALUES ('20','GUIPUZCOA','PAV');
INSERT INTO `Provincias` VALUES ('21','HUELVA','AND');
INSERT INTO `Provincias` VALUES ('22','HUESCA','ARA');
INSERT INTO `Provincias` VALUES ('23','JAEN','AND');
INSERT INTO `Provincias` VALUES ('24','LEON','CAL');
INSERT INTO `Provincias` VALUES ('25','LERIDA','CAT');
INSERT INTO `Provincias` VALUES ('26','LOGROÑO','RIO');
INSERT INTO `Provincias` VALUES ('27','LUGO','GAL');
INSERT INTO `Provincias` VALUES ('28','MADRID','MAD');
INSERT INTO `Provincias` VALUES ('29','MALAGA','AND');
INSERT INTO `Provincias` VALUES ('30','MURCIA','MUR');
INSERT INTO `Provincias` VALUES ('31','NAVARRA','NAV');
INSERT INTO `Provincias` VALUES ('32','ORENSE','GAL');
INSERT INTO `Provincias` VALUES ('33','ASTURIAS','AST');
INSERT INTO `Provincias` VALUES ('34','PALENCIA','CAL');
INSERT INTO `Provincias` VALUES ('35','LAS PALMAS','CAN');
INSERT INTO `Provincias` VALUES ('36','PONTEVEDRA','GAL');
INSERT INTO `Provincias` VALUES ('37','SALAMANCA','CAL');
INSERT INTO `Provincias` VALUES ('38','SANTA CRUZ DE TENERIFE','CAN');
INSERT INTO `Provincias` VALUES ('39','SANTANDER','CANT');
INSERT INTO `Provincias` VALUES ('40','SEGOVIA','CAL');
INSERT INTO `Provincias` VALUES ('41','SEVILLA','AND');
INSERT INTO `Provincias` VALUES ('42','SORIA','CAL');
INSERT INTO `Provincias` VALUES ('43','TARRAGONA','CAT');
INSERT INTO `Provincias` VALUES ('44','TERUEL','ARA');
INSERT INTO `Provincias` VALUES ('45','TOLEDO','CAM');
INSERT INTO `Provincias` VALUES ('46','VALENCIA','VAL');
INSERT INTO `Provincias` VALUES ('47','VALLADOLID','CAL');
INSERT INTO `Provincias` VALUES ('48','VIZCAYA','PAV');
INSERT INTO `Provincias` VALUES ('49','ZAMORA','CAL');
INSERT INTO `Provincias` VALUES ('50','ZARAGOZA','ARA');
INSERT INTO `Subcuentas` VALUES (100000000,'Capital social','','','','');
INSERT INTO `Subcuentas` VALUES (129000000,'Pérdidas y ganancias','','','','');
INSERT INTO `Subcuentas` VALUES (200000000,'Gastos de establecimiento',0.0,0.0,0.0,0.0);
INSERT INTO `Subcuentas` VALUES (210000000,'Inmovilizado inmaterial',0.0,0.0,0.0,0.0);
INSERT INTO `Subcuentas` VALUES (217000000,'Derechos s/bienes en régimen arrendamiento financ.','','','','');
INSERT INTO `Subcuentas` VALUES (221000000,'Inmovilizado material, construcciones','','','','');
INSERT INTO `Subcuentas` VALUES (223000000,'Inmovilizado material, maquinaria','','','','');
INSERT INTO `Subcuentas` VALUES (226000000,'Inmovilizado material, mobiliario','','','','');
INSERT INTO `Subcuentas` VALUES (228000000,'Inmovilizado material, elementos de transporte','','','','');
INSERT INTO `Subcuentas` VALUES (260000000,'Fianzas y depósitos constituidos a largo plazo','','','','');
INSERT INTO `Subcuentas` VALUES (272000000,'Gastos por intereses diferidos','','','','');
INSERT INTO `Subcuentas` VALUES (281700000,'Amort.acum. derechos s/bienes arrendam. financ.','','','','');
INSERT INTO `Subcuentas` VALUES (282100000,'Amortización acumulada de construcciones',0.0,0.0,0.0,0.0);
INSERT INTO `Subcuentas` VALUES (282300000,'Amortización acumulada de maquinaria','','','','');
INSERT INTO `Subcuentas` VALUES (282600000,'Amortización acumulada de mobiliario','','','','');
INSERT INTO `Subcuentas` VALUES (282800000,'Amortización acumulada de elementos de transporte','','','','');
INSERT INTO `Subcuentas` VALUES (300000000,'Mercaderías','','','','');
INSERT INTO `Subcuentas` VALUES (400000000,'Proveedores varios','','','','');
INSERT INTO `Subcuentas` VALUES (401000000,'Efectos comerciales a pagar, proveedores',0.0,0.0,0.0,0.0);
INSERT INTO `Subcuentas` VALUES (410000000,'Acreedores por prestación de servicios',0.0,0.0,0.0,0.0);
INSERT INTO `Subcuentas` VALUES (430000000,'Clientes varios',0.0,0.0,0.0,0.0);
INSERT INTO `Subcuentas` VALUES (431000000,'Efectos comerciales a cobrar, clientes',0.0,0.0,0.0,0.0);
INSERT INTO `Subcuentas` VALUES (470000000,'Hacienda Pública, deudor por IVA','','','','');
INSERT INTO `Subcuentas` VALUES (471000000,'Seguridad Social deudora','','','','');
INSERT INTO `Subcuentas` VALUES (472000004,'IVA soportado 4%','','','','');
INSERT INTO `Subcuentas` VALUES (472000007,'IVA soportado 7%','','','','');
INSERT INTO `Subcuentas` VALUES (472000016,'IVA soportado 16%',0.0,0.0,0.0,0.0);
INSERT INTO `Subcuentas` VALUES (473000000,'Hacienda Pública, retenciones y pagos a cuenta','','','','');
INSERT INTO `Subcuentas` VALUES (475000000,'Hacienda Pública, acreedor por IVA','','','','');
INSERT INTO `Subcuentas` VALUES (476000000,'Seguridad Social, acreedor','','','','');
INSERT INTO `Subcuentas` VALUES (477000004,'IVA repercutido 4%',0.0,0.0,0.0,0.0);
INSERT INTO `Subcuentas` VALUES (477000007,'IVA repercutido 7%','','','','');
INSERT INTO `Subcuentas` VALUES (477000016,'IVA repercutido 16%','','','','');
INSERT INTO `Subcuentas` VALUES (479000000,'Hacienda pública IVA provisional','','','','');
INSERT INTO `Subcuentas` VALUES (487000000,'Hacienda pública IVA soportado diferido','','','','');
INSERT INTO `Subcuentas` VALUES (570000000,'Caja','','','','');
INSERT INTO `Subcuentas` VALUES (572000000,'Bancos',0.0,0.0,0.0,0.0);
INSERT INTO `Subcuentas` VALUES (600000000,'Compras mercaderías','','','','');
INSERT INTO `Subcuentas` VALUES (608000000,'Devolución de compras','','','','');
INSERT INTO `Subcuentas` VALUES (610000000,'Variación de existencias','','','','');
INSERT INTO `Subcuentas` VALUES (621000000,'Arrendamientos y cánones','','','','');
INSERT INTO `Subcuentas` VALUES (622000000,'Reparaciones y conservación','','','','');
INSERT INTO `Subcuentas` VALUES (623000001,'Gastos profesionales independientes','','','','');
INSERT INTO `Subcuentas` VALUES (624000000,'Transportes varios','','','','');
INSERT INTO `Subcuentas` VALUES (625000000,'Primas de seguros','','','','');
INSERT INTO `Subcuentas` VALUES (626000000,'Comisiones bancarias','','','','');
INSERT INTO `Subcuentas` VALUES (627000000,'Publicidad y relaciones públicas','','','','');
INSERT INTO `Subcuentas` VALUES (628000000,'Suministros','','','','');
INSERT INTO `Subcuentas` VALUES (629000000,'Gastos varios',0.0,0.0,0.0,0.0);
INSERT INTO `Subcuentas` VALUES (630000000,'Impuestos sobre beneficios',0.0,0.0,0.0,0.0);
INSERT INTO `Subcuentas` VALUES (631000000,'Otros tributos','','','','');
INSERT INTO `Subcuentas` VALUES (640000000,'Sueldos y salarios','','','','');
INSERT INTO `Subcuentas` VALUES (642000000,'Seguridad Social a cargo de la empresa','','','','');
INSERT INTO `Subcuentas` VALUES (650000000,'Pérdidas de créditos comerciales incobrables','','','','');
INSERT INTO `Subcuentas` VALUES (662000000,'Intereses a largo plazo','','','','');
INSERT INTO `Subcuentas` VALUES (665000000,'Descuentos sobre ventas de pronto pago','','','','');
INSERT INTO `Subcuentas` VALUES (668000000,'Diferencias negativas de cambio','','','','');
INSERT INTO `Subcuentas` VALUES (669000000,'Gastos financieros','','','','');
INSERT INTO `Subcuentas` VALUES (678000000,'Gastos extraordinarios','','','','');
INSERT INTO `Subcuentas` VALUES (681000000,'Amortización inmovilizado inmaterial','','','','');
INSERT INTO `Subcuentas` VALUES (682000000,'Amortización inmovilizado material','','','','');
INSERT INTO `Subcuentas` VALUES (700000000,'Ventas mercaderías','','','','');
INSERT INTO `Subcuentas` VALUES (705000000,'Prestación de servicios','','','','');
INSERT INTO `Subcuentas` VALUES (708000000,'Devolución ventas de mercaderías','','','','');
INSERT INTO `Subcuentas` VALUES (759000000,'Ingresos por servicios diversos','','','','');
INSERT INTO `Subcuentas` VALUES (765000000,'Descuentos sobre compras por pronto pago',0.0,0.0,0.0,0.0);
INSERT INTO `Subcuentas` VALUES (768000000,'Diferencias positivas de cambio','','','','');
INSERT INTO `Subcuentas` VALUES (769000000,'Ingresos financieros','','','','');
INSERT INTO `Subcuentas` VALUES (778000000,'Ingresos extraordinarios','','','','');
INSERT INTO `TiposdeIVA` VALUES (0,0.0,0.0,'','','');
INSERT INTO `TiposdeIVA` VALUES (1,2.0,0.26,472000004,477000004,475000000);
INSERT INTO `TiposdeIVA` VALUES (2,10.0,1.4,472000007,477000007,475000000);
INSERT INTO `TiposdeIVA` VALUES (3,21.0,5.2,472000016,477000016,475000000);
INSERT INTO `WkDevoluciones` VALUES (0,'Efectos devueltos impagados','1999-07-14 00:00:00','1999-07-15 00:00:00',123456789123456.0,15.0,'1999-07-09 00:00:00',1.0,1040.0,1040.0,'1999-07-09 00:00:00',0);
INSERT INTO `WkDevoluciones` VALUES (0,'Efectos reclamados','1999-07-14 00:00:00','1999-07-15 00:00:00',123456789123456.0,15.0,'1999-07-09 00:00:00',1.0,1160.0,1160.0,'1999-07-09 00:00:00',1);
INSERT INTO `WkOrdenantes` VALUES (1,'1999-07-14 00:00:00','123456789123456',2200.0,2200.0,4.0,2.0,'1234','4321','20','1234567890');
INSERT INTO `WkRechazados` VALUES (19,'1999-07-15 00:00:00',1,'08','','BARCELONA',23200.0,'1999-08-14 00:00:00',2,'1999-07-15 00:00:00','CLIENTE 1',2,0,'IBER','4654','65','4654654654','Formal','DOMICILIO 1','01231','POBLACION 1','02','','CUENTA INCORRECTA');
INSERT INTO `WkRechazados` VALUES (20,'1999-07-15 00:00:00',1,'08','','BARCELONA',23200.0,'1999-09-13 00:00:00',2,'1999-07-15 00:00:00','CLIENTE 1',2,0,'IBER','4654','65','4654654654','Informático','DOMICILIO 1','01231','POBLACION 1','02','','DISCO DEFECTUOSO');
INSERT INTO `Zonas` VALUES ('AND','Andalucía');
INSERT INTO `Zonas` VALUES ('ARA','Aragón');
INSERT INTO `Zonas` VALUES ('AST','Asturias');
INSERT INTO `Zonas` VALUES ('BAL','Baleares');
INSERT INTO `Zonas` VALUES ('CAL','Castilla León');
INSERT INTO `Zonas` VALUES ('CAM','Castilla La Mancha');
INSERT INTO `Zonas` VALUES ('CAN','Canarias');
INSERT INTO `Zonas` VALUES ('CANT','Cantabria');
INSERT INTO `Zonas` VALUES ('CAT','Cataluña');
INSERT INTO `Zonas` VALUES ('EXT','Extremadura');
INSERT INTO `Zonas` VALUES ('GAL','Galicia');
INSERT INTO `Zonas` VALUES ('MAD','Madrid');
INSERT INTO `Zonas` VALUES ('MUR','Murcia');
INSERT INTO `Zonas` VALUES ('NAV','Navarra');
INSERT INTO `Zonas` VALUES ('PAV','País Vasco');
INSERT INTO `Zonas` VALUES ('RIO','La Rioja');
INSERT INTO `Zonas` VALUES ('VAL','Comunidad Valenciana');
INSERT INTO `balances` VALUES (100,'A) Accionistas por desembolsos no exigidos','','');
INSERT INTO `balances` VALUES (200,'B) Inmovilizado',0.0,0.0);
INSERT INTO `balances` VALUES (210,'    I. Gastos de establecimiento',0.0,0.0);
INSERT INTO `balances` VALUES (220,'    II. Inmovilizaciones inmateriales',0.0,0.0);
INSERT INTO `balances` VALUES (221,'        1. Gastos de investigación y desarrollo',0.0,0.0);
INSERT INTO `balances` VALUES (222,'        2. Concesiones, patentes, licencias, marcas y similares',0.0,0.0);
INSERT INTO `balances` VALUES (223,'        3. Fondo de comercio',0.0,0.0);
INSERT INTO `balances` VALUES (224,'        4. Derechos de traspaso',0.0,0.0);
INSERT INTO `balances` VALUES (225,'        5. Aplicaciones informáticas',0.0,0.0);
INSERT INTO `balances` VALUES (226,'        6. Anticipos',0.0,0.0);
INSERT INTO `balances` VALUES (227,'        7. Provisiones',0.0,0.0);
INSERT INTO `balances` VALUES (228,'        8. Amortizaciones',0.0,0.0);
INSERT INTO `balances` VALUES (230,'    III. Inmovilizaciones materiales',0.0,0.0);
INSERT INTO `balances` VALUES (231,'        1. Terrenos y construcciones',0.0,0.0);
INSERT INTO `balances` VALUES (232,'        2. Instalaciones técnicas y maquinaria',0.0,0.0);
INSERT INTO `balances` VALUES (233,'        3. Otras instalaciones, utillaje y mobiliario',0.0,0.0);
INSERT INTO `balances` VALUES (234,'        4. Anticipos e inmovilizaciones materiales en curso',0.0,0.0);
INSERT INTO `balances` VALUES (235,'        5. Otro inmovilizado',0.0,0.0);
INSERT INTO `balances` VALUES (236,'        6. Provisiones',0.0,0.0);
INSERT INTO `balances` VALUES (237,'        7. Amortizaciones',0.0,0.0);
INSERT INTO `balances` VALUES (240,'    IV. Inmovilizaciones financieras',0.0,0.0);
INSERT INTO `balances` VALUES (241,'        1. Participaciones en empresas del grupo',0.0,0.0);
INSERT INTO `balances` VALUES (242,'        2. Créditos a empresas del grupo',0.0,0.0);
INSERT INTO `balances` VALUES (243,'        3. Participaciones en empresas asociadas',0.0,0.0);
INSERT INTO `balances` VALUES (244,'        4. Créditos a empresas asociadas',0.0,0.0);
INSERT INTO `balances` VALUES (245,'        5. Valores que tengan carácter de inmovilizaciones',0.0,0.0);
INSERT INTO `balances` VALUES (246,'        6. Otros créditos',0.0,0.0);
INSERT INTO `balances` VALUES (247,'        7. Depósitos y fianzas entregados a largo plazo',0.0,0.0);
INSERT INTO `balances` VALUES (248,'        8. Provisiones',0.0,0.0);
INSERT INTO `balances` VALUES (250,'    V. Acciones propias',0.0,0.0);
INSERT INTO `balances` VALUES (300,'C) Gastos a distribuir en varios ejercicios','','');
INSERT INTO `balances` VALUES (400,'D) Activo circulante','','');
INSERT INTO `balances` VALUES (410,'    I. Accionistas por desembolsos exigidos','','');
INSERT INTO `balances` VALUES (420,'    II. Existencias','','');
INSERT INTO `balances` VALUES (421,'        1. Comerciales','','');
INSERT INTO `balances` VALUES (422,'        2. Materias primas y otros aprovisionamientos','','');
INSERT INTO `balances` VALUES (423,'        3. Productos en curso y semiterminados','','');
INSERT INTO `balances` VALUES (424,'        4. Productos terminados','','');
INSERT INTO `balances` VALUES (425,'        5. Subproductos, residuos y materiales recuperados','','');
INSERT INTO `balances` VALUES (426,'        6. Anticipos','','');
INSERT INTO `balances` VALUES (427,'        7. Provisiones','','');
INSERT INTO `balances` VALUES (430,'    III. Deudores','','');
INSERT INTO `balances` VALUES (431,'        1. Clientes por ventas y prestaciones de servicios','','');
INSERT INTO `balances` VALUES (432,'        2. Sociedades del grupo, deudores','','');
INSERT INTO `balances` VALUES (433,'        3. Sociedades asociadas, deudores','','');
INSERT INTO `balances` VALUES (434,'        4. Deudores varios','','');
INSERT INTO `balances` VALUES (435,'        5. Personal','','');
INSERT INTO `balances` VALUES (436,'        6. Administraciones Públicas','','');
INSERT INTO `balances` VALUES (437,'        7. Provisiones','','');
INSERT INTO `balances` VALUES (440,'    IV. Inversiones financieras temporales','','');
INSERT INTO `balances` VALUES (441,'        1. Participaciones en empresas del grupo','','');
INSERT INTO `balances` VALUES (442,'        2. Créditos a empresas del grupo','','');
INSERT INTO `balances` VALUES (443,'        3. Participaciones en empresas asociadas','','');
INSERT INTO `balances` VALUES (444,'        4. Créditos a empresas asociadas','','');
INSERT INTO `balances` VALUES (445,'        5. Cartera de valores a corto plazo','','');
INSERT INTO `balances` VALUES (446,'        6. Créditos','','');
INSERT INTO `balances` VALUES (447,'        7. Depósitos y fianzas entregados a corto plazo','','');
INSERT INTO `balances` VALUES (448,'        8. Provisiones','','');
INSERT INTO `balances` VALUES (450,'    V. Acciones propias a corto plazo','','');
INSERT INTO `balances` VALUES (460,'    VI. Tesorería','','');
INSERT INTO `balances` VALUES (470,'    VII. Ajustes por periodificación','','');
INSERT INTO `balances` VALUES (1100,'A) Fondos propios','','');
INSERT INTO `balances` VALUES (1110,'    I. Capital suscrito','','');
INSERT INTO `balances` VALUES (1120,'    II. Prima de emisión','','');
INSERT INTO `balances` VALUES (1130,'    III. Reserva de revalorización','','');
INSERT INTO `balances` VALUES (1140,'    IV. Reservas','','');
INSERT INTO `balances` VALUES (1141,'        1. Reserva legal','','');
INSERT INTO `balances` VALUES (1142,'        2. Reservas para acciones propias','','');
INSERT INTO `balances` VALUES (1143,'        3. Reservas para acciones de la sociedad dominante','','');
INSERT INTO `balances` VALUES (1144,'        4. Reservas estatutarias','','');
INSERT INTO `balances` VALUES (1145,'        5. Otras reservas','','');
INSERT INTO `balances` VALUES (1150,'    V. Resultados de ejercicios anteriores','','');
INSERT INTO `balances` VALUES (1151,'        1. Remanente','','');
INSERT INTO `balances` VALUES (1152,'        2. Resultados negatigos de ejercicios anteriores','','');
INSERT INTO `balances` VALUES (1153,'        3. Aportaciones socios para compensación pérdidas','','');
INSERT INTO `balances` VALUES (1160,'    VI.Pérdidas y ganancias (beneficio o pérdida)','','');
INSERT INTO `balances` VALUES (1170,'    VII. Dividendo a cuenta entregado en el ejercicio','','');
INSERT INTO `balances` VALUES (1200,'B) Ingresos a distribuir en varios ejercicios','','');
INSERT INTO `balances` VALUES (1201,'        1. Subvenciones de capital','','');
INSERT INTO `balances` VALUES (1202,'        2. Diferencias positivas de cambio','','');
INSERT INTO `balances` VALUES (1203,'        3.Otros ingresos a distribuir en varios ejercicios','','');
INSERT INTO `balances` VALUES (1300,'C) Provisiones para riesgos y gastos','','');
INSERT INTO `balances` VALUES (1301,'        1. Provisiones pensiones y obligaciones similares','','');
INSERT INTO `balances` VALUES (1302,'        2. Provisiones para impuestos','','');
INSERT INTO `balances` VALUES (1303,'        3. Otras provisiones','','');
INSERT INTO `balances` VALUES (1304,'        4. Fondos de reversión','','');
INSERT INTO `balances` VALUES (1400,'D) Acreedores a largo plazo','','');
INSERT INTO `balances` VALUES (1410,'    I. Emisiones de obligaciones','','');
INSERT INTO `balances` VALUES (1411,'        1. Obligaciones no convertibles','','');
INSERT INTO `balances` VALUES (1412,'        2. Obligaciones convertibles','','');
INSERT INTO `balances` VALUES (1413,'        3. Otras deudas representadas en valores negociab.','','');
INSERT INTO `balances` VALUES (1420,'    II. Deudas con entidades de crédito','','');
INSERT INTO `balances` VALUES (1430,'    III. Deudas con empresas del grupo y asociadas','','');
INSERT INTO `balances` VALUES (1431,'        1. Deudas con empresas del grupo','','');
INSERT INTO `balances` VALUES (1432,'        2. Deudas con empresas asociadas','','');
INSERT INTO `balances` VALUES (1440,'    IV.Otros acreedores','','');
INSERT INTO `balances` VALUES (1441,'        1. Deudas representadas por efectos a pagar','','');
INSERT INTO `balances` VALUES (1442,'        2. Otras deudas','','');
INSERT INTO `balances` VALUES (1443,'        3. Fianzas y depósitos recibidos a largo plazo','','');
INSERT INTO `balances` VALUES (1450,'    V. Desembolsos pendientes sobre acciones no exigidos','','');
INSERT INTO `balances` VALUES (1451,'        1. De empresas del grupo','','');
INSERT INTO `balances` VALUES (1452,'        2. De empresas asociadas','','');
INSERT INTO `balances` VALUES (1453,'        3. De otras empresas','','');
INSERT INTO `balances` VALUES (1500,'E) Acreedores a corto plazo','','');
INSERT INTO `balances` VALUES (1510,'    I. Emisiones de obligaciones','','');
INSERT INTO `balances` VALUES (1511,'        1. Obligaciones no convertibles','','');
INSERT INTO `balances` VALUES (1512,'        2. Obligaciones convertibles','','');
INSERT INTO `balances` VALUES (1513,'        3. Otras deudas representadas valores negociables','','');
INSERT INTO `balances` VALUES (1514,'        4. Intereses de obligaciones y otros valores','','');
INSERT INTO `balances` VALUES (1520,'    II. Deudas con entidades decrédito','','');
INSERT INTO `balances` VALUES (1521,'        1. Préstamos y otras deudas','','');
INSERT INTO `balances` VALUES (1522,'        2. Deudas por intereses','','');
INSERT INTO `balances` VALUES (1530,'    III. Deudas con empresas del grupo y asociadas a corto plazo','','');
INSERT INTO `balances` VALUES (1531,'        1. Deudas con empresas del grupo','','');
INSERT INTO `balances` VALUES (1532,'        2. Deudas con empresas asociadas','','');
INSERT INTO `balances` VALUES (1540,'    IV. Acreedores comerciales','','');
INSERT INTO `balances` VALUES (1541,'        1. Anticipos recibidos por pedidos','','');
INSERT INTO `balances` VALUES (1542,'        2. Deudas por compras o prestaciones de servicios','','');
INSERT INTO `balances` VALUES (1543,'        3. Deudas representadas por efectos a pagar','','');
INSERT INTO `balances` VALUES (1550,'    V. Otras deudas no comerciales','','');
INSERT INTO `balances` VALUES (1551,'        1. Administraciones Públicas','','');
INSERT INTO `balances` VALUES (1552,'        2. Deudas representadas por efectos a pagar','','');
INSERT INTO `balances` VALUES (1553,'        3. Otras deudas','','');
INSERT INTO `balances` VALUES (1554,'        4. Remuneraciones pendientes de pago','','');
INSERT INTO `balances` VALUES (1555,'        5. Fianzas y depósitos recibidos a corto plazo','','');
INSERT INTO `balances` VALUES (1560,'    VI. Provisiones para operaciones de tráfico','','');
INSERT INTO `balances` VALUES (1570,'    VII. Ajustes por periodificación','','');
INSERT INTO `balances` VALUES (2000,'A) GASTOS',0.0,0.0);
INSERT INTO `balances` VALUES (2010,'      1. Reducción de existencias productos terminados y en curso de fabricación','','');
INSERT INTO `balances` VALUES (2020,'      2. Aprovisionamientos','','');
INSERT INTO `balances` VALUES (2021,'          a) Consumo de mercaderías','','');
INSERT INTO `balances` VALUES (2022,'          b) Consumo de materias primas y otras materias consumibles','','');
INSERT INTO `balances` VALUES (2023,'          c) Otros gastos externos','','');
INSERT INTO `balances` VALUES (2030,'      3. Gastos de personal','','');
INSERT INTO `balances` VALUES (2031,'          a) Sueldos, salarios y asimilados','','');
INSERT INTO `balances` VALUES (2032,'          b) Cargas sociales','','');
INSERT INTO `balances` VALUES (2040,'      4. Dotaciones para amortizaciones de inmovilizado','','');
INSERT INTO `balances` VALUES (2050,'      5. Variación de las provisiones de tráfico','','');
INSERT INTO `balances` VALUES (2051,'          a) Variación de provisiones de existencias','','');
INSERT INTO `balances` VALUES (2052,'          b) Variación de provisiones y pérdidas de créditos incobrables','','');
INSERT INTO `balances` VALUES (2053,'          c) Variación de otras provisiones de tráfico','','');
INSERT INTO `balances` VALUES (2060,'      6. Otros gastos de explotación','','');
INSERT INTO `balances` VALUES (2061,'          a) Servicios exteriores','','');
INSERT INTO `balances` VALUES (2062,'          b) Tributos','','');
INSERT INTO `balances` VALUES (2063,'          c) Otros gastos de gestión corriente','','');
INSERT INTO `balances` VALUES (2064,'          d) Dotación al fondo de reversión','','');
INSERT INTO `balances` VALUES (2065,'    I. Beneficios de Explotación',0.0,0.0);
INSERT INTO `balances` VALUES (2070,'      7. Gastos financieros y gastos asimilados','','');
INSERT INTO `balances` VALUES (2071,'          a) Por deudas con empresas del grupo','','');
INSERT INTO `balances` VALUES (2072,'          b) Por deudas con empresas asociadas','','');
INSERT INTO `balances` VALUES (2073,'          c) Por deudas con terceros y gastos asimilados','','');
INSERT INTO `balances` VALUES (2074,'          d) Pérdidas de inversiones financieras','','');
INSERT INTO `balances` VALUES (2080,'      8. Variación de las provisiones de inversiones financieras','','');
INSERT INTO `balances` VALUES (2090,'      9. Diferencias negativas de cambio','','');
INSERT INTO `balances` VALUES (2091,'    II. Resultados financieros positivos',0.0,0.0);
INSERT INTO `balances` VALUES (2092,'    III. Beneficios actividades ordinarias',0.0,0.0);
INSERT INTO `balances` VALUES (2100,'      10. Var. previsiones inmovilizado inmaterial, material y cartera de control','','');
INSERT INTO `balances` VALUES (2110,'      11. Pérds.procedentes inmovilizado inmaterial,material y cartera de control','','');
INSERT INTO `balances` VALUES (2120,'      12. Pérdidas por operaciones con acciones y obligaciones propias','','');
INSERT INTO `balances` VALUES (2130,'      13. Gastos extraordinarios','','');
INSERT INTO `balances` VALUES (2140,'      14. Gastos y pérdidas de otros ejercicios','','');
INSERT INTO `balances` VALUES (2141,'    IV. Resultados extraordinarios positivos ',0.0,0.0);
INSERT INTO `balances` VALUES (2142,'    V. Beneficios antes de impuestos',0.0,0.0);
INSERT INTO `balances` VALUES (2150,'      15. Impuesto sobre sociedades','','');
INSERT INTO `balances` VALUES (2160,'      16. Otros impuestos','','');
INSERT INTO `balances` VALUES (2161,'    VI. Resultado del ejercicio (beneficios)',0.0,0.0);
INSERT INTO `balances` VALUES (3000,'B) INGRESOS','','');
INSERT INTO `balances` VALUES (3010,'      1. Importe neto de la cifra de negocio','','');
INSERT INTO `balances` VALUES (3011,'          a) Ventas','','');
INSERT INTO `balances` VALUES (3012,'          b) Prestaciones de servicios','','');
INSERT INTO `balances` VALUES (3013,'          c) Devoluciones y `rappels` sobre ventas','','');
INSERT INTO `balances` VALUES (3020,'      2.Aumento de las existencias productos terminados y en curso de fabricación','','');
INSERT INTO `balances` VALUES (3030,'      3. Trabajos efectuados por la empresa para el inmovilizado','','');
INSERT INTO `balances` VALUES (3040,'      4. Otros ingresos de explotación','','');
INSERT INTO `balances` VALUES (3041,'          a) Ingresos accesorios y otros de gestión corriente','','');
INSERT INTO `balances` VALUES (3042,'          b) Subvenciones','','');
INSERT INTO `balances` VALUES (3043,'          c) Exceso de provisiones de riesgos y gastos','','');
INSERT INTO `balances` VALUES (3044,'    I. Pérdidas de explotación',0.0,0.0);
INSERT INTO `balances` VALUES (3050,'      5. Ingresos de participaciones en capital','','');
INSERT INTO `balances` VALUES (3051,'          a) En empresas del grupo','','');
INSERT INTO `balances` VALUES (3052,'          b) En empresas asociadas','','');
INSERT INTO `balances` VALUES (3053,'          c) En empresas fuera del grupo','','');
INSERT INTO `balances` VALUES (3060,'      6. Ingresos de otros valores negociables y créditos del activo inmovilizado','','');
INSERT INTO `balances` VALUES (3061,'          a) De empresas del grupo','','');
INSERT INTO `balances` VALUES (3062,'          b) De empresas asociadas','','');
INSERT INTO `balances` VALUES (3063,'          c) De empresas del grupo','','');
INSERT INTO `balances` VALUES (3070,'      7. Otros intereses e ingresos asimilados','','');
INSERT INTO `balances` VALUES (3071,'          a) De empresas del grupo','','');
INSERT INTO `balances` VALUES (3072,'          b) De empresas asociadas','','');
INSERT INTO `balances` VALUES (3073,'          c) Otros intereses','','');
INSERT INTO `balances` VALUES (3074,'          d) Beneficios en inversiones financieras','','');
INSERT INTO `balances` VALUES (3080,'      8. Diferencias positivas de cambio','','');
INSERT INTO `balances` VALUES (3081,'    II. Resultados financieros negativos',0.0,0.0);
INSERT INTO `balances` VALUES (3082,'    III. Pérdidas actividades ordinarias',0.0,0.0);
INSERT INTO `balances` VALUES (3090,'      9. Beneficios enajenación inmovilizado inmaterial,material y cartera contro','','');
INSERT INTO `balances` VALUES (3100,'      10. Beneficios por operaciones con acciones y obligaciones propias','','');
INSERT INTO `balances` VALUES (3110,'      11. Subvenciones de capital transferidas al resultado del ejercicio','','');
INSERT INTO `balances` VALUES (3120,'      12. Ingresos extraordinarios','','');
INSERT INTO `balances` VALUES (3130,'      13. Ingresos y beneficios de otros ejercicios','','');
INSERT INTO `balances` VALUES (3160,'    IV. Resultados extraordinarios negativos',0.0,0.0);
INSERT INTO `balances` VALUES (3170,'    V. Pérdidas antes de impuestos',0.0,0.0);
INSERT INTO `balances` VALUES (3180,'    VI. Resultado del ejercicio (pérdidas)',0.0,0.0);
INSERT INTO `balances` VALUES (4000,'A) GASTOS',0.0,0.0);
INSERT INTO `balances` VALUES (4010,'         1.  Consumos de explotación',0.0,0.0);
INSERT INTO `balances` VALUES (4020,'         2.  Gastos de personal',0.0,0.0);
INSERT INTO `balances` VALUES (4021,'              a) Sueldos, salarios y asimilados',0.0,0.0);
INSERT INTO `balances` VALUES (4022,'              b) Cargas sociales',0.0,0.0);
INSERT INTO `balances` VALUES (4030,'         3.  Dotaciones para amortizaciones de inmovilizado',0.0,0.0);
INSERT INTO `balances` VALUES (4040,'         4.  Variación de las provisiones de tráfico y pérdidas de créditos incobrables',0.0,0.0);
INSERT INTO `balances` VALUES (4050,'         5.  Otros gastos de explotación',0.0,0.0);
INSERT INTO `balances` VALUES (4051,'    I.   BENEFICIOS DE EXPLOTACIÓN',0.0,0.0);
INSERT INTO `balances` VALUES (4060,'         6.  Gastos financieros y gastos asimilados',0.0,0.0);
INSERT INTO `balances` VALUES (4061,'              a) Por deudas con empresas del grupo',0.0,0.0);
INSERT INTO `balances` VALUES (4062,'              b) Por deudas con empresas asociadas',0.0,0.0);
INSERT INTO `balances` VALUES (4063,'              c) Por otras deudas',0.0,0.0);
INSERT INTO `balances` VALUES (4064,'              d) Pérdidas de inversiones financieras',0.0,0.0);
INSERT INTO `balances` VALUES (4070,'         7.  Variación de las provisiones de inversiones financieras',0.0,0.0);
INSERT INTO `balances` VALUES (4080,'         8.  Diferencias negativas de cambio',0.0,0.0);
INSERT INTO `balances` VALUES (4081,'    II.  RESULTADOS FINANCIEROS POSITIVOS',0.0,0.0);
INSERT INTO `balances` VALUES (4082,'    III. BENEFICIOS DE LAS ACTIVIDADES ORDINARIAS',0.0,0.0);
INSERT INTO `balances` VALUES (4090,'         9.  Variación de las provisiones de inmovilizado inmaterial, material y cartera de control',0.0,0.0);
INSERT INTO `balances` VALUES (4100,'        10. Pérdidas procedentes del inmovilizado inmaterial, material y cartera de control',0.0,0.0);
INSERT INTO `balances` VALUES (4110,'        11. Pérdidas por operaciones con acciones y obligaciones propias',0.0,0.0);
INSERT INTO `balances` VALUES (4120,'        12. Gastos extraordinarios',0.0,0.0);
INSERT INTO `balances` VALUES (4130,'        13. Gastos y pérdidas de otros ejercicios',0.0,0.0);
INSERT INTO `balances` VALUES (4131,'   IV. RESULTADOS EXTRAORDINARIOS POSITIVOS',0.0,0.0);
INSERT INTO `balances` VALUES (4132,'   V.  BENEFICIOS ANTES DE IMPUESTOS',0.0,0.0);
INSERT INTO `balances` VALUES (4140,'        14. Impuesto sobre sociedades',0.0,0.0);
INSERT INTO `balances` VALUES (4150,'        15. Otros impuestos',0.0,0.0);
INSERT INTO `balances` VALUES (4151,'   VI. RESULTADO DEL EJERCICIO (BENEFICIOS)',0.0,0.0);
INSERT INTO `balances` VALUES (5000,'B) INGRESOS',0.0,0.0);
INSERT INTO `balances` VALUES (5010,'        1. Ingresos de explotación',0.0,0.0);
INSERT INTO `balances` VALUES (5011,'            a) Importe neto de la cifra de negocios',0.0,0.0);
INSERT INTO `balances` VALUES (5012,'            b) Otros ingresos de explotación',0.0,0.0);
INSERT INTO `balances` VALUES (5013,'    I.  PERDIDAS DE EXPLOTACION',0.0,0.0);
INSERT INTO `balances` VALUES (5020,'        2. Ingresos financieros',0.0,0.0);
INSERT INTO `balances` VALUES (5021,'            a) En empresas del grupo',0.0,0.0);
INSERT INTO `balances` VALUES (5022,'            b) En empresas asociadas',0.0,0.0);
INSERT INTO `balances` VALUES (5023,'            c) Otros',0.0,0.0);
INSERT INTO `balances` VALUES (5024,'            d) Beneficios en inversiones financieras',0.0,0.0);
INSERT INTO `balances` VALUES (5030,'        3. Diferencias positivas de cambio',0.0,0.0);
INSERT INTO `balances` VALUES (5031,'   II.  RESULTADOS FINANCIEROS NEGATIVOS',0.0,0.0);
INSERT INTO `balances` VALUES (5032,'   III. PERDIDAS DE LAS ACTIVIDADES ORDINARIAS',0.0,0.0);
INSERT INTO `balances` VALUES (5040,'       4. Beneficios en enajenación de inmovilizado inmaterial, material y cartera de control',0.0,0.0);
INSERT INTO `balances` VALUES (5050,'       5. Beneficios por operaciones con acciones y obligaciones propias',0.0,0.0);
INSERT INTO `balances` VALUES (5060,'       6. Subvenciones de capital transferidas al resultado del ejercicio',0.0,0.0);
INSERT INTO `balances` VALUES (5070,'       7. Ingresos extraordinarios',0.0,0.0);
INSERT INTO `balances` VALUES (5080,'       8. Ingresos y beneficios de otros ejercicios',0.0,0.0);
INSERT INTO `balances` VALUES (5081,'   IV. RESULTADOS EXTRAORDINARIOS NEGATIVOS',0.0,0.0);
INSERT INTO `balances` VALUES (5082,'   V.  PÉRDIDAS ANTES DE IMPUESTOS',0.0,0.0);
INSERT INTO `balances` VALUES (5083,'   VI. RESULTADO DEL EJERCICIO (PÉRDIDAS)',0.0,0.0);
INSERT INTO `balancescuentas` VALUES (100,190,0);
INSERT INTO `balancescuentas` VALUES (100,191,0);
INSERT INTO `balancescuentas` VALUES (100,192,0);
INSERT INTO `balancescuentas` VALUES (100,193,0);
INSERT INTO `balancescuentas` VALUES (100,194,0);
INSERT INTO `balancescuentas` VALUES (100,195,0);
INSERT INTO `balancescuentas` VALUES (210,20,0);
INSERT INTO `balancescuentas` VALUES (221,210,0);
INSERT INTO `balancescuentas` VALUES (222,211,0);
INSERT INTO `balancescuentas` VALUES (222,212,0);
INSERT INTO `balancescuentas` VALUES (223,213,0);
INSERT INTO `balancescuentas` VALUES (224,214,0);
INSERT INTO `balancescuentas` VALUES (225,215,0);
INSERT INTO `balancescuentas` VALUES (226,219,0);
INSERT INTO `balancescuentas` VALUES (227,291,0);
INSERT INTO `balancescuentas` VALUES (228,281,0);
INSERT INTO `balancescuentas` VALUES (231,220,0);
INSERT INTO `balancescuentas` VALUES (231,221,0);
INSERT INTO `balancescuentas` VALUES (232,222,0);
INSERT INTO `balancescuentas` VALUES (232,223,0);
INSERT INTO `balancescuentas` VALUES (233,224,0);
INSERT INTO `balancescuentas` VALUES (233,225,0);
INSERT INTO `balancescuentas` VALUES (233,226,0);
INSERT INTO `balancescuentas` VALUES (234,23,0);
INSERT INTO `balancescuentas` VALUES (235,227,0);
INSERT INTO `balancescuentas` VALUES (235,228,0);
INSERT INTO `balancescuentas` VALUES (235,229,0);
INSERT INTO `balancescuentas` VALUES (236,292,0);
INSERT INTO `balancescuentas` VALUES (237,282,0);
INSERT INTO `balancescuentas` VALUES (241,240,0);
INSERT INTO `balancescuentas` VALUES (242,242,0);
INSERT INTO `balancescuentas` VALUES (242,244,0);
INSERT INTO `balancescuentas` VALUES (243,241,0);
INSERT INTO `balancescuentas` VALUES (244,243,0);
INSERT INTO `balancescuentas` VALUES (244,245,0);
INSERT INTO `balancescuentas` VALUES (244,247,0);
INSERT INTO `balancescuentas` VALUES (245,250,0);
INSERT INTO `balancescuentas` VALUES (245,251,0);
INSERT INTO `balancescuentas` VALUES (245,256,0);
INSERT INTO `balancescuentas` VALUES (246,252,0);
INSERT INTO `balancescuentas` VALUES (246,253,0);
INSERT INTO `balancescuentas` VALUES (246,254,0);
INSERT INTO `balancescuentas` VALUES (246,257,0);
INSERT INTO `balancescuentas` VALUES (246,258,0);
INSERT INTO `balancescuentas` VALUES (247,260,0);
INSERT INTO `balancescuentas` VALUES (247,265,0);
INSERT INTO `balancescuentas` VALUES (248,293,0);
INSERT INTO `balancescuentas` VALUES (248,294,0);
INSERT INTO `balancescuentas` VALUES (248,295,0);
INSERT INTO `balancescuentas` VALUES (248,296,0);
INSERT INTO `balancescuentas` VALUES (248,297,0);
INSERT INTO `balancescuentas` VALUES (248,298,0);
INSERT INTO `balancescuentas` VALUES (250,198,0);
INSERT INTO `balancescuentas` VALUES (300,27,0);
INSERT INTO `balancescuentas` VALUES (410,558,0);
INSERT INTO `balancescuentas` VALUES (421,30,0);
INSERT INTO `balancescuentas` VALUES (422,31,0);
INSERT INTO `balancescuentas` VALUES (422,32,0);
INSERT INTO `balancescuentas` VALUES (423,33,0);
INSERT INTO `balancescuentas` VALUES (423,34,0);
INSERT INTO `balancescuentas` VALUES (424,35,0);
INSERT INTO `balancescuentas` VALUES (425,36,0);
INSERT INTO `balancescuentas` VALUES (426,407,0);
INSERT INTO `balancescuentas` VALUES (427,39,0);
INSERT INTO `balancescuentas` VALUES (431,430,0);
INSERT INTO `balancescuentas` VALUES (431,431,0);
INSERT INTO `balancescuentas` VALUES (431,435,0);
INSERT INTO `balancescuentas` VALUES (431,436,0);
INSERT INTO `balancescuentas` VALUES (432,432,0);
INSERT INTO `balancescuentas` VALUES (432,551,1);
INSERT INTO `balancescuentas` VALUES (433,433,0);
INSERT INTO `balancescuentas` VALUES (433,552,1);
INSERT INTO `balancescuentas` VALUES (434,44,0);
INSERT INTO `balancescuentas` VALUES (434,553,1);
INSERT INTO `balancescuentas` VALUES (435,460,0);
INSERT INTO `balancescuentas` VALUES (435,544,0);
INSERT INTO `balancescuentas` VALUES (436,470,0);
INSERT INTO `balancescuentas` VALUES (436,471,0);
INSERT INTO `balancescuentas` VALUES (436,472,0);
INSERT INTO `balancescuentas` VALUES (436,474,0);
INSERT INTO `balancescuentas` VALUES (437,490,0);
INSERT INTO `balancescuentas` VALUES (437,493,0);
INSERT INTO `balancescuentas` VALUES (437,494,0);
INSERT INTO `balancescuentas` VALUES (441,530,0);
INSERT INTO `balancescuentas` VALUES (441,538,0);
INSERT INTO `balancescuentas` VALUES (442,532,0);
INSERT INTO `balancescuentas` VALUES (442,534,0);
INSERT INTO `balancescuentas` VALUES (442,536,0);
INSERT INTO `balancescuentas` VALUES (443,531,0);
INSERT INTO `balancescuentas` VALUES (443,539,0);
INSERT INTO `balancescuentas` VALUES (444,533,0);
INSERT INTO `balancescuentas` VALUES (444,535,0);
INSERT INTO `balancescuentas` VALUES (444,537,0);
INSERT INTO `balancescuentas` VALUES (445,540,0);
INSERT INTO `balancescuentas` VALUES (445,541,0);
INSERT INTO `balancescuentas` VALUES (445,546,0);
INSERT INTO `balancescuentas` VALUES (445,549,0);
INSERT INTO `balancescuentas` VALUES (446,542,0);
INSERT INTO `balancescuentas` VALUES (446,543,0);
INSERT INTO `balancescuentas` VALUES (446,545,0);
INSERT INTO `balancescuentas` VALUES (446,547,0);
INSERT INTO `balancescuentas` VALUES (446,548,0);
INSERT INTO `balancescuentas` VALUES (447,565,0);
INSERT INTO `balancescuentas` VALUES (447,566,0);
INSERT INTO `balancescuentas` VALUES (448,593,0);
INSERT INTO `balancescuentas` VALUES (448,594,0);
INSERT INTO `balancescuentas` VALUES (448,595,0);
INSERT INTO `balancescuentas` VALUES (448,596,0);
INSERT INTO `balancescuentas` VALUES (448,597,0);
INSERT INTO `balancescuentas` VALUES (448,598,0);
INSERT INTO `balancescuentas` VALUES (460,57,0);
INSERT INTO `balancescuentas` VALUES (470,480,0);
INSERT INTO `balancescuentas` VALUES (470,580,0);
INSERT INTO `balancescuentas` VALUES (1110,10,2);
INSERT INTO `balancescuentas` VALUES (1120,110,2);
INSERT INTO `balancescuentas` VALUES (1130,111,2);
INSERT INTO `balancescuentas` VALUES (1141,112,2);
INSERT INTO `balancescuentas` VALUES (1142,115,2);
INSERT INTO `balancescuentas` VALUES (1143,114,2);
INSERT INTO `balancescuentas` VALUES (1144,116,2);
INSERT INTO `balancescuentas` VALUES (1145,113,2);
INSERT INTO `balancescuentas` VALUES (1145,117,2);
INSERT INTO `balancescuentas` VALUES (1145,118,2);
INSERT INTO `balancescuentas` VALUES (1151,120,2);
INSERT INTO `balancescuentas` VALUES (1152,121,2);
INSERT INTO `balancescuentas` VALUES (1153,122,2);
INSERT INTO `balancescuentas` VALUES (420,30,0);
INSERT INTO `balancescuentas` VALUES (1160,129,2);
INSERT INTO `balancescuentas` VALUES (1170,557,2);
INSERT INTO `balancescuentas` VALUES (1201,130,2);
INSERT INTO `balancescuentas` VALUES (1201,131,2);
INSERT INTO `balancescuentas` VALUES (1202,136,2);
INSERT INTO `balancescuentas` VALUES (1203,135,2);
INSERT INTO `balancescuentas` VALUES (1301,140,2);
INSERT INTO `balancescuentas` VALUES (1302,141,2);
INSERT INTO `balancescuentas` VALUES (1303,142,2);
INSERT INTO `balancescuentas` VALUES (1303,143,2);
INSERT INTO `balancescuentas` VALUES (1304,144,2);
INSERT INTO `balancescuentas` VALUES (1411,150,2);
INSERT INTO `balancescuentas` VALUES (1412,151,2);
INSERT INTO `balancescuentas` VALUES (1413,155,2);
INSERT INTO `balancescuentas` VALUES (1420,170,2);
INSERT INTO `balancescuentas` VALUES (1431,160,2);
INSERT INTO `balancescuentas` VALUES (1431,162,2);
INSERT INTO `balancescuentas` VALUES (1431,164,2);
INSERT INTO `balancescuentas` VALUES (1432,161,2);
INSERT INTO `balancescuentas` VALUES (1432,163,2);
INSERT INTO `balancescuentas` VALUES (1432,165,2);
INSERT INTO `balancescuentas` VALUES (1441,174,2);
INSERT INTO `balancescuentas` VALUES (1442,171,2);
INSERT INTO `balancescuentas` VALUES (1442,172,2);
INSERT INTO `balancescuentas` VALUES (1442,173,2);
INSERT INTO `balancescuentas` VALUES (1443,180,2);
INSERT INTO `balancescuentas` VALUES (1443,185,2);
INSERT INTO `balancescuentas` VALUES (1451,248,2);
INSERT INTO `balancescuentas` VALUES (1452,249,2);
INSERT INTO `balancescuentas` VALUES (1453,259,2);
INSERT INTO `balancescuentas` VALUES (1511,500,2);
INSERT INTO `balancescuentas` VALUES (1512,501,2);
INSERT INTO `balancescuentas` VALUES (1513,505,2);
INSERT INTO `balancescuentas` VALUES (1514,506,2);
INSERT INTO `balancescuentas` VALUES (1521,520,2);
INSERT INTO `balancescuentas` VALUES (1522,526,2);
INSERT INTO `balancescuentas` VALUES (1531,402,2);
INSERT INTO `balancescuentas` VALUES (1531,510,2);
INSERT INTO `balancescuentas` VALUES (1531,512,2);
INSERT INTO `balancescuentas` VALUES (1531,514,2);
INSERT INTO `balancescuentas` VALUES (1531,516,2);
INSERT INTO `balancescuentas` VALUES (1531,551,3);
INSERT INTO `balancescuentas` VALUES (1532,403,2);
INSERT INTO `balancescuentas` VALUES (1532,511,2);
INSERT INTO `balancescuentas` VALUES (1532,513,2);
INSERT INTO `balancescuentas` VALUES (1532,515,2);
INSERT INTO `balancescuentas` VALUES (1532,517,2);
INSERT INTO `balancescuentas` VALUES (1532,552,3);
INSERT INTO `balancescuentas` VALUES (1541,437,2);
INSERT INTO `balancescuentas` VALUES (1542,400,2);
INSERT INTO `balancescuentas` VALUES (1542,406,2);
INSERT INTO `balancescuentas` VALUES (1542,410,2);
INSERT INTO `balancescuentas` VALUES (1542,419,2);
INSERT INTO `balancescuentas` VALUES (1543,401,2);
INSERT INTO `balancescuentas` VALUES (1543,411,2);
INSERT INTO `balancescuentas` VALUES (1551,475,2);
INSERT INTO `balancescuentas` VALUES (1551,476,2);
INSERT INTO `balancescuentas` VALUES (1551,477,2);
INSERT INTO `balancescuentas` VALUES (1551,479,2);
INSERT INTO `balancescuentas` VALUES (1552,524,2);
INSERT INTO `balancescuentas` VALUES (1553,509,2);
INSERT INTO `balancescuentas` VALUES (1553,521,2);
INSERT INTO `balancescuentas` VALUES (1553,523,2);
INSERT INTO `balancescuentas` VALUES (1553,525,2);
INSERT INTO `balancescuentas` VALUES (1553,527,2);
INSERT INTO `balancescuentas` VALUES (1553,553,3);
INSERT INTO `balancescuentas` VALUES (1553,555,2);
INSERT INTO `balancescuentas` VALUES (1553,556,2);
INSERT INTO `balancescuentas` VALUES (1554,465,2);
INSERT INTO `balancescuentas` VALUES (1555,560,2);
INSERT INTO `balancescuentas` VALUES (1555,561,2);
INSERT INTO `balancescuentas` VALUES (1560,499,2);
INSERT INTO `balancescuentas` VALUES (1570,485,2);
INSERT INTO `balancescuentas` VALUES (1570,585,2);
INSERT INTO `balancescuentas` VALUES (2010,71,1);
INSERT INTO `balancescuentas` VALUES (2021,600,0);
INSERT INTO `balancescuentas` VALUES (2021,6080,0);
INSERT INTO `balancescuentas` VALUES (2021,6090,0);
INSERT INTO `balancescuentas` VALUES (2021,610,0);
INSERT INTO `balancescuentas` VALUES (2022,601,0);
INSERT INTO `balancescuentas` VALUES (2022,602,0);
INSERT INTO `balancescuentas` VALUES (2022,6081,0);
INSERT INTO `balancescuentas` VALUES (2022,6082,0);
INSERT INTO `balancescuentas` VALUES (2022,6091,0);
INSERT INTO `balancescuentas` VALUES (2022,6092,0);
INSERT INTO `balancescuentas` VALUES (2022,611,0);
INSERT INTO `balancescuentas` VALUES (2022,612,0);
INSERT INTO `balancescuentas` VALUES (2023,607,0);
INSERT INTO `balancescuentas` VALUES (2031,640,0);
INSERT INTO `balancescuentas` VALUES (2031,641,0);
INSERT INTO `balancescuentas` VALUES (2032,642,0);
INSERT INTO `balancescuentas` VALUES (2032,643,0);
INSERT INTO `balancescuentas` VALUES (2032,649,0);
INSERT INTO `balancescuentas` VALUES (2040,68,0);
INSERT INTO `balancescuentas` VALUES (2051,693,0);
INSERT INTO `balancescuentas` VALUES (2051,793,0);
INSERT INTO `balancescuentas` VALUES (2052,650,0);
INSERT INTO `balancescuentas` VALUES (2052,694,0);
INSERT INTO `balancescuentas` VALUES (2052,794,0);
INSERT INTO `balancescuentas` VALUES (2053,695,0);
INSERT INTO `balancescuentas` VALUES (2053,795,0);
INSERT INTO `balancescuentas` VALUES (2061,62,0);
INSERT INTO `balancescuentas` VALUES (2062,631,0);
INSERT INTO `balancescuentas` VALUES (2062,634,0);
INSERT INTO `balancescuentas` VALUES (2062,636,0);
INSERT INTO `balancescuentas` VALUES (2062,639,0);
INSERT INTO `balancescuentas` VALUES (2063,651,0);
INSERT INTO `balancescuentas` VALUES (2063,659,0);
INSERT INTO `balancescuentas` VALUES (2064,690,0);
INSERT INTO `balancescuentas` VALUES (2071,6610,0);
INSERT INTO `balancescuentas` VALUES (2071,6615,0);
INSERT INTO `balancescuentas` VALUES (2071,6620,0);
INSERT INTO `balancescuentas` VALUES (2071,6630,0);
INSERT INTO `balancescuentas` VALUES (2071,6640,0);
INSERT INTO `balancescuentas` VALUES (2071,6650,0);
INSERT INTO `balancescuentas` VALUES (2072,6611,0);
INSERT INTO `balancescuentas` VALUES (2072,6616,0);
INSERT INTO `balancescuentas` VALUES (2072,6621,0);
INSERT INTO `balancescuentas` VALUES (2072,6631,0);
INSERT INTO `balancescuentas` VALUES (2072,6641,0);
INSERT INTO `balancescuentas` VALUES (2072,6651,0);
INSERT INTO `balancescuentas` VALUES (2073,6613,0);
INSERT INTO `balancescuentas` VALUES (2073,6618,0);
INSERT INTO `balancescuentas` VALUES (2073,6622,0);
INSERT INTO `balancescuentas` VALUES (2073,6623,0);
INSERT INTO `balancescuentas` VALUES (2073,6632,0);
INSERT INTO `balancescuentas` VALUES (2073,6633,0);
INSERT INTO `balancescuentas` VALUES (2073,6642,0);
INSERT INTO `balancescuentas` VALUES (2073,6653,0);
INSERT INTO `balancescuentas` VALUES (2073,669,0);
INSERT INTO `balancescuentas` VALUES (2074,666,0);
INSERT INTO `balancescuentas` VALUES (2074,667,0);
INSERT INTO `balancescuentas` VALUES (2080,6963,0);
INSERT INTO `balancescuentas` VALUES (2080,6965,0);
INSERT INTO `balancescuentas` VALUES (2080,6966,0);
INSERT INTO `balancescuentas` VALUES (2080,697,0);
INSERT INTO `balancescuentas` VALUES (2080,698,0);
INSERT INTO `balancescuentas` VALUES (2080,699,0);
INSERT INTO `balancescuentas` VALUES (2080,7963,0);
INSERT INTO `balancescuentas` VALUES (2080,7965,0);
INSERT INTO `balancescuentas` VALUES (2080,7966,0);
INSERT INTO `balancescuentas` VALUES (2080,797,0);
INSERT INTO `balancescuentas` VALUES (2080,798,0);
INSERT INTO `balancescuentas` VALUES (2080,799,0);
INSERT INTO `balancescuentas` VALUES (2090,668,0);
INSERT INTO `balancescuentas` VALUES (2100,691,0);
INSERT INTO `balancescuentas` VALUES (2100,692,0);
INSERT INTO `balancescuentas` VALUES (2100,6960,0);
INSERT INTO `balancescuentas` VALUES (2100,6961,0);
INSERT INTO `balancescuentas` VALUES (2100,791,0);
INSERT INTO `balancescuentas` VALUES (2100,792,0);
INSERT INTO `balancescuentas` VALUES (2100,7960,0);
INSERT INTO `balancescuentas` VALUES (2100,7961,0);
INSERT INTO `balancescuentas` VALUES (2110,670,0);
INSERT INTO `balancescuentas` VALUES (2110,671,0);
INSERT INTO `balancescuentas` VALUES (2110,672,0);
INSERT INTO `balancescuentas` VALUES (2110,673,0);
INSERT INTO `balancescuentas` VALUES (2120,674,0);
INSERT INTO `balancescuentas` VALUES (2130,678,0);
INSERT INTO `balancescuentas` VALUES (2140,679,0);
INSERT INTO `balancescuentas` VALUES (2150,630,0);
INSERT INTO `balancescuentas` VALUES (2150,633,0);
INSERT INTO `balancescuentas` VALUES (2150,638,0);
INSERT INTO `balancescuentas` VALUES (3011,700,2);
INSERT INTO `balancescuentas` VALUES (3011,701,2);
INSERT INTO `balancescuentas` VALUES (3011,702,2);
INSERT INTO `balancescuentas` VALUES (3011,703,2);
INSERT INTO `balancescuentas` VALUES (3011,704,2);
INSERT INTO `balancescuentas` VALUES (3012,705,2);
INSERT INTO `balancescuentas` VALUES (3013,708,2);
INSERT INTO `balancescuentas` VALUES (3013,709,2);
INSERT INTO `balancescuentas` VALUES (3020,71,3);
INSERT INTO `balancescuentas` VALUES (3030,73,2);
INSERT INTO `balancescuentas` VALUES (3041,75,2);
INSERT INTO `balancescuentas` VALUES (3042,74,2);
INSERT INTO `balancescuentas` VALUES (3043,790,2);
INSERT INTO `balancescuentas` VALUES (3051,7600,2);
INSERT INTO `balancescuentas` VALUES (3052,7601,2);
INSERT INTO `balancescuentas` VALUES (3053,7603,2);
INSERT INTO `balancescuentas` VALUES (3061,7610,2);
INSERT INTO `balancescuentas` VALUES (3061,7620,2);
INSERT INTO `balancescuentas` VALUES (3062,7611,2);
INSERT INTO `balancescuentas` VALUES (3062,7621,2);
INSERT INTO `balancescuentas` VALUES (3063,7613,2);
INSERT INTO `balancescuentas` VALUES (3063,7623,2);
INSERT INTO `balancescuentas` VALUES (3071,7630,2);
INSERT INTO `balancescuentas` VALUES (3071,7650,2);
INSERT INTO `balancescuentas` VALUES (3072,7631,2);
INSERT INTO `balancescuentas` VALUES (3072,7651,2);
INSERT INTO `balancescuentas` VALUES (3073,7633,2);
INSERT INTO `balancescuentas` VALUES (3073,7653,2);
INSERT INTO `balancescuentas` VALUES (3073,769,2);
INSERT INTO `balancescuentas` VALUES (3074,766,2);
INSERT INTO `balancescuentas` VALUES (3080,768,2);
INSERT INTO `balancescuentas` VALUES (3090,770,2);
INSERT INTO `balancescuentas` VALUES (3090,771,2);
INSERT INTO `balancescuentas` VALUES (3090,772,2);
INSERT INTO `balancescuentas` VALUES (3100,774,2);
INSERT INTO `balancescuentas` VALUES (3110,775,2);
INSERT INTO `balancescuentas` VALUES (3120,778,2);
INSERT INTO `balancescuentas` VALUES (3130,779,2);
INSERT INTO `balancescuentas` VALUES (100,196,0);
INSERT INTO `balancescuentas` VALUES (220,210,0);
INSERT INTO `balancescuentas` VALUES (220,211,0);
INSERT INTO `balancescuentas` VALUES (220,212,0);
INSERT INTO `balancescuentas` VALUES (220,213,0);
INSERT INTO `balancescuentas` VALUES (220,214,0);
INSERT INTO `balancescuentas` VALUES (220,215,0);
INSERT INTO `balancescuentas` VALUES (220,219,0);
INSERT INTO `balancescuentas` VALUES (220,291,0);
INSERT INTO `balancescuentas` VALUES (220,281,0);
INSERT INTO `balancescuentas` VALUES (200,20,0);
INSERT INTO `balancescuentas` VALUES (200,210,0);
INSERT INTO `balancescuentas` VALUES (200,211,0);
INSERT INTO `balancescuentas` VALUES (200,212,0);
INSERT INTO `balancescuentas` VALUES (200,213,0);
INSERT INTO `balancescuentas` VALUES (200,214,0);
INSERT INTO `balancescuentas` VALUES (200,215,0);
INSERT INTO `balancescuentas` VALUES (200,219,0);
INSERT INTO `balancescuentas` VALUES (200,291,0);
INSERT INTO `balancescuentas` VALUES (200,281,0);
INSERT INTO `balancescuentas` VALUES (230,220,0);
INSERT INTO `balancescuentas` VALUES (230,221,0);
INSERT INTO `balancescuentas` VALUES (230,222,0);
INSERT INTO `balancescuentas` VALUES (230,223,0);
INSERT INTO `balancescuentas` VALUES (230,224,0);
INSERT INTO `balancescuentas` VALUES (230,225,0);
INSERT INTO `balancescuentas` VALUES (230,226,0);
INSERT INTO `balancescuentas` VALUES (230,23,0);
INSERT INTO `balancescuentas` VALUES (230,227,0);
INSERT INTO `balancescuentas` VALUES (230,228,0);
INSERT INTO `balancescuentas` VALUES (230,229,0);
INSERT INTO `balancescuentas` VALUES (230,292,0);
INSERT INTO `balancescuentas` VALUES (230,282,0);
INSERT INTO `balancescuentas` VALUES (200,220,0);
INSERT INTO `balancescuentas` VALUES (200,221,0);
INSERT INTO `balancescuentas` VALUES (200,222,0);
INSERT INTO `balancescuentas` VALUES (200,223,0);
INSERT INTO `balancescuentas` VALUES (200,224,0);
INSERT INTO `balancescuentas` VALUES (200,225,0);
INSERT INTO `balancescuentas` VALUES (200,226,0);
INSERT INTO `balancescuentas` VALUES (200,23,0);
INSERT INTO `balancescuentas` VALUES (200,227,0);
INSERT INTO `balancescuentas` VALUES (200,228,0);
INSERT INTO `balancescuentas` VALUES (200,229,0);
INSERT INTO `balancescuentas` VALUES (200,292,0);
INSERT INTO `balancescuentas` VALUES (200,282,0);
INSERT INTO `balancescuentas` VALUES (242,246,0);
INSERT INTO `balancescuentas` VALUES (240,240,0);
INSERT INTO `balancescuentas` VALUES (240,242,0);
INSERT INTO `balancescuentas` VALUES (240,244,0);
INSERT INTO `balancescuentas` VALUES (240,246,0);
INSERT INTO `balancescuentas` VALUES (240,241,0);
INSERT INTO `balancescuentas` VALUES (240,243,0);
INSERT INTO `balancescuentas` VALUES (240,245,0);
INSERT INTO `balancescuentas` VALUES (240,247,0);
INSERT INTO `balancescuentas` VALUES (240,250,0);
INSERT INTO `balancescuentas` VALUES (240,251,0);
INSERT INTO `balancescuentas` VALUES (240,256,0);
INSERT INTO `balancescuentas` VALUES (240,252,0);
INSERT INTO `balancescuentas` VALUES (240,253,0);
INSERT INTO `balancescuentas` VALUES (240,254,0);
INSERT INTO `balancescuentas` VALUES (240,257,0);
INSERT INTO `balancescuentas` VALUES (240,258,0);
INSERT INTO `balancescuentas` VALUES (240,260,0);
INSERT INTO `balancescuentas` VALUES (240,265,0);
INSERT INTO `balancescuentas` VALUES (240,293,0);
INSERT INTO `balancescuentas` VALUES (240,294,0);
INSERT INTO `balancescuentas` VALUES (240,295,0);
INSERT INTO `balancescuentas` VALUES (240,296,0);
INSERT INTO `balancescuentas` VALUES (240,297,0);
INSERT INTO `balancescuentas` VALUES (240,298,0);
INSERT INTO `balancescuentas` VALUES (200,240,0);
INSERT INTO `balancescuentas` VALUES (200,242,0);
INSERT INTO `balancescuentas` VALUES (200,244,0);
INSERT INTO `balancescuentas` VALUES (200,246,0);
INSERT INTO `balancescuentas` VALUES (200,241,0);
INSERT INTO `balancescuentas` VALUES (200,243,0);
INSERT INTO `balancescuentas` VALUES (200,245,0);
INSERT INTO `balancescuentas` VALUES (200,247,0);
INSERT INTO `balancescuentas` VALUES (200,250,0);
INSERT INTO `balancescuentas` VALUES (200,251,0);
INSERT INTO `balancescuentas` VALUES (200,256,0);
INSERT INTO `balancescuentas` VALUES (200,252,0);
INSERT INTO `balancescuentas` VALUES (200,253,0);
INSERT INTO `balancescuentas` VALUES (200,254,0);
INSERT INTO `balancescuentas` VALUES (200,257,0);
INSERT INTO `balancescuentas` VALUES (200,258,0);
INSERT INTO `balancescuentas` VALUES (200,260,0);
INSERT INTO `balancescuentas` VALUES (200,265,0);
INSERT INTO `balancescuentas` VALUES (200,293,0);
INSERT INTO `balancescuentas` VALUES (200,294,0);
INSERT INTO `balancescuentas` VALUES (200,295,0);
INSERT INTO `balancescuentas` VALUES (200,296,0);
INSERT INTO `balancescuentas` VALUES (200,297,0);
INSERT INTO `balancescuentas` VALUES (200,298,0);
INSERT INTO `balancescuentas` VALUES (200,198,0);
INSERT INTO `balancescuentas` VALUES (420,31,0);
INSERT INTO `balancescuentas` VALUES (420,32,0);
INSERT INTO `balancescuentas` VALUES (420,33,0);
INSERT INTO `balancescuentas` VALUES (420,34,0);
INSERT INTO `balancescuentas` VALUES (420,35,0);
INSERT INTO `balancescuentas` VALUES (420,36,0);
INSERT INTO `balancescuentas` VALUES (420,407,0);
INSERT INTO `balancescuentas` VALUES (420,39,0);
INSERT INTO `balancescuentas` VALUES (400,558,0);
INSERT INTO `balancescuentas` VALUES (400,30,0);
INSERT INTO `balancescuentas` VALUES (400,31,0);
INSERT INTO `balancescuentas` VALUES (400,32,0);
INSERT INTO `balancescuentas` VALUES (400,33,0);
INSERT INTO `balancescuentas` VALUES (400,34,0);
INSERT INTO `balancescuentas` VALUES (400,35,0);
INSERT INTO `balancescuentas` VALUES (400,36,0);
INSERT INTO `balancescuentas` VALUES (400,407,0);
INSERT INTO `balancescuentas` VALUES (400,39,0);
INSERT INTO `balancescuentas` VALUES (430,430,0);
INSERT INTO `balancescuentas` VALUES (430,431,0);
INSERT INTO `balancescuentas` VALUES (430,435,0);
INSERT INTO `balancescuentas` VALUES (430,436,0);
INSERT INTO `balancescuentas` VALUES (430,432,0);
INSERT INTO `balancescuentas` VALUES (430,551,1);
INSERT INTO `balancescuentas` VALUES (430,433,0);
INSERT INTO `balancescuentas` VALUES (430,552,1);
INSERT INTO `balancescuentas` VALUES (430,44,0);
INSERT INTO `balancescuentas` VALUES (430,553,1);
INSERT INTO `balancescuentas` VALUES (430,460,0);
INSERT INTO `balancescuentas` VALUES (430,544,0);
INSERT INTO `balancescuentas` VALUES (430,470,0);
INSERT INTO `balancescuentas` VALUES (430,471,0);
INSERT INTO `balancescuentas` VALUES (430,472,0);
INSERT INTO `balancescuentas` VALUES (430,474,0);
INSERT INTO `balancescuentas` VALUES (430,490,0);
INSERT INTO `balancescuentas` VALUES (430,493,0);
INSERT INTO `balancescuentas` VALUES (430,494,0);
INSERT INTO `balancescuentas` VALUES (400,430,0);
INSERT INTO `balancescuentas` VALUES (400,431,0);
INSERT INTO `balancescuentas` VALUES (400,435,0);
INSERT INTO `balancescuentas` VALUES (400,436,0);
INSERT INTO `balancescuentas` VALUES (400,432,0);
INSERT INTO `balancescuentas` VALUES (400,551,1);
INSERT INTO `balancescuentas` VALUES (400,433,0);
INSERT INTO `balancescuentas` VALUES (400,552,1);
INSERT INTO `balancescuentas` VALUES (400,44,0);
INSERT INTO `balancescuentas` VALUES (400,553,1);
INSERT INTO `balancescuentas` VALUES (400,460,0);
INSERT INTO `balancescuentas` VALUES (400,544,0);
INSERT INTO `balancescuentas` VALUES (400,470,0);
INSERT INTO `balancescuentas` VALUES (400,471,0);
INSERT INTO `balancescuentas` VALUES (400,472,0);
INSERT INTO `balancescuentas` VALUES (400,474,0);
INSERT INTO `balancescuentas` VALUES (400,490,0);
INSERT INTO `balancescuentas` VALUES (400,493,0);
INSERT INTO `balancescuentas` VALUES (400,494,0);
INSERT INTO `balancescuentas` VALUES (440,530,0);
INSERT INTO `balancescuentas` VALUES (440,538,0);
INSERT INTO `balancescuentas` VALUES (440,532,0);
INSERT INTO `balancescuentas` VALUES (440,534,0);
INSERT INTO `balancescuentas` VALUES (440,536,0);
INSERT INTO `balancescuentas` VALUES (440,531,0);
INSERT INTO `balancescuentas` VALUES (440,539,0);
INSERT INTO `balancescuentas` VALUES (440,533,0);
INSERT INTO `balancescuentas` VALUES (440,535,0);
INSERT INTO `balancescuentas` VALUES (440,537,0);
INSERT INTO `balancescuentas` VALUES (440,540,0);
INSERT INTO `balancescuentas` VALUES (440,541,0);
INSERT INTO `balancescuentas` VALUES (440,546,0);
INSERT INTO `balancescuentas` VALUES (440,549,0);
INSERT INTO `balancescuentas` VALUES (440,542,0);
INSERT INTO `balancescuentas` VALUES (440,543,0);
INSERT INTO `balancescuentas` VALUES (440,545,0);
INSERT INTO `balancescuentas` VALUES (440,547,0);
INSERT INTO `balancescuentas` VALUES (440,548,0);
INSERT INTO `balancescuentas` VALUES (440,565,0);
INSERT INTO `balancescuentas` VALUES (440,566,0);
INSERT INTO `balancescuentas` VALUES (440,593,0);
INSERT INTO `balancescuentas` VALUES (440,594,0);
INSERT INTO `balancescuentas` VALUES (440,595,0);
INSERT INTO `balancescuentas` VALUES (440,596,0);
INSERT INTO `balancescuentas` VALUES (440,597,0);
INSERT INTO `balancescuentas` VALUES (440,598,0);
INSERT INTO `balancescuentas` VALUES (400,530,0);
INSERT INTO `balancescuentas` VALUES (400,538,0);
INSERT INTO `balancescuentas` VALUES (400,532,0);
INSERT INTO `balancescuentas` VALUES (400,534,0);
INSERT INTO `balancescuentas` VALUES (400,536,0);
INSERT INTO `balancescuentas` VALUES (400,531,0);
INSERT INTO `balancescuentas` VALUES (400,539,0);
INSERT INTO `balancescuentas` VALUES (400,533,0);
INSERT INTO `balancescuentas` VALUES (400,535,0);
INSERT INTO `balancescuentas` VALUES (400,537,0);
INSERT INTO `balancescuentas` VALUES (400,540,0);
INSERT INTO `balancescuentas` VALUES (400,541,0);
INSERT INTO `balancescuentas` VALUES (400,546,0);
INSERT INTO `balancescuentas` VALUES (400,549,0);
INSERT INTO `balancescuentas` VALUES (400,542,0);
INSERT INTO `balancescuentas` VALUES (400,543,0);
INSERT INTO `balancescuentas` VALUES (400,545,0);
INSERT INTO `balancescuentas` VALUES (400,547,0);
INSERT INTO `balancescuentas` VALUES (400,548,0);
INSERT INTO `balancescuentas` VALUES (400,565,0);
INSERT INTO `balancescuentas` VALUES (400,566,0);
INSERT INTO `balancescuentas` VALUES (400,593,0);
INSERT INTO `balancescuentas` VALUES (400,594,0);
INSERT INTO `balancescuentas` VALUES (400,595,0);
INSERT INTO `balancescuentas` VALUES (400,596,0);
INSERT INTO `balancescuentas` VALUES (400,597,0);
INSERT INTO `balancescuentas` VALUES (400,598,0);
INSERT INTO `balancescuentas` VALUES (400,57,0);
INSERT INTO `balancescuentas` VALUES (400,480,0);
INSERT INTO `balancescuentas` VALUES (400,580,0);
INSERT INTO `balancescuentas` VALUES (1140,112,2);
INSERT INTO `balancescuentas` VALUES (1140,115,2);
INSERT INTO `balancescuentas` VALUES (1140,114,2);
INSERT INTO `balancescuentas` VALUES (1140,116,2);
INSERT INTO `balancescuentas` VALUES (1140,113,2);
INSERT INTO `balancescuentas` VALUES (1140,117,2);
INSERT INTO `balancescuentas` VALUES (1140,118,2);
INSERT INTO `balancescuentas` VALUES (1150,120,2);
INSERT INTO `balancescuentas` VALUES (1150,121,2);
INSERT INTO `balancescuentas` VALUES (1150,122,2);
INSERT INTO `balancescuentas` VALUES (1100,10,2);
INSERT INTO `balancescuentas` VALUES (1100,110,2);
INSERT INTO `balancescuentas` VALUES (1100,111,2);
INSERT INTO `balancescuentas` VALUES (1100,112,2);
INSERT INTO `balancescuentas` VALUES (1100,115,2);
INSERT INTO `balancescuentas` VALUES (1100,114,2);
INSERT INTO `balancescuentas` VALUES (1100,116,2);
INSERT INTO `balancescuentas` VALUES (1100,113,2);
INSERT INTO `balancescuentas` VALUES (1100,117,2);
INSERT INTO `balancescuentas` VALUES (1100,118,2);
INSERT INTO `balancescuentas` VALUES (1100,120,2);
INSERT INTO `balancescuentas` VALUES (1100,121,2);
INSERT INTO `balancescuentas` VALUES (1100,122,2);
INSERT INTO `balancescuentas` VALUES (1100,129,2);
INSERT INTO `balancescuentas` VALUES (1100,557,2);
INSERT INTO `balancescuentas` VALUES (1200,130,2);
INSERT INTO `balancescuentas` VALUES (1200,131,2);
INSERT INTO `balancescuentas` VALUES (1200,136,2);
INSERT INTO `balancescuentas` VALUES (1200,135,2);
INSERT INTO `balancescuentas` VALUES (1300,140,2);
INSERT INTO `balancescuentas` VALUES (1300,141,2);
INSERT INTO `balancescuentas` VALUES (1300,142,2);
INSERT INTO `balancescuentas` VALUES (1300,143,2);
INSERT INTO `balancescuentas` VALUES (1300,144,2);
INSERT INTO `balancescuentas` VALUES (1410,150,2);
INSERT INTO `balancescuentas` VALUES (1410,151,2);
INSERT INTO `balancescuentas` VALUES (1410,155,2);
INSERT INTO `balancescuentas` VALUES (1430,160,2);
INSERT INTO `balancescuentas` VALUES (1430,162,2);
INSERT INTO `balancescuentas` VALUES (1430,164,2);
INSERT INTO `balancescuentas` VALUES (1430,161,2);
INSERT INTO `balancescuentas` VALUES (1430,163,2);
INSERT INTO `balancescuentas` VALUES (1430,165,2);
INSERT INTO `balancescuentas` VALUES (1440,174,2);
INSERT INTO `balancescuentas` VALUES (1440,171,2);
INSERT INTO `balancescuentas` VALUES (1440,172,2);
INSERT INTO `balancescuentas` VALUES (1440,173,2);
INSERT INTO `balancescuentas` VALUES (1440,180,2);
INSERT INTO `balancescuentas` VALUES (1440,185,2);
INSERT INTO `balancescuentas` VALUES (1450,248,2);
INSERT INTO `balancescuentas` VALUES (1450,249,2);
INSERT INTO `balancescuentas` VALUES (1450,259,2);
INSERT INTO `balancescuentas` VALUES (1400,150,2);
INSERT INTO `balancescuentas` VALUES (1400,151,2);
INSERT INTO `balancescuentas` VALUES (1400,155,2);
INSERT INTO `balancescuentas` VALUES (1400,170,2);
INSERT INTO `balancescuentas` VALUES (1400,160,2);
INSERT INTO `balancescuentas` VALUES (1400,162,2);
INSERT INTO `balancescuentas` VALUES (1400,164,2);
INSERT INTO `balancescuentas` VALUES (1400,161,2);
INSERT INTO `balancescuentas` VALUES (1400,163,2);
INSERT INTO `balancescuentas` VALUES (1400,165,2);
INSERT INTO `balancescuentas` VALUES (1400,174,2);
INSERT INTO `balancescuentas` VALUES (1400,171,2);
INSERT INTO `balancescuentas` VALUES (1400,172,2);
INSERT INTO `balancescuentas` VALUES (1400,173,2);
INSERT INTO `balancescuentas` VALUES (1400,180,2);
INSERT INTO `balancescuentas` VALUES (1400,185,2);
INSERT INTO `balancescuentas` VALUES (1400,248,2);
INSERT INTO `balancescuentas` VALUES (1400,249,2);
INSERT INTO `balancescuentas` VALUES (1400,259,0);
INSERT INTO `balancescuentas` VALUES (1510,500,2);
INSERT INTO `balancescuentas` VALUES (1510,501,2);
INSERT INTO `balancescuentas` VALUES (1510,505,2);
INSERT INTO `balancescuentas` VALUES (1510,506,2);
INSERT INTO `balancescuentas` VALUES (1520,520,2);
INSERT INTO `balancescuentas` VALUES (1520,526,2);
INSERT INTO `balancescuentas` VALUES (1530,402,2);
INSERT INTO `balancescuentas` VALUES (1530,510,2);
INSERT INTO `balancescuentas` VALUES (1530,512,2);
INSERT INTO `balancescuentas` VALUES (1530,514,2);
INSERT INTO `balancescuentas` VALUES (1530,516,2);
INSERT INTO `balancescuentas` VALUES (1530,551,3);
INSERT INTO `balancescuentas` VALUES (1530,403,2);
INSERT INTO `balancescuentas` VALUES (1530,511,2);
INSERT INTO `balancescuentas` VALUES (1530,513,2);
INSERT INTO `balancescuentas` VALUES (1530,515,2);
INSERT INTO `balancescuentas` VALUES (1530,517,2);
INSERT INTO `balancescuentas` VALUES (1530,552,3);
INSERT INTO `balancescuentas` VALUES (1540,437,2);
INSERT INTO `balancescuentas` VALUES (1540,400,2);
INSERT INTO `balancescuentas` VALUES (1540,406,2);
INSERT INTO `balancescuentas` VALUES (1540,410,2);
INSERT INTO `balancescuentas` VALUES (1540,419,2);
INSERT INTO `balancescuentas` VALUES (1540,401,2);
INSERT INTO `balancescuentas` VALUES (1540,411,2);
INSERT INTO `balancescuentas` VALUES (1550,475,2);
INSERT INTO `balancescuentas` VALUES (1550,476,2);
INSERT INTO `balancescuentas` VALUES (1550,477,2);
INSERT INTO `balancescuentas` VALUES (1550,479,2);
INSERT INTO `balancescuentas` VALUES (1550,524,2);
INSERT INTO `balancescuentas` VALUES (1550,509,2);
INSERT INTO `balancescuentas` VALUES (1550,521,2);
INSERT INTO `balancescuentas` VALUES (1550,523,2);
INSERT INTO `balancescuentas` VALUES (1550,525,2);
INSERT INTO `balancescuentas` VALUES (1550,527,2);
INSERT INTO `balancescuentas` VALUES (1550,553,3);
INSERT INTO `balancescuentas` VALUES (1550,555,2);
INSERT INTO `balancescuentas` VALUES (1550,556,2);
INSERT INTO `balancescuentas` VALUES (1550,465,2);
INSERT INTO `balancescuentas` VALUES (1550,560,2);
INSERT INTO `balancescuentas` VALUES (1550,561,2);
INSERT INTO `balancescuentas` VALUES (1500,500,2);
INSERT INTO `balancescuentas` VALUES (1500,501,2);
INSERT INTO `balancescuentas` VALUES (1500,505,2);
INSERT INTO `balancescuentas` VALUES (1500,506,2);
INSERT INTO `balancescuentas` VALUES (1500,520,2);
INSERT INTO `balancescuentas` VALUES (1500,526,2);
INSERT INTO `balancescuentas` VALUES (1500,402,2);
INSERT INTO `balancescuentas` VALUES (1500,510,2);
INSERT INTO `balancescuentas` VALUES (1500,512,2);
INSERT INTO `balancescuentas` VALUES (1500,514,2);
INSERT INTO `balancescuentas` VALUES (1500,516,2);
INSERT INTO `balancescuentas` VALUES (1500,551,3);
INSERT INTO `balancescuentas` VALUES (1500,403,2);
INSERT INTO `balancescuentas` VALUES (1500,511,2);
INSERT INTO `balancescuentas` VALUES (1500,513,2);
INSERT INTO `balancescuentas` VALUES (1500,515,2);
INSERT INTO `balancescuentas` VALUES (1500,517,2);
INSERT INTO `balancescuentas` VALUES (1500,552,3);
INSERT INTO `balancescuentas` VALUES (1500,437,2);
INSERT INTO `balancescuentas` VALUES (1500,400,2);
INSERT INTO `balancescuentas` VALUES (1500,406,2);
INSERT INTO `balancescuentas` VALUES (1500,410,2);
INSERT INTO `balancescuentas` VALUES (1500,419,2);
INSERT INTO `balancescuentas` VALUES (1500,401,2);
INSERT INTO `balancescuentas` VALUES (1500,411,2);
INSERT INTO `balancescuentas` VALUES (1500,475,2);
INSERT INTO `balancescuentas` VALUES (1500,476,2);
INSERT INTO `balancescuentas` VALUES (1500,477,2);
INSERT INTO `balancescuentas` VALUES (1500,479,2);
INSERT INTO `balancescuentas` VALUES (1500,524,2);
INSERT INTO `balancescuentas` VALUES (1500,509,2);
INSERT INTO `balancescuentas` VALUES (1500,521,2);
INSERT INTO `balancescuentas` VALUES (1500,523,2);
INSERT INTO `balancescuentas` VALUES (1500,525,2);
INSERT INTO `balancescuentas` VALUES (1500,527,2);
INSERT INTO `balancescuentas` VALUES (1500,553,3);
INSERT INTO `balancescuentas` VALUES (1500,555,2);
INSERT INTO `balancescuentas` VALUES (1500,556,2);
INSERT INTO `balancescuentas` VALUES (1500,465,2);
INSERT INTO `balancescuentas` VALUES (1500,560,2);
INSERT INTO `balancescuentas` VALUES (1500,561,2);
INSERT INTO `balancescuentas` VALUES (1500,499,2);
INSERT INTO `balancescuentas` VALUES (1500,485,2);
INSERT INTO `balancescuentas` VALUES (1500,585,2);
INSERT INTO `balancescuentas` VALUES (2020,600,0);
INSERT INTO `balancescuentas` VALUES (2020,6080,0);
INSERT INTO `balancescuentas` VALUES (2020,6090,0);
INSERT INTO `balancescuentas` VALUES (2020,610,0);
INSERT INTO `balancescuentas` VALUES (2020,601,0);
INSERT INTO `balancescuentas` VALUES (2020,602,0);
INSERT INTO `balancescuentas` VALUES (2020,6081,0);
INSERT INTO `balancescuentas` VALUES (2020,6082,0);
INSERT INTO `balancescuentas` VALUES (2020,6091,0);
INSERT INTO `balancescuentas` VALUES (2020,6092,0);
INSERT INTO `balancescuentas` VALUES (2020,611,0);
INSERT INTO `balancescuentas` VALUES (2020,612,0);
INSERT INTO `balancescuentas` VALUES (2020,607,0);
INSERT INTO `balancescuentas` VALUES (2030,640,0);
INSERT INTO `balancescuentas` VALUES (2030,641,0);
INSERT INTO `balancescuentas` VALUES (2030,642,0);
INSERT INTO `balancescuentas` VALUES (2030,643,0);
INSERT INTO `balancescuentas` VALUES (2030,649,0);
INSERT INTO `balancescuentas` VALUES (2050,693,0);
INSERT INTO `balancescuentas` VALUES (2050,793,0);
INSERT INTO `balancescuentas` VALUES (2050,650,0);
INSERT INTO `balancescuentas` VALUES (2050,694,0);
INSERT INTO `balancescuentas` VALUES (2050,794,0);
INSERT INTO `balancescuentas` VALUES (2050,695,0);
INSERT INTO `balancescuentas` VALUES (2050,795,0);
INSERT INTO `balancescuentas` VALUES (2060,62,0);
INSERT INTO `balancescuentas` VALUES (2060,631,0);
INSERT INTO `balancescuentas` VALUES (2060,634,0);
INSERT INTO `balancescuentas` VALUES (2060,636,0);
INSERT INTO `balancescuentas` VALUES (2060,639,0);
INSERT INTO `balancescuentas` VALUES (2060,651,0);
INSERT INTO `balancescuentas` VALUES (2060,659,0);
INSERT INTO `balancescuentas` VALUES (2060,690,0);
INSERT INTO `balancescuentas` VALUES (2070,6610,0);
INSERT INTO `balancescuentas` VALUES (2070,6615,0);
INSERT INTO `balancescuentas` VALUES (2070,6620,0);
INSERT INTO `balancescuentas` VALUES (2070,6630,0);
INSERT INTO `balancescuentas` VALUES (2070,6640,0);
INSERT INTO `balancescuentas` VALUES (2070,6650,0);
INSERT INTO `balancescuentas` VALUES (2070,6611,0);
INSERT INTO `balancescuentas` VALUES (2070,6616,0);
INSERT INTO `balancescuentas` VALUES (2070,6621,0);
INSERT INTO `balancescuentas` VALUES (2070,6631,0);
INSERT INTO `balancescuentas` VALUES (2070,6641,0);
INSERT INTO `balancescuentas` VALUES (2070,6651,0);
INSERT INTO `balancescuentas` VALUES (2070,6613,0);
INSERT INTO `balancescuentas` VALUES (2070,6618,0);
INSERT INTO `balancescuentas` VALUES (2070,6622,0);
INSERT INTO `balancescuentas` VALUES (2070,6623,0);
INSERT INTO `balancescuentas` VALUES (2070,6632,0);
INSERT INTO `balancescuentas` VALUES (2070,6633,0);
INSERT INTO `balancescuentas` VALUES (2070,6642,0);
INSERT INTO `balancescuentas` VALUES (2070,6653,0);
INSERT INTO `balancescuentas` VALUES (2070,669,0);
INSERT INTO `balancescuentas` VALUES (2070,666,0);
INSERT INTO `balancescuentas` VALUES (2070,667,0);
INSERT INTO `balancescuentas` VALUES (2000,71,1);
INSERT INTO `balancescuentas` VALUES (2000,600,0);
INSERT INTO `balancescuentas` VALUES (2000,6080,0);
INSERT INTO `balancescuentas` VALUES (2000,6090,0);
INSERT INTO `balancescuentas` VALUES (2000,610,0);
INSERT INTO `balancescuentas` VALUES (2000,601,0);
INSERT INTO `balancescuentas` VALUES (2000,602,0);
INSERT INTO `balancescuentas` VALUES (2000,6081,0);
INSERT INTO `balancescuentas` VALUES (2000,6082,0);
INSERT INTO `balancescuentas` VALUES (2000,6091,0);
INSERT INTO `balancescuentas` VALUES (2000,6092,0);
INSERT INTO `balancescuentas` VALUES (2000,611,0);
INSERT INTO `balancescuentas` VALUES (2000,612,0);
INSERT INTO `balancescuentas` VALUES (2000,607,0);
INSERT INTO `balancescuentas` VALUES (2000,640,0);
INSERT INTO `balancescuentas` VALUES (2000,641,0);
INSERT INTO `balancescuentas` VALUES (2000,642,0);
INSERT INTO `balancescuentas` VALUES (2000,643,0);
INSERT INTO `balancescuentas` VALUES (2000,649,0);
INSERT INTO `balancescuentas` VALUES (2000,68,0);
INSERT INTO `balancescuentas` VALUES (2000,693,0);
INSERT INTO `balancescuentas` VALUES (2000,793,0);
INSERT INTO `balancescuentas` VALUES (2000,650,0);
INSERT INTO `balancescuentas` VALUES (2000,694,0);
INSERT INTO `balancescuentas` VALUES (2000,794,0);
INSERT INTO `balancescuentas` VALUES (2000,695,0);
INSERT INTO `balancescuentas` VALUES (2000,795,0);
INSERT INTO `balancescuentas` VALUES (2000,62,0);
INSERT INTO `balancescuentas` VALUES (2000,631,0);
INSERT INTO `balancescuentas` VALUES (2000,634,0);
INSERT INTO `balancescuentas` VALUES (2000,636,0);
INSERT INTO `balancescuentas` VALUES (2000,639,0);
INSERT INTO `balancescuentas` VALUES (2000,651,0);
INSERT INTO `balancescuentas` VALUES (2000,659,0);
INSERT INTO `balancescuentas` VALUES (2000,690,0);
INSERT INTO `balancescuentas` VALUES (2000,6610,0);
INSERT INTO `balancescuentas` VALUES (2000,6615,0);
INSERT INTO `balancescuentas` VALUES (2000,6620,0);
INSERT INTO `balancescuentas` VALUES (2000,6630,0);
INSERT INTO `balancescuentas` VALUES (2000,6640,0);
INSERT INTO `balancescuentas` VALUES (2000,6650,0);
INSERT INTO `balancescuentas` VALUES (2000,6611,0);
INSERT INTO `balancescuentas` VALUES (2000,6616,0);
INSERT INTO `balancescuentas` VALUES (2000,6621,0);
INSERT INTO `balancescuentas` VALUES (2000,6631,0);
INSERT INTO `balancescuentas` VALUES (2000,6641,0);
INSERT INTO `balancescuentas` VALUES (2000,6651,0);
INSERT INTO `balancescuentas` VALUES (2000,6613,0);
INSERT INTO `balancescuentas` VALUES (2000,6618,0);
INSERT INTO `balancescuentas` VALUES (2000,6622,0);
INSERT INTO `balancescuentas` VALUES (2000,6623,0);
INSERT INTO `balancescuentas` VALUES (2000,6632,0);
INSERT INTO `balancescuentas` VALUES (2000,6633,0);
INSERT INTO `balancescuentas` VALUES (2000,6642,0);
INSERT INTO `balancescuentas` VALUES (2000,6653,0);
INSERT INTO `balancescuentas` VALUES (2000,669,0);
INSERT INTO `balancescuentas` VALUES (2000,666,0);
INSERT INTO `balancescuentas` VALUES (2000,667,0);
INSERT INTO `balancescuentas` VALUES (2000,6963,0);
INSERT INTO `balancescuentas` VALUES (2000,6965,0);
INSERT INTO `balancescuentas` VALUES (2000,6966,0);
INSERT INTO `balancescuentas` VALUES (2000,697,0);
INSERT INTO `balancescuentas` VALUES (2000,698,0);
INSERT INTO `balancescuentas` VALUES (2000,699,0);
INSERT INTO `balancescuentas` VALUES (2000,7963,0);
INSERT INTO `balancescuentas` VALUES (2000,7965,0);
INSERT INTO `balancescuentas` VALUES (2000,7966,0);
INSERT INTO `balancescuentas` VALUES (2000,797,0);
INSERT INTO `balancescuentas` VALUES (2000,798,0);
INSERT INTO `balancescuentas` VALUES (2000,799,0);
INSERT INTO `balancescuentas` VALUES (2000,668,0);
INSERT INTO `balancescuentas` VALUES (2000,691,0);
INSERT INTO `balancescuentas` VALUES (2000,692,0);
INSERT INTO `balancescuentas` VALUES (2000,6960,0);
INSERT INTO `balancescuentas` VALUES (2000,6961,0);
INSERT INTO `balancescuentas` VALUES (2000,791,0);
INSERT INTO `balancescuentas` VALUES (2000,792,0);
INSERT INTO `balancescuentas` VALUES (2000,7960,0);
INSERT INTO `balancescuentas` VALUES (2000,7961,0);
INSERT INTO `balancescuentas` VALUES (2000,670,0);
INSERT INTO `balancescuentas` VALUES (2000,671,0);
INSERT INTO `balancescuentas` VALUES (2000,672,0);
INSERT INTO `balancescuentas` VALUES (2000,673,0);
INSERT INTO `balancescuentas` VALUES (2000,674,0);
INSERT INTO `balancescuentas` VALUES (2000,678,0);
INSERT INTO `balancescuentas` VALUES (2000,679,0);
INSERT INTO `balancescuentas` VALUES (2000,630,0);
INSERT INTO `balancescuentas` VALUES (2000,633,0);
INSERT INTO `balancescuentas` VALUES (2000,638,0);
INSERT INTO `balancescuentas` VALUES (3010,700,2);
INSERT INTO `balancescuentas` VALUES (3010,701,2);
INSERT INTO `balancescuentas` VALUES (3010,702,2);
INSERT INTO `balancescuentas` VALUES (3010,703,2);
INSERT INTO `balancescuentas` VALUES (3010,704,2);
INSERT INTO `balancescuentas` VALUES (3010,705,2);
INSERT INTO `balancescuentas` VALUES (3010,708,2);
INSERT INTO `balancescuentas` VALUES (3010,709,2);
INSERT INTO `balancescuentas` VALUES (3040,75,2);
INSERT INTO `balancescuentas` VALUES (3040,74,2);
INSERT INTO `balancescuentas` VALUES (3040,790,2);
INSERT INTO `balancescuentas` VALUES (3050,7600,2);
INSERT INTO `balancescuentas` VALUES (3050,7601,2);
INSERT INTO `balancescuentas` VALUES (3050,7603,2);
INSERT INTO `balancescuentas` VALUES (3060,7610,2);
INSERT INTO `balancescuentas` VALUES (3060,7620,2);
INSERT INTO `balancescuentas` VALUES (3060,7611,2);
INSERT INTO `balancescuentas` VALUES (3060,7621,2);
INSERT INTO `balancescuentas` VALUES (3060,7613,2);
INSERT INTO `balancescuentas` VALUES (3060,7623,2);
INSERT INTO `balancescuentas` VALUES (3070,7630,2);
INSERT INTO `balancescuentas` VALUES (3070,7650,2);
INSERT INTO `balancescuentas` VALUES (3070,7631,2);
INSERT INTO `balancescuentas` VALUES (3070,7651,2);
INSERT INTO `balancescuentas` VALUES (3070,7633,2);
INSERT INTO `balancescuentas` VALUES (3070,7653,2);
INSERT INTO `balancescuentas` VALUES (3070,769,2);
INSERT INTO `balancescuentas` VALUES (3070,766,2);
INSERT INTO `balancescuentas` VALUES (3000,700,2);
INSERT INTO `balancescuentas` VALUES (3000,701,2);
INSERT INTO `balancescuentas` VALUES (3000,702,2);
INSERT INTO `balancescuentas` VALUES (3000,703,2);
INSERT INTO `balancescuentas` VALUES (3000,704,2);
INSERT INTO `balancescuentas` VALUES (3000,705,2);
INSERT INTO `balancescuentas` VALUES (3000,708,2);
INSERT INTO `balancescuentas` VALUES (3000,709,2);
INSERT INTO `balancescuentas` VALUES (3000,71,3);
INSERT INTO `balancescuentas` VALUES (3000,73,2);
INSERT INTO `balancescuentas` VALUES (3000,75,2);
INSERT INTO `balancescuentas` VALUES (3000,74,2);
INSERT INTO `balancescuentas` VALUES (3000,790,2);
INSERT INTO `balancescuentas` VALUES (3000,7600,2);
INSERT INTO `balancescuentas` VALUES (3000,7601,2);
INSERT INTO `balancescuentas` VALUES (3000,7603,2);
INSERT INTO `balancescuentas` VALUES (3000,7610,2);
INSERT INTO `balancescuentas` VALUES (3000,7620,2);
INSERT INTO `balancescuentas` VALUES (3000,7611,2);
INSERT INTO `balancescuentas` VALUES (3000,7621,2);
INSERT INTO `balancescuentas` VALUES (3000,7613,2);
INSERT INTO `balancescuentas` VALUES (3000,7623,2);
INSERT INTO `balancescuentas` VALUES (3000,7630,2);
INSERT INTO `balancescuentas` VALUES (3000,7650,2);
INSERT INTO `balancescuentas` VALUES (3000,7631,2);
INSERT INTO `balancescuentas` VALUES (3000,7651,2);
INSERT INTO `balancescuentas` VALUES (3000,7633,2);
INSERT INTO `balancescuentas` VALUES (3000,7653,2);
INSERT INTO `balancescuentas` VALUES (3000,769,2);
INSERT INTO `balancescuentas` VALUES (3000,766,2);
INSERT INTO `balancescuentas` VALUES (3000,768,2);
INSERT INTO `balancescuentas` VALUES (3000,770,2);
INSERT INTO `balancescuentas` VALUES (3000,771,2);
INSERT INTO `balancescuentas` VALUES (3000,772,2);
INSERT INTO `balancescuentas` VALUES (3000,774,2);
INSERT INTO `balancescuentas` VALUES (3000,775,2);
INSERT INTO `balancescuentas` VALUES (3000,778,2);
INSERT INTO `balancescuentas` VALUES (3000,779,2);
INSERT INTO `balancescuentas` VALUES (224,217,0);
INSERT INTO `balancescuentas` VALUES (4000,60,0);
INSERT INTO `balancescuentas` VALUES (4000,61,0);
INSERT INTO `balancescuentas` VALUES (4000,71,0);
INSERT INTO `balancescuentas` VALUES (4000,640,0);
INSERT INTO `balancescuentas` VALUES (4000,641,0);
INSERT INTO `balancescuentas` VALUES (4000,642,0);
INSERT INTO `balancescuentas` VALUES (4000,643,0);
INSERT INTO `balancescuentas` VALUES (4000,649,0);
INSERT INTO `balancescuentas` VALUES (4000,68,0);
INSERT INTO `balancescuentas` VALUES (4000,650,0);
INSERT INTO `balancescuentas` VALUES (4000,693,0);
INSERT INTO `balancescuentas` VALUES (4000,694,0);
INSERT INTO `balancescuentas` VALUES (4000,695,0);
INSERT INTO `balancescuentas` VALUES (4000,793,0);
INSERT INTO `balancescuentas` VALUES (4000,794,0);
INSERT INTO `balancescuentas` VALUES (4000,795,0);
INSERT INTO `balancescuentas` VALUES (4000,62,0);
INSERT INTO `balancescuentas` VALUES (4000,631,0);
INSERT INTO `balancescuentas` VALUES (4000,634,0);
INSERT INTO `balancescuentas` VALUES (4000,636,0);
INSERT INTO `balancescuentas` VALUES (4000,639,0);
INSERT INTO `balancescuentas` VALUES (4000,651,0);
INSERT INTO `balancescuentas` VALUES (4000,659,0);
INSERT INTO `balancescuentas` VALUES (4000,690,0);
INSERT INTO `balancescuentas` VALUES (4000,6610,0);
INSERT INTO `balancescuentas` VALUES (4000,6615,0);
INSERT INTO `balancescuentas` VALUES (4000,6620,0);
INSERT INTO `balancescuentas` VALUES (4000,6630,0);
INSERT INTO `balancescuentas` VALUES (4000,6640,0);
INSERT INTO `balancescuentas` VALUES (4000,6650,0);
INSERT INTO `balancescuentas` VALUES (4000,6611,0);
INSERT INTO `balancescuentas` VALUES (4000,6616,0);
INSERT INTO `balancescuentas` VALUES (4000,6621,0);
INSERT INTO `balancescuentas` VALUES (4000,6631,0);
INSERT INTO `balancescuentas` VALUES (4000,6641,0);
INSERT INTO `balancescuentas` VALUES (4000,6651,0);
INSERT INTO `balancescuentas` VALUES (4000,6613,0);
INSERT INTO `balancescuentas` VALUES (4000,6618,0);
INSERT INTO `balancescuentas` VALUES (4000,6622,0);
INSERT INTO `balancescuentas` VALUES (4000,6623,0);
INSERT INTO `balancescuentas` VALUES (4000,6632,0);
INSERT INTO `balancescuentas` VALUES (4000,6633,0);
INSERT INTO `balancescuentas` VALUES (4000,6642,0);
INSERT INTO `balancescuentas` VALUES (4000,6653,0);
INSERT INTO `balancescuentas` VALUES (4000,669,0);
INSERT INTO `balancescuentas` VALUES (4000,666,0);
INSERT INTO `balancescuentas` VALUES (4000,667,0);
INSERT INTO `balancescuentas` VALUES (4000,6963,0);
INSERT INTO `balancescuentas` VALUES (4000,6965,0);
INSERT INTO `balancescuentas` VALUES (4000,6966,0);
INSERT INTO `balancescuentas` VALUES (4000,697,0);
INSERT INTO `balancescuentas` VALUES (4000,698,0);
INSERT INTO `balancescuentas` VALUES (4000,699,0);
INSERT INTO `balancescuentas` VALUES (4000,7963,0);
INSERT INTO `balancescuentas` VALUES (4000,7965,0);
INSERT INTO `balancescuentas` VALUES (4000,7966,0);
INSERT INTO `balancescuentas` VALUES (4000,797,0);
INSERT INTO `balancescuentas` VALUES (4000,798,0);
INSERT INTO `balancescuentas` VALUES (4000,799,0);
INSERT INTO `balancescuentas` VALUES (4000,668,0);
INSERT INTO `balancescuentas` VALUES (4000,691,0);
INSERT INTO `balancescuentas` VALUES (4000,692,0);
INSERT INTO `balancescuentas` VALUES (4000,6960,0);
INSERT INTO `balancescuentas` VALUES (4000,6961,0);
INSERT INTO `balancescuentas` VALUES (4000,791,0);
INSERT INTO `balancescuentas` VALUES (4000,792,0);
INSERT INTO `balancescuentas` VALUES (4000,7960,0);
INSERT INTO `balancescuentas` VALUES (4000,7961,0);
INSERT INTO `balancescuentas` VALUES (4000,670,0);
INSERT INTO `balancescuentas` VALUES (4000,671,0);
INSERT INTO `balancescuentas` VALUES (4000,672,0);
INSERT INTO `balancescuentas` VALUES (4000,673,0);
INSERT INTO `balancescuentas` VALUES (4000,674,0);
INSERT INTO `balancescuentas` VALUES (4000,678,0);
INSERT INTO `balancescuentas` VALUES (4000,679,0);
INSERT INTO `balancescuentas` VALUES (4000,630,0);
INSERT INTO `balancescuentas` VALUES (4000,633,0);
INSERT INTO `balancescuentas` VALUES (4000,638,0);
INSERT INTO `balancescuentas` VALUES (4010,60,0);
INSERT INTO `balancescuentas` VALUES (4010,61,0);
INSERT INTO `balancescuentas` VALUES (4010,71,0);
INSERT INTO `balancescuentas` VALUES (4020,640,0);
INSERT INTO `balancescuentas` VALUES (4020,641,0);
INSERT INTO `balancescuentas` VALUES (4020,642,0);
INSERT INTO `balancescuentas` VALUES (4020,643,0);
INSERT INTO `balancescuentas` VALUES (4020,649,0);
INSERT INTO `balancescuentas` VALUES (4021,640,0);
INSERT INTO `balancescuentas` VALUES (4021,641,0);
INSERT INTO `balancescuentas` VALUES (4022,642,0);
INSERT INTO `balancescuentas` VALUES (4022,643,0);
INSERT INTO `balancescuentas` VALUES (4022,649,0);
INSERT INTO `balancescuentas` VALUES (4030,68,0);
INSERT INTO `balancescuentas` VALUES (4040,650,0);
INSERT INTO `balancescuentas` VALUES (4040,693,0);
INSERT INTO `balancescuentas` VALUES (4040,694,0);
INSERT INTO `balancescuentas` VALUES (4040,695,0);
INSERT INTO `balancescuentas` VALUES (4040,793,0);
INSERT INTO `balancescuentas` VALUES (4040,794,0);
INSERT INTO `balancescuentas` VALUES (4040,795,0);
INSERT INTO `balancescuentas` VALUES (4050,62,0);
INSERT INTO `balancescuentas` VALUES (4050,631,0);
INSERT INTO `balancescuentas` VALUES (4050,634,0);
INSERT INTO `balancescuentas` VALUES (4050,636,0);
INSERT INTO `balancescuentas` VALUES (4050,639,0);
INSERT INTO `balancescuentas` VALUES (4050,651,0);
INSERT INTO `balancescuentas` VALUES (4050,659,0);
INSERT INTO `balancescuentas` VALUES (4050,690,0);
INSERT INTO `balancescuentas` VALUES (4060,6610,0);
INSERT INTO `balancescuentas` VALUES (4060,6615,0);
INSERT INTO `balancescuentas` VALUES (4060,6620,0);
INSERT INTO `balancescuentas` VALUES (4060,6630,0);
INSERT INTO `balancescuentas` VALUES (4060,6640,0);
INSERT INTO `balancescuentas` VALUES (4060,6650,0);
INSERT INTO `balancescuentas` VALUES (4060,6611,0);
INSERT INTO `balancescuentas` VALUES (4060,6616,0);
INSERT INTO `balancescuentas` VALUES (4060,6621,0);
INSERT INTO `balancescuentas` VALUES (4060,6631,0);
INSERT INTO `balancescuentas` VALUES (4060,6641,0);
INSERT INTO `balancescuentas` VALUES (4060,6651,0);
INSERT INTO `balancescuentas` VALUES (4060,6613,0);
INSERT INTO `balancescuentas` VALUES (4060,6618,0);
INSERT INTO `balancescuentas` VALUES (4060,6622,0);
INSERT INTO `balancescuentas` VALUES (4060,6623,0);
INSERT INTO `balancescuentas` VALUES (4060,6632,0);
INSERT INTO `balancescuentas` VALUES (4060,6633,0);
INSERT INTO `balancescuentas` VALUES (4060,6642,0);
INSERT INTO `balancescuentas` VALUES (4060,6653,0);
INSERT INTO `balancescuentas` VALUES (4060,669,0);
INSERT INTO `balancescuentas` VALUES (4060,666,0);
INSERT INTO `balancescuentas` VALUES (4060,667,0);
INSERT INTO `balancescuentas` VALUES (4061,6610,0);
INSERT INTO `balancescuentas` VALUES (4061,6615,0);
INSERT INTO `balancescuentas` VALUES (4061,6620,0);
INSERT INTO `balancescuentas` VALUES (4061,6630,0);
INSERT INTO `balancescuentas` VALUES (4061,6640,0);
INSERT INTO `balancescuentas` VALUES (4061,6650,0);
INSERT INTO `balancescuentas` VALUES (4062,6611,0);
INSERT INTO `balancescuentas` VALUES (4062,6616,0);
INSERT INTO `balancescuentas` VALUES (4062,6621,0);
INSERT INTO `balancescuentas` VALUES (4062,6631,0);
INSERT INTO `balancescuentas` VALUES (4062,6641,0);
INSERT INTO `balancescuentas` VALUES (4062,6651,0);
INSERT INTO `balancescuentas` VALUES (4063,6613,0);
INSERT INTO `balancescuentas` VALUES (4064,6618,0);
INSERT INTO `balancescuentas` VALUES (4063,6622,0);
INSERT INTO `balancescuentas` VALUES (4063,6623,0);
INSERT INTO `balancescuentas` VALUES (4063,6632,0);
INSERT INTO `balancescuentas` VALUES (4063,6633,0);
INSERT INTO `balancescuentas` VALUES (4063,6642,0);
INSERT INTO `balancescuentas` VALUES (4063,6653,0);
INSERT INTO `balancescuentas` VALUES (4063,669,0);
INSERT INTO `balancescuentas` VALUES (4064,666,0);
INSERT INTO `balancescuentas` VALUES (4064,667,0);
INSERT INTO `balancescuentas` VALUES (4070,6963,0);
INSERT INTO `balancescuentas` VALUES (4070,6965,0);
INSERT INTO `balancescuentas` VALUES (4070,6966,0);
INSERT INTO `balancescuentas` VALUES (4070,697,0);
INSERT INTO `balancescuentas` VALUES (4070,698,0);
INSERT INTO `balancescuentas` VALUES (4070,699,0);
INSERT INTO `balancescuentas` VALUES (4070,7963,0);
INSERT INTO `balancescuentas` VALUES (4070,7965,0);
INSERT INTO `balancescuentas` VALUES (4070,7966,0);
INSERT INTO `balancescuentas` VALUES (4070,797,0);
INSERT INTO `balancescuentas` VALUES (4070,798,0);
INSERT INTO `balancescuentas` VALUES (4070,799,0);
INSERT INTO `balancescuentas` VALUES (4080,668,0);
INSERT INTO `balancescuentas` VALUES (4090,691,0);
INSERT INTO `balancescuentas` VALUES (4090,692,0);
INSERT INTO `balancescuentas` VALUES (4090,6960,0);
INSERT INTO `balancescuentas` VALUES (4090,6961,0);
INSERT INTO `balancescuentas` VALUES (4090,791,0);
INSERT INTO `balancescuentas` VALUES (4090,792,0);
INSERT INTO `balancescuentas` VALUES (4090,7960,0);
INSERT INTO `balancescuentas` VALUES (4090,7961,0);
INSERT INTO `balancescuentas` VALUES (4100,670,0);
INSERT INTO `balancescuentas` VALUES (4100,671,0);
INSERT INTO `balancescuentas` VALUES (4100,672,0);
INSERT INTO `balancescuentas` VALUES (4100,673,0);
INSERT INTO `balancescuentas` VALUES (4110,674,0);
INSERT INTO `balancescuentas` VALUES (4120,678,0);
INSERT INTO `balancescuentas` VALUES (4130,679,0);
INSERT INTO `balancescuentas` VALUES (4140,630,0);
INSERT INTO `balancescuentas` VALUES (4140,633,0);
INSERT INTO `balancescuentas` VALUES (4140,638,0);
INSERT INTO `balancescuentas` VALUES (5000,70,2);
INSERT INTO `balancescuentas` VALUES (5000,73,2);
INSERT INTO `balancescuentas` VALUES (5000,74,2);
INSERT INTO `balancescuentas` VALUES (5000,75,2);
INSERT INTO `balancescuentas` VALUES (5000,790,2);
INSERT INTO `balancescuentas` VALUES (5000,7600,2);
INSERT INTO `balancescuentas` VALUES (5000,7610,2);
INSERT INTO `balancescuentas` VALUES (5000,7620,2);
INSERT INTO `balancescuentas` VALUES (5000,7630,2);
INSERT INTO `balancescuentas` VALUES (5000,7650,2);
INSERT INTO `balancescuentas` VALUES (5000,7601,2);
INSERT INTO `balancescuentas` VALUES (5000,7611,2);
INSERT INTO `balancescuentas` VALUES (5000,7621,2);
INSERT INTO `balancescuentas` VALUES (5000,7631,2);
INSERT INTO `balancescuentas` VALUES (5000,7651,2);
INSERT INTO `balancescuentas` VALUES (5000,7603,2);
INSERT INTO `balancescuentas` VALUES (5000,7613,2);
INSERT INTO `balancescuentas` VALUES (5000,7623,2);
INSERT INTO `balancescuentas` VALUES (5000,7633,2);
INSERT INTO `balancescuentas` VALUES (5000,7653,2);
INSERT INTO `balancescuentas` VALUES (5000,769,2);
INSERT INTO `balancescuentas` VALUES (5000,766,2);
INSERT INTO `balancescuentas` VALUES (5000,768,2);
INSERT INTO `balancescuentas` VALUES (5000,770,2);
INSERT INTO `balancescuentas` VALUES (5000,771,2);
INSERT INTO `balancescuentas` VALUES (5000,772,2);
INSERT INTO `balancescuentas` VALUES (5000,774,2);
INSERT INTO `balancescuentas` VALUES (5000,775,2);
INSERT INTO `balancescuentas` VALUES (5000,778,2);
INSERT INTO `balancescuentas` VALUES (5000,779,2);
INSERT INTO `balancescuentas` VALUES (5010,70,2);
INSERT INTO `balancescuentas` VALUES (5010,73,2);
INSERT INTO `balancescuentas` VALUES (5010,74,2);
INSERT INTO `balancescuentas` VALUES (5010,75,2);
INSERT INTO `balancescuentas` VALUES (5010,790,2);
INSERT INTO `balancescuentas` VALUES (5020,7600,2);
INSERT INTO `balancescuentas` VALUES (5020,7610,2);
INSERT INTO `balancescuentas` VALUES (5020,7620,2);
INSERT INTO `balancescuentas` VALUES (5020,7630,2);
INSERT INTO `balancescuentas` VALUES (5020,7650,2);
INSERT INTO `balancescuentas` VALUES (5020,7601,2);
INSERT INTO `balancescuentas` VALUES (5020,7611,2);
INSERT INTO `balancescuentas` VALUES (5020,7621,2);
INSERT INTO `balancescuentas` VALUES (5020,7631,2);
INSERT INTO `balancescuentas` VALUES (5020,7651,2);
INSERT INTO `balancescuentas` VALUES (5020,7603,2);
INSERT INTO `balancescuentas` VALUES (5020,7613,2);
INSERT INTO `balancescuentas` VALUES (5020,7623,2);
INSERT INTO `balancescuentas` VALUES (5020,7633,2);
INSERT INTO `balancescuentas` VALUES (5020,7653,2);
INSERT INTO `balancescuentas` VALUES (5020,769,2);
INSERT INTO `balancescuentas` VALUES (5020,766,2);
INSERT INTO `balancescuentas` VALUES (5030,768,2);
INSERT INTO `balancescuentas` VALUES (5040,770,2);
INSERT INTO `balancescuentas` VALUES (5040,771,2);
INSERT INTO `balancescuentas` VALUES (5040,772,2);
INSERT INTO `balancescuentas` VALUES (5050,774,2);
INSERT INTO `balancescuentas` VALUES (5060,775,2);
INSERT INTO `balancescuentas` VALUES (5070,778,2);
INSERT INTO `balancescuentas` VALUES (5080,779,2);
INSERT INTO `balancescuentas` VALUES (5011,70,2);
INSERT INTO `balancescuentas` VALUES (5012,73,2);
INSERT INTO `balancescuentas` VALUES (5012,74,2);
INSERT INTO `balancescuentas` VALUES (5012,75,2);
INSERT INTO `balancescuentas` VALUES (5012,790,2);
INSERT INTO `balancescuentas` VALUES (5021,7600,2);
INSERT INTO `balancescuentas` VALUES (5021,7610,2);
INSERT INTO `balancescuentas` VALUES (5021,7620,2);
INSERT INTO `balancescuentas` VALUES (5021,7630,2);
INSERT INTO `balancescuentas` VALUES (5021,7650,2);
INSERT INTO `balancescuentas` VALUES (5022,7601,2);
INSERT INTO `balancescuentas` VALUES (5022,7611,2);
INSERT INTO `balancescuentas` VALUES (5022,7621,2);
INSERT INTO `balancescuentas` VALUES (5022,7631,2);
INSERT INTO `balancescuentas` VALUES (5022,7651,2);
INSERT INTO `balancescuentas` VALUES (5023,7603,2);
INSERT INTO `balancescuentas` VALUES (5023,7613,2);
INSERT INTO `balancescuentas` VALUES (5023,7623,2);
INSERT INTO `balancescuentas` VALUES (5023,7633,2);
INSERT INTO `balancescuentas` VALUES (5023,7653,2);
INSERT INTO `balancescuentas` VALUES (5023,769,2);
INSERT INTO `balancescuentas` VALUES (5024,766,2);
INSERT INTO `balancescuentas` VALUES (220,217,0);
INSERT INTO `balancescuentas` VALUES (200,217,0);
INSERT INTO `balancescuentas` VALUES (436,473,0);
INSERT INTO `balancescuentas` VALUES (434,550,1);
INSERT INTO `balancescuentas` VALUES (1553,550,3);
INSERT INTO `balancescuentas` VALUES (400,550,1);
INSERT INTO `balancescuentas` VALUES (1500,550,3);
INSERT INTO `balancescuentas` VALUES (1550,550,3);
INSERT INTO `balancescuentas` VALUES (430,550,1);
INSERT INTO `balancescuentas` VALUES (430,473,0);
INSERT INTO `balancescuentas` VALUES (400,473,0);




-- ================================================================
COMMIT;
SET FOREIGN_KEY_CHECKS=1;
-- FIN DEL SCRIPT
-- ================================================================
