package alicanteweb.erp.service;

import alicanteweb.erp.entities.Cliente;
import alicanteweb.erp.entities.Articulo;
import alicanteweb.erp.entities.Proveedor;
import alicanteweb.erp.entities.Usuario;
import alicanteweb.erp.repository.ClienteRepository;
import alicanteweb.erp.repository.ArticuloRepository;
import alicanteweb.erp.repository.ProveedorRepository;
import alicanteweb.erp.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.event.EventListener;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

/**
 * Servicio de diagnóstico que verifica el estado de los datos al iniciar.
 * Usa conteos y muestras limitadas en lugar de cargar todos los registros.
 */
@Service
@Slf4j
@RequiredArgsConstructor
@ConditionalOnProperty(name = "diagnostics.database.enabled", havingValue = "true")
public class DiagnosticoBaseDatosService {

    private static final int MUESTRA_LIMITE = 5;

    private final ClienteRepository clienteRepository;
    private final ArticuloRepository articuloRepository;
    private final ProveedorRepository proveedorRepository;
    private final UsuarioRepository usuarioRepository;

    @EventListener(ApplicationReadyEvent.class)
    public void diagnosticarBaseDatos() {
        log.info("--- DIAGNÓSTICO DE BASE DE DATOS ---");
        try {
            diagnosticarEntidad("USUARIOS",    usuarioRepository.count(),    this::logUsuarios);
            diagnosticarEntidad("CLIENTES",    clienteRepository.count(),    this::logClientes);
            diagnosticarEntidad("ARTÍCULOS",   articuloRepository.count(),   this::logArticulos);
            diagnosticarEntidad("PROVEEDORES", proveedorRepository.count(),  this::logProveedores);

            log.info("--- FIN DIAGNÓSTICO ---");

            boolean sinDatos = clienteRepository.count() == 0
                    && articuloRepository.count() == 0
                    && proveedorRepository.count() == 0;

            if (sinDatos) {
                log.warn("No hay datos en las tablas principales. Usa los botones 'Nuevo' en cada módulo para crear registros.");
            } else {
                log.info("Datos disponibles en la base de datos: OK");
            }
        } catch (Exception e) {
            log.error("ERROR en diagnóstico de base de datos", e);
        }
    }

    private void diagnosticarEntidad(String nombre, long total, Runnable logMuestra) {
        log.info("{}: {} registros", nombre, total);
        if (total > 0) {
            logMuestra.run();
        } else {
            log.warn("  No hay {}. Crea algunos desde la aplicación.", nombre.toLowerCase());
        }
    }

    private void logUsuarios() {
        usuarioRepository.findAll(PageRequest.of(0, MUESTRA_LIMITE)).forEach(u ->
            log.info("  - {} ({})", u.getUsername(), Boolean.TRUE.equals(u.getEnabled()) ? "activo" : "inactivo"));
    }

    private void logClientes() {
        clienteRepository.findAll(PageRequest.of(0, MUESTRA_LIMITE)).forEach(c ->
            log.info("  - {} - {} ({})", c.getCodigo(), c.getNombre(),
                Boolean.TRUE.equals(c.getActivo()) ? "activo" : "inactivo"));
    }

    private void logArticulos() {
        articuloRepository.findAll(PageRequest.of(0, MUESTRA_LIMITE)).forEach(a ->
            log.info("  - {} - {} - PVP: {}", a.getCodigo(), a.getDescripcion(), a.getPvp()));
    }

    private void logProveedores() {
        proveedorRepository.findAll(PageRequest.of(0, MUESTRA_LIMITE)).forEach(p ->
            log.info("  - {} - {} ({})", p.getId(), p.getNombre(),
                Boolean.TRUE.equals(p.getActivo()) ? "activo" : "inactivo"));
    }
}
