package alicanteweb.erp.entities;

/**
 * Estados posibles de una factura en el flujo de emisión
 */
public enum EstadoFactura {
    /**
     * Factura recién creada, en edición
     */
    BORRADOR("Borrador", "Factura en edición, no emitida"),

    /**
     * Factura pendiente de revisión antes de emitir
     */
    REVISION("En Revisión", "Pendiente de revisión y aprobación"),

    /**
     * Factura emitida y enviada a Verifactu/AEAT
     */
    EMITIDA("Emitida", "Factura emitida y registrada"),

    /**
     * Factura anulada, no válida
     */
    ANULADA("Anulada", "Factura anulada o cancelada");

    private final String nombre;
    private final String descripcion;

    EstadoFactura(String nombre, String descripcion) {
        this.nombre = nombre;
        this.descripcion = descripcion;
    }

    public String getNombre() {
        return nombre;
    }

    public String getDescripcion() {
        return descripcion;
    }

    /**
     * Verifica si la factura puede ser modificada
     */
    public boolean esModificable() {
        return this == BORRADOR || this == REVISION;
    }

    /**
     * Verifica si la factura ya fue emitida
     */
    public boolean estaEmitida() {
        return this == EMITIDA;
    }

    /**
     * Verifica si la factura puede pasar a revisión
     */
    public boolean puedeIrARevision() {
        return this == BORRADOR;
    }

    /**
     * Verifica si la factura puede ser emitida
     */
    public boolean puedeSerEmitida() {
        return this == REVISION;
    }

    /**
     * Verifica si la factura puede ser anulada
     */
    public boolean puedeSerAnulada() {
        return this == EMITIDA;
    }
}

