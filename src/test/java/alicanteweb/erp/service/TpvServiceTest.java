package alicanteweb.erp.service;

import alicanteweb.erp.entities.Articulo;
import alicanteweb.erp.entities.Factura;
import alicanteweb.erp.entities.MovimientoCaja;
import alicanteweb.erp.repository.MovimientoCajaRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TpvServiceTest {

    @Mock
    FacturaService facturaService;
    @Mock
    ArticuloService articuloService;
    @Mock
    CarteraService carteraService;
    @Mock
    MovimientoCajaRepository movimientoCajaRepository;
    @Mock
    StockService stockService;

    @InjectMocks
    TpvService service;

    private Articulo barra(BigDecimal pvp) {
        Articulo a = new Articulo();
        a.setId(5L);
        a.setNombre("Barra de pan");
        a.setPvp(pvp);
        a.setIva(new BigDecimal("4"));
        return a;
    }

    @Test
    void vender_emiteSimplificadaCobraYRegistraCaja() {
        when(articuloService.findById(5L)).thenReturn(Optional.of(barra(new BigDecimal("1.20"))));
        // recalcularTotalesDesdeLineas es mock: fijamos el total manualmente
        doAnswer(inv -> {
            Factura f = inv.getArgument(0);
            f.setTotal(new BigDecimal("2.50"));
            return null;
        }).when(facturaService).recalcularTotalesDesdeLineas(any());
        when(facturaService.save(any())).thenAnswer(inv -> {
            Factura f = inv.getArgument(0);
            f.setId(99L);
            return f;
        });
        Factura emitida = new Factura();
        emitida.setId(99L);
        emitida.setNumero("F-TPV-2026-0001");
        emitida.setTotal(new BigDecimal("2.50"));
        when(facturaService.aprobarYEmitir(99L)).thenReturn(emitida);

        var resultado = service.vender(List.of(new TpvService.LineaTpv(5L, new BigDecimal("2"))), "EFECTIVO", null);

        assertEquals("F-TPV-2026-0001", resultado.numero());
        verify(facturaService).pasarARevision(99L);
        verify(carteraService).registrarCobro(eq(99L), any(), eq(new BigDecimal("2.50")),
                eq("EFECTIVO"), eq("TPV"), any(), isNull());
        ArgumentCaptor<MovimientoCaja> mov = ArgumentCaptor.forClass(MovimientoCaja.class);
        verify(movimientoCajaRepository).save(mov.capture());
        assertEquals("INGRESO", mov.getValue().getTipo());
        assertEquals(new BigDecimal("2.50"), mov.getValue().getImporte());

        ArgumentCaptor<Factura> factura = ArgumentCaptor.forClass(Factura.class);
        verify(facturaService).save(factura.capture());
        assertEquals("SIMPLIFICADA", factura.getValue().getTipoFactura());
        assertEquals(TpvService.SERIE_TPV, factura.getValue().getSerie());
    }

    @Test
    void vender_tarjetaNoRegistraMovimientoDeCaja() {
        when(articuloService.findById(5L)).thenReturn(Optional.of(barra(new BigDecimal("1.20"))));
        doAnswer(inv -> {
            ((Factura) inv.getArgument(0)).setTotal(new BigDecimal("1.20"));
            return null;
        }).when(facturaService).recalcularTotalesDesdeLineas(any());
        when(facturaService.save(any())).thenAnswer(inv -> {
            Factura f = inv.getArgument(0);
            f.setId(1L);
            return f;
        });
        Factura emitida = new Factura();
        emitida.setId(1L);
        emitida.setNumero("N");
        emitida.setTotal(new BigDecimal("1.20"));
        when(facturaService.aprobarYEmitir(anyLong())).thenReturn(emitida);

        service.vender(List.of(new TpvService.LineaTpv(5L, BigDecimal.ONE)), "TARJETA", null);

        verify(movimientoCajaRepository, never()).save(any());
    }

    @Test
    void vender_rechazaSimplificadaSuperiorA3000() {
        when(articuloService.findById(5L)).thenReturn(Optional.of(barra(new BigDecimal("4000.00"))));
        doAnswer(inv -> {
            ((Factura) inv.getArgument(0)).setTotal(new BigDecimal("4000.00"));
            return null;
        }).when(facturaService).recalcularTotalesDesdeLineas(any());

        assertThrows(IllegalArgumentException.class, () ->
                service.vender(List.of(new TpvService.LineaTpv(5L, BigDecimal.ONE)), "EFECTIVO", null));
        verify(facturaService, never()).save(any());
    }

    @Test
    void vender_rechazaVentaSinLineas() {
        assertThrows(IllegalArgumentException.class, () -> service.vender(List.of(), "EFECTIVO", null));
    }
}
