package alicanteweb.erp.service;

import alicanteweb.erp.entities.Cliente;
import alicanteweb.erp.entities.Factura;
import alicanteweb.erp.entities.FacturaCompra;
import alicanteweb.erp.entities.FacturaLinea;
import alicanteweb.erp.entities.Proveedor;
import alicanteweb.erp.repository.FacturaCompraRepository;
import alicanteweb.erp.repository.FacturaRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LibroIvaServiceTest {

    @Mock
    FacturaRepository facturaRepository;
    @Mock
    FacturaCompraRepository facturaCompraRepository;

    @InjectMocks
    LibroIvaService service;

    private Factura facturaConLineas() {
        Cliente cliente = new Cliente();
        cliente.setNombre("Bar Pepe");
        cliente.setCif("B12345678");

        Factura f = new Factura();
        f.setNumero("F-1");
        f.setFecha(LocalDate.of(2026, 4, 15));
        f.setEstado("EMITIDA");
        f.setTipoFactura("ORDINARIA");
        f.setCliente(cliente);
        f.setTotal(new BigDecimal("121.00"));

        FacturaLinea l1 = new FacturaLinea();
        l1.setCantidad(new BigDecimal("10"));
        l1.setPrecioUnitario(new BigDecimal("10.00"));
        l1.setIva(new BigDecimal("21"));
        FacturaLinea l2 = new FacturaLinea();
        l2.setCantidad(new BigDecimal("5"));
        l2.setPrecioUnitario(new BigDecimal("2.00"));
        l2.setIva(new BigDecimal("10"));
        f.getFacturaLineas().add(l1);
        f.getFacturaLineas().add(l2);
        return f;
    }

    @Test
    void libroEmitidas_desglosaPorTipoDeIva() {
        when(facturaRepository.findByFechaBetweenAndEstado(any(), any(), any()))
                .thenReturn(List.of(facturaConLineas()));

        var lineas = service.libroEmitidas(LocalDate.of(2026, 4, 1), LocalDate.of(2026, 6, 30));

        assertEquals(2, lineas.size());
        var por10 = lineas.stream().filter(l -> l.tipoIva().compareTo(new BigDecimal("10")) == 0).findFirst().orElseThrow();
        var por21 = lineas.stream().filter(l -> l.tipoIva().compareTo(new BigDecimal("21")) == 0).findFirst().orElseThrow();
        assertEquals(0, por10.base().compareTo(new BigDecimal("10.00")));
        assertEquals(0, por10.cuotaIva().compareTo(new BigDecimal("1.00")));
        assertEquals(0, por21.base().compareTo(new BigDecimal("100.00")));
        assertEquals(0, por21.cuotaIva().compareTo(new BigDecimal("21.00")));
    }

    @Test
    void modelo303_calculaDevengadoDeducibleYResultado() {
        when(facturaRepository.findByFechaBetweenAndEstado(any(), any(), any()))
                .thenReturn(List.of(facturaConLineas()));

        FacturaCompra compra = new FacturaCompra();
        compra.setEstado("PENDIENTE");
        compra.setBaseImponible(new BigDecimal("50.00"));
        compra.setImporteIva(new BigDecimal("5.00"));
        Proveedor prov = new Proveedor();
        prov.setId(1L);
        prov.setNombre("Harinas SA");
        compra.setProveedor(prov);
        when(facturaCompraRepository.findByFechaBetween(any(), any())).thenReturn(List.of(compra));

        var borrador = service.modelo303(2026, 2);

        assertEquals(0, borrador.totalCuotaDevengada().compareTo(new BigDecimal("22.00")));
        assertEquals(0, borrador.cuotaSoportada().compareTo(new BigDecimal("5.00")));
        assertEquals(0, borrador.resultado().compareTo(new BigDecimal("17.00")));
        assertEquals(LocalDate.of(2026, 4, 1), borrador.desde());
        assertEquals(LocalDate.of(2026, 6, 30), borrador.hasta());
    }

    @Test
    void modelo111_cuentaPerceptoresYRetenciones() {
        Proveedor prov = new Proveedor();
        prov.setId(9L);
        prov.setNombre("Asesor SL");

        FacturaCompra conRetencion = new FacturaCompra();
        conRetencion.setEstado("PENDIENTE");
        conRetencion.setProveedor(prov);
        conRetencion.setBaseImponible(new BigDecimal("1000.00"));
        conRetencion.setImporteRetencion(new BigDecimal("150.00"));

        FacturaCompra sinRetencion = new FacturaCompra();
        sinRetencion.setEstado("PENDIENTE");
        sinRetencion.setBaseImponible(new BigDecimal("500.00"));
        sinRetencion.setImporteRetencion(BigDecimal.ZERO);

        when(facturaCompraRepository.findByFechaBetween(any(), any()))
                .thenReturn(List.of(conRetencion, sinRetencion));

        var borrador = service.modelo111(2026, 2);

        assertEquals(1, borrador.numeroPerceptores());
        assertEquals(0, borrador.base().compareTo(new BigDecimal("1000.00")));
        assertEquals(0, borrador.retenciones().compareTo(new BigDecimal("150.00")));
    }
}
