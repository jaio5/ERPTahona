package alicanteweb.erp.controller.ui;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import org.springframework.stereotype.Controller;
import javafx.scene.input.MouseEvent;
import javafx.animation.ScaleTransition;
import javafx.util.Duration;

@Controller
public class MainPanelController {
    @FXML private Button btnClientes;
    @FXML private Button btnProveedores;
    @FXML private Button btnArticulos;
    @FXML private Button btnFacturas;
    @FXML private Button btnAlmacenes;
    @FXML private StackPane mainContent;
    @FXML private GridPane gridModulos;

    @FXML
    public void initialize() {
        btnClientes.setText("Clientes");
        btnProveedores.setText("Proveedores");
        btnArticulos.setText("Artículos");
        btnFacturas.setText("Facturas");
        btnAlmacenes.setText("Almacenes");
        addButtonAnimation(btnClientes, "/ui/clientes_panel.fxml");
        addButtonAnimation(btnProveedores, "/ui/proveedores_panel.fxml");
        addButtonAnimation(btnArticulos, "/ui/articulos_panel.fxml");
        addButtonAnimation(btnFacturas, "/ui/facturas_panel.fxml");
        addButtonAnimation(btnAlmacenes, "/ui/almacenes_panel.fxml");
        mostrarMenuPrincipal();
    }

    private void addButtonAnimation(Button button, String fxmlPath) {
        button.addEventHandler(MouseEvent.MOUSE_ENTERED, e -> {
            ScaleTransition st = new ScaleTransition(Duration.millis(150), button);
            st.setToX(1.08);
            st.setToY(1.08);
            st.play();
        });
        button.addEventHandler(MouseEvent.MOUSE_EXITED, e -> {
            ScaleTransition st = new ScaleTransition(Duration.millis(150), button);
            st.setToX(1.0);
            st.setToY(1.0);
            st.play();
        });
        button.setOnAction(e -> cargarVistaModulo(fxmlPath));
    }

    private void cargarVistaModulo(String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent view = loader.load();
            mainContent.getChildren().setAll(view);
        } catch (Exception e) {
            System.err.println("Error cargando la vista: " + fxmlPath + " - " + e.getMessage());
        }
    }

    private void mostrarMenuPrincipal() {
        mainContent.getChildren().setAll(gridModulos);
    }

    @FXML
    public void onClientes() {
        mostrarMenuPrincipal();
    }

    @FXML
    public void onProveedores() {
        mostrarMenuPrincipal();
    }

    @FXML
    public void onArticulos() {
        mostrarMenuPrincipal();
    }

    @FXML
    public void onFacturas() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ui/facturas_panel.fxml"));
            Parent facturasView = loader.load();
            mainContent.getChildren().setAll(facturasView);
        } catch (Exception e) {
            // Mejor logging
            System.err.println("Error cargando la vista de facturas: " + e.getMessage());
        }
    }

    @FXML
    public void onAlmacenes() {
        mostrarMenuPrincipal();
    }
}
