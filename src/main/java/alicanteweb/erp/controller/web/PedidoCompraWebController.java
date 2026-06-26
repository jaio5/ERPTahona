package alicanteweb.erp.controller.web;

import alicanteweb.erp.entities.PedidoCompra;
import alicanteweb.erp.service.PedidoCompraService;
import alicanteweb.erp.service.ProveedorService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import org.springframework.security.access.prepost.PreAuthorize;

@Controller
@PreAuthorize("@permisos.puede('compras', 'ver')")
@RequestMapping("/web/pedidos-compra")
public class PedidoCompraWebController {

    private static final Logger log = LoggerFactory.getLogger(PedidoCompraWebController.class);

    private final PedidoCompraService pedidoCompraService;
    private final ProveedorService proveedorService;

    public PedidoCompraWebController(PedidoCompraService pedidoCompraService, ProveedorService proveedorService) {
        this.pedidoCompraService = pedidoCompraService;
        this.proveedorService = proveedorService;
    }

    @GetMapping
    public String lista(Model model,
                        @RequestParam(defaultValue = "0") int page,
                        @RequestParam(defaultValue = "20") int size,
                        @RequestParam(required = false) String q,
                        @RequestParam(required = false) String estado) {
        Pageable pageable = PageRequest.of(page, size);
        Page<PedidoCompra> pageResult = pedidoCompraService.findPage(q, estado, pageable);
        model.addAttribute("moduloActivo", "pedidos-compra");
        model.addAttribute("titulo", "Pedidos de compra");
        model.addAttribute("page", pageResult);
        model.addAttribute("pedidos", pageResult.getContent());
        model.addAttribute("q", q);
        model.addAttribute("estado", estado);
        return WebController.layout(model, "pedidos-compra/lista");
    }

    @GetMapping("/nuevo")
    public String formularioNuevo(Model model) {
        model.addAttribute("moduloActivo", "pedidos-compra");
        model.addAttribute("titulo", "Nuevo pedido de compra");
        model.addAttribute("proveedores", proveedorService.findAll());
        return WebController.layout(model, "pedidos-compra/formulario");
    }

    @GetMapping("/{id}")
    public String ver(@PathVariable Long id, Model model, RedirectAttributes ra) {
        return pedidoCompraService.findById(id).map(p -> {
            model.addAttribute("moduloActivo", "pedidos-compra");
            model.addAttribute("titulo", "Pedido compra " + p.getNumero());
            model.addAttribute("pedido", p);
            return WebController.layout(model, "pedidos-compra/ver");
        }).orElseGet(() -> { ra.addFlashAttribute("error", "Registro no encontrado"); return "redirect:/web/pedidos-compra"; });
    }

    @PostMapping
    public String guardar(@RequestParam(required = false) Long id,
                          @RequestParam Long proveedorId,
                          @RequestParam(required = false) String observaciones,
                          RedirectAttributes ra) {
        try {
            PedidoCompra pedido = id != null
                ? pedidoCompraService.findById(id).orElseThrow(() -> new IllegalArgumentException("ID de pedido de compra no válido: " + id))
                : new PedidoCompra();
            proveedorService.findById(proveedorId).ifPresent(pedido::setProveedor);
            pedido.setFecha(LocalDate.now());
            if (pedido.getEstado() == null) pedido.setEstado("BORRADOR");
            pedido.setObservaciones(observaciones);
            pedidoCompraService.save(pedido);
            ra.addFlashAttribute("exito", "Pedido de compra guardado correctamente");
        } catch (RuntimeException e) {
            log.error("Error al guardar pedido compra: {}", e.getMessage(), e);
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/web/pedidos-compra";
    }

    @PostMapping("/{id}/estado")
    @PreAuthorize("@permisos.puede('compras', 'editar')")
    public String cambiarEstado(@PathVariable Long id, @RequestParam String estado, RedirectAttributes ra) {
        try {
            pedidoCompraService.cambiarEstado(id, estado);
            ra.addFlashAttribute("exito", "Estado actualizado a " + estado);
        } catch (RuntimeException e) {
            log.error("Error al cambiar estado pedido compra {}: {}", id, e.getMessage(), e);
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/web/pedidos-compra/" + id;
    }
}
