package alicanteweb.erp.javafx;

import alicanteweb.erp.javafx.model.Cliente;
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

public class ClienteView {
    public static void show() {
        Stage stage = new Stage();
        BorderPane root = new BorderPane();
        TableView<Cliente> table = new TableView<>();
        TableColumn<Cliente, String> codigoCol = new TableColumn<>("Codigo");
        codigoCol.setCellValueFactory(new PropertyValueFactory<>("codigo"));
        TableColumn<Cliente, String> nombreCol = new TableColumn<>("Nombre");
        nombreCol.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        table.getColumns().addAll(codigoCol, nombreCol);

        ObservableList<Cliente> data = FXCollections.observableArrayList();
        try {
            URL url = new URL("http://localhost:8080/api/clientes?limit=200");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Accept", "application/json");
            InputStream in = conn.getInputStream();
            ObjectMapper mapper = new ObjectMapper();
            List<Cliente> list = mapper.readValue(in, new TypeReference<List<Cliente>>(){});
            data.addAll(list);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        table.setItems(data);

        // doble click para abrir detalle
        table.setOnMouseClicked(e -> {
            if (e.getClickCount() == 2 && e.getButton() == MouseButton.PRIMARY) {
                Cliente sel = table.getSelectionModel().getSelectedItem();
                if (sel != null) {
                    ClienteDetailView.show(sel);
                }
            }
        });

        Button nuevo = new Button("Nuevo");
        nuevo.setOnAction(e -> {
            Cliente c = new Cliente();
            c.setCodigo("");
            ClienteDetailView.show(c);
        });

        HBox toolbar = new HBox(8, nuevo);
        toolbar.setPadding(new javafx.geometry.Insets(8));

        VBox content = new VBox(toolbar, table);
        root.setCenter(content);

        stage.setScene(new Scene(root, 800, 600));
        stage.setTitle("Clientes");
        stage.show();
    }
}
