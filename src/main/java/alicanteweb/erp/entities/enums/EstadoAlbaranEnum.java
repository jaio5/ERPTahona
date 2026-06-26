package alicanteweb.erp.entities.enums;

public enum EstadoAlbaranEnum {
    PENDIENTE("Pendiente"),
    FACTURADO("Facturado"),
    ANULADO("Anulado");

    private final String etiqueta;

    EstadoAlbaranEnum(String etiqueta) { this.etiqueta = etiqueta; }

    public String getEtiqueta() { return etiqueta; }
}
