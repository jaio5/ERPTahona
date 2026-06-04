package alicanteweb.erp.controller.web;

import alicanteweb.erp.entities.*;
import alicanteweb.erp.service.*;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.*;

@Controller
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
    public String index(HttpSession s, Model m) {
        if (WebController.requireLogin(s)) return "redirect:/web/login";
        m.addAttribute("moduloActivo","reportes"); m.addAttribute("titulo","Informes");
        return WebController.layout(m, "reportes/index");
    }

    @GetMapping("/ventas-mes")
    public String ventasMes(HttpSession s, Model m) {
        if (WebController.requireLogin(s)) return "redirect:/web/login";
        YearMonth ym = YearMonth.now();
        LocalDate inicio = ym.atDay(1);
        LocalDate fin = ym.atEndOfMonth();

        List<Factura> facturas = facturaService.findAll().stream()
            .filter(f -> f.getFecha() != null && !f.getFecha().isBefore(inicio) && !f.getFecha().isAfter(fin)
                     && "EMITIDA".equals(f.getEstado()))
            .toList();

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

        m.addAttribute("moduloActivo","reportes");
        m.addAttribute("titulo","Ventas del mes");
        m.addAttribute("totalVentas", totalVentas);
        m.addAttribute("numFacturas", facturas.size());
        m.addAttribute("mes", ym.getMonth().toString() + " " + ym.getYear());
        m.addAttribute("porDia", porDia);
        m.addAttribute("porCliente", porCliente);
        return WebController.layout(m, "reportes/ventas-mes");
    }

    @GetMapping("/produccion")
    public String produccion(HttpSession s, Model m) {
        if (WebController.requireLogin(s)) return "redirect:/web/login";
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
    public String rentabilidad(HttpSession s, Model m) {
        if (WebController.requireLogin(s)) return "redirect:/web/login";
        List<Receta> recetas = new java.util.ArrayList<>();
        // Simple profitability: PVP - cost for each article with a recipe
        m.addAttribute("moduloActivo","reportes");
        m.addAttribute("titulo","Rentabilidad");
        m.addAttribute("recetas", recetas);
        return WebController.layout(m, "reportes/rentabilidad");
    }

    @GetMapping("/reparto")
    public String reparto(HttpSession s, Model m) {
        if (WebController.requireLogin(s)) return "redirect:/web/login";
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
    public String trazabilidad(HttpSession s, Model m) {
        if (WebController.requireLogin(s)) return "redirect:/web/login";
        long caducados = loteService.findByFechaCaducidadBefore(LocalDate.now()).size();
        long porCaducar = loteService.findByFechaCaducidadBetween(LocalDate.now(), LocalDate.now().plusDays(7)).size();
        long total = loteService.findAll().size();

        m.addAttribute("moduloActivo","reportes");
        m.addAttribute("titulo","Trazabilidad");
        m.addAttribute("caducados", caducados);
        m.addAttribute("porCaducar", porCaducar);
        m.addAttribute("totalLotes", total);
        return WebController.layout(m, "reportes/trazabilidad");
    }
}
