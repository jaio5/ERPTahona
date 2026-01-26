package alicanteweb.erp.service;

import alicanteweb.erp.entities.Articulo;
import alicanteweb.erp.entities.Factura;
import alicanteweb.erp.entities.Presupuesto;
import alicanteweb.erp.repository.ArticuloRepository;
import alicanteweb.erp.repository.FacturaRepository;
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

    /**
     * Verifica presupuestos por caducar (ejecuta diariamente a las 9:00 AM)
     */
    @Scheduled(cron = "${notificaciones.presupuestos.cron:0 0 9 * * ?}")
    public void verificarPresupuestosPorCaducar() {
        log.info("🔔 Verificando presupuestos por caducar...");

        LocalDate hoy = LocalDate.now();
        LocalDate dentroDeUnaSemana = hoy.plusDays(7);

        List<Presupuesto> porCaducar = presupuestoRepository
            .findByFechaValidezBetween(hoy, dentroDeUnaSemana)
            .stream()
            .filter(p -> !"RECHAZADO".equals(p.getEstado()))
            .filter(p -> !"FACTURADO".equals(p.getEstado()))
            .toList();

        if (!porCaducar.isEmpty()) {
            log.warn("⚠️ {} presupuestos caducan en los próximos 7 días:", porCaducar.size());

            for (Presupuesto p : porCaducar) {
                long diasRestantes = java.time.temporal.ChronoUnit.DAYS.between(hoy, p.getFechaValidez());
                log.warn("   - Presupuesto {} ({}) caduca en {} días ({})",
                    p.getNumero(),
                    p.getCliente() != null ? p.getCliente().getNombre() : "Sin cliente",
                    diasRestantes,
                    p.getFechaValidez());

                // Aquí se podría enviar email, mostrar notificación UI, etc.
                crearNotificacion(
                    "PRESUPUESTO_CADUCAR",
                    String.format("Presupuesto %s caduca en %d días", p.getNumero(), diasRestantes),
                    "MEDIA"
                );
            }
        } else {
            log.info("✅ No hay presupuestos próximos a caducar");
        }
    }

    /**
     * Verifica stock bajo (ejecuta diariamente a las 10:00 AM)
     */
    @Scheduled(cron = "${notificaciones.stock.cron:0 0 10 * * ?}")
    public void verificarStockBajo() {
        log.info("🔔 Verificando stock bajo...");

        List<Articulo> stockBajo = articuloRepository.findAll()
            .stream()
            .filter(a -> a.getStock() != null)
            .filter(a -> a.getStockMinimo() != null)
            .filter(a -> a.getStock().compareTo(a.getStockMinimo()) <= 0)
            .toList();

        if (!stockBajo.isEmpty()) {
            log.warn("⚠️ {} artículos con stock bajo:", stockBajo.size());

            for (Articulo a : stockBajo) {
                log.warn("   - {} (Stock: {}, Mínimo: {})",
                    a.getNombre(),
                    a.getStock(),
                    a.getStockMinimo());

                crearNotificacion(
                    "STOCK_BAJO",
                    String.format("Stock bajo en %s: %s unidades (mínimo: %s)",
                        a.getNombre(), a.getStock(), a.getStockMinimo()),
                    "ALTA"
                );
            }
        } else {
            log.info("✅ Todos los artículos tienen stock suficiente");
        }
    }

    /**
     * Verifica facturas pendientes de pago (ejecuta los lunes a las 9:00 AM)
     */
    @Scheduled(cron = "${notificaciones.pagos.cron:0 0 9 * * MON}")
    public void verificarFacturasPendientesPago() {
        log.info("🔔 Verificando facturas pendientes de pago...");

        List<Factura> pendientes = facturaRepository.findAll()
            .stream()
            .filter(f -> "PENDIENTE".equals(f.getEstado()) || "EMITIDA".equals(f.getEstado()))
            .filter(f -> f.getFecha() != null)
            .filter(f -> f.getFecha().isBefore(LocalDate.now().minusDays(30)))
            .toList();

        if (!pendientes.isEmpty()) {
            log.warn("⚠️ {} facturas con más de 30 días pendientes:", pendientes.size());

            BigDecimal totalPendiente = BigDecimal.ZERO;

            for (Factura f : pendientes) {
                long diasPendientes = java.time.temporal.ChronoUnit.DAYS.between(
                    f.getFecha(), LocalDate.now());

                log.warn("   - Factura {} ({}) - {} días - {}€",
                    f.getNumero(),
                    f.getCliente() != null ? f.getCliente().getNombre() : "Sin cliente",
                    diasPendientes,
                    f.getTotal());

                totalPendiente = totalPendiente.add(f.getTotal() != null ? f.getTotal() : BigDecimal.ZERO);
            }

            log.warn("💰 Total pendiente de cobro: {}€", totalPendiente);

            crearNotificacion(
                "FACTURAS_PENDIENTES",
                String.format("%d facturas pendientes por %s€", pendientes.size(), totalPendiente),
                "ALTA"
            );
        } else {
            log.info("✅ No hay facturas con retraso en el pago");
        }
    }

    /**
     * Resumen semanal (ejecuta los lunes a las 8:00 AM)
     */
    @Scheduled(cron = "${notificaciones.resumen.cron:0 0 8 * * MON}")
    public void resumenSemanal() {
        log.info("📊 Generando resumen semanal...");

        LocalDate inicioSemana = LocalDate.now().minusDays(7);
        LocalDate finSemana = LocalDate.now();

        // Facturas de la semana
        long facturasEmitidas = facturaRepository
            .findByFechaBetween(inicioSemana, finSemana)
            .size();

        BigDecimal ventasSemana = facturaRepository
            .findByFechaBetween(inicioSemana, finSemana)
            .stream()
            .map(f -> f.getTotal() != null ? f.getTotal() : BigDecimal.ZERO)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        log.info("═══════════════════════════════════════════");
        log.info("  RESUMEN SEMANAL ({} - {})", inicioSemana, finSemana);
        log.info("═══════════════════════════════════════════");
        log.info("  Facturas emitidas: {}", facturasEmitidas);
        log.info("  Ventas totales: {}€", ventasSemana);
        log.info("═══════════════════════════════════════════");

        crearNotificacion(
            "RESUMEN_SEMANAL",
            String.format("Semana: %d facturas, %s€ en ventas", facturasEmitidas, ventasSemana),
            "BAJA"
        );
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

        int stockBajo = (int) articuloRepository.findAll()
            .stream()
            .filter(a -> a.getStock() != null && a.getStockMinimo() != null)
            .filter(a -> a.getStock().compareTo(a.getStockMinimo()) <= 0)
            .count();

        int facturasPendientes = (int) facturaRepository.findAll()
            .stream()
            .filter(f -> "PENDIENTE".equals(f.getEstado()) || "EMITIDA".equals(f.getEstado()))
            .filter(f -> f.getFecha() != null)
            .filter(f -> f.getFecha().isBefore(LocalDate.now().minusDays(30)))
            .count();

        return new NotificacionResumen(
            presupuestosCaducar,
            stockBajo,
            facturasPendientes,
            presupuestosCaducar + stockBajo + facturasPendientes
        );
    }

    /**
     * Resumen de notificaciones
     */
    public record NotificacionResumen(
        int presupuestosPorCaducar,
        int articulosStockBajo,
        int facturasPendientes,
        int totalNotificaciones
    ) {}
}

