package alicanteweb.erp.entities;

import jakarta.persistence.*;
import org.hibernate.annotations.ColumnDefault;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.LinkedHashSet;
import java.util.Set;

@Entity
@Table(name = "facturas")
public class Factura {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "numero", nullable = false, length = 50)
    private String numero;

    @Column(name = "fecha")
    private LocalDate fecha;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cliente_id")
    private Cliente cliente;

    @ColumnDefault("0.00")
    @Column(name = "total", precision = 10, scale = 2)
    private BigDecimal total;

    @ColumnDefault("0.00")
    @Column(name = "pagado", precision = 10, scale = 2)
    private BigDecimal pagado;

    @ManyToMany(mappedBy = "facturas")
    private Set<AlbaranesVenta> albaranesVentas = new LinkedHashSet<>();

    @OneToMany(mappedBy = "factura")
    private Set<FacturaLinea> facturaLineas = new LinkedHashSet<>();

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNumero() {
        return numero;
    }

    public void setNumero(String numero) {
        this.numero = numero;
    }

    public LocalDate getFecha() {
        return fecha;
    }

    public void setFecha(LocalDate fecha) {
        this.fecha = fecha;
    }

    public Cliente getCliente() {
        return cliente;
    }

    public void setCliente(Cliente cliente) {
        this.cliente = cliente;
    }

    public BigDecimal getTotal() {
        return total;
    }

    public void setTotal(BigDecimal total) {
        this.total = total;
    }

    public BigDecimal getPagado() {
        return pagado;
    }

    public void setPagado(BigDecimal pagado) {
        this.pagado = pagado;
    }

    public Set<AlbaranesVenta> getAlbaranesVentas() {
        return albaranesVentas;
    }

    public void setAlbaranesVentas(Set<AlbaranesVenta> albaranesVentas) {
        this.albaranesVentas = albaranesVentas;
    }

    public Set<FacturaLinea> getFacturaLineas() {
        return facturaLineas;
    }

    public void setFacturaLineas(Set<FacturaLinea> facturaLineas) {
        this.facturaLineas = facturaLineas;
    }

}