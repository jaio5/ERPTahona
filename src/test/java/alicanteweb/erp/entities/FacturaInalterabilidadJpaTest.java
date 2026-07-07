package alicanteweb.erp.entities;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Verifica el guard de inalterabilidad RRSIF contra Hibernate real (H2):
 * la emisión y el cobro funcionan, y la manipulación o el borrado de una
 * factura emitida fallan en el flush, vengan de donde vengan.
 */
@DataJpaTest
class FacturaInalterabilidadJpaTest {

    @Autowired
    TestEntityManager em;

    private Long crearFacturaEmitida(String numero) {
        Factura factura = new Factura();
        factura.setNumero(numero);
        factura.setSerie("IT");
        factura.setFecha(LocalDate.of(2026, 7, 1));
        factura.setTotal(new BigDecimal("100.00"));
        FacturaLinea linea = new FacturaLinea();
        linea.setFactura(factura);
        linea.setDescripcion("Pan de pueblo");
        linea.setCantidad(new BigDecimal("2.00"));
        linea.setPrecioUnitario(new BigDecimal("50.0000"));
        factura.getFacturaLineas().add(linea);
        em.persist(factura); // BORRADOR
        em.flush();

        // Emisión: transición BORRADOR -> EMITIDA (misma unidad de trabajo, permitida)
        factura.setEstado("EMITIDA");
        factura.setVerifactuHash("hash-de-prueba");
        em.flush();
        em.clear();
        return factura.getId();
    }

    @Test
    void laEmisionYElCobroFuncionanYElContenidoFiscalQuedaBloqueado() {
        Long id = crearFacturaEmitida("FA-IT-0001");

        // Cobro sobre factura emitida: permitido
        Factura cobrada = em.find(Factura.class, id);
        cobrada.setPagado(new BigDecimal("100.00"));
        cobrada.setPagada(true);
        em.flush();
        em.clear();

        assertThat(em.find(Factura.class, id).isPagada()).isTrue();
        em.clear();

        // Cambiar el total de una emitida: bloqueado en el flush
        Factura manipulada = em.find(Factura.class, id);
        manipulada.setTotal(new BigDecimal("999.99"));
        assertThatThrownBy(() -> em.flush())
                .hasStackTraceContaining("inalterable");
        em.clear();
    }

    @Test
    void elBorradoDeUnaFacturaEmitidaSeBloquea() {
        Long id = crearFacturaEmitida("FA-IT-0002");
        Factura factura = em.find(Factura.class, id);
        assertThatThrownBy(() -> {
            em.remove(factura);
            em.flush();
        }).hasStackTraceContaining("no puede borrarse");
        em.clear();
    }

    @Test
    void lasLineasDeUnaFacturaEmitidaSonInalterables() {
        Long id = crearFacturaEmitida("FA-IT-0003");
        Factura factura = em.find(Factura.class, id);
        FacturaLinea linea = factura.getFacturaLineas().iterator().next();
        linea.setCantidad(new BigDecimal("99.00"));
        assertThatThrownBy(() -> em.flush())
                .hasStackTraceContaining("inalterables");
        em.clear();
    }
}
