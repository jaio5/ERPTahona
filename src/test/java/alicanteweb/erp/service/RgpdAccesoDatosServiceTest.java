package alicanteweb.erp.service;

import alicanteweb.erp.entities.Cliente;
import alicanteweb.erp.entities.RgpdAccesoDatos;
import alicanteweb.erp.entities.Usuario;
import alicanteweb.erp.repository.RgpdAccesoDatosRepository;
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
class RgpdAccesoDatosServiceTest {

    @Mock RgpdAccesoDatosRepository repository;
    RgpdAccesoDatosService service;

    @BeforeEach
    void setUp() {
        service = new RgpdAccesoDatosService(repository);
    }

    @Test
    void registrarAccesoGuardaDatosYMetadata() {
        Usuario usuario = usuario(2L, "ana");
        Cliente cliente = cliente(8L);

        service.registrarAcceso(usuario, cliente, "LECTURA", "CLIENTES",
                "Atención comercial", "127.0.0.1");

        RgpdAccesoDatos acceso = capturar();
        assertSame(usuario, acceso.getUsuario());
        assertSame(cliente, acceso.getCliente());
        assertEquals("LECTURA", acceso.getTipoAcceso());
        assertEquals("CLIENTES", acceso.getModulo());
        assertEquals("Atención comercial", acceso.getMotivo());
        assertEquals("127.0.0.1", acceso.getIp());
        assertNotNull(acceso.getFechaAcceso());
        assertEquals("ana", acceso.getMetadata().get("usuario_nombre"));
        assertEquals(8L, acceso.getMetadata().get("cliente_id"));
    }

    @Test
    void registrarAccesoAdmiteSistemaYSinClienteYNoPropagaFallos() {
        service.registrarAcceso(null, null, "EXPORTACION", null, null, null);
        RgpdAccesoDatos acceso = capturar();
        assertEquals("SISTEMA", acceso.getMetadata().get("usuario_nombre"));
        assertNull(acceso.getMetadata().get("cliente_id"));

        reset(repository);
        when(repository.save(any())).thenThrow(new RuntimeException("error"));
        assertDoesNotThrow(() ->
                service.registrarAcceso(null, null, "LECTURA", "X", "Y", "Z"));
    }

    @Test
    void registrarAccesoConCamposCuentaCamposIncluidoNull() {
        Map<String, Object> campos = Map.of("nombre", "Ana", "email", "a@b.es");
        service.registrarAccesoConCampos(usuario(1L, "operador"), cliente(3L),
                "LECTURA", "CLIENTES", "consulta", "ip", campos);

        RgpdAccesoDatos acceso = capturar();
        assertSame(campos, acceso.getCamposAccedidos());
        assertEquals(2, acceso.getMetadata().get("num_campos"));

        reset(repository);
        service.registrarAccesoConCampos(null, null, "LECTURA", null, null, null, null);
        assertEquals(0, capturar().getMetadata().get("num_campos"));

        reset(repository);
        when(repository.save(any())).thenThrow(new RuntimeException("error"));
        assertDoesNotThrow(() ->
                service.registrarAccesoConCampos(null, null, "X", null, null, null, campos));
    }

    @Test
    void consultasContadoresYEstadisticasDelegan() {
        LocalDateTime inicio = LocalDateTime.now().minusDays(1);
        LocalDateTime fin = LocalDateTime.now();
        List<RgpdAccesoDatos> accesos = List.of(new RgpdAccesoDatos());
        List<Object[]> estadisticas = List.<Object[]>of(new Object[]{"CLIENTES", 3L});

        when(repository.findByClienteIdOrderByFechaAccesoDesc(1L)).thenReturn(accesos);
        when(repository.findByUsuarioIdOrderByFechaAccesoDesc(2L)).thenReturn(accesos);
        when(repository.findByTipoAccesoOrderByFechaAccesoDesc("LECTURA")).thenReturn(accesos);
        when(repository.findByModuloOrderByFechaAccesoDesc("CLIENTES")).thenReturn(accesos);
        when(repository.findByFechaAccesoBetweenOrderByFechaAccesoDesc(inicio, fin)).thenReturn(accesos);
        when(repository.findByClienteIdAndFechaAccesoBetweenOrderByFechaAccesoDesc(1L, inicio, fin))
                .thenReturn(accesos);
        when(repository.findByUsuarioIdAndFechaAccesoBetweenOrderByFechaAccesoDesc(2L, inicio, fin))
                .thenReturn(accesos);
        when(repository.countByClienteId(1L)).thenReturn(4L);
        when(repository.countByUsuarioId(2L)).thenReturn(5L);
        when(repository.countByTipoAcceso("LECTURA")).thenReturn(6L);
        when(repository.estadisticasPorModulo()).thenReturn(estadisticas);
        when(repository.estadisticasPorUsuario()).thenReturn(estadisticas);

        assertSame(accesos, service.obtenerAccesosCliente(1L));
        assertSame(accesos, service.obtenerAccesosUsuario(2L));
        assertSame(accesos, service.obtenerPorTipo("LECTURA"));
        assertSame(accesos, service.obtenerPorModulo("CLIENTES"));
        assertSame(accesos, service.obtenerPorFechas(inicio, fin));
        assertSame(accesos, service.obtenerAccesosClientePorFechas(1L, inicio, fin));
        assertSame(accesos, service.obtenerAccesosUsuarioPorFechas(2L, inicio, fin));
        assertEquals(4L, service.contarPorCliente(1L));
        assertEquals(5L, service.contarPorUsuario(2L));
        assertEquals(6L, service.contarPorTipo("LECTURA"));
        assertSame(estadisticas, service.obtenerEstadisticasPorModulo());
        assertSame(estadisticas, service.obtenerEstadisticasPorUsuario());

        when(repository.findAccesosRecientes(any(LocalDateTime.class))).thenReturn(accesos);
        assertSame(accesos, service.obtenerAccesosRecientes());
        verify(repository).findAccesosRecientes(argThat(fecha ->
                fecha.isAfter(LocalDateTime.now().minusHours(25))));
    }

    @Test
    @SuppressWarnings("unchecked")
    void informeAgrupaPorTipoModuloYUsuario() {
        RgpdAccesoDatos primero = acceso("LECTURA", "CLIENTES", usuario(1L, "ana"));
        RgpdAccesoDatos segundo = acceso("LECTURA", "CLIENTES", usuario(2L, "luis"));
        RgpdAccesoDatos tercero = acceso("EXPORTACION", null, null);
        when(repository.findByClienteIdOrderByFechaAccesoDesc(9L))
                .thenReturn(List.of(primero, segundo, tercero));

        Map<String, Object> informe = service.generarInformeAccesosCliente(9L);

        assertEquals(9L, informe.get("cliente_id"));
        assertEquals(3, informe.get("total_accesos"));
        assertNotNull(informe.get("fecha_generacion"));
        assertEquals(Map.of("LECTURA", 2L, "EXPORTACION", 1L),
                (Map<String, Long>) informe.get("accesos_por_tipo"));
        assertEquals(Map.of("CLIENTES", 2L),
                (Map<String, Long>) informe.get("accesos_por_modulo"));
        assertEquals(Map.of("ana", 1L, "luis", 1L, "SISTEMA", 1L),
                (Map<String, Long>) informe.get("accesos_por_usuario"));
    }

    private RgpdAccesoDatos capturar() {
        ArgumentCaptor<RgpdAccesoDatos> captor = ArgumentCaptor.forClass(RgpdAccesoDatos.class);
        verify(repository).save(captor.capture());
        return captor.getValue();
    }

    private Usuario usuario(Long id, String username) {
        Usuario usuario = new Usuario();
        usuario.setId(id);
        usuario.setUsername(username);
        return usuario;
    }

    private Cliente cliente(Long id) {
        Cliente cliente = new Cliente();
        cliente.setId(id);
        return cliente;
    }

    private RgpdAccesoDatos acceso(String tipo, String modulo, Usuario usuario) {
        RgpdAccesoDatos acceso = new RgpdAccesoDatos();
        acceso.setTipoAcceso(tipo);
        acceso.setModulo(modulo);
        acceso.setUsuario(usuario);
        return acceso;
    }
}
