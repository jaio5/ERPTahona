package alicanteweb.erp.service;

import alicanteweb.erp.entities.CobroFactura;
import alicanteweb.erp.entities.Factura;
import alicanteweb.erp.entities.FacturaCompra;
import alicanteweb.erp.entities.PagoFacturaCompra;
import alicanteweb.erp.repository.CobroFacturaRepository;
import alicanteweb.erp.repository.FacturaCompraRepository;
import alicanteweb.erp.repository.FacturaRepository;
import alicanteweb.erp.repository.PagoFacturaCompraRepository;
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
class CarteraServiceTest {

    @Mock
    FacturaRepository facturaRepository;
    @Mock
    FacturaCompraRepository facturaCompraRepository;
    @Mock
    CobroFacturaRepository cobroRepository;
    @Mock
    PagoFacturaCompraRepository pagoRepository;
    @Mock
    ContabilidadService contabilidadService;
    @Mock
    AuditoriaService auditoriaService;

    @InjectMocks
    CarteraService service;

    private Factura facturaEmitida(BigDecimal total, BigDecimal pagado) {
        Factura f = new Factura();
        f.setId(1L);
        f.setNumero("F-2026-001");
        f.setEstado("EMITIDA");
        f.setTotal(total);
        f.setPagado(pagado);
        return f;
    }

    @Test
    void registrarCobro_parcial_actualizaPagadoSinMarcarPagada() {
        Factura f = facturaEmitida(new BigDecimal("100.00"), BigDecimal.ZERO);
        when(facturaRepository.findById(1L)).thenReturn(Optional.of(f));
        when(cobroRepository.save(any(CobroFactura.class))).thenAnswer(inv -> inv.getArgument(0));

        service.registrarCobro(1L, LocalDate.now(), new BigDecimal("40.00"), "EFECTIVO", null, null, null);

        assertEquals(new BigDecimal("40.00"), f.getPagado());
        assertFalse(f.isPagada());
        verify(contabilidadService).generarAsientoPago(eq(f), eq(new BigDecimal("40.00")), eq("EFECTIVO"), isNull());
        verify(facturaRepository).save(f);
    }

    @Test
    void registrarCobro_total_marcaPagada() {
        Factura f = facturaEmitida(new BigDecimal("100.00"), new BigDecimal("40.00"));
        when(facturaRepository.findById(1L)).thenReturn(Optional.of(f));
        when(cobroRepository.save(any(CobroFactura.class))).thenAnswer(inv -> inv.getArgument(0));

        service.registrarCobro(1L, LocalDate.now(), new BigDecimal("60.00"), "TRANSFERENCIA", "REF-1", null, null);

        assertEquals(new BigDecimal("100.00"), f.getPagado());
        assertTrue(f.isPagada());
    }

    @Test
    void registrarCobro_rechazaImporteSuperiorAlPendiente() {
        Factura f = facturaEmitida(new BigDecimal("100.00"), new BigDecimal("90.00"));
        when(facturaRepository.findById(1L)).thenReturn(Optional.of(f));

        assertThrows(IllegalArgumentException.class, () ->
                service.registrarCobro(1L, LocalDate.now(), new BigDecimal("20.00"), "EFECTIVO", null, null, null));
        verify(cobroRepository, never()).save(any());
        verify(contabilidadService, never()).generarAsientoPago(any(), any(), any(), any());
    }

    @Test
    void registrarCobro_rechazaFacturaNoEmitida() {
        Factura f = facturaEmitida(new BigDecimal("100.00"), BigDecimal.ZERO);
        f.setEstado("BORRADOR");
        when(facturaRepository.findById(1L)).thenReturn(Optional.of(f));

        assertThrows(IllegalArgumentException.class, () ->
                service.registrarCobro(1L, LocalDate.now(), new BigDecimal("10.00"), "EFECTIVO", null, null, null));
    }

    @Test
    void registrarPago_total_marcaFacturaCompraPagada() {
        FacturaCompra fc = new FacturaCompra();
        fc.setId(2L);
        fc.setNumero("FC-77");
        fc.setEstado("PENDIENTE");
        fc.setTotal(new BigDecimal("250.00"));
        fc.setPagado(BigDecimal.ZERO);
        when(facturaCompraRepository.findById(2L)).thenReturn(Optional.of(fc));
        when(pagoRepository.save(any(PagoFacturaCompra.class))).thenAnswer(inv -> inv.getArgument(0));

        service.registrarPago(2L, LocalDate.now(), new BigDecimal("250.00"), "TRANSFERENCIA", null, null, null);

        assertEquals(Boolean.TRUE, fc.getPagada());
        assertEquals("PAGADA", fc.getEstado());
        verify(contabilidadService).generarAsientoPagoCompra(eq(fc), eq(new BigDecimal("250.00")), eq("TRANSFERENCIA"), isNull());
    }

    @Test
    void aging_clasificaPorAntiguedad() {
        var items = List.of(
                new CarteraService.CarteraItem(1L, "A", "X", LocalDate.now(), null, BigDecimal.TEN, BigDecimal.ZERO, new BigDecimal("10.00"), -5),
                new CarteraService.CarteraItem(2L, "B", "X", LocalDate.now(), null, BigDecimal.TEN, BigDecimal.ZERO, new BigDecimal("20.00"), 15),
                new CarteraService.CarteraItem(3L, "C", "X", LocalDate.now(), null, BigDecimal.TEN, BigDecimal.ZERO, new BigDecimal("30.00"), 45),
                new CarteraService.CarteraItem(4L, "D", "X", LocalDate.now(), null, BigDecimal.TEN, BigDecimal.ZERO, new BigDecimal("40.00"), 90));

        var aging = service.aging(items);

        assertEquals(new BigDecimal("10.00"), aging.noVencido());
        assertEquals(new BigDecimal("20.00"), aging.hasta30());
        assertEquals(new BigDecimal("30.00"), aging.hasta60());
        assertEquals(new BigDecimal("40.00"), aging.mas60());
        assertEquals(new BigDecimal("100.00"), aging.total());
    }
}
