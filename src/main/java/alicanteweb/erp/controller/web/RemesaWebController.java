package alicanteweb.erp.controller.web;

import alicanteweb.erp.entities.RemesaSepa;
import alicanteweb.erp.service.SepaService;
import alicanteweb.erp.service.UsuarioService;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import alicanteweb.erp.util.Descargas;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.List;

/**
 * Gestión de remesas SEPA de adeudos directos.
 */
@Controller
@PreAuthorize("@permisos.puede('tesoreria', 'ver')")
@RequestMapping("/web/remesas")
public class RemesaWebController extends BaseWebController {

    private static final Logger log = LoggerFactory.getLogger(RemesaWebController.class);

    private final SepaService sepaService;

    public RemesaWebController(SepaService sepaService, UsuarioService usuarioService) {
        super(usuarioService);
        this.sepaService = sepaService;
    }

    @GetMapping
    public String remesas(Model m) {
        m.addAttribute("moduloActivo", "remesas");
        m.addAttribute("titulo", "Remesas SEPA");
        m.addAttribute("remesas", sepaService.listar());
        m.addAttribute("elegibles", sepaService.facturasElegibles());
        m.addAttribute("fechaCobroDefecto", LocalDate.now().plusDays(3));
        m.addAttribute("breadcrumb", BreadcrumbBuilder.of(
                BreadcrumbBuilder.inicio(),
                BreadcrumbBuilder.link("Finanzas", "#"),
                BreadcrumbBuilder.active("Remesas SEPA")));
        return WebController.layout(m, "remesas/index");
    }

    @PostMapping
    @PreAuthorize("@permisos.puede('tesoreria', 'crear')")
    public String crear(@RequestParam List<Long> facturaIds,
                        @RequestParam(required = false) LocalDate fechaCobro,
                        @RequestParam(required = false) String concepto,
                        HttpSession session,
                        RedirectAttributes ra) {
        try {
            RemesaSepa remesa = sepaService.crearRemesa(facturaIds, fechaCobro, concepto, usuarioActual(session));
            ra.addFlashAttribute("exito", "Remesa " + remesa.getId() + " generada con "
                    + remesa.getNumRecibos() + " recibos");
        } catch (RuntimeException e) {
            log.error("Error al crear remesa SEPA: {}", e.getMessage(), e);
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/web/remesas";
    }

    @GetMapping("/{id}/xml")
    public ResponseEntity<byte[]> descargarXml(@PathVariable Long id) {
        return sepaService.buscarPorId(id)
                .filter(r -> r.getXml() != null)
                .map(r -> Descargas.adjunto(r.getXml().getBytes(StandardCharsets.UTF_8),
                        "remesa-sepa-" + r.getId() + ".xml", MediaType.APPLICATION_XML))
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/{id}/cobrar")
    @PreAuthorize("@permisos.puede('tesoreria', 'crear')")
    public String marcarCobrada(@PathVariable Long id, HttpSession session, RedirectAttributes ra) {
        try {
            sepaService.marcarCobrada(id, usuarioActual(session));
            ra.addFlashAttribute("exito", "Remesa marcada como cobrada; cobros registrados en cartera");
        } catch (RuntimeException e) {
            log.error("Error al cobrar remesa {}: {}", id, e.getMessage(), e);
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/web/remesas";
    }

    @PostMapping("/{id}/anular")
    @PreAuthorize("@permisos.puede('tesoreria', 'editar')")
    public String anular(@PathVariable Long id, HttpSession session, RedirectAttributes ra) {
        try {
            sepaService.anular(id, usuarioActual(session));
            ra.addFlashAttribute("exito", "Remesa anulada");
        } catch (RuntimeException e) {
            log.error("Error al anular remesa {}: {}", id, e.getMessage(), e);
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/web/remesas";
    }
}
