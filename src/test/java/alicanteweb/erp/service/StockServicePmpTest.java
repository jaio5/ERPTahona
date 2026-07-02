package alicanteweb.erp.service;

import alicanteweb.erp.entities.Articulo;
import alicanteweb.erp.repository.AlmacenRepository;
import alicanteweb.erp.repository.ArticuloAlmacenRepository;
import alicanteweb.erp.repository.ArticuloRepository;
import alicanteweb.erp.repository.MovimientoStockRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StockServicePmpTest {

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

    private Articulo articulo(BigDecimal stock, BigDecimal costeMedio, BigDecimal coste) {
        Articulo a = new Articulo();
        a.setId(1L);
        a.setCodigo("HAR-01");
        a.setNombre("Harina");
        a.setStock(stock);
        a.setCosteMedio(costeMedio);
        a.setCoste(coste);
        return a;
    }

    @Test
    void entradaConCoste_calculaPmpPonderado() {
        // 10 uds a PMP 1.00 + 10 uds a 2.00 → PMP 1.50
        Articulo art = articulo(new BigDecimal("10"), new BigDecimal("1.0000"), null);
        when(articuloRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(art));

        service.registrarEntradaConCoste(1L, null, new BigDecimal("10"), new BigDecimal("2.00"),
                "Recepción", "RECEPCION", 1L);

        assertEquals(0, art.getCosteMedio().compareTo(new BigDecimal("1.5000")));
        assertEquals(0, art.getStock().compareTo(new BigDecimal("20")));
    }

    @Test
    void entradaConCoste_sinPmpPrevioUsaCosteEstandar() {
        Articulo art = articulo(new BigDecimal("5"), null, new BigDecimal("3.00"));
        when(articuloRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(art));

        // 5 uds a 3.00 (coste estándar) + 5 uds a 5.00 → PMP 4.00
        service.registrarEntradaConCoste(1L, null, new BigDecimal("5"), new BigDecimal("5.00"),
                "Recepción", "RECEPCION", 1L);

        assertEquals(0, art.getCosteMedio().compareTo(new BigDecimal("4.0000")));
    }

    @Test
    void entradaSinPrecio_noTocaElPmp() {
        Articulo art = articulo(new BigDecimal("5"), null, null);
        when(articuloRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(art));

        service.registrarEntrada(1L, null, new BigDecimal("2"), "Producción", "ORDEN", 9L);

        assertNull(art.getCosteMedio());
        assertEquals(0, art.getStock().compareTo(new BigDecimal("7")));
    }

    @Test
    void entradaConStockCero_pmpEsElPrecioDeEntrada() {
        Articulo art = articulo(BigDecimal.ZERO, null, null);
        when(articuloRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(art));

        service.registrarEntradaConCoste(1L, null, new BigDecimal("8"), new BigDecimal("2.50"),
                "Recepción", "RECEPCION", 2L);

        assertEquals(0, art.getCosteMedio().compareTo(new BigDecimal("2.5000")));
    }
}
