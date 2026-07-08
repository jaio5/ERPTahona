package alicanteweb.erp.service;

import alicanteweb.erp.entities.CampoPersonalizado;
import alicanteweb.erp.entities.Cliente;
import alicanteweb.erp.entities.EmpresaConfig;
import alicanteweb.erp.entities.Factura;
import alicanteweb.erp.entities.FacturaLinea;
import alicanteweb.erp.repository.CampoPersonalizadoRepository;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfReader;
import com.itextpdf.kernel.pdf.canvas.parser.PdfTextExtractor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

/** End-to-end: un campo personalizado configurado aparece en el PDF generado. */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class CampoPersonalizadoRenderTest {

    @Autowired ImpresionService impresionService;
    @Autowired CampoPersonalizadoRepository campoRepository;
    @MockBean EmpresaConfigService empresaConfigService;

    private EmpresaConfig empresa() {
        EmpresaConfig e = new EmpresaConfig();
        e.setNombreComercial("Tahona de Fernando");
        e.setNombreEmpresa("Grupo BABO, S.C.V.L.");
        e.setCif("F-54059985");
        return e;
    }

    private Factura factura() {
        Factura f = new Factura();
        f.setNumero("F-CAMPO");
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

    private String textoPdf(File pdf) throws Exception {
        try (PdfDocument doc = new PdfDocument(new PdfReader(pdf.getAbsolutePath()))) {
            StringBuilder sb = new StringBuilder();
            for (int i = 1; i <= doc.getNumberOfPages(); i++) {
                sb.append(PdfTextExtractor.getTextFromPage(doc.getPage(i)));
            }
            return sb.toString();
        }
    }

    @Test
    void elCampoGlobalApareceEnLaFactura() throws Exception {
        when(empresaConfigService.getConfiguracionActivaOrThrow()).thenReturn(empresa());
        when(empresaConfigService.getConfiguracionActiva()).thenReturn(Optional.of(empresa()));

        CampoPersonalizado campo = new CampoPersonalizado();
        campo.setAmbito(CampoPersonalizado.AMBITO_EMPRESA);
        campo.setEtiqueta("Forma de pago");
        campo.setValor("PAGO A 30 DIAS FIN DE MES");
        campo.setUbicacion(CampoPersonalizado.UB_PIE);
        campo.setDocumento(CampoPersonalizado.DOC_AMBOS);
        campo.setActivo(true);
        campoRepository.saveAndFlush(campo);

        File pdf = impresionService.generarFacturaPdf(factura());
        String texto = textoPdf(pdf);

        assertTrue(texto.contains("PAGO A 30 DIAS FIN DE MES"),
                "El campo personalizado debe imprimirse en la factura");
    }
}
