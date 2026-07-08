package alicanteweb.erp.controller.web;

import alicanteweb.erp.entities.*;
import alicanteweb.erp.exception.ErpException;
import alicanteweb.erp.service.*;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.web.servlet.mvc.support.RedirectAttributesModelMap;

import java.io.File;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class CriticalWebControllersCoverageTest {

    @Test
    void facturaCubreListadoFormulariosCicloYDescarga() throws Exception {
        FacturaService facturas = mock(FacturaService.class);
        ClienteService clientes = mock(ClienteService.class);
        ArticuloService articulos = mock(ArticuloService.class);
        ImpresionService impresion = mock(ImpresionService.class);
        AuditoriaService auditoria = mock(AuditoriaService.class);
        UsuarioService usuarios = mock(UsuarioService.class);
        DocumentoService documentos = mock(DocumentoService.class);
        Factura factura = factura(1L, "F-1", "BORRADOR");
        FacturaLinea linea = new FacturaLinea();
        Articulo articulo = new Articulo();
        articulo.setId(3L);
        linea.setArticulo(articulo);
        linea.setCantidad(BigDecimal.ONE);
        linea.setPrecioUnitario(new BigDecimal("2"));
        linea.setIva(new BigDecimal("4"));
        linea.setDescuento(BigDecimal.ZERO);
        factura.getFacturaLineas().add(linea);

        when(facturas.buscarPaginado(any(), any(), any(), any(), any())).thenReturn(new PageImpl<>(List.of(factura)));
        when(facturas.findByIdParaPdf(1L)).thenReturn(Optional.of(factura));
        when(clientes.findAll()).thenReturn(List.of());
        when(articulos.findAll()).thenReturn(List.of(articulo));
        when(documentos.guardarFactura(any(), any())).thenReturn(factura);
        Factura rectificativa = factura(2L, "R-1", "BORRADOR");
        when(facturas.crearRectificativa(eq(1L), anyString(), anyString(), any())).thenReturn(rectificativa);
        File pdf = Files.createTempFile("factura-test", ".pdf").toFile();
        Files.writeString(pdf.toPath(), "PDF");
        when(impresion.generarFacturaPdf(factura)).thenReturn(pdf);

        FacturaWebController controller = new FacturaWebController(
                facturas, clientes, articulos, impresion, auditoria, usuarios, documentos,
                mock(EmailService.class), mock(FacturaeService.class), mock(TipoImpositivoService.class));

        assertEquals("layout", controller.listado(new ExtendedModelMap(), null, null, null, null, 0, 25, "fecha", "desc"));
        assertEquals("layout", controller.formularioNueva(new ExtendedModelMap()));
        assertEquals("layout", controller.formularioEditar(1L, new ExtendedModelMap(), new RedirectAttributesModelMap()));
        assertEquals("layout", controller.ver(1L, new ExtendedModelMap(), new RedirectAttributesModelMap()));
        assertEquals("redirect:/web/facturas", controller.ver(9L, new ExtendedModelMap(), new RedirectAttributesModelMap()));

        MockHttpServletRequest request = lineRequest();
        RedirectAttributesModelMap ra = new RedirectAttributesModelMap();
        assertEquals("redirect:/web/facturas/1",
                controller.guardar(null, 4L, "2026-06-19", "EFECTIVO", null, "Obs", null, null, null, null, request, ra));
        verify(documentos).guardarFactura(isNull(), any());

        factura.setEstado("EMITIDA");
        assertEquals("layout", controller.formularioRectificativa(1L, new ExtendedModelMap(), new RedirectAttributesModelMap()));
        factura.setEstado("BORRADOR");
        assertEquals("redirect:/web/facturas/1", controller.formularioRectificativa(1L, new ExtendedModelMap(), new RedirectAttributesModelMap()));
        factura.setEstado("EMITIDA");

        assertEquals("redirect:/web/facturas/2",
                controller.crearRectificativa(1L, "Error", "SUSTITUCION", "2026-06-19", ra));
        assertEquals("redirect:/web/facturas/1", controller.emitir(1L, ra));
        assertEquals("redirect:/web/facturas/1", controller.anular(1L, ra));
        verify(facturas).aprobarYEmitir(1L);
        verify(facturas).anularFactura(1L, "Anulada desde web");

        assertEquals("PDF", new String(controller.descargarPdf(new MockHttpSession(), 1L).getBody()));
        verify(auditoria).registrarImpresion(isNull(), eq("FACTURA"), eq("1"), contains("F-1"));

        // Impresión por lotes
        when(impresion.generarFacturasLotePdf(any())).thenReturn("LOTE".getBytes());
        assertEquals("LOTE", new String(controller.imprimirLote(new MockHttpSession(), List.of(1L)).getBody()));
        assertThrows(RuntimeException.class, () -> controller.imprimirLote(new MockHttpSession(), List.of()));

        when(documentos.guardarFactura(any(), any())).thenThrow(new IllegalArgumentException("lineas"));
        assertEquals("redirect:/web/facturas",
                controller.guardar(1L, 4L, "2026-06-19", null, null, null, null, null, null, null, request, ra));
        doThrow(new IllegalStateException("emitida")).when(facturas).aprobarYEmitir(8L);
        controller.emitir(8L, ra);
        assertEquals("emitida", ra.getFlashAttributes().get("error"));
        when(facturas.findByIdParaPdf(9L)).thenReturn(Optional.empty());
        assertThrows(ErpException.class, () -> controller.descargarPdf(new MockHttpSession(), 9L));
    }

    @Test
    void albaranCubreListadoEdicionCicloYDescarga() throws Exception {
        AlbaranService albaranes = mock(AlbaranService.class);
        ClienteService clientes = mock(ClienteService.class);
        ArticuloService articulos = mock(ArticuloService.class);
        AlmacenService almacenes = mock(AlmacenService.class);
        ImpresionService impresion = mock(ImpresionService.class);
        AuditoriaService auditoria = mock(AuditoriaService.class);
        UsuarioService usuarios = mock(UsuarioService.class);
        DocumentoService documentos = mock(DocumentoService.class);
        AlbaranVenta albaran = new AlbaranVenta();
        albaran.setId(1L);
        albaran.setNumero("A-1");
        AlbaranVentaLinea linea = new AlbaranVentaLinea();
        Articulo articulo = new Articulo();
        articulo.setId(3L);
        linea.setArticulo(articulo);
        linea.setCantidad(BigDecimal.ONE);
        linea.setPrecio(new BigDecimal("2"));
        linea.setIva(new BigDecimal("4"));
        albaran.getAlbaranVentaLineas().add(linea);
        Factura factura = factura(5L, "F-5", "BORRADOR");

        when(albaranes.buscarPaginado(any(), any(), any(), any(), any())).thenReturn(new PageImpl<>(List.of(albaran)));
        when(albaranes.obtenerPorIdParaPdf(1L)).thenReturn(Optional.of(albaran));
        when(documentos.guardarAlbaran(any(), any())).thenReturn(albaran);
        when(albaranes.convertirAFactura(eq(1L), any(), any(), any(), any(), any())).thenReturn(factura);
        File pdf = Files.createTempFile("albaran-test", ".pdf").toFile();
        Files.writeString(pdf.toPath(), "ALB");
        when(impresion.generarAlbaranPdf(albaran)).thenReturn(pdf);
        AlbaranWebController controller = new AlbaranWebController(
                albaranes, clientes, articulos, almacenes, impresion, auditoria, usuarios, documentos,
                mock(TipoImpositivoService.class));

        assertEquals("layout", controller.listado(new ExtendedModelMap(), null, null, null, null, 0, 25, "fecha", "desc"));
        assertEquals("layout", controller.nuevo(new ExtendedModelMap()));
        assertEquals("layout", controller.ver(1L, new ExtendedModelMap(), new RedirectAttributesModelMap()));
        assertEquals("layout", controller.editar(1L, new ExtendedModelMap(), new RedirectAttributesModelMap()));
        assertEquals("redirect:/web/albaranes", controller.ver(9L, new ExtendedModelMap(), new RedirectAttributesModelMap()));

        RedirectAttributesModelMap ra = new RedirectAttributesModelMap();
        assertEquals("redirect:/web/albaranes/1",
                controller.guardar(null, 2L, 3L, "2026-06-19", "Obs", null, null, null, lineRequest(), ra));
        assertEquals("redirect:/web/albaranes/1", controller.marcarEntregado(1L, ra));
        assertEquals("redirect:/web/facturas/5",
                controller.facturar(new MockHttpSession(), 1L, "2026-06-19", "EFECTIVO", null, "Obs", ra));
        assertEquals("ALB", new String(controller.descargarPdf(new MockHttpSession(), 1L).getBody()));

        // Impresión por lotes
        when(impresion.generarAlbaranesLotePdf(any())).thenReturn("LOTE".getBytes());
        assertEquals("LOTE", new String(controller.imprimirLote(new MockHttpSession(), List.of(1L)).getBody()));
        assertThrows(RuntimeException.class, () -> controller.imprimirLote(new MockHttpSession(), List.of()));

        when(documentos.guardarAlbaran(any(), any())).thenThrow(new IllegalArgumentException("lineas"));
        assertEquals("redirect:/web/albaranes/1/editar",
                controller.guardar(1L, 2L, null, "2026-06-19", null, null, null, null, lineRequest(), ra));
        doThrow(new IllegalArgumentException("stock")).when(albaranes).marcarEntregado(8L);
        controller.marcarEntregado(8L, ra);
        assertEquals("stock", ra.getFlashAttributes().get("error"));
        when(albaranes.convertirAFactura(eq(8L), any(), any(), any(), any(), any()))
                .thenThrow(new IllegalArgumentException("facturado"));
        assertEquals("redirect:/web/albaranes/8",
                controller.facturar(new MockHttpSession(), 8L, null, null, null, null, ra));
    }

    @Test
    void presupuestoCubreFiltrosEdicionAltaYAprobacion() {
        PresupuestoService presupuestos = mock(PresupuestoService.class);
        ClienteService clientes = mock(ClienteService.class);
        ArticuloService articulos = mock(ArticuloService.class);
        DocumentoService documentos = mock(DocumentoService.class);
        Presupuesto presupuesto = new Presupuesto();
        presupuesto.setId(1L);
        presupuesto.setNumero("P-1");
        PresupuestoLinea linea = new PresupuestoLinea();
        Articulo articulo = new Articulo();
        articulo.setId(3L);
        linea.setArticulo(articulo);
        linea.setCantidad(BigDecimal.ONE);
        linea.setPrecioUnitario(new BigDecimal("2"));
        linea.setTipoIva(new BigDecimal("4"));
        linea.setDescuento(BigDecimal.ZERO);
        presupuesto.getLineas().add(linea);

        when(presupuestos.obtenerTodos(any(Pageable.class))).thenReturn(Page.empty());
        when(presupuestos.buscar(eq("p"), any(Pageable.class))).thenReturn(Page.empty());
        when(presupuestos.buscarPorEstado(eq("BORRADOR"), any(Pageable.class))).thenReturn(Page.empty());
        when(presupuestos.obtenerPorId(1L)).thenReturn(Optional.of(presupuesto));
        when(documentos.guardarPresupuesto(any(), any())).thenReturn(presupuesto);
        PresupuestoWebController controller = new PresupuestoWebController(presupuestos, clientes, articulos, documentos);

        assertEquals("layout", controller.lista(new ExtendedModelMap(), null, null, 0, 20));
        assertEquals("layout", controller.lista(new ExtendedModelMap(), "p", null, 0, 20));
        assertEquals("layout", controller.lista(new ExtendedModelMap(), null, "BORRADOR", 0, 20));
        assertEquals("layout", controller.formularioNuevo(new ExtendedModelMap()));
        assertEquals("layout", controller.editar(1L, new ExtendedModelMap(), new RedirectAttributesModelMap()));
        assertEquals("layout", controller.ver(1L, new ExtendedModelMap(), new RedirectAttributesModelMap()));

        RedirectAttributesModelMap ra = new RedirectAttributesModelMap();
        MockHttpServletRequest request = lineRequest();
        request.setParameter("clienteId", "2");
        request.setParameter("fecha", "2026-06-19");
        assertEquals("redirect:/web/presupuestos/1", controller.guardar(null, request, ra));
        assertEquals("redirect:/web/presupuestos/1", controller.aprobar(1L, ra));
        verify(presupuestos).aceptar(1L);

        when(documentos.guardarPresupuesto(any(), any())).thenThrow(new IllegalArgumentException("datos"));
        assertEquals("redirect:/web/presupuestos", controller.guardar(1L, request, ra));
        doThrow(new IllegalArgumentException("estado")).when(presupuestos).aceptar(8L);
        controller.aprobar(8L, ra);
        assertEquals("estado", ra.getFlashAttributes().get("error"));
    }

    @Test
    void reportesCalculanVentasProduccionRentabilidadRepartoYTrazabilidad() {
        FacturaService facturas = mock(FacturaService.class);
        FacturaCompraService compras = mock(FacturaCompraService.class);
        OrdenProduccionService produccion = mock(OrdenProduccionService.class);
        LoteService lotes = mock(LoteService.class);
        HojaRutaService rutas = mock(HojaRutaService.class);
        ArticuloService articulos = mock(ArticuloService.class);
        ClienteService clientes = mock(ClienteService.class);
        Factura factura = factura(1L, "F-1", "EMITIDA");
        factura.setFecha(LocalDate.now());
        factura.setTotal(new BigDecimal("100"));
        Cliente cliente = new Cliente();
        cliente.setNombre("Cliente");
        factura.setCliente(cliente);
        when(facturas.findByFechaBetweenAndEstado(any(), any(), eq("EMITIDA"))).thenReturn(List.of(factura));

        OrdenProduccion finalizada = orden("FINALIZADA", "10", "8");
        OrdenProduccion curso = orden("EN_CURSO", "5", "2");
        OrdenProduccion cancelada = orden("CANCELADA", null, null);
        when(produccion.findByFechaBetween(any(), any())).thenReturn(List.of(finalizada, curso, cancelada));

        Articulo rentable = new Articulo();
        rentable.setActivo(true);
        rentable.setCodigo("A1");
        rentable.setNombre("Pan");
        rentable.setPvp(new BigDecimal("2"));
        rentable.setCoste(new BigDecimal("1"));
        when(articulos.findAll()).thenReturn(List.of(rentable));

        HojaRuta terminada = new HojaRuta();
        terminada.setEstado("FINALIZADA");
        HojaRuta enCurso = new HojaRuta();
        enCurso.setEstado("EN_CURSO");
        when(rutas.findByFechaBetween(any(), any())).thenReturn(List.of(terminada, enCurso));
        when(lotes.count()).thenReturn(10L);
        when(lotes.countByFechaCaducidadBefore(any())).thenReturn(2L);
        when(lotes.countByFechaCaducidadBetween(any(), any())).thenReturn(3L);

        ReportesWebController controller = new ReportesWebController(
                facturas, compras, produccion, lotes, rutas, articulos, clientes);
        assertEquals("layout", controller.index(new ExtendedModelMap()));
        ExtendedModelMap ventas = new ExtendedModelMap();
        assertEquals("layout", controller.ventasMes(ventas));
        assertEquals(new BigDecimal("100"), ventas.get("totalVentas"));
        ExtendedModelMap prod = new ExtendedModelMap();
        assertEquals("layout", controller.produccion(prod));
        assertEquals(3L, prod.get("totalOrdenes"));
        ExtendedModelMap rent = new ExtendedModelMap();
        assertEquals("layout", controller.rentabilidad(rent));
        assertEquals(1, rent.get("numArticulos"));
        assertEquals("layout", controller.reparto(new ExtendedModelMap()));
        assertEquals("layout", controller.trazabilidad(new ExtendedModelMap()));
        assertEquals("layout", controller.inventario(new ExtendedModelMap()));
    }

    private MockHttpServletRequest lineRequest() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setParameter("lineaArticuloId", "3");
        request.setParameter("lineaCantidad", "2");
        request.setParameter("lineaPrecio", "1,50");
        request.setParameter("lineaIva", "4");
        request.setParameter("lineaDescuento", "0");
        return request;
    }

    private Factura factura(Long id, String numero, String estado) {
        Factura factura = new Factura();
        factura.setId(id);
        factura.setNumero(numero);
        factura.setEstado(estado);
        factura.setFecha(LocalDate.now());
        factura.setTotal(BigDecimal.TEN);
        return factura;
    }

    private OrdenProduccion orden(String estado, String planificada, String producida) {
        OrdenProduccion orden = new OrdenProduccion();
        orden.setEstado(estado);
        if (planificada != null) orden.setCantidadPlanificada(new BigDecimal(planificada));
        if (producida != null) orden.setCantidadProducida(new BigDecimal(producida));
        return orden;
    }
}
