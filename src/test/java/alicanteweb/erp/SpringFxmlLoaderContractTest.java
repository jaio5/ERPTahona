package alicanteweb.erp;

import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

class SpringFxmlLoaderContractTest {

    @Test
    void losCargadoresFxmlUsanControllerFactoryDeSpring() throws Exception {
        List<Path> javaFiles;
        try (var stream = Files.walk(Path.of("src/main/java"))) {
            javaFiles = stream
                .filter(path -> path.toString().endsWith(".java"))
                .toList();
        }

        List<String> errores = new ArrayList<>();
        for (Path javaFile : javaFiles) {
            String content = Files.readString(javaFile);
            if (content.contains("new FXMLLoader(") || content.contains("new javafx.fxml.FXMLLoader(")) {
                if (!content.contains("setControllerFactory(")) {
                    errores.add("FXMLLoader sin controllerFactory en: " + javaFile);
                }
            }
        }

        assertTrue(errores.isEmpty(), String.join(System.lineSeparator(), errores));
    }
}
