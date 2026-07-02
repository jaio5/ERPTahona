package alicanteweb.erp.service;

import alicanteweb.erp.entities.*;
import alicanteweb.erp.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProduccionServiceTest {

    @Mock private RecetaRepository recetaRepository;
    @Mock private RecetaIngredienteRepository ingredienteRepository;
    @Mock private OrdenProduccionRepository ordenProduccionRepository;
    @Mock private HorneadaRepository horneadaRepository;
    @Mock private ArticuloService articuloService;
    @Mock private AlmacenService almacenService;

    @InjectMocks private RecetaService recetaService;
    @InjectMocks private OrdenProduccionService ordenProduccionService;
    @InjectMocks private HorneadaService horneadaService;

    private Receta receta;
    private OrdenProduccion orden;

    @BeforeEach
    void setUp() {
        receta = new Receta();
        receta.setId(1L);
        receta.setCodigo("REC-001");
        receta.setNombre("Pan integral");
        receta.setTiempoHorneado(45);
        receta.setTemperaturaHorneado(200);

        orden = new OrdenProduccion();
        orden.setId(1L);
        orden.setNumero("OP-2026-0001");
        orden.setFecha(LocalDate.now());
        orden.setCantidadPlanificada(new BigDecimal("50"));
        orden.setEstado("PLANIFICADA");
    }

    @Test
    @DisplayName("Guardar receta nueva")
    void guardarReceta() {
        when(recetaRepository.findByCodigo("REC-001")).thenReturn(Optional.empty());
        when(recetaRepository.save(any())).thenReturn(receta);

        Receta saved = recetaService.save(receta);
        assertNotNull(saved);
        assertEquals("REC-001", saved.getCodigo());
        verify(recetaRepository).save(any());
    }

    @Test
    @DisplayName("No permitir código duplicado")
    void codigoDuplicado() {
        Receta existente = new Receta();
        existente.setId(99L);
        existente.setCodigo("REC-001");
        when(recetaRepository.findByCodigo("REC-001")).thenReturn(Optional.of(existente));

        assertThrows(IllegalArgumentException.class, () -> recetaService.save(receta));
        verify(recetaRepository, never()).save(any());
    }

    @Test
    @DisplayName("Generar número de orden secuencial")
    void generarNumeroOrden() {
        orden.setNumero(null); // Forzar generación de número
        when(ordenProduccionRepository.findMaxNumeroSecuencialBySerie(anyString())).thenReturn(5);
        when(ordenProduccionRepository.save(any())).thenReturn(orden);

        OrdenProduccion saved = ordenProduccionService.save(orden);
        assertNotNull(saved);
        assertNotNull(saved.getNumero());
        assertTrue(saved.getNumero().startsWith("OP-"));
    }

    @Test
    @DisplayName("Iniciar orden de producción")
    void iniciarOrden() {
        when(ordenProduccionRepository.findById(1L)).thenReturn(Optional.of(orden));
        when(ordenProduccionRepository.save(any())).thenReturn(orden);

        OrdenProduccion result = ordenProduccionService.iniciarProduccion(1L);
        assertEquals("EN_CURSO", result.getEstado());
        assertNotNull(result.getFechaInicio());
    }

    @Test
    @DisplayName("No se puede iniciar orden finalizada")
    void noIniciarFinalizada() {
        orden.setEstado("FINALIZADA");
        when(ordenProduccionRepository.findById(1L)).thenReturn(Optional.of(orden));

        assertThrows(IllegalStateException.class, () -> ordenProduccionService.iniciarProduccion(1L));
    }

    @Test
    @DisplayName("Finalizar orden con cantidades y merma")
    void finalizarOrden() {
        orden.setEstado("EN_CURSO");
        when(ordenProduccionRepository.findById(1L)).thenReturn(Optional.of(orden));
        when(ordenProduccionRepository.save(any())).thenReturn(orden);

        BigDecimal producido = new BigDecimal("48");
        BigDecimal merma = new BigDecimal("2");
        OrdenProduccion result = ordenProduccionService.finalizarProduccion(1L, producido, merma);
        assertEquals("FINALIZADA", result.getEstado());
        assertEquals(0, producido.compareTo(result.getCantidadProducida()));
        assertEquals(0, merma.compareTo(result.getMerma()));
    }

    @Test
    @DisplayName("Registrar horneada")
    void registrarHorneada() {
        Horneada horn = new Horneada();
        horn.setFecha(LocalDate.now());
        horn.setTemperaturaInicial(200);
        horn.setResultado("OK");
        when(horneadaRepository.save(any())).thenReturn(horn);

        Horneada saved = horneadaService.save(horn);
        assertNotNull(saved);
        assertEquals("OK", saved.getResultado());
    }
}
