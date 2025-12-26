package alicanteweb.erp.service;

import alicanteweb.erp.entities.Factura;
import alicanteweb.erp.entities.FacturaLinea;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Servicio de validaciÃ³n de facturas segÃºn normativa espaÃ±ola
 * RD 1619/2012 - Reglamento de facturaciÃ³n
 */
@Service
@Slf4j
public class FacturaValidacionService {

    private static final Pattern CIF_PATTERN = Pattern.compile("^[A-HJ-NP-SUVW][0-9]{7}[0-9A-J]$");
    private static final Pattern NIF_PATTERN = Pattern.compile("^[0-9]{8}[A-Z]$");
    private static final Pattern NIE_PATTERN = Pattern.compile("^[XYZ][0-9]{7}[A-Z]$");

    private final FacturaLineaService facturaLineaService;

    public FacturaValidacionService(FacturaLineaService facturaLineaService) {
        this.facturaLineaService = facturaLineaService;
    }

    /**
     * Valida una factura antes de emisiÃ³n
     * @return Lista de errores (vacÃ­a si vÃ¡lida)
     */
    public List<String> validarParaEmision(Factura factura) {
        log.info("Validando factura {} para emisiÃ³n", factura.getNumero());

        List<String> errores = new ArrayList<>();

        // 1. Validaciones bÃ¡sicas obligatorias (RD 1619/2012 Art. 6)
        validarCamposObligatorios(factura, errores);

        // 2. Validar cliente
        validarCliente(factura, errores);

        // 3. Validar lÃ­neas de factura
        validarLineas(factura, errores);

        // 4. Validar importes
        validarImportes(factura, errores);

        // 5. Validar numeraciÃ³n
        validarNumeracion(factura, errores);

        // 6. Validar segÃºn tipo de factura
        validarSegunTipo(factura, errores);

        // 7. Validar VeriFactu si estÃ¡ habilitado
        validarVeriFactu(factura, errores);

        if (errores.isEmpty()) {
            log.info("Factura {} vÃ¡lida para emisiÃ³n", factura.getNumero());
        } else {
            log.warn("Factura {} tiene {} errores de validaciÃ³n", factura.getNumero(), errores.size());
        }

        return errores;
    }

    /**
     * Valida campos obligatorios segÃºn RD 1619/2012
     */
    private void validarCamposObligatorios(Factura factura, List<String> errores) {
        // NÃºmero de factura (Art. 6.1.a)
        if (factura.getNumero() == null || factura.getNumero().trim().isEmpty()) {
            errores.add("El nÃºmero de factura es obligatorio");
        }

        // Fecha de expediciÃ³n (Art. 6.1.b)
        if (factura.getFecha() == null) {
            errores.add("La fecha de expediciÃ³n es obligatoria");
        }

        // Serie
        if (factura.getSerie() == null || factura.getSerie().trim().isEmpty()) {
            errores.add("La serie de factura es obligatoria");
        }

        // Tipo de factura
        if (factura.getTipoFactura() == null || factura.getTipoFactura().trim().isEmpty()) {
            errores.add("El tipo de factura es obligatorio");
        }
    }

    /**
     * Valida los datos del cliente
     */
    private void validarCliente(Factura factura, List<String> errores) {
        if (factura.getCliente() == null) {
            errores.add("La factura debe tener un cliente asignado");
            return;
        }

        // Validar CIF/NIF del cliente
        String cif = factura.getCliente().getCif();
        if (cif == null || cif.trim().isEmpty()) {
            errores.add("El cliente debe tener CIF/NIF");
        } else if (!validarCifNif(cif)) {
            errores.add("El CIF/NIF del cliente no es vÃ¡lido: " + cif);
        }

        // Nombre del cliente
        if (factura.getCliente().getNombre() == null ||
            factura.getCliente().getNombre().trim().isEmpty()) {
            errores.add("El cliente debe tener nombre/razÃ³n social");
        }
    }

    /**
     * Valida las lÃ­neas de factura
     */
    private void validarLineas(Factura factura, List<String> errores) {
        if (factura.getId() == null) {
            errores.add("La factura debe guardarse antes de validar lÃ­neas");
            return;
        }

        List<FacturaLinea> lineas = facturaLineaService.findByFacturaId(factura.getId());

        if (lineas == null || lineas.isEmpty()) {
            errores.add("La factura debe tener al menos una lÃ­nea");
            return;
        }

        // Validar cada lÃ­nea
        for (int i = 0; i < lineas.size(); i++) {
            FacturaLinea linea = lineas.get(i);
            String prefijo = "LÃ­nea " + (i + 1) + ": ";

            // ArtÃ­culo obligatorio
            if (linea.getArticulo() == null) {
                errores.add(prefijo + "Debe tener un artÃ­culo asignado");
            }

            // Cantidad > 0
            if (linea.getCantidad() == null || linea.getCantidad().compareTo(BigDecimal.ZERO) <= 0) {
                errores.add(prefijo + "La cantidad debe ser mayor que cero");
            }

            // Precio >= 0
            if (linea.getPrecio() == null || linea.getPrecio().compareTo(BigDecimal.ZERO) < 0) {
                errores.add(prefijo + "El precio no puede ser negativo");
            }

            // IVA vÃ¡lido (0, 4, 10, 21 son los tipos habituales)
            if (linea.getIva() != null) {
                BigDecimal iva = linea.getIva();
                if (iva.compareTo(BigDecimal.ZERO) < 0 || iva.compareTo(new BigDecimal("100")) > 0) {
                    errores.add(prefijo + "El IVA debe estar entre 0 y 100");
                }
            }
        }
    }

    /**
     * Valida los importes de la factura
     */
    private void validarImportes(Factura factura, List<String> errores) {
        // Total debe ser >= 0
        if (factura.getTotal() == null || factura.getTotal().compareTo(BigDecimal.ZERO) < 0) {
            errores.add("El total de la factura no puede ser negativo");
        }

        // Base imponible >= 0
        if (factura.getBaseImponible() != null &&
            factura.getBaseImponible().compareTo(BigDecimal.ZERO) < 0) {
            errores.add("La base imponible no puede ser negativa");
        }

        // Total IVA >= 0
        if (factura.getTotalIva() != null &&
            factura.getTotalIva().compareTo(BigDecimal.ZERO) < 0) {
            errores.add("El total de IVA no puede ser negativo");
        }

        // Verificar que base + IVA - retenciÃ³n = total (aproximadamente)
        if (factura.getBaseImponible() != null && factura.getTotalIva() != null) {
            BigDecimal calculado = factura.getBaseImponible()
                    .add(factura.getTotalIva())
                    .add(factura.getTotalRecargo() != null ? factura.getTotalRecargo() : BigDecimal.ZERO)
                    .subtract(factura.getRetencionIrpf() != null ? factura.getRetencionIrpf() : BigDecimal.ZERO);

            if (factura.getTotal() != null) {
                BigDecimal diferencia = calculado.subtract(factura.getTotal()).abs();
                if (diferencia.compareTo(new BigDecimal("0.10")) > 0) {
                    errores.add("Los importes no cuadran. Diferencia: " + diferencia);
                }
            }
        }

        // Pagado <= Total
        if (factura.getPagado() != null && factura.getTotal() != null &&
            factura.getPagado().compareTo(factura.getTotal()) > 0) {
            errores.add("El importe pagado no puede ser mayor que el total");
        }
    }

    /**
     * Valida la numeraciÃ³n de la factura
     */
    private void validarNumeracion(Factura factura, List<String> errores) {
        String numero = factura.getNumero();

        // Debe tener formato vÃ¡lido
        if (numero != null && !numero.matches("^[A-Z0-9\\-/]+$")) {
            errores.add("El nÃºmero de factura contiene caracteres no vÃ¡lidos");
        }

        // TODO: Verificar numeraciÃ³n correlativa por serie
        // Esto requerirÃ­a consultar la Ãºltima factura de la serie
    }

    /**
     * Valida segÃºn el tipo de factura
     */
    private void validarSegunTipo(Factura factura, List<String> errores) {
        String tipo = factura.getTipoFactura();

        if ("RECTIFICATIVA".equals(tipo)) {
            // Factura rectificativa debe referenciar la original
            if (factura.getFacturaRectificadaNumero() == null ||
                factura.getFacturaRectificadaNumero().trim().isEmpty()) {
                errores.add("Factura rectificativa: Debe indicar el nÃºmero de factura original");
            }

            if (factura.getFacturaRectificadaFecha() == null) {
                errores.add("Factura rectificativa: Debe indicar la fecha de factura original");
            }

            if (factura.getMotivoRectificacion() == null ||
                factura.getMotivoRectificacion().trim().isEmpty()) {
                errores.add("Factura rectificativa: Debe indicar el motivo de rectificaciÃ³n");
            }
        }

        if ("SIMPLIFICADA".equals(tipo)) {
            // Las facturas simplificadas tienen requisitos menores
            // Pero el importe no puede superar 3.000â‚¬ (IVA incluido)
            if (factura.getTotal() != null &&
                factura.getTotal().compareTo(new BigDecimal("3000")) > 0) {
                errores.add("Factura simplificada: El importe no puede superar 3.000â‚¬");
            }
        }
    }

    /**
     * Valida requisitos de VeriFactu
     */
    private void validarVeriFactu(Factura factura, List<String> errores) {
        // Si ya tiene hash, ya fue enviada a VeriFactu
        if (factura.getVerifactuHash() != null && !factura.getVerifactuHash().isEmpty()) {
            if (factura.getVerifactuQr() == null || factura.getVerifactuQr().isEmpty()) {
                errores.add("VeriFactu: Falta el cÃ³digo QR");
            }
        }
    }

    /**
     * Valida un CIF/NIF/NIE espaÃ±ol
     */
    public boolean validarCifNif(String documento) {
        if (documento == null || documento.trim().isEmpty()) {
            return false;
        }

        documento = documento.trim().toUpperCase();

        // Intentar validar como CIF
        if (CIF_PATTERN.matcher(documento).matches()) {
            return validarCIF(documento);
        }

        // Intentar validar como NIF
        if (NIF_PATTERN.matcher(documento).matches()) {
            return validarNIF(documento);
        }

        // Intentar validar como NIE
        if (NIE_PATTERN.matcher(documento).matches()) {
            return validarNIE(documento);
        }

        return false;
    }

    /**
     * Valida un CIF espaÃ±ol
     */
    private boolean validarCIF(String cif) {
        // Algoritmo de validaciÃ³n del CIF
        String letra = cif.substring(0, 1);
        String numeros = cif.substring(1, 8);
        String control = cif.substring(8);

        int suma = 0;
        for (int i = 0; i < 7; i++) {
            int digito = Character.getNumericValue(numeros.charAt(i));
            if (i % 2 == 0) {
                digito *= 2;
                if (digito > 9) {
                    digito = digito / 10 + digito % 10;
                }
            }
            suma += digito;
        }

        int unidad = suma % 10;
        int digitoControl = unidad > 0 ? 10 - unidad : 0;

        // Algunos tipos de CIF usan letra
        char letraControl = (char) ('A' + digitoControl - 1);
        if (digitoControl == 10) letraControl = 'J';

        return control.equals(String.valueOf(digitoControl)) ||
               control.equals(String.valueOf(letraControl));
    }

    /**
     * Valida un NIF espaÃ±ol
     */
    private boolean validarNIF(String nif) {
        String numeros = nif.substring(0, 8);
        char letra = nif.charAt(8);

        String letrasNIF = "TRWAGMYFPDXBNJZSQVHLCKE";
        int numero = Integer.parseInt(numeros);
        char letraCalculada = letrasNIF.charAt(numero % 23);

        return letra == letraCalculada;
    }

    /**
     * Valida un NIE espaÃ±ol
     */
    private boolean validarNIE(String nie) {
        // Convertir NIE a NIF
        String nif = nie.replace('X', '0').replace('Y', '1').replace('Z', '2');
        return validarNIF(nif);
    }

    /**
     * Verifica si una factura puede ser emitida
     */
    public boolean puedeEmitirse(Factura factura) {
        return validarParaEmision(factura).isEmpty();
    }

    /**
     * Obtiene un informe detallado de validaciÃ³n
     */
    public String obtenerInformeValidacion(Factura factura) {
        List<String> errores = validarParaEmision(factura);

        if (errores.isEmpty()) {
            return "âœ… La factura es vÃ¡lida y puede ser emitida";
        }

        StringBuilder informe = new StringBuilder();
        informe.append("âŒ La factura NO puede ser emitida. Errores encontrados:\n\n");

        for (int i = 0; i < errores.size(); i++) {
            informe.append((i + 1)).append(". ").append(errores.get(i)).append("\n");
        }

        return informe.toString();
    }
}


