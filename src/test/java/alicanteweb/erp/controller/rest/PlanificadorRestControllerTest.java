package alicanteweb.erp.controller.rest;

import alicanteweb.erp.entities.*;
import alicanteweb.erp.service.AlbaranService;
import alicanteweb.erp.service.ArticuloService;
import alicanteweb.erp.service.OrdenProduccionService;
import alicanteweb.erp.service.PedidoService;
import alicanteweb.erp.service.PlanificadorService;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class PlanificadorRestControllerTest {

    @Test
    void cargaLasRecetasEnBloqueYCalculaLasNecesidades() {
        PedidoService pedidos = mock(PedidoService.class);
        AlbaranService albaranes = mock(AlbaranService.class);
        OrdenProduccionService ordenes = mock(OrdenProduccionService.class);
        PlanificadorService planificador = mock(PlanificadorService.class);
        ArticuloService articulos = mock(ArticuloService.class);
        PlanificadorRestController controller = new PlanificadorRestController(
                pedidos, albaranes, ordenes, planificador, articulos);

        Articulo producto = new Articulo();
        producto.setId(1L);
        Articulo harina = new Articulo();
        harina.setId(2L);
        harina.setNombre("Harina");
        harina.setStock(new BigDecimal("20"));

        PedidoLinea linea = new PedidoLinea();
        linea.setArticulo(producto);
        linea.setCantidad(new BigDecimal("4"));
        Pedido pedido = new Pedido();
        pedido.setId(10L);
        pedido.setLineas(new LinkedHashSet<>(List.of(linea)));

        LocalDate fecha = LocalDate.of(2026, 6, 19);
        when(pedidos.findByFechaBetween(fecha, fecha)).thenReturn(List.of(pedido));
        when(albaranes.findByFecha(fecha)).thenReturn(List.of());
        when(ordenes.findByFechaBetween(fecha, fecha)).thenReturn(List.of());
        when(planificador.calcularNecesidadesMateriaPrima(any(), any()))
                .thenReturn(Map.of(2L, new BigDecimal("2.0000")));
        when(articulos.findAllById(any())).thenReturn(List.of(harina));

        var response = controller.plan(fecha);

        verify(planificador, times(1)).calcularNecesidadesMateriaPrima(any(), any());
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> necesidades =
                (List<Map<String, Object>>) response.getBody().get("necesidades");
        assertThat(necesidades).singleElement()
                .satisfies(item -> assertThat(item.get("necesario")).isEqualTo(new BigDecimal("2.0000")));
    }
}
