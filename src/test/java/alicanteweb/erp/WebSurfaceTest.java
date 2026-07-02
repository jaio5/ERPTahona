package alicanteweb.erp;

import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.web.bind.annotation.RestController;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class WebSurfaceTest {

    @Test
    void laAplicacionExponeControladoresRestPropios() {
        ClassPathScanningCandidateComponentProvider scanner =
            new ClassPathScanningCandidateComponentProvider(false);
        scanner.addIncludeFilter(new AnnotationTypeFilter(RestController.class));

        var candidates = scanner.findCandidateComponents("alicanteweb.erp");
        assertFalse(candidates.isEmpty(),
            "La aplicacion web debe exponer @RestController propios");
        assertTrue(candidates.stream().anyMatch(candidate ->
                candidate.getBeanClassName() != null
                        && candidate.getBeanClassName().endsWith("WebApiController")),
            "Debe existir la API web principal");
    }

    @Test
    void existenRecursosEstaticosDelFrontendWeb() {
        assertTrue(Files.exists(Path.of("src/main/resources/static/index.html")), "Falta index.html");
        assertTrue(Files.exists(Path.of("src/main/resources/static/css/app.css")), "Falta css/app.css");
        assertTrue(Files.exists(Path.of("src/main/resources/static/js/app.js")), "Falta js/app.js");
    }
}
