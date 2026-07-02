package alicanteweb.erp.controller.web;

import alicanteweb.erp.service.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/web/buscar")
@PreAuthorize("isAuthenticated()")
public class BusquedaWebController {

    private static final Logger log = LoggerFactory.getLogger(BusquedaWebController.class);

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
    public String buscar(@RequestParam(required = false) String q, Model m) {
        if (q == null || q.isBlank()) {
            m.addAttribute("moduloActivo","buscar");
            m.addAttribute("titulo","Búsqueda global");
            m.addAttribute("query", "");
            m.addAttribute("resultados", List.of());
            m.addAttribute("breadcrumb", BreadcrumbBuilder.of(
                BreadcrumbBuilder.link("Inicio", "/web/dashboard"),
                BreadcrumbBuilder.active("Búsqueda global")));
            return WebController.layout(m, "buscar/resultados");
        }

        List<Map<String, String>> resultados = new ArrayList<>();

        clienteService.searchByNombre(q).stream().limit(5)
            .forEach(c -> resultados.add(Map.of("tipo","Cliente","texto",c.getNombre(),"url","/web/clientes/"+c.getId())));

        articuloService.search(q, 5)
            .forEach(a -> resultados.add(Map.of("tipo","Artículo","texto",a.getNombre(),"url","/web/articulos/"+a.getId())));

        facturaService.searchByNumero(q).stream().limit(5)
            .forEach(f -> resultados.add(Map.of("tipo","Factura","texto",f.getNumero(),"url","/web/facturas/"+f.getId())));

        recetaService.searchByNombre(q).stream().limit(5)
            .forEach(r -> resultados.add(Map.of("tipo","Receta","texto",r.getNombre(),"url","/web/recetas/"+r.getId())));

        loteService.searchByCodigo(q).stream().limit(5)
            .forEach(l -> resultados.add(Map.of("tipo","Lote","texto",l.getCodigo(),"url","/web/lotes/"+l.getId())));

        m.addAttribute("moduloActivo","buscar");
        m.addAttribute("titulo","Búsqueda: " + q);
        m.addAttribute("query", q);
        m.addAttribute("resultados", resultados);
        m.addAttribute("breadcrumb", BreadcrumbBuilder.of(
            BreadcrumbBuilder.link("Inicio", "/web/dashboard"),
            BreadcrumbBuilder.active("Búsqueda global")));
        return WebController.layout(m, "buscar/resultados");
    }
}
