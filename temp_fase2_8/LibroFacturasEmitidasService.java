package alicanteweb.erp.service;

import alicanteweb.erp.entities.Factura;
import alicanteweb.erp.entities.FacturaLinea;
import alicanteweb.erp.entities.LibroFacturasEmitidas;
import alicanteweb.erp.repository.LibroFacturasEmitidasRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Servicio para gestiÃ³n del Libro de Facturas Emitidas
 * Registro oficial segÃºn Reglamento IVA
 */
@Service
@Slf4j
public class LibroFacturasEmitidasService {

    private final LibroFacturasEmitidasRepository repository;
    private final FacturaLineaService facturaLineaService;

    public LibroFacturasEmitidasService(LibroFacturasEmitidasRepository repository,
                                       FacturaLineaService facturaLineaService) {
        this.repository = repository;
        this.facturaLineaService = facturaLineaService;
    }

    /**
     * Registra una factura en el libro de facturas emitidas
     */
    @Transactional
    public LibroFacturasEmitidas registrarFactura(Factura factura) {
        log.info("Registrando factura {} en libro de facturas emitidas", factura.getNumero());

        LibroFacturasEmitidas registro = new LibroFacturasEmitidas();

        // Datos bÃ¡sicos
        registro.setFactura(factura);
        registro.setFechaExpedicion(factura.getFecha());
        registro.setFechaOperacion(factura.getFechaOperacion());
        registro.setSerie(factura.getSerie());
        registro.setNumero(factura.getNumero());
        registro.setNumeroFacturaCompleto(factura.getNumero());
        registro.setTipoFactura(factura.getTipoFactura());

        // Cliente
        if (factura.getCliente() != null) {
            registro.setCifDestinatario(factura.getCliente().getCif());
            registro.setNombreDestinatario(factura.getCliente().getNombre());
        }

        // Importes
        registro.setBaseImponible(factura.getBaseImponible() != null ? factura.getBaseImponible() : BigDecimal.ZERO);
        registro.setCuotaIva(factura.getTotalIva() != null ? factura.getTotalIva() : BigDecimal.ZERO);
        registro.setCuotaRecargo(factura.getTotalRecargo() != null ? factura.getTotalRecargo() : BigDecimal.ZERO);
        registro.setTotalFactura(factura.getTotal() != null ? factura.getTotal() : BigDecimal.ZERO);

        // Tipo IVA promedio (si hay lÃ­neas)
        BigDecimal tipoIva = calcularTipoIvaPromedio(factura);
        registro.setTipoIva(tipoIva);

        // Tipo recargo si aplica
        if (factura.getTotalRecargo() != null && factura.getTotalRecargo().compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal tipoRecargo = calcularTipoRecargoPromedio(factura);
            registro.setTipoRecargo(tipoRecargo);
        }

        // RÃ©gimen especial
        if (factura.getCriterioCaja() != null && factura.getCriterioCaja()) {
            registro.setRegimenEspecial("CRITERIO_CAJA");
        }

        // Factura rectificativa
        if ("RECTIFICATIVA".equals(factura.getTipoFactura())) {
            registro.setFacturaRectificativaTipo("S"); // SustituciÃ³n por defecto
            if (factura.getFacturaRectificadaNumero() != null) {
                registro.setFacturaRectificativaBase(factura.getBaseImponible());
                registro.setFacturaRectificativaCuota(factura.getTotalIva());
            }
        }

        // Operaciones especiales
        registro.setIntracomunitaria(factura.getOperacionTriangular() != null && factura.getOperacionTriangular());
        registro.setExportacion(false); // TODO: Detectar exportaciones

        // Clave operaciÃ³n SII
        registro.setClaveOperacion(determinarClaveOperacion(factura));

        // Ejercicio y periodo
        Integer ejercicio = factura.getFecha().getYear();
        Integer periodo = (factura.getFecha().getMonthValue() - 1) / 3 + 1;
        registro.setEjercicio(ejercicio);
        registro.setPeriodo(periodo);

        // NÃºmero de registro correlativo
        Integer maxRegistro = repository.findMaxNumeroRegistro(ejercicio, periodo);
        registro.setNumeroRegistro(maxRegistro + 1);

        LibroFacturasEmitidas guardado = repository.save(registro);
        log.info("Factura {} registrada con nÃºmero de registro {}", factura.getNumero(), guardado.getNumeroRegistro());

        return guardado;
    }

    /**
     * Obtiene el libro por ejercicio y periodo
     */
    public List<LibroFacturasEmitidas> obtenerLibro(Integer ejercicio, Integer periodo) {
        return repository.findByEjercicioAndPeriodoOrderByNumeroRegistroAsc(ejercicio, periodo);
    }

    /**
     * Obtiene el libro por ejercicio completo
     */
    public List<LibroFacturasEmitidas> obtenerLibroAnual(Integer ejercicio) {
        return repository.findByEjercicioOrderByNumeroRegistroAsc(ejercicio);
    }

    /**
     * Obtiene resumen del libro por tipo de IVA
     */
    public List<Object[]> obtenerResumenPorTipoIva(Integer ejercicio, Integer periodo) {
        return repository.resumenPorTipoIva(ejercicio, periodo);
    }

    /**
     * Genera resumen para Modelo 303
     */
    public Map<String, BigDecimal> generarResumen303(Integer ejercicio, Integer periodo) {
        Map<String, BigDecimal> resumen = new HashMap<>();

        BigDecimal totalBase = repository.sumBaseImponibleByEjercicioAndPeriodo(ejercicio, periodo);
        BigDecimal totalCuota = repository.sumCuotaIvaByEjercicioAndPeriodo(ejercicio, periodo);

        resumen.put("base_imponible", totalBase != null ? totalBase : BigDecimal.ZERO);
        resumen.put("cuota_iva", totalCuota != null ? totalCuota : BigDecimal.ZERO);

        return resumen;
    }

    /**
     * Exportar a XML para Registro Mercantil
     */
    public String exportarAXML(Integer ejercicio, Integer periodo) {
        List<LibroFacturasEmitidas> registros = obtenerLibro(ejercicio, periodo);

        StringBuilder xml = new StringBuilder();
        xml.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        xml.append("<LibroFacturasEmitidas>\n");
        xml.append("  <Ejercicio>").append(ejercicio).append("</Ejercicio>\n");
        xml.append("  <Periodo>").append(periodo).append("</Periodo>\n");
        xml.append("  <Registros>\n");

        for (LibroFacturasEmitidas registro : registros) {
            xml.append("    <Registro>\n");
            xml.append("      <NumeroRegistro>").append(registro.getNumeroRegistro()).append("</NumeroRegistro>\n");
            xml.append("      <FechaExpedicion>").append(registro.getFechaExpedicion()).append("</FechaExpedicion>\n");
            xml.append("      <NumeroFactura>").append(xmlEscape(registro.getNumeroFacturaCompleto())).append("</NumeroFactura>\n");
            xml.append("      <CifDestinatario>").append(xmlEscape(registro.getCifDestinatario())).append("</CifDestinatario>\n");
            xml.append("      <NombreDestinatario>").append(xmlEscape(registro.getNombreDestinatario())).append("</NombreDestinatario>\n");
            xml.append("      <BaseImponible>").append(registro.getBaseImponible()).append("</BaseImponible>\n");
            xml.append("      <TipoIVA>").append(registro.getTipoIva()).append("</TipoIVA>\n");
            xml.append("      <CuotaIVA>").append(registro.getCuotaIva()).append("</CuotaIVA>\n");
            xml.append("      <TotalFactura>").append(registro.getTotalFactura()).append("</TotalFactura>\n");
            xml.append("    </Registro>\n");
        }

        xml.append("  </Registros>\n");
        xml.append("</LibroFacturasEmitidas>");

        return xml.toString();
    }

    // MÃ©todos auxiliares privados

    private BigDecimal calcularTipoIvaPromedio(Factura factura) {
        if (factura.getId() == null) return BigDecimal.ZERO;

        List<FacturaLinea> lineas = facturaLineaService.findByFacturaId(factura.getId());
        if (lineas.isEmpty()) return BigDecimal.ZERO;

        BigDecimal sumaIvas = BigDecimal.ZERO;
        for (FacturaLinea linea : lineas) {
            if (linea.getIva() != null) {
                sumaIvas = sumaIvas.add(linea.getIva());
            }
        }

        return sumaIvas.divide(new BigDecimal(lineas.size()), 2, java.math.RoundingMode.HALF_UP);
    }

    private BigDecimal calcularTipoRecargoPromedio(Factura factura) {
        if (factura.getId() == null) return BigDecimal.ZERO;

        List<FacturaLinea> lineas = facturaLineaService.findByFacturaId(factura.getId());
        if (lineas.isEmpty()) return BigDecimal.ZERO;

        BigDecimal sumaRecargos = BigDecimal.ZERO;
        int count = 0;
        for (FacturaLinea linea : lineas) {
            if (linea.getRecargo() != null && linea.getRecargo().compareTo(BigDecimal.ZERO) > 0) {
                sumaRecargos = sumaRecargos.add(linea.getRecargo());
                count++;
            }
        }

        if (count == 0) return BigDecimal.ZERO;
        return sumaRecargos.divide(new BigDecimal(count), 2, java.math.RoundingMode.HALF_UP);
    }

    private String determinarClaveOperacion(Factura factura) {
        if ("RECTIFICATIVA".equals(factura.getTipoFactura())) {
            return "R1"; // Factura rectificativa
        }
        if (factura.getOperacionTriangular() != null && factura.getOperacionTriangular()) {
            return "N1"; // OperaciÃ³n triangular
        }
        return "F1"; // Factura ordinaria
    }

    private String xmlEscape(String text) {
        if (text == null) return "";
        return text.replace("&", "&amp;")
                  .replace("<", "&lt;")
                  .replace(">", "&gt;")
                  .replace("\"", "&quot;")
                  .replace("'", "&apos;");
    }
}


