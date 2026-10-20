package alicanteweb.erp.controller.web;

import alicanteweb.erp.ErpWebApplication;
import alicanteweb.erp.config.SecurityConfig;
import alicanteweb.erp.entities.Cliente;
import alicanteweb.erp.entities.Pedido;
import alicanteweb.erp.entities.PedidoLinea;
import alicanteweb.erp.entities.Presupuesto;
import alicanteweb.erp.entities.PresupuestoLinea;
import alicanteweb.erp.entities.Rol;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrlPattern;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Botón "Duplicar" de pedidos de venta y presupuestos: crea una copia editable
 * (PENDIENTE / BORRADOR, fecha de hoy, total recalculado) y abre su edición.
 * Exige ventas:crear; un id inexistente vuelve al listado con un error.
 */
@SpringBootTest(classes = ErpWebApplication.class)
@AutoConfigureMockMvc
class DuplicarDocumentosWebControllerTest {

    private static final String CLIENTE_CODIGO = "CLI-TDUP-1";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PlatformTransactionManager txManager;

    @PersistenceContext
    private EntityManager em;

    private TransactionTemplate tx;
    private Long rolVentasId;   // ventas: ver + crear
    private Long rolLectorId;   // ventas: solo ver
    private Long clienteId;
    private Long pedidoId;
    private Long presupuestoId;

    @BeforeEach
    void setUp() {
        tx = new TransactionTemplate(txManager);
        tx.executeWithoutResult(s -> {
            rolVentasId = crearRol("TESTDUP_VENTAS", Map.of("ventas", Map.of("ver", true, "crear", true)));
            rolLectorId = crearRol("TESTDUP_LECTOR", Map.of("ventas", Map.of("ver", true)));

            Cliente cliente = new Cliente();
            cliente.setCodigo(CLIENTE_CODIGO);
            cliente.setNombre("Cliente Duplicar Test");
            cliente.setActivo(true);
            em.persist(cliente);
            clienteId = cliente.getId();

            Pedido pedido = new Pedido();
            pedido.setNumero("PED-TDUP-00010");
            pedido.setFecha(LocalDate.now().minusDays(15));
            pedido.setEstado("SERVIDO");
            pedido.setCliente(cliente);
            pedido.setTotal(new BigDecimal("1.00")); // desfasado a propósito: la copia lo recalcula
            pedido.getPedidoLineas().add(lineaPedido(pedido, "2", "3.00", "10", "10"));   // 5,40 + 0,54
            pedido.getPedidoLineas().add(lineaPedido(pedido, "1", "4.00", "0", "21"));    // 4,00 + 0,84
            pedido.getPedidoLineas().add(lineaPedido(pedido, "5", "1.00", "0", "0"));     // 5,00
            em.persist(pedido);
            pedidoId = pedido.getId();

            Presupuesto presupuesto = new Presupuesto();
            presupuesto.setNumero("PRE-TDUP-00001");
            presupuesto.setFecha(LocalDate.now().minusDays(15));
            presupuesto.setEstado("ACEPTADO");
            presupuesto.setCliente(cliente);
            presupuesto.setTotal(new BigDecimal("21.78"));
            PresupuestoLinea linea = new PresupuestoLinea();
            linea.setPresupuesto(presupuesto);
            linea.setDescripcion("Tarta");
            linea.setCantidad(new BigDecimal("2"));
            linea.setPrecioUnitario(new BigDecimal("10"));
            linea.setDescuento(new BigDecimal("10"));
            linea.setTipoIva(new BigDecimal("21"));
            linea.setImporte(new BigDecimal("18.00"));
            linea.setOrden(1);
            presupuesto.getLineas().add(linea);
            em.persist(presupuesto);
            presupuestoId = presupuesto.getId();
        });
    }

    @AfterEach
    void limpiar() {
        tx.executeWithoutResult(s -> {
            em.createQuery("delete from PedidoLinea l where l.pedido.id in "
                    + "(select p.id from Pedido p where p.cliente.id = :c)")
                    .setParameter("c", clienteId).executeUpdate();
            em.createQuery("delete from Pedido p where p.cliente.id = :c")
                    .setParameter("c", clienteId).executeUpdate();
            em.createQuery("delete from PresupuestoLinea l where l.presupuesto.id in "
                    + "(select p.id from Presupuesto p where p.cliente.id = :c)")
                    .setParameter("c", clienteId).executeUpdate();
            em.createQuery("delete from Presupuesto p where p.cliente.id = :c")
                    .setParameter("c", clienteId).executeUpdate();
            em.createQuery("delete from Cliente c where c.codigo = :codigo")
                    .setParameter("codigo", CLIENTE_CODIGO).executeUpdate();
            em.createQuery("delete from Rol r where r.nombre like 'TESTDUP_%'").executeUpdate();
        });
    }

    private Long crearRol(String nombre, Map<String, Map<String, Boolean>> permisos) {
        Rol rol = new Rol();
        rol.setNombre(nombre);
        rol.setDescripcion("Rol de test de duplicar");
        rol.setPermisos(permisos);
        rol.setActivo(true);
        rol.setEsSistema(false);
        em.persist(rol);
        return rol.getId();
    }

    private PedidoLinea lineaPedido(Pedido pedido, String cantidad, String precio, String descuento, String iva) {
        PedidoLinea l = new PedidoLinea();
        l.setPedido(pedido);
        l.setDescripcion("Línea " + cantidad);
        l.setCantidad(new BigDecimal(cantidad));
        l.setPrecio(new BigDecimal(precio));
        l.setDescuento(new BigDecimal(descuento));
        l.setIva(new BigDecimal(iva));
        return l;
    }

    /** Usuario autenticado no-admin cuyo rol granular es el indicado. */
    private RequestPostProcessor usuarioConRol(Long rolId) {
        var principal = new SecurityConfig.ErpUserPrincipal(9998L, rolId, "tester-dup", "Tester Duplicar",
                null, true, true, List.of(new SimpleGrantedAuthority("ROLE_USUARIO")));
        return user(principal);
    }

    private long contar(String entidad) {
        return tx.execute(s -> em.createQuery("select count(x) from " + entidad + " x where x.cliente.id = :c", Long.class)
                .setParameter("c", clienteId).getSingleResult());
    }

    private static Long idDesdeEditar(String url, String modulo) {
        String resto = url.substring(("/web/" + modulo + "/").length());
        return Long.valueOf(resto.substring(0, resto.indexOf('/')));
    }

    // ---------------- Pedidos de venta ----------------

    @Test
    void duplicarPedidoCreaCopiaPendienteYAbreSuEdicion() throws Exception {
        MvcResult res = mockMvc.perform(post("/web/pedidos-venta/" + pedidoId + "/duplicar")
                        .with(usuarioConRol(rolVentasId))
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("/web/pedidos-venta/*/editar"))
                .andReturn();

        String exito = (String) res.getFlashMap().get("exito");
        assertThat(exito).startsWith("Duplicado de PED-TDUP-00010: PED-" + LocalDate.now().getYear() + "-");

        Long nuevoId = idDesdeEditar(res.getResponse().getRedirectedUrl(), "pedidos-venta");
        assertThat(nuevoId).isNotEqualTo(pedidoId);
        tx.executeWithoutResult(s -> {
            Pedido copia = em.find(Pedido.class, nuevoId);
            assertThat(copia.getEstado()).isEqualTo("PENDIENTE");
            assertThat(copia.getFecha()).isEqualTo(LocalDate.now());
            assertThat(copia.getCliente().getId()).isEqualTo(clienteId);
            assertThat(copia.getPedidoLineas()).hasSize(3);
            assertThat(copia.getTotal()).isEqualByComparingTo("15.78");
            assertThat(exito).endsWith(copia.getNumero());
            // El original queda intacto
            assertThat(em.find(Pedido.class, pedidoId).getEstado()).isEqualTo("SERVIDO");
        });
    }

    @Test
    void duplicarPedidoSinPermisoCrearSeDeniega() throws Exception {
        mockMvc.perform(post("/web/pedidos-venta/" + pedidoId + "/duplicar")
                        .with(usuarioConRol(rolLectorId))
                        .with(csrf()))
                // Las rutas web convierten el 403 en redirección a la página de acceso denegado
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/web/acceso-denegado"));
        assertThat(contar("Pedido")).isEqualTo(1);
    }

    @Test
    void duplicarPedidoSinCsrfSeRechaza() throws Exception {
        mockMvc.perform(post("/web/pedidos-venta/" + pedidoId + "/duplicar")
                        .with(usuarioConRol(rolVentasId)))
                // Sin token CSRF no llega al controlador: 403 o redirección a acceso denegado
                .andExpect(r -> assertThat(r.getResponse().getStatus() == 403
                        || "/web/acceso-denegado".equals(r.getResponse().getRedirectedUrl())).isTrue());
        assertThat(contar("Pedido")).isEqualTo(1);
    }

    @Test
    void duplicarPedidoInexistenteVuelveAlListadoConError() throws Exception {
        mockMvc.perform(post("/web/pedidos-venta/999999999/duplicar")
                        .with(usuarioConRol(rolVentasId))
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/web/pedidos-venta"))
                .andExpect(flash().attribute("error", "Registro no encontrado"));
    }

    // ---------------- Presupuestos ----------------

    @Test
    void duplicarPresupuestoAprobadoCreaBorradorYAbreSuEdicion() throws Exception {
        MvcResult res = mockMvc.perform(post("/web/presupuestos/" + presupuestoId + "/duplicar")
                        .with(usuarioConRol(rolVentasId))
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("/web/presupuestos/*/editar"))
                .andReturn();

        String exito = (String) res.getFlashMap().get("exito");
        assertThat(exito).startsWith("Duplicado de PRE-TDUP-00001: PRE-" + LocalDate.now().getYear() + "-");

        Long nuevoId = idDesdeEditar(res.getResponse().getRedirectedUrl(), "presupuestos");
        tx.executeWithoutResult(s -> {
            Presupuesto copia = em.find(Presupuesto.class, nuevoId);
            assertThat(copia.getEstado()).isEqualTo("BORRADOR");
            assertThat(copia.getNumero()).isNotEqualTo("PRE-TDUP-00001");
            assertThat(copia.getFecha()).isEqualTo(LocalDate.now());
            assertThat(copia.getFactura()).isNull();
            assertThat(copia.getLineas()).hasSize(1);
            assertThat(copia.getTotal()).isEqualByComparingTo("21.78");
            assertThat(em.find(Presupuesto.class, presupuestoId).getEstado()).isEqualTo("ACEPTADO");
        });
    }

    @Test
    void duplicarPresupuestoSinPermisoCrearSeDeniega() throws Exception {
        mockMvc.perform(post("/web/presupuestos/" + presupuestoId + "/duplicar")
                        .with(usuarioConRol(rolLectorId))
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/web/acceso-denegado"));
        assertThat(contar("Presupuesto")).isEqualTo(1);
    }

    @Test
    void duplicarPresupuestoInexistenteVuelveAlListadoConError() throws Exception {
        mockMvc.perform(post("/web/presupuestos/999999999/duplicar")
                        .with(usuarioConRol(rolVentasId))
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/web/presupuestos"))
                .andExpect(flash().attribute("error", "Registro no encontrado"));
    }
}
