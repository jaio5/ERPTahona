package alicanteweb.erp.entities;

/**
 * Estados posibles de una factura en el flujo de emisiÃ³n
 */
public enum EstadoFactura {
    /**
     * Factura reciÃ©n creada, en ediciÃ³n
     */
    BORRADOR("Borrador", "Factura en ediciÃ³n, no emitida"),

    /**
     * Factura pendiente de revisiÃ³n antes de emitir
     */
    REVISION("En RevisiÃ³n", "Pendiente de revisiÃ³n y aprobaciÃ³n"),

    /**
     * Factura emitida y enviada a Verifactu/AEAT
     */
    EMITIDA("Emitida", "Factura emitida y registrada"),

    /**
     * Factura anulada, no vÃ¡lida
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
     * Verifica si la factura puede pasar a revisiÃ³n
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


