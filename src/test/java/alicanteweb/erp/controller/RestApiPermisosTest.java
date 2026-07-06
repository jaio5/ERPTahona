package alicanteweb.erp.controller;

import alicanteweb.erp.ErpWebApplication;
import alicanteweb.erp.config.SecurityConfig;
import alicanteweb.erp.entities.Cliente;
import alicanteweb.erp.entities.Factura;
import alicanteweb.erp.entities.Rol;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Autorización granular de la API REST (Fase 2): los endpoints exigen el permiso
 * (módulo, acción) del rol del usuario — la UI ya ocultaba los botones, pero el
 * servidor no negaba nada. Un rol sin el permiso recibe 403; con él, la operación
 * procede (y los ADMIN conservan acceso total).
 */
@SpringBootTest(classes = ErpWebApplication.class)
@AutoConfigureMockMvc
class RestApiPermisosTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private PlatformTransactionManager txManager;

    @PersistenceContext
    private EntityManager em;

    private TransactionTemplate tx;
    private Long rolVentasId;      // ventas: ver + crear + editar
    private Long rolLectorId;      // ventas: solo ver
    private Long rolRepartoId;     // reparto: ver + editar (sin ventas)
    private Long rolReportesId;    // reportes: solo ver (sin almacen)
    private Long rolAlmacenId;     // almacen: ver + editar
    private Long clienteId;
    private Long facturaBorradorId;
    private final List<Long> facturasCreadas = new ArrayList<>();

    @BeforeEach
    void setUp() {
        tx = new TransactionTemplate(txManager);
        tx.executeWithoutResult(s -> {
            rolVentasId = crearRol("TESTPERM_VENTAS",
                    Map.of("ventas", Map.of("ver", true, "crear", true, "editar", true)));
            rolLectorId = crearRol("TESTPERM_LECTOR",
                    Map.of("ventas", Map.of("ver", true)));
            rolRepartoId = crearRol("TESTPERM_REPARTO",
                    Map.of("reparto", Map.of("ver", true, "editar", true)));
            rolReportesId = crearRol("TESTPERM_REPORTES",
                    Map.of("reportes", Map.of("ver", true)));
            rolAlmacenId = crearRol("TESTPERM_ALMACEN",
                    Map.of("almacen", Map.of("ver", true, "editar", true)));

            Cliente cliente = new Cliente();
            cliente.setCodigo("CLI-TPM-1");
            cliente.setNombre("Cliente Permisos Test");
            cliente.setActivo(true);
            em.persist(cliente);
            clienteId = cliente.getId();

            Factura factura = new Factura();
            factura.setNumero("FA-TPM-1");
            factura.setSerie("TPM");
            factura.setFecha(LocalDate.of(2026, 7, 1));
            factura.setTotal(BigDecimal.ZERO);
            em.persist(factura); // BORRADOR por defecto
            facturaBorradorId = factura.getId();
        });
    }

    @AfterEach
    void limpiar() {
        tx.executeWithoutResult(s -> {
            List<Long> ids = new ArrayList<>(facturasCreadas);
            ids.add(facturaBorradorId);
            em.createQuery("delete from FacturaLinea l where l.factura.id in :ids")
                    .setParameter("ids", ids).executeUpdate();
            em.createQuery("delete from Factura f where f.id in :ids")
                    .setParameter("ids", ids).executeUpdate();
            em.createQuery("delete from Cliente c where c.codigo = 'CLI-TPM-1'").executeUpdate();
            em.createQuery("delete from Rol r where r.nombre like 'TESTPERM_%'").executeUpdate();
        });
        facturasCreadas.clear();
    }

    private Long crearRol(String nombre, Map<String, Map<String, Boolean>> permisos) {
        Rol rol = new Rol();
        rol.setNombre(nombre);
        rol.setDescripcion("Rol de test de permisos");
        rol.setPermisos(permisos);
        rol.setActivo(true);
        rol.setEsSistema(false);
        em.persist(rol);
        return rol.getId();
    }

    /** Usuario autenticado no-admin cuyo rol granular es el indicado. */
    private RequestPostProcessor usuarioConRol(Long rolId) {
        return usuarioConRol(rolId, "ROLE_USUARIO");
    }

    private RequestPostProcessor usuarioConRol(Long rolId, String authority) {
        var principal = new SecurityConfig.ErpUserPrincipal(9999L, rolId, "tester", "Tester",
                null, true, true, List.of(new SimpleGrantedAuthority(authority)));
        return user(principal);
    }

    @Test
    void sinPermisoCrearLaFacturaSeRechaza() throws Exception {
        mockMvc.perform(post("/api/web/documentos/factura")
                        .with(usuarioConRol(rolLectorId))
                        .with(csrf())
                        .contentType("application/json")
                        .content("{\"clienteId\":" + clienteId + "}"))
                .andExpect(status().isForbidden());
    }

    @Test
    void conPermisoCrearLaFacturaSeCrea() throws Exception {
        MvcResult res = mockMvc.perform(post("/api/web/documentos/factura")
                        .with(usuarioConRol(rolVentasId))
                        .with(csrf())
                        .contentType("application/json")
                        .content("{\"clienteId\":" + clienteId + "}"))
                .andExpect(status().isCreated())
                .andReturn();
        facturasCreadas.add(objectMapper.readTree(res.getResponse().getContentAsString()).get("id").asLong());
    }

    @Test
    void emitirExigeVentasEditar() throws Exception {
        mockMvc.perform(post("/api/web/facturas/" + facturaBorradorId + "/emitir")
                        .with(usuarioConRol(rolLectorId))
                        .with(csrf()))
                .andExpect(status().isForbidden());

        // Con el permiso, la petición pasa la autorización y falla por estado
        // (BORRADOR, no REVISION): 400, no 403.
        mockMvc.perform(post("/api/web/facturas/" + facturaBorradorId + "/emitir")
                        .with(usuarioConRol(rolVentasId))
                        .with(csrf()))
                .andExpect(status().isBadRequest());
    }

    @Test
    void laApiGenericaRespetaElModuloDelRol() throws Exception {
        // ventas:ver permite listar facturas...
        mockMvc.perform(get("/api/web/entities/facturas").with(usuarioConRol(rolLectorId)))
                .andExpect(status().isOk());
        // ...pero no otros módulos (proveedores) ni a un rol sin el módulo
        mockMvc.perform(get("/api/web/entities/proveedores").with(usuarioConRol(rolLectorId)))
                .andExpect(status().isForbidden());
        mockMvc.perform(get("/api/web/entities/facturas").with(usuarioConRol(rolRepartoId)))
                .andExpect(status().isForbidden());
    }

    @Test
    void lasLineasHijasExigenEditarDelModuloDelPadre() throws Exception {
        String linea = "{\"descripcion\":\"Pan\",\"cantidad\":1,\"precioUnitario\":10,\"iva\":10}";
        mockMvc.perform(post("/api/web/children/factura-lineas/" + facturaBorradorId)
                        .with(usuarioConRol(rolLectorId))
                        .with(csrf())
                        .contentType("application/json")
                        .content(linea))
                .andExpect(status().isForbidden());

        mockMvc.perform(post("/api/web/children/factura-lineas/" + facturaBorradorId)
                        .with(usuarioConRol(rolVentasId))
                        .with(csrf())
                        .contentType("application/json")
                        .content(linea))
                .andExpect(status().isOk());
    }

    @Test
    void ajustarInventarioExigeAlmacenEditarNoBastaReportesVer() throws Exception {
        // Un usuario raso no pasa ni la regla de URL (requiere CONTABLE/ADMIN)
        mockMvc.perform(post("/api/web/reportes/inventario/ajustar")
                        .with(usuarioConRol(rolAlmacenId))
                        .with(csrf())
                        .param("articuloId", "999999")
                        .param("cantidadNueva", "5"))
                .andExpect(status().isForbidden());

        // Un CONTABLE cuyo rol granular solo tiene reportes:ver tampoco: ajustar
        // stock es escritura de almacén, no lectura de informes
        mockMvc.perform(post("/api/web/reportes/inventario/ajustar")
                        .with(usuarioConRol(rolReportesId, "ROLE_CONTABLE"))
                        .with(csrf())
                        .param("articuloId", "999999")
                        .param("cantidadNueva", "5"))
                .andExpect(status().isForbidden());

        // Con CONTABLE + almacen:editar la autorización pasa y la petición
        // falla por artículo inexistente: 400, no 403.
        mockMvc.perform(post("/api/web/reportes/inventario/ajustar")
                        .with(usuarioConRol(rolAlmacenId, "ROLE_CONTABLE"))
                        .with(csrf())
                        .param("articuloId", "999999")
                        .param("cantidadNueva", "5"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void laApiMovilExigeReparto() throws Exception {
        mockMvc.perform(get("/api/movil/mi-ruta").with(usuarioConRol(rolRepartoId)))
                .andExpect(status().isOk());
        mockMvc.perform(get("/api/movil/mi-ruta").with(usuarioConRol(rolLectorId)))
                .andExpect(status().isForbidden());
    }

    @Test
    void losAdminConservanAccesoTotal() throws Exception {
        mockMvc.perform(get("/api/web/entities/proveedores").with(user("admin").roles("ADMIN")))
                .andExpect(status().isOk());
        mockMvc.perform(get("/api/web/entities/facturas").with(user("admin").roles("ADMIN")))
                .andExpect(status().isOk());
    }
}
