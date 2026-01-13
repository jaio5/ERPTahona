package alicanteweb.erp.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.hibernate.annotations.ColumnDefault;

import java.math.BigDecimal;
import java.util.LinkedHashSet;
import java.util.Set;

@Data
@Entity
@Table(name = "articulos")
public class Articulo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Size(max = 50)
    @NotNull
    @Column(name = "codigo", nullable = false, length = 50)
    private String codigo;

    @Size(max = 255)
    @Column(name = "descripcion")
    private String descripcion;

    @Size(max = 255)
    @Column(name = "nombre")
    private String nombre;

    @Size(max = 50)
    @Column(name = "codigo_barras", length = 50)
    private String codigoBarras;

    @Size(max = 100)
    @Column(name = "categoria", length = 100)
    private String categoria;

    @Size(max = 100)
    @Column(name = "familia", length = 100)
    private String familia;

    @Size(max = 20)
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

    @Column(name = "stock", precision = 10, scale = 2)
    private BigDecimal stock;

    @Column(name = "stock_minimo", precision = 10, scale = 2)
    private BigDecimal stockMinimo;

    @Column(name = "stock_maximo", precision = 10, scale = 2)
    private BigDecimal stockMaximo;

    @Column(name = "control_stock")
    private Boolean controlStock;

    @Column(name = "activo")
    private Boolean activo = true;

    @OneToMany(mappedBy = "articulo")
    private Set<AlbaranVentaLinea> albaranVentaLineas = new LinkedHashSet<>();

    @OneToMany(mappedBy = "articulo")
    private Set<FacturaLinea> facturaLineas = new LinkedHashSet<>();

    @OneToMany(mappedBy = "articulo")
    private Set<PedidoLinea> pedidoLineas = new LinkedHashSet<>();

    // Getters y Setters explícitos
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getCodigo() { return codigo; }
    public void setCodigo(String codigo) { this.codigo = codigo; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getCodigoBarras() { return codigoBarras; }
    public void setCodigoBarras(String codigoBarras) { this.codigoBarras = codigoBarras; }

    public String getCategoria() { return categoria; }
    public void setCategoria(String categoria) { this.categoria = categoria; }

    public String getFamilia() { return familia; }
    public void setFamilia(String familia) { this.familia = familia; }

    public String getUnidad() { return unidad; }
    public void setUnidad(String unidad) { this.unidad = unidad; }

    public BigDecimal getIva() { return iva; }
    public void setIva(BigDecimal iva) { this.iva = iva; }

    public BigDecimal getPvp() { return pvp; }
    public void setPvp(BigDecimal pvp) { this.pvp = pvp; }

    public BigDecimal getCoste() { return coste; }
    public void setCoste(BigDecimal coste) { this.coste = coste; }

    public BigDecimal getStock() { return stock; }
    public void setStock(BigDecimal stock) { this.stock = stock; }

    public BigDecimal getStockMinimo() { return stockMinimo; }
    public void setStockMinimo(BigDecimal stockMinimo) { this.stockMinimo = stockMinimo; }

    public BigDecimal getStockMaximo() { return stockMaximo; }
    public void setStockMaximo(BigDecimal stockMaximo) { this.stockMaximo = stockMaximo; }

    public Boolean getControlStock() { return controlStock; }
    public void setControlStock(Boolean controlStock) { this.controlStock = controlStock; }

    public Boolean getActivo() { return activo; }
    public void setActivo(Boolean activo) { this.activo = activo; }

    public Set<AlbaranVentaLinea> getAlbaranVentaLineas() { return albaranVentaLineas; }
    public void setAlbaranVentaLineas(Set<AlbaranVentaLinea> albaranVentaLineas) { this.albaranVentaLineas = albaranVentaLineas; }

    public Set<FacturaLinea> getFacturaLineas() { return facturaLineas; }
    public void setFacturaLineas(Set<FacturaLinea> facturaLineas) { this.facturaLineas = facturaLineas; }

    public Set<PedidoLinea> getPedidoLineas() { return pedidoLineas; }
    public void setPedidoLineas(Set<PedidoLinea> pedidoLineas) { this.pedidoLineas = pedidoLineas; }
}