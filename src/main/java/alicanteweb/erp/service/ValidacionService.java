package alicanteweb.erp.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.regex.Pattern;

/**
 * Servicio de validaciones avanzadas segun normativa española
 */
@Service
public class ValidacionService {
    private static final Logger log = LoggerFactory.getLogger(ValidacionService.class);

    // Patrones de validacion
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
        "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"
    );

    private static final Pattern CODIGO_POSTAL_PATTERN = Pattern.compile(
        "^(?:0[1-9]|[1-4][0-9]|5[0-2])[0-9]{3}$"
    );

    private static final Pattern TELEFONO_PATTERN = Pattern.compile(
        "^[679][0-9]{8}$"
    );

    private static final String LETRAS_NIF = "TRWAGMYFPDXBNJZSQVHLCKE";

    /**
     * Valida un NIF español (DNI/NIE)
     */
    public boolean validarNIF(String nif) {
        if (nif == null || nif.trim().isEmpty()) {
            return false;
        }

        nif = nif.trim().toUpperCase();

        // DNI: 8 digitos + letra
        if (nif.matches("^[0-9]{8}[A-Z]$")) {
            return validarDNI(nif);
        }

        // NIE: X/Y/Z + 7 digitos + letra
        if (nif.matches("^[XYZ][0-9]{7}[A-Z]$")) {
            return validarNIE(nif);
        }

        return false;
    }

    /**
     * Valida un DNI español
     */
    public boolean validarDNI(String dni) {
        if (dni == null || !dni.matches("^[0-9]{8}[A-Z]$")) {
            return false;
        }

        String numeros = dni.substring(0, 8);
        char letra = dni.charAt(8);

        try {
            int numero = Integer.parseInt(numeros);
            char letraEsperada = LETRAS_NIF.charAt(numero % 23);
            return letra == letraEsperada;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    /**
     * Valida un NIE español
     */
    public boolean validarNIE(String nie) {
        if (nie == null || !nie.matches("^[XYZ][0-9]{7}[A-Z]$")) {
            return false;
        }

        // Reemplazar primera letra por numero
        char primeraLetra = nie.charAt(0);
        int prefijo = switch (primeraLetra) {
            case 'X' -> 0;
            case 'Y' -> 1;
            case 'Z' -> 2;
            default -> -1;
        };

        if (prefijo == -1) return false;

        String numeros = prefijo + nie.substring(1, 8);
        char letra = nie.charAt(8);

        try {
            int numero = Integer.parseInt(numeros);
            char letraEsperada = LETRAS_NIF.charAt(numero % 23);
            return letra == letraEsperada;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    /**
     * Valida un CIF español (NIF empresas)
     */
    public boolean validarCIF(String cif) {
        if (cif == null || cif.trim().isEmpty()) {
            return false;
        }

        cif = cif.trim().toUpperCase();

        // Formato: Letra + 7 digitos + digito/letra control
        if (!cif.matches("^[A-Z][0-9]{7}[0-9A-Z]$")) {
            return false;
        }

        char primeraLetra = cif.charAt(0);

        // Tipos validos de CIF
        String tiposValidos = "ABCDEFGHJKLMNPQRSUVW";
        if (tiposValidos.indexOf(primeraLetra) == -1) {
            return false;
        }

        // Validar digito de control
        return validarDigitoControlCIF(cif);
    }

    /**
     * Valida el digito de control del CIF
     */
    private boolean validarDigitoControlCIF(String cif) {
        String numeros = cif.substring(1, 8);
        char control = cif.charAt(8);
        char primeraLetra = cif.charAt(0);

        int sumaImpares = 0;
        int sumaPares = 0;

        // Posiciones impares (0, 2, 4, 6) - multiplicar por 2
        for (int i = 0; i < 7; i += 2) {
            int digito = Character.getNumericValue(numeros.charAt(i)) * 2;
            sumaImpares += digito / 10 + digito % 10;
        }

        // Posiciones pares (1, 3, 5)
        for (int i = 1; i < 7; i += 2) {
            sumaPares += Character.getNumericValue(numeros.charAt(i));
        }

        int sumaTotal = sumaPares + sumaImpares;
        int unidad = sumaTotal % 10;
        int digitoControl = unidad == 0 ? 0 : 10 - unidad;

        // Letras que usan solo digito de control numerico
        String soloNumero = "ABEH";
        // Letras que usan solo letra de control
        String soloLetra = "KPQSNW";

        char letraControl = "JABCDEFGHI".charAt(digitoControl);

        if (soloNumero.indexOf(primeraLetra) != -1) {
            return control == Character.forDigit(digitoControl, 10);
        } else if (soloLetra.indexOf(primeraLetra) != -1) {
            return control == letraControl;
        } else {
            // Acepta ambos
            return control == Character.forDigit(digitoControl, 10) || control == letraControl;
        }
    }

    /**
     * Valida un email
     */
    public boolean validarEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return false;
        }
        return EMAIL_PATTERN.matcher(email.trim()).matches();
    }

    /**
     * Valida un codigo postal español
     */
    public boolean validarCodigoPostal(String codigoPostal) {
        if (codigoPostal == null || codigoPostal.trim().isEmpty()) {
            return false;
        }
        return CODIGO_POSTAL_PATTERN.matcher(codigoPostal.trim()).matches();
    }

    /**
     * Valida un numero de telefono movil español
     */
    public boolean validarTelefono(String telefono) {
        if (telefono == null || telefono.trim().isEmpty()) {
            return false;
        }
        // Limpiar espacios y guiones
        String telefonoLimpio = telefono.trim().replaceAll("[\\s-]", "");
        return TELEFONO_PATTERN.matcher(telefonoLimpio).matches();
    }

    /**
     * Valida que un importe sea positivo
     */
    public boolean validarImportePositivo(BigDecimal importe) {
        return importe != null && importe.compareTo(BigDecimal.ZERO) > 0;
    }

    /**
     * Valida que un importe no sea negativo
     */
    public boolean validarImporteNoNegativo(BigDecimal importe) {
        return importe != null && importe.compareTo(BigDecimal.ZERO) >= 0;
    }

    /**
     * Valida un IBAN español
     */
    public boolean validarIBAN(String iban) {
        if (iban == null || iban.trim().isEmpty()) {
            return false;
        }

        // Limpiar espacios
        iban = iban.trim().replaceAll("\\s", "").toUpperCase();

        // IBAN español: ES + 2 digitos control + 20 digitos cuenta
        if (!iban.matches("^ES[0-9]{22}$")) {
            return false;
        }

        // Validar digitos de control
        String reordenado = iban.substring(4) + "1428" + iban.substring(2, 4); // ES = 14 28

        try {
            java.math.BigInteger numero = new java.math.BigInteger(reordenado);
            return numero.mod(java.math.BigInteger.valueOf(97)).intValue() == 1;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    /**
     * Valida un numero de cuenta bancaria español (CCC)
     */
    public boolean validarCCC(String ccc) {
        if (ccc == null || ccc.trim().isEmpty()) {
            return false;
        }

        ccc = ccc.trim().replaceAll("[\\s-]", "");

        if (!ccc.matches("^[0-9]{20}$")) {
            return false;
        }

        String entidad = ccc.substring(0, 4);
        String oficina = ccc.substring(4, 8);
        String dc = ccc.substring(8, 10);
        String cuenta = ccc.substring(10, 20);

        int dc1 = calcularDigitoControl("00" + entidad + oficina);
        int dc2 = calcularDigitoControl(cuenta);

        return dc.equals(String.format("%d%d", dc1, dc2));
    }

    private int calcularDigitoControl(String cadena) {
        int[] pesos = {1, 2, 4, 8, 5, 10, 9, 7, 3, 6};
        int suma = 0;
        for (int i = 0; i < 10; i++) {
            suma += Character.getNumericValue(cadena.charAt(i)) * pesos[i];
        }
        int resto = suma % 11;
        return resto == 0 ? 0 : 11 - resto;
    }

    /**
     * Valida que una fecha no sea futura
     */
    public boolean validarFechaNoFutura(LocalDate fecha) {
        return fecha != null && !fecha.isAfter(LocalDate.now());
    }

    /**
     * Valida que una fecha esté dentro del ejercicio actual
     */
    public boolean validarFechaEjercicioActual(LocalDate fecha) {
        if (fecha == null) return false;
        int anioActual = LocalDate.now().getYear();
        return fecha.getYear() == anioActual;
    }

    /**
     * Valida datos completos de un cliente
     */
    public ValidationResult validarCliente(String nombre, String cif, String email) {
        ValidationResult result = new ValidationResult();

        if (nombre == null || nombre.trim().isEmpty()) {
            result.addError("nombre", "El nombre es obligatorio");
        }

        if (cif != null && !cif.trim().isEmpty()) {
            if (!validarCIF(cif) && !validarNIF(cif)) {
                result.addError("cif", "El CIF/NIF no es valido");
            }
        }

        if (email != null && !email.trim().isEmpty()) {
            if (!validarEmail(email)) {
                result.addError("email", "El email no es valido");
            }
        }

        return result;
    }

    /**
     * Valida datos de una factura
     */
    public ValidationResult validarFactura(String numero, LocalDate fecha, BigDecimal total) {
        ValidationResult result = new ValidationResult();

        if (numero == null || numero.trim().isEmpty()) {
            result.addError("numero", "El numero de factura es obligatorio");
        }

        if (fecha == null) {
            result.addError("fecha", "La fecha es obligatoria");
        } else if (fecha.isAfter(LocalDate.now())) {
            result.addError("fecha", "La fecha no puede ser futura");
        }

        if (total == null) {
            result.addError("total", "El total es obligatorio");
        } else if (total.compareTo(BigDecimal.ZERO) < 0) {
            result.addError("total", "El total no puede ser negativo");
        }

        return result;
    }

    /**
     * Clase para resultados de validacion
     */
    public static class ValidationResult {
        private final java.util.Map<String, String> errors = new java.util.HashMap<>();

        public void addError(String campo, String mensaje) {
            errors.put(campo, mensaje);
        }

        public boolean isValid() {
            return errors.isEmpty();
        }

        public java.util.Map<String, String> getErrors() {
            return errors;
        }

        public String getError(String campo) {
            return errors.get(campo);
        }
    }
}

