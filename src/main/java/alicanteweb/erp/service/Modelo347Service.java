package alicanteweb.erp.service;

import alicanteweb.erp.entities.Cliente;
import alicanteweb.erp.entities.Factura;
import alicanteweb.erp.entities.Proveedor;
import alicanteweb.erp.repository.ClienteRepository;
import alicanteweb.erp.repository.FacturaRepository;
import alicanteweb.erp.repository.ProveedorRepository;
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
 * Declaración anual de operaciones con terceras personas
 * Ley 58/2003 General Tributaria
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class Modelo347Service {
    private static final Logger log = LoggerFactory.getLogger(Modelo347Service.class);

    // Umbral mínimo para declarar: 3.005,06€
    private static final BigDecimal UMBRAL_MODELO_347 = new BigDecimal("3005.06");

    private final FacturaRepository facturaRepository;
    private final ClienteRepository clienteRepository;
    private final ProveedorRepository proveedorRepository;

    /**
     * Genera el Modelo 347 para un ejercicio fiscal
     */
    public Modelo347Result generarModelo347(int ejercicio) {
        log.info("📊 Generando Modelo 347 para el ejercicio {}", ejercicio);

        LocalDate inicioEjercicio = LocalDate.of(ejercicio, 1, 1);
        LocalDate finEjercicio = LocalDate.of(ejercicio, 12, 31);

        // Obtener todas las facturas del ejercicio
        List<Factura> facturasEjercicio = facturaRepository.findByFechaBetween(inicioEjercicio, finEjercicio);

        log.info("📄 Procesando {} facturas del ejercicio {}", facturasEjercicio.size(), ejercicio);

        // Agrupar por cliente y calcular totales
        Map<Long, OperacionTercero> operacionesClientes = new HashMap<>();

        for (Factura factura : facturasEjercicio) {
            if (factura.getCliente() == null) continue;

            Long clienteId = factura.getCliente().getId();
            OperacionTercero operacion = operacionesClientes.getOrDefault(clienteId,
                new OperacionTercero(
                    factura.getCliente().getCif(),
                    factura.getCliente().getNombre(),
                    "CLIENTE",
                    BigDecimal.ZERO,
                    BigDecimal.ZERO,
                    BigDecimal.ZERO,
                    BigDecimal.ZERO
                ));

            // Sumar totales
            if (factura.getBaseImponible() != null) {
                operacion = new OperacionTercero(
                    operacion.nif(),
                    operacion.nombre(),
                    operacion.tipo(),
                    operacion.importeOperaciones().add(factura.getBaseImponible()),
                    operacion.importeIVA().add(factura.getTotalIva() != null ? factura.getTotalIva() : BigDecimal.ZERO),
                    operacion.importeRetencion(),
                    operacion.totalDeclarar().add(factura.getTotal())
                );
            }

            operacionesClientes.put(clienteId, operacion);
        }

        // Filtrar operaciones que superan el umbral
        List<OperacionTercero> operacionesDeclarar = new ArrayList<>();
        BigDecimal totalDeclarado = BigDecimal.ZERO;
        int totalDeclarantes = 0;

        for (OperacionTercero operacion : operacionesClientes.values()) {
            if (operacion.totalDeclarar().compareTo(UMBRAL_MODELO_347) >= 0) {
                operacionesDeclarar.add(operacion);
                totalDeclarado = totalDeclarado.add(operacion.totalDeclarar());
                totalDeclarantes++;
            }
        }

        log.info("✅ Modelo 347 generado: {} declarantes, total: {}€",
            totalDeclarantes, totalDeclarado);

        return new Modelo347Result(
            ejercicio,
            operacionesDeclarar,
            totalDeclarantes,
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

        log.info("✅ Fichero BOE generado con {} registros", modelo347.operaciones().size() + 1);

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

        // Posición 60: Clave de operación
        registro.append("B");  // B = Adquisiciones

        // Posición 61-75: Importe anual de las operaciones
        registro.append(formatearImporte(operacion.importeOperaciones()));

        // Posición 76-161: Desglose trimestral (4 trimestres x 15 posiciones + otros datos)
        // Simplificado: se pone todo en el total anual
        for (int i = 0; i < 100; i++) {
            registro.append(" ");
        }

        // Posición 162-500: Blancos
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
            errores.add("No hay operaciones a declarar");
        }

        for (OperacionTercero operacion : modelo.operaciones()) {
            if (operacion.nif() == null || operacion.nif().trim().isEmpty()) {
                errores.add("NIF vacío para: " + operacion.nombre());
            }
            if (operacion.totalDeclarar().compareTo(UMBRAL_MODELO_347) < 0) {
                errores.add("Operación por debajo del umbral: " + operacion.nombre());
            }
        }

        if (errores.isEmpty()) {
            log.info("✅ Modelo 347 validado correctamente");
        } else {
            log.warn("⚠️ Errores en validación del Modelo 347: {}", errores.size());
        }

        return errores;
    }

    // ==========================================
    // UTILIDADES
    // ==========================================

    private String formatearImporte(BigDecimal importe) {
        if (importe == null) importe = BigDecimal.ZERO;

        // Formato: 13 posiciones + 2 decimales sin coma
        long importeCentimos = importe.multiply(new BigDecimal("100")).longValue();
        String importeStr = String.format("%015d", Math.abs(importeCentimos));

        // Signo: + o -
        char signo = importe.compareTo(BigDecimal.ZERO) >= 0 ? '+' : '-';

        return signo + importeStr.substring(importeStr.length() - 14);
    }

    private String truncar(String texto, int longitud) {
        if (texto == null) return "";
        return texto.length() > longitud ? texto.substring(0, longitud) : texto;
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
     * Operación con un tercero
     */
    public record OperacionTercero(
        String nif,
        String nombre,
        String tipo,  // CLIENTE, PROVEEDOR
        BigDecimal importeOperaciones,
        BigDecimal importeIVA,
        BigDecimal importeRetencion,
        BigDecimal totalDeclarar
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

