package alicanteweb.erp.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.regex.Pattern;

/**
 * Servicio de validaciones avanzadas según normativa española
 */
@Service
public class ValidacionService {
    private static final Logger log = LoggerFactory.getLogger(ValidacionService.class);

    // Patrones de validación
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
        "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"
    );

    private static final Pattern CODIGO_POSTAL_PATTERN = Pattern.compile(
        "^(?:0[1-9]|[1-4][0-9]|5[0-2])[0-9]{3}$"
    );

    private static final Pattern TELEFONO_PATTERN = Pattern.compile(
        "^[679][0-9]{8}$"
    );

    /**
     * Valida un NIF español (DNI/NIE)
     */
    public boolean validarNIF(String nif) {
        if (nif == null || nif.trim().isEmpty()) {
            return false;
        }

        nif = nif.trim().toUpperCase();

        // DNI: 8 dígitos + letra
        if (nif.matches("^[0-9]{8}[A-Z]$")) {
            return validarDNI(nif);
        }

        // NIE: X/Y/Z + 7 dígitos + letra
        if (nif.matches("^[XYZ][0-9]{7}[A-Z]$")) {
            return validarNIE(nif);
        }

        return false;
    }

    /**
     * Valida un DNI español
     */
    private boolean validarDNI(String dni) {
        String letras = "TRWAGMYFPDXBNJZSQVHLCKE";
        String numeros = dni.substring(0, 8);
        char letra = dni.charAt(8);

        try {
            int numero = Integer.parseInt(numeros);
            char letraEsperada = letras.charAt(numero % 23);
            return letra == letraEsperada;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    /**
     * Valida un NIE español
     */
    private boolean validarNIE(String nie) {
        String letras = "TRWAGMYFPDXBNJZSQVHLCKE";

        // Reemplazar primera letra por número
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
            char letraEsperada = letras.charAt(numero % 23);
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

        // Formato: Letra + 7 dígitos + dígito/letra control
        if (!cif.matches("^[A-Z][0-9]{7}[0-9A-Z]$")) {
            return false;
        }

        char primeraLetra = cif.charAt(0);

        // Tipos válidos de CIF
        String tiposValidos = "ABCDEFGHJNPQRSUVW";
        if (tiposValidos.indexOf(primeraLetra) == -1) {
            return false;
        }

        // Validar dígito de control
        return validarDigitoControlCIF(cif);
    }

    /**
     * Valida el dígito de control del CIF
     */
    private boolean validarDigitoControlCIF(String cif) {
        String numeros = cif.substring(1, 8);
        char control = cif.charAt(8);

        int suma = 0;

        // Sumar dígitos en posiciones pares (multiplicados por 2)
        for (int i = 1; i < 7; i += 2) {
            int digito = Character.getNumericValue(numeros.charAt(i)) * 2;
            suma += digito / 10 + digito % 10;
        }

        // Sumar dígitos en posiciones impares
        for (int i = 0; i < 7; i += 2) {
            suma += Character.getNumericValue(numeros.charAt(i));
        }

        int unidad = suma % 10;
        int digitoControl = unidad == 0 ? 0 : 10 - unidad;

        // Algunos CIF usan letra en vez de dígito
        char letraControl = "JABCDEFGHI".charAt(digitoControl);

        return control == Character.forDigit(digitoControl, 10) || control == letraControl;
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
     * Valida un código postal español
     */
    public boolean validarCodigoPostal(String codigoPostal) {
        if (codigoPostal == null || codigoPostal.trim().isEmpty()) {
            return false;
        }

        return CODIGO_POSTAL_PATTERN.matcher(codigoPostal.trim()).matches();
    }

    /**
     * Valida un número de teléfono móvil español
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
     * Valida que una fecha no sea futura
     */
    public boolean validarFechaNoFutura(LocalDate fecha) {
        return fecha != null && !fecha.isAfter(LocalDate.now());
    }

    /**
     * Valida que una fecha esté en un rango
     */
    public boolean validarFechaEnRango(LocalDate fecha, LocalDate desde, LocalDate hasta) {
        if (fecha == null) return false;
        if (desde != null && fecha.isBefore(desde)) return false;
        if (hasta != null && fecha.isAfter(hasta)) return false;
        return true;
    }

    /**
     * Valida que un texto no exceda una longitud máxima
     */
    public boolean validarLongitudMaxima(String texto, int longitudMaxima) {
        if (texto == null) return true;
        return texto.length() <= longitudMaxima;
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

        // IBAN español: ES + 2 dígitos control + 20 dígitos
        if (!iban.matches("^ES[0-9]{22}$")) {
            return false;
        }

        // Validar dígito de control
        return validarDigitoControlIBAN(iban);
    }

    /**
     * Valida el dígito de control del IBAN
     */
    private boolean validarDigitoControlIBAN(String iban) {
        // Mover los 4 primeros caracteres al final
        String reordenado = iban.substring(4) + iban.substring(0, 4);

        // Reemplazar letras por números (A=10, B=11, ..., Z=35)
        StringBuilder numerico = new StringBuilder();
        for (char c : reordenado.toCharArray()) {
            if (Character.isLetter(c)) {
                numerico.append(c - 'A' + 10);
            } else {
                numerico.append(c);
            }
        }

        // Calcular módulo 97
        return mod97(numerico.toString()) == 1;
    }

    /**
     * Calcula módulo 97 para cadenas grandes
     */
    private int mod97(String numero) {
        int resultado = 0;
        for (char c : numero.toCharArray()) {
            resultado = (resultado * 10 + Character.getNumericValue(c)) % 97;
        }
        return resultado;
    }

    /**
     * Validación completa de un cliente
     */
    public ValidationResult validarCliente(String nombre, String cif, String email, String codigoPostal) {
        ValidationResult result = new ValidationResult();

        if (nombre == null || nombre.trim().isEmpty()) {
            result.addError("Nombre obligatorio");
        } else if (!validarLongitudMaxima(nombre, 100)) {
            result.addError("Nombre demasiado largo (máximo 100 caracteres)");
        }

        if (cif != null && !cif.trim().isEmpty()) {
            if (!validarNIF(cif) && !validarCIF(cif)) {
                result.addError("NIF/CIF inválido");
            }
        }

        if (email != null && !email.trim().isEmpty()) {
            if (!validarEmail(email)) {
                result.addError("Email inválido");
            }
        }

        if (codigoPostal != null && !codigoPostal.trim().isEmpty()) {
            if (!validarCodigoPostal(codigoPostal)) {
                result.addError("Código postal inválido");
            }
        }

        return result;
    }

    /**
     * Resultado de validación
     */
    public static class ValidationResult {
        private final java.util.List<String> errores = new java.util.ArrayList<>();

        public void addError(String error) {
            errores.add(error);
        }

        public boolean isValid() {
            return errores.isEmpty();
        }

        public java.util.List<String> getErrores() {
            return errores;
        }

        public String getErroresComoTexto() {
            return String.join(", ", errores);
        }
    }
}

