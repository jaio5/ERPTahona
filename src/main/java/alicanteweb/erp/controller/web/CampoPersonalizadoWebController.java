package alicanteweb.erp.controller.web;

import alicanteweb.erp.entities.CampoPersonalizado;
import alicanteweb.erp.entities.Cliente;
import alicanteweb.erp.service.CampoPersonalizadoService;
import alicanteweb.erp.service.ClienteService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

/** CRUD de los campos personalizados del formato de impresión (Ajustes). */
@Controller
@PreAuthorize("@permisos.puede('configuracion', 'ver')")
@RequestMapping("/web/campos-impresion")
public class CampoPersonalizadoWebController {

    private static final Logger log = LoggerFactory.getLogger(CampoPersonalizadoWebController.class);

    private final CampoPersonalizadoService service;
    private final ClienteService clienteService;

    public CampoPersonalizadoWebController(CampoPersonalizadoService service, ClienteService clienteService) {
        this.service = service;
        this.clienteService = clienteService;
    }

    @GetMapping
    public String lista(Model m) {
        m.addAttribute("moduloActivo", "camposImpresion");
        m.addAttribute("titulo", "Campos de impresión");
        m.addAttribute("campos", service.findAll());
        // id -> nombre para mostrar el cliente de cada campo sin acceso lazy en la plantilla
        Map<Long, String> clientesById = clienteService.findAll().stream()
            .collect(Collectors.toMap(Cliente::getId, Cliente::getNombre, (a, b) -> a, LinkedHashMap::new));
        m.addAttribute("clientesById", clientesById);
        m.addAttribute("breadcrumb", BreadcrumbBuilder.of(
            BreadcrumbBuilder.inicio(),
            BreadcrumbBuilder.link("Administración", "#"),
            BreadcrumbBuilder.active("Campos de impresión")));
        return WebController.layout(m, "campos-impresion/lista");
    }

    @GetMapping("/nuevo")
    public String nuevo(Model m) {
        return formulario(m, new CampoPersonalizado(), "Nuevo campo de impresión");
    }

    @GetMapping("/{id}/editar")
    public String editar(@PathVariable Long id, Model m, RedirectAttributes ra) {
        return service.findById(id)
            .map(c -> formulario(m, c, "Editar campo de impresión"))
            .orElseGet(() -> {
                ra.addFlashAttribute("error", "Campo no encontrado");
                return "redirect:/web/campos-impresion";
            });
    }

    private String formulario(Model m, CampoPersonalizado campo, String titulo) {
        m.addAttribute("moduloActivo", "camposImpresion");
        m.addAttribute("titulo", titulo);
        m.addAttribute("campo", campo);
        m.addAttribute("clientes", clienteService.findAll());
        m.addAttribute("breadcrumb", BreadcrumbBuilder.of(
            BreadcrumbBuilder.inicio(),
            BreadcrumbBuilder.link("Campos de impresión", "/web/campos-impresion"),
            BreadcrumbBuilder.active(titulo)));
        return WebController.layout(m, "campos-impresion/formulario");
    }

    @PostMapping
    public String guardar(@RequestParam(required = false) Long id,
                          @RequestParam String ambito,
                          @RequestParam(required = false) Long clienteId,
                          @RequestParam String etiqueta,
                          @RequestParam(required = false) String valor,
                          @RequestParam String ubicacion,
                          @RequestParam String documento,
                          @RequestParam(required = false, defaultValue = "0") Integer orden,
                          @RequestParam(required = false, defaultValue = "false") boolean activo,
                          RedirectAttributes ra) {
        try {
            CampoPersonalizado c = id != null
                ? service.findById(id).orElseThrow(() -> new IllegalArgumentException("Campo no válido: " + id))
                : new CampoPersonalizado();
            c.setAmbito(ambito);
            c.setClienteId(clienteId);
            c.setEtiqueta(etiqueta);
            c.setValor(valor);
            c.setUbicacion(ubicacion);
            c.setDocumento(documento);
            c.setOrden(orden != null ? orden : 0);
            c.setActivo(activo);
            service.save(c);
            ra.addFlashAttribute("exito", "Campo guardado");
        } catch (RuntimeException e) {
            log.error("Error al guardar campo de impresión: {}", e.getMessage(), e);
            ra.addFlashAttribute("error", e.getMessage());
            if (id != null) return "redirect:/web/campos-impresion/" + id + "/editar";
            return "redirect:/web/campos-impresion/nuevo";
        }
        return "redirect:/web/campos-impresion";
    }

    @PostMapping("/{id}/eliminar")
    public String eliminar(@PathVariable Long id, RedirectAttributes ra) {
        try {
            service.deleteById(id);
            ra.addFlashAttribute("exito", "Campo eliminado");
        } catch (RuntimeException e) {
            log.error("Error al eliminar campo de impresión {}: {}", id, e.getMessage(), e);
            ra.addFlashAttribute("error", "No se pudo eliminar el campo");
        }
        return "redirect:/web/campos-impresion";
    }
}
