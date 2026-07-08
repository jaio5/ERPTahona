package alicanteweb.erp.service;

import alicanteweb.erp.entities.*;
import alicanteweb.erp.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import alicanteweb.erp.util.DesgloseFiscal;
import alicanteweb.erp.util.FinancialMath;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class DocumentoService {

    private final PedidoService pedidoService;
    private final AlbaranService albaranService;
    private final FacturaService facturaService;
    private final ArticuloService articuloService;
    private final ClienteService clienteService;
    private final AlmacenService almacenService;
    private final AlbaranVentaRepository albaranRepository;
    private final FacturaRepository facturaRepository;
    private final PedidoRepository pedidoRepository;
    private final PresupuestoRepository presupuestoRepository;
    private final TarifaClienteService tarifaClienteService;

    public DocumentoService(PedidoService pedidoService,
                            AlbaranService albaranService,
                            FacturaService facturaService,
                            ArticuloService articuloService,
                            ClienteService clienteService,
                            AlmacenService almacenService,
                            AlbaranVentaRepository albaranRepository,
                            FacturaRepository facturaRepository,
                            PedidoRepository pedidoRepository,
                            PresupuestoRepository presupuestoRepository,
                            TarifaClienteService tarifaClienteService) {
        this.pedidoService = pedidoService;
        this.albaranService = albaranService;
        this.facturaService = facturaService;
        this.articuloService = articuloService;
        this.clienteService = clienteService;
        this.almacenService = almacenService;
        this.albaranRepository = albaranRepository;
        this.facturaRepository = facturaRepository;
        this.pedidoRepository = pedidoRepository;
        this.presupuestoRepository = presupuestoRepository;
        this.tarifaClienteService = tarifaClienteService;
    }

    // =========================== PEDIDO ===========================

    @Transactional
    public Pedido guardarPedido(Long id, Map<String, Object> datos) {
        Pedido pedido = id != null ? pedidoRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Pedido no encontrado")) : new Pedido();
        Long clienteId = parseLong(datos.get("clienteId"));
        if (clienteId == null) throw new IllegalArgumentException("Cliente obligatorio");
        pedido.setCliente(clienteService.findById(clienteId).orElseThrow(() -> new IllegalArgumentException("Cliente no encontrado")));
        pedido.setFecha(parseDate(datos.get("fecha")));
        pedido.setObservaciones(parseString(datos.get("observaciones")));
        if (pedido.getEstado() == null || pedido.getEstado().isBlank()) pedido.setEstado("PENDIENTE");
        boolean esNuevo = pedido.getId() == null;
        pedido = pedidoService.guardar(pedido);
        guardarLineasPedido(pedido, datos, esNuevo);
        return pedido;
    }

    private void guardarLineasPedido(Pedido pedido, Map<String, Object> datos, boolean esNuevo) {
        List<Map<String, Object>> lineas = castList(datos.get("lineas"));
        if (lineas == null || lineas.isEmpty()) return;

        Map<Long, Articulo> articuloById = cargarArticulosDeLíneas(lineas);
        Long clienteId = pedido.getCliente() != null ? pedido.getCliente().getId() : null;

        pedido.getPedidoLineas().clear();
        BigDecimal total = BigDecimal.ZERO;
        for (Map<String, Object> l : lineas) {
            PedidoLinea linea = new PedidoLinea();
            linea.setPedido(pedido);
            Long artId = parseLong(l.get("articuloId"));
            asignarArticulo(linea, artId, articuloById);
            BigDecimal precio = parseDecimal(l.get("precio"));
            BigDecimal descuento = parseDecimal(l.get("descuento"));
            PrecioDescuento pd = resolverPrecioDescuento(clienteId, artId, precio, descuento);
            BigDecimal cantidad = parseDecimal(l.get("cantidad"));
            BigDecimal iva = parseDecimal(l.get("iva"));
            linea.setCantidad(cantidad);
            linea.setPrecio(pd.precio());
            linea.setIva(iva);
            linea.setDescuento(pd.descuento());
            pedido.getPedidoLineas().add(linea);
            BigDecimal base = FinancialMath.subtotalConDescuento(cantidad, pd.precio(), pd.descuento());
            total = total.add(base).add(FinancialMath.porcentaje(base, iva));
        }
        pedido.setTotal(total);
        pedidoRepository.save(pedido);
    }

    // =========================== PRESUPUESTO ===========================

    @Transactional
    public Presupuesto guardarPresupuesto(Long id, Map<String, Object> datos) {
        Presupuesto pre = id != null
                ? presupuestoRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Presupuesto no encontrado"))
                : new Presupuesto();
        Long clienteId = parseLong(datos.get("clienteId"));
        if (clienteId == null) throw new IllegalArgumentException("Cliente obligatorio");
        pre.setCliente(clienteService.findById(clienteId).orElseThrow(() -> new IllegalArgumentException("Cliente no encontrado")));
        pre.setFecha(parseDate(datos.get("fecha")));
        pre.setFechaValidez(parseDate(datos.get("fechaValidez")));
        pre.setObservaciones(parseString(datos.get("observaciones")));
        if (pre.getEstado() == null || pre.getEstado().isBlank()) pre.setEstado("BORRADOR");
        if (pre.getNumero() == null || pre.getNumero().isBlank()) {
            int year = java.time.LocalDate.now().getYear();
            long count = presupuestoRepository.count() + 1;
            pre.setNumero(String.format("PRE-%d-%05d", year, count));
        }
        guardarLineasPresupuesto(pre, datos);
        return presupuestoRepository.save(pre);
    }

    private void guardarLineasPresupuesto(Presupuesto pre, Map<String, Object> datos) {
        List<Map<String, Object>> lineas = castList(datos.get("lineas"));
        if (lineas == null || lineas.isEmpty()) return;

        Map<Long, Articulo> articuloById = cargarArticulosDeLíneas(lineas);
        Long clienteId = pre.getCliente() != null ? pre.getCliente().getId() : null;

        pre.getLineas().clear();
        BigDecimal total = BigDecimal.ZERO;
        int orden = 1;
        for (Map<String, Object> l : lineas) {
            PresupuestoLinea linea = new PresupuestoLinea();
            linea.setPresupuesto(pre);
            Long artId = parseLong(l.get("articuloId"));
            asignarArticulo(linea, artId, articuloById);
            BigDecimal cantidad = parseDecimal(l.get("cantidad"));
            BigDecimal iva = parseDecimal(l.get("iva"));
            PrecioDescuento pd = resolverPrecioDescuento(clienteId, artId,
                    parseDecimal(l.get("precio")), parseDecimal(l.get("descuento")));
            linea.setCantidad(cantidad);
            linea.setPrecioUnitario(pd.precio());
            linea.setTipoIva(iva);
            linea.setDescuento(pd.descuento());
            BigDecimal base = FinancialMath.subtotalConDescuento(cantidad, pd.precio(), pd.descuento());
            linea.setImporte(base);
            linea.setOrden(orden++);
            total = total.add(base).add(FinancialMath.porcentaje(base, iva));
            pre.getLineas().add(linea);
        }
        pre.setTotal(total);
    }

    // =========================== ALBARÁN ===========================

    @Transactional
    public AlbaranVenta guardarAlbaran(Long id, Map<String, Object> datos) {
        AlbaranVenta alb = id != null ? albaranRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Albarán no encontrado")) : new AlbaranVenta();
        Long clienteId = parseLong(datos.get("clienteId"));
        Long almacenId = parseLong(datos.get("almacenId"));
        if (clienteId == null) throw new IllegalArgumentException("Cliente obligatorio");
        alb.setCliente(clienteService.findById(clienteId).orElseThrow(() -> new IllegalArgumentException("Cliente no encontrado")));
        if (almacenId != null) {
            alb.setAlmacen(almacenService.findById(almacenId).orElseThrow(() -> new IllegalArgumentException("Almacén no encontrado")));
        }
        alb.setFecha(parseDate(datos.get("fecha")));
        alb.setObservaciones(parseString(datos.get("observaciones")));
        alb.setNumeroLote(parseString(datos.get("numeroLote")));
        if (datos.containsKey("descuentoGlobalTipo")) alb.setDescuentoGlobalTipo(descuentoTipo(datos.get("descuentoGlobalTipo")));
        if (datos.containsKey("descuentoGlobalValor")) alb.setDescuentoGlobalValor(parseDecimal(datos.get("descuentoGlobalValor")));
        boolean esNuevo = alb.getId() == null;
        alb = albaranService.guardar(alb);
        guardarLineasAlbaran(alb, datos, esNuevo);
        recalcularTotalAlbaran(alb);
        albaranRepository.updateTotal(alb.getId(), alb.getTotal());
        return alb;
    }

    private void guardarLineasAlbaran(AlbaranVenta alb, Map<String, Object> datos, boolean esNuevo) {
        List<Map<String, Object>> lineas = castList(datos.get("lineas"));
        if (lineas == null || lineas.isEmpty()) return;

        Map<Long, Articulo> articuloById = cargarArticulosDeLíneas(lineas);
        Long clienteId = alb.getCliente() != null ? alb.getCliente().getId() : null;

        alb.getAlbaranVentaLineas().clear();
        for (Map<String, Object> l : lineas) {
            AlbaranVentaLinea linea = new AlbaranVentaLinea();
            linea.setAlbaran(alb);
            Long artId = parseLong(l.get("articuloId"));
            asignarArticulo(linea, artId, articuloById);
            BigDecimal descuentoManual = parseDecimal(l.get("descuento"));
            PrecioDescuento pd = resolverPrecioDescuento(clienteId, artId,
                    parseDecimal(l.get("precio")), descuentoManual);
            String descTipo = descuentoManual.signum() > 0 ? descuentoTipo(l.get("descuentoTipo")) : DesgloseFiscal.PORCENTAJE;
            linea.setCantidad(parseDecimal(l.get("cantidad")));
            linea.setPrecio(pd.precio());
            linea.setIva(parseDecimal(l.get("iva")));
            linea.setDescuento(pd.descuento());
            linea.setDescuentoTipo(descTipo);
            alb.getAlbaranVentaLineas().add(linea);
        }
        albaranRepository.save(alb);
    }

    /** Recalcula el total del albarán con la fuente única {@link DesgloseFiscal} (incluye descuento global). */
    private void recalcularTotalAlbaran(AlbaranVenta alb) {
        List<DesgloseFiscal.Linea> calculo = new ArrayList<>();
        for (AlbaranVentaLinea linea : alb.getAlbaranVentaLineas()) {
            calculo.add(new DesgloseFiscal.Linea(linea.getCantidad(), linea.getPrecio(), linea.getIva(),
                    linea.getDescuentoTipo(), linea.getDescuento()));
        }
        DesgloseFiscal.Resultado r = DesgloseFiscal.calcular(
                calculo, alb.getDescuentoGlobalTipo(), alb.getDescuentoGlobalValor());
        alb.setTotal(r.total());
    }

    // =========================== FACTURA ===========================

    @Transactional
    public Factura guardarFactura(Long id, Map<String, Object> datos) {
        Factura fac = id != null ? facturaRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Factura no encontrada")) : new Factura();
        Long clienteId = parseLong(datos.get("clienteId"));
        if (clienteId == null) throw new IllegalArgumentException("Cliente obligatorio");
        fac.setCliente(clienteService.findById(clienteId).orElseThrow(() -> new IllegalArgumentException("Cliente no encontrado")));
        fac.setFecha(parseDate(datos.get("fecha")));
        fac.setMedioCobro(parseString(datos.get("medioCobro")));
        fac.setObservaciones(parseString(datos.get("observaciones")));
        fac.setFechaVencimiento(parseDate(datos.get("fechaVencimiento")));
        if (datos.containsKey("rappelPorcentaje")) fac.setRappelPorcentaje(parseDecimal(datos.get("rappelPorcentaje")));
        if (datos.containsKey("rappelImporte")) fac.setRappelImporte(parseDecimal(datos.get("rappelImporte")));
        if (datos.containsKey("descuentoGlobalTipo")) fac.setDescuentoGlobalTipo(descuentoTipo(datos.get("descuentoGlobalTipo")));
        if (datos.containsKey("descuentoGlobalValor")) fac.setDescuentoGlobalValor(parseDecimal(datos.get("descuentoGlobalValor")));
        if (fac.getEstado() == null || fac.getEstado().isBlank()) fac.setEstado("BORRADOR");
        boolean esNuevo = fac.getId() == null;
        fac = facturaService.save(fac);
        guardarLineasFactura(fac, datos, esNuevo);
        return fac;
    }

    private void guardarLineasFactura(Factura fac, Map<String, Object> datos, boolean esNuevo) {
        List<Map<String, Object>> lineas = castList(datos.get("lineas"));
        if (lineas == null || lineas.isEmpty()) return;

        Map<Long, Articulo> articuloById = cargarArticulosDeLíneas(lineas);
        Long clienteId = fac.getCliente() != null ? fac.getCliente().getId() : null;

        fac.getFacturaLineas().clear();
        List<DesgloseFiscal.Linea> calculo = new ArrayList<>();
        for (Map<String, Object> l : lineas) {
            FacturaLinea linea = new FacturaLinea();
            linea.setFactura(fac);
            Long artId = parseLong(l.get("articuloId"));
            asignarArticuloFactura(linea, artId, articuloById);
            BigDecimal cantidad = parseDecimal(l.get("cantidad"));
            BigDecimal iva = parseDecimal(l.get("iva"));
            BigDecimal descuentoManual = parseDecimal(l.get("descuento"));
            PrecioDescuento pd = resolverPrecioDescuento(clienteId, artId,
                    parseDecimal(l.get("precio")), descuentoManual);
            // El tipo de descuento solo lo fija el usuario si el descuento es manual; si viene de
            // la tarifa del cliente es un porcentaje.
            String descTipo = descuentoManual.signum() > 0 ? descuentoTipo(l.get("descuentoTipo")) : DesgloseFiscal.PORCENTAJE;
            linea.setCantidad(cantidad);
            linea.setPrecioUnitario(pd.precio());
            linea.setIva(iva);
            linea.setDescuento(pd.descuento());
            linea.setDescuentoTipo(descTipo);
            fac.getFacturaLineas().add(linea);
            calculo.add(new DesgloseFiscal.Linea(cantidad, pd.precio(), iva, descTipo, pd.descuento()));
        }
        DesgloseFiscal.Resultado r = DesgloseFiscal.calcular(
                calculo, fac.getDescuentoGlobalTipo(), fac.getDescuentoGlobalValor());
        fac.setBaseImponible(r.base());
        fac.setTotalIva(r.iva());
        fac.setTotal(r.total());
        facturaRepository.save(fac);
        facturaRepository.updateTotales(fac.getId(), r.total(), r.base(), r.iva());
    }

    // =========================== HELPERS LÍNEAS ===========================

    private Map<Long, Articulo> cargarArticulosDeLíneas(List<Map<String, Object>> lineas) {
        List<Long> artIds = lineas.stream()
                .map(l -> parseLong(l.get("articuloId")))
                .filter(java.util.Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());
        return articuloService.findAllById(artIds).stream()
                .collect(Collectors.toMap(Articulo::getId, a -> a));
    }

    private void asignarArticulo(Object linea, Long artId, Map<Long, Articulo> articuloById) {
        if (artId == null) return;
        Articulo art = articuloById.get(artId);
        if (art == null) throw new IllegalArgumentException("Artículo no encontrado: " + artId);
        if (linea instanceof PedidoLinea l) { l.setArticulo(art); l.setDescripcion(art.getNombre()); }
        else if (linea instanceof PresupuestoLinea l) { l.setArticulo(art); l.setDescripcion(art.getNombre()); }
        else if (linea instanceof AlbaranVentaLinea l) { l.setArticulo(art); l.setDescripcion(art.getNombre()); }
    }

    private void asignarArticuloFactura(FacturaLinea linea, Long artId, Map<Long, Articulo> articuloById) {
        if (artId == null) return;
        Articulo art = articuloById.get(artId);
        if (art == null) throw new IllegalArgumentException("Artículo no encontrado: " + artId);
        linea.setArticulo(art);
        linea.setDescripcion(art.getNombre());
    }

    private PrecioDescuento resolverPrecioDescuento(Long clienteId, Long artId, BigDecimal precio, BigDecimal descuento) {
        if (artId != null && clienteId != null) {
            precio = tarifaClienteService.resolverPrecio(clienteId, artId, precio);
            // La tarifa del cliente solo se aplica si NO se ha indicado un descuento manual
            // en el documento (el manual, específico de esta factura/albarán, tiene prioridad).
            boolean sinDescuentoManual = descuento == null || descuento.compareTo(BigDecimal.ZERO) == 0;
            if (sinDescuentoManual) {
                BigDecimal tarifaDesc = tarifaClienteService.resolverDescuento(clienteId, artId);
                if (tarifaDesc.compareTo(BigDecimal.ZERO) > 0) descuento = tarifaDesc;
            }
        }
        return new PrecioDescuento(precio, descuento);
    }

    private record PrecioDescuento(BigDecimal precio, BigDecimal descuento) {}

    /** Normaliza el tipo de descuento a PORCENTAJE (por defecto) o IMPORTE. */
    private static String descuentoTipo(Object v) {
        String s = v == null ? null : String.valueOf(v).trim();
        return DesgloseFiscal.IMPORTE.equalsIgnoreCase(s) ? DesgloseFiscal.IMPORTE : DesgloseFiscal.PORCENTAJE;
    }

    // =========================== UTILIDADES ===========================

    @SuppressWarnings("unchecked")
    private static <T> List<T> castList(Object obj) {
        return obj == null ? null : (List<T>) obj;
    }

    private Long parseLong(Object v) {
        if (v == null) return null;
        if (v instanceof Number) return ((Number) v).longValue();
        String s = String.valueOf(v).trim();
        if (s.isEmpty()) return null;
        return Long.valueOf(s);
    }

    private BigDecimal parseDecimal(Object v) {
        if (v == null) return BigDecimal.ZERO;
        String s = (v instanceof Number n) ? new BigDecimal(n.toString()).toPlainString()
                : String.valueOf(v).trim().replace(",", ".");
        if (s.isEmpty()) return BigDecimal.ZERO;
        return new BigDecimal(s);
    }

    private LocalDate parseDate(Object v) {
        if (v == null) return LocalDate.now();
        if (v instanceof LocalDate) return (LocalDate) v;
        String s = String.valueOf(v).trim();
        if (s.isEmpty()) return LocalDate.now();
        return LocalDate.parse(s);
    }

    private String parseString(Object v) {
        return v == null ? null : String.valueOf(v).trim();
    }
}
