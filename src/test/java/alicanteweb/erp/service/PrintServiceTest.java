package alicanteweb.erp.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.File;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests para PrintService
 * Verifica la funcionalidad de impresion de documentos
 */
@ExtendWith(MockitoExtension.class)
class PrintServiceTest {

    private PrintService printService;

    @BeforeEach
    void setUp() {
        printService = new PrintService();
    }

    @Test
    void testHayImpresoraDisponible() {
        // Act - Este test verifica que el metodo no lance excepcion
        boolean disponible = printService.hayImpresoraDisponible();

        // Assert - El resultado depende del sistema, solo verificamos que no sea null
        assertNotNull(disponible);
    }

    @Test
    void testGetImpresoraPredeterminada() {
        // Act
        String impresora = printService.getImpresoraPredeterminada();

        // Assert - Siempre debe retornar algo (nombre o mensaje)
        assertNotNull(impresora);
        assertFalse(impresora.isEmpty());
    }

    @Test
    void testListarImpresoras() {
        // Act
        List<String> impresoras = printService.listarImpresoras();

        // Assert - Debe retornar una lista (puede estar vacia)
        assertNotNull(impresoras);
    }

    @Test
    void testImprimirDocumentoNull() {
        // Act & Assert - No debe lanzar excepcion con null
        assertDoesNotThrow(() -> printService.imprimir(null));
    }

    @Test
    void testImprimirDocumentoInexistente() {
        // Arrange
        File archivoInexistente = new File("archivo_que_no_existe.pdf");

        // Act & Assert - No debe lanzar excepcion
        assertDoesNotThrow(() -> printService.imprimir(archivoInexistente));
    }

    @Test
    void testImprimirEnImpresoraEspecificaNull() {
        // Act & Assert - No debe lanzar excepcion
        assertDoesNotThrow(() -> printService.imprimirEn(null, "Impresora"));
    }

    @Test
    void testImprimirCopiasDocumentoNull() {
        // Act & Assert - No debe lanzar excepcion
        assertDoesNotThrow(() -> printService.imprimirCopias(null, 1));
    }
}

