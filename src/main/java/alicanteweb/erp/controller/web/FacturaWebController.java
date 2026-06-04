package alicanteweb.erp.controller.web;

import alicanteweb.erp.entities.*;
import alicanteweb.erp.service.*;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.File;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.time.LocalDate;
import java.util.List;

@Controller
@RequestMapping("/web/facturas")
public class FacturaWebController {

    private final FacturaService facturaService;
    private final ClienteService clienteService;
    private final ArticuloService articuloService;
    private final ImpresionService impresionService;
    private final AuditoriaService auditoriaService;
    private final UsuarioService usuarioService;

    public FacturaWebController(FacturaService facturaService, ClienteService clienteService,
                                 ArticuloService articuloService, ImpresionService impresionService,
                                 AuditoriaService auditoriaService, UsuarioService usuarioService) {
        this.facturaService = facturaService;
        this.clienteService = clienteService;
        this.articuloService = articuloService;
        this.impresionService = impresionService;
        this.auditoriaService = auditoriaService;
        this.usuarioService = usuarioService;
    }

    @GetMapping
    public String listado(HttpSession session, Model model,
                          @RequestParam(required = false) String q,
                          @RequestParam(required = false) String estado) {
        if (WebController.requireLogin(session)) return "redirect:/web/login";
        List<Factura> facturas = facturaService.findAll();
        if (q != null && !q.isBlank()) {
            String t = q.toLowerCase();
            facturas = facturas.stream()
                .filter(f -> (f.getNumero() != null && f.getNumero().toLowerCase().contains(t))
                    || (f.getCliente() != null && f.getCliente().getNombre() != null
                        && f.getCliente().getNombre().toLowerCase().contains(t)))
                .toList();
        }
        if (estado != null && !estado.isBlank()) {
            facturas = facturas.stream().filter(f -> estado.equals(f.getEstado())).toList();
        }
        facturas = facturas.stream()
                .sorted((a, b) -> b.getFecha() != null && a.getFecha() != null
                    ? b.getFecha().compareTo(a.getFecha()) : 0).toList();

        model.addAttribute("moduloActivo", "facturas");
        model.addAttribute("titulo", "Facturas");
        model.addAttribute("facturas", facturas);
        model.addAttribute("q", q);
        model.addAttribute("estado", estado);
        return WebController.layout(model, "facturas/lista");
    }

    @GetMapping("/nueva")
    public String formularioNueva(HttpSession session, Model model) {
        if (WebController.requireLogin(session)) return "redirect:/web/login";
        model.addAttribute("moduloActivo", "facturas");
        model.addAttribute("titulo", "Nueva factura");
        model.addAttribute("factura", new Factura());
        model.addAttribute("clientes", clienteService.findAll());
        model.addAttribute("articulos", articuloService.findAll());
        return WebController.layout(model, "facturas/formulario");
    }

    @GetMapping("/{id}")
    public String ver(HttpSession session, @PathVariable Long id, Model model) {
        if (WebController.requireLogin(session)) return "redirect:/web/login";
        return facturaService.findById(id).map(f -> {
            model.addAttribute("moduloActivo", "facturas");
            model.addAttribute("titulo", "Factura " + f.getNumero());
            model.addAttribute("factura", f);
            return WebController.layout(model, "facturas/ver");
        }).orElse("redirect:/web/facturas");
    }

    @PostMapping
    public String guardar(HttpSession session, @RequestParam(required = false) Long id,
                           @RequestParam Long clienteId, @RequestParam(required = false) String numero,
                           @RequestParam String fecha,
                           @RequestParam(required = false) String observaciones,
                           RedirectAttributes ra) {
        if (WebController.requireLogin(session)) return "redirect:/web/login";
        try {
            Factura f = id != null ? facturaService.findById(id).orElse(new Factura()) : new Factura();
            f.setNumero(numero);
            f.setFecha(LocalDate.parse(fecha));
            f.setObservaciones(observaciones);
            clienteService.findById(clienteId).ifPresent(f::setCliente);
            if (f.getEstado() == null) f.setEstado("BORRADOR");
            facturaService.save(f);
            ra.addFlashAttribute("exito", "Factura guardada");
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/web/facturas";
    }

    @PostMapping("/{id}/emitir")
    public String emitir(HttpSession session, @PathVariable Long id, RedirectAttributes ra) {
        if (WebController.requireLogin(session)) return "redirect:/web/login";
        try {
            facturaService.aprobarYEmitir(id);
            ra.addFlashAttribute("exito", "Factura emitida");
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/web/facturas";
    }

    @PostMapping("/{id}/anular")
    public String anular(HttpSession session, @PathVariable Long id, RedirectAttributes ra) {
        if (WebController.requireLogin(session)) return "redirect:/web/login";
        try {
            facturaService.anularFactura(id, "Anulada desde web");
            ra.addFlashAttribute("exito", "Factura anulada");
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/web/facturas";
    }

    @GetMapping("/{id}/pdf")
    public ResponseEntity<byte[]> descargarPdf(HttpSession session, @PathVariable Long id) throws Exception {
        if (WebController.requireLogin(session)) {
            return ResponseEntity.status(302).header(HttpHeaders.LOCATION, "/web/login").build();
        }
        Factura factura = facturaService.findByIdParaPdf(id)
            .orElseThrow(() -> new IllegalArgumentException("Factura no encontrada"));
        File pdf = impresionService.generarFacturaPdf(factura);
        auditoriaService.registrarImpresion(usuarioActual(session), "FACTURA", String.valueOf(id),
            "PDF de factura generado: " + factura.getNumero());
        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment()
                .filename(pdf.getName())
                .build()
                .toString())
            .header(HttpHeaders.CACHE_CONTROL, "no-store")
            .contentType(MediaType.APPLICATION_PDF)
            .body(Files.readAllBytes(pdf.toPath()));
    }

    private Usuario usuarioActual(HttpSession session) {
        Object usuarioId = session != null ? session.getAttribute("usuarioId") : null;
        if (usuarioId instanceof Long id) {
            return usuarioService.buscarPorId(id).orElse(null);
        }
        if (usuarioId instanceof Number n) {
            return usuarioService.buscarPorId(n.longValue()).orElse(null);
        }
        return null;
    }
}
