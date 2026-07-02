package alicanteweb.erp.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Base64;

import static org.junit.jupiter.api.Assertions.*;

class QrCodeServiceTest {

    private QrCodeService service;

    @BeforeEach
    void setUp() {
        service = new QrCodeService();
        ReflectionTestUtils.setField(service, "qrBaseUrl",
                "https://www2.agenciatributaria.gob.es/wlpl/TIKE-CONT/ValidarQR");
    }

    @Test
    void construyeUrlOficialDeCotejoConParametrosReglamentarios() {
        String url = service.construirUrlVerificacionQr(
                "B12345678", "F-GEN-2026-0001", "02-07-2026", "121.00");

        assertEquals("https://www2.agenciatributaria.gob.es/wlpl/TIKE-CONT/ValidarQR"
                        + "?nif=B12345678&numserie=F-GEN-2026-0001&fecha=02-07-2026&importe=121.00",
                url);
        // La URL oficial no lleva hash ni parámetros adicionales
        assertFalse(url.contains("hash="));
    }

    @Test
    void codificaCaracteresEspecialesEnParametros() {
        String url = service.construirUrlVerificacionQr("B12345678", "A&B/01", "02-07-2026", "10.00");
        assertTrue(url.contains("numserie=A%26B%2F01"));
    }

    @Test
    void generaImagenQrEnBase64Png() {
        String qr = service.generarQRVeriFactu("B12345678", "F-GEN-2026-0001", "02-07-2026", "121.00");
        byte[] png = Base64.getDecoder().decode(qr);
        // Firma PNG
        assertEquals((byte) 0x89, png[0]);
        assertEquals('P', png[1]);
        assertEquals('N', png[2]);
        assertEquals('G', png[3]);
    }
}
