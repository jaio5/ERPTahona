package alicanteweb.erp.service;

import alicanteweb.erp.entities.Articulo;
import alicanteweb.erp.repository.ArticuloRepository;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ArticuloServiceTest {

    @Test
    void normalizaDatosYAplicaValoresPorDefectoAlCrear() {
        ArticuloRepository repository = mock(ArticuloRepository.class);
        when(repository.findByCodigo("PAN-01")).thenReturn(Optional.empty());
        when(repository.save(any(Articulo.class))).thenAnswer(invocation -> invocation.getArgument(0));
        ArticuloService service = new ArticuloService(repository);

        Articulo articulo = new Articulo();
        articulo.setCodigo(" PAN-01 ");
        articulo.setNombre(" Pan rústico ");

        Articulo saved = service.save(articulo);

        assertEquals("PAN-01", saved.getCodigo());
        assertEquals("Pan rústico", saved.getNombre());
        assertEquals("Pan rústico", saved.getDescripcion());
        assertEquals(BigDecimal.ZERO, saved.getPvp());
        assertEquals(BigDecimal.ZERO, saved.getCoste());
        assertEquals(BigDecimal.ZERO, saved.getStock());
        assertEquals(BigDecimal.ZERO, saved.getIva());
        assertTrue(saved.getActivo());
    }
}
