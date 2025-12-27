package alicanteweb.erp.service;

import alicanteweb.erp.entities.EmpresaConfig;
import alicanteweb.erp.repository.LibroFacturasEmitidasRepository;
import alicanteweb.erp.repository.LibroFacturasRecibidasRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

/**
 * Servicio para generaciÃ³n del Modelo 390
 * Resumen anual del IVA
 */
@Service
@Slf4j
public class Modelo390Service {

    private final LibroFacturasEmitidasRepository emitidasRepository;
    private final LibroFacturasRecibidasRepository recibidasRepository;
    private final Modelo303Service modelo303Service;
    private final EmpresaConfigService empresaConfigService;

    public Modelo390Service(LibroFacturasEmitidasRepository emitidasRepository,
                           LibroFacturasRecibidasRepository recibidasRepository,
                           Modelo303Service modelo303Service,
                           EmpresaConfigService empresaConfigService) {
        this.emitidasRepository = emitidasRepository;
        this.recibidasRepository = recibidasRepository;
        this.modelo303Service = modelo303Service;
        this.empresaConfigService = empresaConfigService;
    }

    /**
     * Genera el Modelo 390 para un ejercicio completo
     */
    public Map<String, Object> generarModelo390(Integer ejercicio) {
        log.info("Generando Modelo 390 para ejercicio {}", ejercicio);

        Map<String, Object> modelo = new HashMap<>();

        // Datos del declarante
        EmpresaConfig empresa = empresaConfigService.getConfiguracionActivaOrThrow();
        modelo.put("nif", empresa.getCif());
        modelo.put("nombre", empresa.getNombreEmpresa());
        modelo.put("ejercicio", ejercicio);

        // Resumen de los 4 trimestres (basado en Modelo 303)
        Map<String, Object>[] trimestres = new Map[4];
        BigDecimal totalDevengadoAnual = BigDecimal.ZERO;
        BigDecimal totalDeducibleAnual = BigDecimal.ZERO;
        BigDecimal resultadoAnual = BigDecimal.ZERO;

        for (int trimestre = 1; trimestre <= 4; trimestre++) {
            Map<String, Object> modelo303 = modelo303Service.generarModelo303(ejercicio, trimestre);
            trimestres[trimestre - 1] = modelo303;

            Map<String, BigDecimal> devengado = (Map<String, BigDecimal>) modelo303.get("iva_devengado");
            Map<String, BigDecimal> deducible = (Map<String, BigDecimal>) modelo303.get("iva_deducible");
            BigDecimal resultado = (BigDecimal) modelo303.get("resultado");

            totalDevengadoAnual = totalDevengadoAnual.add(devengado.get("total_cuota"));
            totalDeducibleAnual = totalDeducibleAnual.add(deducible.get("total_deducible"));
            resultadoAnual = resultadoAnual.add(resultado);
        }

        modelo.put("trimestres", trimestres);
        modelo.put("total_devengado_anual", totalDevengadoAnual);
        modelo.put("total_deducible_anual", totalDeducibleAnual);
        modelo.put("resultado_anual", resultadoAnual);

        // Operaciones especiales del aÃ±o
        Map<String, Object> operacionesEspeciales = calcularOperacionesEspeciales(ejercicio);
        modelo.put("operaciones_especiales", operacionesEspeciales);

        // Casillas especÃ­ficas del Modelo 390
        Map<String, Object> casillas = generarCasillas(trimestres, totalDevengadoAnual,
                                                       totalDeducibleAnual, resultadoAnual);
        modelo.put("casillas", casillas);

        log.info("Modelo 390 generado. Resultado anual: {}", resultadoAnual);

        return modelo;
    }

    /**
     * Calcula operaciones especiales del ejercicio
     */
    private Map<String, Object> calcularOperacionesEspeciales(Integer ejercicio) {
        Map<String, Object> especiales = new HashMap<>();

        // Operaciones intracomunitarias
        Long numIntracomunitarias = emitidasRepository.findByIntracomunitariaTrue().stream()
                .filter(f -> f.getEjercicio().equals(ejercicio))
                .count();
        especiales.put("intracomunitarias", numIntracomunitarias);

        // Exportaciones
        Long numExportaciones = emitidasRepository.findByExportacionTrue().stream()
                .filter(f -> f.getEjercicio().equals(ejercicio))
                .count();
        especiales.put("exportaciones", numExportaciones);

        // Importaciones
        Long numImportaciones = recibidasRepository.findByImportacionTrue().stream()
                .filter(f -> f.getEjercicio().equals(ejercicio))
                .count();
        especiales.put("importaciones", numImportaciones);

        // InversiÃ³n sujeto pasivo
        Long numInversionSP = recibidasRepository.findByInversionSujetoPasivoTrue().stream()
                .filter(f -> f.getEjercicio().equals(ejercicio))
                .count();
        especiales.put("inversion_sujeto_pasivo", numInversionSP);

        return especiales;
    }

    /**
     * Genera las casillas del Modelo 390
     */
    private Map<String, Object> generarCasillas(Map<String, Object>[] trimestres,
                                                BigDecimal totalDevengado,
                                                BigDecimal totalDeducible,
                                                BigDecimal resultado) {
        Map<String, Object> casillas = new HashMap<>();

        // IVA DEVENGADO ANUAL
        casillas.put("casilla_80", totalDevengado); // Total devengado

        // IVA DEDUCIBLE ANUAL
        casillas.put("casilla_94", totalDeducible); // Total deducible

        // RESULTADO
        casillas.put("casilla_110", totalDevengado); // Suma cuotas devengadas
        casillas.put("casilla_111", totalDeducible); // Suma cuotas deducibles

        if (resultado.compareTo(BigDecimal.ZERO) > 0) {
            casillas.put("casilla_112", resultado); // Resultado positivo (a ingresar)
        } else {
            casillas.put("casilla_113", resultado.abs()); // Resultado negativo (a compensar)
        }

        // Resumen por trimestres
        for (int i = 0; i < 4; i++) {
            Map<String, Object> trim = trimestres[i];
            casillas.put("trimestre_" + (i + 1) + "_resultado", trim.get("resultado"));
        }

        return casillas;
    }

    /**
     * Exporta el Modelo 390 a formato texto
     */
    public String exportarATXT(Integer ejercicio) {
        Map<String, Object> modelo = generarModelo390(ejercicio);

        StringBuilder txt = new StringBuilder();
        txt.append("MODELO 390 - DeclaraciÃ³n-Resumen Anual del IVA\n");
        txt.append("==============================================\n\n");
        txt.append("EJERCICIO: ").append(ejercicio).append("\n\n");
        txt.append("DECLARANTE:\n");
        txt.append("NIF: ").append(modelo.get("nif")).append("\n");
        txt.append("Nombre: ").append(modelo.get("nombre")).append("\n\n");

        txt.append("RESUMEN ANUAL:\n");
        txt.append("Total IVA Devengado: ").append(formatCurrency((BigDecimal) modelo.get("total_devengado_anual"))).append("\n");
        txt.append("Total IVA Deducible: ").append(formatCurrency((BigDecimal) modelo.get("total_deducible_anual"))).append("\n");
        txt.append("Resultado Anual: ").append(formatCurrency((BigDecimal) modelo.get("resultado_anual"))).append("\n\n");

        txt.append("RESUMEN POR TRIMESTRES:\n");
        Map<String, Object>[] trimestres = (Map<String, Object>[]) modelo.get("trimestres");
        for (int i = 0; i < 4; i++) {
            txt.append("Trimestre ").append(i + 1).append(": ");
            txt.append(formatCurrency((BigDecimal) trimestres[i].get("resultado"))).append("\n");
        }

        txt.append("\nOPERACIONES ESPECIALES:\n");
        Map<String, Object> especiales = (Map<String, Object>) modelo.get("operaciones_especiales");
        txt.append("Intracomunitarias: ").append(especiales.get("intracomunitarias")).append("\n");
        txt.append("Exportaciones: ").append(especiales.get("exportaciones")).append("\n");
        txt.append("Importaciones: ").append(especiales.get("importaciones")).append("\n");
        txt.append("InversiÃ³n Sujeto Pasivo: ").append(especiales.get("inversion_sujeto_pasivo")).append("\n");

        return txt.toString();
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

        // Verificar que existan datos para los 4 trimestres
        for (int trimestre = 1; trimestre <= 4; trimestre++) {
            if (!modelo303Service.validarModelo(ejercicio, trimestre)) {
                log.warn("No hay datos vÃ¡lidos para el trimestre {}", trimestre);
                return false;
            }
        }

        return true;
    }

    private String formatCurrency(BigDecimal value) {
        if (value == null) return "0,00 â‚¬";
        return String.format("%,.2f â‚¬", value);
    }
}


