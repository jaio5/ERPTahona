package alicanteweb.erp.service;

import alicanteweb.erp.entities.TipoImpositivo;
import alicanteweb.erp.repository.TipoImpositivoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TipoImpositivoServiceTest {

    @Mock TipoImpositivoRepository repository;
    TipoImpositivoService service;

    @BeforeEach
    void setUp() {
        service = new TipoImpositivoService(repository);
    }

    private TipoImpositivo tipo(String nombre, String porcentaje) {
        TipoImpositivo t = new TipoImpositivo();
        t.setNombre(nombre);
        t.setPorcentaje(porcentaje == null ? null : new BigDecimal(porcentaje));
        return t;
    }

    @Test
    void guardaValidoYMarcaUnicoPorDefecto() {
        TipoImpositivo t = tipo("General 21%", "21");
        t.setEsDefecto(true);
        when(repository.save(t)).thenAnswer(i -> { t.setId(5L); return t; });

        TipoImpositivo guardado = service.save(t);

        assertEquals(5L, guardado.getId());
        verify(repository).desmarcarDefectoExcepto(5L); // desmarca los demás
    }

    @Test
    void noDesmarcaSiNoEsPorDefecto() {
        TipoImpositivo t = tipo("Reducido 10%", "10");
        when(repository.save(t)).thenAnswer(i -> { t.setId(6L); return t; });

        service.save(t);

        verify(repository, never()).desmarcarDefectoExcepto(anyLong());
    }

    @Test
    void rechazaNombreVacio() {
        assertThrows(IllegalArgumentException.class, () -> service.save(tipo("  ", "21")));
    }

    @Test
    void rechazaPorcentajeNulo() {
        assertThrows(IllegalArgumentException.class, () -> service.save(tipo("X", null)));
    }

    @Test
    void rechazaPorcentajeFueraDeRango() {
        assertThrows(IllegalArgumentException.class, () -> service.save(tipo("X", "150")));
    }

    @Test
    void rechazaRecargoFueraDeRango() {
        TipoImpositivo t = tipo("X", "21");
        t.setRecargoEquivalencia(new BigDecimal("150"));
        assertThrows(IllegalArgumentException.class, () -> service.save(t));
    }

    @Test
    void findActivosDelegaEnElRepositorioOrdenado() {
        when(repository.findByActivoTrueOrderByOrdenAscPorcentajeAsc())
                .thenReturn(List.of(tipo("Superreducido 4%", "4")));
        assertEquals(1, service.findActivos().size());
    }
}
