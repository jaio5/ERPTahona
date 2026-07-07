package alicanteweb.erp.entities.enums;

public enum EstadoOrdenProduccionEnum {
    PLANIFICADA("Planificada"),
    EN_CURSO("En curso"),
    FINALIZADA("Finalizada"),
    CANCELADA("Cancelada");

    private final String etiqueta;

    EstadoOrdenProduccionEnum(String etiqueta) { this.etiqueta = etiqueta; }

    public String getEtiqueta() { return etiqueta; }
}
