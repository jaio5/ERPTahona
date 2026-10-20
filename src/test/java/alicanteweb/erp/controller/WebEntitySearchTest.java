package alicanteweb.erp.controller;

import alicanteweb.erp.ErpWebApplication;
import alicanteweb.erp.controller.rest.WebEntityController;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

/**
 * La búsqueda libre (?q=) de la API genérica no debe romper en ningún módulo.
 * Regresión: facturas-compra devolvía 500 porque se aplicaba lower() a campos @Lob.
 */
@SpringBootTest(classes = ErpWebApplication.class)
@AutoConfigureMockMvc
@WithMockUser(roles = "ADMIN")
class WebEntitySearchTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private WebEntityController controller;

    @Test
    void facturasCompraAdmiteBusquedaLibre() throws Exception {
        int status = mockMvc.perform(get("/api/web/entities/facturas-compra").param("q", "abc"))
                .andReturn().getResponse().getStatus();
        assertThat(status).isEqualTo(200);
    }

    @Test
    void ningunModuloFallaConBusquedaLibre() throws Exception {
        List<String> fallos = new ArrayList<>();
        for (String module : controller.modules()) {
            int status = mockMvc.perform(get("/api/web/entities/" + module).param("q", "abc"))
                    .andReturn().getResponse().getStatus();
            if (status >= 500) {
                fallos.add(module + " -> " + status);
            }
        }
        assertThat(fallos).isEmpty();
    }
}
