package alicanteweb.erp.controller.web;

import alicanteweb.erp.entities.CampoPersonalizado;
import alicanteweb.erp.service.CampoPersonalizadoService;
import alicanteweb.erp.service.ClienteService;
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
class CampoPersonalizadoWebControllerTest {

    @Mock CampoPersonalizadoService service;
    @Mock ClienteService clienteService;
    CampoPersonalizadoWebController controller;

    @BeforeEach
    void setUp() {
        controller = new CampoPersonalizadoWebController(service, clienteService);
    }

    @Test
    void listaYFormularios() {
        when(service.findAll()).thenReturn(List.of());
        when(clienteService.findAll()).thenReturn(List.of());
        assertEquals("layout", controller.lista(new ExtendedModelMap()));
        assertEquals("layout", controller.nuevo(new ExtendedModelMap()));

        when(service.findById(1L)).thenReturn(Optional.of(new CampoPersonalizado()));
        assertEquals("layout", controller.editar(1L, new ExtendedModelMap(), new RedirectAttributesModelMap()));

        when(service.findById(9L)).thenReturn(Optional.empty());
        assertEquals("redirect:/web/campos-impresion",
                controller.editar(9L, new ExtendedModelMap(), new RedirectAttributesModelMap()));
    }

    @Test
    void guardarYEliminar() {
        RedirectAttributesModelMap ra = new RedirectAttributesModelMap();

        assertEquals("redirect:/web/campos-impresion",
                controller.guardar(null, "EMPRESA", null, "Nota", "Pago a 30 días",
                        "PIE", "AMBOS", 1, true, ra));
        verify(service).save(any());

        // Error de validación (cliente obligatorio) -> vuelve al alta
        doThrow(new IllegalArgumentException("cliente")).when(service).save(any());
        assertEquals("redirect:/web/campos-impresion/nuevo",
                controller.guardar(null, "CLIENTE", null, "Ref", null, "CLIENTE", "FACTURA", 0, true, ra));

        assertEquals("redirect:/web/campos-impresion", controller.eliminar(3L, ra));
        verify(service).deleteById(3L);
    }
}
