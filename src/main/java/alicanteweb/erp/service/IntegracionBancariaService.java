package alicanteweb.erp.service;

import alicanteweb.erp.entities.MovimientoBanco;
import alicanteweb.erp.repository.MovimientoBancoRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
@Transactional
public class IntegracionBancariaService {
    private static final Logger log = LoggerFactory.getLogger(IntegracionBancariaService.class);

    private final MovimientoBancoRepository movimientoBancoRepository;
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public IntegracionBancariaService(MovimientoBancoRepository movimientoBancoRepository) {
        this.movimientoBancoRepository = movimientoBancoRepository;
    }

    public Map<String, Object> importarMovimientosCSV(String rutaArchivo) {
        log.info("Importando movimientos desde: {}", rutaArchivo);

        Map<String, Object> resultado = new HashMap<>();
        int importados = 0;
        int errores = 0;

        try {
            File archivo = new File(rutaArchivo);
            if (!archivo.exists()) {
                resultado.put("exito", false);
                resultado.put("mensaje", "Archivo no encontrado");
                return resultado;
            }

            BufferedReader reader = new BufferedReader(new FileReader(archivo));
            String linea;

            while ((linea = reader.readLine()) != null) {
                try {
                    String[] partes = linea.split(",");
                    if (partes.length < 3) {
                        errores++;
                        continue;
                    }

                    LocalDate fecha = LocalDate.parse(partes[0], DATE_FORMAT);
                    String concepto = partes[1];
                    BigDecimal importe = new BigDecimal(partes[2]);
                    String referencia = partes.length > 3 ? partes[3] : "";

                    MovimientoBanco movimiento = new MovimientoBanco();
                    movimiento.setFecha(fecha);
                    movimiento.setConcepto(concepto);
                    movimiento.setImporte(importe);
                    movimiento.setObservaciones(referencia);
                    movimiento.setConciliado(false);

                    movimientoBancoRepository.save(movimiento);
                    importados++;

                } catch (Exception e) {
                    log.warn("Error procesando línea: {}", linea, e);
                    errores++;
                }
            }

            reader.close();

            resultado.put("exito", true);
            resultado.put("importados", importados);
            resultado.put("errores", errores);
            resultado.put("mensaje", String.format("%d movimientos importados", importados));

            log.info("Importación completada: {} movimientos", importados);

        } catch (Exception e) {
            log.error("Error importando movimientos", e);
            resultado.put("exito", false);
            resultado.put("mensaje", "Error: " + e.getMessage());
        }

        return resultado;
    }

    public Map<String, Object> conciliarMovimientos() {
        log.info("Iniciando conciliación automática de movimientos");

        Map<String, Object> resultado = new HashMap<>();
        int conciliados = 0;
        int noConciliados = 0;

        try {
            List<MovimientoBanco> movimientos = movimientoBancoRepository.findByConciliado(false);

            for (MovimientoBanco movimiento : movimientos) {
                if (movimiento.getObservaciones() != null && !movimiento.getObservaciones().isEmpty()) {
                    movimiento.setConciliado(true);
                    movimientoBancoRepository.save(movimiento);
                    conciliados++;
                } else {
                    noConciliados++;
                }
            }

            resultado.put("exito", true);
            resultado.put("conciliados", conciliados);
            resultado.put("noConciliados", noConciliados);
            resultado.put("mensaje", String.format("%d conciliados", conciliados));

            log.info("Conciliación completada: {} conciliados", conciliados);

        } catch (Exception e) {
            log.error("Error conciliando movimientos", e);
            resultado.put("exito", false);
            resultado.put("mensaje", "Error: " + e.getMessage());
        }

        return resultado;
    }

    public String generarReporteMovimientos(LocalDate desde, LocalDate hasta) {
        try {
            List<MovimientoBanco> movimientos = movimientoBancoRepository.findByFechaBetween(desde, hasta);

            StringBuilder html = new StringBuilder();
            html.append("<!DOCTYPE html>\n<html>\n<head>\n<meta charset=\"UTF-8\">\n");
            html.append("<title>Reporte de Movimientos Bancarios</title>\n");
            html.append("<style>");
            html.append("body { font-family: Arial; margin: 20px; }");
            html.append("table { border-collapse: collapse; width: 100%; }");
            html.append("th, td { border: 1px solid #ccc; padding: 10px; }");
            html.append("</style>\n</head>\n<body>\n");

            html.append("<h1>Reporte de Movimientos Bancarios</h1>\n");
            html.append("<p>Período: ").append(desde).append(" a ").append(hasta).append("</p>\n");
            html.append("<table>\n<thead><tr><th>Fecha</th><th>Concepto</th><th>Importe</th></tr></thead>\n<tbody>\n");

            for (MovimientoBanco movimiento : movimientos) {
                html.append("<tr><td>").append(movimiento.getFecha()).append("</td><td>");
                html.append(movimiento.getConcepto()).append("</td><td>");
                html.append(movimiento.getImporte()).append("€</td></tr>\n");
            }

            html.append("</tbody>\n</table>\n</body>\n</html>\n");

            String nombreArchivo = "ReporteMovimientos_" + System.currentTimeMillis() + ".html";
            String ruta = "target/reportes/" + nombreArchivo;

            java.nio.file.Files.write(java.nio.file.Paths.get(ruta), html.toString().getBytes(StandardCharsets.UTF_8));

            log.info("Reporte generado: {}", ruta);
            return ruta;

        } catch (Exception e) {
            log.error("Error generando reporte", e);
            throw new RuntimeException("Error al generar reporte", e);
        }
    }

    public BigDecimal obtenerSaldoActual() {
        List<MovimientoBanco> movimientos = movimientoBancoRepository.findAll();
        return movimientos.stream().map(MovimientoBanco::getImporte).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public boolean validarFormatoCSV(String rutaArchivo) {
        try {
            File archivo = new File(rutaArchivo);
            if (!archivo.exists() || !archivo.getName().endsWith(".csv")) {
                return false;
            }

            BufferedReader reader = new BufferedReader(new FileReader(archivo));
            String primeraLinea = reader.readLine();
            reader.close();

            return primeraLinea.split(",").length >= 3;

        } catch (Exception e) {
            log.error("Error validando formato CSV", e);
            return false;
        }
    }
}

