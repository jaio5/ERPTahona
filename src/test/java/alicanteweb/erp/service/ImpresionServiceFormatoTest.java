package alicanteweb.erp.service;

import alicanteweb.erp.entities.AlbaranVenta;
import alicanteweb.erp.entities.AlbaranVentaLinea;
import alicanteweb.erp.entities.Cliente;
import alicanteweb.erp.entities.EmpresaConfig;
import alicanteweb.erp.entities.Factura;
import alicanteweb.erp.entities.FacturaLinea;
import com.itextpdf.kernel.geom.Rectangle;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfReader;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
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
}
