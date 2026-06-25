package alicanteweb.erp.controller.web;

import alicanteweb.erp.entities.*;
import alicanteweb.erp.service.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.*;
import java.util.stream.Collectors;
import org.springframework.security.access.prepost.PreAuthorize;

@Controller
@PreAuthorize("@permisos.puede('reportes', 'ver')")
@RequestMapping("/web/reportes")
public class ReportesWebController {

    private final FacturaService facturaService;
    private final FacturaCompraService facturaCompraService;
    private final OrdenProduccionService ordenProduccionService;
    private final LoteService loteService;
    private final HojaRutaService hojaRutaService;
    private final ArticuloService articuloService;
    private final ClienteService clienteService;

    public ReportesWebController(FacturaService fs, FacturaCompraService fcs,
                                  OrdenProduccionService ops, LoteService ls,
                                  HojaRutaService hrs, ArticuloService as,
                                  ClienteService cs) {
        this.facturaService = fs; this.facturaCompraService = fcs;
        this.ordenProduccionService = ops; this.loteService = ls;
        this.hojaRutaService = hrs; this.articuloService = as;
        this.clienteService = cs;
    }

    @GetMapping
    public String index(Model m) {
        m.addAttribute("moduloActivo","reportes"); m.addAttribute("titulo","Informes");
        return WebController.layout(m, "reportes/index");
    }

    @GetMapping("/ventas-mes")
    public String ventasMes(Model m) {
        YearMonth ym = YearMonth.now();
        LocalDate inicio = ym.atDay(1);
        LocalDate fin = ym.atEndOfMonth();

        List<Factura> facturas = facturaService.findByFechaBetweenAndEstado(inicio, fin, "EMITIDA");

        BigDecimal totalVentas = facturas.stream()
            .map(f -> f.getTotal() != null ? f.getTotal() : BigDecimal.ZERO)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Ventas por día
        Map<Integer, BigDecimal> porDia = new TreeMap<>();
        for (int d = 1; d <= fin.getDayOfMonth(); d++) porDia.put(d, BigDecimal.ZERO);
        for (Factura f : facturas) {
            int dia = f.getFecha().getDayOfMonth();
            porDia.merge(dia, f.getTotal() != null ? f.getTotal() : BigDecimal.ZERO, BigDecimal::add);
        }

        // Ventas por cliente
        Map<String, BigDecimal> porCliente = new LinkedHashMap<>();
        for (Factura f : facturas) {
            String nombre = f.getCliente() != null ? f.getCliente().getNombre() : "Sin cliente";
            porCliente.merge(nombre, f.getTotal() != null ? f.getTotal() : BigDecimal.ZERO, BigDecimal::add);
        }
        porCliente = porCliente.entrySet().stream()
            .sorted(Map.Entry.<String, BigDecimal>comparingByValue().reversed())
            .limit(10)
            .collect(LinkedHashMap::new, (m2, e) -> m2.put(e.getKey(), e.getValue()), LinkedHashMap::putAll);

        int dias = ym.lengthOfMonth();
        BigDecimal ticketMedio = totalVentas.compareTo(BigDecimal.ZERO) > 0 && !facturas.isEmpty()
            ? totalVentas.divide(BigDecimal.valueOf(facturas.size()), 2, java.math.RoundingMode.HALF_UP)
            : BigDecimal.ZERO;
        BigDecimal mediaDiaria = totalVentas.divide(BigDecimal.valueOf(dias), 2, java.math.RoundingMode.HALF_UP);

        m.addAttribute("moduloActivo","reportes");
        m.addAttribute("titulo","Ventas del mes");
        m.addAttribute("totalVentas", totalVentas);
        m.addAttribute("numFacturas", facturas.size());
        m.addAttribute("mes", ym.getMonth().toString() + " " + ym.getYear());
        m.addAttribute("porDia", porDia);
        m.addAttribute("porCliente", porCliente);
        m.addAttribute("ticketMedio", ticketMedio);
        m.addAttribute("mediaDiaria", mediaDiaria);
        return WebController.layout(m, "reportes/ventas-mes");
    }

    @GetMapping("/produccion")
    public String produccion(Model m) {
        YearMonth ym = YearMonth.now();
        LocalDate inicio = ym.atDay(1);
        LocalDate fin = ym.atEndOfMonth();

        List<OrdenProduccion> ordenes = ordenProduccionService.findByFechaBetween(inicio, fin);
        long totalOrdenes = ordenes.size();
        long finalizadas = ordenes.stream().filter(o -> "FINALIZADA".equals(o.getEstado())).count();
        long enCurso = ordenes.stream().filter(o -> "EN_CURSO".equals(o.getEstado())).count();
        long canceladas = ordenes.stream().filter(o -> "CANCELADA".equals(o.getEstado())).count();

        BigDecimal totalPlanificado = ordenes.stream()
            .map(o -> o.getCantidadPlanificada() != null ? o.getCantidadPlanificada() : BigDecimal.ZERO)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalProducido = ordenes.stream()
            .map(o -> o.getCantidadProducida() != null ? o.getCantidadProducida() : BigDecimal.ZERO)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        m.addAttribute("moduloActivo","reportes");
        m.addAttribute("titulo","Producción del mes");
        m.addAttribute("totalOrdenes", totalOrdenes);
        m.addAttribute("finalizadas", finalizadas);
        m.addAttribute("enCurso", enCurso);
        m.addAttribute("canceladas", canceladas);
        m.addAttribute("totalPlanificado", totalPlanificado);
        m.addAttribute("totalProducido", totalProducido);
        m.addAttribute("eficiencia", totalPlanificado.compareTo(BigDecimal.ZERO) > 0
            ? totalProducido.multiply(new BigDecimal("100")).divide(totalPlanificado, 1, java.math.RoundingMode.HALF_UP)
            : BigDecimal.ZERO);
        return WebController.layout(m, "reportes/produccion");
    }

    @GetMapping("/rentabilidad")
    public String rentabilidad(Model m) {

        List<Articulo> articulos = articuloService.findAll();

        List<Map<String, Object>> rentabilidad = articulos.stream()
            .filter(a -> Boolean.TRUE.equals(a.getActivo())
                      && a.getPvp() != null && a.getPvp().compareTo(BigDecimal.ZERO) > 0)
            .map(a -> {
                BigDecimal pvp = a.getPvp();
                BigDecimal coste = a.getCoste() != null ? a.getCoste() : BigDecimal.ZERO;
                BigDecimal margen = pvp.subtract(coste);
                BigDecimal margenPct = pvp.compareTo(BigDecimal.ZERO) > 0
                    ? margen.multiply(new BigDecimal("100"))
                            .divide(pvp, 1, java.math.RoundingMode.HALF_UP)
                    : BigDecimal.ZERO;
                Map<String, Object> entry = new LinkedHashMap<>();
                entry.put("nombre", a.getNombre());
                entry.put("codigo", a.getCodigo());
                entry.put("categoria", a.getCategoria() != null ? a.getCategoria() : "-");
                entry.put("pvp", pvp);
                entry.put("coste", coste);
                entry.put("margen", margen);
                entry.put("margenPct", margenPct);
                entry.put("tieneCoste", coste.compareTo(BigDecimal.ZERO) > 0);
                return entry;
            })
            .sorted((a, b) -> ((BigDecimal) b.get("margenPct")).compareTo((BigDecimal) a.get("margenPct")))
            .collect(java.util.stream.Collectors.toList());

        long conCoste = rentabilidad.stream().filter(e -> Boolean.TRUE.equals(e.get("tieneCoste"))).count();

        m.addAttribute("moduloActivo","reportes");
        m.addAttribute("titulo","Rentabilidad por producto");
        m.addAttribute("rentabilidad", rentabilidad);
        m.addAttribute("numArticulos", rentabilidad.size());
        m.addAttribute("numConCoste", conCoste);
        return WebController.layout(m, "reportes/rentabilidad");
    }

    @GetMapping("/reparto")
    public String reparto(Model m) {
        YearMonth ym = YearMonth.now();
        LocalDate inicio = ym.atDay(1);
        LocalDate fin = ym.atEndOfMonth();

        List<HojaRuta> hojas = hojaRutaService.findByFechaBetween(inicio, fin);
        long totalHojas = hojas.size();
        long finalizadas = hojas.stream().filter(h -> "FINALIZADA".equals(h.getEstado())).count();
        long enCurso = hojas.stream().filter(h -> "EN_CURSO".equals(h.getEstado())).count();

        m.addAttribute("moduloActivo","reportes");
        m.addAttribute("titulo","Reparto del mes");
        m.addAttribute("totalHojas", totalHojas);
        m.addAttribute("finalizadas", finalizadas);
        m.addAttribute("enCurso", enCurso);
        return WebController.layout(m, "reportes/reparto");
    }

    @GetMapping("/trazabilidad")
    public String trazabilidad(Model m) {
        long caducados = loteService.countByFechaCaducidadBefore(LocalDate.now());
        long porCaducar = loteService.countByFechaCaducidadBetween(LocalDate.now(), LocalDate.now().plusDays(7));
        long total = loteService.count();

        m.addAttribute("moduloActivo","reportes");
        m.addAttribute("titulo","Trazabilidad");
        m.addAttribute("caducados", caducados);
        m.addAttribute("porCaducar", porCaducar);
        m.addAttribute("totalLotes", total);
        return WebController.layout(m, "reportes/trazabilidad");
    }

    @GetMapping("/inventario")
    public String inventario(Model m) {
        m.addAttribute("moduloActivo","reportes"); m.addAttribute("titulo","Inventario");
        return WebController.layout(m, "reportes/inventario");
    }
}
