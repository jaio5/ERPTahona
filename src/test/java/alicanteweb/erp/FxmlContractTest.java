package alicanteweb.erp;

import org.junit.jupiter.api.Test;
import org.springframework.stereotype.Controller;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilderFactory;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assumptions.assumeTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FxmlContractTest {

    private static final Path UI_DIR = Path.of("src/main/resources/ui");

    @Test
    void todosLosFxmlTienenControladorValidoYHandlersDeclarados() throws Exception {
        assumeTrue(Boolean.getBoolean("erp.test.javafx-contract"),
            "Contrato FXML omitido: el arranque principal de la aplicacion es web");

        assertTrue(Files.isDirectory(UI_DIR), "No existe el directorio de FXML: " + UI_DIR);

        List<Path> fxmlFiles;
        try (var stream = Files.list(UI_DIR)) {
            fxmlFiles = stream
                .filter(path -> path.getFileName().toString().endsWith(".fxml"))
                .sorted()
                .toList();
        }

        assertFalse(fxmlFiles.isEmpty(), "No se encontraron archivos FXML");

        List<String> errores = new ArrayList<>();
        for (Path fxml : fxmlFiles) {
            validarFxml(fxml, errores);
        }

        assertTrue(errores.isEmpty(), String.join(System.lineSeparator(), errores));
    }

    private void validarFxml(Path fxml, List<String> errores) throws Exception {
        Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(fxml.toFile());
        Element root = document.getDocumentElement();

        String controllerClassName = root.getAttribute("fx:controller");
        if (controllerClassName == null || controllerClassName.isBlank()) {
            errores.add("FXML sin fx:controller: " + fxml.getFileName());
            return;
        }

        Class<?> controllerClass;
        try {
            controllerClass = Class.forName(controllerClassName);
        } catch (ClassNotFoundException ex) {
            errores.add("Controlador no encontrado en " + fxml.getFileName() + ": " + controllerClassName);
            return;
        }

        assertNotNull(controllerClass.getAnnotation(Controller.class),
            "El controlador de " + fxml.getFileName() + " no esta anotado con @Controller: " + controllerClassName);

        List<String> handlers = new ArrayList<>();
        collectEventHandlers(root, handlers);
        for (String handler : handlers) {
            if (!declaraMetodo(controllerClass, handler)) {
                errores.add("Handler no encontrado en " + fxml.getFileName() + ": " + controllerClassName + "#" + handler);
            }
        }
    }

    private void collectEventHandlers(Node node, List<String> handlers) {
        if (node == null) {
            return;
        }

        NamedNodeMap attributes = node.getAttributes();
        if (attributes != null) {
            for (int i = 0; i < attributes.getLength(); i++) {
                Node attr = attributes.item(i);
                String value = attr.getNodeValue();
                if (value != null && value.startsWith("#")) {
                    handlers.add(value.substring(1));
                }
            }
        }

        NodeList children = node.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            collectEventHandlers(children.item(i), handlers);
        }
    }

    private boolean declaraMetodo(Class<?> controllerClass, String methodName) {
        Class<?> current = controllerClass;
        while (current != null) {
            for (Method method : current.getDeclaredMethods()) {
                if (method.getName().equals(methodName)) {
                    return true;
                }
            }
            current = current.getSuperclass();
        }
        return false;
    }
}
