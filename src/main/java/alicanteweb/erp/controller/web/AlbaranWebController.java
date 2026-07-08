package alicanteweb.erp.controller.web;

import alicanteweb.erp.util.Flash;
import alicanteweb.erp.entities.*;
import alicanteweb.erp.exception.ErpException;
import alicanteweb.erp.service.*;
import alicanteweb.erp.util.Descargas;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import java.io.File;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@PreAuthorize("@permisos.puede('ventas', 'ver')")
@RequestMapping("/web/albaranes")
public class AlbaranWebController extends BaseWebController {

    private static final Logger log = LoggerFactory.getLogger(AlbaranWebController.class);

    /** Campos por los que se permite ordenar el listado (evita inyección en el Sort). */
    private static final java.util.Set<String> SORTS_PERMITIDOS = java.util.Set.of("fecha", "numero", "total");

    private final AlbaranService albaranService;
    private final ClienteService clienteService;
    private final ArticuloService articuloService;
    private final AlmacenService almacenService;
    private final ImpresionService impresionService;
    private final AuditoriaService auditoriaService;
    private final DocumentoService documentoService;

    private final TipoImpositivoService tipoImpositivoService;

    public AlbaranWebController(AlbaranService albaranService, ClienteService clienteService,
                                 ArticuloService articuloService, AlmacenService almacenService,
                                 ImpresionService impresionService, AuditoriaService auditoriaService,
                                 UsuarioService usuarioService, DocumentoService documentoService,
                                 TipoImpositivoService tipoImpositivoService) {
        super(usuarioService);
        this.albaranService = albaranService;
        this.clienteService = clienteService;
        this.articuloService = articuloService;
        this.almacenService = almacenService;
        this.impresionService = impresionService;
        this.auditoriaService = auditoriaService;
        this.documentoService = documentoService;
        this.tipoImpositivoService = tipoImpositivoService;
    }

    @GetMapping
    public String listado(Model model,
                          @RequestParam(required = false) String q,
                          @RequestParam(required = false) String estado,
                          @RequestParam(required = false) @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE) LocalDate fechaDesde,
                          @RequestParam(required = false) @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE) LocalDate fechaHasta,
                          @RequestParam(defaultValue = "0") int page,
                          @RequestParam(defaultValue = "25") int size,
                          @RequestParam(defaultValue = "fecha") String sort,
                          @RequestParam(defaultValue = "desc") String dir) {
        String sortSeguro = SORTS_PERMITIDOS.contains(sort) ? sort : "fecha";
        Sort.Direction direction = "asc".equalsIgnoreCase(dir) ? Sort.Direction.ASC : Sort.Direction.DESC;
        PageRequest pageable = PageRequest.of(page, size, Sort.by(direction, sortSeguro));
        Page<AlbaranVenta> pageResult = albaranService.buscarPaginado(q, estado, fechaDesde, fechaHasta, pageable);

        model.addAttribute("moduloActivo", "albaranes");
        model.addAttribute("titulo", "Albaranes");
        model.addAttribute("albaranes", pageResult.getContent());
        model.addAttribute("page", pageResult);
        model.addAttribute("q", q);
        model.addAttribute("estado", estado);
        model.addAttribute("fechaDesde", fechaDesde);
        model.addAttribute("fechaHasta", fechaHasta);
        model.addAttribute("sort", sortSeguro);
        model.addAttribute("dir", dir);
        model.addAttribute("breadcrumb", BreadcrumbBuilder.of(
            BreadcrumbBuilder.inicio(),
            BreadcrumbBuilder.active("Albaranes")));
        return WebController.layout(model, "albaranes/lista");
    }

    @GetMapping("/nueva")
    public String nuevaAlias() {
        return "redirect:/web/albaranes/nuevo";
    }

    @GetMapping("/nuevo")
    @PreAuthorize("@permisos.puede('ventas', 'crear')")
    public String nuevo(Model model) {
        model.addAttribute("moduloActivo", "albaranes");
        model.addAttribute("titulo", "Nuevo albarán");
        model.addAttribute("clientes", clienteService.findAll());
        java.util.List<Almacen> almacenes = almacenService.findAll();
        model.addAttribute("almacenes", almacenes);
        if (!almacenes.isEmpty()) {
            model.addAttribute("almacenPorDefectoId", almacenes.get(0).getId());
        }
        model.addAttribute("articulos", articuloService.findAll());
        model.addAttribute("tiposIva", tipoImpositivoService.findActivos());
        model.addAttribute("lineasJson", "[]");
        model.addAttribute("breadcrumb", BreadcrumbBuilder.of(
            BreadcrumbBuilder.inicio(),
            BreadcrumbBuilder.link("Albaranes", "/web/albaranes"),
            BreadcrumbBuilder.active("Nuevo albarán")));
        return WebController.layout(model, "albaranes/formulario");
    }

    @GetMapping("/{id}")
    public String ver(@PathVariable Long id, Model model, RedirectAttributes ra) {
        return albaranService.obtenerPorIdParaPdf(id).map(a -> {
            model.addAttribute("moduloActivo", "albaranes");
            model.addAttribute("titulo", "Albarán " + a.getNumero());
            model.addAttribute("albaran", a);
            model.addAttribute("breadcrumb", BreadcrumbBuilder.of(
                BreadcrumbBuilder.inicio(),
                BreadcrumbBuilder.link("Albaranes", "/web/albaranes"),
                BreadcrumbBuilder.active(a.getNumero())));
            return WebController.layout(model, "albaranes/ver");
        }).orElseGet(() -> { Flash.error(ra, "Registro no encontrado"); return "redirect:/web/albaranes"; });
    }

    @GetMapping("/{id}/editar")
    @PreAuthorize("@permisos.puede('ventas', 'editar')")
    public String editar(@PathVariable Long id, Model model, RedirectAttributes ra) {
        return albaranService.obtenerPorIdParaPdf(id).map(a -> {
            model.addAttribute("moduloActivo", "albaranes");
            model.addAttribute("titulo", "Editar albarán " + a.getNumero());
            model.addAttribute("albaran", a);
            model.addAttribute("clientes", clienteService.findAll());
            model.addAttribute("almacenes", almacenService.findAll());
            model.addAttribute("articulos", articuloService.findAll());
            model.addAttribute("tiposIva", tipoImpositivoService.findActivos());
            model.addAttribute("lineasJson", lineasToJson(a));
            model.addAttribute("breadcrumb", BreadcrumbBuilder.of(
                BreadcrumbBuilder.inicio(),
                BreadcrumbBuilder.link("Albaranes", "/web/albaranes"),
                BreadcrumbBuilder.active("Editar " + a.getNumero())));
            return WebController.layout(model, "albaranes/formulario");
        }).orElseGet(() -> { Flash.error(ra, "Registro no encontrado"); return "redirect:/web/albaranes"; });
    }

    @PostMapping
    @PreAuthorize("@permisos.puedeCrearOEditar('ventas')")
    public String guardar(@RequestParam(required = false) Long id,
                           @RequestParam Long clienteId, @RequestParam(required = false) Long almacenId,
                           @RequestParam String fecha, @RequestParam(required = false) String observaciones,
                           @RequestParam(required = false) String numeroLote,
                           @RequestParam(required = false) String descuentoGlobalTipo,
                           @RequestParam(required = false) String descuentoGlobalValor,
                           HttpServletRequest request,
                           RedirectAttributes ra) {
        try {
            Map<String, Object> datos = new HashMap<>();
            datos.put("clienteId", clienteId);
            datos.put("almacenId", almacenId);
            datos.put("fecha", fecha);
            datos.put("observaciones", observaciones);
            datos.put("numeroLote", numeroLote);
            if (descuentoGlobalTipo != null) datos.put("descuentoGlobalTipo", descuentoGlobalTipo);
            if (descuentoGlobalValor != null) datos.put("descuentoGlobalValor", descuentoGlobalValor);
            datos.put("lineas", DocumentoParserUtil.construirLineas(
                request.getParameterValues("lineaArticuloId"),
                request.getParameterValues("lineaCantidad"),
                request.getParameterValues("lineaPrecio"),
                request.getParameterValues("lineaIva"),
                request.getParameterValues("lineaDescuento"),
                request.getParameterValues("lineaDescuentoTipo")
            ));
            AlbaranVenta albaran = documentoService.guardarAlbaran(id, datos);
            Flash.exito(ra, "Albarán guardado correctamente");
            return "redirect:/web/albaranes/" + albaran.getId();
        } catch (RuntimeException e) {
            log.error("Error al guardar albarán: {}", e.getMessage(), e);
            Flash.error(ra, e.getMessage());
            if (id != null) return "redirect:/web/albaranes/" + id + "/editar";
        }
        return "redirect:/web/albaranes/nuevo";
    }

    private String lineasToJson(AlbaranVenta alb) {
        if (alb.getAlbaranVentaLineas() == null || alb.getAlbaranVentaLineas().isEmpty()) return "[]";
        return alb.getAlbaranVentaLineas().stream().map(l ->
            String.format("{\"articuloId\":%s,\"cantidad\":\"%s\",\"precio\":\"%s\",\"iva\":\"%s\",\"descuento\":\"%s\"}",
                l.getArticulo() != null ? l.getArticulo().getId() : "null",
                DocumentoParserUtil.fmtDecimal(l.getCantidad()),
                DocumentoParserUtil.fmtDecimal(l.getPrecio()),
                DocumentoParserUtil.fmtDecimal(l.getIva()),
                DocumentoParserUtil.fmtDecimal(l.getDescuento()))
        ).collect(Collectors.joining(",", "[", "]"));
    }

    @PostMapping("/{id}/entregar")
    @PreAuthorize("@permisos.puede('ventas', 'editar')")
    public String marcarEntregado(@PathVariable Long id, RedirectAttributes ra) {
        try {
            albaranService.marcarEntregado(id);
            Flash.exito(ra, "Albarán marcado como entregado");
        } catch (RuntimeException e) {
            log.error("Error al marcar albarán {} como entregado: {}", id, e.getMessage(), e);
            Flash.error(ra, e.getMessage());
        }
        return "redirect:/web/albaranes/" + id;
    }

    @PostMapping("/{id}/facturar")
    @PreAuthorize("@permisos.puede('ventas', 'crear')")
    public String facturar(HttpSession session, @PathVariable Long id,
                           @RequestParam(required = false) String fecha,
                           @RequestParam(required = false) String medioCobro,
                           @RequestParam(required = false) String fechaVencimiento,
                           @RequestParam(required = false) String observaciones,
                           RedirectAttributes ra) {
        try {
            Factura factura = albaranService.convertirAFactura(
                id, usuarioActual(session), fecha, medioCobro, fechaVencimiento, observaciones);
            Flash.exito(ra, "Factura " + factura.getNumero() + " creada correctamente");
            return "redirect:/web/facturas/" + factura.getId();
        } catch (RuntimeException e) {
            log.error("Error al convertir albarán {} a factura: {}", id, e.getMessage(), e);
            Flash.error(ra, e.getMessage());
            return "redirect:/web/albaranes/" + id;
        }
    }

    @GetMapping("/{id}/pdf")
    public ResponseEntity<byte[]> descargarPdf(HttpSession session, @PathVariable Long id) {
        try {
            AlbaranVenta albaran = albaranService.obtenerPorIdParaPdf(id)
                .orElseThrow(() -> new IllegalArgumentException("Albaran no encontrado"));
            File pdf = impresionService.generarAlbaranPdf(albaran);
            auditoriaService.registrarImpresion(usuarioActual(session), "ALBARAN", String.valueOf(id),
                "PDF de albaran generado: " + albaran.getNumero());
            return Descargas.pdf(pdf);
        } catch (RuntimeException e) {
            log.error("Error al generar PDF de albarán {}: {}", id, e.getMessage(), e);
            throw e;
        }
    }

    /** Impresión por lotes: un único PDF con los albaranes seleccionados en el listado. */
    @PostMapping("/imprimir-lote")
    public ResponseEntity<byte[]> imprimirLote(HttpSession session,
                                               @RequestParam(name = "ids", required = false) List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            throw new ErpException("Selecciona al menos un albarán para imprimir");
        }
        List<AlbaranVenta> albaranes = new ArrayList<>();
        for (Long id : ids) {
            albaranService.obtenerPorIdParaPdf(id).ifPresent(albaranes::add);
        }
        if (albaranes.isEmpty()) {
            throw new ErpException("No se encontraron los albaranes seleccionados");
        }
        byte[] pdf = impresionService.generarAlbaranesLotePdf(albaranes);
        auditoriaService.registrarImpresion(usuarioActual(session), "ALBARAN", ids.toString(),
            "PDF de lote generado (" + albaranes.size() + " albaranes)");
        return Descargas.pdf(pdf, "albaranes.pdf");
    }

}
