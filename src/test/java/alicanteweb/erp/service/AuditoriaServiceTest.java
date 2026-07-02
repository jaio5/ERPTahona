package alicanteweb.erp.service;

import alicanteweb.erp.entities.AuditoriaAccion;
import alicanteweb.erp.entities.Usuario;
import alicanteweb.erp.repository.AuditoriaAccionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuditoriaServiceTest {

    @Mock AuditoriaAccionRepository repository;
    AuditoriaService service;

    @BeforeEach
    void setUp() {
        service = new AuditoriaService(repository);
    }

    @Test
    void registrarAccionGuardaTodosLosDatos() {
        Usuario usuario = usuario(7L, "maria");
        Map<String, Object> anteriores = Map.of("estado", "A");
        Map<String, Object> nuevos = Map.of("estado", "B");

        service.registrarAccion(usuario, "ACTUALIZAR", "Factura", "12",
                "Cambio de estado", "VENTAS", anteriores, nuevos);

        AuditoriaAccion accion = capturar();
        assertSame(usuario, accion.getUsuario());
        assertEquals("maria", accion.getUsuarioNombre());
        assertEquals("ACTUALIZAR", accion.getTipoAccion());
        assertEquals("Factura", accion.getEntidadTipo());
        assertEquals("12", accion.getEntidadId());
        assertEquals("Cambio de estado", accion.getDescripcion());
        assertEquals("VENTAS", accion.getModulo());
        assertSame(anteriores, accion.getValoresAnteriores());
        assertSame(nuevos, accion.getValoresNuevos());
        assertEquals("EXITO", accion.getResultado());
        assertNotNull(accion.getFecha());
    }

    @Test
    void registrarAccionAdmiteSistemaYNoPropagaFallos() {
        service.registrarAccion(null, "PROCESO", null, null, "Automático");
        assertEquals("SISTEMA", capturar().getUsuarioNombre());

        reset(repository);
        when(repository.save(any())).thenThrow(new RuntimeException("base de datos"));
        assertDoesNotThrow(() ->
                service.registrarAccion(null, "PROCESO", null, null, "Automático"));
    }

    @Test
    void registrarLoginDistingueExitoFalloYUsuarioDesconocido() {
        Usuario usuario = usuario(3L, "ana");
        service.registrarLogin(usuario, "127.0.0.1", true);
        AuditoriaAccion exito = capturar();
        assertEquals("LOGIN", exito.getTipoAccion());
        assertEquals("3", exito.getEntidadId());
        assertEquals("EXITO", exito.getResultado());
        assertEquals("127.0.0.1", exito.getIp());

        reset(repository);
        service.registrarLogin(null, "10.0.0.1", false);
        AuditoriaAccion fallo = capturar();
        assertEquals("DESCONOCIDO", fallo.getUsuarioNombre());
        assertEquals("LOGIN_FALLIDO", fallo.getTipoAccion());
        assertNull(fallo.getEntidadId());
        assertEquals("ERROR", fallo.getResultado());

        reset(repository);
        when(repository.save(any())).thenThrow(new RuntimeException("error"));
        assertDoesNotThrow(() -> service.registrarLogin(usuario, "127.0.0.1", true));
    }

    @Test
    void registraAccesoDenegadoYErroresSinAfectarOperacionPrincipal() {
        service.registrarAccesoDenegado(null, "ADMIN", "borrar usuario");
        AuditoriaAccion denegado = capturar();
        assertEquals("ANONIMO", denegado.getUsuarioNombre());
        assertEquals("ACCESO_DENEGADO", denegado.getTipoAccion());
        assertEquals("DENEGADO", denegado.getResultado());
        assertTrue(denegado.getDescripcion().contains("borrar usuario"));

        reset(repository);
        Usuario usuario = usuario(5L, "pedro");
        service.registrarError(usuario, "Pedido", "9", "No se pudo guardar");
        AuditoriaAccion error = capturar();
        assertEquals("pedro", error.getUsuarioNombre());
        assertEquals("ERROR", error.getTipoAccion());
        assertEquals("ERROR", error.getResultado());

        reset(repository);
        when(repository.save(any())).thenThrow(new RuntimeException("error"));
        assertDoesNotThrow(() -> service.registrarAccesoDenegado(usuario, "X", "Y"));
        assertDoesNotThrow(() -> service.registrarError(null, "X", "1", "Y"));
    }

    @Test
    void metodosDeConvenienciaGeneranLaAccionEsperada() {
        Usuario usuario = usuario(1L, "admin");

        service.registrarLogout(usuario);
        service.registrarCreacion(usuario, "Cliente", "1", "alta");
        service.registrarActualizacion(usuario, "Cliente", "1", "edición");
        service.registrarCambio(usuario, "Cliente", "1", Map.of("a", 1), Map.of("a", 2));
        service.registrarEliminacion(usuario, "Cliente", "1", "baja");
        service.registrarExportacion(usuario, "Clientes", "csv");
        service.registrarImpresion(usuario, "Factura", "2", "pdf");

        ArgumentCaptor<AuditoriaAccion> captor = ArgumentCaptor.forClass(AuditoriaAccion.class);
        verify(repository, times(7)).save(captor.capture());
        assertEquals(List.of("LOGOUT", "CREAR", "ACTUALIZAR", "ACTUALIZAR",
                        "ELIMINAR", "EXPORTAR", "IMPRIMIR"),
                captor.getAllValues().stream().map(AuditoriaAccion::getTipoAccion).toList());
        assertEquals("EXPORTACION", captor.getAllValues().get(5).getModulo());
        assertEquals("IMPRESION", captor.getAllValues().get(6).getModulo());
    }

    @Test
    void consultasYEstadisticasDeleganEnRepositorio() {
        LocalDateTime inicio = LocalDateTime.now().minusDays(1);
        LocalDateTime fin = LocalDateTime.now();
        List<AuditoriaAccion> acciones = List.of(new AuditoriaAccion());
        List<Object[]> estadisticas = List.<Object[]>of(new Object[]{"VENTAS", 2L});

        when(repository.findHistorialEntidad("Factura", "1")).thenReturn(acciones);
        when(repository.findByUsuarioIdOrderByFechaDesc(2L)).thenReturn(acciones);
        when(repository.findByTipoAccionOrderByFechaDesc("CREAR")).thenReturn(acciones);
        when(repository.findByModuloOrderByFechaDesc("VENTAS")).thenReturn(acciones);
        when(repository.findByFechaBetweenOrderByFechaDesc(inicio, fin)).thenReturn(acciones);
        when(repository.findLoginsFallidos()).thenReturn(acciones);
        when(repository.findAccesosDenegados()).thenReturn(acciones);
        when(repository.estadisticasPorModulo()).thenReturn(estadisticas);
        when(repository.estadisticasPorUsuario()).thenReturn(estadisticas);
        when(repository.estadisticasPorAccion()).thenReturn(estadisticas);
        when(repository.countByUsuarioId(2L)).thenReturn(3L);
        when(repository.countByTipoAccion("CREAR")).thenReturn(4L);
        when(repository.countByModulo("VENTAS")).thenReturn(5L);

        assertSame(acciones, service.obtenerHistorial("Factura", "1"));
        assertSame(acciones, service.obtenerPorUsuario(2L));
        assertSame(acciones, service.obtenerPorTipo("CREAR"));
        assertSame(acciones, service.obtenerPorModulo("VENTAS"));
        assertSame(acciones, service.obtenerPorFechas(inicio, fin));
        assertSame(acciones, service.obtenerLoginsFallidos());
        assertSame(acciones, service.obtenerAccesosDenegados());
        assertSame(estadisticas, service.obtenerEstadisticasPorModulo());
        assertSame(estadisticas, service.obtenerEstadisticasPorUsuario());
        assertSame(estadisticas, service.obtenerEstadisticasPorAccion());
        assertEquals(3L, service.contarPorUsuario(2L));
        assertEquals(4L, service.contarPorTipo("CREAR"));
        assertEquals(5L, service.contarPorModulo("VENTAS"));

        when(repository.findRecientes(any(LocalDateTime.class))).thenReturn(acciones);
        assertSame(acciones, service.obtenerRecientes());
        verify(repository).findRecientes(argThat(fecha ->
                fecha.isAfter(LocalDateTime.now().minusHours(25))));
    }

    private AuditoriaAccion capturar() {
        ArgumentCaptor<AuditoriaAccion> captor = ArgumentCaptor.forClass(AuditoriaAccion.class);
        verify(repository).save(captor.capture());
        return captor.getValue();
    }

    private Usuario usuario(Long id, String username) {
        Usuario usuario = new Usuario();
        usuario.setId(id);
        usuario.setUsername(username);
        return usuario;
    }
}
