package alicanteweb.erp.service;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import alicanteweb.erp.entities.Almacen;
import alicanteweb.erp.entities.Articulo;
import alicanteweb.erp.entities.ArticuloAlmacen;
import alicanteweb.erp.entities.MovimientoStock;
import alicanteweb.erp.repository.AlmacenRepository;
import alicanteweb.erp.repository.ArticuloAlmacenRepository;
import alicanteweb.erp.repository.ArticuloRepository;
import alicanteweb.erp.repository.MovimientoStockRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StockServiceTest {

    @Mock
    MovimientoStockRepository movimientoStockRepository;

    @Mock
    ArticuloRepository articuloRepository;

    @Mock
    AlmacenRepository almacenRepository;

    @Mock
    ArticuloAlmacenRepository articuloAlmacenRepository;

    @InjectMocks
    StockService service;

    @Test
    void registrarSalidaActualizaStockYAsignaAlmacenOrigen() {
        Articulo articulo = articulo(10L, "8.00");
        Almacen almacen = new Almacen();
        almacen.setId(4L);

        // El almacén tiene suficiente stock propio
        ArticuloAlmacen aa = new ArticuloAlmacen();
        aa.setStock(new BigDecimal("8.00"));
        when(articuloRepository.findByIdForUpdate(10L)).thenReturn(Optional.of(articulo));
        when(almacenRepository.getReferenceById(4L)).thenReturn(almacen);
        when(articuloAlmacenRepository.findByArticuloIdAndAlmacenId(10L, 4L)).thenReturn(Optional.of(aa));

        service.registrarSalida(10L, 4L, new BigDecimal("3.00"),
                "Entrega", "ALBARAN", 20L);

        assertEquals(new BigDecimal("5.00"), articulo.getStock());
        ArgumentCaptor<MovimientoStock> captor = ArgumentCaptor.forClass(MovimientoStock.class);
        verify(movimientoStockRepository).save(captor.capture());
        assertSame(almacen, captor.getValue().getAlmacenOrigen());
        assertNull(captor.getValue().getAlmacenDestino());
        assertEquals("SALIDA", captor.getValue().getTipo());
    }

    @Test
    void registrarEntradaActualizaStockYAsignaAlmacenDestino() {
        Articulo articulo = articulo(10L, "8.00");
        Almacen almacen = new Almacen();
        almacen.setId(4L);
        when(articuloRepository.findByIdForUpdate(10L)).thenReturn(Optional.of(articulo));
        when(almacenRepository.getReferenceById(4L)).thenReturn(almacen);

        service.registrarEntrada(10L, 4L, new BigDecimal("3.00"),
                "Recepcion", "RECEPCION", 20L);

        assertEquals(new BigDecimal("11.00"), articulo.getStock());
        ArgumentCaptor<MovimientoStock> captor = ArgumentCaptor.forClass(MovimientoStock.class);
        verify(movimientoStockRepository).save(captor.capture());
        assertSame(almacen, captor.getValue().getAlmacenDestino());
        assertNull(captor.getValue().getAlmacenOrigen());
        assertEquals("ENTRADA", captor.getValue().getTipo());
    }

    @Test
    void registrarSalidaRechazaStockInsuficienteSinPersistir() {
        Articulo articulo = articulo(10L, "2.00");
        when(articuloRepository.findByIdForUpdate(10L)).thenReturn(Optional.of(articulo));

        assertThrows(IllegalStateException.class, () ->
                service.registrarSalida(10L, null, new BigDecimal("3.00"),
                        "Entrega", "ALBARAN", 20L));

        assertEquals(new BigDecimal("2.00"), articulo.getStock());
        verify(articuloRepository, never()).save(any());
        verify(movimientoStockRepository, never()).save(any());
    }

    // ===== Stock por almacén: el desglose no bloquea una salida que el stock global cubre =====

    private ListAppender<ILoggingEvent> capturarLog() {
        ListAppender<ILoggingEvent> appender = new ListAppender<>();
        appender.start();
        ((Logger) LoggerFactory.getLogger(StockService.class)).addAppender(appender);
        return appender;
    }

    private void soltarLog(ListAppender<ILoggingEvent> appender) {
        ((Logger) LoggerFactory.getLogger(StockService.class)).detachAppender(appender);
    }

    private List<ILoggingEvent> warns(ListAppender<ILoggingEvent> appender) {
        return appender.list.stream().filter(e -> e.getLevel() == Level.WARN).toList();
    }

    @Test
    void salidaSinFilaDeAlmacenDescuentaGlobalYDejaAlmacenACeroConAviso() {
        // Stock cargado desde la ficha: 50 globales y ninguna fila ArticuloAlmacen
        Articulo articulo = articulo(10L, "50");
        Almacen almacen = new Almacen();
        almacen.setId(4L);
        when(articuloRepository.findByIdForUpdate(10L)).thenReturn(Optional.of(articulo));
        when(almacenRepository.getReferenceById(4L)).thenReturn(almacen);
        when(articuloAlmacenRepository.findByArticuloIdAndAlmacenId(10L, 4L)).thenReturn(Optional.empty());

        ListAppender<ILoggingEvent> log = capturarLog();
        try {
            assertDoesNotThrow(() -> service.registrarSalida(10L, 4L, new BigDecimal("5"),
                    "Entrega", "ALBARAN", 20L));
        } finally {
            soltarLog(log);
        }

        assertEquals(new BigDecimal("45"), articulo.getStock());
        ArgumentCaptor<ArticuloAlmacen> aa = ArgumentCaptor.forClass(ArticuloAlmacen.class);
        verify(articuloAlmacenRepository).save(aa.capture());
        assertEquals(0, aa.getValue().getStock().compareTo(BigDecimal.ZERO));
        verify(movimientoStockRepository).save(any(MovimientoStock.class));
        List<ILoggingEvent> avisos = warns(log);
        assertEquals(1, avisos.size());
        String msg = avisos.get(0).getFormattedMessage();
        assertTrue(msg.contains("almacén 4") && msg.contains("artículo 10") && msg.contains("5"), msg);
    }

    @Test
    void salidaQueDejariaElAlmacenEnNegativoLoAjustaACero() {
        Articulo articulo = articulo(10L, "50");
        ArticuloAlmacen aa = new ArticuloAlmacen();
        aa.setStock(new BigDecimal("2"));
        when(articuloRepository.findByIdForUpdate(10L)).thenReturn(Optional.of(articulo));
        when(almacenRepository.getReferenceById(4L)).thenReturn(new Almacen());
        when(articuloAlmacenRepository.findByArticuloIdAndAlmacenId(10L, 4L)).thenReturn(Optional.of(aa));

        ListAppender<ILoggingEvent> log = capturarLog();
        try {
            service.registrarSalida(10L, 4L, new BigDecimal("5"), "Entrega", "ALBARAN", 20L);
        } finally {
            soltarLog(log);
        }

        assertEquals(new BigDecimal("45"), articulo.getStock());
        assertEquals(0, aa.getStock().compareTo(BigDecimal.ZERO));
        verify(articuloAlmacenRepository).save(aa);
        assertEquals(1, warns(log).size());
    }

    @Test
    void salidaConStockDeAlmacenSuficienteNoAvisa() {
        Articulo articulo = articulo(10L, "50");
        ArticuloAlmacen aa = new ArticuloAlmacen();
        aa.setStock(new BigDecimal("8"));
        when(articuloRepository.findByIdForUpdate(10L)).thenReturn(Optional.of(articulo));
        when(almacenRepository.getReferenceById(4L)).thenReturn(new Almacen());
        when(articuloAlmacenRepository.findByArticuloIdAndAlmacenId(10L, 4L)).thenReturn(Optional.of(aa));

        ListAppender<ILoggingEvent> log = capturarLog();
        try {
            service.registrarSalida(10L, 4L, new BigDecimal("5"), "Entrega", "ALBARAN", 20L);
        } finally {
            soltarLog(log);
        }

        assertEquals(new BigDecimal("3"), aa.getStock());
        assertTrue(warns(log).isEmpty());
    }

    @Test
    void elControlDelStockGlobalNoCambiaAunqueElAlmacenTengaStock() {
        // Global 3 y entrega de 5: sigue fallando aunque el almacén diga tener más
        Articulo articulo = articulo(10L, "3");
        when(articuloRepository.findByIdForUpdate(10L)).thenReturn(Optional.of(articulo));

        IllegalStateException e = assertThrows(IllegalStateException.class, () ->
                service.registrarSalida(10L, 4L, new BigDecimal("5"), "Entrega", "ALBARAN", 20L));

        assertTrue(e.getMessage().startsWith("Stock insuficiente"), e.getMessage());
        assertEquals(new BigDecimal("3"), articulo.getStock());
        verify(articuloRepository, never()).save(any());
        verify(articuloAlmacenRepository, never()).save(any());
        verify(movimientoStockRepository, never()).save(any());
    }

    private Articulo articulo(Long id, String stock) {
        Articulo articulo = new Articulo();
        articulo.setId(id);
        articulo.setNombre("Pan");
        articulo.setStock(new BigDecimal(stock));
        return articulo;
    }
}
