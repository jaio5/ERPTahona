package alicanteweb.erp.javafx;

import alicanteweb.erp.javafx.model.Cliente;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;

public class ClienteDetailView {
    public static void show(Cliente c) {
        Stage stage = new Stage();
        GridPane grid = new GridPane();
        grid.setHgap(8);
        grid.setVgap(8);
        grid.setPadding(new Insets(10));

        Label codigoL = new Label("Código");
        TextField codigo = new TextField(c.getCodigo());
        codigo.setEditable(false);
        Label nombreL = new Label("Nombre");
        TextField nombre = new TextField(c.getNombre());
        Label domicilioL = new Label("Domicilio");
        TextField domicilio = new TextField(c.getDomicilio());

        grid.addRow(0, codigoL, codigo);
        grid.addRow(1, nombreL, nombre);
        grid.addRow(2, domicilioL, domicilio);

        Button save = new Button("Guardar");
        save.setOnAction(e -> {
            c.setNombre(nombre.getText());
            c.setDomicilio(domicilio.getText());
            try {
                String code = HttpUtil.putJson("http://localhost:8080/api/clientes/" + c.getCodigo(), c);
                if (!code.startsWith("2")) {
                    Alert a = new Alert(Alert.AlertType.ERROR, "Error al guardar: código HTTP " + code);
                    a.showAndWait();
                } else {
                    Alert a = new Alert(Alert.AlertType.INFORMATION, "Guardado OK (HTTP " + code + ")");
                    a.showAndWait();
                }
            } catch (Exception ex) {
                Alert a = new Alert(Alert.AlertType.ERROR, "No se pudo conectar con el servidor: " + ex.getMessage());
                a.showAndWait();
            }
        });

        Button close = new Button("Cerrar");
        close.setOnAction(e -> stage.close());

        grid.addRow(3, save, close);
        stage.setScene(new Scene(grid, 420, 220));
        stage.setTitle("Cliente - " + c.getCodigo());
        stage.show();
    }
}
