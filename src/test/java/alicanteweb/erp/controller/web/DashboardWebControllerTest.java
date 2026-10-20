package alicanteweb.erp.controller.web;

import alicanteweb.erp.ErpWebApplication;
import alicanteweb.erp.service.DashboardService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Panel: fila "Hoy" renderizada, importes solo con ventas:ver y Alpine no bloquea la página. */
@SpringBootTest(classes = ErpWebApplication.class)
@AutoConfigureMockMvc
class DashboardWebControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private DashboardService dashboardService;

    private LocalDate hoy;

    @BeforeEach
    void setUp() {
        hoy = LocalDate.now();
        when(dashboardService.resumenDia(any())).thenReturn(new DashboardService.ResumenDia(hoy,
                2, new BigDecimal("9.50"),
                1, new BigDecimal("100.00"),
                3,
                4, new BigDecimal("1234.50")));
    }

    private String html(MvcResult result) throws Exception {
        return result.getResponse().getContentAsString(StandardCharsets.UTF_8);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void elPanelMuestraElResumenDelDiaConImportesYEnlacesFiltrados() throws Exception {
        MvcResult result = mockMvc.perform(get("/web/dashboard"))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("resumenDia", "totalFacturas", "ordenesActivas"))
                .andReturn();
        String html = html(result);

        assertThat(html).contains("Mostrador: 2 tickets · 9,50 €");
        assertThat(html).contains("Facturado: 100,00 €");
        assertThat(html).contains("1.234,50 €");
        String dia = hoy.toString();
        assertThat(html).contains("/web/facturas?fechaDesde=" + dia + "&amp;fechaHasta=" + dia);
        assertThat(html).contains("/web/albaranes?fechaDesde=" + dia + "&amp;fechaHasta=" + dia);
        assertThat(html).contains("/web/facturas?estado=VENCIDAS");
    }

    @Test
    @WithMockUser(roles = "USUARIO")
    void usuarioSinVentasVerNoVeImportesYElPanelCarga() throws Exception {
        MvcResult result = mockMvc.perform(get("/web/dashboard"))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("resumenDia"))
                .andReturn();
        String html = html(result);

        assertThat(html).doesNotContain("9,50 €");
        assertThat(html).doesNotContain("100,00 €");
        assertThat(html).doesNotContain("Mostrador:");
        assertThat(html).doesNotContain("estado=VENCIDA");
        // El recuento de albaranes no lleva importes: sigue visible
        assertThat(html).contains("Hoy · Albaranes");
        // ...pero sin enlace a un listado al que no tiene acceso
        assertThat(html).doesNotContain("/web/albaranes?fechaDesde=");
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void elContenedorRaizNoLlevaXCloak() throws Exception {
        String html = html(mockMvc.perform(get("/web/dashboard"))
                .andExpect(status().isOk())
                .andReturn());

        assertThat(html).contains("x-data=\"dashboardApp\"");
        assertThat(html).doesNotContain("x-data=\"dashboardApp\" x-cloak");
        // x-cloak solo en el bloque de alertas
        assertThat(html).contains("x-show=\"hasAlertas()\" x-cloak");
    }
}
