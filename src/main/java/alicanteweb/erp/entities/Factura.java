package alicanteweb.erp.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.Set;

@Getter
@Setter
@Entity
@Table(name = "facturas")
public class Factura {
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

    @ColumnDefault("0.00")
    @Column(name = "total", precision = 10, scale = 2)
    private BigDecimal total;

    @ColumnDefault("0.00")
    @Column(name = "pagado", precision = 10, scale = 2)
    private BigDecimal pagado;

    @Column(name = "pagada", nullable = false)
    private boolean pagada;

    // Nuevos campos para gestión de estado y Verifactu
    @Size(max = 20)
    @ColumnDefault("'BORRADOR'")
    @Column(name = "estado", length = 20)
    private String estado; // BORRADOR, REVISION, EMITIDA, ANULADA

    @ColumnDefault("FALSE")
    @Column(name = "verifactu_enviada")
    private Boolean verifactuEnviada;

    @Column(name = "fecha_emision_verifactu")
    private LocalDateTime fechaEmisionVerifactu;

    @Lob
    @Column(name = "observaciones_revision")
    private String observacionesRevision;

    @OneToMany(mappedBy = "facturas")
    private Set<AlbaranVentaFactura> albaranesVentaFacturas = new LinkedHashSet<>();

    @OneToMany(mappedBy = "factura")
    private Set<FacturaLinea> facturaLineas = new LinkedHashSet<>();

    @PrePersist
    protected void onCreate() {
        if (estado == null || estado.isEmpty()) {
            estado = "BORRADOR";
        }
        if (verifactuEnviada == null) {
            verifactuEnviada = false;
        }
    }
}