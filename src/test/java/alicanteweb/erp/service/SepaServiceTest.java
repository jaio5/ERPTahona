package alicanteweb.erp.service;

import alicanteweb.erp.entities.Cliente;
import alicanteweb.erp.entities.EmpresaConfig;
import alicanteweb.erp.entities.Factura;
import alicanteweb.erp.entities.RemesaSepa;
import alicanteweb.erp.entities.RemesaSepaLinea;
import alicanteweb.erp.repository.FacturaRepository;
import alicanteweb.erp.repository.RemesaSepaRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SepaServiceTest {

    @Mock
    RemesaSepaRepository remesaRepository;
    @Mock
    FacturaRepository facturaRepository;
    @Mock
    CarteraService carteraService;
    @Mock
    EmpresaConfigService empresaConfigService;
    @Mock
    AuditoriaService auditoriaService;

    @InjectMocks
    SepaService service;

    private EmpresaConfig empresaConSepa() {
        EmpresaConfig e = new EmpresaConfig();
        e.setNombreEmpresa("Panadería & Tahona SL");
        e.setIban("ES9121000418450200051332");
        e.setSepaCreditorId("ES12000B12345678");
        return e;
    }

    private Factura facturaConMandato(Long id, String numero, BigDecimal total) {
        Cliente c = new Cliente();
        c.setNombre("Bar Pepe");
        c.setIban("ES7620770024003102575766");
        c.setMandatoSepaReferencia("MAND-001");
        c.setMandatoSepaFecha(LocalDate.of(2026, 1, 10));
        Factura f = new Factura();
        f.setId(id);
        f.setNumero(numero);
        f.setEstado("EMITIDA");
        f.setTotal(total);
        f.setPagado(BigDecimal.ZERO);
        f.setCliente(c);
        return f;
    }

    @Test
    void crearRemesa_generaXmlPain008ConRecibos() {
        when(empresaConfigService.getConfiguracionActiva()).thenReturn(Optional.of(empresaConSepa()));
        when(remesaRepository.facturasEnRemesasVivas()).thenReturn(List.of());
        when(facturaRepository.findById(1L)).thenReturn(Optional.of(facturaConMandato(1L, "F-1", new BigDecimal("60.50"))));
        when(remesaRepository.save(any(RemesaSepa.class))).thenAnswer(inv -> {
            RemesaSepa r = inv.getArgument(0);
            if (r.getId() == null) r.setId(7L);
            return r;
        });

        RemesaSepa remesa = service.crearRemesa(List.of(1L), LocalDate.of(2026, 7, 10), "Pan reparto", null);

        assertEquals(1, remesa.getNumRecibos());
        assertEquals(0, remesa.getTotal().compareTo(new BigDecimal("60.50")));
        String xml = remesa.getXml();
        assertNotNull(xml);
        assertTrue(xml.contains("pain.008.001.02"));
        assertTrue(xml.contains("<PmtMtd>DD</PmtMtd>"));
        assertTrue(xml.contains("<IBAN>ES9121000418450200051332</IBAN>"));
        assertTrue(xml.contains("<IBAN>ES7620770024003102575766</IBAN>"));
        assertTrue(xml.contains("<MndtId>MAND-001</MndtId>"));
        assertTrue(xml.contains("<InstdAmt Ccy=\"EUR\">60.50</InstdAmt>"));
        assertTrue(xml.contains("<ReqdColltnDt>2026-07-10</ReqdColltnDt>"));
        assertTrue(xml.contains("Panadería &amp; Tahona SL"));
    }

    @Test
    void crearRemesa_rechazaSinIbanDeEmpresa() {
        EmpresaConfig sinIban = new EmpresaConfig();
        sinIban.setNombreEmpresa("X");
        when(empresaConfigService.getConfiguracionActiva()).thenReturn(Optional.of(sinIban));

        assertThrows(IllegalStateException.class,
                () -> service.crearRemesa(List.of(1L), null, null, null));
    }

    @Test
    void crearRemesa_rechazaFacturaSinMandato() {
        when(empresaConfigService.getConfiguracionActiva()).thenReturn(Optional.of(empresaConSepa()));
        when(remesaRepository.facturasEnRemesasVivas()).thenReturn(List.of());
        Factura sinMandato = facturaConMandato(1L, "F-1", BigDecimal.TEN);
        sinMandato.getCliente().setMandatoSepaReferencia(null);
        when(facturaRepository.findById(1L)).thenReturn(Optional.of(sinMandato));

        assertThrows(IllegalArgumentException.class,
                () -> service.crearRemesa(List.of(1L), null, null, null));
    }

    @Test
    void marcarCobrada_registraCobrosEnCartera() {
        RemesaSepa remesa = new RemesaSepa();
        remesa.setId(3L);
        remesa.setEstado("GENERADA");
        remesa.setNumRecibos(1);
        Factura factura = facturaConMandato(1L, "F-1", new BigDecimal("25.00"));
        RemesaSepaLinea linea = new RemesaSepaLinea();
        linea.setFactura(factura);
        linea.setImporte(new BigDecimal("25.00"));
        remesa.getLineas().add(linea);
        when(remesaRepository.findById(3L)).thenReturn(Optional.of(remesa));
        when(remesaRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        service.marcarCobrada(3L, null);

        verify(carteraService).registrarCobro(eq(1L), any(), eq(new BigDecimal("25.00")),
                eq("RECIBO"), eq("Remesa SEPA 3"), isNull(), isNull());
        assertEquals("COBRADA", remesa.getEstado());
    }
}
