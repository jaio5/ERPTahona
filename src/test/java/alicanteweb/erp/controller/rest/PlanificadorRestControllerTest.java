package alicanteweb.erp.controller.rest;

import alicanteweb.erp.entities.*;
import alicanteweb.erp.repository.RecetaRepository;
import alicanteweb.erp.service.AlbaranService;
import alicanteweb.erp.service.ArticuloService;
import alicanteweb.erp.service.OrdenProduccionService;
import alicanteweb.erp.service.PedidoService;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.LinkedHashSet;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class PlanificadorRestControllerTest {

    @Test
    void cargaLasRecetasEnBloqueYCalculaLasNecesidades() {
        PedidoService pedidos = mock(PedidoService.class);
        AlbaranService albaranes = mock(AlbaranService.class);
        OrdenProduccionService ordenes = mock(OrdenProduccionService.class);
        RecetaRepository recetas = mock(RecetaRepository.class);
        ArticuloService articulos = mock(ArticuloService.class);
        PlanificadorRestController controller = new PlanificadorRestController(
                pedidos, albaranes, ordenes, recetas, articulos);

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

        RecetaIngrediente ingrediente = new RecetaIngrediente();
        ingrediente.setArticulo(harina);
        ingrediente.setCantidad(new BigDecimal("2"));
        Receta receta = new Receta();
        receta.setArticuloResultante(producto);
        receta.setRendimientoCantidad(new BigDecimal("4"));
        receta.setIngredientes(new LinkedHashSet<>(List.of(ingrediente)));

        LocalDate fecha = LocalDate.of(2026, 6, 19);
        when(pedidos.findByFechaBetween(fecha, fecha)).thenReturn(List.of(pedido));
        when(albaranes.findByFecha(fecha)).thenReturn(List.of());
        when(ordenes.findByFechaBetween(fecha, fecha)).thenReturn(List.of());
        when(recetas.findActivasConIngredientesByArticuloIds(any())).thenReturn(List.of(receta));
        when(articulos.findAllById(any())).thenReturn(List.of(harina));

        var response = controller.plan(fecha);

        verify(recetas, times(1)).findActivasConIngredientesByArticuloIds(any());
        verify(recetas, never()).findByArticuloResultante_IdAndActivoTrue(any());
        @SuppressWarnings("unchecked")
        List<java.util.Map<String, Object>> necesidades =
                (List<java.util.Map<String, Object>>) response.getBody().get("necesidades");
        assertThat(necesidades).singleElement()
                .satisfies(item -> assertThat(item.get("necesario")).isEqualTo(new BigDecimal("2.0000")));
    }
}
