package alicanteweb.erp.service;

import alicanteweb.erp.entities.AlbaranVenta;
import alicanteweb.erp.entities.AlbaranVentaLinea;
import alicanteweb.erp.entities.Cliente;
import alicanteweb.erp.entities.EmpresaConfig;
import alicanteweb.erp.entities.Factura;
import alicanteweb.erp.entities.FacturaLinea;
import alicanteweb.erp.entities.SifModalidad;
import com.itextpdf.kernel.geom.Rectangle;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfReader;
import com.itextpdf.kernel.pdf.canvas.parser.EventType;
import com.itextpdf.kernel.pdf.canvas.parser.PdfCanvasProcessor;
import com.itextpdf.kernel.pdf.canvas.parser.PdfTextExtractor;
import com.itextpdf.kernel.pdf.canvas.parser.data.IEventData;
import com.itextpdf.kernel.pdf.canvas.parser.data.TextRenderInfo;
import com.itextpdf.kernel.pdf.canvas.parser.listener.IEventListener;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import java.io.File;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

/**
 * Regresión del formato de impresión:
 *  - el nombre de empresa se imprime con la fuente caligráfica (Pinyon Script) embebida;
 *  - el albarán respeta el tamaño de página configurado (A5 media hoja / A4 folio).
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class ImpresionServiceFormatoTest {

    // Tamaños de página en puntos PDF (1/72"): A4 = 595x842, A5 = 420x595.
    private static final float A4_W = 595f, A4_H = 842f, A5_W = 420f, A5_H = 595f, TOL = 2f;

    @Autowired ImpresionService impresionService;
    @Autowired SpringTemplateEngine templateEngine;
    @MockBean EmpresaConfigService empresaConfigService;

    private EmpresaConfig empresa(boolean medioFolio) {
        EmpresaConfig e = new EmpresaConfig();
        e.setNombreComercial("La Tahona de Fernando");
        e.setNombreEmpresa("Grupo BABO, S.C.V.L.");
        e.setCif("F-54059985");
        e.setAlbaranMedioFolio(medioFolio);
        return e;
    }

    private Factura factura() {
        Factura f = new Factura();
        f.setNumero("F-FMT");
        f.setFecha(LocalDate.of(2026, 5, 31));
        f.setVerifactuEnviada(false);
        Cliente c = new Cliente();
        c.setNombre("CLIENTE PRUEBA");
        c.setCodigo("C1");
        f.setCliente(c);
        FacturaLinea l = new FacturaLinea();
        l.setDescripcion("PAN");
        l.setCantidad(new BigDecimal("1"));
        l.setPrecioUnitario(new BigDecimal("1.00"));
        l.setIva(new BigDecimal("4"));
        l.setTotal(new BigDecimal("1.00"));
        f.getFacturaLineas().add(l);
        f.setBaseImponible(new BigDecimal("1.00"));
        f.setTotalIva(new BigDecimal("0.04"));
        f.setTotal(new BigDecimal("1.04"));
        return f;
    }

    /**
     * Los campos de texto en blanco del formulario llegan como cadena vacia, no como null, y la
     * caja de cliente del PDF debe tratarlos igual: sin lineas vacias ni etiquetas huerfanas.
     */
    @Test
    void losCamposDeTextoEnBlancoSeImprimenIgualQueLosNulos() throws Exception {
        when(empresaConfigService.getConfiguracionActivaOrThrow()).thenReturn(empresa(false));
        when(empresaConfigService.getConfiguracionActiva()).thenReturn(Optional.of(empresa(false)));

        String conNulos = textoPrimeraPagina(impresionService.generarFacturaPdf(factura()));

        Factura conVacios = factura();
        Cliente c = conVacios.getCliente();
        c.setDireccion("");
        c.setPoblacion("");
        c.setCodigoPostal("");
        c.setProvincia("");
        c.setCif("");
        c.setRepresentante("");
        String textoConVacios = textoPrimeraPagina(impresionService.generarFacturaPdf(conVacios));

        assertEquals(conNulos, textoConVacios,
                "Una cadena vacia debe imprimirse como si el campo no existiera");
    }

    private AlbaranVenta albaran() {
        AlbaranVenta a = new AlbaranVenta();
        a.setNumero("A-FMT");
        a.setFecha(LocalDate.of(2026, 3, 27));
        Cliente c = new Cliente();
        c.setNombre("CLIENTE PRUEBA");
        a.setCliente(c);
        AlbaranVentaLinea l = new AlbaranVentaLinea();
        l.setDescripcion("PAN");
        l.setCantidad(new BigDecimal("10"));
        l.setPrecio(new BigDecimal("1.00"));
        l.setIva(new BigDecimal("4"));
        a.getAlbaranVentaLineas().add(l);
        return a;
    }

    private String textoPrimeraPagina(File pdf) throws Exception {
        try (PdfDocument doc = new PdfDocument(new PdfReader(pdf.getAbsolutePath()))) {
            return PdfTextExtractor.getTextFromPage(doc.getPage(1));
        }
    }

    private Rectangle primeraPagina(File pdf) throws Exception {
        try (PdfDocument doc = new PdfDocument(new PdfReader(pdf.getAbsolutePath()))) {
            return doc.getPage(1).getPageSize();
        }
    }

    @Test
    void laFuenteCaligraficaSeEmbebeEnLaFactura() throws Exception {
        when(empresaConfigService.getConfiguracionActivaOrThrow()).thenReturn(empresa(false));
        when(empresaConfigService.getConfiguracionActiva()).thenReturn(Optional.of(empresa(false)));

        File pdf = impresionService.generarFacturaPdf(factura());

        // El nombre de la fuente embebida (p. ej. /ABCDEF+PinyonScript-Regular) aparece en el PDF;
        // si hubiera caído en la fuente por defecto (Times), "PinyonScript" no estaría.
        String contenido = new String(Files.readAllBytes(pdf.toPath()), StandardCharsets.ISO_8859_1);
        assertTrue(contenido.contains("PinyonScript"),
                "La fuente caligráfica del nombre de empresa debe quedar embebida en la factura");
    }

    /**
     * Texto impreso con la fuente caligrafica de la marca. Mirar el texto plano de la pagina
     * no sirve: el nombre fiscal tambien aparece en el bloque del emisor, asi que un hueco en
     * la cabecera pasaria desapercibido.
     */
    private String textoDeLaMarca(File pdf) throws Exception {
        StringBuilder sb = new StringBuilder();
        try (PdfDocument doc = new PdfDocument(new PdfReader(pdf.getAbsolutePath()))) {
            IEventListener listener = new IEventListener() {
                @Override
                public void eventOccurred(IEventData data, EventType type) {
                    if (data instanceof TextRenderInfo info) {
                        String fuente = info.getFont().getFontProgram().getFontNames().getFontName();
                        if (fuente != null && fuente.contains("Pinyon")) sb.append(info.getText());
                    }
                }
                @Override
                public Set<EventType> getSupportedEvents() {
                    return Set.of(EventType.RENDER_TEXT);
                }
            };
            new PdfCanvasProcessor(listener).processPageContent(doc.getPage(1));
        }
        return sb.toString().trim();
    }

        @Test
    void laCaligrafiaMuestraSoloElNombreComercialEnFacturaYAlbaran() throws Exception {
        when(empresaConfigService.getConfiguracionActivaOrThrow()).thenReturn(empresa(false));
        when(empresaConfigService.getConfiguracionActiva()).thenReturn(Optional.of(empresa(false)));

        File factura = impresionService.generarFacturaPdf(factura());
        File albaran = impresionService.generarAlbaranPdf(albaran());
        assertEquals("La Tahona de Fernando", textoDeLaMarca(factura));
        assertEquals("La Tahona de Fernando", textoDeLaMarca(albaran));
        // La razón social sigue impresa, en letra normal, bajo el escudo
        assertTrue(textoPrimeraPagina(factura).contains("Grupo BABO, S.C.V.L."));
        assertTrue(textoPrimeraPagina(albaran).contains("Grupo BABO, S.C.V.L."));
    }

    /**
     * Sin nombre comercial no se escribe nada en caligrafía: la razón social no es la marca
     * y ya aparece bajo el escudo.
     */
    @Test
    void conNombreComercialEnBlancoNoSeEscribeLaRazonSocialEnCaligrafia() throws Exception {
        EmpresaConfig sinComercial = empresa(false);
        sinComercial.setNombreComercial("");
        when(empresaConfigService.getConfiguracionActivaOrThrow()).thenReturn(sinComercial);
        when(empresaConfigService.getConfiguracionActiva()).thenReturn(Optional.of(sinComercial));

        File factura = impresionService.generarFacturaPdf(factura());
        File albaran = impresionService.generarAlbaranPdf(albaran());
        assertEquals("", textoDeLaMarca(factura));
        assertEquals("", textoDeLaMarca(albaran));
        assertTrue(textoPrimeraPagina(factura).contains("Grupo BABO, S.C.V.L."));
        assertTrue(textoPrimeraPagina(albaran).contains("Grupo BABO, S.C.V.L."));
    }

    @Test
    void laFuenteCaligraficaSeEmbebeEnElAlbaran() throws Exception {
        when(empresaConfigService.getConfiguracionActivaOrThrow()).thenReturn(empresa(false));
        when(empresaConfigService.getConfiguracionActiva()).thenReturn(Optional.of(empresa(false)));

        File pdf = impresionService.generarAlbaranPdf(albaran());

        String contenido = new String(Files.readAllBytes(pdf.toPath()), StandardCharsets.ISO_8859_1);
        assertTrue(contenido.contains("PinyonScript"),
                "La fuente caligráfica del nombre de empresa debe quedar embebida en el albarán");
    }

    @Test
    void elAlbaranEnMedioFolioSaleEnA5() throws Exception {
        when(empresaConfigService.getConfiguracionActivaOrThrow()).thenReturn(empresa(true));
        when(empresaConfigService.getConfiguracionActiva()).thenReturn(Optional.of(empresa(true)));

        Rectangle size = primeraPagina(impresionService.generarAlbaranPdf(albaran()));
        assertEquals(A5_W, size.getWidth(), TOL, "ancho A5");
        assertEquals(A5_H, size.getHeight(), TOL, "alto A5");
    }

    @Test
    void elAlbaranNormalSaleEnA4() throws Exception {
        when(empresaConfigService.getConfiguracionActivaOrThrow()).thenReturn(empresa(false));
        when(empresaConfigService.getConfiguracionActiva()).thenReturn(Optional.of(empresa(false)));

        Rectangle size = primeraPagina(impresionService.generarAlbaranPdf(albaran()));
        assertEquals(A4_W, size.getWidth(), TOL, "ancho A4");
        assertEquals(A4_H, size.getHeight(), TOL, "alto A4");
    }

    // PNG 1x1 transparente: basta para que la plantilla pinte el bloque del QR
    private static final String QR_MINIMO =
        "iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mNkYPhfDwAChwGA60e6kgAAAABJRU5ErkJggg==";

    private String textoFacturaEmitida(SifModalidad modalidad) throws Exception {
        EmpresaConfig e = empresa(false);
        e.setSifModalidad(modalidad);
        when(empresaConfigService.getConfiguracionActivaOrThrow()).thenReturn(e);
        when(empresaConfigService.getConfiguracionActiva()).thenReturn(Optional.of(e));
        Factura f = factura();
        f.setVerifactuEnviada(true);
        f.setVerifactuQr(QR_MINIMO);
        f.setVerifactuHash("ABCDEF0123456789ABCDEF0123456789");
        return textoPrimeraPagina(impresionService.generarFacturaPdf(f));
    }

    @Test
    void enModoNoVerifactuLaFacturaNoLlevaLeyendaVerifactu() throws Exception {
        String texto = textoFacturaEmitida(SifModalidad.NO_VERIFACTU);
        assertFalse(texto.contains("VERI*FACTU"), texto);
        assertTrue(texto.contains("QR tributario del sistema"), texto);
        assertTrue(texto.contains("SISTEMA:NO_VERIFACTU"), texto);
    }

    @Test
    void enModoVerifactuLaFacturaEmitidaLlevaLaLeyenda() throws Exception {
        String texto = textoFacturaEmitida(SifModalidad.VERIFACTU);
        assertTrue(texto.contains("VERI*FACTU"), texto);
        assertTrue(texto.contains("SISTEMA:VERIFACTU"), texto);
    }

    // ===== Importe de línea del albarán neto de descuento =====

    /** Cliente con 10 % de tarifa: 10 barras a 1,00 € -> la línea vale 9,00 y cuadra con la base. */
    private AlbaranVenta albaranConDescuento() {
        AlbaranVenta a = albaran();
        AlbaranVentaLinea l = a.getAlbaranVentaLineas().iterator().next();
        l.setDescripcion("BARRA");
        l.setDescuento(new BigDecimal("10"));
        l.setDescuentoTipo("PORCENTAJE");
        return a;
    }

    private String htmlAlbaran(AlbaranVenta a) {
        // Mismas variables que ImpresionService.generarAlbaranPdf; el resumen con DesgloseFiscal
        List<alicanteweb.erp.util.DesgloseFiscal.Linea> lineas = a.getAlbaranVentaLineas().stream()
                .map(l -> new alicanteweb.erp.util.DesgloseFiscal.Linea(l.getCantidad(), l.getPrecio(), l.getIva(),
                        l.getDescuentoTipo(), l.getDescuento()))
                .toList();
        var r = alicanteweb.erp.util.DesgloseFiscal.calcular(lineas, a.getDescuentoGlobalTipo(), a.getDescuentoGlobalValor());
        Context ctx = new Context(new java.util.Locale("es", "ES"));
        ctx.setVariables(Map.of(
                "albaran", a,
                "empresa", empresa(false),
                "medioFolio", false,
                "resumen", Map.of("base", r.base(), "iva", r.iva(), "descuentoGlobal", r.descuentoGlobal(), "total", r.total()),
                "campos", List.of(),
                "logo", "",
                "now", LocalDateTime.now()));
        return templateEngine.process("pdf/albaran", ctx);
    }

    private static int ocurrencias(String texto, String buscado) {
        int n = 0;
        for (int i = texto.indexOf(buscado); i >= 0; i = texto.indexOf(buscado, i + 1)) n++;
        return n;
    }

    @Test
    void elImporteDeLineaDelAlbaranEsNetoDeDescuentoYCuadraConLaBase() throws Exception {
        String html = htmlAlbaran(albaranConDescuento());
        // Línea: 9,00 (no 10,00) y columna Dto. con el 10 %; base imponible 9,00
        assertTrue(html.contains("DTO."), "Debe aparecer la columna de descuento");
        assertTrue(html.contains("10,00 %"), html);
        assertTrue(ocurrencias(html, ">9,00<") >= 2, "Línea y base deben decir 9,00: " + html);

        when(empresaConfigService.getConfiguracionActivaOrThrow()).thenReturn(empresa(false));
        when(empresaConfigService.getConfiguracionActiva()).thenReturn(Optional.of(empresa(false)));
        String texto = textoPrimeraPagina(impresionService.generarAlbaranPdf(albaranConDescuento()));
        assertTrue(ocurrencias(texto, "9,00") >= 2, "En el PDF la línea y la base deben decir 9,00: " + texto);
        assertTrue(texto.contains("9,36"), "Total con IVA 4 %: " + texto);
    }

    @Test
    void elDescuentoEnImporteFijoSeRestaDeLaLinea() {
        AlbaranVenta a = albaran();
        AlbaranVentaLinea l = a.getAlbaranVentaLineas().iterator().next();
        l.setDescuento(new BigDecimal("1.50"));
        l.setDescuentoTipo("IMPORTE");

        String html = htmlAlbaran(a);
        assertTrue(html.contains("1,50 €"), html);
        assertTrue(ocurrencias(html, ">8,50<") >= 2, "Línea y base deben decir 8,50: " + html);
    }

    @Test
    void sinDescuentosNoHayColumnaDtoYElImporteEsCantidadPorPrecio() {
        String html = htmlAlbaran(albaran());
        assertFalse(html.contains("DTO."), "Sin descuentos no se muestra la columna");
        assertTrue(ocurrencias(html, ">10,00<") >= 3, "Cantidad, importe y base dicen 10,00: " + html);
    }
}
