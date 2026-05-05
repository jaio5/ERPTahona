package alicanteweb.erp.service;

import alicanteweb.erp.entities.Factura;
import alicanteweb.erp.entities.FacturaLinea;
import alicanteweb.erp.repository.FacturaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Servicio de validación de facturas según normativa española.
 * RD 1619/2012 - Reglamento de facturación.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class FacturaValidacionService {

    private static final Pattern CIF_PATTERN = Pattern.compile("^[A-HJ-NP-SUVW][0-9]{7}[0-9A-J]$");
    private static final Pattern NIF_PATTERN = Pattern.compile("^[0-9]{8}[A-Z]$");
    private static final Pattern NIE_PATTERN = Pattern.compile("^[XYZ][0-9]{7}[A-Z]$");

    private final FacturaLineaService facturaLineaService;
    private final FacturaRepository facturaRepository;

    public List<String> validarParaEmision(Factura factura) {
        log.info("Validando factura {} para emisión", factura.getNumero());

        List<String> errores = new ArrayList<>();
        validarCamposObligatorios(factura, errores);
        validarCliente(factura, errores);
        validarLineas(factura, errores);
        validarImportes(factura, errores);
        validarNumeracion(factura, errores);
        validarSegunTipo(factura, errores);
        validarVeriFactu(factura, errores);

        if (errores.isEmpty()) {
            log.info("Factura {} válida para emisión", factura.getNumero());
        } else {
            log.warn("Factura {} tiene {} errores de validación", factura.getNumero(), errores.size());
        }

        return errores;
    }

    private void validarCamposObligatorios(Factura factura, List<String> errores) {
        if (factura.getNumero() == null || factura.getNumero().trim().isEmpty()) {
            errores.add("El número de factura es obligatorio");
        }

        if (factura.getFecha() == null) {
            errores.add("La fecha de expedición es obligatoria");
        }

        if (factura.getSerie() == null || factura.getSerie().trim().isEmpty()) {
            errores.add("La serie de factura es obligatoria");
        } else if (!factura.getSerie().matches("^[A-Z0-9_-]{1,20}$")) {
            errores.add("La serie contiene caracteres no válidos");
        }

        if (factura.getTipoFactura() == null || factura.getTipoFactura().trim().isEmpty()) {
            errores.add("El tipo de factura es obligatorio");
        }
    }

    private void validarCliente(Factura factura, List<String> errores) {
        if (factura.getCliente() == null) {
            errores.add("La factura debe tener un cliente asignado");
            return;
        }

        String cif = factura.getCliente().getCif();
        if (cif == null || cif.trim().isEmpty()) {
            errores.add("El cliente debe tener CIF/NIF");
        } else if (!validarCifNif(cif)) {
            errores.add("El CIF/NIF del cliente no es válido: " + cif);
        }

        if (factura.getCliente().getNombre() == null || factura.getCliente().getNombre().trim().isEmpty()) {
            errores.add("El cliente debe tener nombre/razón social");
        }
    }

    private void validarLineas(Factura factura, List<String> errores) {
        if (factura.getId() == null) {
            errores.add("La factura debe guardarse antes de validar líneas");
            return;
        }

        List<FacturaLinea> lineas = facturaLineaService.findByFacturaId(factura.getId());
        if (lineas == null || lineas.isEmpty()) {
            errores.add("La factura debe tener al menos una línea");
            return;
        }

        for (int i = 0; i < lineas.size(); i++) {
            FacturaLinea linea = lineas.get(i);
            String prefijo = "Línea " + (i + 1) + ": ";

            if (linea.getArticulo() == null) {
                errores.add(prefijo + "Debe tener un artículo asignado");
            }

            if (linea.getCantidad() == null || linea.getCantidad().compareTo(BigDecimal.ZERO) <= 0) {
                errores.add(prefijo + "La cantidad debe ser mayor que cero");
            }

            if (linea.getPrecio() == null || linea.getPrecio().compareTo(BigDecimal.ZERO) < 0) {
                errores.add(prefijo + "El precio no puede ser negativo");
            }

            if (linea.getIva() != null) {
                BigDecimal iva = linea.getIva();
                if (iva.compareTo(BigDecimal.ZERO) < 0 || iva.compareTo(new BigDecimal("100")) > 0) {
                    errores.add(prefijo + "El IVA debe estar entre 0 y 100");
                }
            }
        }
    }

    private void validarImportes(Factura factura, List<String> errores) {
        if (factura.getTotal() == null || factura.getTotal().compareTo(BigDecimal.ZERO) < 0) {
            errores.add("El total de la factura no puede ser negativo");
        }

        if (factura.getBaseImponible() != null && factura.getBaseImponible().compareTo(BigDecimal.ZERO) < 0) {
            errores.add("La base imponible no puede ser negativa");
        }

        if (factura.getTotalIva() != null && factura.getTotalIva().compareTo(BigDecimal.ZERO) < 0) {
            errores.add("El total de IVA no puede ser negativo");
        }

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

        if (factura.getPagado() != null && factura.getTotal() != null
                && factura.getPagado().compareTo(factura.getTotal()) > 0) {
            errores.add("El importe pagado no puede ser mayor que el total");
        }
    }

    private void validarNumeracion(Factura factura, List<String> errores) {
        String numero = factura.getNumero();
        if (numero == null) {
            return;
        }

        if (!numero.matches("^[A-Z0-9\\-/]+$")) {
            errores.add("El número de factura contiene caracteres no válidos");
        }

        String serie = factura.getSerie() != null ? factura.getSerie().trim().toUpperCase() : "GEN";
        facturaRepository.findBySerieAndNumero(serie, numero).ifPresent(existente -> {
            if (!existente.getId().equals(factura.getId())) {
                errores.add("Ya existe una factura con la serie/número: " + serie + "/" + numero);
            }
        });
    }

    private void validarSegunTipo(Factura factura, List<String> errores) {
        String tipo = factura.getTipoFactura();

        if ("RECTIFICATIVA".equals(tipo)) {
            if (factura.getFacturaRectificadaNumero() == null || factura.getFacturaRectificadaNumero().trim().isEmpty()) {
                errores.add("Factura rectificativa: Debe indicar el número de factura original");
            }

            if (factura.getFacturaRectificadaFecha() == null) {
                errores.add("Factura rectificativa: Debe indicar la fecha de factura original");
            }

            if (factura.getMotivoRectificacion() == null || factura.getMotivoRectificacion().trim().isEmpty()) {
                errores.add("Factura rectificativa: Debe indicar el motivo de rectificación");
            }
        }

        if ("SIMPLIFICADA".equals(tipo)) {
            if (factura.getTotal() != null && factura.getTotal().compareTo(new BigDecimal("3000")) > 0) {
                errores.add("Factura simplificada: El importe no puede superar 3.000 EUR");
            }
        }
    }

    private void validarVeriFactu(Factura factura, List<String> errores) {
        if (factura.getVerifactuHash() != null && !factura.getVerifactuHash().isEmpty()) {
            if (factura.getVerifactuQr() == null || factura.getVerifactuQr().isEmpty()) {
                errores.add("VeriFactu: Falta el código QR");
            }
        }
    }

    public boolean validarCifNif(String documento) {
        if (documento == null || documento.trim().isEmpty()) {
            return false;
        }

        documento = documento.trim().toUpperCase();

        if (CIF_PATTERN.matcher(documento).matches()) {
            return validarCIF(documento);
        }

        if (NIF_PATTERN.matcher(documento).matches()) {
            return validarNIF(documento);
        }

        if (NIE_PATTERN.matcher(documento).matches()) {
            return validarNIE(documento);
        }

        return false;
    }

    private boolean validarCIF(String cif) {
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
        char letraControl = (char) ('A' + digitoControl - 1);
        if (digitoControl == 10) {
            letraControl = 'J';
        }

        return control.equals(String.valueOf(digitoControl)) || control.equals(String.valueOf(letraControl));
    }

    private boolean validarNIF(String nif) {
        String numeros = nif.substring(0, 8);
        char letra = nif.charAt(8);
        String letrasNIF = "TRWAGMYFPDXBNJZSQVHLCKE";
        int numero = Integer.parseInt(numeros);
        char letraCalculada = letrasNIF.charAt(numero % 23);
        return letra == letraCalculada;
    }

    private boolean validarNIE(String nie) {
        String nif = nie.replace('X', '0').replace('Y', '1').replace('Z', '2');
        return validarNIF(nif);
    }

    public boolean puedeEmitirse(Factura factura) {
        return validarParaEmision(factura).isEmpty();
    }

    public String obtenerInformeValidacion(Factura factura) {
        List<String> errores = validarParaEmision(factura);

        if (errores.isEmpty()) {
            return "La factura es válida y puede ser emitida";
        }

        StringBuilder informe = new StringBuilder();
        informe.append("La factura NO puede ser emitida. Errores encontrados:\n\n");
        for (int i = 0; i < errores.size(); i++) {
            informe.append(i + 1).append(". ").append(errores.get(i)).append("\n");
        }
        return informe.toString();
    }
}
