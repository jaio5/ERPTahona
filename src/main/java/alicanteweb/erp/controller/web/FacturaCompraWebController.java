package alicanteweb.erp.controller.web;

import alicanteweb.erp.entities.FacturaCompra;
import alicanteweb.erp.service.ArticuloService;
import alicanteweb.erp.service.FacturaCompraService;
import alicanteweb.erp.service.ProveedorService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.time.LocalDate;
import org.springframework.security.access.prepost.PreAuthorize;

@Controller
@PreAuthorize("@permisos.puede('compras', 'ver')")
@RequestMapping("/web/facturas-compra")
public class FacturaCompraWebController {

    private static final Logger log = LoggerFactory.getLogger(FacturaCompraWebController.class);

    private final FacturaCompraService facturaCompraService;
    private final ProveedorService proveedorService;
    private final ArticuloService articuloService;

    public FacturaCompraWebController(FacturaCompraService facturaCompraService,
                                       ProveedorService proveedorService,
                                       ArticuloService articuloService) {
        this.facturaCompraService = facturaCompraService;
        this.proveedorService = proveedorService;
        this.articuloService = articuloService;
    }

    @GetMapping
    public String lista(Model model,
                        @RequestParam(required = false) String q,
                        @RequestParam(required = false) String estado,
                        @RequestParam(defaultValue = "0") int page,
                        @RequestParam(defaultValue = "20") int size) {
        Page<FacturaCompra> pageResult = facturaCompraService.findPage(q, estado, PageRequest.of(page, size));
        model.addAttribute("moduloActivo", "facturas-compra");
        model.addAttribute("titulo", "Facturas de compra");
        model.addAttribute("facturas", pageResult.getContent());
        model.addAttribute("page", pageResult);
        model.addAttribute("q", q);
        model.addAttribute("estado", estado);
        model.addAttribute("breadcrumb", BreadcrumbBuilder.of(
            BreadcrumbBuilder.inicio(),
            BreadcrumbBuilder.link("Compras", "#"),
            BreadcrumbBuilder.active("Facturas de compra")));
        return WebController.layout(model, "facturas-compra/lista");
    }

    @GetMapping("/nuevo")
    public String formularioNuevo(Model model) {
        model.addAttribute("moduloActivo", "facturas-compra");
        model.addAttribute("titulo", "Nueva factura de compra");
        model.addAttribute("proveedores", proveedorService.findAll());
        model.addAttribute("articulos", articuloService.findAll());
        return WebController.layout(model, "facturas-compra/formulario");
    }

    @GetMapping("/{id}")
    public String ver(@PathVariable Long id, Model model, RedirectAttributes ra) {
        return facturaCompraService.obtenerDetallePorId(id).map(f -> {
            model.addAttribute("moduloActivo", "facturas-compra");
            model.addAttribute("titulo", "Factura compra " + f.getNumero());
            model.addAttribute("factura", f);
            return WebController.layout(model, "facturas-compra/ver");
        }).orElseGet(() -> { ra.addFlashAttribute("error", "Registro no encontrado"); return "redirect:/web/facturas-compra"; });
    }

    @PostMapping
    public String guardar(@RequestParam(required = false) Long id,
                          @RequestParam Long proveedorId,
                          @RequestParam String numero,
                          @RequestParam(required = false) String fecha,
                          @RequestParam(required = false) String fechaVencimiento,
                          @RequestParam(required = false) String formaPago,
                          @RequestParam(required = false) BigDecimal baseImponible,
                          @RequestParam(required = false) BigDecimal tipoIva,
                          @RequestParam(required = false) BigDecimal importeIva,
                          @RequestParam(required = false) String observaciones,
                          RedirectAttributes ra) {
        try {
            FacturaCompra factura = id != null
                ? facturaCompraService.obtenerPorId(id).orElseThrow(() -> new IllegalArgumentException("ID de factura de compra no válido: " + id))
                : new FacturaCompra();
            proveedorService.findById(proveedorId).ifPresent(factura::setProveedor);
            factura.setNumero(numero);
            factura.setFecha(fecha != null && !fecha.isBlank() ? LocalDate.parse(fecha) : LocalDate.now());
            if (fechaVencimiento != null && !fechaVencimiento.isBlank()) factura.setFechaVencimiento(LocalDate.parse(fechaVencimiento));
            factura.setFormaPago(formaPago);
            factura.setBaseImponible(baseImponible != null ? baseImponible : BigDecimal.ZERO);
            factura.setTipoIva(tipoIva);
            factura.setImporteIva(importeIva != null ? importeIva : BigDecimal.ZERO);
            factura.calcularTotal();
            if (factura.getEstado() == null) factura.setEstado("PENDIENTE");
            factura.setObservaciones(observaciones);
            facturaCompraService.guardar(factura);
            ra.addFlashAttribute("exito", "Factura de compra guardada correctamente");
        } catch (RuntimeException e) {
            log.error("Error al guardar factura compra: {}", e.getMessage(), e);
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/web/facturas-compra";
    }
}
