package alicanteweb.erp.entities.enums;

public enum EstadoHojaRutaEnum {
    PLANIFICADA("Planificada"),
    EN_CURSO("En curso"),
    FINALIZADA("Finalizada"),
    INCIDENCIA("Con incidencia"),
    CANCELADA("Cancelada");

    private final String etiqueta;

    EstadoHojaRutaEnum(String etiqueta) { this.etiqueta = etiqueta; }

    public String getEtiqueta() { return etiqueta; }
}
