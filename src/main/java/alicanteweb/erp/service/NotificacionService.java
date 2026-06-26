package alicanteweb.erp.service;

import alicanteweb.erp.entities.Articulo;
import alicanteweb.erp.entities.Factura;
import alicanteweb.erp.entities.Lote;
import alicanteweb.erp.entities.OrdenProduccion;
import alicanteweb.erp.entities.Presupuesto;
import alicanteweb.erp.repository.ArticuloRepository;
import alicanteweb.erp.repository.FacturaRepository;
import alicanteweb.erp.repository.LoteRepository;
import alicanteweb.erp.repository.OrdenProduccionRepository;
import alicanteweb.erp.repository.PresupuestoRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Servicio de notificaciones y alertas automáticas
 */
@Service
@RequiredArgsConstructor
public class NotificacionService {
    private static final Logger log = LoggerFactory.getLogger(NotificacionService.class);

    private final PresupuestoRepository presupuestoRepository;
    private final ArticuloRepository articuloRepository;
    private final FacturaRepository facturaRepository;
    private final LoteRepository loteRepository;
    private final OrdenProduccionRepository ordenProduccionRepository;

    /**
     * Verifica presupuestos por caducar (ejecuta diariamente a las 9:00 AM)
     */
    @Scheduled(cron = "${notificaciones.presupuestos.cron:0 0 9 * * ?}")
    public void verificarPresupuestosPorCaducar() {
        try {
            log.info("Verificando presupuestos por caducar...");
            LocalDate hoy = LocalDate.now();
            LocalDate dentroDeUnaSemana = hoy.plusDays(7);
            List<Presupuesto> porCaducar = presupuestoRepository
                .findByFechaValidezBetween(hoy, dentroDeUnaSemana)
                .stream()
                .filter(p -> !"RECHAZADO".equals(p.getEstado()))
                .filter(p -> !"FACTURADO".equals(p.getEstado()))
                .toList();
            if (!porCaducar.isEmpty()) {
                log.warn("{} presupuestos caducan en los próximos 7 días", porCaducar.size());
                for (Presupuesto p : porCaducar) {
                    long diasRestantes = java.time.temporal.ChronoUnit.DAYS.between(hoy, p.getFechaValidez());
                    crearNotificacion("PRESUPUESTO_CADUCAR",
                        String.format("Presupuesto %s caduca en %d días", p.getNumero(), diasRestantes), "MEDIA");
                }
            }
        } catch (Exception e) {
            log.error("Error verificando presupuestos por caducar", e);
        }
    }

    /**
     * Verifica stock bajo (ejecuta diariamente a las 10:00 AM)
     */
    @Scheduled(cron = "${notificaciones.stock.cron:0 0 10 * * ?}")
    public void verificarStockBajo() {
        try {
            log.info("Verificando stock bajo...");
            List<Articulo> stockBajo = articuloRepository.findConStockBajo();
            if (!stockBajo.isEmpty()) {
                log.warn("{} artículos con stock bajo", stockBajo.size());
                for (Articulo a : stockBajo) {
                    crearNotificacion("STOCK_BAJO",
                        String.format("Stock bajo en %s: %s unidades (mínimo: %s)",
                            a.getNombre(), a.getStock(), a.getStockMinimo()), "ALTA");
                }
            }
        } catch (Exception e) {
            log.error("Error verificando stock bajo", e);
        }
    }

    /**
     * Verifica facturas pendientes de pago (ejecuta los lunes a las 9:00 AM)
     */
    @Scheduled(cron = "${notificaciones.pagos.cron:0 0 9 * * MON}")
    public void verificarFacturasPendientesPago() {
        try {
            log.info("Verificando facturas pendientes de pago...");
            List<Factura> pendientes = facturaRepository.findPendientesCobro(LocalDate.now().minusDays(30));
            if (!pendientes.isEmpty()) {
                BigDecimal totalPendiente = pendientes.stream()
                    .map(f -> f.getTotal() != null ? f.getTotal() : BigDecimal.ZERO)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
                log.warn("{} facturas pendientes por {}€", pendientes.size(), totalPendiente);
                crearNotificacion("FACTURAS_PENDIENTES",
                    String.format("%d facturas pendientes por %s€", pendientes.size(), totalPendiente), "ALTA");
            }
        } catch (Exception e) {
            log.error("Error verificando facturas pendientes", e);
        }
    }

    /**
     * Resumen semanal (ejecuta los lunes a las 8:00 AM)
     */
    @Scheduled(cron = "${notificaciones.resumen.cron:0 0 8 * * MON}")
    public void resumenSemanal() {
        try {
            LocalDate inicioSemana = LocalDate.now().minusDays(7);
            LocalDate finSemana = LocalDate.now();
            List<Factura> facturasSemana = facturaRepository.findByFechaBetween(inicioSemana, finSemana);
            long facturasEmitidas = facturasSemana.size();
            BigDecimal ventasSemana = facturasSemana.stream()
                .map(f -> f.getTotal() != null ? f.getTotal() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
            log.info("Resumen semanal ({} - {}): {} facturas, {}€", inicioSemana, finSemana, facturasEmitidas, ventasSemana);
            crearNotificacion("RESUMEN_SEMANAL",
                String.format("Semana: %d facturas, %s€ en ventas", facturasEmitidas, ventasSemana), "BAJA");
        } catch (Exception e) {
            log.error("Error generando resumen semanal", e);
        }
    }

    /**
     * Crea una notificación (puede guardarse en BD, enviar email, etc.)
     */
    private void crearNotificacion(String tipo, String mensaje, String prioridad) {
        log.info("📬 Notificación [{}] {}: {}", prioridad, tipo, mensaje);

        // Aquí se podría:
        // 1. Guardar en tabla de notificaciones
        // 2. Enviar email
        // 3. Mostrar en UI
        // 4. Enviar push notification
        // 5. Webhook a sistema externo

        // Por ahora solo logueamos
    }

    /**
     * Obtiene el contador de notificaciones pendientes
     */
    public NotificacionResumen obtenerResumenNotificaciones() {
        int presupuestosCaducar = (int) presupuestoRepository
            .findByFechaValidezBetween(LocalDate.now(), LocalDate.now().plusDays(7))
            .stream()
            .filter(p -> !"RECHAZADO".equals(p.getEstado()))
            .filter(p -> !"FACTURADO".equals(p.getEstado()))
            .count();

        int stockBajo = articuloRepository.findConStockBajo().size();

        int facturasPendientes = facturaRepository.findPendientesCobro(
            LocalDate.now().minusDays(30)).size();

        return new NotificacionResumen(
            presupuestosCaducar,
            stockBajo,
            facturasPendientes,
            (int) loteRepository.findByFechaCaducidadBetween(LocalDate.now(), LocalDate.now().plusDays(3)).size(),
            (int) ordenProduccionRepository.findByEstado("PLANIFICADA").size(),
            presupuestosCaducar + stockBajo + facturasPendientes
        );
    }

    /**
     * Verifica lotes próximos a caducar (diario a las 7:00 AM)
     */
    @Scheduled(cron = "0 0 7 * * ?")
    public void verificarLotesProximosACaducar() {
        try {
            LocalDate hoy = LocalDate.now();
            List<Lote> lotes = loteRepository.findByFechaCaducidadBetween(hoy, hoy.plusDays(3));
            if (!lotes.isEmpty()) {
                log.warn("{} lote(s) caducan en los próximos 3 días", lotes.size());
                lotes.forEach(l -> crearNotificacion("LOTE_CADUCAR",
                    String.format("Lote %s (%s) caduca el %s", l.getCodigo(),
                        l.getArticulo() != null ? l.getArticulo().getNombre() : "?", l.getFechaCaducidad()), "ALTA"));
            }
        } catch (Exception e) {
            log.error("Error verificando lotes próximos a caducar", e);
        }
    }

    /**
     * Verifica órdenes de producción pendientes (diario a las 8:00 AM)
     */
    @Scheduled(cron = "0 0 8 * * ?")
    public void verificarOrdenesPendientes() {
        try {
            List<OrdenProduccion> planificadas = ordenProduccionRepository.findByEstado("PLANIFICADA");
            if (!planificadas.isEmpty()) {
                log.info("{} orden(es) de producción planificadas pendientes", planificadas.size());
            }
        } catch (Exception e) {
            log.error("Error verificando órdenes pendientes", e);
        }
    }

    /**
     * Resumen de notificaciones
     */
    public record NotificacionResumen(
        int presupuestosPorCaducar,
        int articulosStockBajo,
        int facturasPendientes,
        int lotesPorCaducar,
        int ordenesPendientes,
        int totalNotificaciones
    ) {}
}

