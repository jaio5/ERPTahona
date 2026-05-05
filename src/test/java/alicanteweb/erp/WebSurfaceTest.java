package alicanteweb.erp;

import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.web.bind.annotation.RestController;

import static org.junit.jupiter.api.Assertions.assertTrue;

class WebSurfaceTest {

    @Test
    void laAplicacionNoExponeControladoresRestPropios() {
        ClassPathScanningCandidateComponentProvider scanner =
            new ClassPathScanningCandidateComponentProvider(false);
        scanner.addIncludeFilter(new AnnotationTypeFilter(RestController.class));

        var candidates = scanner.findCandidateComponents("alicanteweb.erp");
        assertTrue(candidates.isEmpty(),
            "La aplicacion es de escritorio y no deberia exponer @RestController propios: " + candidates);
    }
}
