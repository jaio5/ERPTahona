package alicanteweb.erp.service;

import alicanteweb.erp.entities.*;
import alicanteweb.erp.repository.AlbaranVentaRepository;
import alicanteweb.erp.repository.FacturaRepository;
import alicanteweb.erp.repository.PedidoRepository;
import alicanteweb.erp.repository.PresupuestoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DocumentoServiceTest {

    @Mock PedidoService pedidoService;
    @Mock AlbaranService albaranService;
    @Mock FacturaService facturaService;
    @Mock ArticuloService articuloService;
    @Mock ClienteService clienteService;
    @Mock AlmacenService almacenService;
    @Mock AlbaranVentaRepository albaranRepository;
    @Mock FacturaRepository facturaRepository;
    @Mock PedidoRepository pedidoRepository;
    @Mock PresupuestoRepository presupuestoRepository;
    @Mock TarifaClienteService tarifaClienteService;
    DocumentoService service;

    Cliente cliente;
    Articulo articulo;

    @BeforeEach
    void setUp() {
        service = new DocumentoService(pedidoService, albaranService, facturaService,
                articuloService, clienteService, almacenService, albaranRepository,
                facturaRepository, pedidoRepository, presupuestoRepository, tarifaClienteService);
        cliente = new Cliente();
        cliente.setId(2L);
        articulo = new Articulo();
        articulo.setId(10L);
        articulo.setNombre("Pan");
    }

    @Test
    void guardarPedidoCreaLineasYAplicaTarifa() {
        when(clienteService.findById(2L)).thenReturn(Optional.of(cliente));
        when(pedidoService.guardar(any())).thenAnswer(i -> {
            Pedido p = i.getArgument(0);
            p.setId(1L);
            return p;
        });
        when(articuloService.findAllById(List.of(10L))).thenReturn(List.of(articulo));
        when(tarifaClienteService.resolverPrecio(2L, 10L, new BigDecimal("3.50")))
                .thenReturn(new BigDecimal("3.00"));
        when(tarifaClienteService.resolverDescuento(2L, 10L)).thenReturn(new BigDecimal("5"));

        Pedido pedido = service.guardarPedido(null, Map.of(
                "clienteId", "2",
                "fecha", "2026-06-18",
                "observaciones", " prueba ",
                "lineas", List.of(Map.of(
                        "articuloId", 10,
                        "cantidad", "2,5",
                        "precio", "3.50",
                        "iva", 10,
                        "descuento", 0
                ))
        ));

        assertEquals("PENDIENTE", pedido.getEstado());
        assertEquals(LocalDate.of(2026, 6, 18), pedido.getFecha());
        assertEquals("prueba", pedido.getObservaciones());
        PedidoLinea linea = pedido.getLineas().iterator().next();
        assertEquals(new BigDecimal("2.5"), linea.getCantidad());
        assertEquals(new BigDecimal("3.00"), linea.getPrecio());
        assertEquals(new BigDecimal("5"), linea.getDescuento());
        verify(pedidoRepository).save(pedido);
    }

    @Test
    void guardarPedidoValidaClienteYArticulo() {
        assertThrows(IllegalArgumentException.class,
                () -> service.guardarPedido(null, Map.of("fecha", "")));

        when(clienteService.findById(2L)).thenReturn(Optional.of(cliente));
        when(pedidoService.guardar(any())).thenAnswer(i -> i.getArgument(0));
        when(articuloService.findAllById(List.of(99L))).thenReturn(List.of());
        assertThrows(IllegalArgumentException.class, () -> service.guardarPedido(null, Map.of(
                "clienteId", 2,
                "lineas", List.of(Map.of("articuloId", 99))
        )));
    }

    @Test
    void guardarAlbaranCalculaTotalConDescuentoEIva() {
        Almacen almacen = new Almacen();
        almacen.setId(4L);
        when(clienteService.findById(2L)).thenReturn(Optional.of(cliente));
        when(almacenService.findById(4L)).thenReturn(Optional.of(almacen));
        when(albaranService.guardar(any())).thenAnswer(i -> {
            AlbaranVenta a = i.getArgument(0);
            a.setId(3L);
            a.setNumero("ALB-3");
            return a;
        });
        when(articuloService.findAllById(List.of(10L))).thenReturn(List.of(articulo));
        when(tarifaClienteService.resolverPrecio(anyLong(), anyLong(), any())).thenAnswer(i -> i.getArgument(2));
        when(tarifaClienteService.resolverDescuento(2L, 10L)).thenReturn(BigDecimal.ZERO);

        AlbaranVenta albaran = service.guardarAlbaran(null, Map.of(
                "clienteId", 2,
                "almacenId", 4,
                "lineas", List.of(Map.of(
                        "articuloId", 10,
                        "cantidad", 2,
                        "precio", 10,
                        "descuento", 10,
                        "iva", 21
                ))
        ));

        assertSame(almacen, albaran.getAlmacen());
        assertEquals(new BigDecimal("21.78"), albaran.getTotal());
        verify(albaranRepository).updateTotal(3L, new BigDecimal("21.78"));
    }

    @Test
    void guardarFacturaCalculaBaseIvaYTotal() {
        when(clienteService.findById(2L)).thenReturn(Optional.of(cliente));
        when(facturaService.save(any())).thenAnswer(i -> {
            Factura f = i.getArgument(0);
            f.setId(5L);
            return f;
        });
        when(articuloService.findAllById(List.of(10L))).thenReturn(List.of(articulo));
        when(tarifaClienteService.resolverPrecio(eq(2L), eq(10L), any(BigDecimal.class)))
                .thenReturn(new BigDecimal("10"));
        when(tarifaClienteService.resolverDescuento(2L, 10L)).thenReturn(BigDecimal.ZERO);

        Factura factura = service.guardarFactura(null, Map.of(
                "clienteId", 2,
                "medioCobro", "TARJETA",
                "fechaVencimiento", "2026-07-18",
                "lineas", List.of(Map.of(
                        "articuloId", 10,
                        "cantidad", 2,
                        "precio", 10,
                        "descuento", 10,
                        "iva", 21
                ))
        ));

        assertEquals("BORRADOR", factura.getEstado());
        assertEquals(new BigDecimal("18.00"), factura.getBaseImponible());
        assertEquals(new BigDecimal("3.78"), factura.getTotalIva());
        assertEquals(new BigDecimal("21.78"), factura.getTotal());
        assertEquals(LocalDate.of(2026, 7, 18), factura.getFechaVencimiento());
        verify(facturaRepository).updateTotales(5L, new BigDecimal("21.78"),
                new BigDecimal("18.00"), new BigDecimal("3.78"));
    }

    @Test
    void actualizacionesRequierenDocumentoExistente() {
        assertThrows(IllegalArgumentException.class,
                () -> service.guardarPedido(9L, Map.of()));
        assertThrows(IllegalArgumentException.class,
                () -> service.guardarAlbaran(9L, Map.of()));
        assertThrows(IllegalArgumentException.class,
                () -> service.guardarFactura(9L, Map.of()));
    }
}
