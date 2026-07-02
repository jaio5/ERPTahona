package alicanteweb.erp.controller.web;

import alicanteweb.erp.entities.Recepcion;
import alicanteweb.erp.entities.RecepcionLinea;
import alicanteweb.erp.service.AlmacenService;
import alicanteweb.erp.service.ArticuloService;
import alicanteweb.erp.service.PedidoCompraService;
import alicanteweb.erp.service.ProveedorService;
import alicanteweb.erp.service.RecepcionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;

@Controller
@PreAuthorize("@permisos.puede('compras', 'ver')")
@RequestMapping("/web/recepciones")
public class RecepcionWebController {

    private static final Logger log = LoggerFactory.getLogger(RecepcionWebController.class);

    private final RecepcionService recepcionService;
    private final ProveedorService proveedorService;
    private final PedidoCompraService pedidoCompraService;
    private final AlmacenService almacenService;
    private final ArticuloService articuloService;

    public RecepcionWebController(RecepcionService recepcionService,
                                  ProveedorService proveedorService,
                                  PedidoCompraService pedidoCompraService,
                                  AlmacenService almacenService,
                                  ArticuloService articuloService) {
        this.recepcionService = recepcionService;
        this.proveedorService = proveedorService;
        this.pedidoCompraService = pedidoCompraService;
        this.almacenService = almacenService;
        this.articuloService = articuloService;
    }

    @GetMapping
    public String lista(Model model, @RequestParam(required = false) String estado) {
        List<Recepcion> recepciones = estado != null && !estado.isBlank()
                ? recepcionService.findByEstado(estado)
                : recepcionService.findAll();
        model.addAttribute("moduloActivo", "recepciones");
        model.addAttribute("titulo", "Recepciones de compra");
        model.addAttribute("recepciones", recepciones);
        model.addAttribute("estado", estado);
        return WebController.layout(model, "recepciones/lista");
    }

    @GetMapping("/nuevo")
    public String nuevo(Model model) {
        model.addAttribute("moduloActivo", "recepciones");
        model.addAttribute("titulo", "Nueva recepción");
        model.addAttribute("proveedores", proveedorService.findAll());
        model.addAttribute("almacenes", almacenService.findAll());
        model.addAttribute("pedidosCompra", pedidoCompraService.findAll());
        return WebController.layout(model, "recepciones/formulario");
    }

    @PostMapping
    public String crear(@RequestParam Long proveedorId,
                        @RequestParam(required = false) Long almacenId,
                        @RequestParam(required = false) Long pedidoCompraId,
                        @RequestParam(required = false) String numero,
                        @RequestParam(required = false) String observaciones,
                        RedirectAttributes ra) {
        try {
            Recepcion r = new Recepcion();
            proveedorService.findById(proveedorId).ifPresent(r::setProveedor);
            if (almacenId != null) almacenService.findById(almacenId).ifPresent(r::setAlmacen);
            if (pedidoCompraId != null) pedidoCompraService.findById(pedidoCompraId).ifPresent(r::setPedidoCompra);
            if (numero != null && !numero.isBlank()) r.setNumero(numero.trim());
            r.setFecha(LocalDate.now());
            r.setEstado("PENDIENTE");
            r.setObservaciones(observaciones);
            Recepcion saved = recepcionService.save(r);
            ra.addFlashAttribute("exito", "Recepción creada. Añade las líneas de productos recibidos.");
            return "redirect:/web/recepciones/" + saved.getId();
        } catch (RuntimeException e) {
            log.error("Error al crear recepción: {}", e.getMessage(), e);
            ra.addFlashAttribute("error", e.getMessage());
            return "redirect:/web/recepciones/nuevo";
        }
    }

    @GetMapping("/{id}")
    public String ver(@PathVariable Long id, Model model) {
        Recepcion r = recepcionService.findDetailById(id)
                .orElseThrow(() -> new IllegalArgumentException("Recepción no encontrada: " + id));
        model.addAttribute("moduloActivo", "recepciones");
        model.addAttribute("titulo", "Recepción " + (r.getNumero() != null ? r.getNumero() : "#" + r.getId()));
        model.addAttribute("recepcion", r);
        model.addAttribute("lineas", recepcionService.findByRecepcionId(id));
        model.addAttribute("articulos", articuloService.findAll());
        return WebController.layout(model, "recepciones/ver");
    }

    @GetMapping("/{id}/editar")
    public String editar(@PathVariable Long id, Model model) {
        Recepcion r = recepcionService.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Recepción no encontrada: " + id));
        if ("CONFIRMADA".equals(r.getEstado())) {
            throw new IllegalStateException("No se puede editar una recepción ya confirmada");
        }
        model.addAttribute("moduloActivo", "recepciones");
        model.addAttribute("titulo", "Editar recepción");
        model.addAttribute("recepcion", r);
        model.addAttribute("proveedores", proveedorService.findAll());
        model.addAttribute("almacenes", almacenService.findAll());
        model.addAttribute("pedidosCompra", pedidoCompraService.findAll());
        return WebController.layout(model, "recepciones/formulario");
    }

    @PostMapping("/{id}")
    public String actualizar(@PathVariable Long id,
                             @RequestParam Long proveedorId,
                             @RequestParam(required = false) Long almacenId,
                             @RequestParam(required = false) Long pedidoCompraId,
                             @RequestParam(required = false) String numero,
                             @RequestParam(required = false) String observaciones,
                             RedirectAttributes ra) {
        try {
            Recepcion r = recepcionService.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Recepción no encontrada: " + id));
            if ("CONFIRMADA".equals(r.getEstado())) {
                throw new IllegalStateException("No se puede editar una recepción ya confirmada");
            }
            proveedorService.findById(proveedorId).ifPresent(r::setProveedor);
            r.setAlmacen(almacenId != null ? almacenService.findById(almacenId).orElse(null) : null);
            r.setPedidoCompra(pedidoCompraId != null ? pedidoCompraService.findById(pedidoCompraId).orElse(null) : null);
            if (numero != null && !numero.isBlank()) r.setNumero(numero.trim());
            r.setObservaciones(observaciones);
            recepcionService.save(r);
            ra.addFlashAttribute("exito", "Recepción actualizada");
        } catch (RuntimeException e) {
            log.error("Error al actualizar recepción {}: {}", id, e.getMessage(), e);
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/web/recepciones/" + id;
    }

    @PostMapping("/{id}/lineas")
    public String addLinea(@PathVariable Long id,
                           @RequestParam Long articuloId,
                           @RequestParam BigDecimal cantidadRecibida,
                           @RequestParam(required = false) BigDecimal cantidadPedida,
                           @RequestParam(required = false) BigDecimal precioUnitario,
                           @RequestParam(required = false) String codigoLote,
                           @RequestParam(required = false) String fechaCaducidad,
                           @RequestParam(required = false) String observaciones,
                           RedirectAttributes ra) {
        try {
            Recepcion r = recepcionService.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Recepción no encontrada"));
            if ("CONFIRMADA".equals(r.getEstado())) {
                throw new IllegalStateException("La recepción ya está confirmada");
            }
            var articulo = articuloService.findById(articuloId)
                    .orElseThrow(() -> new IllegalArgumentException("Artículo no encontrado"));
            RecepcionLinea linea = new RecepcionLinea();
            linea.setRecepcion(r);
            linea.setArticulo(articulo);
            linea.setCantidadRecibida(cantidadRecibida);
            linea.setCantidadPedida(cantidadPedida);
            linea.setPrecioUnitario(precioUnitario);
            linea.setCodigoLote(codigoLote);
            if (fechaCaducidad != null && !fechaCaducidad.isBlank()) {
                linea.setFechaCaducidad(LocalDate.parse(fechaCaducidad));
            }
            linea.setObservaciones(observaciones);
            recepcionService.saveLinea(linea);
        } catch (RuntimeException e) {
            log.error("Error al añadir línea a recepción {}: {}", id, e.getMessage(), e);
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/web/recepciones/" + id;
    }

    @PostMapping("/{id}/lineas/{lineaId}/eliminar")
    public String eliminarLinea(@PathVariable Long id, @PathVariable Long lineaId, RedirectAttributes ra) {
        try {
            recepcionService.deleteLinea(lineaId);
        } catch (RuntimeException e) {
            log.error("Error al eliminar línea {} de recepción {}: {}", lineaId, id, e.getMessage(), e);
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/web/recepciones/" + id;
    }

    @PostMapping("/{id}/confirmar")
    public String confirmar(@PathVariable Long id, RedirectAttributes ra) {
        try {
            recepcionService.confirmar(id);
            ra.addFlashAttribute("exito", "Recepción confirmada. Stock actualizado.");
        } catch (RuntimeException e) {
            log.error("Error al confirmar recepción {}: {}", id, e.getMessage(), e);
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/web/recepciones/" + id;
    }
}
