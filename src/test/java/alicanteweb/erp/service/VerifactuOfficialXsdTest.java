package alicanteweb.erp.service;

import alicanteweb.erp.entities.Cliente;
import alicanteweb.erp.entities.EmpresaConfig;
import alicanteweb.erp.entities.Factura;
import alicanteweb.erp.entities.FacturaLinea;
import alicanteweb.erp.repository.VerifactuEvidenceRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.test.util.ReflectionTestUtils;
import org.w3c.dom.ls.LSInput;
import org.w3c.dom.ls.LSResourceResolver;

import javax.xml.XMLConstants;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import java.io.InputStream;
import java.io.Reader;
import java.math.BigDecimal;
import java.net.URL;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;

/**
 * Regresión de conformidad estructural: valida el XML generado por {@link VerifactuService}
 * contra los XSD OFICIALES publicados por AEAT (no la transcripción interna), de modo que un
 * cambio de namespace o de estructura que AEAT rechazaría rompa la build.
 *
 * <p>Los XSD viven en {@code src/test/resources/xsd-oficial-aeat/}. El import remoto de xmldsig
 * (schemaLocation http de w3.org) se resuelve a la copia local mediante un LSResourceResolver,
 * para no depender de la red durante los tests.
 */
@ExtendWith(MockitoExtension.class)
class VerifactuOfficialXsdTest {

    private static final String XSD_DIR = "/xsd-oficial-aeat/";

    @Mock private VerifactuEvidenceRepository evidenceRepository;
    @Mock private EmpresaConfigService empresaConfigService;
    @Mock private FacturaLineaService facturaLineaService;
    @Mock private QrCodeService qrCodeService;
    @Mock private FacturacionEventoService facturacionEventoService;

    private VerifactuService service;
    private EmpresaConfig empresa;

    @BeforeEach
    void setUp() {
        @SuppressWarnings("unchecked")
        ObjectProvider<VerifactuAeatSoapClient> provider = mock(ObjectProvider.class);
        lenient().when(provider.getIfAvailable()).thenReturn(null);

        service = new VerifactuService(evidenceRepository, empresaConfigService,
                facturaLineaService, qrCodeService, provider, facturacionEventoService);
        ReflectionTestUtils.setField(service, "sistemaNombreProductor", "");
        ReflectionTestUtils.setField(service, "sistemaNifProductor", "");
        ReflectionTestUtils.setField(service, "sistemaId", "01");
        ReflectionTestUtils.setField(service, "sistemaNumeroInstalacion", "1");
        ReflectionTestUtils.setField(service, "sistemaSoloVerifactu", "S");

        empresa = new EmpresaConfig();
        empresa.setNombreEmpresa("Panadería Tahona SL");
        empresa.setVerifactuNifEmisor("B12345678");
        lenient().when(empresaConfigService.getConfiguracionActivaOrThrow()).thenReturn(empresa);
        lenient().when(evidenceRepository.findFirstBySerieOrderByFechaGeneracionRegistroDescIdDesc(anyString()))
                .thenReturn(Optional.empty());
        lenient().when(evidenceRepository.findFirstBySerieOrderByFechaEmisionDesc(anyString()))
                .thenReturn(Optional.empty());
    }

    private Factura facturaBase() {
        Cliente cliente = new Cliente();
        cliente.setNombre("Cliente VeriFactu");
        cliente.setCif("B87654321");
        Factura factura = new Factura();
        factura.setNumero("F-GEN-2026-0001");
        factura.setSerie("F-GEN");
        factura.setFecha(LocalDate.of(2026, 7, 2));
        factura.setTipoFactura("ORDINARIA");
        factura.setTotalIva(new BigDecimal("2.10"));
        factura.setTotal(new BigDecimal("12.10"));
        factura.setCliente(cliente);
        return factura;
    }

    private List<FacturaLinea> lineaSimple() {
        FacturaLinea linea = new FacturaLinea();
        linea.setCantidad(new BigDecimal("10"));
        linea.setPrecio(new BigDecimal("1.00"));
        linea.setIva(new BigDecimal("21"));
        linea.setDescuento(BigDecimal.ZERO);
        return List.of(linea);
    }

    @Test
    void registroAltaValidaContraXsdOficialDeAeat() throws Exception {
        String xml = service.generarRegistroAltaXml(facturaBase(), lineaSimple());
        assertDoesNotThrow(() -> validarContraXsdOficial(xml),
                "El registro de alta debe validar contra el XSD oficial de AEAT");
    }

    @Test
    void registroAnulacionValidaContraXsdOficialDeAeat() throws Exception {
        String xml = service.generarRegistroAnulacionXml(facturaBase(), "Prueba");
        assertDoesNotThrow(() -> validarContraXsdOficial(xml),
                "El registro de anulación debe validar contra el XSD oficial de AEAT");
    }

    @Test
    void registroRectificativaValidaContraXsdOficialDeAeat() throws Exception {
        Factura factura = facturaBase();
        factura.setTipoFactura("RECTIFICATIVA");
        factura.setTipoRectificacion("SUSTITUCION");
        factura.setFacturaRectificadaNumero("F-GEN-2026-0000");
        factura.setFacturaRectificadaFecha(LocalDate.of(2026, 7, 1));
        String xml = service.generarRegistroAltaXml(factura, lineaSimple());
        assertDoesNotThrow(() -> validarContraXsdOficial(xml),
                "El registro rectificativo debe validar contra el XSD oficial de AEAT");
    }

    private void validarContraXsdOficial(String xml) throws Exception {
        SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
        factory.setResourceResolver(new LocalXsdResolver());

        URL sumInfo = getClass().getResource(XSD_DIR + "SuministroInformacion.xsd");
        URL sumLr = getClass().getResource(XSD_DIR + "SuministroLR.xsd");
        if (sumInfo == null || sumLr == null) {
            throw new IllegalStateException("No se encontraron los XSD oficiales de AEAT en test resources");
        }
        // systemId apunta a la URL del recurso para que las importaciones relativas resuelvan
        Schema schema = factory.newSchema(new Source[]{
                new StreamSource(sumInfo.openStream(), sumInfo.toExternalForm()),
                new StreamSource(sumLr.openStream(), sumLr.toExternalForm())
        });

        Validator validator = schema.newValidator();
        validator.validate(new StreamSource(new java.io.StringReader(xml)));
    }

    /** Resuelve el import remoto de xmldsig (y xml.xsd) a las copias locales en test resources. */
    private final class LocalXsdResolver implements LSResourceResolver {
        @Override
        public LSInput resolveResource(String type, String namespaceURI, String publicId,
                                       String systemId, String baseURI) {
            String local = null;
            if (systemId != null && systemId.contains("xmldsig-core-schema.xsd")) {
                local = "xmldsig-core-schema.xsd";
            } else if (systemId != null && systemId.endsWith("xml.xsd")) {
                local = "xml.xsd";
            } else if ("http://www.w3.org/XML/1998/namespace".equals(namespaceURI)) {
                local = "xml.xsd";
            }
            if (local == null) {
                return null;
            }
            InputStream is = getClass().getResourceAsStream(XSD_DIR + local);
            if (is == null) {
                return null;
            }
            return new StreamLSInput(is, publicId, systemId, baseURI);
        }
    }

    private static final class StreamLSInput implements LSInput {
        private InputStream byteStream;
        private String publicId;
        private String systemId;
        private String baseURI;

        StreamLSInput(InputStream is, String publicId, String systemId, String baseURI) {
            this.byteStream = is;
            this.publicId = publicId;
            this.systemId = systemId;
            this.baseURI = baseURI;
        }

        @Override public Reader getCharacterStream() { return null; }
        @Override public void setCharacterStream(Reader characterStream) { }
        @Override public InputStream getByteStream() { return byteStream; }
        @Override public void setByteStream(InputStream byteStream) { this.byteStream = byteStream; }
        @Override public String getStringData() { return null; }
        @Override public void setStringData(String stringData) { }
        @Override public String getSystemId() { return systemId; }
        @Override public void setSystemId(String systemId) { this.systemId = systemId; }
        @Override public String getPublicId() { return publicId; }
        @Override public void setPublicId(String publicId) { this.publicId = publicId; }
        @Override public String getBaseURI() { return baseURI; }
        @Override public void setBaseURI(String baseURI) { this.baseURI = baseURI; }
        @Override public String getEncoding() { return "UTF-8"; }
        @Override public void setEncoding(String encoding) { }
        @Override public boolean getCertifiedText() { return false; }
        @Override public void setCertifiedText(boolean certifiedText) { }
    }
}
