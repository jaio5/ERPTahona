package alicanteweb.erp.service;

import alicanteweb.erp.entities.Factura;
import alicanteweb.erp.entities.Cliente;
import alicanteweb.erp.entities.VerifactuEvidence;
import alicanteweb.erp.repository.VerifactuEvidenceRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Tests para VerifactuService
 * Verifica la integracion con el sistema VeriFacTu de la AEAT
 */
@ExtendWith(MockitoExtension.class)
class VerifactuServiceTest {

    @Mock
    private VerifactuEvidenceRepository evidenceRepository;

    @Mock
    private EmpresaConfigService empresaConfigService;

    @Mock
    private FacturaLineaService facturaLineaService;

    @Mock
    private QrCodeService qrCodeService;

    @Mock
    private VerifactuAeatSoapClient aeatSoapClient;

    private VerifactuService verifactuService;

    private Factura facturaPrueba;
    private Cliente clientePrueba;

    @BeforeEach
    void setUp() {
        // Crear instancia del servicio con mocks
        verifactuService = new VerifactuService(
            evidenceRepository,
            empresaConfigService,
            facturaLineaService,
            qrCodeService,
            aeatSoapClient
        );

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
        facturaPrueba.setTotal(new BigDecimal("121.00"));
        facturaPrueba.setEstado("EMITIDA");
    }

    @Test
    void testVerifactuDeshabilitadoPorDefecto() {
        // Sin certificado configurado, el servicio debe estar deshabilitado
        assertFalse(verifactuService.isEnabled());
    }

    @Test
    void testGenerarHash() throws Exception {
        // Arrange
        String datos = "FV-2026-001|2026-01-12|121.00|B12345678";

        // Act
        String hash = verifactuService.generarHash(datos);

        // Assert
        assertNotNull(hash);
        assertFalse(hash.isEmpty());
        // El hash SHA-256 en Base64 tiene longitud fija
        assertTrue(hash.length() > 20);
    }

    @Test
    void testGenerarHashEncadenado() throws Exception {
        // Arrange
        String datos = "FV-2026-002|2026-01-13|150.00|B12345678";
        String hashAnterior = "ABC123XYZ789";

        // Act
        String hashEncadenado = verifactuService.generarHashEncadenado(datos, hashAnterior);

        // Assert
        assertNotNull(hashEncadenado);
        assertFalse(hashEncadenado.isEmpty());
    }

    @Test
    void testGenerarHashSinEncadenar() throws Exception {
        // Arrange
        String datos = "FV-2026-001|2026-01-12|121.00|B12345678";

        // Act
        String hash1 = verifactuService.generarHashEncadenado(datos, null);
        String hash2 = verifactuService.generarHashEncadenado(datos, "");

        // Assert
        assertNotNull(hash1);
        assertNotNull(hash2);
        assertEquals(hash1, hash2); // Sin hash anterior, deben ser iguales
    }

    @Test
    void testObtenerHashAnteriorExistente() {
        // Arrange
        VerifactuEvidence evidenciaAnterior = new VerifactuEvidence();
        evidenciaAnterior.setHash("HASH_ANTERIOR_123");
        when(evidenceRepository.findFirstBySerieOrderByFechaEmisionDesc("FV"))
            .thenReturn(Optional.of(evidenciaAnterior));

        // Act
        String hashAnterior = verifactuService.obtenerHashAnterior("FV");

        // Assert
        assertEquals("HASH_ANTERIOR_123", hashAnterior);
    }

    @Test
    void testObtenerHashAnteriorNoExistente() {
        // Arrange
        when(evidenceRepository.findFirstBySerieOrderByFechaEmisionDesc("FV"))
            .thenReturn(Optional.empty());

        // Act
        String hashAnterior = verifactuService.obtenerHashAnterior("FV");

        // Assert
        assertNull(hashAnterior);
    }

    @Test
    void testFirmarDatosDeshabilitado() {
        // Sin certificado, firmar debe lanzar excepcion
        assertThrows(IllegalStateException.class, () -> {
            verifactuService.firmarDatos("test".getBytes());
        });
    }

    @Test
    void testVerificarFirmaDeshabilitado() {
        // Sin certificado, verificar firma debe lanzar excepcion
        assertThrows(IllegalStateException.class, () -> {
            verifactuService.verificarFirma("test".getBytes(), "firma".getBytes());
        });
    }

    @Test
    void testGetCertificateFingerprintDeshabilitado() throws Exception {
        // Sin certificado, debe retornar null
        String fingerprint = verifactuService.getCertificateFingerprint();
        assertNull(fingerprint);
    }

    @Test
    void testHashesDiferentesParaDatosDiferentes() throws Exception {
        // Arrange
        String datos1 = "FV-2026-001|100.00";
        String datos2 = "FV-2026-002|200.00";

        // Act
        String hash1 = verifactuService.generarHash(datos1);
        String hash2 = verifactuService.generarHash(datos2);

        // Assert
        assertNotEquals(hash1, hash2);
    }

    @Test
    void testHashConsistente() throws Exception {
        // El mismo input debe producir el mismo hash
        String datos = "FV-2026-001|100.00|B12345678";

        String hash1 = verifactuService.generarHash(datos);
        String hash2 = verifactuService.generarHash(datos);

        assertEquals(hash1, hash2);
    }
}

