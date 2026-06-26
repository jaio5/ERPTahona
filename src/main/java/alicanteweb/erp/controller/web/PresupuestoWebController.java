package alicanteweb.erp.controller.web;

import alicanteweb.erp.service.ArticuloService;
import alicanteweb.erp.service.ClienteService;
import alicanteweb.erp.service.DocumentoService;
import alicanteweb.erp.service.PresupuestoService;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.security.access.prepost.PreAuthorize;

@Controller
@PreAuthorize("@permisos.puede('ventas', 'ver')")
@RequestMapping("/web/presupuestos")
public class PresupuestoWebController {

    private static final Logger log = LoggerFactory.getLogger(PresupuestoWebController.class);

    private final PresupuestoService presupuestoService;
    private final ClienteService clienteService;
    private final ArticuloService articuloService;
    private final DocumentoService documentoService;

    public PresupuestoWebController(PresupuestoService presupuestoService, ClienteService clienteService,
                                     ArticuloService articuloService, DocumentoService documentoService) {
        this.presupuestoService = presupuestoService;
        this.clienteService = clienteService;
        this.articuloService = articuloService;
        this.documentoService = documentoService;
    }

    @GetMapping
    public String lista(Model model,
                        @RequestParam(required = false) String q,
                        @RequestParam(required = false) String estado,
                        @RequestParam(defaultValue = "0") int page,
                        @RequestParam(defaultValue = "15") int size) {
        Page presupuestos;
        if (q != null && !q.isBlank()) {
            presupuestos = presupuestoService.buscar(q, PageRequest.of(page, size));
        } else if (estado != null && !estado.isBlank()) {
            presupuestos = presupuestoService.buscarPorEstado(estado, PageRequest.of(page, size));
        } else {
            presupuestos = presupuestoService.obtenerTodos(PageRequest.of(page, size));
        }
        model.addAttribute("moduloActivo", "presupuestos");
        model.addAttribute("titulo", "Presupuestos");
        model.addAttribute("presupuestos", presupuestos.getContent());
        model.addAttribute("page", presupuestos);
        model.addAttribute("q", q);
        model.addAttribute("estado", estado);
        return WebController.layout(model, "presupuestos/lista");
    }

    @GetMapping("/nuevo")
    public String formularioNuevo(Model model) {
        model.addAttribute("moduloActivo", "presupuestos");
        model.addAttribute("titulo", "Nuevo presupuesto");
        model.addAttribute("clientes", clienteService.findAll());
        model.addAttribute("articulos", articuloService.findAll());
        model.addAttribute("lineasJson", "[]");
        model.addAttribute("breadcrumb", BreadcrumbBuilder.of(
            BreadcrumbBuilder.link("Inicio", "/web/dashboard"),
            BreadcrumbBuilder.link("Presupuestos", "/web/presupuestos"),
            BreadcrumbBuilder.active("Nuevo presupuesto")));
        return WebController.layout(model, "presupuestos/formulario");
    }

    @GetMapping("/{id}/editar")
    public String editar(@PathVariable Long id, Model model, RedirectAttributes ra) {
        return presupuestoService.obtenerPorId(id).map(p -> {
            String lineasJson = p.getLineas().stream().map(l ->
                String.format("{\"articuloId\":%s,\"cantidad\":\"%s\",\"precio\":\"%s\",\"iva\":\"%s\",\"descuento\":\"%s\"}",
                    l.getArticulo() != null ? l.getArticulo().getId() : "null",
                    DocumentoParserUtil.fmtDecimal(l.getCantidad()),
                    DocumentoParserUtil.fmtDecimal(l.getPrecioUnitario()),
                    DocumentoParserUtil.fmtDecimal(l.getTipoIva()),
                    DocumentoParserUtil.fmtDecimal(l.getDescuento()))
            ).collect(Collectors.joining(",", "[", "]"));
            model.addAttribute("moduloActivo", "presupuestos");
            model.addAttribute("titulo", "Editar presupuesto " + p.getNumero());
            model.addAttribute("presupuesto", p);
            model.addAttribute("clientes", clienteService.findAll());
            model.addAttribute("articulos", articuloService.findAll());
            model.addAttribute("lineasJson", lineasJson);
            model.addAttribute("breadcrumb", BreadcrumbBuilder.of(
                BreadcrumbBuilder.link("Inicio", "/web/dashboard"),
                BreadcrumbBuilder.link("Presupuestos", "/web/presupuestos"),
                BreadcrumbBuilder.active("Editar " + p.getNumero())));
            return WebController.layout(model, "presupuestos/formulario");
        }).orElseGet(() -> { ra.addFlashAttribute("error", "Registro no encontrado"); return "redirect:/web/presupuestos"; });
    }

    @GetMapping("/{id}")
    public String ver(@PathVariable Long id, Model model, RedirectAttributes ra) {
        return presupuestoService.obtenerPorId(id).map(p -> {
            model.addAttribute("moduloActivo", "presupuestos");
            model.addAttribute("titulo", "Presupuesto " + p.getNumero());
            model.addAttribute("presupuesto", p);
            model.addAttribute("breadcrumb", BreadcrumbBuilder.of(
                BreadcrumbBuilder.link("Inicio", "/web/dashboard"),
                BreadcrumbBuilder.link("Presupuestos", "/web/presupuestos"),
                BreadcrumbBuilder.active(p.getNumero())));
            return WebController.layout(model, "presupuestos/ver");
        }).orElseGet(() -> { ra.addFlashAttribute("error", "Registro no encontrado"); return "redirect:/web/presupuestos"; });
    }

    @PostMapping
    public String guardar(@RequestParam(required = false) Long id,
                          HttpServletRequest request,
                          RedirectAttributes ra) {
        try {
            Map<String, Object> datos = new HashMap<>();
            datos.put("clienteId", request.getParameter("clienteId"));
            datos.put("fecha", request.getParameter("fecha"));
            datos.put("fechaValidez", request.getParameter("fechaValidez"));
            datos.put("observaciones", request.getParameter("observaciones"));
            datos.put("lineas", DocumentoParserUtil.construirLineas(
                request.getParameterValues("lineaArticuloId"),
                request.getParameterValues("lineaCantidad"),
                request.getParameterValues("lineaPrecio"),
                request.getParameterValues("lineaIva"),
                request.getParameterValues("lineaDescuento")
            ));
            var presupuesto = documentoService.guardarPresupuesto(id, datos);
            ra.addFlashAttribute("exito", "Presupuesto " + presupuesto.getNumero() + " guardado");
            return "redirect:/web/presupuestos/" + presupuesto.getId();
        } catch (RuntimeException e) {
            log.error("Error al guardar presupuesto: {}", e.getMessage(), e);
            ra.addFlashAttribute("error", e.getMessage());
            return "redirect:/web/presupuestos";
        }
    }

    @PostMapping("/{id}/aprobar")
    public String aprobar(@PathVariable Long id, RedirectAttributes ra) {
        try {
            presupuestoService.aceptar(id);
            ra.addFlashAttribute("exito", "Presupuesto aprobado");
        } catch (RuntimeException e) {
            log.error("Error al aprobar presupuesto {}: {}", id, e.getMessage(), e);
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/web/presupuestos/" + id;
    }
}
