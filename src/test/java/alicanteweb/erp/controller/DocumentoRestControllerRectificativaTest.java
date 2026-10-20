package alicanteweb.erp.controller;

import alicanteweb.erp.controller.rest.DocumentoRestController;
import org.junit.jupiter.api.Test;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Contención VERI*FACTU F0: el endpoint POST /facturas/{id}/anular-rectificativa anulaba cualquier
 * factura (también emitidas) y generaba un registro de anulación. Se ha retirado; las facturas
 * emitidas solo se corrigen con una rectificativa, que no anula la original.
 */
class DocumentoRestControllerRectificativaTest {

    private boolean expone(String ruta) {
        return Arrays.stream(DocumentoRestController.class.getDeclaredMethods())
                .map(m -> m.getAnnotation(PostMapping.class))
                .filter(a -> a != null)
                .flatMap(a -> Arrays.stream(a.value()))
                .anyMatch(ruta::equals);
    }

    @Test
    void noExponeLaAnulacionPorRectificativa() {
        assertFalse(expone("/facturas/{id}/anular-rectificativa"));
    }

    @Test
    void laAnulacionPreviaALaEmisionSigueDisponible() {
        // Control: la anulación directa (solo BORRADOR/REVISION, la valida FacturaService) se mantiene
        assertTrue(expone("/facturas/{id}/anular"));
    }
}
