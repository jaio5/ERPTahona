package alicanteweb.erp.javafx;

import alicanteweb.erp.javafx.model.Articulo;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

public class ArticuloView {
    public static void show() {
        Stage stage = new Stage();
        BorderPane root = new BorderPane();
        TableView<Articulo> table = new TableView<>();
        TableColumn<Articulo, String> codigoCol = new TableColumn<>("Codigo");
        codigoCol.setCellValueFactory(new PropertyValueFactory<>("codigoArticulo"));
        TableColumn<Articulo, String> descCol = new TableColumn<>("Descripcion");
        descCol.setCellValueFactory(new PropertyValueFactory<>("descripcionArticulo"));
        TableColumn<Articulo, String> pvpCol = new TableColumn<>("PVP");
        pvpCol.setCellValueFactory(new PropertyValueFactory<>("pvp1"));
        table.getColumns().addAll(codigoCol, descCol, pvpCol);

        ObservableList<Articulo> data = FXCollections.observableArrayList();
        try {
            URL url = new URL("http://localhost:8080/api/articulos?limit=200");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Accept", "application/json");
            InputStream in = conn.getInputStream();
            ObjectMapper mapper = new ObjectMapper();
            List<Articulo> list = mapper.readValue(in, new TypeReference<List<Articulo>>(){});
            data.addAll(list);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        table.setItems(data);

        table.setOnMouseClicked(e -> {
            if (e.getClickCount() == 2 && e.getButton() == MouseButton.PRIMARY) {
                Articulo sel = table.getSelectionModel().getSelectedItem();
                if (sel != null) {
                    ArticuloDetailView.show(sel);
                }
            }
        });

        Button nuevo = new Button("Nuevo");
        nuevo.setOnAction(e -> {
            Articulo a = new Articulo();
            a.setCodigoArticulo("");
            ArticuloDetailView.show(a);
        });

        HBox toolbar = new HBox(8, nuevo);
        toolbar.setPadding(new javafx.geometry.Insets(8));

        VBox content = new VBox(toolbar, table);
        root.setCenter(content);

        stage.setScene(new Scene(root, 800, 600));
        stage.setTitle("Artículos");
        stage.show();
    }
}
