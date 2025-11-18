package alicanteweb.erp.javafx;

import alicanteweb.erp.javafx.model.Articulo;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;

public class ArticuloDetailView {
    public static void show(Articulo a) {
        Stage stage = new Stage();
        GridPane grid = new GridPane();
        grid.setHgap(8);
        grid.setVgap(8);
        grid.setPadding(new Insets(10));

        Label codigoL = new Label("Código");
        TextField codigo = new TextField(a.getCodigoArticulo());
        codigo.setEditable(false);
        Label descL = new Label("Descripción");
        TextField desc = new TextField(a.getDescripcionArticulo());
        Label pvpL = new Label("PVP");
        TextField pvp = new TextField(a.getPvp1() != null ? a.getPvp1().toString() : "");

        grid.addRow(0, codigoL, codigo);
        grid.addRow(1, descL, desc);
        grid.addRow(2, pvpL, pvp);

        Button save = new Button("Guardar");
        save.setOnAction(e -> {
            a.setDescripcionArticulo(desc.getText());
            try {
                String code = HttpUtil.putJson("http://localhost:8080/api/articulos/" + a.getCodigoArticulo(), a);
                if (!code.startsWith("2")) {
                    Alert aerr = new Alert(Alert.AlertType.ERROR, "Error al guardar: código HTTP " + code);
                    aerr.showAndWait();
                } else {
                    Alert aok = new Alert(Alert.AlertType.INFORMATION, "Guardado OK (HTTP " + code + ")");
                    aok.showAndWait();
                }
            } catch (Exception ex) {
                Alert aerr = new Alert(Alert.AlertType.ERROR, "No se pudo conectar con el servidor: " + ex.getMessage());
                aerr.showAndWait();
            }
        });

        Button close = new Button("Cerrar");
        close.setOnAction(e -> stage.close());

        grid.addRow(3, save, close);
        stage.setScene(new Scene(grid, 420, 220));
        stage.setTitle("Artículo - " + a.getCodigoArticulo());
        stage.show();
    }
}

