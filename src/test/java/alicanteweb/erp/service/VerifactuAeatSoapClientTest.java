package alicanteweb.erp.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests unitarios para VerifactuAeatSoapClient.
 * El bean solo se registra cuando verifactu.aeat.enabled=true, por lo que se instancia
 * directamente y se configuran los campos via ReflectionTestUtils.
 */
class VerifactuAeatSoapClientTest {

    private VerifactuAeatSoapClient client;

    @BeforeEach
    void setUp() {
        client = new VerifactuAeatSoapClient();
        ReflectionTestUtils.setField(client, "aeatEnabled", false);
        ReflectionTestUtils.setField(client, "aeatEndpoint", "https://prewww1.aeat.es/wlpl/TIKE-CONT/ws/SistemaFacturacion/VerifactuSOAP");
        ReflectionTestUtils.setField(client, "keystorePath", "");
        ReflectionTestUtils.setField(client, "keystorePassword", "");
        ReflectionTestUtils.setField(client, "keyAlias", "mi_certificado");
        ReflectionTestUtils.setField(client, "keyPassword", "");
    }

    @Test
    void enviarFacturaAeat_retornaSimuladoOk_cuandoEstaDeshabilitado() throws Exception {
        String xmlFactura = "<RegistroFactura><Cabecera/></RegistroFactura>";

        String resultado = client.enviarFacturaAeat(xmlFactura, null);

        assertEquals("SIMULADO_OK", resultado);
    }

    @Test
    void enviarFacturaAeat_retornaSimuladoOk_conFirmaExterna_cuandoDeshabilitado() throws Exception {
        String xmlFactura = "<RegistroFactura><Cabecera/></RegistroFactura>";
        byte[] firma = new byte[]{1, 2, 3};

        String resultado = client.enviarFacturaAeat(xmlFactura, firma);

        assertEquals("SIMULADO_OK", resultado);
    }

    @Test
    void enviarFacturaAeat_lanzaException_cuandoEnabledYEndpointInaccesible() {
        ReflectionTestUtils.setField(client, "aeatEnabled", true);
        ReflectionTestUtils.setField(client, "aeatEndpoint", "https://localhost:9/inexistente");
        ReflectionTestUtils.setField(client, "keystorePath", "inexistente.p12");
        String xmlFactura = "<RegistroFactura><Cabecera/></RegistroFactura>";

        assertThrows(Exception.class, () -> client.enviarFacturaAeat(xmlFactura, null));
    }

    @Test
    void enviarFacturaAeat_aceptaXmlVacio_cuandoDeshabilitado() throws Exception {
        String resultado = client.enviarFacturaAeat("", null);

        assertEquals("SIMULADO_OK", resultado);
    }

    @Test
    void enviarFacturaAeat_aceptaXmlNull_cuandoDeshabilitado() throws Exception {
        String resultado = client.enviarFacturaAeat(null, null);

        assertEquals("SIMULADO_OK", resultado);
    }
}
