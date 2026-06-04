package alicanteweb.erp.controller.web;

import alicanteweb.erp.service.*;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@Controller
@RequestMapping("/web/buscar")
class BusquedaWebController {

    private final ClienteService clienteService;
    private final ArticuloService articuloService;
    private final FacturaService facturaService;
    private final RecetaService recetaService;
    private final LoteService loteService;

    public BusquedaWebController(ClienteService cs, ArticuloService as, FacturaService fs,
                                  RecetaService rs, LoteService ls) {
        this.clienteService = cs; this.articuloService = as;
        this.facturaService = fs; this.recetaService = rs; this.loteService = ls;
    }

    @GetMapping
    public String buscar(@RequestParam(required = false) String q, HttpSession s, Model m) {
        if (WebController.requireLogin(s)) return "redirect:/web/login";
        if (q == null || q.isBlank()) {
            m.addAttribute("moduloActivo","buscar");
            m.addAttribute("titulo","Busqueda global");
            m.addAttribute("query", "");
            m.addAttribute("resultados", List.of());
            return WebController.layout(m, "buscar/resultados");
        }

        String t = q.toLowerCase();
        List<Map<String, String>> resultados = new ArrayList<>();

        clienteService.findAll().stream()
            .filter(c -> c.getNombre() != null && c.getNombre().toLowerCase().contains(t))
            .limit(5).forEach(c -> resultados.add(Map.of("tipo","Cliente","texto",c.getNombre(),"url","/web/clientes/"+c.getId())));

        articuloService.findAll().stream()
            .filter(a -> a.getNombre() != null && a.getNombre().toLowerCase().contains(t))
            .limit(5).forEach(a -> resultados.add(Map.of("tipo","Artículo","texto",a.getNombre(),"url","/web/articulos/"+a.getId())));

        facturaService.findAll().stream()
            .filter(f -> f.getNumero() != null && f.getNumero().toLowerCase().contains(t))
            .limit(5).forEach(f -> resultados.add(Map.of("tipo","Factura","texto",f.getNumero(),"url","/web/facturas/"+f.getId())));

        recetaService.findAll().stream()
            .filter(r -> r.getNombre() != null && r.getNombre().toLowerCase().contains(t))
            .limit(5).forEach(r -> resultados.add(Map.of("tipo","Receta","texto",r.getNombre(),"url","/web/recetas/"+r.getId())));

        loteService.findAll().stream()
            .filter(l -> l.getCodigo() != null && l.getCodigo().toLowerCase().contains(t))
            .limit(5).forEach(l -> resultados.add(Map.of("tipo","Lote","texto",l.getCodigo(),"url","/web/lotes/"+l.getId())));

        m.addAttribute("moduloActivo","buscar");
        m.addAttribute("titulo","Buscar: " + q);
        m.addAttribute("query", q);
        m.addAttribute("resultados", resultados);
        return WebController.layout(m, "buscar/resultados");
    }
}
