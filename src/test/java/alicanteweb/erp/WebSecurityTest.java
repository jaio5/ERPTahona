package alicanteweb.erp;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrlPattern;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = ErpWebApplication.class)
@AutoConfigureMockMvc
class WebSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void loginEsPublico() throws Exception {
        mockMvc.perform(get("/web/login"))
                .andExpect(status().isOk());
    }

    @Test
    void webProtegidaRedirigeALoginSinSesion() throws Exception {
        mockMvc.perform(get("/web/dashboard"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/web/login"));
    }

    @Test
    void apiProtegidaRechazaAnonimos() throws Exception {
        mockMvc.perform(get("/api/web/entities"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser
    void appSpaProtegidaPermiteUsuarioAutenticado() throws Exception {
        mockMvc.perform(get("/web/app"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/web/dashboard"));
    }

    @Test
    @WithMockUser
    void logoutPostRequiereCsrfValido() throws Exception {
        mockMvc.perform(post("/web/logout").with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/web/login?logout"));
    }

    @Test
    @WithMockUser
    void apiPostRechazaJsonSinCsrf() throws Exception {
        mockMvc.perform(post("/api/web/clientes")
                        .contentType("application/json")
                        .content("{}"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser
    void apiPostConCsrfLlegaAlControlador() throws Exception {
        mockMvc.perform(post("/api/web/clientes")
                        .with(csrf())
                        .contentType("application/json")
                        .content("{}"))
                .andExpect(status().is4xxClientError());
    }

    @Test
    @WithMockUser(roles = "USUARIO")
    void usuarioNormalNoPuedeAccederAAdministracion() throws Exception {
        // Las rutas web redirigen al usuario al acceso-denegado (302) en lugar de 403 en bruto
        mockMvc.perform(get("/web/usuarios"))
                .andExpect(status().is3xxRedirection());
        // Las rutas API devuelven 403 JSON directamente
        mockMvc.perform(get("/api/web/entities/auditoria"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void superficiesFiscalesRenderizanParaUsuarioAutenticado() throws Exception {
        mockMvc.perform(get("/web/modelo347").param("ejercicio", "2025"))
                .andExpect(status().isOk());
        mockMvc.perform(get("/web/verifactu"))
                .andExpect(status().isOk());
    }
}
