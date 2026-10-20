package alicanteweb.erp.service;

import alicanteweb.erp.entities.Cliente;
import alicanteweb.erp.entities.Pedido;
import alicanteweb.erp.entities.Presupuesto;
import alicanteweb.erp.repository.PedidoRepository;
import alicanteweb.erp.repository.PresupuestoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Numeración PED/PRE por ejercicio contra las consultas reales (H2 en modo MySQL):
 * el siguiente número sale del máximo con el prefijo del año, no de un count(),
 * de modo que un borrado intermedio no reutiliza números y cada año reinicia.
 */
@DataJpaTest
class NumeracionPedidoPresupuestoJpaTest {

    private static final int ANIO = LocalDate.now().getYear();
    private static final String PED = "PED-" + ANIO + "-";
    private static final String PRE = "PRE-" + ANIO + "-";

    @Autowired TestEntityManager em;
    @Autowired PedidoRepository pedidoRepository;
    @Autowired PresupuestoRepository presupuestoRepository;

    PedidoService pedidoService;
    PresupuestoService presupuestoService;
    Cliente cliente;

    @BeforeEach
    void setUp() {
        // La numeración solo usa el repositorio; el resto de colaboradores no intervienen
        pedidoService = new PedidoService(pedidoRepository, null, null);
        presupuestoService = new PresupuestoService(presupuestoRepository, null);
        cliente = new Cliente();
        cliente.setCodigo("CLI-NUM-1");
        cliente.setNombre("Cliente numeración");
        cliente.setActivo(true);
        em.persist(cliente);
    }

    private void pedido(String numero) {
        Pedido p = new Pedido();
        p.setNumero(numero);
        p.setFecha(LocalDate.now());
        p.setEstado("PENDIENTE");
        p.setCliente(cliente);
        em.persist(p);
    }

    private void presupuesto(String numero) {
        Presupuesto p = new Presupuesto();
        p.setNumero(numero);
        p.setFecha(LocalDate.now());
        p.setEstado("BORRADOR");
        p.setTotal(BigDecimal.ZERO);
        p.setCliente(cliente);
        em.persist(p);
    }

    @Test
    void pedidoTrasBorradoIntermedioNoReutilizaNumero() {
        for (int i = 1; i <= 5; i++) pedido(String.format("%s%05d", PED, i));
        em.flush();
        pedidoRepository.findByNumero(PED + "00003").ifPresent(pedidoRepository::delete);
        em.flush();
        em.clear();

        Pedido nuevo = pedidoService.guardar(new Pedido());
        em.flush();

        assertThat(nuevo.getNumero()).isEqualTo(PED + "00006");
        assertThat(pedidoRepository.existsByNumero(PED + "00006")).isTrue();
    }

    @Test
    void pedidoPrimeroDelEjercicioReiniciaEnUno() {
        int anterior = ANIO - 1;
        for (int i = 1; i <= 7; i++) pedido(String.format("PED-%d-%05d", anterior, i));
        em.flush();

        assertThat(pedidoService.guardar(new Pedido()).getNumero()).isEqualTo(PED + "00001");
    }

    @Test
    void pedidoIgnoraSufijosNoNumericos() {
        pedido(PED + "00004");
        pedido(PED + "ABC");
        pedido("PED-" + (ANIO - 1) + "-00099");
        em.flush();

        assertThat(pedidoRepository.maxNumeroConPrefijo(PED)).isEqualTo(PED + "ABC");
        assertThat(pedidoService.guardar(new Pedido()).getNumero()).isEqualTo(PED + "00005");
    }

    @Test
    void presupuestoTrasBorradoYConDatosDelAnoAnterior() {
        presupuesto("PRE-" + (ANIO - 1) + "-00040");
        for (int i = 1; i <= 5; i++) presupuesto(String.format("%s%05d", PRE, i));
        em.flush();
        presupuestoRepository.findByNumero(PRE + "00003").ifPresent(presupuestoRepository::delete);
        em.flush();

        assertThat(presupuestoService.generarNumeroPresupuesto()).isEqualTo(PRE + "00006");
    }

    @Test
    void presupuestoPrimeroDelEjercicioIgnorandoNoNumericos() {
        presupuesto("PRE-" + (ANIO - 1) + "-00040");
        presupuesto(PRE + "BIS");
        em.flush();

        assertThat(presupuestoService.generarNumeroPresupuesto()).isEqualTo(PRE + "00001");
    }
}
