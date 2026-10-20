package alicanteweb.erp.service;

import alicanteweb.erp.entities.AlbaranVenta;
import alicanteweb.erp.entities.AlbaranVentaLinea;
import alicanteweb.erp.entities.Articulo;
import alicanteweb.erp.entities.Cliente;
import alicanteweb.erp.entities.Factura;
import alicanteweb.erp.repository.AlbaranVentaFacturaRepository;
import alicanteweb.erp.repository.AlbaranVentaRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AlbaranServiceTest {

    @Mock
    AlbaranVentaRepository albaranRepository;

    @Mock
    AlbaranVentaFacturaRepository albaranFacturaRepository;

    @Mock
    FacturaService facturaService;

    @Mock
    AuditoriaService auditoriaService;

    @Mock
    StockService stockService;

    @Mock
    AlbaranNumeroService albaranNumeroService;

    @InjectMocks
    AlbaranService service;

    @Test
    void duplicarCreaAlbaranNuevoConLineasDelOriginal() {
        int year = LocalDate.now().getYear();
        Cliente cliente = new Cliente();
        cliente.setId(7L);
        Articulo articulo = new Articulo();
        articulo.setId(11L);

        AlbaranVenta original = new AlbaranVenta();
        original.setId(3L);
        original.setNumero("ALB-" + year + "-000010");
        original.setFecha(LocalDate.of(year, 1, 10));
        original.setCliente(cliente);
        original.setObservaciones("Dejar en almacen");

        AlbaranVentaLinea lineaOriginal = new AlbaranVentaLinea();
        lineaOriginal.setId(44L);
        lineaOriginal.setAlbaran(original);
        lineaOriginal.setArticulo(articulo);
        lineaOriginal.setDescripcion("Pan diario");
        lineaOriginal.setCantidad(new BigDecimal("2"));
        lineaOriginal.setPrecio(new BigDecimal("3.50"));
        lineaOriginal.setDescuento(BigDecimal.ZERO);
        lineaOriginal.setIva(new BigDecimal("21"));
        original.getAlbaranVentaLineas().add(lineaOriginal);

        when(albaranRepository.findById(3L)).thenReturn(Optional.of(original));
        when(albaranNumeroService.generarNumero()).thenReturn("ALB-" + year + "-000011");
        when(albaranRepository.save(any(AlbaranVenta.class))).thenAnswer(inv -> inv.getArgument(0));

        AlbaranVenta duplicado = service.duplicar(3L);

        ArgumentCaptor<AlbaranVenta> captor = ArgumentCaptor.forClass(AlbaranVenta.class);
        verify(albaranRepository).save(captor.capture());
        AlbaranVenta guardado = captor.getValue();

        assertSame(guardado, duplicado);
        assertNull(guardado.getId());
        assertEquals("ALB-" + year + "-000011", guardado.getNumero());
        assertEquals(LocalDate.now(), guardado.getFecha());
        assertSame(cliente, guardado.getCliente());
        assertEquals("Dejar en almacen", guardado.getObservaciones());
        assertEquals(new BigDecimal("8.47"), guardado.getTotal());

        assertEquals(1, guardado.getAlbaranVentaLineas().size());
        AlbaranVentaLinea lineaDuplicada = guardado.getAlbaranVentaLineas().iterator().next();
        assertNull(lineaDuplicada.getId());
        assertSame(guardado, lineaDuplicada.getAlbaran());
        assertSame(articulo, lineaDuplicada.getArticulo());
        assertEquals(lineaOriginal.getDescripcion(), lineaDuplicada.getDescripcion());
        assertEquals(lineaOriginal.getCantidad(), lineaDuplicada.getCantidad());
        assertEquals(lineaOriginal.getPrecio(), lineaDuplicada.getPrecio());
        assertEquals(lineaOriginal.getDescuento(), lineaDuplicada.getDescuento());
        assertEquals(lineaOriginal.getIva(), lineaDuplicada.getIva());
    }

    @Test
    void marcarEntregadoDescuentaStockUnaVezYDespuesCambiaEstado() {
        Articulo articulo = new Articulo();
        articulo.setId(11L);

        AlbaranVenta albaran = new AlbaranVenta();
        albaran.setId(3L);
        albaran.setNumero("ALB-2026-000003");
        albaran.setEstado("PENDIENTE");

        AlbaranVentaLinea linea = new AlbaranVentaLinea();
        linea.setAlbaran(albaran);
        linea.setArticulo(articulo);
        linea.setCantidad(new BigDecimal("2.50"));
        albaran.getAlbaranVentaLineas().add(linea);

        when(albaranRepository.findById(3L)).thenReturn(Optional.of(albaran));
        when(albaranRepository.save(albaran)).thenReturn(albaran);

        AlbaranVenta result = service.marcarEntregado(3L);

        verify(stockService).registrarSalida(
                11L, null, new BigDecimal("2.50"),
                "Entrega albaran ALB-2026-000003", "ALBARAN", 3L);
        verify(albaranRepository).save(albaran);
        assertEquals("ENTREGADO", result.getEstado());
    }

    @Test
    void marcarEntregadoNoCambiaEstadoSiFallaElStock() {
        Articulo articulo = new Articulo();
        articulo.setId(11L);

        AlbaranVenta albaran = new AlbaranVenta();
        albaran.setId(3L);
        albaran.setNumero("ALB-2026-000003");
        albaran.setEstado("PENDIENTE");

        AlbaranVentaLinea linea = new AlbaranVentaLinea();
        linea.setAlbaran(albaran);
        linea.setArticulo(articulo);
        linea.setCantidad(BigDecimal.ONE);
        albaran.getAlbaranVentaLineas().add(linea);

        when(albaranRepository.findById(3L)).thenReturn(Optional.of(albaran));
        doThrow(new IllegalStateException("Stock insuficiente"))
                .when(stockService).registrarSalida(anyLong(), any(), any(), anyString(), anyString(), anyLong());

        assertThrows(IllegalStateException.class, () -> service.marcarEntregado(3L));
        assertEquals("PENDIENTE", albaran.getEstado());
        verify(albaranRepository, never()).save(albaran);
    }

    @Test
    void facturarCuatroAlbaranesGuardaTodosLosNumerosSinDesbordar() {
        Cliente cliente = new Cliente();
        cliente.setId(7L);
        List<AlbaranVenta> albaranes = new ArrayList<>();
        for (long i = 1; i <= 4; i++) {
            AlbaranVenta a = new AlbaranVenta();
            a.setId(i);
            a.setNumero("ALB-2026-00000" + i);
            a.setCliente(cliente);
            albaranes.add(a);
        }
        List<Long> ids = List.of(1L, 2L, 3L, 4L);
        when(albaranRepository.findAllById(ids)).thenReturn(albaranes);
        when(facturaService.save(any(Factura.class))).thenAnswer(inv -> inv.getArgument(0));

        Factura factura = service.convertirVariosAFactura(ids, null);

        assertEquals("ALB-2026-000001, ALB-2026-000002, ALB-2026-000003, ALB-2026-000004",
                factura.getNumeroAlbaran());
        assertTrue(factura.getNumeroAlbaran().length() <= Factura.NUMERO_ALBARAN_MAX);
    }

    @Test
    void resumirNumerosAlbaranRecortaConContadorCuandoNoCabenTodos() {
        List<String> numeros = new ArrayList<>();
        for (int i = 1; i <= 100; i++) {
            numeros.add(String.format("ALB-2026-%06d", i));
        }

        String resumen = AlbaranService.resumirNumerosAlbaran(numeros, Factura.NUMERO_ALBARAN_MAX);

        assertTrue(resumen.length() <= Factura.NUMERO_ALBARAN_MAX, resumen);
        assertTrue(resumen.startsWith("ALB-2026-000001, ALB-2026-000002"));
        int incluidos = resumen.split(", ").length;
        assertTrue(resumen.endsWith(" (+" + (100 - incluidos) + " más)"), resumen);
    }

    @Test
    void resumirNumerosAlbaranDevuelveListaCompletaSiCabe() {
        assertEquals("A1, A2", AlbaranService.resumirNumerosAlbaran(List.of("A1", "A2"), 50));
        assertNull(AlbaranService.resumirNumerosAlbaran(List.of(), 50));
    }
}
