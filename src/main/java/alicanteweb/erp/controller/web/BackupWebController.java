package alicanteweb.erp.controller.web;

import alicanteweb.erp.service.BackupService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Collections;
import org.springframework.security.access.prepost.PreAuthorize;

@Controller
@PreAuthorize("@permisos.puede('backup', 'ver')")
@RequestMapping("/web/backups")
public class BackupWebController {

    private static final Logger log = LoggerFactory.getLogger(BackupWebController.class);

    private final BackupService s;

    public BackupWebController(BackupService s) {
        this.s = s;
    }

    @GetMapping
    public String lista(Model m) {
        m.addAttribute("moduloActivo", "backups");
        m.addAttribute("titulo", "Backups");
        m.addAttribute("breadcrumb", BreadcrumbBuilder.of(
            BreadcrumbBuilder.inicio(),
            BreadcrumbBuilder.link("Administración", "#"),
            BreadcrumbBuilder.active("Backups")));
        try {
            m.addAttribute("backups", s.listarBackups());
        } catch (java.io.IOException e) {
            log.error("Error al listar backups: {}", e.getMessage(), e);
            m.addAttribute("backups", Collections.emptyList());
            m.addAttribute("error", e.getMessage());
        }
        return WebController.layout(m, "backups/lista");
    }

    @PostMapping("/crear")
    public String crear(RedirectAttributes ra) {
        try {
            s.realizarBackup();
            ra.addFlashAttribute("exito", "Backup creado");
        } catch (java.io.IOException | InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("Error al crear backup: {}", e.getMessage(), e);
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/web/backups";
    }

    /**
     * Restaura la base de datos desde un backup. Operación destructiva: solo ADMIN,
     * y exige repetir el nombre del fichero como confirmación explícita. El servicio
     * valida la ruta (anti-traversal) y registra la restauración en auditoría.
     */
    @PostMapping("/restaurar")
    @PreAuthorize("hasAnyRole('ADMIN','ADMINISTRADOR')")
    public String restaurar(@RequestParam String archivo,
                            @RequestParam String confirmacion,
                            RedirectAttributes ra) {
        if (!archivo.equals(confirmacion == null ? "" : confirmacion.trim())) {
            ra.addFlashAttribute("error",
                "Confirmación incorrecta: escribe exactamente el nombre del fichero (" + archivo + ") para restaurar");
            return "redirect:/web/backups";
        }
        log.warn("Restauración de backup solicitada: {} ", archivo);
        try {
            s.restaurarBackup(archivo);
            ra.addFlashAttribute("exito", "Base de datos restaurada desde " + archivo
                + ". Revisa los datos y reinicia sesión el resto de usuarios.");
        } catch (IllegalArgumentException e) {
            log.warn("Restauración rechazada para '{}': {}", archivo, e.getMessage());
            ra.addFlashAttribute("error", e.getMessage());
        } catch (java.io.IOException e) {
            log.error("Error al restaurar backup {}: {}", archivo, e.getMessage(), e);
            ra.addFlashAttribute("error", e.getMessage());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("Restauración interrumpida para {}", archivo, e);
            ra.addFlashAttribute("error", "Restauración interrumpida: " + e.getMessage());
        }
        return "redirect:/web/backups";
    }
}
