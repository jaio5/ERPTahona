package alicanteweb.erp.service;

import alicanteweb.erp.entities.Articulo;
import alicanteweb.erp.entities.Cliente;
import alicanteweb.erp.entities.Pedido;
import alicanteweb.erp.entities.PedidoLinea;
import alicanteweb.erp.repository.PedidoRepository;
import alicanteweb.erp.repository.RecetaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.LinkedHashSet;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/** Hoja de obrador: agregación de pedidos del día por artículo. */
class PlanificadorServiceTest {

    private static final LocalDate FECHA = LocalDate.of(2026, 10, 20);

    private PedidoRepository pedidoRepository;
    private PlanificadorService service;
    private Articulo barra;
    private Articulo croissant;
    private long secuencia = 0;

    @BeforeEach
    void setUp() {
        pedidoRepository = mock(PedidoRepository.class);
        service = new PlanificadorService(mock(RecetaRepository.class), pedidoRepository);
        barra = articulo(1L, "Barra", null);
        croissant = articulo(2L, "Croissant", null);
    }

    private static Articulo articulo(Long id, String nombre, String stock) {
        Articulo a = new Articulo();
        a.setId(id);
        a.setNombre(nombre);
        a.setStock(stock != null ? new BigDecimal(stock) : null);
        return a;
    }

    private Pedido pedido(String cliente, String estado, Object... articuloYCantidad) {
        Pedido p = new Pedido();
        p.setId(++secuencia);
        p.setFecha(FECHA);
        p.setEstado(estado);
        if (cliente != null) {
            Cliente c = new Cliente();
            c.setNombre(cliente);
            p.setCliente(c);
        }
        LinkedHashSet<PedidoLinea> lineas = new LinkedHashSet<>();
        for (int i = 0; i < articuloYCantidad.length; i += 2) {
            PedidoLinea l = new PedidoLinea();
            l.setId(secuencia * 100 + i);
            l.setArticulo((Articulo) articuloYCantidad[i]);
            l.setCantidad(new BigDecimal((String) articuloYCantidad[i + 1]));
            lineas.add(l);
        }
        p.setLineas(lineas);
        return p;
    }

    @Test
    void agregaPorArticuloConDesglosePorClienteYOrdenaPorNombre() {
        when(pedidoRepository.findByFecha(FECHA)).thenReturn(List.of(
                pedido("Rest. Sol", "PENDIENTE", croissant, "10", barra, "20"),
                pedido("Bar Pepe", "CONFIRMADO", barra, "30")));

        List<PlanificadorService.TotalArticulo> hoja = service.totalesPorArticulo(FECHA);

        assertThat(hoja).extracting(PlanificadorService.TotalArticulo::nombre)
                .containsExactly("Barra", "Croissant");
        PlanificadorService.TotalArticulo b = hoja.get(0);
        assertThat(b.articuloId()).isEqualTo(1L);
        assertThat(b.total()).isEqualByComparingTo("50");
        // Desglose de mayor a menor cantidad
        assertThat(b.desglose()).containsOnlyKeys("Bar Pepe", "Rest. Sol");
        assertThat(b.desglose().keySet()).containsExactly("Bar Pepe", "Rest. Sol");
        assertThat(b.desglose().get("Bar Pepe")).isEqualByComparingTo("30");
        assertThat(b.desglose().get("Rest. Sol")).isEqualByComparingTo("20");
        assertThat(hoja.get(1).total()).isEqualByComparingTo("10");
    }

    @Test
    void mismoClienteEnVariosPedidosSeSuma() {
        when(pedidoRepository.findByFecha(FECHA)).thenReturn(List.of(
                pedido("Bar Pepe", "PENDIENTE", barra, "5"),
                pedido("Bar Pepe", "PENDIENTE", barra, "7"),
                pedido(null, "PENDIENTE", barra, "1")));

        PlanificadorService.TotalArticulo b = service.totalesPorArticulo(FECHA).get(0);

        assertThat(b.total()).isEqualByComparingTo("13");
        assertThat(b.desglose().get("Bar Pepe")).isEqualByComparingTo("12");
        assertThat(b.desglose().get("Sin cliente")).isEqualByComparingTo("1");
    }

    @Test
    void excluyePedidosCanceladosYAnulados() {
        when(pedidoRepository.findByFecha(FECHA)).thenReturn(List.of(
                pedido("Bar Pepe", "PENDIENTE", barra, "30"),
                pedido("Rest. Sol", "CANCELADO", barra, "20"),
                pedido("Rest. Sol", "anulado", croissant, "10")));

        List<PlanificadorService.TotalArticulo> hoja = service.totalesPorArticulo(FECHA);

        assertThat(hoja).singleElement().satisfies(t -> {
            assertThat(t.nombre()).isEqualTo("Barra");
            assertThat(t.total()).isEqualByComparingTo("30");
            assertThat(t.desglose()).containsOnlyKeys("Bar Pepe");
        });
    }

    @Test
    void restaElStockYNuncaDevuelveNegativo() {
        barra.setStock(new BigDecimal("5"));
        croissant.setStock(new BigDecimal("25"));
        when(pedidoRepository.findByFecha(FECHA)).thenReturn(List.of(
                pedido("Bar Pepe", "PENDIENTE", barra, "30", croissant, "10"),
                pedido("Rest. Sol", "PENDIENTE", barra, "20")));

        List<PlanificadorService.TotalArticulo> hoja = service.totalesPorArticulo(FECHA);

        assertThat(hoja.get(0).stock()).isEqualByComparingTo("5");
        assertThat(hoja.get(0).aElaborar()).isEqualByComparingTo("45");
        assertThat(hoja.get(1).stock()).isEqualByComparingTo("25");
        assertThat(hoja.get(1).aElaborar()).isEqualByComparingTo("0");
    }

    @Test
    void stockNuloCuentaComoCero() {
        when(pedidoRepository.findByFecha(FECHA)).thenReturn(List.of(
                pedido("Bar Pepe", "PENDIENTE", croissant, "10")));

        PlanificadorService.TotalArticulo c = service.totalesPorArticulo(FECHA).get(0);

        assertThat(c.stock()).isEqualByComparingTo("0");
        assertThat(c.aElaborar()).isEqualByComparingTo("10");
    }

    @Test
    void sinPedidosDevuelveListaVacia() {
        when(pedidoRepository.findByFecha(FECHA)).thenReturn(List.of());

        assertThat(service.totalesPorArticulo(FECHA)).isEmpty();
    }
}
