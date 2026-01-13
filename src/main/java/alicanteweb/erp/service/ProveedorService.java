package alicanteweb.erp.service;

import alicanteweb.erp.entities.Proveedor;
import alicanteweb.erp.repository.ProveedorRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Servicio de gestión de proveedores
 */
@Service
@Slf4j
@Transactional
public class ProveedorService {

    private final ProveedorRepository proveedorRepository;

    public ProveedorService(ProveedorRepository proveedorRepository) {
        this.proveedorRepository = proveedorRepository;
    }

    /**
     * Obtiene todos los proveedores
     */
    public List<Proveedor> findAll() {
        log.debug("Obteniendo todos los proveedores");
        return proveedorRepository.findAll();
    }

    /**
     * Obtiene solo los proveedores activos
     */
    public List<Proveedor> findActivos() {
        log.debug("Obteniendo proveedores activos");
        return proveedorRepository.findAllActivos();
    }

    /**
     * Busca un proveedor por ID
     */
    public Optional<Proveedor> findById(Long id) {
        return proveedorRepository.findById(id);
    }

    /**
     * Busca un proveedor por código
     */
    public Optional<Proveedor> findByCodigo(String codigo) {
        return proveedorRepository.findByCodigo(codigo);
    }

    /**
     * Guarda o actualiza un proveedor
     */
    public Proveedor save(Proveedor proveedor) {
        // Si es nuevo, generar código automáticamente
        if (proveedor.getId() == null && (proveedor.getCodigo() == null || proveedor.getCodigo().isEmpty())) {
            proveedor.setCodigo(generarCodigoProveedor());
        }

        log.info("Guardando proveedor: {}", proveedor.getCodigo());
        return proveedorRepository.save(proveedor);
    }

    /**
     * Elimina un proveedor por ID
     */
    public void delete(Long id) {
        log.info("Eliminando proveedor con ID: {}", id);
        proveedorRepository.deleteById(id);
    }

    /**
     * Elimina un proveedor por ID (alias de delete)
     */
    public void deleteById(Long id) {
        delete(id);
    }

    /**
     * Activa un proveedor
     */
    public void activar(Long id) {
        proveedorRepository.findById(id).ifPresent(proveedor -> {
            proveedor.setActivo(true);
            proveedorRepository.save(proveedor);
            log.info("Proveedor activado: {}", proveedor.getCodigo());
        });
    }

    /**
     * Desactiva un proveedor
     */
    public void desactivar(Long id) {
        proveedorRepository.findById(id).ifPresent(proveedor -> {
            proveedor.setActivo(false);
            proveedorRepository.save(proveedor);
            log.info("Proveedor desactivado: {}", proveedor.getCodigo());
        });
    }

    /**
     * Busca proveedores por criterio (código, nombre o CIF)
     */
    public List<Proveedor> buscar(String criterio) {
        log.debug("Buscando proveedores con criterio: {}", criterio);
        return proveedorRepository.buscarPorCriterio(criterio);
    }

    /**
     * Genera un código único para un proveedor nuevo
     */
    private String generarCodigoProveedor() {
        List<Proveedor> proveedores = proveedorRepository.findAll();
        int maxNumero = 0;

        for (Proveedor p : proveedores) {
            if (p.getCodigo() != null && p.getCodigo().startsWith("PROV")) {
                try {
                    int numero = Integer.parseInt(p.getCodigo().substring(4));
                    if (numero > maxNumero) {
                        maxNumero = numero;
                    }
                } catch (NumberFormatException e) {
                    // Ignorar códigos que no sigan el patrón
                }
            }
        }

        return String.format("PROV%04d", maxNumero + 1);
    }
}

