package alicanteweb.erp.service;

import alicanteweb.erp.entities.Cliente;
import alicanteweb.erp.entities.EmpresaConfig;
import alicanteweb.erp.entities.Factura;
import alicanteweb.erp.entities.FacturaLinea;
import alicanteweb.erp.repository.FacturaRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FacturaeServiceTest {

    @Mock
    FacturaRepository facturaRepository;
    @Mock
    EmpresaConfigService empresaConfigService;

    @InjectMocks
    FacturaeService service;

    private Factura facturaEmitida() {
        Cliente cliente = new Cliente();
        cliente.setNombre("Ayuntamiento de Alicante");
        cliente.setCif("P0301400H");
        Factura f = new Factura();
        f.setId(1L);
        f.setNumero("F-A-2026-0001");
        f.setSerie("A");
        f.setFecha(LocalDate.of(2026, 7, 1));
        f.setEstado("EMITIDA");
        f.setTipoFactura("ORDINARIA");
        f.setCliente(cliente);
        f.setTotal(new BigDecimal("121.00"));
        FacturaLinea linea = new FacturaLinea();
        linea.setDescripcion("Suministro de pan");
        linea.setCantidad(new BigDecimal("100"));
        linea.setPrecioUnitario(new BigDecimal("1.00"));
        linea.setIva(new BigDecimal("21"));
        f.getFacturaLineas().add(linea);
        return f;
    }

    private EmpresaConfig empresa() {
        EmpresaConfig e = new EmpresaConfig();
        e.setNombreEmpresa("Panadería Tahona SL");
        e.setCif("B03123456");
        e.setDireccion("C/ Horno 1");
        e.setCodigoPostal("03001");
        e.setCiudad("Alicante");
        e.setProvincia("Alicante");
        return e;
    }

    @Test
    void generaFacturaeConEstructuraBasica() {
        when(facturaRepository.findById(1L)).thenReturn(Optional.of(facturaEmitida()));
        when(empresaConfigService.getConfiguracionActiva()).thenReturn(Optional.of(empresa()));

        String xml = service.generarFacturaeXml(1L);

        assertTrue(xml.contains("<SchemaVersion>3.2.2</SchemaVersion>"));
        assertTrue(xml.contains("<TaxIdentificationNumber>B03123456</TaxIdentificationNumber>"));
        assertTrue(xml.contains("<TaxIdentificationNumber>P0301400H</TaxIdentificationNumber>"));
        assertTrue(xml.contains("<InvoiceNumber>F-A-2026-0001</InvoiceNumber>"));
        assertTrue(xml.contains("<TaxRate>21.00</TaxRate>"));
        assertTrue(xml.contains("<TaxableBase><TotalAmount>100.00</TotalAmount></TaxableBase>"));
        assertTrue(xml.contains("<TaxAmount><TotalAmount>21.00</TotalAmount></TaxAmount>"));
        assertTrue(xml.contains("<InvoiceTotal>121.00</InvoiceTotal>"));
        assertTrue(xml.contains("<ItemDescription>Suministro de pan</ItemDescription>"));
    }

    @Test
    void descuentoGlobalSeModelaComoGeneralDiscountsYCuadra() {
        Factura f = facturaEmitida();
        // 10% de descuento global sobre 100 -> base neta 90, IVA 18,90, total 108,90.
        f.setDescuentoGlobalTipo("PORCENTAJE");
        f.setDescuentoGlobalValor(new BigDecimal("10"));
        f.setTotal(new BigDecimal("108.90"));
        when(facturaRepository.findById(1L)).thenReturn(Optional.of(f));
        when(empresaConfigService.getConfiguracionActiva()).thenReturn(Optional.of(empresa()));

        String xml = service.generarFacturaeXml(1L);

        // Base bruta (100) - descuento general (10) = base neta (90); 90 + IVA 18,90 = total 108,90.
        assertTrue(xml.contains("<TotalGrossAmount>100.00</TotalGrossAmount>"));
        assertTrue(xml.contains("<TotalGeneralDiscounts>10.00</TotalGeneralDiscounts>"));
        assertTrue(xml.contains("<DiscountAmount>10.00</DiscountAmount>"));
        assertTrue(xml.contains("<TotalGrossAmountBeforeTaxes>90.00</TotalGrossAmountBeforeTaxes>"));
        assertTrue(xml.contains("<TaxableBase><TotalAmount>90.00</TotalAmount></TaxableBase>"));
        assertTrue(xml.contains("<TotalTaxOutputs>18.90</TotalTaxOutputs>"));
        assertTrue(xml.contains("<InvoiceTotal>108.90</InvoiceTotal>"));
    }

    @Test
    void rechazaFacturaNoEmitida() {
        Factura borrador = facturaEmitida();
        borrador.setEstado("BORRADOR");
        when(facturaRepository.findById(1L)).thenReturn(Optional.of(borrador));

        assertThrows(IllegalStateException.class, () -> service.generarFacturaeXml(1L));
    }

    @Test
    void rechazaClienteSinNif() {
        Factura sinNif = facturaEmitida();
        sinNif.getCliente().setCif(null);
        when(facturaRepository.findById(1L)).thenReturn(Optional.of(sinNif));

        assertThrows(IllegalStateException.class, () -> service.generarFacturaeXml(1L));
    }
}
