package alicanteweb.erp.entities.enums;

public enum EstadoRgpdSolicitudEnum {
    PENDIENTE("Pendiente"),
    EN_PROCESO("En proceso"),
    COMPLETADA("Completada"),
    RECHAZADA("Rechazada");

    private final String etiqueta;

    EstadoRgpdSolicitudEnum(String etiqueta) { this.etiqueta = etiqueta; }

    public String getEtiqueta() { return etiqueta; }
}
