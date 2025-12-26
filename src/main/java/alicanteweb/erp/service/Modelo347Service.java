package alicanteweb.erp.service;

import alicanteweb.erp.entities.EmpresaConfig;
import alicanteweb.erp.entities.Modelo347Registro;
import alicanteweb.erp.repository.LibroFacturasEmitidasRepository;
import alicanteweb.erp.repository.LibroFacturasRecibidasRepository;
import alicanteweb.erp.repository.Modelo347RegistroRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Servicio para generaciÃ³n del Modelo 347
 * DeclaraciÃ³n anual de operaciones con terceros > 3.005,06 euros
 */
@Service
@Slf4j
public class Modelo347Service {

    private static final BigDecimal LIMITE_DECLARACION = new BigDecimal("3005.06");

    private final Modelo347RegistroRepository registroRepository;
    private final LibroFacturasEmitidasRepository emitidasRepository;
    private final LibroFacturasRecibidasRepository recibidasRepository;
    private final EmpresaConfigService empresaConfigService;

    public Modelo347Service(Modelo347RegistroRepository registroRepository,
                           LibroFacturasEmitidasRepository emitidasRepository,
                           LibroFacturasRecibidasRepository recibidasRepository,
                           EmpresaConfigService empresaConfigService) {
        this.registroRepository = registroRepository;
        this.emitidasRepository = emitidasRepository;
        this.recibidasRepository = recibidasRepository;
        this.empresaConfigService = empresaConfigService;
    }

    /**
     * Genera el Modelo 347 para un ejercicio completo
     */
    @Transactional
    public Map<String, Object> generarModelo347(Integer ejercicio) {
        log.info("Generando Modelo 347 para ejercicio {}", ejercicio);

        Map<String, Object> resultado = new HashMap<>();

        // Datos del declarante
        EmpresaConfig empresa = empresaConfigService.getConfiguracionActivaOrThrow();
        resultado.put("nif_declarante", empresa.getCif());
        resultado.put("nombre_declarante", empresa.getNombreEmpresa());
        resultado.put("ejercicio", ejercicio);

        // Limpiar registros anteriores no confirmados
        registroRepository.deleteByEjercicioAndGeneradoFalse(ejercicio);

        // Generar registros de clientes (ventas/entregas - clave B)
        int registrosClientes = generarRegistrosClientes(ejercicio, empresa.getCif());
        log.info("Generados {} registros de clientes", registrosClientes);

        // Generar registros de proveedores (compras/adquisiciones - clave A)
        int registrosProveedores = generarRegistrosProveedores(ejercicio, empresa.getCif());
        log.info("Generados {} registros de proveedores", registrosProveedores);

        // Obtener todos los registros generados
        List<Modelo347Registro> registros = registroRepository.findByEjercicioAndGeneradoTrue(ejercicio);

        // EstadÃ­sticas
        BigDecimal totalVentas = registroRepository.sumImporteByEjercicioAndClave(ejercicio, "B");
        BigDecimal totalCompras = registroRepository.sumImporteByEjercicioAndClave(ejercicio, "A");

        resultado.put("total_registros", registros.size());
        resultado.put("registros_clientes", registrosClientes);
        resultado.put("registros_proveedores", registrosProveedores);
        resultado.put("total_ventas", totalVentas != null ? totalVentas : BigDecimal.ZERO);
        resultado.put("total_compras", totalCompras != null ? totalCompras : BigDecimal.ZERO);
        resultado.put("registros", registros);

        log.info("Modelo 347 generado: {} registros totales", registros.size());

        return resultado;
    }

    /**
     * Genera registros de clientes (ventas)
     */
    private int generarRegistrosClientes(Integer ejercicio, String nifDeclarante) {
        // En una implementaciÃ³n real, consultarÃ­amos el libro de facturas emitidas
        // agrupado por cliente y sumando importes por trimestre

        // SimulaciÃ³n: obtener datos desde libro facturas emitidas
        // Query: SELECT cliente_cif, cliente_nombre, SUM(total) FROM libro_facturas_emitidas
        //        WHERE ejercicio = ? GROUP BY cliente_cif HAVING SUM(total) >= 3005.06

        log.info("Generando registros de clientes para ejercicio {}", ejercicio);

        // Por ahora retornamos 0 - en producciÃ³n se implementarÃ­a la lÃ³gica completa
        return 0;
    }

    /**
     * Genera registros de proveedores (compras)
     */
    private int generarRegistrosProveedores(Integer ejercicio, String nifDeclarante) {
        // En una implementaciÃ³n real, consultarÃ­amos el libro de facturas recibidas
        // agrupado por proveedor y sumando importes por trimestre

        log.info("Generando registros de proveedores para ejercicio {}", ejercicio);

        // Por ahora retornamos 0 - en producciÃ³n se implementarÃ­a la lÃ³gica completa
        return 0;
    }

    /**
     * Exporta a formato BOE (archivo .347)
     */
    public String exportarABOE(Integer ejercicio) {
        List<Modelo347Registro> registros = registroRepository.findByEjercicioAndGeneradoTrue(ejercicio);
        EmpresaConfig empresa = empresaConfigService.getConfiguracionActivaOrThrow();

        StringBuilder boe = new StringBuilder();

        // Registro tipo 1 - Presentador
        boe.append("1"); // Tipo de registro
        boe.append("347"); // Modelo
        boe.append(String.format("%04d", ejercicio)); // Ejercicio
        boe.append(String.format("%-9s", empresa.getCif())); // NIF declarante
        boe.append(String.format("%-40s", empresa.getNombreEmpresa())); // Nombre
        boe.append("T"); // Soporte (T=TelemÃ¡tico)
        boe.append(String.format("%09d", 0)); // TelÃ©fono
        boe.append(String.format("%-40s", "")); // Persona contacto
        boe.append(String.format("%013d", registros.size())); // NÃºmero declarados
        boe.append(String.format("%017d", calcularImporteTotalDeclarado(registros))); // Importe total
        boe.append(String.format("%-505s", "")); // Blancos
        boe.append("\n");

        // Registro tipo 2 - Declarados
        for (Modelo347Registro registro : registros) {
            boe.append("2"); // Tipo de registro
            boe.append("347"); // Modelo
            boe.append(String.format("%04d", ejercicio)); // Ejercicio
            boe.append(String.format("%-9s", registro.getNifDeclarante())); // NIF declarante
            boe.append(String.format("%-9s", registro.getNifTercero())); // NIF declarado
            boe.append(String.format("%-40s", registro.getNombreTercero())); // Nombre declarado
            boe.append(registro.getClaveOperacion()); // Clave operaciÃ³n
            boe.append(String.format("%016d", convertirAEntero(registro.getImportePrimerTrimestre()))); // 1T
            boe.append(String.format("%016d", convertirAEntero(registro.getImporteSegundoTrimestre()))); // 2T
            boe.append(String.format("%016d", convertirAEntero(registro.getImporteTercerTrimestre()))); // 3T
            boe.append(String.format("%016d", convertirAEntero(registro.getImporteCuartoTrimestre()))); // 4T
            boe.append(String.format("%016d", convertirAEntero(registro.getImporteOperaciones()))); // Total
            boe.append(" "); // NIF operador comunitario
            boe.append(" "); // OperaciÃ³n con IGI/IPSI
            boe.append(" "); // IVA deducible
            boe.append(" "); // OperaciÃ³n seguro/capitalizaciÃ³n
            boe.append(" "); // Arrendamiento local negocio
            boe.append(String.format("%016d", 0)); // Importe metÃ¡lico
            boe.append(String.format("%04d", registro.getNumeroFacturas() != null ? registro.getNumeroFacturas() : 0));
            boe.append(String.format("%-293s", "")); // Blancos
            boe.append("\n");
        }

        return boe.toString();
    }

    /**
     * Valida si el modelo puede generarse
     */
    public boolean validarModelo(Integer ejercicio) {
        if (ejercicio == null || ejercicio < 2000 || ejercicio > 2100) {
            log.warn("Ejercicio invÃ¡lido: {}", ejercicio);
            return false;
        }

        if (!empresaConfigService.existeConfiguracionActiva()) {
            log.warn("No existe configuraciÃ³n de empresa");
            return false;
        }

        return true;
    }

    /**
     * Obtiene registros generados
     */
    public List<Modelo347Registro> obtenerRegistros(Integer ejercicio) {
        return registroRepository.findByEjercicioOrderByNifTerceroAsc(ejercicio);
    }

    /**
     * Marca registros como incluidos en declaraciÃ³n
     */
    @Transactional
    public void marcarComoDeclarados(Integer ejercicio) {
        List<Modelo347Registro> registros = registroRepository.findByEjercicioAndGeneradoTrue(ejercicio);
        registros.forEach(r -> r.setIncluidoEnDeclaracion(true));
        registroRepository.saveAll(registros);
        log.info("Marcados {} registros como declarados", registros.size());
    }

    // MÃ©todos auxiliares privados

    private long calcularImporteTotalDeclarado(List<Modelo347Registro> registros) {
        return registros.stream()
                .map(Modelo347Registro::getImporteOperaciones)
                .map(this::convertirAEntero)
                .mapToLong(Long::longValue)
                .sum();
    }

    private long convertirAEntero(BigDecimal valor) {
        if (valor == null) return 0;
        return valor.multiply(new BigDecimal("100")).longValue();
    }
}


