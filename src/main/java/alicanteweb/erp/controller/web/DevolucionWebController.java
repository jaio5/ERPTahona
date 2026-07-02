package alicanteweb.erp.controller.web;

import alicanteweb.erp.entities.Devolucion;
import alicanteweb.erp.service.ClienteService;
import alicanteweb.erp.service.DevolucionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;

@Controller
@PreAuthorize("@permisos.puede('ventas', 'ver')")
@RequestMapping("/web/devoluciones")
public class DevolucionWebController {

    private static final Logger log = LoggerFactory.getLogger(DevolucionWebController.class);

    private final DevolucionService devolucionService;
    private final ClienteService clienteService;

    public DevolucionWebController(DevolucionService devolucionService, ClienteService clienteService) {
        this.devolucionService = devolucionService;
        this.clienteService = clienteService;
    }

    @GetMapping
    public String lista(Model model, @RequestParam(required = false) String q) {
        List<Devolucion> devoluciones = (q != null && !q.isBlank())
                ? devolucionService.buscar(q) : devolucionService.findAll();
        model.addAttribute("moduloActivo", "devoluciones");
        model.addAttribute("titulo", "Devoluciones");
        model.addAttribute("devoluciones", devoluciones);
        model.addAttribute("q", q);
        model.addAttribute("breadcrumb", BreadcrumbBuilder.of(
            BreadcrumbBuilder.link("Inicio", "/web/dashboard"),
            BreadcrumbBuilder.active("Devoluciones")));
        return WebController.layout(model, "devoluciones/lista");
    }

    @GetMapping("/nuevo")
    public String formularioNuevo(Model model) {
        model.addAttribute("moduloActivo", "devoluciones");
        model.addAttribute("titulo", "Nueva devolución");
        model.addAttribute("clientes", clienteService.findAll());
        model.addAttribute("breadcrumb", BreadcrumbBuilder.of(
            BreadcrumbBuilder.link("Inicio", "/web/dashboard"),
            BreadcrumbBuilder.link("Devoluciones", "/web/devoluciones"),
            BreadcrumbBuilder.active("Nueva devolución")));
        return WebController.layout(model, "devoluciones/formulario");
    }

    @GetMapping("/{id}/editar")
    public String editar(@PathVariable Long id, Model model, RedirectAttributes ra) {
        return devolucionService.findById(id).map(d -> {
            model.addAttribute("moduloActivo", "devoluciones");
            model.addAttribute("titulo", "Editar devolución");
            model.addAttribute("devolucion", d);
            model.addAttribute("clientes", clienteService.findAll());
            return WebController.layout(model, "devoluciones/formulario");
        }).orElseGet(() -> { ra.addFlashAttribute("error", "Registro no encontrado"); return "redirect:/web/devoluciones"; });
    }

    @GetMapping("/{id}")
    public String ver(@PathVariable Long id, Model model, RedirectAttributes ra) {
        return devolucionService.findDetailById(id).map(d -> {
            model.addAttribute("moduloActivo", "devoluciones");
            model.addAttribute("titulo", "Devolución " + d.getNumero());
            model.addAttribute("devolucion", d);
            return WebController.layout(model, "devoluciones/ver");
        }).orElseGet(() -> { ra.addFlashAttribute("error", "Registro no encontrado"); return "redirect:/web/devoluciones"; });
    }

    @PostMapping
    public String guardar(@RequestParam(required = false) Long id,
                          @RequestParam Long clienteId,
                          @RequestParam String numero,
                          @RequestParam(required = false) String motivo,
                          @RequestParam(required = false) String observaciones,
                          RedirectAttributes ra) {
        try {
            Devolucion devolucion = id != null ? devolucionService.findById(id).orElseThrow(() -> new IllegalArgumentException("ID de devolución no válido: " + id)) : new Devolucion();
            clienteService.findById(clienteId).ifPresent(devolucion::setCliente);
            devolucion.setNumero(numero);
            if (devolucion.getFecha() == null) devolucion.setFecha(LocalDate.now());
            devolucion.setMotivo(motivo);
            devolucion.setObservaciones(observaciones);
            devolucionService.save(devolucion);
            ra.addFlashAttribute("exito", "Devolución guardada correctamente");
        } catch (RuntimeException e) {
            log.error("Error al guardar devolución: {}", e.getMessage(), e);
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/web/devoluciones";
    }
}
