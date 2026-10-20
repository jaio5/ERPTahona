package alicanteweb.erp.service;

import alicanteweb.erp.entities.Articulo;
import alicanteweb.erp.entities.Devolucion;
import alicanteweb.erp.entities.DevolucionLinea;
import alicanteweb.erp.repository.AlmacenRepository;
import alicanteweb.erp.repository.ArticuloAlmacenRepository;
import alicanteweb.erp.repository.ArticuloRepository;
import alicanteweb.erp.repository.DevolucionLineaRepository;
import alicanteweb.erp.repository.DevolucionRepository;
import alicanteweb.erp.repository.MovimientoStockRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Aceptar y rechazar una devolución solo proceden desde PENDIENTE: repetir la aceptación
 * no debe reponer el stock dos veces.
 */
@ExtendWith(MockitoExtension.class)
class DevolucionServiceTest {

    @Mock DevolucionRepository devolucionRepository;
    @Mock DevolucionLineaRepository lineaRepository;
    @Mock ArticuloService articuloService;
    @Mock LoteService loteService;
    @Mock MovimientoStockRepository movimientoStockRepository;
    @Mock ArticuloRepository articuloRepository;
    @Mock AlmacenRepository almacenRepository;
    @Mock ArticuloAlmacenRepository articuloAlmacenRepository;

    DevolucionService service;
    Articulo articulo;
    Devolucion devolucion;

    @BeforeEach
    void setUp() {
        // StockService real (con repositorios simulados) para comprobar el stock resultante
        StockService stockService = new StockService(movimientoStockRepository, articuloRepository,
                almacenRepository, articuloAlmacenRepository);
        service = new DevolucionService(devolucionRepository, lineaRepository, articuloService,
                loteService, stockService);

        articulo = new Articulo();
        articulo.setId(10L);
        articulo.setCodigo("BARRA");
        articulo.setNombre("Barra");
        articulo.setStock(BigDecimal.ZERO);

        devolucion = new Devolucion();
        devolucion.setId(1L);
        devolucion.setNumero("DEV-1");
        DevolucionLinea linea = new DevolucionLinea();
        linea.setDevolucion(devolucion);
        linea.setArticulo(articulo);
        linea.setCantidad(new BigDecimal("5"));
        devolucion.getLineas().add(linea);
    }

    @Test
    void laDevolucionNuevaEstaPendiente() {
        assertEquals("PENDIENTE", new Devolucion().getEstado());
    }

    @Test
    void aceptarDosVecesSoloReponeElStockUnaVez() {
        when(devolucionRepository.findById(1L)).thenReturn(Optional.of(devolucion));
        when(devolucionRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        when(articuloRepository.findByIdForUpdate(10L)).thenReturn(Optional.of(articulo));

        assertEquals("ACEPTADA", service.aceptarDevolucion(1L).getEstado());
        assertEquals(0, articulo.getStock().compareTo(new BigDecimal("5")));

        IllegalStateException e = assertThrows(IllegalStateException.class,
                () -> service.aceptarDevolucion(1L));

        assertEquals("La devolución ya está en estado ACEPTADA", e.getMessage());
        assertEquals(0, articulo.getStock().compareTo(new BigDecimal("5")), "No debe sumar 10");
        verify(articuloRepository, times(1)).findByIdForUpdate(10L);
        verify(movimientoStockRepository, times(1)).save(any());
        verify(devolucionRepository, times(1)).save(any());
    }

    @Test
    void rechazarUnaDevolucionAceptadaLanza() {
        devolucion.setEstado("ACEPTADA");
        devolucion.setObservaciones("original");
        when(devolucionRepository.findById(1L)).thenReturn(Optional.of(devolucion));

        IllegalStateException e = assertThrows(IllegalStateException.class,
                () -> service.rechazarDevolucion(1L, "Producto en mal estado"));

        assertEquals("La devolución ya está en estado ACEPTADA", e.getMessage());
        assertEquals("ACEPTADA", devolucion.getEstado());
        assertEquals("original", devolucion.getObservaciones());
        verify(devolucionRepository, never()).save(any());
    }

    @Test
    void aceptarUnaRechazadaLanzaSinTocarStock() {
        devolucion.setEstado("RECHAZADA");
        when(devolucionRepository.findById(1L)).thenReturn(Optional.of(devolucion));

        assertThrows(IllegalStateException.class, () -> service.aceptarDevolucion(1L));

        verifyNoInteractions(articuloRepository, movimientoStockRepository);
        verify(devolucionRepository, never()).save(any());
    }

    @Test
    void rechazarUnaPendienteFunciona() {
        when(devolucionRepository.findById(1L)).thenReturn(Optional.of(devolucion));
        when(devolucionRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        Devolucion rechazada = service.rechazarDevolucion(1L, "Fuera de plazo");

        assertEquals("RECHAZADA", rechazada.getEstado());
        assertEquals("Fuera de plazo", rechazada.getObservaciones());
        verifyNoInteractions(articuloRepository);
    }
}
