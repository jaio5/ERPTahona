package alicanteweb.erp.service;

import alicanteweb.erp.entities.Factura;
import alicanteweb.erp.repository.FacturaRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Year;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Servicio para generación automática del Modelo 347
 */
@Service
@Transactional
public class Modelo347Service {
    private static final Logger log = LoggerFactory.getLogger(Modelo347Service.class);

    private final FacturaRepository facturaRepository;

    public Modelo347Service(FacturaRepository facturaRepository) {
        this.facturaRepository = facturaRepository;
    }

    public Map<String, Object> generarModelo347(int anyo) {
        log.info("Generando Modelo 347 para el año {}", anyo);

        Map<String, Object> resultado = new HashMap<>();

        try {
            List<Factura> facturas = obtenerFacturasDelAnio(anyo);

            if (facturas.isEmpty()) {
                resultado.put("exito", false);
                resultado.put("mensaje", "No hay facturas para el año " + anyo);
                return resultado;
            }

            String xmlModelo347 = generarXMLModelo347(anyo, facturas);

            resultado.put("exito", true);
            resultado.put("anyo", anyo);
            resultado.put("total_registros", facturas.size());
            resultado.put("xml", xmlModelo347);

            log.info("Modelo 347 generado: {} registros", facturas.size());

        } catch (Exception e) {
            log.error("Error generando Modelo 347", e);
            resultado.put("exito", false);
            resultado.put("mensaje", "Error: " + e.getMessage());
        }

        return resultado;
    }

    private List<Factura> obtenerFacturasDelAnio(int anyo) {
        return facturaRepository.findAll().stream()
            .filter(f -> f.getFecha() != null && f.getFecha().getYear() == anyo)
            .filter(f -> f.getTotal() != null && f.getTotal().signum() > 0)
            .collect(Collectors.toList());
    }

    private String generarXMLModelo347(int anyo, List<Factura> facturas) {
        StringBuilder xml = new StringBuilder();
        xml.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        xml.append("<Modelo347>\n");
        xml.append("  <Anyo>").append(anyo).append("</Anyo>\n");
        xml.append("  <FechaGeneracion>").append(java.time.LocalDate.now()).append("</FechaGeneracion>\n");
        xml.append("  <TotalRegistros>").append(facturas.size()).append("</TotalRegistros>\n");
        xml.append("</Modelo347>\n");

        return xml.toString();
    }

    public String exportarModelo347(String xmlModelo347, int anyo) {
        try {
            String nombreArchivo = String.format("Modelo347_%d_%d.xml", anyo, System.currentTimeMillis());
            String ruta = "target/reportes/" + nombreArchivo;

            java.nio.file.Files.write(
                java.nio.file.Paths.get(ruta),
                xmlModelo347.getBytes("UTF-8")
            );

            log.info("Modelo 347 exportado a: {}", ruta);
            return ruta;
        } catch (Exception e) {
            log.error("Error exportando Modelo 347", e);
            throw new RuntimeException("Error al exportar Modelo 347", e);
        }
    }
}

