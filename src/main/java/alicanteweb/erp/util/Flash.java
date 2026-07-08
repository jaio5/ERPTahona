package alicanteweb.erp.util;

import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Mensajes flash centralizados para los controladores web.
 *
 * <p>Unifica en un único punto las claves que las plantillas leen para pintar
 * las alertas ({@code exito} y {@code error}), evitando que se repartan como
 * literales por decenas de controladores y que una errata o un renombrado
 * provoque que un mensaje deje de mostrarse.
 */
public final class Flash {

    private Flash() {}

    /** Mensaje de operación correcta (alerta verde). */
    public static void exito(RedirectAttributes ra, String mensaje) {
        ra.addFlashAttribute("exito", mensaje);
    }

    /** Mensaje de error (alerta roja). */
    public static void error(RedirectAttributes ra, String mensaje) {
        ra.addFlashAttribute("error", mensaje);
    }
}
