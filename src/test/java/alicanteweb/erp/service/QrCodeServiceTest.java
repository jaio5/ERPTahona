package alicanteweb.erp.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests para QrCodeService
 */
@SpringBootTest
class QrCodeServiceTest {

    @Autowired
    private QrCodeService qrCodeService;

    @Test
    void testGenerarQR() {
        // Given
        String texto = "https://www.test.com/verificar?id=12345";

        // When
        String qrBase64 = qrCodeService.generarQR(texto);

        // Then
        assertNotNull(qrBase64);
        assertFalse(qrBase64.isEmpty());
        assertTrue(qrCodeService.esQRValido(qrBase64));
    }

    @Test
    void testGenerarQRHash() {
        // Given
        String hash = "a1b2c3d4e5f6g7h8i9j0k1l2m3n4o5p6";

        // When
        String qrBase64 = qrCodeService.generarQRHash(hash);

        // Then
        assertNotNull(qrBase64);
        assertTrue(qrCodeService.esQRValido(qrBase64));
    }

    @Test
    void testGenerarQRVeriFactu() {
        // Given
        String hash = "abc123def456";
        String nif = "B12345678";
        String numeroFactura = "2025/0001";
        String fecha = "26-12-2025";
        String importe = "1234.56";

        // When
        String qrBase64 = qrCodeService.generarQRVeriFactu(hash, nif, numeroFactura, fecha, importe);

        // Then
        assertNotNull(qrBase64);
        assertFalse(qrBase64.isEmpty());
        assertTrue(qrCodeService.esQRValido(qrBase64));
    }

    @Test
    void testGenerarQRContacto() {
        // Given
        String nombre = "Juan Pérez";
        String telefono = "123456789";
        String email = "juan@test.com";
        String empresa = "Test SL";

        // When
        String qrBase64 = qrCodeService.generarQRContacto(nombre, telefono, email, empresa);

        // Then
        assertNotNull(qrBase64);
        assertTrue(qrCodeService.esQRValido(qrBase64));
    }

    @Test
    void testObtenerTamanoQR() {
        // Given
        String texto = "Test QR Code";
        String qrBase64 = qrCodeService.generarQR(texto);

        // When
        int tamano = qrCodeService.obtenerTamanoQR(qrBase64);

        // Then
        assertTrue(tamano > 0);
    }

    @Test
    void testQRInvalido() {
        // When
        boolean esValido = qrCodeService.esQRValido("esto no es un QR válido");

        // Then
        assertFalse(esValido);
    }

    @Test
    void testQRVacio() {
        // When
        boolean esValido = qrCodeService.esQRValido("");

        // Then
        assertFalse(esValido);
    }

    @Test
    void testQRNull() {
        // When
        boolean esValido = qrCodeService.esQRValido(null);

        // Then
        assertFalse(esValido);
    }
}

