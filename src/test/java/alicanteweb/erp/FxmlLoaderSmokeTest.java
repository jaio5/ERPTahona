package alicanteweb.erp;

import alicanteweb.erp.controller.formcontroller.PresupuestoFormController;
import alicanteweb.erp.service.ArticuloService;
import alicanteweb.erp.service.ClienteService;
import alicanteweb.erp.service.PresupuestoService;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class FxmlLoaderSmokeTest {

    @BeforeAll
    static void inicializarJavaFx() {
        try {
            Platform.startup(() -> {
            });
        } catch (IllegalStateException ignored) {
            // Toolkit already initialized by another test.
        }
    }

    @Test
    public void loadPresupuestoForm() throws Exception {
        try (InputStream is = getClass().getResourceAsStream("/ui/presupuesto_form.fxml")) {
            if (is == null) throw new RuntimeException("FXML not found");
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(getClass().getResource("/ui/presupuesto_form.fxml"));
            loader.setControllerFactory(type -> {
                if (type == PresupuestoFormController.class) {
                    PresupuestoService presupuestoService = mock(PresupuestoService.class);
                    ClienteService clienteService = mock(ClienteService.class);
                    ArticuloService articuloService = mock(ArticuloService.class);
                    when(clienteService.findAll()).thenReturn(List.of());
                    when(articuloService.findAll()).thenReturn(List.of());
                    return new PresupuestoFormController(presupuestoService, clienteService, articuloService);
                }
                try {
                    return type.getDeclaredConstructor().newInstance();
                } catch (ReflectiveOperationException ex) {
                    throw new IllegalStateException("No se pudo instanciar controlador FXML: " + type.getName(), ex);
                }
            });
            loader.load(is);
        }
    }
}
