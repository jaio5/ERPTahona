package alicanteweb.erp.controller.web;

import alicanteweb.erp.entities.TipoImpositivo;
import alicanteweb.erp.service.TipoImpositivoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.web.servlet.mvc.support.RedirectAttributesModelMap;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TipoImpositivoWebControllerTest {

    @Mock TipoImpositivoService service;
    TipoImpositivoWebController controller;

    @BeforeEach
    void setUp() {
        controller = new TipoImpositivoWebController(service);
    }

    @Test
    void listaYFormularios() {
        when(service.findAll()).thenReturn(List.of());
        assertEquals("layout", controller.lista(new ExtendedModelMap()));
        assertEquals("layout", controller.nuevo(new ExtendedModelMap()));

        when(service.findById(1L)).thenReturn(Optional.of(new TipoImpositivo()));
        assertEquals("layout", controller.editar(1L, new ExtendedModelMap(), new RedirectAttributesModelMap()));

        when(service.findById(9L)).thenReturn(Optional.empty());
        assertEquals("redirect:/web/tipos-iva",
                controller.editar(9L, new ExtendedModelMap(), new RedirectAttributesModelMap()));
    }

    @Test
    void guardarYEliminar() {
        RedirectAttributesModelMap ra = new RedirectAttributesModelMap();

        assertEquals("redirect:/web/tipos-iva",
                controller.guardar(null, "General 21%", "21", "5.2", 4, true, true, ra));
        verify(service).save(any());

        // Error de validación -> vuelve al formulario de alta
        doThrow(new IllegalArgumentException("bad")).when(service).save(any());
        assertEquals("redirect:/web/tipos-iva/nuevo",
                controller.guardar(null, "X", "999", null, 0, true, false, ra));

        assertEquals("redirect:/web/tipos-iva", controller.eliminar(3L, ra));
        verify(service).deleteById(3L);
    }
}
