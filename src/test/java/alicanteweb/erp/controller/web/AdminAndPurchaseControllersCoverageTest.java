package alicanteweb.erp.controller.web;

import alicanteweb.erp.entities.*;
import alicanteweb.erp.service.*;
import org.junit.jupiter.api.Test;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.web.servlet.mvc.support.RedirectAttributesModelMap;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class AdminAndPurchaseControllersCoverageTest {

    @Test
    void empresaCubreConsultaGuardadoValidacionVatYError() {
        EmpresaConfigService empresas = mock(EmpresaConfigService.class);
        VatValidationService vat = mock(VatValidationService.class);
        EmpresaConfig empresa = new EmpresaConfig();
        when(empresas.getConfiguracionActiva()).thenReturn(Optional.of(empresa));
        EmpresaWebController controller = new EmpresaWebController(empresas, vat);

        assertEquals("layout", controller.form(new ExtendedModelMap()));
        RedirectAttributesModelMap ra = new RedirectAttributesModelMap();
        assertEquals("redirect:/web/empresa", controller.guardar(
                "Tahona", "B123", "Calle", "03001", "Alicante", "Alicante",
                "1", "a@b.es", "RS", "RM", "https://example.test", ra));
        verify(empresas).save(empresa);

        assertEquals("layout", controller.validarVat(new ExtendedModelMap(),
                "Tahona", "B123", null, null, null, null, null, null, null, null, null));
        verify(vat).validar("B123");

        doThrow(new IllegalArgumentException("cif")).when(empresas).save(any());
        controller.guardar("X", "X", null, null, null, null, null, null, null, null, null, ra);
        assertEquals("cif", ra.getFlashAttributes().get("error"));
    }

    @Test
    void usuarioCubreAdministracionCompleta() {
        UsuarioService service = mock(UsuarioService.class);
        Usuario usuario = new Usuario();
        usuario.setId(1L);
        usuario.setUsername("ana");
        when(service.listarPaginado(isNull(), any(Pageable.class))).thenReturn(Page.empty());
        when(service.buscarPorId(1L)).thenReturn(Optional.of(usuario));
        UsuarioWebController controller = new UsuarioWebController(service);
        ExtendedModelMap model = new ExtendedModelMap();

        assertEquals("layout", controller.lista(model, 0, 20, null));
        assertEquals("layout", controller.nuevo(new ExtendedModelMap()));
        assertEquals("layout", controller.ver(1L, new ExtendedModelMap()));
        assertEquals("layout", controller.editar(1L, new ExtendedModelMap()));

        RedirectAttributesModelMap ra = new RedirectAttributesModelMap();
        controller.crear("bea", "secreto", "Bea", "b@e.es", "GESTOR", ra);
        verify(service).crearUsuario(any(Usuario.class), eq("secreto"));
        controller.actualizar(1L, "Ana", "a@e.es", "ADMIN", ra);
        verify(service).actualizarUsuario(usuario);
        controller.cambiarPassword(1L, "nueva", ra);
        controller.bloquear(1L, ra);
        controller.desbloquear(1L, ra);
        controller.desactivar(1L, ra);
        controller.activar(1L, ra);
        verify(service).cambiarPasswordAdmin(1L, "nueva");
        verify(service).bloquearUsuario(1L);
        verify(service).desbloquearUsuario(1L);
        verify(service).eliminarUsuario(1L);
        verify(service).activarUsuario(1L);

        doThrow(new IllegalArgumentException("error")).when(service).bloquearUsuario(2L);
        controller.bloquear(2L, ra);
        assertEquals("error", ra.getFlashAttributes().get("error"));
    }

    @Test
    void pedidoCompraCubreFiltrosAltaYDetalle() {
        PedidoCompraService pedidos = mock(PedidoCompraService.class);
        ProveedorService proveedores = mock(ProveedorService.class);
        PedidoCompra pedido = new PedidoCompra();
        pedido.setId(1L);
        pedido.setNumero("PC-1");
        Proveedor proveedor = new Proveedor();
        proveedor.setId(2L);
        when(pedidos.findPage(any(), any(), any(Pageable.class))).thenReturn(Page.empty());
        when(pedidos.findByIdWithLineas(1L)).thenReturn(Optional.of(pedido));
        when(proveedores.findAll()).thenReturn(List.of(proveedor));
        when(proveedores.findById(2L)).thenReturn(Optional.of(proveedor));
        PedidoCompraWebController controller = new PedidoCompraWebController(pedidos, proveedores);

        assertEquals("layout", controller.lista(new ExtendedModelMap(), 0, 20, null, null));
        assertEquals("layout", controller.lista(new ExtendedModelMap(), 0, 20, "pc", null));
        assertEquals("layout", controller.lista(new ExtendedModelMap(), 0, 20, null, "BORRADOR"));
        assertEquals("layout", controller.formularioNuevo(new ExtendedModelMap()));
        assertEquals("layout", controller.ver(1L, new ExtendedModelMap(), new RedirectAttributesModelMap()));
        assertEquals("redirect:/web/pedidos-compra", controller.ver(9L, new ExtendedModelMap(), new RedirectAttributesModelMap()));

        RedirectAttributesModelMap ra = new RedirectAttributesModelMap();
        controller.guardar(null, 2L, "Obs", ra);
        verify(pedidos).save(any(PedidoCompra.class));
        when(pedidos.save(any())).thenThrow(new IllegalArgumentException("pedido"));
        controller.guardar(null, 2L, null, ra);
        assertEquals("pedido", ra.getFlashAttributes().get("error"));
    }

    @Test
    void facturaCompraCubreFiltrosFormularioDetalleYAlta() {
        FacturaCompraService facturas = mock(FacturaCompraService.class);
        ProveedorService proveedores = mock(ProveedorService.class);
        ArticuloService articulos = mock(ArticuloService.class);
        FacturaCompra factura = new FacturaCompra();
        factura.setId(1L);
        factura.setNumero("FC-1");
        factura.setEstado("PENDIENTE");
        Proveedor proveedor = new Proveedor();
        proveedor.setId(2L);
        when(facturas.findPage(any(), any(), any(Pageable.class))).thenReturn(Page.empty());
        when(facturas.obtenerDetallePorId(1L)).thenReturn(Optional.of(factura));
        when(proveedores.findById(2L)).thenReturn(Optional.of(proveedor));
        FacturaCompraWebController controller = new FacturaCompraWebController(facturas, proveedores, articulos);

        assertEquals("layout", controller.lista(new ExtendedModelMap(), null, null, 0, 20));
        assertEquals("layout", controller.lista(new ExtendedModelMap(), "fc", null, 0, 20));
        assertEquals("layout", controller.lista(new ExtendedModelMap(), null, "PENDIENTE", 0, 20));
        assertEquals("layout", controller.formularioNuevo(new ExtendedModelMap()));
        assertEquals("layout", controller.ver(1L, new ExtendedModelMap(), new RedirectAttributesModelMap()));
        assertEquals("redirect:/web/facturas-compra", controller.ver(9L, new ExtendedModelMap(), new RedirectAttributesModelMap()));

        RedirectAttributesModelMap ra = new RedirectAttributesModelMap();
        controller.guardar(null, 2L, "FC-2", "2026-06-19", "2026-07-19", "TRANSFERENCIA",
                new BigDecimal("100"), new BigDecimal("21"), new BigDecimal("21"), "Obs", ra);
        verify(facturas).guardar(any(FacturaCompra.class));
        doThrow(new IllegalArgumentException("factura")).when(facturas).guardar(any());
        controller.guardar(null, 2L, "FC-3", null, null, null, null, null, null, null, ra);
        assertEquals("factura", ra.getFlashAttributes().get("error"));
    }

    @Test
    void devolucionCubreListadosFormulariosDetalleYAlta() {
        DevolucionService devoluciones = mock(DevolucionService.class);
        ClienteService clientes = mock(ClienteService.class);
        Devolucion devolucion = new Devolucion();
        devolucion.setId(1L);
        devolucion.setNumero("D-1");
        Cliente cliente = new Cliente();
        cliente.setId(2L);
        when(devoluciones.findAll()).thenReturn(List.of(devolucion));
        when(devoluciones.buscar("d")).thenReturn(List.of(devolucion));
        when(devoluciones.findById(1L)).thenReturn(Optional.of(devolucion));
        when(devoluciones.findDetailById(1L)).thenReturn(Optional.of(devolucion));
        when(clientes.findAll()).thenReturn(List.of(cliente));
        when(clientes.findById(2L)).thenReturn(Optional.of(cliente));
        DevolucionWebController controller = new DevolucionWebController(devoluciones, clientes);

        assertEquals("layout", controller.lista(new ExtendedModelMap(), null));
        assertEquals("layout", controller.lista(new ExtendedModelMap(), "d"));
        assertEquals("layout", controller.formularioNuevo(new ExtendedModelMap()));
        assertEquals("layout", controller.editar(1L, new ExtendedModelMap(), new RedirectAttributesModelMap()));
        assertEquals("layout", controller.ver(1L, new ExtendedModelMap(), new RedirectAttributesModelMap()));
        assertEquals("redirect:/web/devoluciones", controller.editar(9L, new ExtendedModelMap(), new RedirectAttributesModelMap()));

        RedirectAttributesModelMap ra = new RedirectAttributesModelMap();
        controller.guardar(null, 2L, "D-2", "Rotura", "Obs", ra);
        verify(devoluciones).save(any(Devolucion.class));
        when(devoluciones.save(any())).thenThrow(new IllegalArgumentException("devolucion"));
        controller.guardar(null, 2L, "D-3", null, null, ra);
        assertEquals("devolucion", ra.getFlashAttributes().get("error"));
    }
}
