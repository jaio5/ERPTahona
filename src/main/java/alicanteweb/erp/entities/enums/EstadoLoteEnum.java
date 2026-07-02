package alicanteweb.erp.entities.enums;

public enum EstadoLoteEnum {
    ACTIVO("Activo"),
    CONSUMIDO("Consumido"),
    CADUCADO("Caducado"),
    BLOQUEADO("Bloqueado"),
    RETIRADO("Retirado");

    private final String etiqueta;

    EstadoLoteEnum(String etiqueta) { this.etiqueta = etiqueta; }

    public String getEtiqueta() { return etiqueta; }
}
