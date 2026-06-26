package alicanteweb.erp.service;

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

import java.math.BigDecimal;
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

    private Articulo articulo(Long id, String stock) {
        Articulo articulo = new Articulo();
        articulo.setId(id);
        articulo.setNombre("Pan");
        articulo.setStock(new BigDecimal(stock));
        return articulo;
    }
}
