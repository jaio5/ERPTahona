package alicanteweb.erp.entities.enums;

public enum EstadoPresupuestoEnum {
    BORRADOR("Borrador"),
    ENVIADO("Enviado"),
    ACEPTADO("Aceptado"),
    RECHAZADO("Rechazado"),
    EXPIRADO("Expirado"),
    CONVERTIDO("Convertido a pedido");

    private final String etiqueta;

    EstadoPresupuestoEnum(String etiqueta) { this.etiqueta = etiqueta; }

    public String getEtiqueta() { return etiqueta; }
}
