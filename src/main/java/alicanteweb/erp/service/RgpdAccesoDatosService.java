package alicanteweb.erp.service;

import alicanteweb.erp.entities.Cliente;
import alicanteweb.erp.entities.RgpdAccesoDatos;
import alicanteweb.erp.entities.Usuario;
import alicanteweb.erp.repository.RgpdAccesoDatosRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Servicio de gestión de accesos a datos personales (RGPD)
 */
@Service
@Slf4j
public class RgpdAccesoDatosService {

    private final RgpdAccesoDatosRepository accesoDatosRepository;

    public RgpdAccesoDatosService(RgpdAccesoDatosRepository accesoDatosRepository) {
        this.accesoDatosRepository = accesoDatosRepository;
    }

    /**
     * Registrar un acceso a datos personales
     */
    @Transactional
    public void registrarAcceso(Usuario usuario, Cliente cliente, String tipoAcceso,
                               String modulo, String motivo, String ip) {
        try {
            RgpdAccesoDatos acceso = new RgpdAccesoDatos();
            acceso.setUsuario(usuario);
            acceso.setCliente(cliente);
            acceso.setTipoAcceso(tipoAcceso);
            acceso.setFechaAcceso(LocalDateTime.now());
            acceso.setIp(ip);
            acceso.setModulo(modulo);
            acceso.setMotivo(motivo);

            // Metadata adicional
            Map<String, Object> metadata = new HashMap<>();
            metadata.put("usuario_nombre", usuario != null ? usuario.getUsername() : "SISTEMA");
            metadata.put("cliente_id", cliente != null ? cliente.getId() : null);
            acceso.setMetadata(metadata);

            accesoDatosRepository.save(acceso);

            log.debug("Acceso a datos registrado: {} - {} - {}", tipoAcceso, modulo,
                    usuario != null ? usuario.getUsername() : "SISTEMA");
        } catch (Exception e) {
            log.error("Error registrando acceso a datos", e);
            // No propagar excepción para no afectar operación principal
        }
    }

    /**
     * Registrar acceso con campos específicos
     */
    @Transactional
    public void registrarAccesoConCampos(Usuario usuario, Cliente cliente, String tipoAcceso,
                                        String modulo, String motivo, String ip,
                                        Map<String, Object> camposAccedidos) {
        try {
            RgpdAccesoDatos acceso = new RgpdAccesoDatos();
            acceso.setUsuario(usuario);
            acceso.setCliente(cliente);
            acceso.setTipoAcceso(tipoAcceso);
            acceso.setFechaAcceso(LocalDateTime.now());
            acceso.setIp(ip);
            acceso.setModulo(modulo);
            acceso.setMotivo(motivo);
            acceso.setCamposAccedidos(camposAccedidos);

            // Metadata
            Map<String, Object> metadata = new HashMap<>();
            metadata.put("usuario_nombre", usuario != null ? usuario.getUsername() : "SISTEMA");
            metadata.put("cliente_id", cliente != null ? cliente.getId() : null);
            metadata.put("num_campos", camposAccedidos != null ? camposAccedidos.size() : 0);
            acceso.setMetadata(metadata);

            accesoDatosRepository.save(acceso);

            log.debug("Acceso a datos con campos registrado: {} campos accedidos",
                    camposAccedidos != null ? camposAccedidos.size() : 0);
        } catch (Exception e) {
            log.error("Error registrando acceso a datos con campos", e);
        }
    }

    /**
     * Obtener accesos de un cliente
     */
    public List<RgpdAccesoDatos> obtenerAccesosCliente(Long clienteId) {
        return accesoDatosRepository.findByClienteIdOrderByFechaAccesoDesc(clienteId);
    }

    /**
     * Obtener accesos de un usuario
     */
    public List<RgpdAccesoDatos> obtenerAccesosUsuario(Long usuarioId) {
        return accesoDatosRepository.findByUsuarioIdOrderByFechaAccesoDesc(usuarioId);
    }

    /**
     * Obtener accesos por tipo
     */
    public List<RgpdAccesoDatos> obtenerPorTipo(String tipoAcceso) {
        return accesoDatosRepository.findByTipoAccesoOrderByFechaAccesoDesc(tipoAcceso);
    }

    /**
     * Obtener accesos por módulo
     */
    public List<RgpdAccesoDatos> obtenerPorModulo(String modulo) {
        return accesoDatosRepository.findByModuloOrderByFechaAccesoDesc(modulo);
    }

    /**
     * Obtener accesos por rango de fechas
     */
    public List<RgpdAccesoDatos> obtenerPorFechas(LocalDateTime inicio, LocalDateTime fin) {
        return accesoDatosRepository.findByFechaAccesoBetweenOrderByFechaAccesoDesc(inicio, fin);
    }

    /**
     * Obtener accesos de un cliente por rango de fechas
     */
    public List<RgpdAccesoDatos> obtenerAccesosClientePorFechas(Long clienteId,
                                                                LocalDateTime inicio,
                                                                LocalDateTime fin) {
        return accesoDatosRepository.findByClienteIdAndFechaAccesoBetweenOrderByFechaAccesoDesc(
                clienteId, inicio, fin);
    }

    /**
     * Obtener accesos de un usuario por rango de fechas
     */
    public List<RgpdAccesoDatos> obtenerAccesosUsuarioPorFechas(Long usuarioId,
                                                                LocalDateTime inicio,
                                                                LocalDateTime fin) {
        return accesoDatosRepository.findByUsuarioIdAndFechaAccesoBetweenOrderByFechaAccesoDesc(
                usuarioId, inicio, fin);
    }

    /**
     * Obtener accesos recientes (últimas 24 horas)
     */
    public List<RgpdAccesoDatos> obtenerAccesosRecientes() {
        LocalDateTime hace24h = LocalDateTime.now().minusHours(24);
        return accesoDatosRepository.findAccesosRecientes(hace24h);
    }

    /**
     * Contar accesos por cliente
     */
    public long contarPorCliente(Long clienteId) {
        return accesoDatosRepository.countByClienteId(clienteId);
    }

    /**
     * Contar accesos por usuario
     */
    public long contarPorUsuario(Long usuarioId) {
        return accesoDatosRepository.countByUsuarioId(usuarioId);
    }

    /**
     * Contar accesos por tipo
     */
    public long contarPorTipo(String tipoAcceso) {
        return accesoDatosRepository.countByTipoAcceso(tipoAcceso);
    }

    /**
     * Obtener estadísticas por módulo
     */
    public List<Object[]> obtenerEstadisticasPorModulo() {
        return accesoDatosRepository.estadisticasPorModulo();
    }

    /**
     * Obtener estadísticas por usuario
     */
    public List<Object[]> obtenerEstadisticasPorUsuario() {
        return accesoDatosRepository.estadisticasPorUsuario();
    }

    /**
     * Generar informe de accesos para un cliente (derecho de acceso RGPD)
     */
    public Map<String, Object> generarInformeAccesosCliente(Long clienteId) {
        List<RgpdAccesoDatos> accesos = obtenerAccesosCliente(clienteId);

        Map<String, Object> informe = new HashMap<>();
        informe.put("cliente_id", clienteId);
        informe.put("total_accesos", accesos.size());
        informe.put("fecha_generacion", LocalDateTime.now());
        informe.put("accesos", accesos);

        // Estadísticas
        Map<String, Long> porTipo = new HashMap<>();
        Map<String, Long> porModulo = new HashMap<>();
        Map<String, Long> porUsuario = new HashMap<>();

        for (RgpdAccesoDatos acceso : accesos) {
            // Por tipo
            String tipo = acceso.getTipoAcceso();
            porTipo.put(tipo, porTipo.getOrDefault(tipo, 0L) + 1);

            // Por módulo
            String modulo = acceso.getModulo();
            if (modulo != null) {
                porModulo.put(modulo, porModulo.getOrDefault(modulo, 0L) + 1);
            }

            // Por usuario
            String usuario = acceso.getUsuario() != null ?
                    acceso.getUsuario().getUsername() : "SISTEMA";
            porUsuario.put(usuario, porUsuario.getOrDefault(usuario, 0L) + 1);
        }

        informe.put("accesos_por_tipo", porTipo);
        informe.put("accesos_por_modulo", porModulo);
        informe.put("accesos_por_usuario", porUsuario);

        return informe;
    }
}


