package alicanteweb.erp.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

/**
 * Entidad que representa el plan de cuentas contables (PGC España)
 */
@Getter
@Setter
@Entity
@Table(name = "plan_cuentas")
public class PlanCuentas {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "codigo", length = 20, unique = true, nullable = false)
    private String codigo;

    @Column(name = "nombre", length = 255, nullable = false)
    private String nombre;

    @Column(name = "tipo", length = 50, nullable = false)
    private String tipo; // ACTIVO, PASIVO, GASTO, INGRESO, PATRIMONIO

    @Column(name = "nivel", nullable = false)
    private Integer nivel = 1;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cuenta_padre_id")
    private PlanCuentas cuentaPadre;

    @Column(name = "descripcion", columnDefinition = "TEXT")
    private String descripcion;

    @Column(name = "activo")
    private Boolean activo = true;

    @PrePersist
    protected void onCreate() {
        if (activo == null) activo = true;
        if (nivel == null) nivel = 1;
    }
}

