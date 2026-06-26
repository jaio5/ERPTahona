package alicanteweb.erp.service;

import alicanteweb.erp.entities.*;
import alicanteweb.erp.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificacionServiceTest {

    @Mock PresupuestoRepository presupuestoRepository;
    @Mock ArticuloRepository articuloRepository;
    @Mock FacturaRepository facturaRepository;
    @Mock LoteRepository loteRepository;
    @Mock OrdenProduccionRepository ordenProduccionRepository;
    NotificacionService service;

    @BeforeEach
    void setUp() {
        service = new NotificacionService(presupuestoRepository, articuloRepository,
                facturaRepository, loteRepository, ordenProduccionRepository);
    }

    @Test
    void verificaPresupuestosProximosYDescartaEstadosFinales() {
        Presupuesto vigente = new Presupuesto();
        vigente.setNumero("P-1");
        vigente.setEstado("ENVIADO");
        vigente.setFechaValidez(LocalDate.now().plusDays(3));
        Cliente cliente = new Cliente();
        cliente.setNombre("Ana");
        vigente.setCliente(cliente);
        Presupuesto rechazado = new Presupuesto();
        rechazado.setEstado("RECHAZADO");
        Presupuesto facturado = new Presupuesto();
        facturado.setEstado("FACTURADO");
        when(presupuestoRepository.findByFechaValidezBetween(any(), any()))
                .thenReturn(List.of(vigente, rechazado, facturado));

        service.verificarPresupuestosPorCaducar();

        verify(presupuestoRepository).findByFechaValidezBetween(
                LocalDate.now(), LocalDate.now().plusDays(7));
    }

    @Test
    void verificaStockBajoIgnorandoDatosIncompletos() {
        Articulo bajo = articulo("Harina", "2", "5");
        when(articuloRepository.findConStockBajo()).thenReturn(List.of(bajo));

        service.verificarStockBajo();

        verify(articuloRepository).findConStockBajo();
    }

    @Test
    void verificaFacturasVencidasYAcumulaTotalesInclusoNulos() {
        Factura vencida = factura("F-1", "EMITIDA", LocalDate.now().minusDays(40), BigDecimal.TEN);
        Cliente cliente = new Cliente();
        cliente.setNombre("Cliente");
        vencida.setCliente(cliente);
        Factura sinTotal = factura("F-2", "PENDIENTE", LocalDate.now().minusDays(31), null);
        sinTotal.setCliente(cliente);
        when(facturaRepository.findPendientesCobro(any()))
                .thenReturn(List.of(vencida, sinTotal));

        service.verificarFacturasPendientesPago();

        verify(facturaRepository).findPendientesCobro(any());
    }

    @Test
    void generaResumenSemanalSumandoFacturasConTotalNulo() {
        Factura primera = factura("F-1", "EMITIDA", LocalDate.now(), new BigDecimal("12.50"));
        Factura segunda = factura("F-2", "EMITIDA", LocalDate.now(), null);
        when(facturaRepository.findByFechaBetween(any(), any()))
                .thenReturn(List.of(primera, segunda));

        service.resumenSemanal();

        verify(facturaRepository, times(1)).findByFechaBetween(
                LocalDate.now().minusDays(7), LocalDate.now());
    }

    @Test
    void resumenCuentaCadaCategoriaYProcesaLotesYOrdenes() {
        Presupuesto presupuesto = new Presupuesto();
        presupuesto.setEstado("ENVIADO");
        Articulo bajo = articulo("Harina", "1", "2");
        Factura vencida = factura("F-1", "PENDIENTE", LocalDate.now().minusDays(40), BigDecimal.TEN);
        Lote lote = new Lote();
        lote.setCodigo("L-1");
        lote.setFechaCaducidad(LocalDate.now().plusDays(2));
        Articulo articulo = new Articulo();
        articulo.setNombre("Pan");
        lote.setArticulo(articulo);
        OrdenProduccion orden = new OrdenProduccion();
        orden.setNumero("OP-1");

        when(presupuestoRepository.findByFechaValidezBetween(any(), any()))
                .thenReturn(List.of(presupuesto));
        when(articuloRepository.findConStockBajo()).thenReturn(List.of(bajo));
        when(facturaRepository.findPendientesCobro(any())).thenReturn(List.of(vencida));
        when(loteRepository.findByFechaCaducidadBetween(any(), any())).thenReturn(List.of(lote));
        when(ordenProduccionRepository.findByEstado("PLANIFICADA")).thenReturn(List.of(orden));

        NotificacionService.NotificacionResumen resumen = service.obtenerResumenNotificaciones();

        assertEquals(1, resumen.presupuestosPorCaducar());
        assertEquals(1, resumen.articulosStockBajo());
        assertEquals(1, resumen.facturasPendientes());
        assertEquals(1, resumen.lotesPorCaducar());
        assertEquals(1, resumen.ordenesPendientes());
        assertEquals(3, resumen.totalNotificaciones());

        service.verificarLotesProximosACaducar();
        service.verificarOrdenesPendientes();
        verify(loteRepository, times(2)).findByFechaCaducidadBetween(any(), any());
        verify(ordenProduccionRepository, times(2)).findByEstado("PLANIFICADA");
    }

    @Test
    void verificacionesVaciasNoGeneranErrores() {
        when(presupuestoRepository.findByFechaValidezBetween(any(), any())).thenReturn(List.of());
        when(articuloRepository.findConStockBajo()).thenReturn(List.of());
        when(facturaRepository.findPendientesCobro(any())).thenReturn(List.of());
        when(loteRepository.findByFechaCaducidadBetween(any(), any())).thenReturn(List.of());
        when(ordenProduccionRepository.findByEstado("PLANIFICADA")).thenReturn(List.of());

        service.verificarPresupuestosPorCaducar();
        service.verificarStockBajo();
        service.verificarFacturasPendientesPago();
        service.verificarLotesProximosACaducar();
        service.verificarOrdenesPendientes();
    }

    private Articulo articulo(String nombre, String stock, String minimo) {
        Articulo articulo = new Articulo();
        articulo.setNombre(nombre);
        articulo.setStock(new BigDecimal(stock));
        articulo.setStockMinimo(new BigDecimal(minimo));
        return articulo;
    }

    private Factura factura(String numero, String estado, LocalDate fecha, BigDecimal total) {
        Factura factura = new Factura();
        factura.setNumero(numero);
        factura.setEstado(estado);
        factura.setFecha(fecha);
        factura.setTotal(total);
        return factura;
    }
}
