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

    @Test
    void guardarInicializaNumeroFechaYEstado() {
        when(repository.count()).thenReturn(4L);
        when(repository.save(any())).thenAnswer(i -> i.getArgument(0));
        Pedido pedido = service.guardar(new Pedido());
        assertTrue(pedido.getNumero().startsWith("PED-" + LocalDate.now().getYear()));
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
    void duplicarCopiaCabeceraYLineas() {
        Pedido original = pedidoBase();
        original.getLineas().add(linea(10L, "Pan", "2", "3", BigDecimal.ONE, new BigDecimal("10")));
        when(repository.findById(1L)).thenReturn(Optional.of(original));
        when(repository.count()).thenReturn(9L);
        when(repository.save(any())).thenAnswer(i -> i.getArgument(0));

        Pedido copia = service.duplicar(1L);
        assertEquals("PENDIENTE", copia.getEstado());
        assertEquals("Duplicado de PED-1", copia.getObservaciones());
        assertEquals(1, copia.getLineas().size());
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
