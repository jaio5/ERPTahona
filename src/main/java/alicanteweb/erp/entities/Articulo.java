package alicanteweb.erp.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;

import java.math.BigDecimal;
import java.util.LinkedHashSet;
import java.util.Set;

@Getter
@Setter
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

    @OneToMany(mappedBy = "articulo")
    private Set<AlbaranVentaLinea> albaranVentaLineas = new LinkedHashSet<>();

    @OneToMany(mappedBy = "articulo")
    private Set<FacturaLinea> facturaLineas = new LinkedHashSet<>();

    @OneToMany(mappedBy = "articulo")
    private Set<PedidoLinea> pedidoLineas = new LinkedHashSet<>();

}