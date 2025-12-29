package alicanteweb.erp.service;

import alicanteweb.erp.entities.Cliente;
import alicanteweb.erp.entities.Articulo;
import alicanteweb.erp.entities.Proveedor;
import alicanteweb.erp.entities.Usuario;
import alicanteweb.erp.repository.ClienteRepository;
import alicanteweb.erp.repository.ArticuloRepository;
import alicanteweb.erp.repository.ProveedorRepository;
import alicanteweb.erp.repository.UsuarioRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Servicio de diagnóstico que verifica el estado de los datos al iniciar
 */
@Service
@Slf4j
public class DiagnosticoBaseDatosService {

    private final ClienteRepository clienteRepository;
    private final ArticuloRepository articuloRepository;
    private final ProveedorRepository proveedorRepository;
    private final UsuarioRepository usuarioRepository;

    public DiagnosticoBaseDatosService(
            ClienteRepository clienteRepository,
            ArticuloRepository articuloRepository,
            ProveedorRepository proveedorRepository,
            UsuarioRepository usuarioRepository) {
        this.clienteRepository = clienteRepository;
        this.articuloRepository = articuloRepository;
        this.proveedorRepository = proveedorRepository;
        this.usuarioRepository = usuarioRepository;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void diagnosticarBaseDatos() {
        log.info("╔══════════════════════════════════════════════════════════╗");
        log.info("║                                                          ║");
        log.info("║        DIAGNÓSTICO DE BASE DE DATOS                     ║");
        log.info("║                                                          ║");
        log.info("╚══════════════════════════════════════════════════════════╝");

        try {
            // Verificar usuarios
            long totalUsuarios = usuarioRepository.count();
            log.info("📊 USUARIOS:");
            log.info("   Total: {}", totalUsuarios);
            if (totalUsuarios > 0) {
                List<Usuario> usuarios = usuarioRepository.findAll();
                usuarios.forEach(u -> log.info("   - {} ({})",
                    u.getUsername(),
                    (u.getEnabled() != null && u.getEnabled()) ? "activo" : "inactivo"));
            } else {
                log.warn("   ⚠️ No hay usuarios en la base de datos");
            }

            // Verificar clientes
            long totalClientes = clienteRepository.count();
            log.info("📊 CLIENTES:");
            log.info("   Total: {}", totalClientes);
            if (totalClientes > 0) {
                List<Cliente> clientes = clienteRepository.findAll();
                log.info("   Primeros 5 clientes:");
                clientes.stream().limit(5).forEach(c ->
                    log.info("   - {} - {} ({})",
                        c.getCodigo(),
                        c.getNombre(),
                        (c.getActivo() != null && c.getActivo()) ? "activo" : "inactivo"));
            } else {
                log.warn("   ⚠️ No hay clientes en la base de datos");
                log.warn("   💡 Consejo: Crea algunos clientes de prueba");
            }

            // Verificar artículos
            long totalArticulos = articuloRepository.count();
            log.info("📊 ARTÍCULOS:");
            log.info("   Total: {}", totalArticulos);
            if (totalArticulos > 0) {
                List<Articulo> articulos = articuloRepository.findAll();
                log.info("   Primeros 5 artículos:");
                articulos.stream().limit(5).forEach(a ->
                    log.info("   - {} - {} - PVP: {}", a.getCodigo(), a.getDescripcion(), a.getPvp()));
            } else {
                log.warn("   ⚠️ No hay artículos en la base de datos");
                log.warn("   💡 Consejo: Crea algunos artículos de prueba");
            }

            // Verificar proveedores
            long totalProveedores = proveedorRepository.count();
            log.info("📊 PROVEEDORES:");
            log.info("   Total: {}", totalProveedores);
            if (totalProveedores > 0) {
                List<Proveedor> proveedores = proveedorRepository.findAll();
                log.info("   Primeros 5 proveedores:");
                proveedores.stream().limit(5).forEach(p ->
                    log.info("   - {} - {} ({})",
                        p.getId(),
                        p.getNombre(),
                        (p.getActivo() != null && p.getActivo()) ? "activo" : "inactivo"));
            } else {
                log.warn("   ⚠️ No hay proveedores en la base de datos");
                log.warn("   💡 Consejo: Crea algunos proveedores de prueba");
            }

            log.info("╔══════════════════════════════════════════════════════════╗");
            log.info("║                                                          ║");
            log.info("║        DIAGNÓSTICO COMPLETADO                            ║");
            log.info("║                                                          ║");
            log.info("╚══════════════════════════════════════════════════════════╝");

            // Resumen
            if (totalClientes == 0 && totalArticulos == 0 && totalProveedores == 0) {
                log.warn("⚠️⚠️⚠️ IMPORTANTE ⚠️⚠️⚠️");
                log.warn("No hay datos en las tablas principales.");
                log.warn("Las tablas estarán vacías hasta que crees registros.");
                log.warn("Usa los botones 'Nuevo' en cada módulo para crear datos.");
            } else {
                log.info("✅ Hay datos disponibles en la base de datos");
                log.info("✅ Las tablas deberían cargarse correctamente");
            }

        } catch (Exception e) {
            log.error("❌ ERROR en diagnóstico de base de datos", e);
        }
    }
}

