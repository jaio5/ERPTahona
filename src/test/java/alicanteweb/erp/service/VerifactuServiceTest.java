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
    void desgloseReflejaDescuentoGlobalYCuadraConElCuotaTotal() {
        Cliente cliente = new Cliente();
        cliente.setNombre("Cliente");
        cliente.setCif("B87654321");
        Factura f = new Factura();
        f.setNumero("F-GEN-2026-0002");
        f.setSerie("F-GEN");
        f.setFecha(LocalDate.of(2026, 7, 2));
        f.setTipoFactura("ORDINARIA");
        f.setCliente(cliente);
        // 10% de descuento global sobre 200 (100 al 21% + 100 al 10%) -> base 180, IVA 27,90.
        f.setDescuentoGlobalTipo("PORCENTAJE");
        f.setDescuentoGlobalValor(new BigDecimal("10"));
        f.setTotalIva(new BigDecimal("27.90"));
        f.setTotal(new BigDecimal("207.90"));

        FacturaLinea l1 = new FacturaLinea();
        l1.setCantidad(new BigDecimal("1")); l1.setPrecio(new BigDecimal("100.00")); l1.setIva(new BigDecimal("21"));
        FacturaLinea l2 = new FacturaLinea();
        l2.setCantidad(new BigDecimal("1")); l2.setPrecio(new BigDecimal("100.00")); l2.setIva(new BigDecimal("10"));

        String xml = service.generarRegistroAltaXml(f, List.of(l1, l2));

        // El desglose lleva el descuento global prorrateado (cada base 100 -> 90)...
        assertTrue(xml.contains("<sum1:BaseImponibleOimporteNoSujeto>90.00</sum1:BaseImponibleOimporteNoSujeto>"),
                "La base de cada tipo debe reflejar el descuento global");
        assertTrue(xml.contains("<sum1:CuotaRepercutida>9.00</sum1:CuotaRepercutida>"));   // 10% de 90
        assertTrue(xml.contains("<sum1:CuotaRepercutida>18.90</sum1:CuotaRepercutida>"));  // 21% de 90
        // ...y la suma del desglose cuadra con el CuotaTotal firmado.
        assertTrue(xml.contains("<sum1:CuotaTotal>27.90</sum1:CuotaTotal>"),
                "El desglose debe cuadrar con el CuotaTotal");
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

    // ===== Contención F0: ningún estado fiscal falso al emitir =====

    /** Prepara una factura en REVISION lista para emitir y los colaboradores mínimos del flujo. */
    private Factura facturaParaEmitir(VerifactuService svc) {
        Factura factura = facturaBase();
        factura.setId(42L);
        factura.setEstado("REVISION");
        lenient().when(empresaConfigService.existeConfiguracionActiva()).thenReturn(true);
        lenient().when(empresaConfigService.isFuncionamientoVerifactuVigente(empresa)).thenReturn(true);
        lenient().when(facturaLineaService.findByFacturaId(42L)).thenReturn(lineaSimple());
        lenient().when(qrCodeService.generarQRVeriFactu(anyString(), anyString(), anyString(), anyString())).thenReturn("QR");
        lenient().when(evidenceRepository.save(org.mockito.ArgumentMatchers.any(VerifactuEvidence.class)))
                .thenAnswer(inv -> {
                    VerifactuEvidence ev = inv.getArgument(0);
                    if (ev.getId() == null) ev.setId(7L);
                    return ev;
                });
        return factura;
    }

    private VerifactuEvidence ultimaEvidenciaGuardada() {
        org.mockito.ArgumentCaptor<VerifactuEvidence> captor = org.mockito.ArgumentCaptor.forClass(VerifactuEvidence.class);
        org.mockito.Mockito.verify(evidenceRepository, org.mockito.Mockito.atLeastOnce()).save(captor.capture());
        return captor.getValue();
    }

    /** Servicio con remisión "habilitada": certificado de prueba y cliente SOAP simulado. */
    private VerifactuService servicioConRemision(VerifactuAeatSoapClient cliente) throws Exception {
        @SuppressWarnings("unchecked")
        ObjectProvider<VerifactuAeatSoapClient> provider = mock(ObjectProvider.class);
        lenient().when(provider.getIfAvailable()).thenReturn(cliente);
        VerifactuService svc = new VerifactuService(evidenceRepository, empresaConfigService,
                facturaLineaService, qrCodeService, provider, facturacionEventoService);
        ReflectionTestUtils.setField(svc, "sistemaNombreProductor", "");
        ReflectionTestUtils.setField(svc, "sistemaNifProductor", "");
        ReflectionTestUtils.setField(svc, "sistemaId", "01");
        ReflectionTestUtils.setField(svc, "sistemaNumeroInstalacion", "1");
        ReflectionTestUtils.setField(svc, "sistemaSoloVerifactu", "S");
        ReflectionTestUtils.setField(svc, "aeatEnabled", true);
        ReflectionTestUtils.setField(svc, "aeatEndpoint", "https://prewww1.aeat.es/test");
        java.security.KeyPairGenerator kpg = java.security.KeyPairGenerator.getInstance("RSA");
        kpg.initialize(2048);
        ReflectionTestUtils.setField(svc, "privateKey", kpg.generateKeyPair().getPrivate());
        ReflectionTestUtils.setField(svc, "enabled", true);
        return svc;
    }

    @Test
    void conRemisionDesactivadaLaFacturaSeExpidePeroNoConstaComoEnviada() {
        ReflectionTestUtils.setField(service, "aeatEnabled", false);
        Factura factura = facturaParaEmitir(service);

        service.enviarFacturaVerifactu(factura);

        assertEquals("EMITIDA", factura.getEstado(), "La factura queda expedida");
        assertFalse(Boolean.TRUE.equals(factura.getVerifactuEnviada()), "No se ha remitido nada a la AEAT");
        assertNotNull(factura.getVerifactuHash());
        VerifactuEvidence ev = ultimaEvidenciaGuardada();
        assertEquals(VerifactuEvidence.ESTADO_NO_REMITIDO, ev.getEstado());
        assertNotEquals("ENVIADO", ev.getEstado());
        assertNull(ev.getFechaEnvio(), "Sin remisión no hay fecha de envío");
        org.mockito.Mockito.verify(facturacionEventoService).registrarEvento(
                org.mockito.ArgumentMatchers.eq(FacturacionEventoService.AMBITO_FACTURAS),
                org.mockito.ArgumentMatchers.eq("ENVIO_AEAT_OMITIDO"),
                org.mockito.ArgumentMatchers.eq("F-GEN-2026-0001"),
                org.mockito.ArgumentMatchers.any());
    }

    @Test
    void conRespuestaCorrectaDeLaAeatLaFacturaConstaComoRemitida() throws Exception {
        VerifactuAeatSoapClient cliente = mock(VerifactuAeatSoapClient.class);
        org.mockito.Mockito.when(cliente.enviarFacturaAeat(anyString(), org.mockito.ArgumentMatchers.any())).thenReturn("ACEPTADA");
        VerifactuService svc = servicioConRemision(cliente);
        Factura factura = facturaParaEmitir(svc);

        svc.enviarFacturaVerifactu(factura);

        assertEquals("EMITIDA", factura.getEstado());
        assertTrue(Boolean.TRUE.equals(factura.getVerifactuEnviada()));
        VerifactuEvidence ev = ultimaEvidenciaGuardada();
        assertEquals(VerifactuEvidence.ESTADO_ACEPTADO, ev.getEstado());
        assertEquals("Correcto", ev.getCodigoRespuestaAEAT());
        assertNotNull(ev.getFechaEnvio());
    }

    @Test
    void conAceptadoConErroresConstaRemitidaConSusAvisos() throws Exception {
        VerifactuAeatSoapClient cliente = mock(VerifactuAeatSoapClient.class);
        org.mockito.Mockito.when(cliente.enviarFacturaAeat(anyString(), org.mockito.ArgumentMatchers.any()))
                .thenReturn("ACEPTADA_CON_ERRORES: 1100 aviso");
        VerifactuService svc = servicioConRemision(cliente);
        Factura factura = facturaParaEmitir(svc);

        svc.enviarFacturaVerifactu(factura);

        assertTrue(Boolean.TRUE.equals(factura.getVerifactuEnviada()));
        VerifactuEvidence ev = ultimaEvidenciaGuardada();
        assertEquals(VerifactuEvidence.ESTADO_ACEPTADO_CON_ERRORES, ev.getEstado());
        assertEquals("AceptadoConErrores", ev.getCodigoRespuestaAEAT());
        assertTrue(ev.getErrorMessage().contains("1100"));
    }

    /**
     * Antes una respuesta que no se sabía interpretar (RESPUESTA_INESPERADA) se daba por buena
     * (código OK, ENVIADO, verifactuEnviada=true). Ahora la factura se expide, pero el registro
     * queda como no confirmado y la factura no consta como remitida.
     */
    @Test
    void conRespuestaInesperadaNoSeDaPorRemitida() throws Exception {
        VerifactuAeatSoapClient cliente = mock(VerifactuAeatSoapClient.class);
        org.mockito.Mockito.when(cliente.enviarFacturaAeat(anyString(), org.mockito.ArgumentMatchers.any()))
                .thenReturn("RESPUESTA_INESPERADA");
        VerifactuService svc = servicioConRemision(cliente);
        Factura factura = facturaParaEmitir(svc);

        svc.enviarFacturaVerifactu(factura);

        assertEquals("EMITIDA", factura.getEstado());
        assertFalse(Boolean.TRUE.equals(factura.getVerifactuEnviada()));
        VerifactuEvidence ev = ultimaEvidenciaGuardada();
        assertEquals(VerifactuEvidence.ESTADO_RESPUESTA_NO_CONFIRMADA, ev.getEstado());
        assertNotEquals("OK", ev.getCodigoRespuestaAEAT());
        assertNull(ev.getFechaEnvio());
        org.mockito.Mockito.verify(facturacionEventoService).registrarEvento(
                org.mockito.ArgumentMatchers.eq(FacturacionEventoService.AMBITO_FACTURAS),
                org.mockito.ArgumentMatchers.eq("ENVIO_AEAT_SIN_CONFIRMAR"),
                org.mockito.ArgumentMatchers.anyString(),
                org.mockito.ArgumentMatchers.any());
    }

    @Test
    void conErrorDeComunicacionLaFacturaNoSeExpideYLaEvidenciaQuedaEnError() throws Exception {
        VerifactuAeatSoapClient cliente = mock(VerifactuAeatSoapClient.class);
        org.mockito.Mockito.when(cliente.enviarFacturaAeat(anyString(), org.mockito.ArgumentMatchers.any()))
                .thenThrow(new java.io.IOException("timeout"));
        VerifactuService svc = servicioConRemision(cliente);
        Factura factura = facturaParaEmitir(svc);

        assertThrows(alicanteweb.erp.exception.ErpException.class, () -> svc.enviarFacturaVerifactu(factura));

        assertEquals("REVISION", factura.getEstado());
        assertFalse(Boolean.TRUE.equals(factura.getVerifactuEnviada()));
        assertEquals(VerifactuEvidence.ESTADO_ERROR, ultimaEvidenciaGuardada().getEstado());
    }

    @Test
    void estadoRemisionSoloConfirmaRespuestasDeAceptacion() {
        assertEquals(VerifactuEvidence.ESTADO_ACEPTADO, VerifactuService.estadoRemisionDesdeRespuesta("ACEPTADA"));
        assertEquals(VerifactuEvidence.ESTADO_ACEPTADO_CON_ERRORES,
                VerifactuService.estadoRemisionDesdeRespuesta("ACEPTADA_CON_ERRORES: x"));
        assertEquals(VerifactuEvidence.ESTADO_RESPUESTA_NO_CONFIRMADA, VerifactuService.estadoRemisionDesdeRespuesta("SIMULADO_OK"));
        assertEquals(VerifactuEvidence.ESTADO_RESPUESTA_NO_CONFIRMADA, VerifactuService.estadoRemisionDesdeRespuesta("RESPUESTA_INESPERADA"));
        assertEquals(VerifactuEvidence.ESTADO_RESPUESTA_NO_CONFIRMADA, VerifactuService.estadoRemisionDesdeRespuesta(""));
        assertEquals(VerifactuEvidence.ESTADO_RESPUESTA_NO_CONFIRMADA, VerifactuService.estadoRemisionDesdeRespuesta(null));
    }

    /**
     * error_message es VARCHAR(255): unos avisos largos de AceptadoConErrores no deben romper el
     * guardado de la evidencia después de que la AEAT haya aceptado el registro.
     */
    @Test
    void conAceptadoConErroresLargosElMensajeCabeEnLaColumna() throws Exception {
        String avisos = "ACEPTADA_CON_ERRORES: " + "1100 aviso muy largo; ".repeat(40);
        VerifactuAeatSoapClient cliente = mock(VerifactuAeatSoapClient.class);
        org.mockito.Mockito.when(cliente.enviarFacturaAeat(anyString(), org.mockito.ArgumentMatchers.any()))
                .thenReturn(avisos);
        VerifactuService svc = servicioConRemision(cliente);
        Factura factura = facturaParaEmitir(svc);

        svc.enviarFacturaVerifactu(factura);

        VerifactuEvidence ev = ultimaEvidenciaGuardada();
        assertEquals(VerifactuEvidence.ESTADO_ACEPTADO_CON_ERRORES, ev.getEstado());
        assertTrue(ev.getErrorMessage().length() <= VerifactuService.MAX_ERROR_MESSAGE);
        assertEquals(avisos, ev.getMetadata().get("respuesta_aeat"), "La respuesta completa se conserva en metadata");
    }

    @Test
    void limitarMensajeErrorRespetaLaLongitudDeLaColumna() {
        assertNull(VerifactuService.limitarMensajeError(null));
        assertEquals("corto", VerifactuService.limitarMensajeError("corto"));
        String largo = "x".repeat(300);
        String recortado = VerifactuService.limitarMensajeError(largo);
        assertEquals(VerifactuService.MAX_ERROR_MESSAGE, recortado.length());
        assertTrue(recortado.endsWith("..."));
    }
}
