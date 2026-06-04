package alicanteweb.erp.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalTime;
import java.util.Objects;

@Getter
@Setter
@Entity
@Table(name = "rutas_paradas")
public class RutaParada {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ruta_id", nullable = false)
    private RutaReparto ruta;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cliente_id", nullable = false)
    private Cliente cliente;

    @Column(name = "orden")
    private Integer orden;

    @Column(name = "hora_estimada")
    private LocalTime horaEstimada;

    @Column(name = "tiempo_parada_minutos")
    private Integer tiempoParadaMinutos;

    @Column(name = "notas", length = 500)
    private String notas;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof RutaParada)) return false;
        return Objects.equals(id, ((RutaParada) o).id);
    }

    @Override
    public int hashCode() { return Objects.hashCode(id); }
}
