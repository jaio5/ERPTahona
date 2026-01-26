# Review: dashboard.fxml
Path: src/main/resources/ui/dashboard.fxml
fx:controller: alicanteweb.erp.controller.ui.DashboardController

## fx:ids
lblVentasDia
lblFacturasHoy
lblVentasMes
lblFacturasMes
lblClientes
lblArticulos
tableUltimasFacturas
colNumero
colFecha
colCliente
colTotal
colEstado

## handlers
#onRefresh
#onNuevaFactura
#onNuevoAlbaran
#onNuevoCliente
#onNuevoArticulo
#onProveedores
#onCaja
#onContabilidad
#onConfiguracion

## Notes
- Initial scan: verify that `alicanteweb.erp.controller.ui.DashboardController` exists and contains @FXML fields for the fx:ids and methods for handlers with correct signatures (e.g., `@FXML public void onRefresh(ActionEvent e)` or no-arg methods depending on usage).
