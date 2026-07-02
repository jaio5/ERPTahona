package alicanteweb.erp.controller.web;

import alicanteweb.erp.entities.Articulo;
import alicanteweb.erp.service.ArticuloService;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.mvc.support.RedirectAttributesModelMap;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ArticuloWebControllerTest {

    @Test
    void guardaImportesConComaDecimal() {
        ArticuloService service = mock(ArticuloService.class);
        RedirectAttributes redirect = mock(RedirectAttributes.class);
        when(service.save(any(Articulo.class))).thenAnswer(invocation -> invocation.getArgument(0));
        ArticuloWebController controller = new ArticuloWebController(service);

        String view = controller.guardar(
                null, " PAN-01 ", "Pan rústico", "Pan", "ud.",
                "1,50", "10", "0,45", "12,5", "Gluten", redirect);

        assertEquals("redirect:/web/articulos", view);
        verify(service).save(org.mockito.ArgumentMatchers.argThat(articulo ->
                articulo.getPvp().compareTo(new BigDecimal("1.50")) == 0
                        && articulo.getIva().compareTo(new BigDecimal("10")) == 0
                        && articulo.getCoste().compareTo(new BigDecimal("0.45")) == 0
                        && articulo.getStock().compareTo(new BigDecimal("12.5")) == 0));
        verify(redirect).addFlashAttribute("exito", "Artículo guardado correctamente");
    }

    @Test
    void cubreListadoFormulariosDetalleEdicionYErroresDeFormato() {
        ArticuloService service = mock(ArticuloService.class);
        Articulo articulo = new Articulo();
        articulo.setId(1L);
        articulo.setCodigo("A1");
        articulo.setNombre("Pan");
        when(service.buscarPaginado(any(), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(articulo)));
        when(service.findById(1L)).thenReturn(Optional.of(articulo));
        when(service.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        ArticuloWebController controller = new ArticuloWebController(service);

        ExtendedModelMap listado = new ExtendedModelMap();
        assertEquals("layout", controller.lista(listado, "pan", 0, 25, "nombre", "asc"));
        assertEquals(articulo, ((List<?>) listado.get("articulos")).get(0));
        assertEquals("layout", controller.nuevo(new ExtendedModelMap()));
        assertEquals("layout", controller.ver(1L, new ExtendedModelMap(), new RedirectAttributesModelMap()));
        assertEquals("layout", controller.editar(1L, new ExtendedModelMap(), new RedirectAttributesModelMap()));
        assertEquals("redirect:/web/articulos", controller.ver(9L, new ExtendedModelMap(), new RedirectAttributesModelMap()));
        assertEquals("redirect:/web/articulos", controller.editar(9L, new ExtendedModelMap(), new RedirectAttributesModelMap()));

        RedirectAttributesModelMap ra = new RedirectAttributesModelMap();
        controller.guardar(1L, "A1", "Pan", "Pan", "ud",
                "1.234,56", "4", "1,000.25", "", null, ra);
        verify(service).save(org.mockito.ArgumentMatchers.argThat(a ->
                a.getPvp().compareTo(new BigDecimal("1234.56")) == 0
                        && a.getCoste().compareTo(new BigDecimal("1000.25")) == 0
                        && a.getStock().compareTo(BigDecimal.ZERO) == 0));

        controller.guardar(null, "A2", "Mal", null, null,
                "no-numero", null, null, null, null, ra);
        assertNotNull(ra.getFlashAttributes().get("error"));
    }
}
