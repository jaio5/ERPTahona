package alicanteweb.erp.controller.web;

import alicanteweb.erp.entities.Usuario;
import alicanteweb.erp.service.UsuarioService;
import jakarta.servlet.http.HttpSession;

public abstract class BaseWebController {

    private final UsuarioService usuarioService;

    protected BaseWebController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    protected Usuario usuarioActual(HttpSession session) {
        Object id = session != null ? session.getAttribute("usuarioId") : null;
        if (id instanceof Long uid) {
            return usuarioService.buscarPorId(uid).orElse(null);
        }
        if (id instanceof Number n) {
            return usuarioService.buscarPorId(n.longValue()).orElse(null);
        }
        return null;
    }
}
