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

    @Test
    void guardarInicializaNumeroYFecha() {
        when(repository.count()).thenReturn(2L);
        when(repository.save(any())).thenAnswer(i -> i.getArgument(0));
        Presupuesto p = service.guardar(new Presupuesto());
        assertTrue(p.getNumero().startsWith("PRE-" + LocalDate.now().getYear()));
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
        when(repository.count()).thenReturn(5L);
        when(repository.save(any())).thenAnswer(i -> i.getArgument(0));

        Presupuesto copia = service.duplicar(1L);
        assertEquals("BORRADOR", copia.getEstado());
        assertEquals(LocalDate.now().plusDays(30), copia.getFechaValidez());
        assertEquals(1, copia.getLineas().size());
        assertSame(copia, copia.getLineas().get(0).getPresupuesto());
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
