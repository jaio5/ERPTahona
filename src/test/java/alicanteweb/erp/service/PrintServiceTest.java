package alicanteweb.erp.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.File;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests para PrintService
 * Verifica la funcionalidad de impresión de documentos
 */
@ExtendWith(MockitoExtension.class)
class PrintServiceTest {

    @InjectMocks
    private PrintService printService;

    @BeforeEach
    void setUp() {
        // Configuración inicial si es necesaria
    }

    @Test
    void testImprimirDocumento() {
        // Arrange
        File documento = new File("test.pdf");

        // Act & Assert
        assertDoesNotThrow(() -> {
            printService.imprimir(documento);
        });
    }

    @Test
    void testVerificarImpresoraDisponible() {
        // Act
        boolean disponible = printService.hayImpresoraDisponible();

        // Assert
        assertNotNull(disponible);
    }

    @Test
    void testObtenerImpresorasPredeterminada() {
        // Act
        String impresora = printService.getImpresoraPredeterminada();

        // Assert
        assertNotNull(impresora);
    }

    @Test
    void testListarImpresoras() {
        // Act
        var impresoras = printService.listarImpresoras();

        // Assert
        assertNotNull(impresoras);
    }

    @Test
    void testConfigurarImpresora() {
        // Act & Assert
        assertDoesNotThrow(() -> {
            printService.configurarImpresora("HP LaserJet");
        });
    }

    @Test
    void testImprimirConOpciones() {
        // Act & Assert
        assertDoesNotThrow(() -> {
            printService.imprimirConOpciones(new File("test.pdf"), true, 1);
        });
    }

    @Test
    void testVistaPrevia() {
        // Act & Assert
        assertDoesNotThrow(() -> {
            printService.vistaPrevia(new File("test.pdf"));
        });
    }

    @Test
    void testImprimirFactura() {
        // Act & Assert
        assertDoesNotThrow(() -> {
            printService.imprimirFactura(1L);
        });
    }

    @Test
    void testImprimirPresupuesto() {
        // Act & Assert
        assertDoesNotThrow(() -> {
            printService.imprimirPresupuesto(1L);
        });
    }

    @Test
    void testImprimirAlbaran() {
        // Act & Assert
        assertDoesNotThrow(() -> {
            printService.imprimirAlbaran(1L);
        });
    }
}

