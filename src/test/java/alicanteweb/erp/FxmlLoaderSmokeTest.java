package alicanteweb.erp;

import javafx.fxml.FXMLLoader;
import org.junit.jupiter.api.Test;

import java.io.InputStream;

public class FxmlLoaderSmokeTest {

    @Test
    public void loadPresupuestoForm() throws Exception {
        try (InputStream is = getClass().getResourceAsStream("/ui/presupuesto_form.fxml")) {
            if (is == null) throw new RuntimeException("FXML not found");
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(getClass().getResource("/ui/presupuesto_form.fxml"));
            loader.load(is);
        }
    }
}
