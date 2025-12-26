package alicanteweb.erp.service;

import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.regex.Pattern;

/**
 * Servicio de validaciones y cÃ¡lculos fiscales segÃºn normativa espaÃ±ola
 */
@Service
public class FiscalService {

    private static final Logger log = LoggerFactory.getLogger(FiscalService.class);

    // Tipos de IVA en EspaÃ±a
    public static final BigDecimal IVA_GENERAL = new BigDecimal("21");
    public static final BigDecimal IVA_REDUCIDO = new BigDecimal("10");
    public static final BigDecimal IVA_SUPERREDUCIDO = new BigDecimal("4");

    // Retenciones IRPF
    public static final BigDecimal IRPF_PROFESIONALES = new BigDecimal("15");
    public static final BigDecimal IRPF_ACTIVIDADES = new BigDecimal("7");

    // Patrones de validaciÃ³n
    private static final Pattern CIF_PATTERN = Pattern.compile("^[A-Z][0-9]{8}$");
    private static final Pattern NIF_PATTERN = Pattern.compile("^[0-9]{8}[A-Z]$");
    private static final Pattern NIE_PATTERN = Pattern.compile("^[XYZ][0-9]{7}[A-Z]$");

    /**
     * Valida un CIF espaÃ±ol
     */
    public boolean validarCIF(String cif) {
        if (cif == null || cif.isEmpty()) {
            return false;
        }

        cif = cif.trim().toUpperCase();

        if (!CIF_PATTERN.matcher(cif).matches()) {
            return false;
        }

        // ValidaciÃ³n del dÃ­gito de control
        String tipoOrganizacion = cif.substring(0, 1);
        String numero = cif.substring(1, 8);
        String digitoControl = cif.substring(8);

        // Calcular dÃ­gito de control
        int suma = 0;
        for (int i = 0; i < numero.length(); i++) {
            int digito = Character.getNumericValue(numero.charAt(i));

            if (i % 2 == 0) {
                // Posiciones impares: multiplicar por 2
                int doble = digito * 2;
                suma += (doble / 10) + (doble % 10);
            } else {
                // Posiciones pares: sumar directamente
                suma += digito;
            }
        }

        int unidad = suma % 10;
        int digitoCalculado = (unidad == 0) ? 0 : (10 - unidad);

        // Para ciertos tipos de organizaciÃ³n, el dÃ­gito es una letra
        if ("NPQRSW".contains(tipoOrganizacion)) {
            String letras = "JABCDEFGHI";
            return digitoControl.equals(String.valueOf(letras.charAt(digitoCalculado)));
        } else {
            return digitoControl.equals(String.valueOf(digitoCalculado));
        }
    }

    /**
     * Valida un NIF espaÃ±ol
     */
    public boolean validarNIF(String nif) {
        if (nif == null || nif.isEmpty()) {
            return false;
        }

        nif = nif.trim().toUpperCase();

        if (!NIF_PATTERN.matcher(nif).matches()) {
            return false;
        }

        String letras = "TRWAGMYFPDXBNJZSQVHLCKE";
        int numero = Integer.parseInt(nif.substring(0, 8));
        char letraCalculada = letras.charAt(numero % 23);
        char letraDocumento = nif.charAt(8);

        return letraCalculada == letraDocumento;
    }

    /**
     * Valida un NIE espaÃ±ol
     */
    public boolean validarNIE(String nie) {
        if (nie == null || nie.isEmpty()) {
            return false;
        }

        nie = nie.trim().toUpperCase();

        if (!NIE_PATTERN.matcher(nie).matches()) {
            return false;
        }

        // Convertir primera letra a nÃºmero
        char primeraLetra = nie.charAt(0);
        String numero = nie.substring(1, 8);

        switch (primeraLetra) {
            case 'X': numero = "0" + numero; break;
            case 'Y': numero = "1" + numero; break;
            case 'Z': numero = "2" + numero; break;
        }

        // Validar como NIF
        return validarNIF(numero + nie.charAt(8));
    }

    /**
     * Valida cualquier identificador fiscal espaÃ±ol (NIF, NIE o CIF)
     */
    public boolean validarIdentificadorFiscal(String identificador) {
        if (identificador == null || identificador.isEmpty()) {
            return false;
        }

        identificador = identificador.trim().toUpperCase();

        // Intentar validar como cada tipo
        return validarNIF(identificador) ||
               validarNIE(identificador) ||
               validarCIF(identificador);
    }

    /**
     * Calcula el IVA de una cantidad
     */
    public BigDecimal calcularIVA(BigDecimal base, BigDecimal tipoIVA) {
        if (base == null || tipoIVA == null) {
            return BigDecimal.ZERO;
        }

        return base.multiply(tipoIVA)
                   .divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
    }

    /**
     * Calcula el total con IVA incluido
     */
    public BigDecimal calcularTotalConIVA(BigDecimal base, BigDecimal tipoIVA) {
        BigDecimal iva = calcularIVA(base, tipoIVA);
        return base.add(iva).setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * Calcula la retenciÃ³n IRPF de una cantidad
     */
    public BigDecimal calcularIRPF(BigDecimal base, BigDecimal tipoIRPF) {
        if (base == null || tipoIRPF == null) {
            return BigDecimal.ZERO;
        }

        return base.multiply(tipoIRPF)
                   .divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
    }

    /**
     * Determina si una factura es intracomunitaria (IVA 0%)
     */
    public boolean esFacturaIntracomunitaria(String paisCliente) {
        if (paisCliente == null) {
            return false;
        }

        // PaÃ­ses de la UE (excepto EspaÃ±a)
        String[] paisesUE = {
            "ALEMANIA", "AUSTRIA", "BELGICA", "BULGARIA", "CHIPRE", "CROACIA",
            "DINAMARCA", "ESLOVAQUIA", "ESLOVENIA", "ESTONIA", "FINLANDIA",
            "FRANCIA", "GRECIA", "HUNGRIA", "IRLANDA", "ITALIA", "LETONIA",
            "LITUANIA", "LUXEMBURGO", "MALTA", "PAISES BAJOS", "POLONIA",
            "PORTUGAL", "REPUBLICA CHECA", "RUMANIA", "SUECIA"
        };

        String paisUpper = paisCliente.toUpperCase();
        for (String pais : paisesUE) {
            if (paisUpper.contains(pais)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Valida que una fecha de factura sea vÃ¡lida segÃºn normativa espaÃ±ola
     */
    public boolean validarFechaFactura(LocalDate fechaFactura) {
        if (fechaFactura == null) {
            return false;
        }

        LocalDate hoy = LocalDate.now();

        // No puede ser futura
        if (fechaFactura.isAfter(hoy)) {
            log.warn("Fecha de factura futura: {}", fechaFactura);
            return false;
        }

        // No puede ser anterior a 4 aÃ±os (prescripciÃ³n)
        LocalDate hace4Anios = hoy.minusYears(4);
        if (fechaFactura.isBefore(hace4Anios)) {
            log.warn("Fecha de factura demasiado antigua: {}", fechaFactura);
            return false;
        }

        return true;
    }

    /**
     * Genera nÃºmero de factura segÃºn formato espaÃ±ol
     */
    public String generarNumeroFactura(String serie, int numero, int anio) {
        // Formato: SERIE/NUMERO/AÃ‘O
        return String.format("%s/%06d/%04d", serie, numero, anio);
    }

    /**
     * Valida nÃºmero de factura espaÃ±ol
     */
    public boolean validarNumeroFactura(String numeroFactura) {
        if (numeroFactura == null || numeroFactura.isEmpty()) {
            return false;
        }

        // Formato comÃºn: SERIE/NUMERO o SERIE-NUMERO o solo NUMERO
        return numeroFactura.matches("^[A-Z0-9]{1,10}(/|-)?[0-9]{1,10}(/[0-9]{4})?$");
    }

    /**
     * Obtiene el tipo de IVA recomendado para un producto de panaderÃ­a
     */
    public BigDecimal getIVAPanaderia(String tipoProducto) {
        if (tipoProducto == null) {
            return IVA_GENERAL;
        }

        String tipo = tipoProducto.toUpperCase();

        // Pan comÃºn: IVA superreducido 4%
        if (tipo.contains("PAN") && (tipo.contains("COMUN") || tipo.contains("BARRA"))) {
            return IVA_SUPERREDUCIDO;
        }

        // Pan especial, bollerÃ­a: IVA reducido 10%
        if (tipo.contains("BOLLERIA") || tipo.contains("CROISSANT") ||
            tipo.contains("NAPOLITANA") || tipo.contains("ESPECIAL")) {
            return IVA_REDUCIDO;
        }

        // Otros productos: IVA general 21%
        return IVA_GENERAL;
    }

    /**
     * Calcula el recargo de equivalencia (para minoristas en rÃ©gimen especial)
     */
    public BigDecimal calcularRecargoEquivalencia(BigDecimal tipoIVA) {
        if (tipoIVA == null) {
            return BigDecimal.ZERO;
        }

        // Tabla de recargos segÃºn tipo de IVA
        if (tipoIVA.compareTo(IVA_GENERAL) == 0) {
            return new BigDecimal("5.2"); // 5.2% para IVA 21%
        } else if (tipoIVA.compareTo(IVA_REDUCIDO) == 0) {
            return new BigDecimal("1.4"); // 1.4% para IVA 10%
        } else if (tipoIVA.compareTo(IVA_SUPERREDUCIDO) == 0) {
            return new BigDecimal("0.5"); // 0.5% para IVA 4%
        }

        return BigDecimal.ZERO;
    }

    /**
     * Verifica si un proveedor estÃ¡ sujeto a retenciÃ³n IRPF
     */
    public boolean sujetoARetencionIRPF(String tipoProveedor) {
        if (tipoProveedor == null) {
            return false;
        }

        String tipo = tipoProveedor.toUpperCase();

        // Profesionales y artistas sujetos a retenciÃ³n
        return tipo.contains("PROFESIONAL") ||
               tipo.contains("AUTONOMO") ||
               tipo.contains("ASESOR") ||
               tipo.contains("CONSULTOR");
    }
}


