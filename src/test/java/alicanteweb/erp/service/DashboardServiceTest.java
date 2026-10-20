package alicanteweb.erp.service;

import alicanteweb.erp.entities.AlbaranVenta;
import alicanteweb.erp.entities.Factura;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Resumen "Hoy" del panel contra H2 real: separa mostrador (serie TPV) de
 * facturación ordinaria y nunca cuenta borradores ni anuladas.
 */
@DataJpaTest
@Import(DashboardService.class)
class DashboardServiceTest {

    /** Día aislado del resto de datos de prueba. */
    private static final LocalDate DIA = LocalDate.of(2031, 3, 14);

    @Autowired
    TestEntityManager em;

    @Autowired
    DashboardService service;

    private int secuencia = 0;

    private Factura factura(String serie, LocalDate fecha, String total, String estado) {
        Factura f = nueva(serie, fecha, total, estado);
        em.persist(f);
        return f;
    }

    /** Factura sin persistir: los campos se fijan antes del persist (las emitidas son inalterables). */
    private Factura nueva(String serie, LocalDate fecha, String total, String estado) {
        Factura f = new Factura();
        f.setNumero("DSH-" + serie + "-" + (++secuencia) + "-" + System.nanoTime());
        f.setSerie(serie);
        f.setFecha(fecha);
        f.setTotal(new BigDecimal(total));
        f.setEstado(estado);
        return f;
    }

    private void albaran(LocalDate fecha, String estado) {
        AlbaranVenta a = new AlbaranVenta();
        a.setNumero("ALB-DSH-" + (++secuencia) + "-" + System.nanoTime());
        a.setFecha(fecha);
        a.setEstado(estado);
        em.persist(a);
    }

    @Test
    void separaVentasTpvDeFacturasOrdinarias() {
        factura(TpvService.SERIE_TPV, DIA, "3.50", "EMITIDA");
        factura(TpvService.SERIE_TPV, DIA, "6.00", "EMITIDA");
        factura("A", DIA, "100.00", "EMITIDA");
        // Otro día: no cuenta
        factura(TpvService.SERIE_TPV, DIA.minusDays(1), "50.00", "EMITIDA");
        factura("A", DIA.plusDays(1), "70.00", "EMITIDA");
        em.flush();

        DashboardService.ResumenDia r = service.resumenDia(DIA);

        assertThat(r.dia()).isEqualTo(DIA);
        assertThat(r.ticketsMostrador()).isEqualTo(2);
        assertThat(r.importeMostrador()).isEqualByComparingTo("9.50");
        assertThat(r.facturasEmitidas()).isEqualTo(1);
        assertThat(r.importeFacturado()).isEqualByComparingTo("100.00");
    }

    @Test
    void facturaSinSerieCuentaComoOrdinaria() {
        factura(null, DIA, "12.00", "EMITIDA");
        em.flush();

        DashboardService.ResumenDia r = service.resumenDia(DIA);

        assertThat(r.importeFacturado()).isEqualByComparingTo("12.00");
        assertThat(r.ticketsMostrador()).isZero();
    }

    @Test
    void excluyeBorradoresYAnuladas() {
        factura(TpvService.SERIE_TPV, DIA, "4.00", "ANULADA");
        factura(TpvService.SERIE_TPV, DIA, "4.00", "BORRADOR");
        factura("A", DIA, "200.00", "ANULADA");
        factura("A", DIA, "300.00", "BORRADOR");
        factura("A", DIA, "25.00", "REVISION");
        em.flush();

        DashboardService.ResumenDia r = service.resumenDia(DIA);

        assertThat(r.ticketsMostrador()).isZero();
        assertThat(r.importeMostrador()).isEqualByComparingTo("0");
        // REVISION no es borrador ni anulada: cuenta
        assertThat(r.facturasEmitidas()).isEqualTo(1);
        assertThat(r.importeFacturado()).isEqualByComparingTo("25.00");
    }

    @Test
    void diaSinActividadDevuelveCerosSinNulos() {
        DashboardService.ResumenDia r = service.resumenDia(LocalDate.of(2031, 1, 2));

        assertThat(r.ticketsMostrador()).isZero();
        assertThat(r.importeMostrador()).isNotNull().isEqualByComparingTo("0");
        assertThat(r.importeMostrador().scale()).isEqualTo(2);
        assertThat(r.facturasEmitidas()).isZero();
        assertThat(r.importeFacturado()).isNotNull().isEqualByComparingTo("0");
        assertThat(r.albaranesSinEntregar()).isZero();
        assertThat(r.importeVencido()).isNotNull();
    }

    @Test
    void cuentaAlbaranesDelDiaSinEntregar() {
        albaran(DIA, "PENDIENTE");
        albaran(DIA, null);
        albaran(DIA, "ENTREGADO");
        albaran(DIA, "FACTURADO");
        albaran(DIA, "ANULADO");
        albaran(DIA.plusDays(1), "PENDIENTE");
        em.flush();

        assertThat(service.resumenDia(DIA).albaranesSinEntregar()).isEqualTo(2);
    }

    @Test
    void sumaFacturasVencidasPendientesDeCobro() {
        DashboardService.ResumenDia antes = service.resumenDia(DIA);

        Factura vencida = nueva("A", DIA.minusDays(40), "100.00", "EMITIDA");
        vencida.setFechaVencimiento(DIA.minusDays(10));
        vencida.setPagado(new BigDecimal("30.00"));
        em.persist(vencida);
        Factura marcada = nueva("A", DIA.minusDays(40), "50.00", "VENCIDA");
        marcada.setFechaVencimiento(DIA.minusDays(5));
        em.persist(marcada);
        // No cuentan: pagada, aún no vencida y anulada
        Factura pagada = nueva("A", DIA.minusDays(40), "80.00", "EMITIDA");
        pagada.setFechaVencimiento(DIA.minusDays(10));
        pagada.setPagada(true);
        em.persist(pagada);
        Factura futura = nueva("A", DIA.minusDays(1), "90.00", "EMITIDA");
        futura.setFechaVencimiento(DIA.plusDays(30));
        em.persist(futura);
        Factura anulada = nueva("A", DIA.minusDays(40), "60.00", "ANULADA");
        anulada.setFechaVencimiento(DIA.minusDays(10));
        em.persist(anulada);
        em.flush();

        DashboardService.ResumenDia despues = service.resumenDia(DIA);

        assertThat(despues.facturasVencidas() - antes.facturasVencidas()).isEqualTo(2);
        // Importe pendiente: (100 - 30) + 50
        assertThat(despues.importeVencido().subtract(antes.importeVencido())).isEqualByComparingTo("120.00");
    }
}
