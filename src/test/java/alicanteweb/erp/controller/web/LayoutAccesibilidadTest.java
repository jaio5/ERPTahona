package alicanteweb.erp.controller.web;

import alicanteweb.erp.ErpWebApplication;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Render real (MockMvc + Thymeleaf) del layout, el TPV y el login: comprueba los atributos de
 * accesibilidad que no se ven en los tests unitarios de controlador (que solo miran el nombre
 * de la vista). Sin parser HTML en el classpath, se trabaja sobre las etiquetas de apertura.
 */
@SpringBootTest(classes = ErpWebApplication.class)
@AutoConfigureMockMvc
class LayoutAccesibilidadTest {

    private static final Pattern ETIQUETA = Pattern.compile("<([a-zA-Z][a-zA-Z0-9]*)\\b[^>]*>");

    @Autowired
    private MockMvc mockMvc;

    @Test
    @WithMockUser(roles = "ADMIN")
    void layoutExponeAvisosMenuYLogoutAccesibles() throws Exception {
        String html = render(get("/web/tipos-iva"));

        assertTrue(etiquetas(html, "div").stream().anyMatch(t ->
                        t.contains("id=\"toast-container\"") && t.contains("aria-live=\"polite\"")),
                "El contenedor de toasts debe anunciar los avisos (aria-live)");
        assertTrue(etiquetas(html, "nav").stream().anyMatch(t -> t.contains("aria-label=\"Menú principal\"")),
                "La barra lateral debe ser un <nav> etiquetado como 'Menú principal'");
        assertTrue(etiquetas(html, "button").stream().anyMatch(t ->
                        t.contains("id=\"sidebarToggle\"") && t.contains("aria-expanded=\"false\"")),
                "El botón del menú móvil debe declarar aria-expanded");
        assertTrue(etiquetas(html, "a").stream().anyMatch(t ->
                        t.contains("aria-label=\"Panel de trabajo\"") && !t.contains("font-size")),
                "El enlace del panel en la barra superior necesita nombre accesible");

        Matcher logout = Pattern.compile("<form[^>]*action=\"/web/logout\"[^>]*>(.*?)</form>", Pattern.DOTALL)
                .matcher(html);
        assertTrue(logout.find(), "Falta el formulario de logout");
        assertTrue(logout.group(1).contains("data-no-double-click"), "El botón de logout debe llevar data-no-double-click");
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void soloElEnlaceDelModuloActivoLlevaAriaCurrent() throws Exception {
        String html = render(get("/web/tipos-iva"));

        List<String> actuales = etiquetas(html, "a").stream()
                .filter(t -> t.contains("sidebar-link") && t.contains("aria-current=\"page\""))
                .toList();
        assertEquals(1, actuales.size(), "Debe haber exactamente un enlace del menú con aria-current=page: " + actuales);
        assertTrue(actuales.get(0).contains("href=\"/web/tipos-iva\""), actuales.get(0));
        assertTrue(actuales.get(0).contains("active"), "aria-current acompaña a la clase active");
        assertFalse(html.contains("#f97316"), "El color del logotipo debe salir de un token CSS");
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void toastDeErrorNoSeOcultaSoloYTieneCierreAccesible() throws Exception {
        String html = render(get("/web/tipos-iva").flashAttr("error", "No se pudo guardar"));

        assertTrue(etiquetas(html, "div").stream().anyMatch(t ->
                        t.contains("text-bg-danger") && t.contains("role=\"alert\"")
                                && t.contains("data-bs-autohide=\"false\"")),
                "El toast de error debe tener role=alert y no ocultarse solo");
        assertTrue(etiquetas(html, "button").stream().anyMatch(t ->
                        t.contains("btn-close") && t.contains("aria-label=\"Cerrar\"")),
                "El botón de cierre del toast necesita aria-label");
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void toastDeExitoEsUnEstado() throws Exception {
        String html = render(get("/web/tipos-iva").flashAttr("exito", "Guardado"));

        assertTrue(etiquetas(html, "div").stream().anyMatch(t ->
                        t.contains("text-bg-success") && t.contains("role=\"status\"")),
                "El toast de éxito debe tener role=status");
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void tpvAnunciaResultadoYEtiquetaLaBusqueda() throws Exception {
        String html = render(get("/web/tpv"));

        assertTrue(etiquetas(html, "div").stream().anyMatch(t ->
                        t.contains("id=\"tpvResultado\"") && t.contains("aria-live=\"polite\"")),
                "#tpvResultado debe ser una región aria-live");
        assertTrue(etiquetas(html, "span").stream().anyMatch(t ->
                        t.contains("id=\"tpvTotal\"") && t.contains("aria-live=\"polite\"")),
                "El total del ticket debe anunciarse");
        assertTrue(etiquetas(html, "input").stream().anyMatch(t ->
                        t.contains("id=\"tpvBuscar\"") && t.contains("aria-label=\"Buscar artículo\"")),
                "El buscador del TPV necesita aria-label");
        // Los textos de los +/− se generan en JS: basta con que el script los contenga
        assertTrue(html.contains("Quitar una unidad de "), "Falta el aria-label del botón −");
        assertTrue(html.contains("Añadir una unidad de "), "Falta el aria-label del botón +");
    }

    @Test
    void loginNoSugiereUsuarioYAvisaErroresConRole() throws Exception {
        String html = render(get("/web/login").param("error", ""));

        assertFalse(html.contains("placeholder=\"admin\""), "El login no debe sugerir el usuario admin");
        assertFalse(html.contains("⚠"), "El aviso de error no debe depender de glifos");
        assertTrue(etiquetas(html, "div").stream().anyMatch(t ->
                        t.contains("alert-danger") && t.contains("role=\"alert\"")),
                "El error de login debe anunciarse con role=alert");
    }

    private String render(MockHttpServletRequestBuilder peticion) throws Exception {
        return mockMvc.perform(peticion)
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);
    }

    /** Etiquetas de apertura (con sus atributos) de un elemento dado. */
    private static List<String> etiquetas(String html, String elemento) {
        List<String> encontradas = new ArrayList<>();
        Matcher m = ETIQUETA.matcher(html);
        while (m.find()) {
            if (m.group(1).equalsIgnoreCase(elemento)) encontradas.add(m.group());
        }
        return encontradas;
    }
}
