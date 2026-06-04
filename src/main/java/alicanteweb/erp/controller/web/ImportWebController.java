package alicanteweb.erp.controller.web;

import alicanteweb.erp.service.*;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/web/importar")
class ImportWebController {
    private final ImportService importService;
    private final ClienteService clienteService;
    private final ArticuloService articuloService;

    public ImportWebController(ImportService is, ClienteService cs, ArticuloService as) {
        this.importService = is; this.clienteService = cs; this.articuloService = as;
    }

    @GetMapping
    public String form(HttpSession s, Model m) {
        if (WebController.requireLogin(s)) return "redirect:/web/login";
        m.addAttribute("moduloActivo","importar"); m.addAttribute("titulo","Importar datos");
        return WebController.layout(m, "importar/form");
    }

    @PostMapping("/clientes")
    public String importarClientes(HttpSession s, @RequestParam MultipartFile archivo, RedirectAttributes ra) {
        if (WebController.requireLogin(s)) return "redirect:/web/login";
        var r = importService.importarClientes(archivo, clienteService);
        ra.addFlashAttribute("resultado", r);
        return "redirect:/web/importar";
    }

    @PostMapping("/articulos")
    public String importarArticulos(HttpSession s, @RequestParam MultipartFile archivo, RedirectAttributes ra) {
        if (WebController.requireLogin(s)) return "redirect:/web/login";
        var r = importService.importarArticulos(archivo, articuloService);
        ra.addFlashAttribute("resultado", r);
        return "redirect:/web/importar";
    }
}
