package alicanteweb.erp.controller;

import alicanteweb.erp.controller.rest.WebChildEntityController;
import alicanteweb.erp.entities.Articulo;
import alicanteweb.erp.entities.Pedido;
import alicanteweb.erp.entities.PedidoLinea;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.math.BigDecimal;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WebChildEntityControllerTest {

    @Mock EntityManager entityManager;
    WebChildEntityController controller;

    @BeforeEach
    void setUp() {
        controller = new WebChildEntityController(entityManager);
    }

    @Test
    void exponeHijosYRechazaDefinicionesDesconocidas() {
        assertEquals("PedidoLinea", controller.modules().get("pedido-lineas"));
        assertEquals(HttpStatus.NOT_FOUND,
                controller.create("desconocido", 1L, Map.of()).getStatusCode());
        assertEquals(HttpStatus.NOT_FOUND,
                controller.update("desconocido", 1L, 2L, Map.of()).getStatusCode());
        assertEquals(HttpStatus.NOT_FOUND,
                controller.delete("desconocido", 1L, 2L).getStatusCode());
    }

    @Test
    void creaLineaYConvierteCamposYRelacion() {
        Pedido pedido = new Pedido();
        pedido.setId(1L);
        Articulo articulo = new Articulo();
        articulo.setId(4L);
        when(entityManager.find(Pedido.class, 1L)).thenReturn(pedido);
        when(entityManager.getReference(Articulo.class, 4L)).thenReturn(articulo);

        var response = controller.create("pedido-lineas", 1L, Map.of(
                "articuloId", "4",
                "descripcion", "Pan",
                "cantidad", "2.5",
                "precio", 3,
                "descuento", "",
                "iva", "10"
        ));

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(entityManager).persist(any(PedidoLinea.class));
    }

    @Test
    void updateVerificaPertenenciaAlPadre() {
        Pedido padre = new Pedido();
        padre.setId(1L);
        Pedido otro = new Pedido();
        otro.setId(9L);
        PedidoLinea linea = new PedidoLinea();
        linea.setId(2L);
        linea.setPedido(otro);
        when(entityManager.find(PedidoLinea.class, 2L)).thenReturn(linea);
        when(entityManager.find(Pedido.class, 1L)).thenReturn(padre);

        assertEquals(HttpStatus.NOT_FOUND,
                controller.update("pedido-lineas", 1L, 2L, Map.of()).getStatusCode());

        linea.setPedido(padre);
        assertEquals(HttpStatus.OK,
                controller.update("pedido-lineas", 1L, 2L,
                        Map.of("cantidad", new BigDecimal("4"))).getStatusCode());
        assertEquals(new BigDecimal("4"), linea.getCantidad());
    }

    @Test
    void deleteVerificaPertenenciaYElimina() {
        Pedido padre = new Pedido();
        padre.setId(1L);
        PedidoLinea linea = new PedidoLinea();
        linea.setId(2L);
        linea.setPedido(padre);
        when(entityManager.find(PedidoLinea.class, 2L)).thenReturn(linea);

        assertEquals(HttpStatus.NO_CONTENT,
                controller.delete("pedido-lineas", 1L, 2L).getStatusCode());
        verify(entityManager).remove(linea);
    }
}
