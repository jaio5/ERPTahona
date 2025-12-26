package alicanteweb.erp.service;

import alicanteweb.erp.entities.EmpresaConfig;
import alicanteweb.erp.repository.LibroFacturasEmitidasRepository;
import alicanteweb.erp.repository.LibroFacturasRecibidasRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Servicio para generaciÃ³n del Modelo 303 - AutoliquidaciÃ³n de IVA
 * Modelo oficial de la AEAT
 */
@Service
@Slf4j
public class Modelo303Service {

    private final LibroFacturasEmitidasRepository emitidasRepository;
    private final LibroFacturasRecibidasRepository recibidasRepository;
    private final EmpresaConfigService empresaConfigService;

    public Modelo303Service(LibroFacturasEmitidasRepository emitidasRepository,
                           LibroFacturasRecibidasRepository recibidasRepository,
                           EmpresaConfigService empresaConfigService) {
        this.emitidasRepository = emitidasRepository;
        this.recibidasRepository = recibidasRepository;
        this.empresaConfigService = empresaConfigService;
    }

    /**
     * Genera el Modelo 303 para un ejercicio y periodo
     */
    public Map<String, Object> generarModelo303(Integer ejercicio, Integer periodo) {
        log.info("Generando Modelo 303 para ejercicio {} periodo {}", ejercicio, periodo);

        Map<String, Object> modelo = new HashMap<>();

        // Datos del declarante
        EmpresaConfig empresa = empresaConfigService.getConfiguracionActivaOrThrow();
        modelo.put("nif", empresa.getCif());
        modelo.put("nombre", empresa.getNombreEmpresa());
        modelo.put("ejercicio", ejercicio);
        modelo.put("periodo", periodo);

        // IVA DEVENGADO (Facturas emitidas)
        Map<String, BigDecimal> ivaDevengado = calcularIvaDevengado(ejercicio, periodo);
        modelo.put("iva_devengado", ivaDevengado);

        // IVA DEDUCIBLE (Facturas recibidas)
        Map<String, BigDecimal> ivaDeducible = calcularIvaDeducible(ejercicio, periodo);
        modelo.put("iva_deducible", ivaDeducible);

        // RESULTADO
        BigDecimal totalDevengado = (BigDecimal) ivaDevengado.get("total_cuota");
        BigDecimal totalDeducible = (BigDecimal) ivaDeducible.get("total_deducible");
        BigDecimal resultado = totalDevengado.subtract(totalDeducible);

        modelo.put("resultado", resultado);
        modelo.put("tipo_resultado", resultado.compareTo(BigDecimal.ZERO) > 0 ? "A_INGRESAR" : "A_COMPENSAR");

        // Casillas especÃ­ficas del modelo
        Map<String, Object> casillas = generarCasillas(ivaDevengado, ivaDeducible, resultado);
        modelo.put("casillas", casillas);

        log.info("Modelo 303 generado. Resultado: {} ({})", resultado, modelo.get("tipo_resultado"));

        return modelo;
    }

    /**
     * Calcula el IVA devengado (facturas emitidas)
     */
    private Map<String, BigDecimal> calcularIvaDevengado(Integer ejercicio, Integer periodo) {
        Map<String, BigDecimal> devengado = new HashMap<>();

        // Obtener resumen por tipos de IVA
        List<Object[]> resumen = emitidasRepository.resumenPorTipoIva(ejercicio, periodo);

        BigDecimal totalBase = BigDecimal.ZERO;
        BigDecimal totalCuota = BigDecimal.ZERO;

        Map<String, Map<String, BigDecimal>> porTipo = new HashMap<>();

        for (Object[] row : resumen) {
            BigDecimal tipoIva = (BigDecimal) row[0];
            BigDecimal base = (BigDecimal) row[1];
            BigDecimal cuota = (BigDecimal) row[2];

            totalBase = totalBase.add(base);
            totalCuota = totalCuota.add(cuota);

            Map<String, BigDecimal> datos = new HashMap<>();
            datos.put("base", base);
            datos.put("cuota", cuota);
            porTipo.put(tipoIva.toString(), datos);
        }

        devengado.put("total_base", totalBase);
        devengado.put("total_cuota", totalCuota);
        devengado.put("por_tipo", (BigDecimal) null); // Placeholder, se rellena con porTipo

        return devengado;
    }

    /**
     * Calcula el IVA deducible (facturas recibidas)
     */
    private Map<String, BigDecimal> calcularIvaDeducible(Integer ejercicio, Integer periodo) {
        Map<String, BigDecimal> deducible = new HashMap<>();

        // Obtener resumen por tipos de IVA
        List<Object[]> resumen = recibidasRepository.resumenPorTipoIva(ejercicio, periodo);

        BigDecimal totalBase = BigDecimal.ZERO;
        BigDecimal totalSoportado = BigDecimal.ZERO;
        BigDecimal totalDeducible = BigDecimal.ZERO;

        for (Object[] row : resumen) {
            BigDecimal tipoIva = (BigDecimal) row[0];
            BigDecimal base = (BigDecimal) row[1];
            BigDecimal soportado = (BigDecimal) row[2];
            BigDecimal deducibleIva = (BigDecimal) row[3];

            totalBase = totalBase.add(base);
            totalSoportado = totalSoportado.add(soportado);
            totalDeducible = totalDeducible.add(deducibleIva);
        }

        deducible.put("total_base", totalBase);
        deducible.put("total_soportado", totalSoportado);
        deducible.put("total_deducible", totalDeducible);

        return deducible;
    }

    /**
     * Genera las casillas del Modelo 303
     */
    private Map<String, Object> generarCasillas(Map<String, BigDecimal> devengado,
                                                Map<String, BigDecimal> deducible,
                                                BigDecimal resultado) {
        Map<String, Object> casillas = new HashMap<>();

        // IVA DEVENGADO
        // Casilla 01-03: Base imponible al 21%
        // Casilla 04-06: Base imponible al 10%
        // Casilla 07-09: Base imponible al 4%
        // etc.

        // Simplificado: solo totales
        casillas.put("casilla_01", devengado.get("total_base")); // Base imponible total
        casillas.put("casilla_02", devengado.get("total_cuota")); // Cuota devengada total

        // IVA DEDUCIBLE
        casillas.put("casilla_28", deducible.get("total_base")); // Compras corrientes
        casillas.put("casilla_29", deducible.get("total_deducible")); // Cuota deducible

        // RESULTADO
        casillas.put("casilla_46", devengado.get("total_cuota")); // Total devengado
        casillas.put("casilla_47", deducible.get("total_deducible")); // Total deducible

        if (resultado.compareTo(BigDecimal.ZERO) > 0) {
            casillas.put("casilla_69", resultado); // A ingresar
        } else {
            casillas.put("casilla_70", resultado.abs()); // A compensar
        }

        return casillas;
    }

    /**
     * Exporta el Modelo 303 a formato texto (para importar en web AEAT)
     */
    public String exportarATXT(Integer ejercicio, Integer periodo) {
        Map<String, Object> modelo = generarModelo303(ejercicio, periodo);

        StringBuilder txt = new StringBuilder();
        txt.append("MODELO 303 - AutoliquidaciÃ³n de IVA\n");
        txt.append("=====================================\n\n");
        txt.append("EJERCICIO: ").append(ejercicio).append("\n");
        txt.append("PERIODO: ").append(periodo).append("T\n\n");
        txt.append("DECLARANTE:\n");
        txt.append("NIF: ").append(modelo.get("nif")).append("\n");
        txt.append("Nombre: ").append(modelo.get("nombre")).append("\n\n");

        txt.append("IVA DEVENGADO:\n");
        Map<String, BigDecimal> devengado = (Map<String, BigDecimal>) modelo.get("iva_devengado");
        txt.append("Base imponible: ").append(formatCurrency(devengado.get("total_base"))).append("\n");
        txt.append("Cuota devengada: ").append(formatCurrency(devengado.get("total_cuota"))).append("\n\n");

        txt.append("IVA DEDUCIBLE:\n");
        Map<String, BigDecimal> deducible = (Map<String, BigDecimal>) modelo.get("iva_deducible");
        txt.append("Base compras: ").append(formatCurrency(deducible.get("total_base"))).append("\n");
        txt.append("Cuota deducible: ").append(formatCurrency(deducible.get("total_deducible"))).append("\n\n");

        txt.append("RESULTADO:\n");
        BigDecimal resultado = (BigDecimal) modelo.get("resultado");
        txt.append(modelo.get("tipo_resultado")).append(": ").append(formatCurrency(resultado.abs())).append("\n");

        return txt.toString();
    }

    /**
     * Valida si el modelo puede generarse
     */
    public boolean validarModelo(Integer ejercicio, Integer periodo) {
        if (ejercicio == null || periodo == null) {
            log.warn("Ejercicio o periodo nulo");
            return false;
        }

        if (periodo < 1 || periodo > 4) {
            log.warn("Periodo invÃ¡lido: {}", periodo);
            return false;
        }

        if (!empresaConfigService.existeConfiguracionActiva()) {
            log.warn("No existe configuraciÃ³n de empresa");
            return false;
        }

        return true;
    }

    private String formatCurrency(BigDecimal value) {
        if (value == null) return "0,00";
        return String.format("%,.2f â‚¬", value);
    }
}


