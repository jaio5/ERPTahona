package alicanteweb.erp.controller.web;

import alicanteweb.erp.util.Flash;
import alicanteweb.erp.entities.Pedido;
import alicanteweb.erp.entities.PedidoLinea;
import alicanteweb.erp.service.ArticuloService;
import alicanteweb.erp.service.ClienteService;
import alicanteweb.erp.service.DocumentoService;
import alicanteweb.erp.service.PedidoService;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.security.access.prepost.PreAuthorize;

@Controller
@PreAuthorize("@permisos.puede('ventas', 'ver')")
@RequestMapping("/web/pedidos-venta")
public class PedidoVentaWebController {

    private static final Logger log = LoggerFactory.getLogger(PedidoVentaWebController.class);

    private final PedidoService pedidoService;
    private final ClienteService clienteService;
    private final DocumentoService documentoService;
    private final ArticuloService articuloService;

    public PedidoVentaWebController(PedidoService pedidoService, ClienteService clienteService,
                                     DocumentoService documentoService, ArticuloService articuloService) {
        this.pedidoService = pedidoService;
        this.clienteService = clienteService;
        this.documentoService = documentoService;
        this.articuloService = articuloService;
    }

    @GetMapping
    public String lista(Model model, @RequestParam(required = false) String q,
                        @RequestParam(required = false) String estado,
                        @RequestParam(defaultValue = "0") int page,
                        @RequestParam(defaultValue = "15") int size) {
        Page<Pedido> pageResult;
        if (q != null && !q.isBlank()) {
            pageResult = pedidoService.buscar(q, PageRequest.of(page, size));
        } else if (estado != null && !estado.isBlank()) {
            pageResult = pedidoService.buscarPorEstado(estado, PageRequest.of(page, size));
        } else {
            pageResult = pedidoService.obtenerTodos(PageRequest.of(page, size));
        }
        model.addAttribute("moduloActivo", "pedidos-venta");
        model.addAttribute("titulo", "Pedidos de venta");
        model.addAttribute("pedidos", pageResult.getContent());
        model.addAttribute("page", pageResult);
        model.addAttribute("q", q);
        model.addAttribute("estado", estado);
        return WebController.layout(model, "pedidos-venta/lista");
    }

    @GetMapping("/nuevo")
    public String formularioNuevo(Model model) {
        model.addAttribute("moduloActivo", "pedidos-venta");
        model.addAttribute("titulo", "Nuevo pedido de venta");
        model.addAttribute("clientes", clienteService.findAll());
        model.addAttribute("articulos", articuloService.findAll());
        model.addAttribute("lineasJson", "[]");
        return WebController.layout(model, "pedidos-venta/formulario");
    }

    @GetMapping("/{id}/editar")
    public String editar(@PathVariable Long id, Model model, RedirectAttributes ra) {
        return pedidoService.obtenerPorId(id).map(p -> {
            String lineasJson = p.getPedidoLineas().stream().map(l ->
                String.format("{\"articuloId\":%s,\"cantidad\":\"%s\",\"precio\":\"%s\",\"iva\":\"%s\",\"descuento\":\"%s\"}",
                    l.getArticulo() != null ? l.getArticulo().getId() : "null",
                    DocumentoParserUtil.fmtDecimal(l.getCantidad()),
                    DocumentoParserUtil.fmtDecimal(l.getPrecio()),
                    DocumentoParserUtil.fmtDecimal(l.getIva()),
                    DocumentoParserUtil.fmtDecimal(l.getDescuento()))
            ).collect(Collectors.joining(",", "[", "]"));
            model.addAttribute("moduloActivo", "pedidos-venta");
            model.addAttribute("titulo", "Editar pedido " + p.getNumero());
            model.addAttribute("pedido", p);
            model.addAttribute("clientes", clienteService.findAll());
            model.addAttribute("articulos", articuloService.findAll());
            model.addAttribute("lineasJson", lineasJson);
            return WebController.layout(model, "pedidos-venta/formulario");
        }).orElseGet(() -> { Flash.error(ra, "Registro no encontrado"); return "redirect:/web/pedidos-venta"; });
    }

    @GetMapping("/{id}")
    public String ver(@PathVariable Long id, Model model, RedirectAttributes ra) {
        return pedidoService.obtenerPorId(id).map(p -> {
            model.addAttribute("moduloActivo", "pedidos-venta");
            model.addAttribute("titulo", "Pedido " + p.getNumero());
            model.addAttribute("pedido", p);
            model.addAttribute("totalCalculado", calcularTotal(p));
            // Importe neto (con descuento) e IVA por línea: misma fórmula que el total del pedido
            Map<Long, BigDecimal> importesLinea = new HashMap<>();
            BigDecimal baseLineas = BigDecimal.ZERO;
            BigDecimal ivaLineas = BigDecimal.ZERO;
            boolean hayDescuento = false;
            for (PedidoLinea l : p.getPedidoLineas()) {
                BigDecimal importe = PedidoService.importeLinea(l);
                importesLinea.put(l.getId(), importe);
                baseLineas = baseLineas.add(importe);
                ivaLineas = ivaLineas.add(PedidoService.ivaLinea(l));
                if (l.getDescuento() != null && l.getDescuento().compareTo(BigDecimal.ZERO) > 0) hayDescuento = true;
            }
            model.addAttribute("importesLinea", importesLinea);
            model.addAttribute("baseLineas", baseLineas);
            model.addAttribute("ivaLineas", ivaLineas);
            model.addAttribute("hayDescuento", hayDescuento);
            return WebController.layout(model, "pedidos-venta/ver");
        }).orElseGet(() -> { Flash.error(ra, "Registro no encontrado"); return "redirect:/web/pedidos-venta"; });
    }

    // El total almacenado puede ser 0 en pedidos antiguos; en ese caso se recalcula desde las líneas
    private BigDecimal calcularTotal(Pedido p) {
        if (p.getTotal() != null && p.getTotal().compareTo(BigDecimal.ZERO) != 0) {
            return p.getTotal();
        }
        return PedidoService.calcularTotal(p.getPedidoLineas());
    }

    @PostMapping
    public String guardar(@RequestParam(required = false) Long id,
                          @RequestParam Long clienteId,
                          @RequestParam(required = false) String fecha,
                          @RequestParam(required = false) String observaciones,
                          HttpServletRequest request,
                          RedirectAttributes ra) {
        try {
            Map<String, Object> datos = new HashMap<>();
            datos.put("clienteId", clienteId);
            datos.put("fecha", fecha);
            datos.put("observaciones", observaciones);
            datos.put("lineas", DocumentoParserUtil.construirLineas(
                request.getParameterValues("lineaArticuloId"),
                request.getParameterValues("lineaCantidad"),
                request.getParameterValues("lineaPrecio"),
                request.getParameterValues("lineaIva"),
                request.getParameterValues("lineaDescuento")
            ));
            documentoService.guardarPedido(id, datos);
            Flash.exito(ra, "Pedido guardado correctamente");
        } catch (RuntimeException e) {
            log.error("Error al guardar pedido: {}", e.getMessage(), e);
            Flash.error(ra, e.getMessage());
        }
        return "redirect:/web/pedidos-venta";
    }

    @PostMapping("/{id}/confirmar")
    public String confirmar(@PathVariable Long id, RedirectAttributes ra) {
        try {
            pedidoService.cambiarEstado(id, "CONFIRMADO");
            Flash.exito(ra, "Pedido confirmado");
        } catch (RuntimeException e) {
            log.error("Error al confirmar pedido {}: {}", id, e.getMessage(), e);
            Flash.error(ra, e.getMessage());
        }
        return "redirect:/web/pedidos-venta/" + id;
    }

    @PostMapping("/{id}/duplicar")
    @PreAuthorize("@permisos.puede('ventas', 'crear')")
    public String duplicar(@PathVariable Long id, RedirectAttributes ra) {
        Optional<Pedido> original = pedidoService.obtenerPorId(id);
        if (original.isEmpty()) {
            Flash.error(ra, "Registro no encontrado");
            return "redirect:/web/pedidos-venta";
        }
        try {
            Pedido copia = pedidoService.duplicar(id);
            Flash.exito(ra, "Duplicado de " + original.get().getNumero() + ": " + copia.getNumero());
            return "redirect:/web/pedidos-venta/" + copia.getId() + "/editar";
        } catch (RuntimeException e) {
            log.error("Error al duplicar pedido {}: {}", id, e.getMessage(), e);
            Flash.error(ra, e.getMessage());
            return "redirect:/web/pedidos-venta/" + id;
        }
    }
}
