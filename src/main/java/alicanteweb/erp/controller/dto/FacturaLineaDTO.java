package alicanteweb.erp.controller.dto;

import alicanteweb.erp.entities.Articulo;
import javafx.beans.property.*;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * DTO para representar una línea de factura en la tabla del formulario
 */
public class FacturaLineaDTO {
    private final ObjectProperty<Articulo> articulo = new SimpleObjectProperty<>();
    private final StringProperty descripcion = new SimpleStringProperty();
    private final ObjectProperty<BigDecimal> cantidad = new SimpleObjectProperty<>(BigDecimal.ONE);
    private final ObjectProperty<BigDecimal> precio = new SimpleObjectProperty<>(BigDecimal.ZERO);
    private final ObjectProperty<BigDecimal> iva = new SimpleObjectProperty<>(new BigDecimal("21.00"));
    private final ObjectProperty<BigDecimal> subtotal = new SimpleObjectProperty<>(BigDecimal.ZERO);
    private final ObjectProperty<BigDecimal> total = new SimpleObjectProperty<>(BigDecimal.ZERO);
    
    public FacturaLineaDTO() {
        // Listeners para calcular automáticamente subtotal y total
        cantidad.addListener((obs, oldVal, newVal) -> calcularTotales());
        precio.addListener((obs, oldVal, newVal) -> calcularTotales());
        iva.addListener((obs, oldVal, newVal) -> calcularTotales());
    }
    
    public FacturaLineaDTO(Articulo articulo) {
        this();
        setArticulo(articulo);
        if (articulo != null) {
            setDescripcion(articulo.getDescripcion());
            if (articulo.getPvp() != null) {
                setPrecio(articulo.getPvp());
            }
            if (articulo.getIva() != null) {
                setIva(articulo.getIva());
            }
        }
    }
    
    private void calcularTotales() {
        BigDecimal cant = cantidad.get() != null ? cantidad.get() : BigDecimal.ZERO;
        BigDecimal prec = precio.get() != null ? precio.get() : BigDecimal.ZERO;
        BigDecimal ivaVal = iva.get() != null ? iva.get() : BigDecimal.ZERO;
        
        // Subtotal = cantidad * precio
        BigDecimal sub = cant.multiply(prec).setScale(2, RoundingMode.HALF_UP);
        subtotal.set(sub);
        
        // Total = subtotal + (subtotal * iva / 100)
        BigDecimal importeIva = sub.multiply(ivaVal).divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
        BigDecimal tot = sub.add(importeIva).setScale(2, RoundingMode.HALF_UP);
        total.set(tot);
    }
    
    // Getters y setters
    public Articulo getArticulo() { return articulo.get(); }
    public void setArticulo(Articulo value) { articulo.set(value); }
    public ObjectProperty<Articulo> articuloProperty() { return articulo; }
    
    public String getDescripcion() { return descripcion.get(); }
    public void setDescripcion(String value) { descripcion.set(value); }
    public StringProperty descripcionProperty() { return descripcion; }
    
    public BigDecimal getCantidad() { return cantidad.get(); }
    public void setCantidad(BigDecimal value) { cantidad.set(value); }
    public ObjectProperty<BigDecimal> cantidadProperty() { return cantidad; }
    
    public BigDecimal getPrecio() { return precio.get(); }
    public void setPrecio(BigDecimal value) { precio.set(value); }
    public ObjectProperty<BigDecimal> precioProperty() { return precio; }
    
    public BigDecimal getIva() { return iva.get(); }
    public void setIva(BigDecimal value) { iva.set(value); }
    public ObjectProperty<BigDecimal> ivaProperty() { return iva; }
    
    public BigDecimal getSubtotal() { return subtotal.get(); }
    public void setSubtotal(BigDecimal value) { subtotal.set(value); }
    public ObjectProperty<BigDecimal> subtotalProperty() { return subtotal; }
    
    public BigDecimal getTotal() { return total.get(); }
    public void setTotal(BigDecimal value) { total.set(value); }
    public ObjectProperty<BigDecimal> totalProperty() { return total; }
}

