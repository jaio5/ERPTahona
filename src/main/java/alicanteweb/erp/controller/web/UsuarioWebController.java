package alicanteweb.erp.controller.web;

import alicanteweb.erp.entities.Usuario;
import alicanteweb.erp.service.UsuarioService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@PreAuthorize("@permisos.puede('usuarios', 'ver')")
@RequestMapping("/web/usuarios")
public class UsuarioWebController {

    private static final Logger log = LoggerFactory.getLogger(UsuarioWebController.class);

    private final UsuarioService s;

    public UsuarioWebController(UsuarioService s) {
        this.s = s;
    }

    @GetMapping
    public String lista(Model m,
                        @RequestParam(defaultValue = "0") int page,
                        @RequestParam(defaultValue = "20") int size,
                        @RequestParam(required = false) String q) {
        m.addAttribute("moduloActivo", "usuarios");
        m.addAttribute("titulo", "Usuarios");
        Pageable pageable = PageRequest.of(page, size);
        Page<Usuario> pageResult = s.listarPaginado(q, pageable);
        m.addAttribute("page", pageResult);
        m.addAttribute("usuarios", pageResult.getContent());
        return WebController.layout(m, "usuarios/lista");
    }

    @GetMapping("/nuevo")
    public String nuevo(Model m) {
        m.addAttribute("moduloActivo", "usuarios");
        m.addAttribute("titulo", "Nuevo usuario");
        return WebController.layout(m, "usuarios/formulario");
    }

    @PostMapping
    public String crear(@RequestParam String username,
                        @RequestParam String password,
                        @RequestParam(required = false) String nombre,
                        @RequestParam(required = false) String email,
                        @RequestParam(required = false) String role,
                        RedirectAttributes ra) {
        try {
            Usuario u = new Usuario();
            u.setUsername(username);
            u.setNombre(nombre);
            u.setEmail(email);
            u.setRole(role);
            s.crearUsuario(u, password);
            ra.addFlashAttribute("exito", "Usuario creado correctamente");
        } catch (RuntimeException e) {
            log.error("Error al crear usuario: {}", e.getMessage(), e);
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/web/usuarios";
    }

    @GetMapping("/{id}")
    public String ver(@PathVariable Long id, Model m) {
        Usuario u = s.buscarPorId(id).orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));
        m.addAttribute("moduloActivo", "usuarios");
        m.addAttribute("titulo", "Usuario: " + u.getUsername());
        m.addAttribute("usuario", u);
        return WebController.layout(m, "usuarios/ver");
    }

    @GetMapping("/{id}/editar")
    public String editar(@PathVariable Long id, Model m) {
        Usuario u = s.buscarPorId(id).orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));
        m.addAttribute("moduloActivo", "usuarios");
        m.addAttribute("titulo", "Editar usuario");
        m.addAttribute("usuario", u);
        return WebController.layout(m, "usuarios/formulario");
    }

    @PostMapping("/{id}")
    public String actualizar(@PathVariable Long id,
                             @RequestParam(required = false) String nombre,
                             @RequestParam(required = false) String email,
                             @RequestParam(required = false) String role,
                             RedirectAttributes ra) {
        try {
            Usuario u = s.buscarPorId(id).orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));
            u.setNombre(nombre);
            u.setEmail(email);
            u.setRole(role);
            s.actualizarUsuario(u);
            ra.addFlashAttribute("exito", "Usuario actualizado correctamente");
        } catch (RuntimeException e) {
            log.error("Error al actualizar usuario {}: {}", id, e.getMessage(), e);
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/web/usuarios/" + id;
    }

    @PostMapping("/{id}/password")
    public String cambiarPassword(@PathVariable Long id,
                                  @RequestParam String newPassword,
                                  RedirectAttributes ra) {
        try {
            s.cambiarPasswordAdmin(id, newPassword);
            ra.addFlashAttribute("exito", "Contraseña cambiada correctamente");
        } catch (RuntimeException e) {
            log.error("Error al cambiar contraseña de usuario {}: {}", id, e.getMessage(), e);
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/web/usuarios/" + id + "/editar";
    }

    @PostMapping("/{id}/bloquear")
    public String bloquear(@PathVariable Long id, RedirectAttributes ra) {
        try {
            s.bloquearUsuario(id);
            ra.addFlashAttribute("exito", "Usuario bloqueado");
        } catch (RuntimeException e) {
            log.error("Error al bloquear usuario {}: {}", id, e.getMessage(), e);
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/web/usuarios/" + id;
    }

    @PostMapping("/{id}/desbloquear")
    public String desbloquear(@PathVariable Long id, RedirectAttributes ra) {
        try {
            s.desbloquearUsuario(id);
            ra.addFlashAttribute("exito", "Usuario desbloqueado");
        } catch (RuntimeException e) {
            log.error("Error al desbloquear usuario {}: {}", id, e.getMessage(), e);
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/web/usuarios/" + id;
    }

    @PostMapping("/{id}/desactivar")
    public String desactivar(@PathVariable Long id, RedirectAttributes ra) {
        try {
            s.eliminarUsuario(id);
            ra.addFlashAttribute("exito", "Usuario desactivado");
        } catch (RuntimeException e) {
            log.error("Error al desactivar usuario {}: {}", id, e.getMessage(), e);
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/web/usuarios/" + id;
    }

    @PostMapping("/{id}/activar")
    public String activar(@PathVariable Long id, RedirectAttributes ra) {
        try {
            s.activarUsuario(id);
            ra.addFlashAttribute("exito", "Usuario activado");
        } catch (RuntimeException e) {
            log.error("Error al activar usuario {}: {}", id, e.getMessage(), e);
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/web/usuarios/" + id;
    }
}
