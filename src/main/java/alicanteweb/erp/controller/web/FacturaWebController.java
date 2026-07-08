package alicanteweb.erp.controller.web;

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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@PreAuthorize("@permisos.puede('ventas', 'ver')")
@RequestMapping("/web/facturas")
public class FacturaWebController extends BaseWebController {

    private static final Logger log = LoggerFactory.getLogger(FacturaWebController.class);

    private final FacturaService facturaService;
    private final ClienteService clienteService;
    private final ArticuloService articuloService;
    private final ImpresionService impresionService;
    private final AuditoriaService auditoriaService;
    private final DocumentoService documentoService;
    private final EmailService emailService;
    private final FacturaeService facturaeService;

    private final TipoImpositivoService tipoImpositivoService;

    public FacturaWebController(FacturaService facturaService, ClienteService clienteService,
                                 ArticuloService articuloService, ImpresionService impresionService,
                                 AuditoriaService auditoriaService, UsuarioService usuarioService,
                                 DocumentoService documentoService, EmailService emailService,
                                 FacturaeService facturaeService, TipoImpositivoService tipoImpositivoService) {
        super(usuarioService);
        this.facturaService = facturaService;
        this.clienteService = clienteService;
        this.articuloService = articuloService;
        this.impresionService = impresionService;
        this.auditoriaService = auditoriaService;
        this.documentoService = documentoService;
        this.emailService = emailService;
        this.facturaeService = facturaeService;
        this.tipoImpositivoService = tipoImpositivoService;
    }

    @GetMapping("/{id}/facturae")
    public Object descargarFacturae(@PathVariable Long id, RedirectAttributes ra) {
        try {
            String xml = facturaeService.generarFacturaeXml(id);
            return ResponseEntity.ok()
                    .header(org.springframework.http.HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; filename=\"facturae-" + id + ".xml\"")
                    .contentType(org.springframework.http.MediaType.APPLICATION_XML)
                    .body(xml.getBytes(java.nio.charset.StandardCharsets.UTF_8));
        } catch (RuntimeException e) {
            log.error("Error generando Facturae de {}: {}", id, e.getMessage(), e);
            ra.addFlashAttribute("error", e.getMessage());
            return "redirect:/web/facturas/" + id;
        }
    }

    @PostMapping("/{id}/enviar-email")
    public String enviarEmail(@PathVariable Long id,
                              @RequestParam(required = false) String emailDestino,
                              HttpSession session,
                              RedirectAttributes ra) {
        try {
            String destino = emailService.enviarFacturaPorEmail(id, emailDestino, usuarioActual(session));
            ra.addFlashAttribute("exito", "Factura enviada por email a " + destino);
        } catch (RuntimeException e) {
            log.error("Error al enviar factura {} por email: {}", id, e.getMessage(), e);
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/web/facturas/" + id;
    }

    @GetMapping
    public String listado(Model model,
                          @RequestParam(required = false) String q,
                          @RequestParam(required = false) String estado,
                          @RequestParam(defaultValue = "0") int page,
                          @RequestParam(defaultValue = "25") int size,
                          @RequestParam(defaultValue = "fecha") String sort,
                          @RequestParam(defaultValue = "desc") String dir) {
        Sort.Direction direction = "asc".equalsIgnoreCase(dir) ? Sort.Direction.ASC : Sort.Direction.DESC;
        PageRequest pageable = PageRequest.of(page, size, Sort.by(direction, sort));
        Page<Factura> pageResult = facturaService.buscarPaginado(q, estado, pageable);

        model.addAttribute("moduloActivo", "facturas");
        model.addAttribute("titulo", "Facturas");
        model.addAttribute("facturas", pageResult.getContent());
        model.addAttribute("page", pageResult);
        model.addAttribute("q", q);
        model.addAttribute("estado", estado);
        model.addAttribute("sort", sort);
        model.addAttribute("dir", dir);
        model.addAttribute("breadcrumb", BreadcrumbBuilder.of(
            BreadcrumbBuilder.inicio(),
            BreadcrumbBuilder.active("Facturas")));
        return WebController.layout(model, "facturas/lista");
    }

    @GetMapping("/nueva")
    @PreAuthorize("@permisos.puede('ventas', 'crear')")
    public String formularioNueva(Model model) {
        model.addAttribute("moduloActivo", "facturas");
        model.addAttribute("titulo", "Nueva factura");
        model.addAttribute("factura", new Factura());
        model.addAttribute("clientes", clienteService.findAll());
        model.addAttribute("articulos", articuloService.findAll());
        model.addAttribute("tiposIva", tipoImpositivoService.findActivos());
        model.addAttribute("lineasJson", "[]");
        model.addAttribute("breadcrumb", BreadcrumbBuilder.of(
            BreadcrumbBuilder.inicio(),
            BreadcrumbBuilder.link("Facturas", "/web/facturas"),
            BreadcrumbBuilder.active("Nueva factura")));
        return WebController.layout(model, "facturas/formulario");
    }

    @GetMapping("/{id}/editar")
    @PreAuthorize("@permisos.puede('ventas', 'editar')")
    public String formularioEditar(@PathVariable Long id, Model model, RedirectAttributes ra) {
        return facturaService.findByIdParaPdf(id).map(f -> {
            model.addAttribute("moduloActivo", "facturas");
            model.addAttribute("titulo", "Editar factura " + f.getNumero());
            model.addAttribute("breadcrumb", BreadcrumbBuilder.of(
                BreadcrumbBuilder.inicio(),
                BreadcrumbBuilder.link("Facturas", "/web/facturas"),
                BreadcrumbBuilder.active("Editar " + f.getNumero())));
            model.addAttribute("factura", f);
            model.addAttribute("clientes", clienteService.findAll());
            model.addAttribute("articulos", articuloService.findAll());
            model.addAttribute("tiposIva", tipoImpositivoService.findActivos());
            String lineasJson = f.getFacturaLineas().stream().map(l ->
                String.format("{\"articuloId\":%s,\"cantidad\":\"%s\",\"precio\":\"%s\",\"iva\":\"%s\",\"descuento\":\"%s\"}",
                    l.getArticulo() != null ? l.getArticulo().getId() : "null",
                    DocumentoParserUtil.fmtDecimal(l.getCantidad()),
                    DocumentoParserUtil.fmtDecimal(l.getPrecioUnitario()),
                    DocumentoParserUtil.fmtDecimal(l.getIva()),
                    DocumentoParserUtil.fmtDecimal(l.getDescuento()))
            ).collect(Collectors.joining(",", "[", "]"));
            model.addAttribute("lineasJson", lineasJson);
            return WebController.layout(model, "facturas/formulario");
        }).orElseGet(() -> { ra.addFlashAttribute("error", "Registro no encontrado"); return "redirect:/web/facturas"; });
    }

    @GetMapping("/{id}")
    public String ver(@PathVariable Long id, Model model, RedirectAttributes ra) {
        return facturaService.findByIdParaPdf(id).map(f -> {
            model.addAttribute("moduloActivo", "facturas");
            model.addAttribute("titulo", "Factura " + f.getNumero());
            model.addAttribute("factura", f);
            model.addAttribute("breadcrumb", BreadcrumbBuilder.of(
                BreadcrumbBuilder.inicio(),
                BreadcrumbBuilder.link("Facturas", "/web/facturas"),
                BreadcrumbBuilder.active(f.getNumero())));
            return WebController.layout(model, "facturas/ver");
        }).orElseGet(() -> { ra.addFlashAttribute("error", "Registro no encontrado"); return "redirect:/web/facturas"; });
    }

    @PostMapping
    @PreAuthorize("@permisos.puedeCrearOEditar('ventas')")
    public String guardar(@RequestParam(required = false) Long id,
                           @RequestParam Long clienteId,
                           @RequestParam String fecha,
                           @RequestParam(required = false) String medioCobro,
                           @RequestParam(required = false) String fechaVencimiento,
                           @RequestParam(required = false) String observaciones,
                           @RequestParam(required = false) String rappelPorcentaje,
                           @RequestParam(required = false) String rappelImporte,
                           @RequestParam(required = false) String descuentoGlobalTipo,
                           @RequestParam(required = false) String descuentoGlobalValor,
                           HttpServletRequest request,
                           RedirectAttributes ra) {
        try {
            Map<String, Object> datos = new HashMap<>();
            datos.put("clienteId", clienteId);
            datos.put("fecha", fecha);
            datos.put("medioCobro", medioCobro);
            datos.put("fechaVencimiento", fechaVencimiento);
            datos.put("observaciones", observaciones);
            if (rappelPorcentaje != null) datos.put("rappelPorcentaje", rappelPorcentaje);
            if (rappelImporte != null) datos.put("rappelImporte", rappelImporte);
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
            Factura factura = documentoService.guardarFactura(id, datos);
            ra.addFlashAttribute("exito", "Factura guardada correctamente");
            return "redirect:/web/facturas/" + factura.getId();
        } catch (RuntimeException e) {
            log.error("Error al guardar factura: {}", e.getMessage(), e);
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/web/facturas";
    }

    @GetMapping("/{id}/rectificar")
    @PreAuthorize("@permisos.puede('ventas', 'editar')")
    public String formularioRectificativa(@PathVariable Long id, Model model, RedirectAttributes ra) {
        return facturaService.findByIdParaPdf(id).map(f -> {
            if (!"EMITIDA".equalsIgnoreCase(f.getEstado())) {
                return "redirect:/web/facturas/" + id;
            }
            model.addAttribute("moduloActivo", "facturas");
            model.addAttribute("titulo", "Rectificativa de " + f.getNumero());
            model.addAttribute("facturaOriginal", f);
            model.addAttribute("fechaHoy", LocalDate.now().toString());
            model.addAttribute("breadcrumb", BreadcrumbBuilder.of(
                BreadcrumbBuilder.inicio(),
                BreadcrumbBuilder.link("Facturas", "/web/facturas"),
                BreadcrumbBuilder.link(f.getNumero(), "/web/facturas/" + f.getId()),
                BreadcrumbBuilder.active("Rectificativa")));
            return WebController.layout(model, "facturas/rectificativa");
        }).orElseGet(() -> { ra.addFlashAttribute("error", "Registro no encontrado"); return "redirect:/web/facturas"; });
    }

    @PostMapping("/{id}/rectificar")
    @PreAuthorize("@permisos.puede('ventas', 'editar')")
    public String crearRectificativa(@PathVariable Long id,
                                      @RequestParam String motivo,
                                      @RequestParam(required = false, defaultValue = "SUSTITUCION") String tipoRectificacion,
                                      @RequestParam(required = false) String fecha,
                                      RedirectAttributes ra) {
        try {
            LocalDate fechaRectificativa = (fecha != null && !fecha.isBlank()) ? LocalDate.parse(fecha) : LocalDate.now();
            Factura rectificativa = facturaService.crearRectificativa(id, motivo, tipoRectificacion, fechaRectificativa);
            ra.addFlashAttribute("exito", "Factura rectificativa " + rectificativa.getNumero() + " creada correctamente");
            return "redirect:/web/facturas/" + rectificativa.getId();
        } catch (RuntimeException e) {
            log.error("Error al crear rectificativa de factura {}: {}", id, e.getMessage(), e);
            ra.addFlashAttribute("error", e.getMessage());
            return "redirect:/web/facturas/" + id;
        }
    }

    @PostMapping("/{id}/revision")
    @PreAuthorize("@permisos.puede('ventas', 'editar')")
    public String enviarARevision(@PathVariable Long id, RedirectAttributes ra) {
        try {
            facturaService.pasarARevision(id);
            ra.addFlashAttribute("exito", "Factura enviada a revisión");
        } catch (RuntimeException e) {
            log.error("Error al enviar factura {} a revisión: {}", id, e.getMessage(), e);
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/web/facturas/" + id;
    }

    @PostMapping("/{id}/emitir")
    @PreAuthorize("@permisos.puede('ventas', 'editar')")
    public String emitir(@PathVariable Long id, RedirectAttributes ra) {
        try {
            facturaService.aprobarYEmitir(id);
            ra.addFlashAttribute("exito", "Factura emitida");
        } catch (RuntimeException e) {
            log.error("Error al emitir factura {}: {}", id, e.getMessage(), e);
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/web/facturas/" + id;
    }

    @PostMapping("/{id}/anular")
    @PreAuthorize("@permisos.puede('ventas', 'eliminar')")
    public String anular(@PathVariable Long id, RedirectAttributes ra) {
        try {
            facturaService.anularFactura(id, "Anulada desde web");
            ra.addFlashAttribute("exito", "Factura anulada");
        } catch (RuntimeException e) {
            log.error("Error al anular factura {}: {}", id, e.getMessage(), e);
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/web/facturas/" + id;
    }

    @GetMapping("/{id}/pdf")
    public ResponseEntity<byte[]> descargarPdf(HttpSession session, @PathVariable Long id) {
        try {
            Factura factura = facturaService.findByIdParaPdf(id)
                .orElseThrow(() -> new IllegalArgumentException("Factura no encontrada"));
            File pdf = impresionService.generarFacturaPdf(factura);
            auditoriaService.registrarImpresion(usuarioActual(session), "FACTURA", String.valueOf(id),
                "PDF de factura generado: " + factura.getNumero());
            return Descargas.pdf(pdf);
        } catch (RuntimeException e) {
            log.error("Error al generar PDF de factura {}: {}", id, e.getMessage(), e);
            throw new ErpException("Error al generar PDF: " + e.getMessage(), e);
        }
    }

}
