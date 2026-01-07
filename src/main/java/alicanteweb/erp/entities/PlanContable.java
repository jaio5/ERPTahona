package alicanteweb.erp.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

/**
 * Plan Contable Español - Cuentas contables
 */
@Getter
@Setter
@Entity
@Table(name = "plan_contable")
public class PlanContable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Size(max = 20)
    @Column(name = "codigo", nullable = false, unique = true, length = 20)
    private String codigo;

    @NotNull
    @Size(max = 255)
    @Column(name = "nombre", nullable = false)
    private String nombre;

    @Size(max = 20)
    @Column(name = "tipo", length = 20)
    private String tipo; // ACTIVO, PASIVO, PATRIMONIO_NETO, INGRESOS, GASTOS

    @Column(name = "nivel")
    private Integer nivel;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "padre_id")
    private PlanContable padre;

    @Column(name = "activa")
    private Boolean activa;

    @PrePersist
    protected void onCreate() {
        if (activa == null) activa = true;
    }

    @Override
    public String toString() {
        return codigo + " - " + nombre;
    }
}

