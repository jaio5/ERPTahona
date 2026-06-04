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
@RequestMapping("/web/albaranes")
public class AlbaranWebController {

    private final AlbaranService albaranService;
    private final ClienteService clienteService;
    private final ArticuloService articuloService;
    private final AlmacenService almacenService;
    private final ImpresionService impresionService;
    private final AuditoriaService auditoriaService;
    private final UsuarioService usuarioService;

    public AlbaranWebController(AlbaranService albaranService, ClienteService clienteService,
                                 ArticuloService articuloService, AlmacenService almacenService,
                                 ImpresionService impresionService, AuditoriaService auditoriaService,
                                 UsuarioService usuarioService) {
        this.albaranService = albaranService;
        this.clienteService = clienteService;
        this.articuloService = articuloService;
        this.almacenService = almacenService;
        this.impresionService = impresionService;
        this.auditoriaService = auditoriaService;
        this.usuarioService = usuarioService;
    }

    @GetMapping
    public String listado(HttpSession session, Model model, @RequestParam(required = false) String q) {
        if (WebController.requireLogin(session)) return "redirect:/web/login";
        List<AlbaranVenta> albaranes = albaranService.obtenerTodos();
        if (q != null && !q.isBlank()) {
            String t = q.toLowerCase();
            albaranes = albaranes.stream()
                .filter(a -> (a.getNumero() != null && a.getNumero().toLowerCase().contains(t))
                    || (a.getCliente() != null && a.getCliente().getNombre() != null
                        && a.getCliente().getNombre().toLowerCase().contains(t))).toList();
        }
        model.addAttribute("moduloActivo", "albaranes");
        model.addAttribute("titulo", "Albaranes");
        model.addAttribute("albaranes", albaranes);
        return WebController.layout(model, "albaranes/lista");
    }

    @GetMapping("/nuevo")
    public String nuevo(HttpSession session, Model model) {
        if (WebController.requireLogin(session)) return "redirect:/web/login";
        model.addAttribute("moduloActivo", "albaranes");
        model.addAttribute("titulo", "Nuevo albarán");
        model.addAttribute("clientes", clienteService.findAll());
        model.addAttribute("almacenes", almacenService.findAll());
        return WebController.layout(model, "albaranes/formulario");
    }

    @GetMapping("/{id}")
    public String ver(HttpSession session, @PathVariable Long id, Model model) {
        if (WebController.requireLogin(session)) return "redirect:/web/login";
        return albaranService.obtenerPorId(id).map(a -> {
            model.addAttribute("moduloActivo", "albaranes");
            model.addAttribute("titulo", "Albarán " + a.getNumero());
            model.addAttribute("albaran", a);
            return WebController.layout(model, "albaranes/ver");
        }).orElse("redirect:/web/albaranes");
    }

    @PostMapping
    public String guardar(HttpSession session, @RequestParam(required = false) Long id,
                           @RequestParam Long clienteId, @RequestParam(required = false) Long almacenId,
                           @RequestParam String fecha, @RequestParam(required = false) String observaciones,
                           RedirectAttributes ra) {
        if (WebController.requireLogin(session)) return "redirect:/web/login";
        try {
            AlbaranVenta a = id != null ? albaranService.obtenerPorId(id).orElse(new AlbaranVenta()) : new AlbaranVenta();
            a.setFecha(LocalDate.parse(fecha));
            a.setObservaciones(observaciones);
            clienteService.findById(clienteId).ifPresent(a::setCliente);
            almacenService.findById(almacenId).ifPresent(a::setAlmacen);
            albaranService.guardar(a);
            ra.addFlashAttribute("exito", "Albarán guardado");
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/web/albaranes";
    }

    @GetMapping("/{id}/pdf")
    public ResponseEntity<byte[]> descargarPdf(HttpSession session, @PathVariable Long id) throws Exception {
        if (WebController.requireLogin(session)) {
            return ResponseEntity.status(302).header(HttpHeaders.LOCATION, "/web/login").build();
        }
        AlbaranVenta albaran = albaranService.obtenerPorIdParaPdf(id)
            .orElseThrow(() -> new IllegalArgumentException("Albaran no encontrado"));
        File pdf = impresionService.generarAlbaranPdf(albaran);
        auditoriaService.registrarImpresion(usuarioActual(session), "ALBARAN", String.valueOf(id),
            "PDF de albaran generado: " + albaran.getNumero());
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
