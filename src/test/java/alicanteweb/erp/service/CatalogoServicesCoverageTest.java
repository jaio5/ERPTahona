package alicanteweb.erp.service;

import alicanteweb.erp.entities.*;
import alicanteweb.erp.repository.*;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageRequest;
import org.springframework.mock.web.MockMultipartFile;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class CatalogoServicesCoverageTest {

    @Test
    void clienteCubreConsultasValidacionesYEstados() {
        ClienteRepository repo = mock(ClienteRepository.class);
        ClienteService service = new ClienteService(repo);
        Cliente cliente = cliente(1L, "C1", "B123");

        when(repo.findAll()).thenReturn(List.of(cliente));
        when(repo.count()).thenReturn(1L);
        when(repo.findById(1L)).thenReturn(Optional.of(cliente));
        when(repo.findByCodigo("C1")).thenReturn(Optional.of(cliente));
        when(repo.findByCifIgnoreCase("B123")).thenReturn(Optional.of(cliente));
        when(repo.findFirstByNombreIgnoreCase("Panadería")).thenReturn(Optional.of(cliente));
        when(repo.findByNombreContainingIgnoreCase("pan")).thenReturn(List.of(cliente));
        when(repo.save(any())).thenAnswer(i -> i.getArgument(0));

        assertEquals(1, service.findAll().size());
        assertEquals(1, service.count());
        assertTrue(service.findById(1L).isPresent());
        assertTrue(service.findByCodigo("C1").isPresent());
        assertTrue(service.findByCif(" B123 ").isPresent());
        assertTrue(service.findByCif(" ").isEmpty());
        assertTrue(service.findByNombreExacto(" Panadería ").isPresent());
        assertTrue(service.findByNombreExacto(null).isEmpty());
        assertEquals(1, service.searchByNombre("pan").size());

        service.darDeBaja(1L);
        assertFalse(cliente.getActivo());
        service.activar(1L);
        assertTrue(cliente.getActivo());
        service.deleteById(1L);
        verify(repo).deleteById(1L);

        assertThrows(IllegalArgumentException.class, () -> service.save(null));
        assertThrows(IllegalArgumentException.class, () -> service.save(new Cliente()));

        Cliente nuevo = cliente(null, "N1", "NIF");
        when(repo.existsByCodigo("N1")).thenReturn(false);
        when(repo.findByCif("NIF")).thenReturn(Optional.empty());
        assertSame(nuevo, service.save(nuevo));

        when(repo.existsByCodigo("N1")).thenReturn(true);
        assertThrows(IllegalArgumentException.class, () -> service.save(nuevo));

        Cliente otro = cliente(2L, "C1", "OTRO");
        when(repo.findByCodigo("C1")).thenReturn(Optional.of(cliente));
        assertThrows(IllegalArgumentException.class, () -> service.save(otro));

        Cliente cifDuplicado = cliente(null, "N2", "B123");
        when(repo.existsByCodigo("N2")).thenReturn(false);
        when(repo.findByCif("B123")).thenReturn(Optional.of(cliente));
        assertThrows(IllegalArgumentException.class, () -> service.save(cifDuplicado));

        service.buscarPaginado(" pan ", PageRequest.of(0, 10));
        service.buscarPaginado(" ", PageRequest.of(0, 10));
        service.buscarParaApi(" pan ", PageRequest.of(0, 10));
        verify(repo).buscarParaApi("pan", PageRequest.of(0, 10));
    }

    @Test
    void proveedorGeneraCodigoYValidaDuplicados() {
        ProveedorRepository repo = mock(ProveedorRepository.class);
        ProveedorService service = new ProveedorService(repo);
        Proveedor existente = proveedor(1L, "PROV0007", "B1");
        Proveedor codigoRoto = proveedor(2L, "PROVXX", null);
        when(repo.findAll()).thenReturn(List.of(existente, codigoRoto));
        when(repo.findAllActivos()).thenReturn(List.of(existente));
        when(repo.findById(1L)).thenReturn(Optional.of(existente));
        when(repo.findByCodigo(anyString())).thenReturn(Optional.empty());
        when(repo.findByCif(anyString())).thenReturn(Optional.empty());
        when(repo.findByCifIgnoreCase("B1")).thenReturn(Optional.of(existente));
        when(repo.findFirstByNombreIgnoreCase("Proveedor")).thenReturn(Optional.of(existente));
        when(repo.findByNombreContainingIgnoreCase("prov")).thenReturn(List.of(existente));
        when(repo.buscarPorCriterio("x")).thenReturn(List.of(existente));
        when(repo.save(any())).thenAnswer(i -> i.getArgument(0));

        assertEquals(2, service.findAll().size());
        assertEquals(1, service.findActivos().size());
        assertTrue(service.findById(1L).isPresent());
        assertTrue(service.findByCif(" B1 ").isPresent());
        assertTrue(service.findByCif("").isEmpty());
        assertTrue(service.findByNombreExacto(" Proveedor ").isPresent());
        assertTrue(service.findByNombreExacto(null).isEmpty());
        assertEquals(1, service.searchByNombre(" prov ").size());
        assertTrue(service.searchByNombre(" ").isEmpty());
        assertEquals(1, service.buscar("x").size());

        Proveedor nuevo = new Proveedor();
        nuevo.setNombre("Nuevo");
        assertSame(nuevo, service.save(nuevo));
        assertEquals("PROV0008", nuevo.getCodigo());

        when(repo.findByCodigo("PROV0007")).thenReturn(Optional.of(existente));
        Proveedor duplicado = proveedor(null, "PROV0007", null);
        assertThrows(IllegalArgumentException.class, () -> service.save(duplicado));

        when(repo.findByCodigo("P2")).thenReturn(Optional.empty());
        when(repo.findByCif("B1")).thenReturn(Optional.of(existente));
        assertThrows(IllegalArgumentException.class, () -> service.save(proveedor(null, "P2", "B1")));

        service.activar(1L);
        assertTrue(existente.getActivo());
        service.desactivar(1L);
        assertFalse(existente.getActivo());
        service.deleteById(1L);
        verify(repo).deleteById(1L);
        assertThrows(IllegalArgumentException.class, () -> service.save(null));
    }

    @Test
    void recetaGestionaIngredientesCostesYEstados() {
        RecetaRepository recetas = mock(RecetaRepository.class);
        RecetaIngredienteRepository ingredientes = mock(RecetaIngredienteRepository.class);
        RecetaService service = new RecetaService(recetas, ingredientes);
        Receta receta = new Receta();
        receta.setId(1L);
        receta.setCodigo("R1");
        receta.setRendimientoCantidad(new BigDecimal("4"));
        Articulo articulo = new Articulo();
        articulo.setCoste(new BigDecimal("2.50"));
        RecetaIngrediente ing = new RecetaIngrediente();
        ing.setArticulo(articulo);
        ing.setCantidad(new BigDecimal("2"));
        ing.setOrden(2);
        ing.setReceta(receta);

        when(recetas.findById(1L)).thenReturn(Optional.of(receta));
        when(recetas.findByCodigo("R1")).thenReturn(Optional.empty());
        when(recetas.save(any())).thenAnswer(i -> i.getArgument(0));
        when(ingredientes.findByRecetaIdOrderByOrden(1L)).thenReturn(List.of(ing));
        when(ingredientes.findDetailByRecetaId(1L)).thenReturn(List.of(ing));
        when(ingredientes.save(any())).thenAnswer(i -> i.getArgument(0));

        assertSame(receta, service.save(receta));
        assertEquals(1, service.getIngredientes(1L).size());
        assertEquals(new BigDecimal("5.00"), service.calcularCoste(1L));
        assertEquals(new BigDecimal("1.2500"), service.calcularCostePorUnidad(1L));

        RecetaIngrediente nuevo = new RecetaIngrediente();
        nuevo.setReceta(receta);
        service.addIngrediente(nuevo);
        assertEquals(3, nuevo.getOrden());
        service.removeIngrediente(3L);
        service.deleteById(1L);

        service.darDeBaja(1L);
        assertFalse(receta.getActivo());
        service.activar(1L);
        assertTrue(receta.getActivo());

        assertThrows(IllegalArgumentException.class, () -> service.save(null));
        assertThrows(IllegalArgumentException.class, () -> service.save(new Receta()));
        when(recetas.findByCodigo("R1")).thenReturn(Optional.of(receta));
        Receta duplicada = new Receta();
        duplicada.setCodigo("R1");
        assertThrows(IllegalArgumentException.class, () -> service.save(duplicada));

        when(ingredientes.findByRecetaIdOrderByOrden(2L)).thenReturn(List.of());
        assertEquals(BigDecimal.ZERO, service.calcularCoste(2L));
        receta.setRendimientoCantidad(BigDecimal.ZERO);
        assertEquals(new BigDecimal("5.00"), service.calcularCostePorUnidad(1L));
    }

    @Test
    void facturaCompraCubreCicloCompleto() {
        FacturaCompraRepository repo = mock(FacturaCompraRepository.class);
        FacturaCompraService service = new FacturaCompraService(repo);
        FacturaCompra factura = new FacturaCompra();
        factura.setId(1L);
        when(repo.findById(1L)).thenReturn(Optional.of(factura));
        when(repo.save(any())).thenAnswer(i -> i.getArgument(0));

        assertSame(factura, service.guardar(factura));
        assertNotNull(factura.getFecha());
        assertEquals("PENDIENTE", factura.getEstado());

        FacturaCompra cambios = new FacturaCompra();
        cambios.setNumero("FC-1");
        cambios.setFecha(LocalDate.now());
        cambios.setEstado("VALIDADA");
        assertEquals("FC-1", service.actualizar(1L, cambios).getNumero());
        assertEquals("PAGADA", service.cambiarEstado(1L, "PAGADA").getEstado());
        assertTrue(service.marcarComoPagada(1L, LocalDate.now()).getPagada());

        service.obtenerTodas();
        service.obtenerPorId(1L);
        service.obtenerPorNumero("FC-1");
        service.buscarPorProveedor(2L);
        service.buscarPorEstado("PENDIENTE");
        service.buscarPendientesPago();
        service.buscar("x");
        service.eliminar(1L);

        when(repo.findById(9L)).thenReturn(Optional.empty());
        assertThrows(RuntimeException.class, () -> service.actualizar(9L, cambios));
        assertThrows(RuntimeException.class, () -> service.cambiarEstado(9L, "X"));
        assertThrows(RuntimeException.class, () -> service.marcarComoPagada(9L, LocalDate.now()));
    }

    @Test
    void almacenVehiculoMermaYHorneadaValidanOperaciones() {
        AlmacenRepository almacenes = mock(AlmacenRepository.class);
        AlmacenService almacenService = new AlmacenService(almacenes);
        Almacen almacen = new Almacen();
        almacen.setCapacidad(new BigDecimal("10"));
        almacen.setDisponible(new BigDecimal("5"));
        when(almacenes.save(any())).thenAnswer(i -> i.getArgument(0));
        assertSame(almacen, almacenService.save(almacen));
        almacen.setCapacidad(new BigDecimal("-1"));
        assertThrows(IllegalArgumentException.class, () -> almacenService.save(almacen));
        almacen.setCapacidad(new BigDecimal("10"));
        almacen.setDisponible(new BigDecimal("-1"));
        assertThrows(IllegalArgumentException.class, () -> almacenService.save(almacen));
        almacen.setDisponible(new BigDecimal("11"));
        assertThrows(IllegalArgumentException.class, () -> almacenService.save(almacen));

        VehiculoRepository vehiculos = mock(VehiculoRepository.class);
        VehiculoService vehiculoService = new VehiculoService(vehiculos);
        Vehiculo vehiculo = new Vehiculo();
        vehiculo.setId(1L);
        vehiculo.setMatricula("1234ABC");
        when(vehiculos.findByMatricula("1234ABC")).thenReturn(Optional.empty());
        when(vehiculos.findById(1L)).thenReturn(Optional.of(vehiculo));
        when(vehiculos.save(any())).thenAnswer(i -> i.getArgument(0));
        assertSame(vehiculo, vehiculoService.save(vehiculo));
        vehiculoService.darDeBaja(1L);
        assertFalse(vehiculo.getActivo());
        assertThrows(IllegalArgumentException.class, () -> vehiculoService.save(null));
        assertThrows(IllegalArgumentException.class, () -> vehiculoService.save(new Vehiculo()));

        MermaRepository mermas = mock(MermaRepository.class);
        StockService stock = mock(StockService.class);
        MermaService mermaService = new MermaService(mermas, stock);
        Merma merma = new Merma();
        Articulo art = new Articulo();
        art.setId(7L);
        merma.setArticulo(art);
        merma.setCantidad(BigDecimal.ONE);
        merma.setMotivo("Rotura");
        merma.setId(8L);
        when(mermas.save(any())).thenAnswer(i -> i.getArgument(0));
        assertSame(merma, mermaService.save(merma));
        verify(stock).registrarSalida(7L, null, BigDecimal.ONE, "Merma Rotura", "MERMA", 8L);
        Merma invalida = new Merma();
        assertThrows(IllegalArgumentException.class, () -> mermaService.save(invalida));
        invalida.setArticulo(art);
        invalida.setCantidad(BigDecimal.ZERO);
        assertThrows(IllegalArgumentException.class, () -> mermaService.save(invalida));

        HorneadaRepository horneadas = mock(HorneadaRepository.class);
        HorneadaService horneadaService = new HorneadaService(horneadas);
        Horneada horneada = new Horneada();
        when(horneadas.save(horneada)).thenReturn(horneada);
        assertSame(horneada, horneadaService.save(horneada));
        assertThrows(IllegalArgumentException.class, () -> horneadaService.save(null));
        horneadaService.findAll();
        horneadaService.findById(1L);
        horneadaService.findByOrdenProduccionId(1L);
        horneadaService.findByFecha(LocalDate.now());
        horneadaService.findByFechaBetween(LocalDate.now(), LocalDate.now());
        horneadaService.buscar("x");
        horneadaService.deleteById(1L);
    }

    @Test
    void importacionProcesaFilasValidasYReportaErrores() {
        ImportService service = new ImportService();
        ClienteService clientes = mock(ClienteService.class);
        ArticuloService articulos = mock(ArticuloService.class);
        when(clientes.save(any())).thenAnswer(i -> i.getArgument(0));
        when(articulos.save(any())).thenAnswer(i -> i.getArgument(0));

        MockMultipartFile csvClientes = new MockMultipartFile("file", "clientes.csv", "text/csv",
                "codigo;nombre;cif;telefono;email;direccion;poblacion\nC1;Cliente;B1;1;a@b.es;Calle;Alicante\nsolo\n".getBytes());
        ImportService.ImportResult rc = service.importarClientes(csvClientes, clientes);
        assertEquals(1, rc.ok());

        MockMultipartFile csvArticulos = new MockMultipartFile("file", "articulos.csv", "text/csv",
                "codigo;nombre;pvp;iva;categoria;alergenos\nA1;Pan;1,50;4;Pan;Gluten\nA2;Mal;xx;4;;\n".getBytes());
        ImportService.ImportResult ra = service.importarArticulos(csvArticulos, articulos);
        assertEquals(1, ra.ok());
        assertFalse(ra.mensajes().isEmpty());
    }

    private Cliente cliente(Long id, String codigo, String cif) {
        Cliente c = new Cliente();
        c.setId(id);
        c.setCodigo(codigo);
        c.setCif(cif);
        c.setNombre("Panadería");
        c.setActivo(true);
        return c;
    }

    private Proveedor proveedor(Long id, String codigo, String cif) {
        Proveedor p = new Proveedor();
        p.setId(id);
        p.setCodigo(codigo);
        p.setCif(cif);
        p.setNombre("Proveedor");
        p.setActivo(true);
        return p;
    }
}
