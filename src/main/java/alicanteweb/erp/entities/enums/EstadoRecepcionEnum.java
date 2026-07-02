package alicanteweb.erp.entities.enums;

public enum EstadoRecepcionEnum {
    PENDIENTE("Pendiente"),
    CONFIRMADA("Confirmada"),
    CANCELADA("Cancelada");

    private final String etiqueta;

    EstadoRecepcionEnum(String etiqueta) { this.etiqueta = etiqueta; }

    public String getEtiqueta() { return etiqueta; }
}
