# Review: articulos_panel.fxml
Path: src/main/resources/ui/articulos_panel.fxml
fx:controller: alicanteweb.erp.controller.ArticuloController

## fx:ids
txtBuscar
cmbCategoria
cmbActivo
tableArticulos
colCodigo
colNombre
colPrecio
colIVA
colStock
colActivo
lblTotal

## handlers
#onRefresh
#onNuevo
#onBuscar
#onVer
#onEditar
#onDarBaja

## Notes
- Initial scan: verify `ArticuloController` exists and contains @FXML fields for the ids above and methods for handlers. Prefer constructor injection for services used by the controller.
