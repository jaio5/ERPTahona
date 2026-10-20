package alicanteweb.erp.controller.web;

import alicanteweb.erp.ErpWebApplication;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Smoke test de render tras el rediseño visual: cada listado, formulario nuevo y pantalla
 * de consulta se renderiza de verdad con Thymeleaf (base de datos vacía). Detecta errores de
 * sintaxis o expresiones rotas en las plantillas, que los tests de controlador no ven.
 */
@SpringBootTest(classes = ErpWebApplication.class)
@AutoConfigureMockMvc
class PantallasRenderSmokeTest {

    @Autowired
    private MockMvc mockMvc;

    @ParameterizedTest(name = "{0}")
    @WithMockUser(roles = "ADMIN")
    @ValueSource(strings = {
            // Ventas
            "/web/facturas", "/web/facturas/nueva",
            "/web/albaranes", "/web/albaranes/nuevo",
            "/web/pedidos-venta", "/web/pedidos-venta/nuevo",
            "/web/presupuestos", "/web/presupuestos/nuevo",
            "/web/devoluciones", "/web/devoluciones/nuevo",
            // Clientes y cobros
            "/web/clientes", "/web/clientes/nuevo",
            "/web/tpv", "/web/cartera", "/web/remesas",
            "/web/tesoreria", "/web/tesoreria/conciliacion", "/web/tesoreria/extractos",
            "/web/buscar", "/web/buscar?q=pan",
            // Compras y almacén
            "/web/proveedores", "/web/proveedores/nuevo",
            "/web/pedidos-compra", "/web/pedidos-compra/nuevo",
            "/web/facturas-compra", "/web/facturas-compra/nuevo",
            "/web/recepciones", "/web/recepciones/nuevo",
            "/web/articulos", "/web/articulos/nuevo",
            "/web/almacenes", "/web/almacenes/nuevo",
            "/web/lotes", "/web/lotes/nuevo",
            "/web/mermas", "/web/mermas/nueva",
            "/web/tipos-iva", "/web/tipos-iva/nuevo",
            "/web/importar",
            // Producción y reparto
            "/web/recetas", "/web/recetas/nuevo",
            "/web/horneadas", "/web/horneadas/nuevo",
            "/web/ordenes-produccion", "/web/ordenes-produccion/nuevo",
            "/web/appcc", "/web/appcc/nuevo",
            "/web/rutas", "/web/rutas/nuevo",
            "/web/hojas-ruta", "/web/hojas-ruta/nuevo",
            "/web/vehiculos", "/web/vehiculos/nuevo",
            "/web/planificador", "/web/calendario",
            // Finanzas y administración
            "/web/contabilidad", "/web/contabilidad/mayor", "/web/contabilidad/balance",
            "/web/contabilidad/balance-pgc",
            "/web/fiscal/cumplimiento", "/web/fiscal/libros-iva", "/web/fiscal/modelo303",
            "/web/modelo347", "/web/verifactu",
            "/web/reportes", "/web/reportes/ventas-mes", "/web/reportes/produccion",
            "/web/reportes/reparto", "/web/reportes/trazabilidad", "/web/reportes/rentabilidad",
            "/web/reportes/inventario",
            "/web/empresa", "/web/campos-impresion", "/web/campos-impresion/nuevo",
            "/web/usuarios", "/web/usuarios/nuevo", "/web/auditoria", "/web/backups",
            "/web/dashboard"
    })
    void pantallaRenderiza(String url) throws Exception {
        String html = mockMvc.perform(get(url))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        assertFalse(html.isBlank(), "La pantalla " + url + " no devuelve HTML");
    }
}
