package alicanteweb.erp.entities.enums;

public enum EstadoFacturaEnum {
    BORRADOR("Borrador"),
    EMITIDA("Emitida"),
    PAGADA("Pagada"),
    VENCIDA("Vencida"),
    ANULADA("Anulada"),
    RECTIFICADA("Rectificada");

    private final String etiqueta;

    EstadoFacturaEnum(String etiqueta) { this.etiqueta = etiqueta; }

    public String getEtiqueta() { return etiqueta; }
}
