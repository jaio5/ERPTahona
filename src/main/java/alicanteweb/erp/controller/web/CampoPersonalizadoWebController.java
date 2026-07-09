package alicanteweb.erp.controller.web;

import alicanteweb.erp.util.Flash;
import alicanteweb.erp.entities.CampoPersonalizado;
import alicanteweb.erp.entities.Cliente;
import alicanteweb.erp.entities.enums.CampoSistema;
import alicanteweb.erp.service.CampoPersonalizadoService;
import alicanteweb.erp.service.ClienteService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/** CRUD de los campos del formato de impresión (Administración). */
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

    /** clave del catálogo -> etiqueta, para mostrar el nombre de los campos de sistema. */
    private Map<String, String> catalogo() {
        Map<String, String> m = new LinkedHashMap<>();
        for (CampoSistema cs : CampoSistema.values()) m.put(cs.getClave(), cs.getEtiqueta());
        return m;
    }

    @GetMapping
    public String lista(Model m) {
        m.addAttribute("moduloActivo", "camposImpresion");
        m.addAttribute("titulo", "Campos de impresión");
        m.addAttribute("campos", service.findAll());
        m.addAttribute("catalogo", catalogo());
        // id -> nombre para mostrar los clientes de cada campo sin acceso lazy en la plantilla
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
                Flash.error(ra, "Campo no encontrado");
                return "redirect:/web/campos-impresion";
            });
    }

    private String formulario(Model m, CampoPersonalizado campo, String titulo) {
        m.addAttribute("moduloActivo", "camposImpresion");
        m.addAttribute("titulo", titulo);
        m.addAttribute("campo", campo);
        m.addAttribute("camposEmpresa", List.of(
            CampoSistema.EMPRESA_REGISTRO_SANITARIO, CampoSistema.EMPRESA_REGISTRO_MERCANTIL,
            CampoSistema.EMPRESA_IBAN, CampoSistema.EMPRESA_SEPA_CREDITOR, CampoSistema.EMPRESA_TELEFONO,
            CampoSistema.EMPRESA_WHATSAPP, CampoSistema.EMPRESA_EMAIL, CampoSistema.EMPRESA_WEB));
        m.addAttribute("camposCliente", List.of(
            CampoSistema.CLIENTE_CODIGO, CampoSistema.CLIENTE_REPRESENTANTE,
            CampoSistema.CLIENTE_IBAN, CampoSistema.CLIENTE_MANDATO_SEPA));
        m.addAttribute("clientes", clienteService.findAll());
        m.addAttribute("breadcrumb", BreadcrumbBuilder.of(
            BreadcrumbBuilder.inicio(),
            BreadcrumbBuilder.link("Campos de impresión", "/web/campos-impresion"),
            BreadcrumbBuilder.active(titulo)));
        return WebController.layout(m, "campos-impresion/formulario");
    }

    @PreAuthorize("@permisos.puede('configuracion', 'editar')")
    @PostMapping
    public String guardar(@RequestParam(required = false) Long id,
                          @RequestParam(required = false) String claveSistema,
                          @RequestParam(required = false) String etiqueta,
                          @RequestParam(required = false) String valor,
                          @RequestParam String visibilidad,
                          @RequestParam(required = false) List<Long> clienteIds,
                          @RequestParam String ubicacion,
                          @RequestParam String documento,
                          @RequestParam(required = false, defaultValue = "0") Integer orden,
                          @RequestParam(required = false, defaultValue = "false") boolean activo,
                          RedirectAttributes ra) {
        try {
            CampoPersonalizado c = id != null
                ? service.findById(id).orElseThrow(() -> new IllegalArgumentException("Campo no válido: " + id))
                : new CampoPersonalizado();
            boolean sistema = claveSistema != null && !claveSistema.isBlank();
            c.setOrigen(sistema ? CampoPersonalizado.ORIGEN_SISTEMA : CampoPersonalizado.ORIGEN_PROPIO);
            c.setClaveSistema(sistema ? claveSistema : null);
            c.setEtiqueta(etiqueta != null && !etiqueta.isBlank() ? etiqueta.trim() : null);
            c.setValor(valor);
            c.setVisibilidad(visibilidad);
            c.getClientes().clear();
            if (clienteIds != null) c.getClientes().addAll(new HashSet<>(clienteIds));
            c.setUbicacion(ubicacion);
            c.setDocumento(documento);
            c.setOrden(orden != null ? orden : 0);
            c.setActivo(activo);
            service.save(c);
            Flash.exito(ra, "Campo guardado");
        } catch (RuntimeException e) {
            log.error("Error al guardar campo de impresión: {}", e.getMessage(), e);
            Flash.error(ra, e.getMessage());
            if (id != null) return "redirect:/web/campos-impresion/" + id + "/editar";
            return "redirect:/web/campos-impresion/nuevo";
        }
        return "redirect:/web/campos-impresion";
    }

    @PreAuthorize("@permisos.puede('configuracion', 'editar')")
    @PostMapping("/{id}/eliminar")
    public String eliminar(@PathVariable Long id, RedirectAttributes ra) {
        try {
            service.deleteById(id);
            Flash.exito(ra, "Campo eliminado");
        } catch (RuntimeException e) {
            log.error("Error al eliminar campo de impresión {}: {}", id, e.getMessage(), e);
            Flash.error(ra, "No se pudo eliminar el campo");
        }
        return "redirect:/web/campos-impresion";
    }
}
