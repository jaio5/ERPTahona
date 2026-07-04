package alicanteweb.erp.service;

import alicanteweb.erp.entities.Factura;
import alicanteweb.erp.entities.FacturaCompra;
import alicanteweb.erp.repository.FacturaCompraRepository;
import alicanteweb.erp.util.FinancialMath;
import alicanteweb.erp.repository.FacturaRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Servicio para generar el Modelo 347
 * Declaración anual de operaciones con terceras personas (> 3.005,06 €)
 * Ley 58/2003 General Tributaria y RD 1065/2007.
 *
 * Incluye:
 *  - Ventas a clientes (clave B) a partir de facturas EMITIDAS (excluye borradores, en revisión y anuladas).
 *  - Compras a proveedores (clave A) a partir de facturas de compra no anuladas.
 *  - Desglose trimestral de operaciones, obligatorio desde el ejercicio 2011 (RD 1615/2011).
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class Modelo347Service {
    private static final Logger log = LoggerFactory.getLogger(Modelo347Service.class);

    // Umbral mínimo para declarar: 3.005,06€
    private static final BigDecimal UMBRAL_MODELO_347 = new BigDecimal("3005.06");

    /** Estado de las facturas de venta que se declaran (efectivamente expedidas). */
    private static final String ESTADO_FACTURA_DECLARABLE = "EMITIDA";

    private final FacturaRepository facturaRepository;
    private final FacturaCompraRepository facturaCompraRepository;

    /**
     * Genera el Modelo 347 para un ejercicio fiscal
     */
    public Modelo347Result generarModelo347(int ejercicio) {
        log.info("[347] Generando Modelo 347 para el ejercicio {}", ejercicio);

        LocalDate inicioEjercicio = LocalDate.of(ejercicio, 1, 1);
        LocalDate finEjercicio = LocalDate.of(ejercicio, 12, 31);

        Map<String, Acumulador> acumuladores = new HashMap<>();

        // ---- Ventas (clave B): solo facturas emitidas ----
        List<Factura> facturasVenta = facturaRepository.findByFechaBetweenAndEstado(
                inicioEjercicio, finEjercicio, ESTADO_FACTURA_DECLARABLE);
        log.info("[347] Procesando {} facturas de venta emitidas del ejercicio {}", facturasVenta.size(), ejercicio);

        for (Factura factura : facturasVenta) {
            if (factura.getCliente() == null || factura.getTotal() == null) continue;
            Acumulador acc = acumuladores.computeIfAbsent("B|" + factura.getCliente().getId(),
                    k -> new Acumulador(factura.getCliente().getCif(), factura.getCliente().getNombre(), "CLIENTE", "B"));
            acc.sumar(factura.getBaseImponible(), factura.getTotalIva(), factura.getTotal(), factura.getFecha());
        }

        // ---- Compras (clave A): facturas de compra no anuladas ----
        List<FacturaCompra> facturasCompra = facturaCompraRepository.findByFechaBetween(inicioEjercicio, finEjercicio);
        log.info("[347] Procesando {} facturas de compra del ejercicio {}", facturasCompra.size(), ejercicio);

        for (FacturaCompra fc : facturasCompra) {
            if (fc.getProveedor() == null || fc.getTotal() == null) continue;
            if ("ANULADA".equalsIgnoreCase(fc.getEstado())) continue;
            Acumulador acc = acumuladores.computeIfAbsent("A|" + fc.getProveedor().getId(),
                    k -> new Acumulador(fc.getProveedor().getCif(), fc.getProveedor().getNombre(), "PROVEEDOR", "A"));
            acc.sumar(fc.getBaseImponible(), fc.getImporteIva(), fc.getTotal(), fc.getFecha());
        }

        // ---- Filtrar por umbral ----
        List<OperacionTercero> operacionesDeclarar = new ArrayList<>();
        BigDecimal totalDeclarado = BigDecimal.ZERO;

        for (Acumulador acc : acumuladores.values()) {
            if (acc.totalDeclarar.compareTo(UMBRAL_MODELO_347) >= 0) {
                operacionesDeclarar.add(acc.toOperacion());
                totalDeclarado = totalDeclarado.add(acc.totalDeclarar);
            }
        }
        operacionesDeclarar.sort((a, b) -> {
            int porClave = a.claveOperacion().compareTo(b.claveOperacion());
            return porClave != 0 ? porClave : a.nombre().compareToIgnoreCase(b.nombre());
        });

        log.info("[OK] Modelo 347 generado: {} declarados, total: {}€",
            operacionesDeclarar.size(), totalDeclarado);

        return new Modelo347Result(
            ejercicio,
            operacionesDeclarar,
            operacionesDeclarar.size(),
            totalDeclarado,
            LocalDate.now()
        );
    }

    /**
     * Genera el fichero para presentación telemática (formato BOE)
     */
    public String generarFicheroBOE(Modelo347Result modelo347, DatosDeclarante declarante) {
        log.info("📝 Generando fichero BOE para Modelo 347");

        StringBuilder fichero = new StringBuilder();

        // Tipo de registro 1: Declarante
        fichero.append(generarRegistroTipo1(declarante, modelo347));

        // Tipo de registro 2: Declarados
        for (OperacionTercero operacion : modelo347.operaciones()) {
            fichero.append(generarRegistroTipo2(operacion, declarante.ejercicio()));
        }

        log.info("[OK] Fichero BOE generado con {} registros", modelo347.operaciones().size() + 1);

        return fichero.toString();
    }

    /**
     * Genera registro tipo 1 (Declarante)
     */
    private String generarRegistroTipo1(DatosDeclarante declarante, Modelo347Result modelo) {
        StringBuilder registro = new StringBuilder();

        // Posición 1: Tipo de registro
        registro.append("1");

        // Posición 2-5: Modelo (347)
        registro.append("347 ");

        // Posición 6-9: Ejercicio
        registro.append(String.format("%4d", declarante.ejercicio()));

        // Posición 10-18: NIF del declarante
        registro.append(String.format("%-9s", declarante.nif()));

        // Posición 19-58: Nombre/Razón social
        registro.append(String.format("%-40s", truncar(declarante.nombre(), 40)));

        // Posición 59-60: Tipo de soporte
        registro.append("T ");  // Telemática

        // Posición 61-69: Teléfono
        registro.append(String.format("%-9s", declarante.telefono() != null ? declarante.telefono() : ""));

        // Posición 70-109: Persona de contacto
        registro.append(String.format("%-40s", declarante.contacto() != null ? truncar(declarante.contacto(), 40) : ""));

        // Posición 110-122: Número total de registros
        registro.append(String.format("%013d", modelo.totalDeclarantes() + 1));

        // Posición 123-137: Importe total de las operaciones
        registro.append(formatearImporte(modelo.totalDeclarado()));

        // Posición 138-146: Número de identificación electrónica
        registro.append("         ");

        // Posición 147-160: Fecha de presentación
        registro.append(LocalDate.now().format(DateTimeFormatter.ofPattern("ddMMyyyy")));
        registro.append("      ");

        // Posición 161-500: Blancos
        while (registro.length() < 500) {
            registro.append(" ");
        }

        registro.append("\r\n");
        return registro.toString();
    }

    /**
     * Genera registro tipo 2 (Declarado)
     */
    private String generarRegistroTipo2(OperacionTercero operacion, int ejercicio) {
        StringBuilder registro = new StringBuilder();

        // Posición 1: Tipo de registro
        registro.append("2");

        // Posición 2-5: Modelo
        registro.append("347 ");

        // Posición 6-9: Ejercicio
        registro.append(String.format("%4d", ejercicio));

        // Posición 10-18: NIF del declarado
        registro.append(String.format("%-9s", operacion.nif()));

        // Posición 19-58: Nombre/Razón social del declarado
        registro.append(String.format("%-40s", truncar(operacion.nombre(), 40)));

        // Posición 59: Tipo de hoja (sin valor)
        registro.append(" ");

        // Posición 60: Clave de operación (A = adquisiciones/compras, B = entregas/ventas)
        registro.append(operacion.claveOperacion());

        // Posición 61-75: Importe anual de las operaciones
        registro.append(formatearImporte(operacion.importeOperaciones()));

        // Posición 76-135: Desglose trimestral del importe de las operaciones (4 x 15)
        registro.append(formatearImporte(operacion.trimestre1()));
        registro.append(formatearImporte(operacion.trimestre2()));
        registro.append(formatearImporte(operacion.trimestre3()));
        registro.append(formatearImporte(operacion.trimestre4()));

        // Posición 136-500: Blancos
        while (registro.length() < 500) {
            registro.append(" ");
        }

        registro.append("\r\n");
        return registro.toString();
    }

    /**
     * Valida los datos del Modelo 347
     */
    public List<String> validarModelo347(Modelo347Result modelo) {
        List<String> errores = new ArrayList<>();

        if (modelo == null) {
            errores.add("El modelo 347 no puede ser nulo");
            return errores;
        }

        if (modelo.ejercicio() < 2000 || modelo.ejercicio() > 2100) {
            errores.add("Ejercicio inválido: " + modelo.ejercicio());
        }

        if (modelo.operaciones() == null || modelo.operaciones().isEmpty()) {
            log.info("Modelo 347 sin operaciones a declarar para el ejercicio {}", modelo.ejercicio());
            return errores;
        }

        for (OperacionTercero operacion : modelo.operaciones()) {
            if (operacion.nif() == null || operacion.nif().isBlank()) {
                errores.add("NIF vacío para: " + operacion.nombre());
            }
            if (operacion.totalDeclarar().compareTo(UMBRAL_MODELO_347) < 0) {
                errores.add("Operación por debajo del umbral: " + operacion.nombre());
            }
            BigDecimal sumaTrimestres = operacion.trimestre1().add(operacion.trimestre2())
                    .add(operacion.trimestre3()).add(operacion.trimestre4());
            if (sumaTrimestres.compareTo(operacion.totalDeclarar()) != 0) {
                errores.add("El desglose trimestral no cuadra con el total anual para: " + operacion.nombre());
            }
        }

        if (errores.isEmpty()) {
            log.info("[OK] Modelo 347 validado correctamente");
        } else {
            log.warn("[AVISO] Errores en validación del Modelo 347: {}", errores.size());
        }

        return errores;
    }

    /**
     * Obtiene los registros del Modelo 347 para un ejercicio
     */
    public List<alicanteweb.erp.entities.Modelo347Registro> obtenerRegistrosEjercicio(int ejercicio) {
        log.info("Obteniendo registros Modelo 347 para ejercicio {}", ejercicio);

        // Generar el modelo para obtener los datos
        Modelo347Result resultado = generarModelo347(ejercicio);

        // Convertir a entidades Modelo347Registro
        List<alicanteweb.erp.entities.Modelo347Registro> registros = new ArrayList<>();

        for (OperacionTercero operacion : resultado.operaciones()) {
            alicanteweb.erp.entities.Modelo347Registro registro = new alicanteweb.erp.entities.Modelo347Registro();
            registro.setEjercicio(ejercicio);
            registro.setNifDeclarado(operacion.nif());
            registro.setNombreDeclarado(operacion.nombre());
            registro.setTipoOperacion(operacion.claveOperacion());
            registro.setEsCliente("CLIENTE".equals(operacion.tipo()));
            registro.setEsProveedor("PROVEEDOR".equals(operacion.tipo()));
            registro.setImporteTotal(operacion.totalDeclarar());
            registros.add(registro);
        }

        log.info("Encontrados {} registros para ejercicio {}", registros.size(), ejercicio);
        return registros;
    }

    // ==========================================
    // UTILIDADES
    // ==========================================

    private String formatearImporte(BigDecimal importe) {
        if (importe == null) importe = BigDecimal.ZERO;

        // Formato: 13 posiciones + 2 decimales sin coma
        long importeCentimos = importe.multiply(FinancialMath.CIEN).longValue();
        String importeStr = String.format("%015d", Math.abs(importeCentimos));

        // Signo: + o -
        char signo = importe.compareTo(BigDecimal.ZERO) >= 0 ? '+' : '-';

        return signo + importeStr.substring(importeStr.length() - 14);
    }

    private String truncar(String texto, int longitud) {
        if (texto == null) return "";
        return texto.length() > longitud ? texto.substring(0, longitud) : texto;
    }

    /**
     * Acumulador mutable por tercero: totales anuales y desglose trimestral.
     */
    private static final class Acumulador {
        private final String nif;
        private final String nombre;
        private final String tipo;
        private final String clave;
        private BigDecimal importeOperaciones = BigDecimal.ZERO;
        private BigDecimal importeIVA = BigDecimal.ZERO;
        private BigDecimal totalDeclarar = BigDecimal.ZERO;
        private final BigDecimal[] trimestres = {BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO};

        private Acumulador(String nif, String nombre, String tipo, String clave) {
            this.nif = nif;
            this.nombre = nombre;
            this.tipo = tipo;
            this.clave = clave;
        }

        private void sumar(BigDecimal base, BigDecimal iva, BigDecimal total, LocalDate fecha) {
            importeOperaciones = importeOperaciones.add(base != null ? base : BigDecimal.ZERO);
            importeIVA = importeIVA.add(iva != null ? iva : BigDecimal.ZERO);
            totalDeclarar = totalDeclarar.add(total);
            if (fecha != null) {
                int trimestre = (fecha.getMonthValue() - 1) / 3;
                trimestres[trimestre] = trimestres[trimestre].add(total);
            }
        }

        private OperacionTercero toOperacion() {
            return new OperacionTercero(nif, nombre, tipo, clave,
                    importeOperaciones, importeIVA, BigDecimal.ZERO, totalDeclarar,
                    trimestres[0], trimestres[1], trimestres[2], trimestres[3]);
        }
    }

    // ==========================================
    // RECORDS Y CLASES AUXILIARES
    // ==========================================

    /**
     * Resultado del cálculo del Modelo 347
     */
    public record Modelo347Result(
        int ejercicio,
        List<OperacionTercero> operaciones,
        int totalDeclarantes,
        BigDecimal totalDeclarado,
        LocalDate fechaGeneracion
    ) {}

    /**
     * Operación con un tercero, con desglose trimestral (importe total, IVA incluido)
     */
    public record OperacionTercero(
        String nif,
        String nombre,
        String tipo,  // CLIENTE, PROVEEDOR
        String claveOperacion, // A = adquisiciones (compras), B = entregas (ventas)
        BigDecimal importeOperaciones,
        BigDecimal importeIVA,
        BigDecimal importeRetencion,
        BigDecimal totalDeclarar,
        BigDecimal trimestre1,
        BigDecimal trimestre2,
        BigDecimal trimestre3,
        BigDecimal trimestre4
    ) {}

    /**
     * Datos del declarante
     */
    public record DatosDeclarante(
        String nif,
        String nombre,
        String telefono,
        String contacto,
        int ejercicio
    ) {}
}
