package alicanteweb.erp.entities;

import jakarta.persistence.*;
import org.hibernate.annotations.ColumnDefault;

import java.math.BigDecimal;
import java.util.LinkedHashSet;
import java.util.Set;

@Entity
@Table(name = "articulos")
public class Articulo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "codigo", nullable = false, length = 50)
    private String codigo;

    @Column(name = "descripcion")
    private String descripcion;

    @Column(name = "familia", length = 100)
    private String familia;

    @Column(name = "unidad", length = 20)
    private String unidad;

    @Column(name = "iva", precision = 5, scale = 2)
    private BigDecimal iva;

    @ColumnDefault("0.00")
    @Column(name = "pvp", precision = 10, scale = 2)
    private BigDecimal pvp;

    @ColumnDefault("0.00")
    @Column(name = "coste", precision = 10, scale = 2)
    private BigDecimal coste;

    @OneToMany(mappedBy = "articulo")
    private Set<AlbaranVentaLinea> albaranVentaLineas = new LinkedHashSet<>();

    @OneToMany(mappedBy = "articulo")
    private Set<FacturaLinea> facturaLineas = new LinkedHashSet<>();

    @OneToMany(mappedBy = "articulo")
    private Set<PedidoLinea> pedidoLineas = new LinkedHashSet<>();

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getFamilia() {
        return familia;
    }

    public void setFamilia(String familia) {
        this.familia = familia;
    }

    public String getUnidad() {
        return unidad;
    }

    public void setUnidad(String unidad) {
        this.unidad = unidad;
    }

    public BigDecimal getIva() {
        return iva;
    }

    public void setIva(BigDecimal iva) {
        this.iva = iva;
    }

    public BigDecimal getPvp() {
        return pvp;
    }

    public void setPvp(BigDecimal pvp) {
        this.pvp = pvp;
    }

    public BigDecimal getCoste() {
        return coste;
    }

    public void setCoste(BigDecimal coste) {
        this.coste = coste;
    }

    public Set<AlbaranVentaLinea> getAlbaranVentaLineas() {
        return albaranVentaLineas;
    }

    public void setAlbaranVentaLineas(Set<AlbaranVentaLinea> albaranVentaLineas) {
        this.albaranVentaLineas = albaranVentaLineas;
    }

    public Set<FacturaLinea> getFacturaLineas() {
        return facturaLineas;
    }

    public void setFacturaLineas(Set<FacturaLinea> facturaLineas) {
        this.facturaLineas = facturaLineas;
    }

    public Set<PedidoLinea> getPedidoLineas() {
        return pedidoLineas;
    }

    public void setPedidoLineas(Set<PedidoLinea> pedidoLineas) {
        this.pedidoLineas = pedidoLineas;
    }

}