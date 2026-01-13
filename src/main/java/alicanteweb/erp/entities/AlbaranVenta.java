package alicanteweb.erp.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.hibernate.annotations.ColumnDefault;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.LinkedHashSet;
import java.util.Set;

@Data
@Entity
@Table(name = "albaranes_venta")
public class AlbaranVenta {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Size(max = 50)
    @NotNull
    @Column(name = "numero", nullable = false, length = 50)
    private String numero;

    @Column(name = "fecha")
    private LocalDate fecha;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cliente_id")
    private Cliente cliente;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "almacen_id")
    private Almacen almacen;

    @Lob
    @Column(name = "observaciones")
    private String observaciones;

    @ColumnDefault("0.00")
    @Column(name = "total", precision = 10, scale = 2)
    private BigDecimal total;

    @OneToMany(mappedBy = "albaran", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<AlbaranVentaLinea> albaranVentaLineas = new LinkedHashSet<>();

    // Getters y Setters explícitos
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNumero() { return numero; }
    public void setNumero(String numero) { this.numero = numero; }

    public LocalDate getFecha() { return fecha; }
    public void setFecha(LocalDate fecha) { this.fecha = fecha; }

    public Cliente getCliente() { return cliente; }
    public void setCliente(Cliente cliente) { this.cliente = cliente; }

    public Almacen getAlmacen() { return almacen; }
    public void setAlmacen(Almacen almacen) { this.almacen = almacen; }

    public String getObservaciones() { return observaciones; }
    public void setObservaciones(String observaciones) { this.observaciones = observaciones; }

    public BigDecimal getTotal() { return total; }
    public void setTotal(BigDecimal total) { this.total = total; }

    public Set<AlbaranVentaLinea> getAlbaranVentaLineas() { return albaranVentaLineas; }
    public void setAlbaranVentaLineas(Set<AlbaranVentaLinea> albaranVentaLineas) { this.albaranVentaLineas = albaranVentaLineas; }

    /**
     * Alias para compatibilidad - getLineas() retorna albaranVentaLineas
     */
    public Set<AlbaranVentaLinea> getLineas() {
        return this.albaranVentaLineas;
    }
}