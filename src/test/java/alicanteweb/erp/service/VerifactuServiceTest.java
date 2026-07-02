package alicanteweb.erp.service;

import alicanteweb.erp.entities.Cliente;
import alicanteweb.erp.entities.EmpresaConfig;
import alicanteweb.erp.entities.Factura;
import alicanteweb.erp.entities.FacturaLinea;
import alicanteweb.erp.entities.VerifactuEvidence;
import alicanteweb.erp.repository.VerifactuEvidenceRepository;
import alicanteweb.erp.util.HashUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;

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
    private FacturacionEventoService facturacionEventoService;

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

    private String extraer(String xml, String elemento) {
        Matcher m = Pattern.compile("<sum1:" + elemento + ">([^<]*)</sum1:" + elemento + ">").matcher(xml);
        return m.find() ? m.group(1) : null;
    }

    @Test
    void registroAltaPrimerRegistroConEstructuraOficialYHuellaReproducible() {
        String xml = service.generarRegistroAltaXml(facturaBase(), lineaSimple());

        // Estructura oficial (el propio método valida contra los XSD internos antes de devolver)
        assertTrue(xml.contains("<sum:RegFactuSistemaFacturacion"));
        assertTrue(xml.contains("<sum1:IDVersion>1.0</sum1:IDVersion>"));
        assertTrue(xml.contains("<sum1:IDEmisorFactura>B12345678</sum1:IDEmisorFactura>"));
        assertTrue(xml.contains("<sum1:NumSerieFactura>F-GEN-2026-0001</sum1:NumSerieFactura>"));
        assertTrue(xml.contains("<sum1:TipoFactura>F1</sum1:TipoFactura>"));
        assertTrue(xml.contains("<sum1:PrimerRegistro>S</sum1:PrimerRegistro>"));
        assertTrue(xml.contains("<sum1:NombreSistemaInformatico>"));
        assertTrue(xml.contains("<sum1:TipoHuella>01</sum1:TipoHuella>"));
        assertTrue(xml.contains("<sum1:TipoImpositivo>21.00</sum1:TipoImpositivo>"));
        assertTrue(xml.contains("<sum1:BaseImponibleOimporteNoSujeto>10.00</sum1:BaseImponibleOimporteNoSujeto>"));
        assertTrue(xml.contains("<sum1:CuotaRepercutida>2.10</sum1:CuotaRepercutida>"));

        // La huella debe seguir la fórmula de la Orden HAC/1177/2024 y ser reproducible
        String fechaHora = extraer(xml, "FechaHoraHusoGenRegistro");
        String huella = extraer(xml, "Huella");
        assertNotNull(fechaHora);
        assertNotNull(huella);
        String esperada = HashUtils.sha256Hex(
                "IDEmisorFactura=B12345678"
                        + "&NumSerieFacturaEmisor=F-GEN-2026-0001"
                        + "&FechaExpedicionFacturaEmisor=02-07-2026"
                        + "&TipoFactura=F1"
                        + "&CuotaTotal=2.10"
                        + "&ImporteTotal=12.10"
                        + "&Huella="
                        + "&FechaHoraHusoGenRegistro=" + fechaHora
        ).toUpperCase(Locale.ROOT);
        assertEquals(esperada, huella);
        assertEquals(64, huella.length());
    }

    @Test
    void registroAltaEncadenaConElRegistroAnterior() {
        VerifactuEvidence anterior = new VerifactuEvidence();
        anterior.setNifEmisor("B12345678");
        anterior.setNumero("F-GEN-2026-0000");
        anterior.setFechaExpedicionFactura(LocalDate.of(2026, 7, 1));
        anterior.setHuellaRegistro("A".repeat(64));
        lenient().when(evidenceRepository.findFirstBySerieOrderByFechaGeneracionRegistroDescIdDesc(anyString()))
                .thenReturn(Optional.of(anterior));

        String xml = service.generarRegistroAltaXml(facturaBase(), lineaSimple());

        assertFalse(xml.contains("PrimerRegistro"));
        assertTrue(xml.contains("<sum1:RegistroAnterior>"));
        assertTrue(xml.contains("<sum1:NumSerieFactura>F-GEN-2026-0000</sum1:NumSerieFactura>"));
        assertTrue(xml.contains("<sum1:Huella>" + "A".repeat(64) + "</sum1:Huella>"));

        // La huella del registro (última del documento) debe incorporar la huella anterior en la fórmula
        Matcher m = Pattern.compile("<sum1:Huella>([^<]*)</sum1:Huella>").matcher(xml);
        String ultimaHuella = null;
        while (m.find()) ultimaHuella = m.group(1);
        String fechaHora = extraer(xml, "FechaHoraHusoGenRegistro");
        String esperada = HashUtils.sha256Hex(
                "IDEmisorFactura=B12345678"
                        + "&NumSerieFacturaEmisor=F-GEN-2026-0001"
                        + "&FechaExpedicionFacturaEmisor=02-07-2026"
                        + "&TipoFactura=F1"
                        + "&CuotaTotal=2.10"
                        + "&ImporteTotal=12.10"
                        + "&Huella=" + "A".repeat(64)
                        + "&FechaHoraHusoGenRegistro=" + fechaHora
        ).toUpperCase(Locale.ROOT);
        assertEquals(esperada, ultimaHuella);
    }

    @Test
    void rectificativaLlevaTipoOficialYReferenciaALaOriginal() {
        Factura factura = facturaBase();
        factura.setTipoFactura("RECTIFICATIVA");
        factura.setTipoRectificacion("SUSTITUCION");
        factura.setFacturaRectificadaNumero("F-GEN-2026-0001");
        factura.setFacturaRectificadaFecha(LocalDate.of(2026, 7, 1));
        factura.setNumero("R-GEN-2026-0002");

        String xml = service.generarRegistroAltaXml(factura, lineaSimple());

        assertTrue(xml.contains("<sum1:TipoFactura>R4</sum1:TipoFactura>"));
        assertTrue(xml.contains("<sum1:TipoRectificativa>S</sum1:TipoRectificativa>"));
        assertTrue(xml.contains("<sum1:FacturasRectificadas>"));
        assertTrue(xml.contains("<sum1:NumSerieFactura>F-GEN-2026-0001</sum1:NumSerieFactura>"));
    }

    @Test
    void registroAnulacionConEstructuraOficialYHuellaReproducible() {
        String xml = service.generarRegistroAnulacionXml(facturaBase(), "Prueba");

        assertTrue(xml.contains("<sum1:RegistroAnulacion>"));
        assertTrue(xml.contains("<sum1:IDEmisorFacturaAnulada>B12345678</sum1:IDEmisorFacturaAnulada>"));
        assertTrue(xml.contains("<sum1:NumSerieFacturaAnulada>F-GEN-2026-0001</sum1:NumSerieFacturaAnulada>"));

        String fechaHora = extraer(xml, "FechaHoraHusoGenRegistro");
        String huella = extraer(xml, "Huella");
        String esperada = HashUtils.sha256Hex(
                "IDEmisorFacturaAnulada=B12345678"
                        + "&NumSerieFacturaAnulada=F-GEN-2026-0001"
                        + "&FechaExpedicionFacturaAnulada=02-07-2026"
                        + "&Huella="
                        + "&FechaHoraHusoGenRegistro=" + fechaHora
        ).toUpperCase(Locale.ROOT);
        assertEquals(esperada, huella);
    }

    @Test
    void mapeaTiposDeFacturaALosCodigosOficiales() {
        Factura ordinaria = new Factura();
        ordinaria.setTipoFactura("ORDINARIA");
        assertEquals("F1", service.tipoFacturaOficial(ordinaria));

        Factura simplificada = new Factura();
        simplificada.setTipoFactura("SIMPLIFICADA");
        assertEquals("F2", service.tipoFacturaOficial(simplificada));

        Factura rectificativa = new Factura();
        rectificativa.setTipoFactura("RECTIFICATIVA");
        assertEquals("R4", service.tipoFacturaOficial(rectificativa));

        assertEquals("S", service.tipoRectificativaOficial("SUSTITUCION"));
        assertEquals("I", service.tipoRectificativaOficial("DIFERENCIAS"));
    }
}
