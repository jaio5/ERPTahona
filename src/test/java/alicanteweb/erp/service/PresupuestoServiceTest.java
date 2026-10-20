package alicanteweb.erp.service;

import alicanteweb.erp.entities.*;
import alicanteweb.erp.repository.PresupuestoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PresupuestoServiceTest {

    @Mock PresupuestoRepository repository;
    @Mock FacturaService facturaService;
    PresupuestoService service;

    @BeforeEach
    void setUp() {
        service = new PresupuestoService(repository, facturaService);
    }

    private static final String PREFIJO = "PRE-" + LocalDate.now().getYear() + "-";

    @Test
    void guardarInicializaNumeroYFecha() {
        when(repository.maxNumeroConPrefijo(PREFIJO)).thenReturn(PREFIJO + "00002");
        when(repository.save(any())).thenAnswer(i -> i.getArgument(0));
        Presupuesto p = service.guardar(new Presupuesto());
        assertEquals(PREFIJO + "00003", p.getNumero());
        assertEquals(LocalDate.now(), p.getFecha());
    }

    @Test
    void consultasActualizacionEstadosYEliminacionDelegan() {
        Presupuesto p = presupuesto();
        when(repository.findAllOrdenados()).thenReturn(List.of(p));
        when(repository.findById(1L)).thenReturn(Optional.of(p));
        when(repository.findByNumero("PRE-1")).thenReturn(Optional.of(p));
        when(repository.findByClienteId(2L)).thenReturn(List.of(p));
        when(repository.findByEstado("BORRADOR")).thenReturn(List.of(p));
        when(repository.buscar("x")).thenReturn(List.of(p));
        when(repository.save(any())).thenAnswer(i -> i.getArgument(0));

        assertEquals(1, service.obtenerTodos().size());
        assertTrue(service.obtenerPorId(1L).isPresent());
        assertTrue(service.obtenerPorNumero("PRE-1").isPresent());
        assertEquals(1, service.buscarPorCliente(2L).size());
        assertEquals(1, service.buscarPorEstado("BORRADOR").size());
        assertEquals(1, service.buscar("x").size());

        Presupuesto cambios = presupuesto();
        cambios.setEstado("ENVIADO");
        cambios.setObservaciones("nueva");
        assertEquals("ENVIADO", service.actualizar(1L, cambios).getEstado());
        assertEquals("ACEPTADO", service.aceptar(1L).getEstado());
        assertEquals("RECHAZADO", service.rechazar(1L).getEstado());
        service.eliminar(1L);
        verify(repository).deleteById(1L);
    }

    @Test
    void operacionesRechazanIdInexistente() {
        assertThrows(RuntimeException.class, () -> service.actualizar(1L, new Presupuesto()));
        assertThrows(RuntimeException.class, () -> service.cambiarEstado(1L, "X"));
        assertThrows(IllegalArgumentException.class, () -> service.convertirAFactura(1L, null));
        assertThrows(IllegalArgumentException.class, () -> service.duplicar(1L));
    }

    @Test
    void detectaCaducidad() {
        Presupuesto p = presupuesto();
        p.setFechaValidez(null);
        assertFalse(service.estaCaducado(p));
        p.setFechaValidez(LocalDate.now().minusDays(1));
        assertTrue(service.estaCaducado(p));
        p.setFechaValidez(LocalDate.now().plusDays(1));
        assertFalse(service.estaCaducado(p));
    }

    @Test
    void convertirAFacturaCalculaDescuentoEIva() {
        Presupuesto p = presupuesto();
        p.setEstado("ACEPTADO");
        p.getLineas().add(linea("2", "10", "10", "21"));
        Factura facturaGuardada = new Factura();
        facturaGuardada.setNumero("F-1");
        when(repository.findById(1L)).thenReturn(Optional.of(p));
        when(facturaService.save(any())).thenReturn(facturaGuardada);

        Factura result = service.convertirAFactura(1L, null);

        assertSame(facturaGuardada, result);
        assertEquals("FACTURADO", p.getEstado());
        var captor = org.mockito.ArgumentCaptor.forClass(Factura.class);
        verify(facturaService).save(captor.capture());
        Factura creada = captor.getValue();
        assertEquals(new BigDecimal("18.00"), creada.getBaseImponible());
        assertEquals(new BigDecimal("3.78"), creada.getTotalIva());
        assertEquals(new BigDecimal("21.78"), creada.getTotal());
    }

    @Test
    void convertirRechazaFacturadoORechazado() {
        Presupuesto p = presupuesto();
        when(repository.findById(1L)).thenReturn(Optional.of(p));
        p.setEstado("FACTURADO");
        assertThrows(IllegalStateException.class, () -> service.convertirAFactura(1L, null));
        p.setEstado("RECHAZADO");
        assertThrows(IllegalStateException.class, () -> service.convertirAFactura(1L, null));
    }

    @Test
    void duplicarCopiaLineasYRenuevaValidez() {
        Presupuesto original = presupuesto();
        original.getLineas().add(linea("1", "10", "0", "21"));
        when(repository.findById(1L)).thenReturn(Optional.of(original));
        when(repository.maxNumeroConPrefijo(PREFIJO)).thenReturn(PREFIJO + "00005");
        when(repository.save(any())).thenAnswer(i -> i.getArgument(0));

        Presupuesto copia = service.duplicar(1L);
        assertEquals("BORRADOR", copia.getEstado());
        assertEquals(PREFIJO + "00006", copia.getNumero());
        assertEquals(LocalDate.now().plusDays(30), copia.getFechaValidez());
        assertEquals(1, copia.getLineas().size());
        assertSame(copia, copia.getLineas().get(0).getPresupuesto());
    }

    @Test
    void duplicarPresupuestoAprobadoQuedaEnBorradorConTotalRecalculadoYSinFactura() {
        Presupuesto original = presupuesto();
        original.setEstado("ACEPTADO");
        original.setFecha(LocalDate.now().minusDays(20));
        original.setTotal(new BigDecimal("999.99")); // total almacenado desfasado
        original.setFactura(new Factura());
        // 2 x 10 = 20 - 10 % = 18,00 + IVA 21 % (3,78) = 21,78
        original.getLineas().add(linea("2", "10", "10", "21"));
        // 3 x 1,50 = 4,50 + IVA 10 % (0,45) = 4,95
        original.getLineas().add(linea("3", "1.50", "0", "10"));
        when(repository.findById(1L)).thenReturn(Optional.of(original));
        when(repository.maxNumeroConPrefijo(PREFIJO)).thenReturn(null);
        when(repository.save(any())).thenAnswer(i -> i.getArgument(0));

        Presupuesto copia = service.duplicar(1L);

        assertEquals("BORRADOR", copia.getEstado());
        assertEquals(PREFIJO + "00001", copia.getNumero());
        assertEquals(LocalDate.now(), copia.getFecha());
        assertSame(original.getCliente(), copia.getCliente());
        assertNull(copia.getFactura());
        assertEquals(0, new BigDecimal("26.73").compareTo(copia.getTotal()));
        assertEquals(new BigDecimal("18.00"), copia.getLineas().get(0).getImporte());
        assertEquals(new BigDecimal("4.50"), copia.getLineas().get(1).getImporte());
        assertEquals("ACEPTADO", original.getEstado());
        verifyNoInteractions(facturaService);
    }

    // ---------- Numeración PRE-AAAA-NNNNN ----------

    @Test
    void generarNumeroPresupuestoEsPublicoYSinParametros() throws Exception {
        var metodo = PresupuestoService.class.getMethod("generarNumeroPresupuesto");
        assertTrue(java.lang.reflect.Modifier.isPublic(metodo.getModifiers()));
        assertEquals(String.class, metodo.getReturnType());
    }

    @Test
    void numeracionTrasBorradoIntermedioUsaElMaximoYNoElRecuento() {
        // Existen 00001, 00002, 00004 y 00005 (el 00003 se borró)
        lenient().when(repository.count()).thenReturn(4L);
        when(repository.maxNumeroConPrefijo(PREFIJO)).thenReturn(PREFIJO + "00005");

        assertEquals(PREFIJO + "00006", service.generarNumeroPresupuesto());
    }

    @Test
    void numeracionReiniciaAlCambiarDeEjercicio() {
        // Solo hay presupuestos del año anterior
        when(repository.maxNumeroConPrefijo(PREFIJO)).thenReturn(null);

        assertEquals(PREFIJO + "00001", service.generarNumeroPresupuesto());
        verify(repository, never()).numerosConPrefijo(any());
    }

    @Test
    void numeracionIgnoraSufijosNoNumericos() {
        when(repository.maxNumeroConPrefijo(PREFIJO)).thenReturn(PREFIJO + "BIS");
        when(repository.numerosConPrefijo(PREFIJO)).thenReturn(
                List.of(PREFIJO + "00011", PREFIJO + "BIS", PREFIJO + "00003"));

        assertEquals(PREFIJO + "00012", service.generarNumeroPresupuesto());
    }

    @Test
    void marcarCaducadosSoloActualizaPendientesVencidos() {
        Presupuesto vencido = presupuesto();
        vencido.setFechaValidez(LocalDate.now().minusDays(1));
        Presupuesto vigente = presupuesto();
        vigente.setId(2L);
        vigente.setFechaValidez(LocalDate.now().plusDays(1));
        Presupuesto sinFecha = presupuesto();
        sinFecha.setId(3L);
        sinFecha.setFechaValidez(null);
        when(repository.findByEstado("PENDIENTE")).thenReturn(List.of(vencido, vigente, sinFecha));

        assertEquals(1, service.marcarCaducados());
        assertEquals("CADUCADO", vencido.getEstado());
        verify(repository, times(1)).save(any());
    }

    private Presupuesto presupuesto() {
        Presupuesto p = new Presupuesto();
        p.setId(1L);
        p.setNumero("PRE-1");
        p.setFecha(LocalDate.now());
        p.setEstado("BORRADOR");
        p.setTotal(new BigDecimal("100"));
        Cliente cliente = new Cliente();
        cliente.setId(2L);
        p.setCliente(cliente);
        return p;
    }

    private PresupuestoLinea linea(String cantidad, String precio, String descuento, String iva) {
        PresupuestoLinea linea = new PresupuestoLinea();
        linea.setCantidad(new BigDecimal(cantidad));
        linea.setPrecioUnitario(new BigDecimal(precio));
        linea.setDescuento(new BigDecimal(descuento));
        linea.setTipoIva(new BigDecimal(iva));
        linea.setImporte(BigDecimal.ZERO);
        linea.setArticulo(new Articulo());
        return linea;
    }
}
