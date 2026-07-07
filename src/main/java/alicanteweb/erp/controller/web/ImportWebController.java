package alicanteweb.erp.controller.web;

import alicanteweb.erp.service.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@PreAuthorize("@permisos.puede('configuracion', 'editar')")
@RequestMapping("/web/importar")
public class ImportWebController {

    private static final Logger log = LoggerFactory.getLogger(ImportWebController.class);

    private final ImportService importService;
    private final ClienteService clienteService;
    private final ArticuloService articuloService;

    public ImportWebController(ImportService is, ClienteService cs, ArticuloService as) {
        this.importService = is; this.clienteService = cs; this.articuloService = as;
    }

    @GetMapping
    public String form(Model m) {
        m.addAttribute("moduloActivo", "importar");
        m.addAttribute("titulo", "Importar datos");
        m.addAttribute("breadcrumb", BreadcrumbBuilder.of(
            BreadcrumbBuilder.inicio(),
            BreadcrumbBuilder.link("Administración", "#"),
            BreadcrumbBuilder.active("Importar datos")));
        return WebController.layout(m, "importar/form");
    }

    @PostMapping("/clientes")
    public String importarClientes(@RequestParam MultipartFile archivo, RedirectAttributes ra) {
        var r = importService.importarClientes(archivo, clienteService);
        ra.addFlashAttribute("resultado", r);
        return "redirect:/web/importar";
    }

    @PostMapping("/articulos")
    public String importarArticulos(@RequestParam MultipartFile archivo, RedirectAttributes ra) {
        var r = importService.importarArticulos(archivo, articuloService);
        ra.addFlashAttribute("resultado", r);
        return "redirect:/web/importar";
    }
}
