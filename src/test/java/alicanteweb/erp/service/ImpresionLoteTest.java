package alicanteweb.erp.service;

import alicanteweb.erp.entities.Cliente;
import alicanteweb.erp.entities.EmpresaConfig;
import alicanteweb.erp.entities.Factura;
import alicanteweb.erp.entities.FacturaLinea;
import alicanteweb.erp.exception.ErpException;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfReader;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;

import java.io.ByteArrayInputStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

/** Impresión por lotes: el PDF combinado contiene la suma de páginas de los documentos. */
@SpringBootTest
@ActiveProfiles("test")
class ImpresionLoteTest {

    @Autowired ImpresionService impresionService;
    @MockBean EmpresaConfigService empresaConfigService;

    private EmpresaConfig empresa() {
        EmpresaConfig e = new EmpresaConfig();
        e.setNombreComercial("Tahona de Fernando");
        e.setNombreEmpresa("Grupo BABO, S.C.V.L.");
        e.setCif("F-54059985");
        return e;
    }

    private Factura factura(String numero) {
        Factura f = new Factura();
        f.setNumero(numero);
        f.setFecha(LocalDate.of(2026, 5, 31));
        f.setVerifactuEnviada(false);
        Cliente c = new Cliente();
        c.setNombre("CLIENTE " + numero);
        c.setCodigo("C" + numero);
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

    private int paginas(byte[] pdf) throws Exception {
        try (PdfDocument doc = new PdfDocument(new PdfReader(new ByteArrayInputStream(pdf)))) {
            return doc.getNumberOfPages();
        }
    }

    @Test
    void combinaVariasFacturasEnUnPdf() throws Exception {
        when(empresaConfigService.getConfiguracionActivaOrThrow()).thenReturn(empresa());
        when(empresaConfigService.getConfiguracionActiva()).thenReturn(Optional.of(empresa()));

        byte[] pdf = impresionService.generarFacturasLotePdf(
                List.of(factura("F-1"), factura("F-2"), factura("F-3")));

        // Cada factura de ejemplo ocupa 1 página -> 3 páginas combinadas.
        assertEquals(3, paginas(pdf));
    }

    @Test
    void loteVacioLanzaError() {
        assertThrows(ErpException.class, () -> impresionService.generarFacturasLotePdf(List.of()));
    }
}
