// java
package alicanteweb.erp.controller.ui;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

@Component
public class MainPanelController implements Initializable {

    @FXML
    private BorderPane root;

    @FXML
    private StackPane contentPane;

    @FXML
    private Label titleLabel;

    private final ApplicationContext applicationContext;

    // Inyección por constructor: no se usa @Autowired
    public MainPanelController(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        loadView("/ui/factura_panel.fxml");
    }

    @FXML
    private void onNavFactura(ActionEvent event) {
        loadView("/ui/factura_panel.fxml");
    }

    @FXML
    private void onNavClientes(ActionEvent event) {
        loadView("/ui/cliente_panel.fxml");
    }

    @FXML
    private void onNavArticulos(ActionEvent event) {
        loadView("/ui/articulo_panel.fxml");
    }

    @FXML
    private void onExit(ActionEvent event) {
        javafx.application.Platform.exit();
    }

    @FXML
    private void onAbout(ActionEvent event) {
        System.out.println("ERP - Acerca de");
    }

    public void loadView(String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            if (applicationContext != null) {
                loader.setControllerFactory(applicationContext::getBean);
            }
            Node node = loader.load();
            contentPane.getChildren().setAll(node);

            Object ctrl = loader.getController();
            if (ctrl != null) {
                try {
                    var m = ctrl.getClass().getMethod("getTitle");
                    Object t = m.invoke(ctrl);
                    if (t != null) titleLabel.setText(t.toString());
                } catch (NoSuchMethodException ignored) {
                }
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
