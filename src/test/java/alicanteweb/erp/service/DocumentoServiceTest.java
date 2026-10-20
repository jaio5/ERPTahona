package alicanteweb.erp.service;

import alicanteweb.erp.entities.*;
import alicanteweb.erp.repository.AlbaranVentaRepository;
import alicanteweb.erp.repository.FacturaRepository;
import alicanteweb.erp.repository.PedidoRepository;
import alicanteweb.erp.repository.PresupuestoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
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
    @Mock PresupuestoService presupuestoService;
    DocumentoService service;

    Cliente cliente;
    Articulo articulo;

    @BeforeEach
    void setUp() {
        service = new DocumentoService(pedidoService, albaranService, facturaService,
                articuloService, clienteService, almacenService, albaranRepository,
                facturaRepository, pedidoRepository, presupuestoRepository, tarifaClienteService,
                presupuestoService);
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
        // El descuento manual de la línea (10) tiene prioridad: no se consulta la tarifa.

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
        // El descuento manual de la línea (10) tiene prioridad: no se consulta la tarifa.

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
    void guardarFacturaAplicaDescuentoGlobalPorcentaje() {
        when(clienteService.findById(2L)).thenReturn(Optional.of(cliente));
        when(facturaService.save(any())).thenAnswer(i -> {
            Factura f = i.getArgument(0);
            f.setId(7L);
            return f;
        });
        when(articuloService.findAllById(List.of(10L))).thenReturn(List.of(articulo));
        when(tarifaClienteService.resolverPrecio(eq(2L), eq(10L), any(BigDecimal.class)))
                .thenAnswer(i -> i.getArgument(2));
        when(tarifaClienteService.resolverDescuento(2L, 10L)).thenReturn(BigDecimal.ZERO);

        // Línea sin descuento (base 2 x 10 = 20) y 10% de descuento global -> base 18.
        Factura factura = service.guardarFactura(null, Map.of(
                "clienteId", 2,
                "descuentoGlobalTipo", "PORCENTAJE",
                "descuentoGlobalValor", "10",
                "lineas", List.of(Map.of(
                        "articuloId", 10, "cantidad", 2, "precio", 10, "iva", 21
                ))
        ));

        assertEquals("PORCENTAJE", factura.getDescuentoGlobalTipo());
        assertEquals(new BigDecimal("18.00"), factura.getBaseImponible());
        assertEquals(new BigDecimal("3.78"), factura.getTotalIva());   // 21% de 18
        assertEquals(new BigDecimal("21.78"), factura.getTotal());
        verify(facturaRepository).updateTotales(7L, new BigDecimal("21.78"),
                new BigDecimal("18.00"), new BigDecimal("3.78"));
    }

    @Test
    void guardarAlbaranAplicaDescuentoGlobalImporte() {
        when(clienteService.findById(2L)).thenReturn(Optional.of(cliente));
        when(albaranService.guardar(any())).thenAnswer(i -> {
            AlbaranVenta a = i.getArgument(0);
            a.setId(8L);
            a.setNumero("ALB-8");
            return a;
        });
        when(articuloService.findAllById(List.of(10L))).thenReturn(List.of(articulo));
        when(tarifaClienteService.resolverPrecio(anyLong(), anyLong(), any())).thenAnswer(i -> i.getArgument(2));
        when(tarifaClienteService.resolverDescuento(2L, 10L)).thenReturn(BigDecimal.ZERO);

        // Base 20, descuento global de 5 € (importe fijo) -> base 15; IVA 21% -> 3,15; total 18,15.
        AlbaranVenta albaran = service.guardarAlbaran(null, Map.of(
                "clienteId", 2,
                "descuentoGlobalTipo", "IMPORTE",
                "descuentoGlobalValor", "5",
                "lineas", List.of(Map.of(
                        "articuloId", 10, "cantidad", 2, "precio", 10, "iva", 21
                ))
        ));

        assertEquals("IMPORTE", albaran.getDescuentoGlobalTipo());
        assertEquals(new BigDecimal("18.15"), albaran.getTotal());
        verify(albaranRepository).updateTotal(8L, new BigDecimal("18.15"));
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

    // ===================== Líneas: null (no tocar) vs. lista vacía (error) =====================

    private static Map<String, Object> datosSinLineas() {
        Map<String, Object> datos = new HashMap<>();
        datos.put("clienteId", 2);
        datos.put("lineas", List.of());
        return datos;
    }

    @Test
    void editarPedidoConListaVaciaRechazaYConservaLasLineas() {
        Pedido pedido = new Pedido();
        pedido.setId(1L);
        pedido.setEstado("PENDIENTE");
        for (int i = 0; i < 3; i++) pedido.getPedidoLineas().add(new PedidoLinea());
        List<PedidoLinea> antes = List.copyOf(pedido.getPedidoLineas());
        when(pedidoRepository.findById(1L)).thenReturn(Optional.of(pedido));

        IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                () -> service.guardarPedido(1L, datosSinLineas()));

        assertEquals("El documento debe tener al menos una línea", e.getMessage());
        assertEquals(antes, List.copyOf(pedido.getPedidoLineas()));
        verify(pedidoService, never()).guardar(any());
        verify(pedidoRepository, never()).save(any());
    }

    @Test
    void editarPresupuestoConListaVaciaRechazaYConservaLasLineas() {
        Presupuesto pre = new Presupuesto();
        pre.setId(1L);
        pre.setNumero("PRE-2026-00001");
        for (int i = 0; i < 3; i++) pre.getLineas().add(new PresupuestoLinea());
        List<PresupuestoLinea> antes = List.copyOf(pre.getLineas());
        when(presupuestoRepository.findById(1L)).thenReturn(Optional.of(pre));

        IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                () -> service.guardarPresupuesto(1L, datosSinLineas()));

        assertEquals("El documento debe tener al menos una línea", e.getMessage());
        assertEquals(antes, pre.getLineas());
        verify(presupuestoRepository, never()).save(any());
    }

    @Test
    void editarAlbaranConListaVaciaRechazaYConservaLasLineas() {
        AlbaranVenta alb = new AlbaranVenta();
        alb.setId(1L);
        alb.setEstado("PENDIENTE");
        for (int i = 0; i < 3; i++) alb.getAlbaranVentaLineas().add(new AlbaranVentaLinea());
        List<AlbaranVentaLinea> antes = List.copyOf(alb.getAlbaranVentaLineas());
        when(albaranRepository.findById(1L)).thenReturn(Optional.of(alb));

        IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                () -> service.guardarAlbaran(1L, datosSinLineas()));

        assertEquals("El documento debe tener al menos una línea", e.getMessage());
        assertEquals(antes, List.copyOf(alb.getAlbaranVentaLineas()));
        verify(albaranService, never()).guardar(any());
        verify(albaranRepository, never()).updateTotal(any(), any());
    }

    @Test
    void editarFacturaBorradorConListaVaciaRechazaYConservaLasLineas() {
        Factura fac = new Factura();
        fac.setId(1L);
        fac.setEstado("BORRADOR");
        for (int i = 0; i < 3; i++) fac.getFacturaLineas().add(new FacturaLinea());
        List<FacturaLinea> antes = List.copyOf(fac.getFacturaLineas());
        when(facturaRepository.findById(1L)).thenReturn(Optional.of(fac));

        IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                () -> service.guardarFactura(1L, datosSinLineas()));

        assertEquals("El documento debe tener al menos una línea", e.getMessage());
        assertEquals(antes, List.copyOf(fac.getFacturaLineas()));
        verify(facturaService, never()).save(any());
        verify(facturaRepository, never()).updateTotales(any(), any(), any(), any());
    }

    @Test
    void facturaEmitidaConListaVaciaSigueSuPropioFlujoDeInalterabilidad() {
        Factura fac = new Factura();
        fac.setId(1L);
        fac.setEstado("EMITIDA");
        when(facturaRepository.findById(1L)).thenReturn(Optional.of(fac));
        when(clienteService.findById(2L)).thenReturn(Optional.of(cliente));
        when(facturaService.save(any())).thenThrow(new IllegalStateException(
                "Las facturas emitidas no se modifican. Cree una factura rectificativa."));

        IllegalStateException e = assertThrows(IllegalStateException.class,
                () -> service.guardarFactura(1L, datosSinLineas()));
        assertTrue(e.getMessage().contains("rectificativa"));
    }

    @Test
    void crearDocumentoConListaVaciaTambienSeRechaza() {
        assertThrows(IllegalArgumentException.class, () -> service.guardarPedido(null, datosSinLineas()));
        assertThrows(IllegalArgumentException.class, () -> service.guardarPresupuesto(null, datosSinLineas()));
        assertThrows(IllegalArgumentException.class, () -> service.guardarAlbaran(null, datosSinLineas()));
        assertThrows(IllegalArgumentException.class, () -> service.guardarFactura(null, datosSinLineas()));
        verifyNoInteractions(pedidoService, albaranService, facturaService, presupuestoService);
    }

    @Test
    void sinClaveLineasNoSeTocanLasLineasExistentes() {
        Pedido pedido = new Pedido();
        pedido.setId(1L);
        pedido.setEstado("PENDIENTE");
        for (int i = 0; i < 3; i++) pedido.getPedidoLineas().add(new PedidoLinea());
        List<PedidoLinea> antes = List.copyOf(pedido.getPedidoLineas());
        when(pedidoRepository.findById(1L)).thenReturn(Optional.of(pedido));
        when(clienteService.findById(2L)).thenReturn(Optional.of(cliente));
        when(pedidoService.guardar(any())).thenAnswer(i -> i.getArgument(0));

        Pedido guardado = service.guardarPedido(1L, Map.of("clienteId", 2, "observaciones", "solo cabecera"));

        assertEquals("solo cabecera", guardado.getObservaciones());
        assertEquals(antes, List.copyOf(guardado.getPedidoLineas()));
        verify(articuloService, never()).findAllById(any());
    }

    // ===================== Albaranes no editables =====================

    @ParameterizedTest
    @ValueSource(strings = {"ENTREGADO", "FACTURADO", "ANULADO"})
    void guardarAlbaranEnEstadoCerradoLanzaYNoModificaNada(String estado) {
        AlbaranVenta alb = new AlbaranVenta();
        alb.setId(1L);
        alb.setEstado(estado);
        alb.setObservaciones("original");
        when(albaranRepository.findById(1L)).thenReturn(Optional.of(alb));

        IllegalStateException e = assertThrows(IllegalStateException.class,
                () -> service.guardarAlbaran(1L, Map.of("clienteId", 2, "observaciones", "cambio",
                        "lineas", List.of(Map.of("articuloId", 10, "cantidad", 1, "precio", 1, "iva", 4)))));

        assertEquals("No se puede modificar un albarán en estado " + estado, e.getMessage());
        assertEquals("original", alb.getObservaciones());
        verify(albaranService, never()).guardar(any());
        verify(albaranRepository, never()).updateTotal(any(), any());
    }

    @Test
    void albaranPendienteSeSigueEditando() {
        AlbaranVenta alb = new AlbaranVenta();
        alb.setId(1L);
        alb.setEstado("PENDIENTE");
        for (int i = 0; i < 3; i++) alb.getAlbaranVentaLineas().add(new AlbaranVentaLinea());
        when(albaranRepository.findById(1L)).thenReturn(Optional.of(alb));
        when(clienteService.findById(2L)).thenReturn(Optional.of(cliente));
        when(albaranService.guardar(any())).thenAnswer(i -> i.getArgument(0));
        when(articuloService.findAllById(List.of(10L))).thenReturn(List.of(articulo));
        when(tarifaClienteService.resolverPrecio(anyLong(), anyLong(), any())).thenAnswer(i -> i.getArgument(2));
        when(tarifaClienteService.resolverDescuento(2L, 10L)).thenReturn(BigDecimal.ZERO);

        AlbaranVenta guardado = service.guardarAlbaran(1L, Map.of("clienteId", 2,
                "lineas", List.of(Map.of("articuloId", 10, "cantidad", 2, "precio", 10, "iva", 21))));

        assertEquals(1, guardado.getAlbaranVentaLineas().size());
        assertEquals(new BigDecimal("24.20"), guardado.getTotal());
        verify(albaranRepository).updateTotal(1L, new BigDecimal("24.20"));
    }

    // ===================== Numeración de presupuestos =====================

    @Test
    void numeracionDelPresupuestoSeDelegaEnPresupuestoService() {
        when(clienteService.findById(2L)).thenReturn(Optional.of(cliente));
        when(presupuestoService.generarNumeroPresupuesto()).thenReturn("PRE-2026-00004");
        when(presupuestoRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        Presupuesto pre = service.guardarPresupuesto(null, Map.of("clienteId", 2));

        assertEquals("PRE-2026-00004", pre.getNumero());
        assertEquals("BORRADOR", pre.getEstado());
        verify(presupuestoRepository, never()).count();
    }

    @Test
    void presupuestoConNumeroNoSeRenumera() {
        Presupuesto existente = new Presupuesto();
        existente.setId(1L);
        existente.setNumero("PRE-2026-00002");
        when(presupuestoRepository.findById(1L)).thenReturn(Optional.of(existente));
        when(clienteService.findById(2L)).thenReturn(Optional.of(cliente));
        when(presupuestoRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        assertEquals("PRE-2026-00002", service.guardarPresupuesto(1L, Map.of("clienteId", 2)).getNumero());
        verify(presupuestoService, never()).generarNumeroPresupuesto();
    }
}
