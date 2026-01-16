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

    @Column(name = "punto_pedido", precision = 10, scale = 2)
    private BigDecimal puntoPedido;

    @OneToMany(mappedBy = "articulo")
    private Set<AlbaranVentaLinea> albaranVentaLineas = new LinkedHashSet<>();

    @OneToMany(mappedBy = "articulo")
    private Set<FacturaLinea> facturaLineas = new LinkedHashSet<>();

    @OneToMany(mappedBy = "articulo")
    private Set<PedidoLinea> pedidoLineas = new LinkedHashSet<>();

    // Lombok @Data genera getters/setters automáticamente
}