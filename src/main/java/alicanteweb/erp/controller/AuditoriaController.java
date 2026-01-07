package alicanteweb.erp.controller;

import alicanteweb.erp.entities.AuditoriaAccion;
import alicanteweb.erp.service.AuditoriaService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
public class AuditoriaController {

    @FXML private TableView<AuditoriaAccion> tableAuditoria;
    @FXML private TableColumn<AuditoriaAccion, Long> colId;
    @FXML private TableColumn<AuditoriaAccion, String> colFecha;
    @FXML private TableColumn<AuditoriaAccion, String> colUsuario;
    @FXML private TableColumn<AuditoriaAccion, String> colTipoAccion;
    @FXML private TableColumn<AuditoriaAccion, String> colEntidad;
    @FXML private TableColumn<AuditoriaAccion, String> colResultado;
    @FXML private TextField txtBuscar;

    private final AuditoriaService auditoriaService;
    private ObservableList<AuditoriaAccion> auditorias;

    public AuditoriaController(AuditoriaService auditoriaService) {
        this.auditoriaService = auditoriaService;
    }

    @FXML
    public void initialize() {
        log.info("Inicializando AuditoriaController");

        // Configurar columnas
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colFecha.setCellValueFactory(new PropertyValueFactory<>("fecha"));
        colUsuario.setCellValueFactory(new PropertyValueFactory<>("usuarioNombre"));
        colTipoAccion.setCellValueFactory(new PropertyValueFactory<>("tipoAccion"));
        colEntidad.setCellValueFactory(new PropertyValueFactory<>("entidadTipo"));
        colResultado.setCellValueFactory(new PropertyValueFactory<>("resultado"));

        tableAuditoria.setStyle("-fx-background-color: #2b2b2b;");

        cargarAuditorias();
    }

    private void cargarAuditorias() {
        try {
            List<AuditoriaAccion> lista = new java.util.ArrayList<>();
            // TODO: Cargar desde auditoriaService cuando tenga método findAll()
            auditorias = FXCollections.observableArrayList(lista);
            tableAuditoria.setItems(auditorias);
            log.info("Registros de auditoría cargados: {}", auditorias.size());
        } catch (Exception e) {
            log.error("Error cargando auditoría", e);
            mostrarError("Error", "No se pudo cargar la auditoría: " + e.getMessage());
        }
    }

    @FXML
    private void onDetalles() {
        AuditoriaAccion seleccionado = tableAuditoria.getSelectionModel().getSelectedItem();
        if (seleccionado == null) {
            mostrarAdvertencia("Selección requerida", "Por favor, selecciona un registro para ver detalles.");
            return;
        }
        log.info("Mostrando detalles de auditoría: {}", seleccionado.getId());
        mostrarInfo("Detalles", "Registro de auditoría:\n" +
                "Fecha: " + seleccionado.getFecha() + "\n" +
                "Usuario: " + seleccionado.getUsuarioNombre() + "\n" +
                "Acción: " + seleccionado.getTipoAccion() + "\n" +
                "Resultado: " + seleccionado.getResultado());
    }

    @FXML
    private void onRefresh() {
        log.info("Refrescando auditoría");
        cargarAuditorias();
    }

    private void mostrarError(String titulo, String mensaje) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }

    private void mostrarAdvertencia(String titulo, String mensaje) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }

    private void mostrarInfo(String titulo, String mensaje) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
}

