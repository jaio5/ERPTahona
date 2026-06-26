package alicanteweb.erp.controller.web;

import alicanteweb.erp.entities.*;
import alicanteweb.erp.service.*;
import org.junit.jupiter.api.Test;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.web.servlet.mvc.support.RedirectAttributesModelMap;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class SimpleWebControllersCoverageTest {

    @Test
    void proveedorCubreListadosFormulariosDetalleYGuardado() {
        ProveedorService service = mock(ProveedorService.class);
        Proveedor proveedor = new Proveedor();
        proveedor.setId(1L);
        proveedor.setNombre("Harinas");
        when(service.buscarPaginado(any(), any(Pageable.class))).thenReturn(Page.empty());
        when(service.findById(1L)).thenReturn(Optional.of(proveedor));
        ProveedorWebController controller = new ProveedorWebController(service);

        assertEquals("layout", controller.lista(new ExtendedModelMap(), null, 0, 20));
        assertEquals("layout", controller.lista(new ExtendedModelMap(), "har", 0, 20));
        assertEquals("layout", controller.nuevo(new ExtendedModelMap()));
        assertEquals("layout", controller.ver(1L, new ExtendedModelMap(), new RedirectAttributesModelMap()));
        assertEquals("layout", controller.editar(1L, new ExtendedModelMap(), new RedirectAttributesModelMap()));
        assertEquals("redirect:/web/proveedores", controller.ver(9L, new ExtendedModelMap(), new RedirectAttributesModelMap()));

        RedirectAttributesModelMap ra = new RedirectAttributesModelMap();
        assertEquals("redirect:/web/proveedores",
                controller.guardar(null, "P1", "Harinas", "B1", "1", "a@b.es", ra));
        verify(service).save(any(Proveedor.class));

        when(service.findById(2L)).thenReturn(Optional.empty());
        controller.guardar(2L, "P2", "X", null, null, null, ra);
        assertNotNull(ra.getFlashAttributes().get("error"));
    }

    @Test
    void almacenCubreFlujosPrincipales() {
        AlmacenService service = mock(AlmacenService.class);
        Almacen almacen = new Almacen();
        almacen.setId(1L);
        almacen.setNombre("Central");
        when(service.findAll()).thenReturn(List.of(almacen));
        when(service.buscar("cen")).thenReturn(List.of(almacen));
        when(service.findById(1L)).thenReturn(Optional.of(almacen));
        AlmacenWebController controller = new AlmacenWebController(service);

        assertEquals("layout", controller.lista(new ExtendedModelMap(), null));
        assertEquals("layout", controller.lista(new ExtendedModelMap(), "cen"));
        assertEquals("layout", controller.nuevo(new ExtendedModelMap()));
        assertEquals("layout", controller.editar(1L, new ExtendedModelMap(), new RedirectAttributesModelMap()));
        assertEquals("layout", controller.ver(1L, new ExtendedModelMap(), new RedirectAttributesModelMap()));
        assertEquals("redirect:/web/almacenes", controller.ver(9L, new ExtendedModelMap(), new RedirectAttributesModelMap()));

        RedirectAttributesModelMap ra = new RedirectAttributesModelMap();
        controller.guardar(null, "A1", "Central", "Principal", new BigDecimal("100"), "Alicante", "Ana", ra);
        verify(service).save(any(Almacen.class));
        when(service.save(any())).thenThrow(new IllegalArgumentException("capacidad"));
        controller.guardar(null, "A2", "Error", null, null, null, null, ra);
        assertEquals("capacidad", ra.getFlashAttributes().get("error"));
    }

    @Test
    void vehiculoCubreFlujosPrincipales() {
        VehiculoService service = mock(VehiculoService.class);
        Vehiculo vehiculo = new Vehiculo();
        vehiculo.setId(1L);
        vehiculo.setMatricula("1234ABC");
        when(service.findAll()).thenReturn(List.of(vehiculo));
        when(service.buscar("1234")).thenReturn(List.of(vehiculo));
        when(service.findById(1L)).thenReturn(Optional.of(vehiculo));
        VehiculoWebController controller = new VehiculoWebController(service);

        assertEquals("layout", controller.lista(new ExtendedModelMap(), null));
        assertEquals("layout", controller.lista(new ExtendedModelMap(), "1234"));
        assertEquals("layout", controller.formularioNuevo(new ExtendedModelMap()));
        assertEquals("layout", controller.formularioEditar(1L, new ExtendedModelMap(), new RedirectAttributesModelMap()));
        assertEquals("layout", controller.ver(1L, new ExtendedModelMap(), new RedirectAttributesModelMap()));
        assertEquals("redirect:/web/vehiculos", controller.formularioEditar(9L, new ExtendedModelMap(), new RedirectAttributesModelMap()));

        RedirectAttributesModelMap ra = new RedirectAttributesModelMap();
        controller.guardar(null, "1234ABC", "Ford", "Transit", "FURGON", new BigDecimal("900"), ra);
        verify(service).save(any(Vehiculo.class));
        when(service.findById(2L)).thenReturn(Optional.empty());
        controller.guardar(2L, "X", null, null, null, null, ra);
        assertNotNull(ra.getFlashAttributes().get("error"));
    }

    @Test
    void recetaCubreFlujosPrincipales() {
        RecetaService service = mock(RecetaService.class);
        Receta receta = new Receta();
        receta.setId(1L);
        receta.setNombre("Pan");
        when(service.findAll()).thenReturn(List.of(receta));
        when(service.searchByNombre("pan")).thenReturn(List.of(receta));
        when(service.findById(1L)).thenReturn(Optional.of(receta));
        when(service.findDetailById(1L)).thenReturn(Optional.of(receta));
        when(service.getIngredientes(1L)).thenReturn(List.of());
        RecetaWebController controller = new RecetaWebController(service);

        assertEquals("layout", controller.lista(new ExtendedModelMap(), null));
        assertEquals("layout", controller.lista(new ExtendedModelMap(), "pan"));
        assertEquals("layout", controller.nuevo(new ExtendedModelMap()));
        assertEquals("layout", controller.editar(1L, new ExtendedModelMap(), new RedirectAttributesModelMap()));
        assertEquals("layout", controller.ver(1L, new ExtendedModelMap(), new RedirectAttributesModelMap()));
        assertEquals("redirect:/web/recetas", controller.editar(9L, new ExtendedModelMap(), new RedirectAttributesModelMap()));

        RedirectAttributesModelMap ra = new RedirectAttributesModelMap();
        controller.guardar(null, "R1", "Pan", "Masa", 10, 20, 200, "Gluten", ra);
        verify(service).save(any(Receta.class));
        when(service.save(any())).thenThrow(new IllegalArgumentException("duplicada"));
        controller.guardar(null, "R1", "Pan", null, null, null, null, null, ra);
        assertEquals("duplicada", ra.getFlashAttributes().get("error"));
    }

    @Test
    void mermaCubreAltaConsultaYErrores() {
        MermaService mermas = mock(MermaService.class);
        ArticuloService articulos = mock(ArticuloService.class);
        Merma merma = new Merma();
        merma.setId(1L);
        Articulo articulo = new Articulo();
        articulo.setId(2L);
        when(mermas.findAll()).thenReturn(List.of(merma));
        when(mermas.findByTipo("CADUCIDAD")).thenReturn(List.of(merma));
        when(mermas.findDetailById(1L)).thenReturn(Optional.of(merma));
        when(articulos.findAll()).thenReturn(List.of(articulo));
        when(articulos.findById(2L)).thenReturn(Optional.of(articulo));
        MermaWebController controller = new MermaWebController(mermas, articulos);

        assertEquals("layout", controller.lista(new ExtendedModelMap(), null));
        assertEquals("layout", controller.lista(new ExtendedModelMap(), "CADUCIDAD"));
        assertEquals("layout", controller.formulario(new ExtendedModelMap()));
        assertEquals("layout", controller.ver(1L, new ExtendedModelMap(), new RedirectAttributesModelMap()));
        assertEquals("redirect:/web/mermas", controller.ver(9L, new ExtendedModelMap(), new RedirectAttributesModelMap()));

        RedirectAttributesModelMap ra = new RedirectAttributesModelMap();
        controller.guardar(null, 2L, BigDecimal.ONE, "Caducado", "CADUCIDAD",
                "Ana", "Obs", "2026-06-19", ra);
        verify(mermas).save(any(Merma.class));
        when(mermas.save(any())).thenThrow(new IllegalArgumentException("stock"));
        controller.guardar(null, 2L, BigDecimal.ONE, null, null, null, null, null, ra);
        assertEquals("stock", ra.getFlashAttributes().get("error"));
    }

    @Test
    void horneadaCubreAltaListadosYDetalle() {
        HorneadaService service = mock(HorneadaService.class);
        Horneada horneada = new Horneada();
        horneada.setId(1L);
        horneada.setFecha(LocalDate.now());
        when(service.findAll()).thenReturn(List.of(horneada));
        when(service.buscar("pan")).thenReturn(List.of(horneada));
        when(service.findById(1L)).thenReturn(Optional.of(horneada));
        HorneadaWebController controller = new HorneadaWebController(service);

        assertEquals("layout", controller.lista(new ExtendedModelMap(), null));
        assertEquals("layout", controller.lista(new ExtendedModelMap(), "pan"));
        assertEquals("layout", controller.formularioNuevo(new ExtendedModelMap()));
        assertEquals("layout", controller.ver(1L, new ExtendedModelMap(), new RedirectAttributesModelMap()));
        assertEquals("redirect:/web/horneadas", controller.ver(9L, new ExtendedModelMap(), new RedirectAttributesModelMap()));

        RedirectAttributesModelMap ra = new RedirectAttributesModelMap();
        controller.guardar(null, "PAN", 180, 200, new BigDecimal("20"), "OK", "Obs",
                new BigDecimal("50"), new BigDecimal("30"), new BigDecimal("100"), ra);
        verify(service).save(any(Horneada.class));
        when(service.save(any())).thenThrow(new IllegalArgumentException("horno"));
        controller.guardar(null, null, null, null, null, null, null, null, null, null, ra);
        assertEquals("horno", ra.getFlashAttributes().get("error"));
    }
}
