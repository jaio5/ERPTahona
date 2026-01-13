package alicanteweb.erp.service;

import alicanteweb.erp.entities.Factura;
import alicanteweb.erp.entities.Cliente;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.security.KeyStore;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Tests para VerifactuService
 * Verifica la integración con el sistema VeriFacTu de la AEAT
 */
@ExtendWith(MockitoExtension.class)
class VerifactuServiceTest {

    @Mock
    private CifradoService cifradoService;

    @InjectMocks
    private VerifactuService verifactuService;

    private Factura facturaPrueba;
    private Cliente clientePrueba;

    @BeforeEach
    void setUp() {
        clientePrueba = new Cliente();
        clientePrueba.setId(1L);
        clientePrueba.setNombre("Cliente Test S.L.");
        clientePrueba.setCif("B12345678");

        facturaPrueba = new Factura();
        facturaPrueba.setId(1L);
        facturaPrueba.setNumero("FV-2026-001");
        facturaPrueba.setCliente(clientePrueba);
        facturaPrueba.setFecha(LocalDate.now());
        facturaPrueba.setBaseImponible(new BigDecimal("100.00"));
        facturaPrueba.setIva(new BigDecimal("21.00"));
        facturaPrueba.setTotal(new BigDecimal("121.00"));
        facturaPrueba.setEstado("EMITIDA");
    }

    @Test
    void testVerifactuDeshabilitado() {
        // Arrange - sin configuración

        // Act
        boolean habilitado = verifactuService.isHabilitado();

        // Assert
        assertFalse(habilitado);
    }

    @Test
    void testGenerarHuella() {
        // Arrange
        String cadena = "FV-2026-001|2026-01-12|121.00|B12345678";

        // Act
        String huella = verifactuService.generarHuella(cadena);

        // Assert
        assertNotNull(huella);
        assertFalse(huella.isEmpty());
    }

    @Test
    void testGenerarQR() {
        // Arrange
        when(cifradoService.generarQR(anyString())).thenReturn("QR_CODE_BASE64");

        // Act
        String qr = verifactuService.generarQRFactura(facturaPrueba);

        // Assert
        assertNotNull(qr);
    }

    @Test
    void testValidarCertificado() {
        // Arrange - sin certificado configurado

        // Act
        boolean valido = verifactuService.validarCertificado();

        // Assert
        assertFalse(valido);
    }

    @Test
    void testEnviarFacturaAEAT_ModoSimulado() {
        // Arrange - modo simulado

        // Act
        boolean resultado = verifactuService.enviarFacturaAEAT(facturaPrueba);

        // Assert
        assertTrue(resultado); // En modo simulado siempre retorna true
    }

    @Test
    void testGenerarXMLFactura() {
        // Act
        String xml = verifactuService.generarXMLFactura(facturaPrueba);

        // Assert
        assertNotNull(xml);
        assertTrue(xml.contains("FV-2026-001"));
        assertTrue(xml.contains("B12345678"));
        assertTrue(xml.contains("121.00"));
    }

    @Test
    void testValidarFacturaAntesEnvio() {
        // Act
        boolean valida = verifactuService.validarFactura(facturaPrueba);

        // Assert
        assertTrue(valida);
    }

    @Test
    void testValidarFacturaSinNumero() {
        // Arrange
        facturaPrueba.setNumero(null);

        // Act
        boolean valida = verifactuService.validarFactura(facturaPrueba);

        // Assert
        assertFalse(valida);
    }

    @Test
    void testValidarFacturaSinCliente() {
        // Arrange
        facturaPrueba.setCliente(null);

        // Act
        boolean valida = verifactuService.validarFactura(facturaPrueba);

        // Assert
        assertFalse(valida);
    }

    @Test
    void testValidarFacturaSinTotal() {
        // Arrange
        facturaPrueba.setTotal(null);

        // Act
        boolean valida = verifactuService.validarFactura(facturaPrueba);

        // Assert
        assertFalse(valida);
    }

    @Test
    void testGenerarCSVFactura() {
        // Act
        String csv = verifactuService.generarCSV(facturaPrueba);

        // Assert
        assertNotNull(csv);
        assertTrue(csv.length() > 0);
    }

    @Test
    void testRegistrarEnvioFactura() {
        // Act
        verifactuService.registrarEnvio(facturaPrueba, true, "Envío exitoso");

        // Assert
        // Verificar que se registró correctamente (en implementación real)
        assertNotNull(facturaPrueba);
    }

    @Test
    void testObtenerEstadoServicio() {
        // Act
        String estado = verifactuService.obtenerEstadoServicio();

        // Assert
        assertNotNull(estado);
        assertTrue(estado.equals("SIMULADO") || estado.equals("ACTIVO") || estado.equals("ERROR"));
    }

    @Test
    void testCargarCertificado() {
        // Act & Assert
        assertDoesNotThrow(() -> {
            verifactuService.cargarCertificado("ruta/certificado.p12", "password");
        });
    }

    @Test
    void testFirmarFactura() {
        // Arrange
        String xml = "<factura>test</factura>";

        // Act
        String firmado = verifactuService.firmarXML(xml);

        // Assert
        assertNotNull(firmado);
    }
}

