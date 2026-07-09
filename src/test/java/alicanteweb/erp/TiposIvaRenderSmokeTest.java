package alicanteweb.erp;

import alicanteweb.erp.entities.CampoPersonalizado;
import alicanteweb.erp.entities.enums.CampoSistema;
import alicanteweb.erp.repository.CampoPersonalizadoRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Set;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Smoke test de render: comprueba que las plantillas Thymeleaf nuevas de la Fase 2 (catálogo
 * de tipos de IVA y los formularios que ahora lo consumen) se renderizan sin errores. Los tests
 * unitarios de controlador solo verifican el nombre de la vista, no el render real.
 */
@SpringBootTest(classes = ErpWebApplication.class)
@AutoConfigureMockMvc
class TiposIvaRenderSmokeTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private CampoPersonalizadoRepository campoRepository;

    @Test
    @WithMockUser(roles = "ADMIN")
    void listaTiposIvaRenderiza() throws Exception {
        mockMvc.perform(get("/web/tipos-iva")).andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void formularioTipoIvaRenderiza() throws Exception {
        mockMvc.perform(get("/web/tipos-iva/nuevo")).andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void formularioFacturaRenderizaConDesplegableIva() throws Exception {
        mockMvc.perform(get("/web/facturas/nueva")).andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void formularioAlbaranRenderizaConDesplegableIva() throws Exception {
        mockMvc.perform(get("/web/albaranes/nuevo")).andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void listadoFacturasRenderizaConSeleccionMultiple() throws Exception {
        mockMvc.perform(get("/web/facturas")).andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void listadoAlbaranesRenderizaConSeleccionMultiple() throws Exception {
        mockMvc.perform(get("/web/albaranes")).andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void camposImpresionRenderiza() throws Exception {
        mockMvc.perform(get("/web/campos-impresion")).andExpect(status().isOk());
        mockMvc.perform(get("/web/campos-impresion/nuevo")).andExpect(status().isOk());
    }

    /**
     * Regresión: la edición de un campo con clientes debe renderizar sin
     * LazyInitializationException al leer el conjunto {@code clientes} en la plantilla.
     */
    @Test
    @WithMockUser(roles = "ADMIN")
    void formularioEdicionConClientesRenderiza() throws Exception {
        CampoPersonalizado campo = new CampoPersonalizado();
        campo.setOrigen(CampoPersonalizado.ORIGEN_SISTEMA);
        campo.setClaveSistema(CampoSistema.EMPRESA_REGISTRO_SANITARIO.getClave());
        campo.setVisibilidad(CampoPersonalizado.VIS_SOLO);
        campo.setClientes(Set.of(1L));
        campo = campoRepository.saveAndFlush(campo);

        mockMvc.perform(get("/web/campos-impresion/{id}/editar", campo.getId()))
                .andExpect(status().isOk());
    }
}
