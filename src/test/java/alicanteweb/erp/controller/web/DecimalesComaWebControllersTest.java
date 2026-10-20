package alicanteweb.erp.controller.web;

import alicanteweb.erp.entities.FacturaCompra;
import alicanteweb.erp.entities.Horneada;
import alicanteweb.erp.entities.Lote;
import alicanteweb.erp.entities.Merma;
import alicanteweb.erp.service.FacturaCompraService;
import alicanteweb.erp.service.HorneadaService;
import alicanteweb.erp.service.LoteService;
import alicanteweb.erp.service.MermaService;
import alicanteweb.erp.service.ProveedorService;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Los formularios de compras, mermas, lotes y horneadas aceptan la coma decimal ('100,50', '2,5')
 * y un valor no numérico vuelve al formulario con un Flash.error en lugar de una página 400.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@WithMockUser(roles = "ADMIN")
class DecimalesComaWebControllersTest {

    @Autowired MockMvc mvc;

    @MockBean FacturaCompraService facturaCompraService;
    @MockBean ProveedorService proveedorService;
    @MockBean MermaService mermaService;
    @MockBean LoteService loteService;
    @MockBean HorneadaService horneadaService;

    @Test
    void facturaCompraAceptaImportesConComa() throws Exception {
        mvc.perform(post("/web/facturas-compra").with(csrf())
                        .param("proveedorId", "1")
                        .param("numero", "FC-COMA")
                        .param("fecha", "2026-10-19")
                        .param("baseImponible", "100,50")
                        .param("tipoIva", "21")
                        .param("importeIva", "21,11"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/web/facturas-compra"))
                .andExpect(flash().attributeExists("exito"));

        ArgumentCaptor<FacturaCompra> captor = ArgumentCaptor.forClass(FacturaCompra.class);
        verify(facturaCompraService).guardar(captor.capture());
        assertEquals(new BigDecimal("100.50"), captor.getValue().getBaseImponible());
        assertEquals(new BigDecimal("21"), captor.getValue().getTipoIva());
        assertEquals(new BigDecimal("21.11"), captor.getValue().getImporteIva());
    }

    @Test
    void facturaCompraAceptaMilesConPunto() throws Exception {
        mvc.perform(post("/web/facturas-compra").with(csrf())
                        .param("proveedorId", "1")
                        .param("numero", "FC-MILES")
                        .param("baseImponible", "1.234,50")
                        .param("tipoIva", "21")
                        .param("importeIva", "259.25"))
                .andExpect(status().is3xxRedirection());

        ArgumentCaptor<FacturaCompra> captor = ArgumentCaptor.forClass(FacturaCompra.class);
        verify(facturaCompraService).guardar(captor.capture());
        assertEquals(new BigDecimal("1234.50"), captor.getValue().getBaseImponible());
        assertEquals(new BigDecimal("259.25"), captor.getValue().getImporteIva());
    }

    @Test
    void facturaCompraConImporteInvalidoVuelveAlFormularioConError() throws Exception {
        mvc.perform(post("/web/facturas-compra").with(csrf())
                        .param("proveedorId", "1")
                        .param("numero", "FC-MAL")
                        .param("baseImponible", "abc"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/web/facturas-compra/nuevo"))
                .andExpect(flash().attribute("error", "Importe no válido: abc"));

        verify(facturaCompraService, never()).guardar(any());
    }

    @Test
    void mermaAceptaCantidadConComa() throws Exception {
        mvc.perform(post("/web/mermas").with(csrf())
                        .param("articuloId", "1")
                        .param("cantidad", "2,5")
                        .param("tipo", "CADUCIDAD"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/web/mermas"));

        ArgumentCaptor<Merma> captor = ArgumentCaptor.forClass(Merma.class);
        verify(mermaService).save(captor.capture());
        assertEquals(new BigDecimal("2.5"), captor.getValue().getCantidad());
    }

    @Test
    void mermaConCantidadInvalidaVuelveAlFormularioConError() throws Exception {
        mvc.perform(post("/web/mermas").with(csrf())
                        .param("articuloId", "1")
                        .param("cantidad", "abc"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/web/mermas/nueva"))
                .andExpect(flash().attribute("error", "Cantidad no válida: abc"));

        verify(mermaService, never()).save(any());
    }

    @Test
    void loteAceptaCantidadConComa() throws Exception {
        mvc.perform(post("/web/lotes").with(csrf())
                        .param("codigo", "L-COMA")
                        .param("fechaProd", "2026-10-19")
                        .param("fechaCad", "2026-10-25")
                        .param("cantidad", "2,5"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/web/lotes"));

        ArgumentCaptor<Lote> captor = ArgumentCaptor.forClass(Lote.class);
        verify(loteService).save(captor.capture());
        assertEquals(new BigDecimal("2.5"), captor.getValue().getCantidadInicial());
    }

    @Test
    void loteConCantidadInvalidaVuelveAlFormularioConError() throws Exception {
        mvc.perform(post("/web/lotes").with(csrf())
                        .param("codigo", "L-MAL")
                        .param("fechaProd", "2026-10-19")
                        .param("fechaCad", "2026-10-25")
                        .param("cantidad", "abc"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/web/lotes/nuevo"))
                .andExpect(flash().attribute("error", "Cantidad no válida: abc"));

        verify(loteService, never()).save(any());
    }

    @Test
    void horneadaAceptaCantidadYCostesConComa() throws Exception {
        mvc.perform(post("/web/horneadas").with(csrf())
                        .param("tipo", "PAN")
                        .param("cantidad", "2,5")
                        .param("costeManoObra", "100,50")
                        .param("costeEnergia", "12.5")
                        .param("costeMateriales", "1.234,50"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/web/horneadas"));

        ArgumentCaptor<Horneada> captor = ArgumentCaptor.forClass(Horneada.class);
        verify(horneadaService).save(captor.capture());
        assertEquals(new BigDecimal("2.5"), captor.getValue().getCantidadProducida());
        assertEquals(new BigDecimal("100.50"), captor.getValue().getCosteManoObra());
        assertEquals(new BigDecimal("12.5"), captor.getValue().getCosteEnergia());
        assertEquals(new BigDecimal("1234.50"), captor.getValue().getCosteMateriales());
    }

    @Test
    void horneadaConImporteInvalidoVuelveAlFormularioConError() throws Exception {
        mvc.perform(post("/web/horneadas").with(csrf())
                        .param("tipo", "PAN")
                        .param("costeEnergia", "abc"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/web/horneadas/nuevo"))
                .andExpect(flash().attribute("error", "Importe no válido: abc"));

        verify(horneadaService, never()).save(any());
    }
}
