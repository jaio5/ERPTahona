package alicanteweb.erp.controller.web;

import alicanteweb.erp.ErpWebApplication;
import alicanteweb.erp.entities.*;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.support.TransactionTemplate;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

/**
 * Smoke test de render con datos: crea un juego mínimo de documentos (en transacciones
 * confirmadas, como en producción con open-in-view desactivado) y renderiza listados, fichas y
 * formularios de edición. Así se evalúan las filas de las tablas y las expresiones de las fichas,
 * que con la base de datos vacía no llegan a ejecutarse. Al terminar borra lo creado.
 */
@SpringBootTest(classes = ErpWebApplication.class)
@AutoConfigureMockMvc
class FichasRenderSmokeTest {

    private static final BigDecimal DIEZ = new BigDecimal("10.00");
    private static final BigDecimal MIL = new BigDecimal("1234.50");

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private EntityManager em;

    @Autowired
    private TransactionTemplate tx;

    /** Entidades creadas, en orden de alta; se borran en orden inverso. */
    private final List<Object> creadas = new ArrayList<>();

    /** Factura emitida: la entidad impide borrarla, así que se elimina por SQL al final. */
    private Long facturaEmitidaId;

    private <T> T alta(T entidad) {
        em.persist(entidad);
        creadas.add(entidad);
        return entidad;
    }

    @AfterEach
    void limpiar() {
        if (facturaEmitidaId != null) {
            tx.executeWithoutResult(s -> em.createNativeQuery("DELETE FROM facturas WHERE id = :id")
                    .setParameter("id", facturaEmitidaId).executeUpdate());
            facturaEmitidaId = null;
        }
        for (int i = creadas.size() - 1; i >= 0; i--) {
            Object e = creadas.get(i);
            Object id = em.getEntityManagerFactory().getPersistenceUnitUtil().getIdentifier(e);
            tx.executeWithoutResult(s -> {
                Object gestionada = em.find(e.getClass(), id);
                if (gestionada != null) {
                    em.remove(gestionada);
                }
            });
        }
        creadas.clear();
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void fichasListadosYEdicionesRenderizanConDatos() throws Exception {
        List<String> urls = tx.execute(s -> sembrar());

        List<String> fallos = new ArrayList<>();
        for (String url : urls) {
            try {
                MvcResult r = mockMvc.perform(get(url)).andReturn();
                int st = r.getResponse().getStatus();
                if (st != 200) {
                    fallos.add(url + " -> HTTP " + st);
                }
            } catch (Exception ex) {
                Throwable raiz = ex;
                while (raiz.getCause() != null) {
                    raiz = raiz.getCause();
                }
                fallos.add(url + " -> " + raiz.getClass().getSimpleName() + ": " + raiz.getMessage());
            }
        }
        assertTrue(fallos.isEmpty(), "Pantallas que no renderizan:\n" + String.join("\n", fallos));
    }

    private List<String> sembrar() {
        LocalDate hoy = LocalDate.now();

        Cliente cli = new Cliente();
        cli.setCodigo("SMK-C1");
        cli.setNombre("Cliente de prueba");
        cli.setTelefono("600000000");
        cli.setPoblacion("Alicante");
        cli.setEmail("cliente@example.com");
        cli.setNotas("Línea 1\nLínea 2");
        cli.setIban("ES9121000418450200051332");
        alta(cli);

        Articulo art = new Articulo();
        art.setCodigo("SMK-A1");
        art.setNombre("Pan de prueba");
        art.setDescripcion("Pan de prueba");
        art.setIva(DIEZ);
        art.setPvp(new BigDecimal("1.50"));
        art.setCoste(new BigDecimal("0.40"));
        art.setStock(MIL);
        art.setStockMinimo(DIEZ);
        art.setAlergenos("Gluten, Sésamo");
        alta(art);

        Almacen alm = new Almacen();
        alm.setCodigo("SMK-ALM");
        alm.setNombre("Obrador");
        alm.setCapacidad(MIL);
        alm.setDisponible(DIEZ);
        alta(alm);

        Proveedor prov = new Proveedor();
        prov.setCodigo("SMK-P1");
        prov.setNombre("Harinas de prueba");
        prov.setEmail("prov@example.com");
        alta(prov);

        // Ventas
        Factura fac = new Factura();
        fac.setNumero("SMK-F1");
        fac.setVersion(0L);
        fac.setFecha(hoy);
        fac.setFechaVencimiento(hoy.plusDays(30));
        fac.setCliente(cli);
        fac.setTotal(MIL);
        fac.setBaseImponible(MIL);
        fac.setTotalIva(DIEZ);
        fac.setMedioCobro("TRANSFERENCIA");
        alta(fac);
        FacturaLinea fl = new FacturaLinea();
        fl.setFactura(fac);
        fl.setArticulo(art);
        fl.setDescripcion("Pan");
        fl.setCantidad(MIL);
        fl.setPrecioUnitario(DIEZ);
        fl.setPrecio(DIEZ);
        fl.setDescuento(BigDecimal.ONE);
        fl.setIva(DIEZ);
        fl.setTotal(MIL);
        alta(fl);

        Factura emitida = new Factura();
        emitida.setNumero("SMK-F2");
        emitida.setVersion(0L);
        emitida.setFecha(hoy);
        emitida.setCliente(cli);
        emitida.setTotal(MIL);
        emitida.setEstado("EMITIDA");
        em.persist(emitida);
        facturaEmitidaId = emitida.getId();

        AlbaranVenta alb = new AlbaranVenta();
        alb.setNumero("SMK-AL1");
        alb.setFecha(hoy);
        alb.setEstado("PENDIENTE");
        alb.setCliente(cli);
        alb.setAlmacen(alm);
        alb.setNumeroLote("L-1");
        alb.setObservaciones("Dejar en la puerta");
        alb.setTotal(MIL);
        alta(alb);
        AlbaranVentaLinea al = new AlbaranVentaLinea();
        al.setAlbaran(alb);
        al.setArticulo(art);
        al.setDescripcion("Pan");
        al.setCantidad(MIL);
        al.setPrecio(DIEZ);
        al.setDescuento(BigDecimal.ONE);
        al.setIva(DIEZ);
        alta(al);

        Pedido ped = new Pedido();
        ped.setNumero("SMK-PV1");
        ped.setFecha(hoy);
        ped.setCliente(cli);
        ped.setEstado("PENDIENTE");
        ped.setTotal(MIL);
        ped.setObservaciones("Urgente");
        alta(ped);
        PedidoLinea pl = new PedidoLinea();
        pl.setPedido(ped);
        pl.setArticulo(art);
        pl.setDescripcion("Pan");
        pl.setCantidad(MIL);
        pl.setPrecio(DIEZ);
        alta(pl);

        Presupuesto pre = new Presupuesto();
        pre.setNumero("SMK-PR1");
        pre.setVersion(0L);
        pre.setFecha(hoy);
        pre.setFechaValidez(hoy.plusDays(15));
        pre.setCliente(cli);
        pre.setTotal(MIL);
        pre.setEstado("ENVIADO");
        alta(pre);
        PresupuestoLinea prl = new PresupuestoLinea();
        prl.setPresupuesto(pre);
        prl.setArticulo(art);
        prl.setDescripcion("Pan");
        prl.setCantidad(MIL);
        prl.setPrecioUnitario(DIEZ);
        prl.setDescuento(BigDecimal.ONE);
        prl.setTipoIva(DIEZ);
        prl.setImporte(MIL);
        alta(prl);

        Devolucion dev = new Devolucion();
        dev.setNumero("SMK-D1");
        dev.setFecha(hoy);
        dev.setCliente(cli);
        dev.setAlbaran(alb);
        dev.setFactura(fac);
        dev.setMotivo("Producto dañado");
        dev.setImporteTotal(DIEZ);
        alta(dev);
        DevolucionLinea dl = new DevolucionLinea();
        dl.setDevolucion(dev);
        dl.setArticulo(art);
        dl.setCantidad(BigDecimal.ONE);
        dl.setPrecioUnitario(DIEZ);
        dl.setImporte(DIEZ);
        alta(dl);

        // Compras y almacén
        PedidoCompra pc = new PedidoCompra();
        pc.setNumero("SMK-PC1");
        pc.setFecha(hoy);
        pc.setProveedor(prov);
        pc.setTotal(MIL);
        pc.setEstado("RECIBIDO_PARCIAL");
        alta(pc);
        PedidoCompraLinea pcl = new PedidoCompraLinea();
        pcl.setPedidoCompra(pc);
        pcl.setArticulo(art);
        pcl.setDescripcion("Harina");
        pcl.setCantidad(MIL);
        pcl.setCantidadRecibida(DIEZ);
        pcl.setPrecioUnitario(DIEZ);
        pcl.setTipoIva(DIEZ);
        pcl.setImporte(MIL);
        alta(pcl);

        FacturaCompra fc = new FacturaCompra();
        fc.setNumero("SMK-FC1");
        fc.setFecha(hoy);
        fc.setFechaVencimiento(hoy.minusDays(1));
        fc.setProveedor(prov);
        fc.setBaseImponible(MIL);
        fc.setTipoIva(DIEZ);
        fc.setImporteIva(DIEZ);
        fc.setTotal(MIL);
        fc.setEstado("CONTABILIZADA");
        alta(fc);

        Recepcion rec = new Recepcion();
        rec.setNumero("SMK-R1");
        rec.setFecha(hoy);
        rec.setProveedor(prov);
        rec.setAlmacen(alm);
        rec.setPedidoCompra(pc);
        alta(rec);
        RecepcionLinea rl = new RecepcionLinea();
        rl.setRecepcion(rec);
        rl.setArticulo(art);
        rl.setCantidadPedida(MIL);
        rl.setCantidadRecibida(DIEZ);
        rl.setCodigoLote("LR-1");
        rl.setFechaCaducidad(hoy.plusDays(5));
        rl.setPrecioUnitario(DIEZ);
        alta(rl);

        Lote lote = new Lote();
        lote.setCodigo("SMK-L1");
        lote.setArticulo(art);
        lote.setFechaProduccion(hoy.minusDays(10));
        lote.setFechaCaducidad(hoy.minusDays(1));
        lote.setCantidadInicial(MIL);
        lote.setCantidadActual(DIEZ);
        lote.setAlmacen(alm);
        alta(lote);
        Lote lote2 = new Lote();
        lote2.setCodigo("SMK-L2");
        lote2.setArticulo(art);
        lote2.setFechaProduccion(hoy);
        lote2.setFechaCaducidad(hoy.plusDays(3));
        lote2.setCantidadInicial(DIEZ);
        lote2.setCantidadActual(DIEZ);
        alta(lote2);
        LoteInsumo li = new LoteInsumo();
        li.setLoteProducto(lote2);
        li.setLoteInsumo(lote);
        li.setCantidadUsada(BigDecimal.ONE);
        alta(li);

        Merma mer = new Merma();
        mer.setFecha(hoy);
        mer.setArticulo(art);
        mer.setCantidad(MIL);
        mer.setTipo("CADUCIDAD");
        mer.setMotivo("Caducado");
        alta(mer);

        // Producción y reparto
        Receta rct = new Receta();
        rct.setCodigo("SMK-RC1");
        rct.setNombre("Barra");
        rct.setTiempoPreparacion(30);
        rct.setTiempoHorneado(20);
        rct.setTemperaturaHorneado(220);
        rct.setRendimientoCantidad(MIL);
        rct.setUnidadRendimiento("ud");
        rct.setArticuloResultante(art);
        alta(rct);
        RecetaIngrediente ri = new RecetaIngrediente();
        ri.setReceta(rct);
        ri.setArticulo(art);
        ri.setCantidad(new BigDecimal("0.125"));
        ri.setUnidad("kg");
        alta(ri);

        OrdenProduccion op = new OrdenProduccion();
        op.setNumero("SMK-OP1");
        op.setFecha(hoy);
        op.setReceta(rct);
        op.setArticulo(art);
        op.setAlmacen(alm);
        op.setCantidadPlanificada(MIL);
        op.setCantidadProducida(DIEZ);
        op.setMerma(BigDecimal.ONE);
        op.setEstado("EN_PROCESO");
        op.setFechaInicio(LocalDateTime.now());
        alta(op);

        Horneada hor = new Horneada();
        hor.setFecha(hoy);
        hor.setOrdenProduccion(op);
        hor.setHoraInicio(LocalTime.of(5, 0));
        hor.setHoraFin(LocalTime.of(5, 40));
        hor.setTemperaturaInicial(220);
        hor.setTemperaturaFinal(200);
        hor.setTipoHorneada("PAN");
        hor.setCantidadProducida(MIL);
        hor.setMerma(BigDecimal.ONE);
        hor.setResultado("OK");
        hor.setCosteManoObra(DIEZ);
        hor.setCosteEnergia(DIEZ);
        hor.setCosteMateriales(DIEZ);
        alta(hor);

        AppccControl ap = new AppccControl();
        ap.setFecha(hoy);
        ap.setHora(LocalDateTime.now());
        ap.setPuntoCritico("CAMARA");
        ap.setTemperatura(new BigDecimal("4.50"));
        ap.setLimiteCritico(new BigDecimal("5.00"));
        ap.setResultado("NOK");
        ap.setLote(lote);
        alta(ap);

        Vehiculo veh = new Vehiculo();
        veh.setMatricula("1234ABC");
        veh.setMarca("Furgo");
        veh.setCapacidadKg(MIL);
        veh.setCapacidadVolumen(DIEZ);
        veh.setConsumoMedio(new BigDecimal("8.5"));
        alta(veh);

        RutaReparto ruta = new RutaReparto();
        ruta.setCodigo("SMK-RT1");
        ruta.setNombre("Ruta centro");
        ruta.setVehiculo(veh);
        ruta.setDistanciaTotalKm(new BigDecimal("42.5"));
        ruta.setTiempoEstimadoMinutos(90);
        alta(ruta);
        RutaParada par = new RutaParada();
        par.setRuta(ruta);
        par.setCliente(cli);
        par.setOrden(1);
        par.setHoraEstimada(LocalTime.of(7, 30));
        alta(par);

        HojaRuta hr = new HojaRuta();
        hr.setRuta(ruta);
        hr.setFecha(hoy);
        hr.setVehiculo(veh);
        hr.setEstado("EN_CURSO");
        hr.setHoraSalida(LocalDateTime.now());
        hr.setIncidencias("Calle cortada");
        alta(hr);
        HojaRutaEntrega ent = new HojaRutaEntrega();
        ent.setHojaRuta(hr);
        ent.setCliente(cli);
        ent.setAlbaran(alb);
        ent.setOrden(1);
        ent.setEntregado(true);
        ent.setImporteCobrado(DIEZ);
        alta(ent);

        // Tesorería
        Banco banco = new Banco();
        banco.setNombre("Banco de prueba");
        alta(banco);
        MovimientoBanco mb = new MovimientoBanco();
        mb.setBanco(banco);
        mb.setFecha(hoy);
        mb.setTipo("INGRESO");
        mb.setConcepto("Transferencia <b>cliente</b>");
        mb.setImporte(MIL);
        mb.setConciliado(false);
        alta(mb);

        em.flush();

        return List.of(
                // Ventas
                "/web/facturas", "/web/facturas/" + fac.getId(), "/web/facturas/" + fac.getId() + "/editar",
                "/web/facturas/" + emitida.getId(), "/web/facturas/" + emitida.getId() + "/rectificar",
                "/web/albaranes", "/web/albaranes/" + alb.getId(), "/web/albaranes/" + alb.getId() + "/editar",
                "/web/pedidos-venta", "/web/pedidos-venta/" + ped.getId(),
                "/web/pedidos-venta/" + ped.getId() + "/editar",
                "/web/presupuestos", "/web/presupuestos/" + pre.getId(),
                "/web/presupuestos/" + pre.getId() + "/editar",
                "/web/devoluciones", "/web/devoluciones/" + dev.getId(),
                "/web/devoluciones/" + dev.getId() + "/editar",
                // Clientes y cobros
                "/web/clientes", "/web/clientes/" + cli.getId(), "/web/clientes/" + cli.getId() + "/editar",
                "/web/tpv", "/web/cartera", "/web/remesas",
                "/web/tesoreria", "/web/tesoreria/conciliacion", "/web/tesoreria/extractos",
                "/web/buscar?q=prueba",
                // Compras y almacén
                "/web/proveedores", "/web/proveedores/" + prov.getId(),
                "/web/proveedores/" + prov.getId() + "/editar",
                "/web/pedidos-compra", "/web/pedidos-compra/" + pc.getId(),
                "/web/facturas-compra", "/web/facturas-compra/" + fc.getId(),
                "/web/recepciones", "/web/recepciones/" + rec.getId(),
                "/web/recepciones/" + rec.getId() + "/editar",
                "/web/articulos", "/web/articulos/" + art.getId(), "/web/articulos/" + art.getId() + "/editar",
                "/web/almacenes", "/web/almacenes/" + alm.getId(), "/web/almacenes/" + alm.getId() + "/editar",
                "/web/lotes", "/web/lotes/" + lote.getId(), "/web/lotes/" + lote2.getId(),
                "/web/lotes/" + lote.getId() + "/editar",
                "/web/mermas", "/web/mermas/" + mer.getId(),
                // Producción y reparto
                "/web/recetas", "/web/recetas/" + rct.getId(), "/web/recetas/" + rct.getId() + "/editar",
                "/web/horneadas", "/web/horneadas/" + hor.getId(),
                "/web/ordenes-produccion", "/web/ordenes-produccion/" + op.getId(),
                "/web/ordenes-produccion/" + op.getId() + "/editar",
                "/web/appcc", "/web/appcc/" + ap.getId(),
                "/web/rutas", "/web/rutas/" + ruta.getId(), "/web/rutas/" + ruta.getId() + "/editar",
                "/web/hojas-ruta", "/web/hojas-ruta/" + hr.getId(),
                "/web/vehiculos", "/web/vehiculos/" + veh.getId(), "/web/vehiculos/" + veh.getId() + "/editar",
                "/web/planificador", "/web/calendario",
                // Finanzas
                "/web/contabilidad", "/web/fiscal/libros-iva", "/web/fiscal/modelo303", "/web/modelo347",
                "/web/verifactu", "/web/reportes/ventas-mes", "/web/reportes/produccion",
                "/web/reportes/reparto", "/web/reportes/trazabilidad", "/web/reportes/rentabilidad",
                "/web/reportes/inventario", "/web/dashboard"
        );
    }
}
