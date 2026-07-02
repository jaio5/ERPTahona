package alicanteweb.erp.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Remesa SEPA de adeudos directos (pain.008.001.02, esquema CORE).
 */
@Getter
@Setter
@Entity
@Table(name = "remesas_sepa")
public class RemesaSepa {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "fecha_creacion")
    private LocalDateTime fechaCreacion;

    /** Fecha de cobro solicitada (ReqdColltnDt). */
    @Column(name = "fecha_cobro", nullable = false)
    private LocalDate fechaCobro;

    @Column(name = "concepto", length = 140)
    private String concepto;

    @Column(name = "estado", nullable = false, length = 20)
    private String estado; // GENERADA, COBRADA, ANULADA

    @Column(name = "total", nullable = false, precision = 12, scale = 2)
    private BigDecimal total;

    @Column(name = "num_recibos", nullable = false)
    private Integer numRecibos;

    @Column(name = "xml", columnDefinition = "LONGTEXT")
    private String xml;

    @Column(name = "usuario_creacion")
    private String usuarioCreacion;

    @OneToMany(mappedBy = "remesa", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<RemesaSepaLinea> lineas = new ArrayList<>();

    @PrePersist
    void prePersist() {
        if (fechaCreacion == null) {
            fechaCreacion = LocalDateTime.now();
        }
        if (estado == null) {
            estado = "GENERADA";
        }
    }
}
