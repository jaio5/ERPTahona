package alicanteweb.erp.entities.enums;

import alicanteweb.erp.entities.Cliente;
import alicanteweb.erp.entities.EmpresaConfig;

import java.util.Arrays;
import java.util.Optional;

/**
 * Catálogo de datos que el ERP ya conoce y puede imprimir automáticamente en facturas/albaranes.
 * Cada entrada resuelve su valor desde la configuración de empresa o desde el cliente del documento,
 * de modo que el usuario solo elige el campo del desplegable, sin teclear el valor.
 */
public enum CampoSistema {

    // --- Datos de la empresa (EmpresaConfig) ---
    EMPRESA_REGISTRO_SANITARIO("Registro sanitario", Fuente.EMPRESA),
    EMPRESA_REGISTRO_MERCANTIL("Registro mercantil", Fuente.EMPRESA),
    EMPRESA_IBAN("IBAN empresa", Fuente.EMPRESA),
    EMPRESA_SEPA_CREDITOR("Identificador de acreedor SEPA", Fuente.EMPRESA),
    EMPRESA_TELEFONO("Teléfono", Fuente.EMPRESA),
    EMPRESA_WHATSAPP("WhatsApp", Fuente.EMPRESA),
    EMPRESA_EMAIL("Email", Fuente.EMPRESA),
    EMPRESA_WEB("Web", Fuente.EMPRESA),

    // --- Datos del cliente del documento (Cliente) ---
    CLIENTE_CODIGO("Código de cliente", Fuente.CLIENTE),
    CLIENTE_REPRESENTANTE("Representante", Fuente.CLIENTE),
    CLIENTE_IBAN("IBAN cliente", Fuente.CLIENTE),
    CLIENTE_MANDATO_SEPA("Referencia mandato SEPA", Fuente.CLIENTE);

    public enum Fuente { EMPRESA, CLIENTE }

    private final String etiqueta;
    private final Fuente fuente;

    CampoSistema(String etiqueta, Fuente fuente) {
        this.etiqueta = etiqueta;
        this.fuente = fuente;
    }

    public String getEtiqueta() { return etiqueta; }

    public Fuente getFuente() { return fuente; }

    /** Nombre estable que se guarda en {@code CampoPersonalizado.claveSistema}. */
    public String getClave() { return name(); }

    /** Resuelve la clave a su entrada del catálogo, o vacío si no existe (dato huérfano). */
    public static Optional<CampoSistema> desde(String clave) {
        if (clave == null || clave.isBlank()) return Optional.empty();
        return Arrays.stream(values()).filter(c -> c.name().equals(clave)).findFirst();
    }

    /** Valor a imprimir, tomado de empresa o cliente según la fuente. Puede ser {@code null}. */
    public String valor(EmpresaConfig empresa, Cliente cliente) {
        return switch (this) {
            case EMPRESA_REGISTRO_SANITARIO -> empresa == null ? null : empresa.getRegistroSanitario();
            case EMPRESA_REGISTRO_MERCANTIL -> empresa == null ? null : empresa.getRegistroMercantil();
            case EMPRESA_IBAN -> empresa == null ? null : empresa.getIban();
            case EMPRESA_SEPA_CREDITOR -> empresa == null ? null : empresa.getSepaCreditorId();
            case EMPRESA_TELEFONO -> empresa == null ? null : empresa.getTelefono();
            case EMPRESA_WHATSAPP -> empresa == null ? null : empresa.getWhatsapp();
            case EMPRESA_EMAIL -> empresa == null ? null : empresa.getEmail();
            case EMPRESA_WEB -> empresa == null ? null : empresa.getWeb();
            case CLIENTE_CODIGO -> cliente == null ? null : cliente.getCodigo();
            case CLIENTE_REPRESENTANTE -> cliente == null ? null : cliente.getRepresentante();
            case CLIENTE_IBAN -> cliente == null ? null : cliente.getIban();
            case CLIENTE_MANDATO_SEPA -> cliente == null ? null : cliente.getMandatoSepaReferencia();
        };
    }
}
