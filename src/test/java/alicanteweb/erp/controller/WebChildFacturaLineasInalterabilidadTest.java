package alicanteweb.erp.controller;

import alicanteweb.erp.ErpWebApplication;
import alicanteweb.erp.entities.Factura;
import alicanteweb.erp.entities.FacturaCompra;
import alicanteweb.erp.entities.FacturaCompraLinea;
import alicanteweb.erp.entities.FacturaLinea;
import alicanteweb.erp.entities.Proveedor;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Inalterabilidad RRSIF a través de la API genérica de hijos
 * (/api/web/children/factura-lineas y factura-compra-lineas):
 * - las líneas de una factura emitida no se crean, editan ni borran (4xx claro);
 * - sobre un borrador sí, y los totales de la cabecera quedan recalculados;
 * - las facturas de compra contabilizadas/pagadas tampoco admiten cambios de líneas.
 */
// ADMIN: este test cubre la inalterabilidad RRSIF, no la autorización granular
// (cubierta en RestApiPermisosTest); ni siquiera un admin altera una emitida.
@SpringBootTest(classes = ErpWebApplication.class)
@AutoConfigureMockMvc
@WithMockUser(roles = "ADMIN")
class WebChildFacturaLineasInalterabilidadTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private PlatformTransactionManager txManager;

    @PersistenceContext
    private EntityManager em;

    private TransactionTemplate tx;

    @BeforeEach
    void setUp() {
        tx = new TransactionTemplate(txManager);
    }

    @AfterEach
    void limpiar() {
        // Bulk JPQL: no dispara los callbacks de inalterabilidad (documentado en Factura),
        // así el fixture EMITIDA también se puede retirar entre tests.
        tx.executeWithoutResult(s -> {
            em.createQuery("delete from FacturaLinea l where l.factura.id in "
                    + "(select f.id from Factura f where f.serie = 'TIN')").executeUpdate();
            em.createQuery("delete from Factura f where f.serie = 'TIN'").executeUpdate();
            em.createQuery("delete from FacturaCompraLinea l where l.facturaCompra.id in "
                    + "(select fc.id from FacturaCompra fc where fc.numero like 'FC-TIN-%')").executeUpdate();
            em.createQuery("delete from FacturaCompra fc where fc.numero like 'FC-TIN-%'").executeUpdate();
            em.createQuery("delete from Proveedor p where p.codigo like 'PROV-TIN-%'").executeUpdate();
        });
    }

    private record Fixture(Long padreId, Long lineaId) {
    }

    /** Factura con una línea (2 x 50,00 €, IVA 10%): base 100,00, IVA 10,00, total 110,00. */
    private Fixture crearFacturaConLinea(String numero, String estadoFinal) {
        return tx.execute(s -> {
            Factura f = new Factura();
            f.setNumero(numero);
            f.setSerie("TIN");
            f.setFecha(LocalDate.of(2026, 7, 1));
            f.setBaseImponible(new BigDecimal("100.00"));
            f.setTotalIva(new BigDecimal("10.00"));
            f.setTotal(new BigDecimal("110.00"));
            FacturaLinea linea = new FacturaLinea();
            linea.setFactura(f);
            linea.setDescripcion("Pan de pueblo");
            linea.setCantidad(new BigDecimal("2.00"));
            linea.setPrecioUnitario(new BigDecimal("50.0000"));
            linea.setIva(new BigDecimal("10.00"));
            f.getFacturaLineas().add(linea);
            em.persist(f); // BORRADOR por defecto
            em.flush();
            if (!"BORRADOR".equals(estadoFinal)) {
                // Transición BORRADOR -> EMITIDA en la misma unidad de trabajo (permitida)
                f.setEstado(estadoFinal);
                em.flush();
            }
            return new Fixture(f.getId(), linea.getId());
        });
    }

    /** Factura de compra con una línea (2 x 50,00 €, IVA 21%): base 100,00, IVA 21,00, total 121,00. */
    private Fixture crearFacturaCompraConLinea(String numero, String estado) {
        return tx.execute(s -> {
            Proveedor prov = new Proveedor();
            prov.setCodigo("PROV-TIN-" + numero);
            prov.setNombre("Harinas Test SL");
            em.persist(prov);
            FacturaCompra fc = new FacturaCompra();
            fc.setNumero(numero);
            fc.setFecha(LocalDate.of(2026, 7, 1));
            fc.setProveedor(prov);
            fc.setBaseImponible(new BigDecimal("100.00"));
            fc.setImporteIva(new BigDecimal("21.00"));
            fc.setTotal(new BigDecimal("121.00"));
            fc.setEstado(estado);
            FacturaCompraLinea linea = new FacturaCompraLinea();
            linea.setFacturaCompra(fc);
            linea.setDescripcion("Harina de trigo");
            linea.setCantidad(new BigDecimal("2.00"));
            linea.setPrecioUnitario(new BigDecimal("50.0000"));
            linea.setTipoIva(new BigDecimal("21.00"));
            linea.setImporte(new BigDecimal("100.00"));
            fc.getLineas().add(linea);
            em.persist(fc);
            em.flush();
            return new Fixture(fc.getId(), linea.getId());
        });
    }

    // =================== FACTURA DE VENTA ===================

    @Test
    void lasLineasDeUnaFacturaEmitidaNoSeCreanNiEditanNiBorran() throws Exception {
        Fixture fx = crearFacturaConLinea("FA-TIN-0001", "EMITIDA");

        mockMvc.perform(post("/api/web/children/factura-lineas/" + fx.padreId())
                        .with(csrf())
                        .contentType("application/json")
                        .content("{\"descripcion\":\"Extra\",\"cantidad\":1,\"precioUnitario\":10,\"iva\":21}"))
                .andExpect(status().isBadRequest());

        mockMvc.perform(put("/api/web/children/factura-lineas/" + fx.padreId() + "/" + fx.lineaId())
                        .with(csrf())
                        .contentType("application/json")
                        .content("{\"cantidad\":99}"))
                .andExpect(status().isBadRequest());

        // DELETE en /api/web/children/** exige ADMIN (SecurityConfig); ni siquiera un admin
        // puede borrar líneas de una factura emitida
        mockMvc.perform(delete("/api/web/children/factura-lineas/" + fx.padreId() + "/" + fx.lineaId())
                        .with(user("admin").roles("ADMIN"))
                        .with(csrf()))
                .andExpect(status().isBadRequest());

        // La línea y los totales quedan exactamente como estaban
        tx.executeWithoutResult(s -> {
            List<FacturaLinea> lineas = em.createQuery(
                            "select l from FacturaLinea l where l.factura.id = :fid", FacturaLinea.class)
                    .setParameter("fid", fx.padreId())
                    .getResultList();
            assertThat(lineas).hasSize(1);
            assertThat(lineas.get(0).getCantidad()).isEqualByComparingTo("2.00");
            Factura f = em.find(Factura.class, fx.padreId());
            assertThat(f.getTotal()).isEqualByComparingTo("110.00");
        });
    }

    @Test
    void sobreUnBorradorSePuedeYLosTotalesDeLaCabeceraSeRecalculan() throws Exception {
        Fixture fx = crearFacturaConLinea("FA-TIN-0002", "BORRADOR");

        // Crear línea nueva: 1 x 10,00 € al 21% -> base 110,00, IVA 12,10, total 122,10
        MvcResult creada = mockMvc.perform(post("/api/web/children/factura-lineas/" + fx.padreId())
                        .with(csrf())
                        .contentType("application/json")
                        .content("{\"descripcion\":\"Extra\",\"cantidad\":1,\"precioUnitario\":10,\"iva\":21}"))
                .andExpect(status().isOk())
                .andReturn();
        long nuevaLineaId = objectMapper.readTree(creada.getResponse().getContentAsString()).get("id").asLong();
        verificarTotalesFactura(fx.padreId(), "110.00", "12.10", "122.10");

        // Editar la línea: 3 x 10,00 € al 21% -> base 130,00, IVA 16,30, total 146,30
        mockMvc.perform(put("/api/web/children/factura-lineas/" + fx.padreId() + "/" + nuevaLineaId)
                        .with(csrf())
                        .contentType("application/json")
                        .content("{\"cantidad\":3}"))
                .andExpect(status().isOk());
        verificarTotalesFactura(fx.padreId(), "130.00", "16.30", "146.30");

        // Borrar la línea (DELETE exige ADMIN): vuelve a base 100,00, IVA 10,00, total 110,00
        mockMvc.perform(delete("/api/web/children/factura-lineas/" + fx.padreId() + "/" + nuevaLineaId)
                        .with(user("admin").roles("ADMIN"))
                        .with(csrf()))
                .andExpect(status().isNoContent());
        verificarTotalesFactura(fx.padreId(), "100.00", "10.00", "110.00");
    }

    private void verificarTotalesFactura(Long facturaId, String base, String iva, String total) {
        tx.executeWithoutResult(s -> {
            Factura f = em.find(Factura.class, facturaId);
            assertThat(f.getBaseImponible()).isEqualByComparingTo(base);
            assertThat(f.getTotalIva()).isEqualByComparingTo(iva);
            assertThat(f.getTotal()).isEqualByComparingTo(total);
        });
    }

    // =================== FACTURA DE COMPRA ===================

    @Test
    void lasLineasDeUnaFacturaDeCompraContabilizadaNoSeModifican() throws Exception {
        Fixture fx = crearFacturaCompraConLinea("FC-TIN-0001", "CONTABILIZADA");

        mockMvc.perform(post("/api/web/children/factura-compra-lineas/" + fx.padreId())
                        .with(csrf())
                        .contentType("application/json")
                        .content("{\"descripcion\":\"Levadura\",\"cantidad\":1,\"precioUnitario\":10,\"tipoIva\":10}"))
                .andExpect(status().isBadRequest());

        mockMvc.perform(put("/api/web/children/factura-compra-lineas/" + fx.padreId() + "/" + fx.lineaId())
                        .with(csrf())
                        .contentType("application/json")
                        .content("{\"cantidad\":99}"))
                .andExpect(status().isBadRequest());

        mockMvc.perform(delete("/api/web/children/factura-compra-lineas/" + fx.padreId() + "/" + fx.lineaId())
                        .with(user("admin").roles("ADMIN"))
                        .with(csrf()))
                .andExpect(status().isBadRequest());

        tx.executeWithoutResult(s -> {
            FacturaCompraLinea linea = em.find(FacturaCompraLinea.class, fx.lineaId());
            assertThat(linea).isNotNull();
            assertThat(linea.getCantidad()).isEqualByComparingTo("2.00");
            FacturaCompra fc = em.find(FacturaCompra.class, fx.padreId());
            assertThat(fc.getTotal()).isEqualByComparingTo("121.00");
        });
    }

    @Test
    void sobreUnaFacturaDeCompraPendienteSePuedeYSeRecalculaElTotal() throws Exception {
        Fixture fx = crearFacturaCompraConLinea("FC-TIN-0002", "PENDIENTE");

        // Nueva línea: 1 x 10,00 € al 10% -> base 110,00, IVA 22,00, total 132,00
        mockMvc.perform(post("/api/web/children/factura-compra-lineas/" + fx.padreId())
                        .with(csrf())
                        .contentType("application/json")
                        .content("{\"descripcion\":\"Levadura\",\"cantidad\":1,\"precioUnitario\":10,\"tipoIva\":10}"))
                .andExpect(status().isOk());

        tx.executeWithoutResult(s -> {
            FacturaCompra fc = em.find(FacturaCompra.class, fx.padreId());
            assertThat(fc.getBaseImponible()).isEqualByComparingTo("110.00");
            assertThat(fc.getImporteIva()).isEqualByComparingTo("22.00");
            assertThat(fc.getTotal()).isEqualByComparingTo("132.00");
        });
    }
}
