package alicanteweb.erp.controller.web;

import alicanteweb.erp.entities.*;
import alicanteweb.erp.service.*;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Controller
@RequestMapping("/web/lotes")
class LoteWebController {
    private final LoteService service;
    private final ArticuloService articuloService;
    public LoteWebController(LoteService service, ArticuloService articuloService) { this.service = service; this.articuloService = articuloService; }
    @GetMapping
    public String lista(HttpSession s, Model m, @RequestParam(required=false) String q) {
        if(WebController.requireLogin(s)) return "redirect:/web/login";
        List<Lote> items = service.findAll();
        if(q != null && !q.isBlank()) { String t = q.toLowerCase(); items = items.stream().filter(l -> (l.getCodigo()!=null&&l.getCodigo().toLowerCase().contains(t))||(l.getOrigen()!=null&&l.getOrigen().toLowerCase().contains(t))).toList(); }
        m.addAttribute("moduloActivo","lotes"); m.addAttribute("titulo","Lotes / Trazabilidad"); m.addAttribute("lotes",items);
        return WebController.layout(m, "lotes/lista");
    }
    @GetMapping("/nuevo")
    public String nuevo(HttpSession s, Model m) { if(WebController.requireLogin(s)) return "redirect:/web/login"; m.addAttribute("moduloActivo","lotes"); m.addAttribute("titulo","Nuevo lote"); m.addAttribute("articulos", articuloService.findAll()); return WebController.layout(m, "lotes/formulario"); }
    @PostMapping
    public String guardar(HttpSession s, @RequestParam(required=false) Long id, @RequestParam String codigo, @RequestParam Long articuloId, @RequestParam String fechaProd, @RequestParam String fechaCad, @RequestParam(required=false) BigDecimal cantidad, @RequestParam(required=false) String origen, @RequestParam(required=false) String registroSanitario, RedirectAttributes ra) {
        if(WebController.requireLogin(s)) return "redirect:/web/login";
        try { Lote l = id!=null ? service.findById(id).orElse(new Lote()) : new Lote(); l.setCodigo(codigo); articuloService.findById(articuloId).ifPresent(l::setArticulo); l.setFechaProduccion(LocalDate.parse(fechaProd)); l.setFechaCaducidad(LocalDate.parse(fechaCad)); l.setCantidadInicial(cantidad); l.setOrigen(origen); l.setNumeroRegistroSanitario(registroSanitario); service.save(l); ra.addFlashAttribute("exito","Lote guardado"); } catch(Exception e) { ra.addFlashAttribute("error", e.getMessage()); }
        return "redirect:/web/lotes";
    }
}
