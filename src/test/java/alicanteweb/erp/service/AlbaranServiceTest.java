package alicanteweb.erp.service;

import alicanteweb.erp.entities.AlbaranVenta;
import alicanteweb.erp.entities.AlbaranVentaLinea;
import alicanteweb.erp.entities.Articulo;
import alicanteweb.erp.entities.Cliente;
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
}
