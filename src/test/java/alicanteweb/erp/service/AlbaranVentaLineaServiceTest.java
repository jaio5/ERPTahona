package alicanteweb.erp.service;

import alicanteweb.erp.entities.AlbaranVentaLinea;
import alicanteweb.erp.repository.AlbaranVentaLineaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AlbaranVentaLineaServiceTest {

    @Mock
    AlbaranVentaLineaRepository repository;

    @InjectMocks
    AlbaranVentaLineaService service;

    @BeforeEach
    void setUp() {
    }

    @Test
    void findByAlbaranId_llamaRepositorio_y_retornaLista() {
        Long albaranId = 123L;
        AlbaranVentaLinea l1 = new AlbaranVentaLinea();
        l1.setId(1L);
        when(repository.findByAlbaranIdWithArticulo(albaranId)).thenReturn(List.of(l1));

        List<AlbaranVentaLinea> res = service.findByAlbaranId(albaranId);
        assertNotNull(res);
        assertEquals(1, res.size());
        verify(repository, times(1)).findByAlbaranIdWithArticulo(albaranId);
    }
}
