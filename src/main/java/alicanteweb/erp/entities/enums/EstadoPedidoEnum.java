package alicanteweb.erp.entities.enums;

public enum EstadoPedidoEnum {
    PENDIENTE("Pendiente"),
    CONFIRMADO("Confirmado"),
    EN_PROCESO("En proceso"),
    SERVIDO("Servido"),
    PARCIAL("Servido parcial"),
    CANCELADO("Cancelado");

    private final String etiqueta;

    EstadoPedidoEnum(String etiqueta) { this.etiqueta = etiqueta; }

    public String getEtiqueta() { return etiqueta; }
}
