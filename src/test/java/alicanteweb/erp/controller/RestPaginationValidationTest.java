package alicanteweb.erp.controller;

import alicanteweb.erp.config.RestExceptionHandler;
import alicanteweb.erp.controller.rest.DevolucionRestController;
import alicanteweb.erp.controller.rest.ProduccionRestController;
import alicanteweb.erp.service.*;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.Mockito.mock;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class RestPaginationValidationTest {

    @Test
    void finalizarOrdenValidaCantidades() throws Exception {
        ProduccionRestController controller = new ProduccionRestController(
                mock(RecetaService.class), mock(OrdenProduccionService.class), mock(HorneadaService.class));
        MockMvc mvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new RestExceptionHandler())
                .build();

        mvc.perform(post("/api/produccion/ordenes/1/finalizar")
                        .contentType("application/json")
                        .content("{\"cantidad\":0,\"merma\":-1}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("VALIDATION_ERROR"));
    }

    @Test
    void rechazarDevolucionExigeMotivo() throws Exception {
        DevolucionRestController controller = new DevolucionRestController(mock(DevolucionService.class));
        MockMvc mvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new RestExceptionHandler())
                .build();

        mvc.perform(post("/api/devoluciones/1/rechazar")
                        .contentType("application/json")
                        .content("{\"motivo\":\"\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("VALIDATION_ERROR"));
    }
}
