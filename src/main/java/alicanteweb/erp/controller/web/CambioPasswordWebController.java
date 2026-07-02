package alicanteweb.erp.controller.web;

import alicanteweb.erp.service.UsuarioService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Cambio de contraseña del propio usuario. Obligatorio en el primer acceso
 * (ver CambioPasswordObligatorioFilter); accesible también de forma voluntaria.
 */
@Controller
@RequestMapping("/web/cambiar-password")
public class CambioPasswordWebController {

    private final UsuarioService usuarioService;

    public CambioPasswordWebController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @GetMapping
    public String formulario(HttpSession session, Model model) {
        model.addAttribute("obligatorio",
                Boolean.TRUE.equals(session.getAttribute("requiereCambioPassword")));
        return "cambiar-password";
    }

    @PostMapping
    public String cambiar(@RequestParam String oldPassword,
                          @RequestParam String newPassword,
                          @RequestParam String confirmPassword,
                          HttpSession session,
                          Model model) {
        model.addAttribute("obligatorio",
                Boolean.TRUE.equals(session.getAttribute("requiereCambioPassword")));
        Object id = session.getAttribute("usuarioId");
        if (!(id instanceof Number usuarioId)) {
            return "redirect:/web/login";
        }
        if (!newPassword.equals(confirmPassword)) {
            model.addAttribute("error", "La nueva contraseña y su confirmación no coinciden");
            return "cambiar-password";
        }
        try {
            usuarioService.cambiarPassword(usuarioId.longValue(), oldPassword, newPassword);
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            return "cambiar-password";
        }
        session.setAttribute("requiereCambioPassword", false);
        return "redirect:/web/dashboard";
    }
}
