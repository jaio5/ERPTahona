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
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TrazabilidadRepartoServiceTest {

    @Mock private LoteRepository loteRepository;
    @Mock private LoteInsumoRepository loteInsumoRepository;
    @Mock private VehiculoRepository vehiculoRepository;
    @Mock private RutaRepartoRepository rutaRepository;
    @Mock private RutaParadaRepository paradaRepository;
    @Mock private HojaRutaRepository hojaRutaRepository;
    @Mock private HojaRutaEntregaRepository entregaRepository;
    @Mock private DevolucionRepository devolucionRepository;
    @Mock private DevolucionLineaRepository devolucionLineaRepository;
    @Mock private ArticuloService articuloService;
    @Mock private AlmacenService almacenService;

    @InjectMocks private LoteService loteService;
    @InjectMocks private VehiculoService vehiculoService;
    @InjectMocks private RutaRepartoService rutaService;
    @InjectMocks private HojaRutaService hojaRutaService;
    @InjectMocks private DevolucionService devolucionService;

    @BeforeEach
    void setUp() {
        // HojaRutaService needs RutaRepartoService injected
        hojaRutaService = new HojaRutaService(hojaRutaRepository, entregaRepository, rutaService);
        loteService = new LoteService(loteRepository, loteInsumoRepository);
        devolucionService = new DevolucionService(devolucionRepository, devolucionLineaRepository, articuloService, loteService);
    }

    @Test
    @DisplayName("Guardar lote con trazabilidad")
    void guardarLote() {
        Lote lote = new Lote();
        lote.setCodigo("LT-001");
        lote.setFechaProduccion(LocalDate.now());
        lote.setFechaCaducidad(LocalDate.now().plusDays(3));

        when(loteRepository.findByCodigo("LT-001")).thenReturn(Optional.empty());
        when(loteRepository.save(any())).thenReturn(lote);

        Lote saved = loteService.save(lote);
        assertNotNull(saved);
        assertEquals("LT-001", saved.getCodigo());
    }

    @Test
    @DisplayName("Lotes próximos a caducar")
    void lotesProximosACaducar() {
        Lote lote = new Lote();
        lote.setCodigo("LT-CAD-001");
        lote.setFechaCaducidad(LocalDate.now().plusDays(2));

        when(loteRepository.findByFechaCaducidadBetween(any(), any())).thenReturn(List.of(lote));

        List<Lote> resultado = loteService.findByFechaCaducidadBetween(
                LocalDate.now(), LocalDate.now().plusDays(3));
        assertEquals(1, resultado.size());
        assertEquals("LT-CAD-001", resultado.get(0).getCodigo());
    }

    @Test
    @DisplayName("Trazabilidad hacia atrás (insumos de un producto)")
    void trazabilidadAtras() {
        LoteInsumo insumo = new LoteInsumo();
        when(loteInsumoRepository.findByLoteProductoId(1L)).thenReturn(List.of(insumo));

        var result = loteService.findInsumosDeProducto(1L);
        assertEquals(1, result.size());
    }

    @Test
    @DisplayName("Guardar vehículo nuevo")
    void guardarVehiculo() {
        Vehiculo v = new Vehiculo();
        v.setMatricula("1234ABC");
        v.setMarca("Fiat");
        v.setTipo("FURGONETA");

        when(vehiculoRepository.findByMatricula("1234ABC")).thenReturn(Optional.empty());
        when(vehiculoRepository.save(any())).thenReturn(v);

        Vehiculo saved = vehiculoService.save(v);
        assertEquals("1234ABC", saved.getMatricula());
    }

    @Test
    @DisplayName("No permitir matrícula duplicada")
    void matriculaDuplicada() {
        Vehiculo existente = new Vehiculo();
        existente.setId(99L);
        existente.setMatricula("1234ABC");
        when(vehiculoRepository.findByMatricula("1234ABC")).thenReturn(Optional.of(existente));

        Vehiculo nuevo = new Vehiculo();
        nuevo.setMatricula("1234ABC");
        assertThrows(IllegalArgumentException.class, () -> vehiculoService.save(nuevo));
    }

    @Test
    @DisplayName("Generar hoja de ruta desde ruta maestra")
    void generarHojaDesdeRuta() {
        RutaReparto ruta = new RutaReparto();
        ruta.setId(1L);
        ruta.setCodigo("RUTA-01");
        ruta.setNombre("Ruta Centro");
        ruta.setConductor("Carlos");

        Vehiculo vehiculo = new Vehiculo();
        vehiculo.setId(1L);
        vehiculo.setMatricula("1234ABC");
        ruta.setVehiculo(vehiculo);

        Cliente cliente = new Cliente();
        cliente.setId(1L);
        cliente.setNombre("Cliente Test");

        RutaParada parada = new RutaParada();
        parada.setCliente(cliente);
        parada.setOrden(1);

        when(rutaRepository.findById(1L)).thenReturn(Optional.of(ruta));
        when(paradaRepository.findByRutaIdOrderByOrden(1L)).thenReturn(List.of(parada));
        when(hojaRutaRepository.save(any())).thenAnswer(inv -> {
            HojaRuta h = inv.getArgument(0);
            h.setId(1L);
            return h;
        });
        when(entregaRepository.save(any())).thenAnswer(inv -> {
            HojaRutaEntrega e = inv.getArgument(0);
            e.setId(1L);
            return e;
        });

        HojaRuta hoja = hojaRutaService.generarDesdeRuta(1L, LocalDate.now());
        assertNotNull(hoja.getId());
        assertEquals("Carlos", hoja.getConductor());
        assertEquals("PLANIFICADA", hoja.getEstado());
    }

    @Test
    @DisplayName("Confirmar entrega en hoja de ruta")
    void confirmarEntrega() {
        HojaRutaEntrega entrega = new HojaRutaEntrega();
        entrega.setId(1L);
        entrega.setEntregado(false);
        when(entregaRepository.findById(1L)).thenReturn(Optional.of(entrega));
        when(entregaRepository.save(any())).thenReturn(entrega);

        HojaRutaEntrega result = hojaRutaService.confirmarEntrega(1L, "Juan Pérez", null, null, null);
        assertTrue(result.getEntregado());
        assertEquals("Juan Pérez", result.getPersonaRecepcion());
        assertEquals("ENTREGADO", result.getEstadoEntrega());
    }

    @Test
    @DisplayName("Aceptar devolución")
    void aceptarDevolucion() {
        Devolucion dev = new Devolucion();
        dev.setId(1L);
        dev.setEstado("PENDIENTE");
        when(devolucionRepository.findById(1L)).thenReturn(Optional.of(dev));
        when(devolucionRepository.save(any())).thenReturn(dev);

        Devolucion result = devolucionService.aceptarDevolucion(1L);
        assertEquals("ACEPTADA", result.getEstado());
    }
}
