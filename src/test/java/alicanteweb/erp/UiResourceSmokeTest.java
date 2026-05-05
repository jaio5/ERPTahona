package alicanteweb.erp;

import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

class UiResourceSmokeTest {

    @Test
    void existenLasVistasPrincipalesDelPanel() {
        List<String> requiredViews = List.of(
            "dashboard.fxml",
            "clientes_panel.fxml",
            "proveedores_panel.fxml",
            "articulos_panel.fxml",
            "albaranes_panel.fxml",
            "facturas_panel.fxml",
            "almacenes_panel.fxml",
            "verifactu_panel.fxml",
            "facturas_compra_panel.fxml",
            "pedidos_compra_panel.fxml",
            "pedidos_venta_panel.fxml",
            "presupuestos_panel.fxml",
            "usuarios_panel.fxml",
            "auditoria_panel.fxml",
            "backup_panel.fxml",
            "asientos_panel.fxml",
            "plan_contable_panel.fxml",
            "modelo347_panel.fxml",
            "caja_panel.fxml",
            "movimientos_banco_panel.fxml",
            "empresa_config_panel.fxml"
        );

        for (String view : requiredViews) {
            assertTrue(Files.exists(Path.of("src/main/resources/ui", view)), "Falta la vista: " + view);
        }
    }
}
