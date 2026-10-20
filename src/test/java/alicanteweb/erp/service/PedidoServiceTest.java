package alicanteweb.erp.service;

import alicanteweb.erp.entities.*;
import alicanteweb.erp.repository.PedidoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PedidoServiceTest {

    @Mock PedidoRepository repository;
    @Mock AlbaranService albaranService;
    @Mock AuditoriaService auditoriaService;
    PedidoService service;

    @BeforeEach
    void setUp() {
        service = new PedidoService(repository, albaranService, auditoriaService);
    }

    private static final String PREFIJO = "PED-" + LocalDate.now().getYear() + "-";

    @Test
    void guardarInicializaNumeroFechaYEstado() {
        when(repository.maxNumeroConPrefijo(PREFIJO)).thenReturn(PREFIJO + "00004");
        when(repository.save(any())).thenAnswer(i -> i.getArgument(0));
        Pedido pedido = service.guardar(new Pedido());
        assertEquals(PREFIJO + "00005", pedido.getNumero());
        assertEquals(LocalDate.now(), pedido.getFecha());
        assertEquals("PENDIENTE", pedido.getEstado());
    }

    @Test
    void consultasYCambioEstadoDelegan() {
        Pedido pedido = pedidoBase();
        when(repository.findAll()).thenReturn(List.of(pedido));
        when(repository.findById(1L)).thenReturn(Optional.of(pedido));
        when(repository.findByNumero("PED")).thenReturn(Optional.of(pedido));
        when(repository.findByClienteId(2L)).thenReturn(List.of(pedido));
        when(repository.findByEstado("PENDIENTE")).thenReturn(List.of(pedido));
        when(repository.findByNumeroContainingIgnoreCaseOrCliente_NombreContainingIgnoreCase("x", "x")).thenReturn(List.of(pedido));
        when(repository.findByFechaBetween(any(), any())).thenReturn(List.of(pedido));
        when(repository.countByEstado("PENDIENTE")).thenReturn(1L);
        when(repository.save(pedido)).thenReturn(pedido);

        assertEquals(1, service.obtenerTodos().size());
        assertTrue(service.obtenerPorId(1L).isPresent());
        assertTrue(service.obtenerPorNumero("PED").isPresent());
        assertEquals(1, service.buscarPorCliente(2L).size());
        assertEquals(1, service.buscarPorEstado("PENDIENTE").size());
        assertEquals(1, service.buscar("x").size());
        assertEquals(1, service.findByFechaBetween(LocalDate.now(), LocalDate.now()).size());
        assertEquals(1, service.countByEstado("PENDIENTE"));
        assertEquals("CONFIRMADO", service.cambiarEstado(1L, "CONFIRMADO").getEstado());
        service.eliminar(1L);
        verify(repository).deleteById(1L);
    }

    @Test
    void cambiarEstadoRechazaPedidoInexistente() {
        assertThrows(RuntimeException.class, () -> service.cambiarEstado(99L, "X"));
    }

    @Test
    void convertirPedidoCompletoCopiaLineasYMarcaServido() {
        Pedido pedido = pedidoBase();
        pedido.getLineas().add(linea(10L, "Pan", "2", "3", null, null));
        AlbaranVenta guardado = new AlbaranVenta();
        guardado.setNumero("ALB-1");
        when(repository.findById(1L)).thenReturn(Optional.of(pedido));
        when(albaranService.guardar(any())).thenReturn(guardado);

        AlbaranVenta result = service.convertirAAlbaran(1L, new Usuario());

        assertSame(guardado, result);
        assertEquals("SERVIDO", pedido.getEstado());
        var captor = org.mockito.ArgumentCaptor.forClass(AlbaranVenta.class);
        verify(albaranService).guardar(captor.capture());
        AlbaranVenta creado = captor.getValue();
        assertEquals(1, creado.getAlbaranVentaLineas().size());
        AlbaranVentaLinea l = creado.getAlbaranVentaLineas().iterator().next();
        assertEquals("Pan", l.getDescripcion());
        assertEquals(BigDecimal.ZERO, l.getDescuento());
        assertEquals(new BigDecimal("21"), l.getIva());
    }

    @Test
    void convertirRechazaServidoCanceladoOInexistente() {
        assertThrows(IllegalArgumentException.class, () -> service.convertirAAlbaran(1L, null));
        Pedido servido = pedidoBase();
        servido.setEstado("SERVIDO");
        when(repository.findById(1L)).thenReturn(Optional.of(servido));
        assertThrows(IllegalStateException.class, () -> service.convertirAAlbaran(1L, null));
        servido.setEstado("CANCELADO");
        assertThrows(IllegalStateException.class, () -> service.convertirAAlbaran(1L, null));
    }

    @Test
    void convertirParcialDistingueEntregaCompletaEIncompleta() {
        Pedido pedido = pedidoBase();
        pedido.getLineas().add(linea(10L, "Pan", "5", "2", BigDecimal.ZERO, new BigDecimal("10")));
        AlbaranVenta guardado = new AlbaranVenta();
        guardado.setNumero("ALB");
        when(repository.findById(1L)).thenReturn(Optional.of(pedido));
        when(albaranService.guardar(any())).thenReturn(guardado);

        service.convertirAAlbaranParcial(1L,
                List.of(new PedidoService.EntregaParcial(10L, new BigDecimal("2"))), null);
        assertEquals("SERVIDO_PARCIAL", pedido.getEstado());

        service.convertirAAlbaranParcial(1L,
                List.of(new PedidoService.EntregaParcial(10L, new BigDecimal("5"))), null);
        assertEquals("SERVIDO", pedido.getEstado());
    }

    @Test
    void convertirParcialRechazaCanceladoOLineaInexistente() {
        Pedido pedido = pedidoBase();
        pedido.setEstado("CANCELADO");
        when(repository.findById(1L)).thenReturn(Optional.of(pedido));
        assertThrows(IllegalStateException.class, () ->
                service.convertirAAlbaranParcial(1L, List.of(), null));

        pedido.setEstado("PENDIENTE");
        assertThrows(IllegalArgumentException.class, () ->
                service.convertirAAlbaranParcial(1L,
                        List.of(new PedidoService.EntregaParcial(99L, BigDecimal.ONE)), null));
    }

    @Test
    void duplicarCopiaCabeceraYLineasYRecalculaTotal() {
        Pedido original = pedidoBase();
        original.setEstado("SERVIDO");
        original.setFecha(LocalDate.now().minusDays(10));
        original.setTotal(new BigDecimal("999"));
        // 2 x 3 = 6, -10 % = 5,40 + IVA 10 % (0,54) = 5,94
        original.getLineas().add(linea(10L, "Pan", "2", "3", BigDecimal.TEN, BigDecimal.TEN));
        // 1 x 4 = 4, sin descuento + IVA 21 % (0,84) = 4,84
        original.getLineas().add(linea(11L, "Bollería", "1", "4", null, new BigDecimal("21")));
        // 5 x 1 = 5, sin descuento ni IVA = 5,00
        original.getLineas().add(linea(12L, "Agua", "5", "1", BigDecimal.ZERO, null));
        when(repository.findById(1L)).thenReturn(Optional.of(original));
        when(repository.maxNumeroConPrefijo(PREFIJO)).thenReturn(PREFIJO + "00009");
        when(repository.save(any())).thenAnswer(i -> i.getArgument(0));

        Pedido copia = service.duplicar(1L);
        assertEquals(PREFIJO + "00010", copia.getNumero());
        assertEquals("PENDIENTE", copia.getEstado());
        assertEquals(LocalDate.now(), copia.getFecha());
        assertSame(original.getCliente(), copia.getCliente());
        assertEquals("Duplicado de PED-1", copia.getObservaciones());
        assertEquals(3, copia.getLineas().size());
        assertEquals(0, new BigDecimal("15.78").compareTo(copia.getTotal()));
        assertTrue(copia.getLineas().stream().allMatch(l -> l.getPedido() == copia && l.getId() == null));
        // El original no se toca y no se generan albaranes ni movimientos
        assertEquals("SERVIDO", original.getEstado());
        verifyNoInteractions(albaranService);
    }

    @Test
    void duplicarPedidoInexistenteLanzaExcepcion() {
        assertThrows(IllegalArgumentException.class, () -> service.duplicar(99L));
        verify(repository, never()).save(any());
    }

    // ---------- Numeración PED-AAAA-NNNNN ----------

    @Test
    void numeracionTrasBorradoIntermedioUsaElMaximoYNoElRecuento() {
        // Existen 00001, 00002, 00004 y 00005 (el 00003 se borró): count()+1 daría 00005 (duplicado)
        lenient().when(repository.count()).thenReturn(4L);
        when(repository.maxNumeroConPrefijo(PREFIJO)).thenReturn(PREFIJO + "00005");
        when(repository.save(any())).thenAnswer(i -> i.getArgument(0));

        assertEquals(PREFIJO + "00006", service.guardar(new Pedido()).getNumero());
    }

    @Test
    void numeracionReiniciaAlCambiarDeEjercicio() {
        // Solo hay pedidos del año anterior: ninguno coincide con el prefijo del año en curso
        when(repository.maxNumeroConPrefijo(PREFIJO)).thenReturn(null);
        when(repository.save(any())).thenAnswer(i -> i.getArgument(0));

        assertEquals(PREFIJO + "00001", service.guardar(new Pedido()).getNumero());
        verify(repository, never()).numerosConPrefijo(any());
    }

    @Test
    void numeracionIgnoraSufijosNoNumericos() {
        // 'PED-AAAA-ABC' ordena por encima de los dígitos: se descarta y se busca el mayor numérico
        when(repository.maxNumeroConPrefijo(PREFIJO)).thenReturn(PREFIJO + "ABC");
        when(repository.numerosConPrefijo(PREFIJO)).thenReturn(
                List.of(PREFIJO + "00002", PREFIJO + "ABC", PREFIJO + "00007", PREFIJO + "7-B"));
        when(repository.save(any())).thenAnswer(i -> i.getArgument(0));

        assertEquals(PREFIJO + "00008", service.guardar(new Pedido()).getNumero());
    }

    @Test
    void numeracionConSoloSufijosNoNumericosEmpiezaEnUno() {
        when(repository.maxNumeroConPrefijo(PREFIJO)).thenReturn(PREFIJO + "XYZ");
        when(repository.numerosConPrefijo(PREFIJO)).thenReturn(List.of(PREFIJO + "XYZ"));
        when(repository.save(any())).thenAnswer(i -> i.getArgument(0));

        assertEquals(PREFIJO + "00001", service.guardar(new Pedido()).getNumero());
    }

    @Test
    void calcularTotalAplicaDescuentoEIvaPorLinea() {
        PedidoLinea l = linea(1L, "Pan", "4", "2.50", new BigDecimal("20"), BigDecimal.TEN);
        // 4 x 2,50 = 10,00 - 20 % = 8,00; IVA 10 % = 0,80
        assertEquals(new BigDecimal("8.00"), PedidoService.importeLinea(l));
        assertEquals(new BigDecimal("0.80"), PedidoService.ivaLinea(l));
        assertEquals(0, new BigDecimal("8.80").compareTo(PedidoService.calcularTotal(List.of(l))));
        assertEquals(0, BigDecimal.ZERO.compareTo(PedidoService.calcularTotal(null)));
    }

    private Pedido pedidoBase() {
        Pedido pedido = new Pedido();
        pedido.setId(1L);
        pedido.setNumero("PED-1");
        pedido.setEstado("PENDIENTE");
        Cliente cliente = new Cliente();
        cliente.setId(2L);
        pedido.setCliente(cliente);
        return pedido;
    }

    private PedidoLinea linea(Long id, String descripcion, String cantidad, String precio,
                              BigDecimal descuento, BigDecimal iva) {
        PedidoLinea linea = new PedidoLinea();
        linea.setId(id);
        linea.setDescripcion(descripcion);
        linea.setCantidad(new BigDecimal(cantidad));
        linea.setPrecio(new BigDecimal(precio));
        linea.setDescuento(descuento);
        linea.setIva(iva);
        Articulo articulo = new Articulo();
        articulo.setNombre(descripcion);
        linea.setArticulo(articulo);
        return linea;
    }
}
