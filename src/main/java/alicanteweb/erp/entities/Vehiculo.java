package alicanteweb.erp.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.Objects;

@Getter
@Setter
@Entity
@Table(name = "vehiculos")
public class Vehiculo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Size(max = 20)
    @NotNull
    @Column(name = "matricula", nullable = false, length = 20)
    private String matricula;

    @Size(max = 100)
    @Column(name = "marca", length = 100)
    private String marca;

    @Size(max = 100)
    @Column(name = "modelo", length = 100)
    private String modelo;

    @Size(max = 50)
    @Column(name = "tipo", length = 50)
    private String tipo;

    @Column(name = "capacidad_kg", precision = 10, scale = 2)
    private BigDecimal capacidadKg;

    @Column(name = "capacidad_volumen", precision = 10, scale = 2)
    private BigDecimal capacidadVolumen;

    @Column(name = "consumo_medio", precision = 5, scale = 2)
    private BigDecimal consumoMedio;

    @Column(name = "activo")
    private Boolean activo = true;

    @Column(name = "observaciones", length = 1000)
    private String observaciones;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Vehiculo)) return false;
        return Objects.equals(id, ((Vehiculo) o).id);
    }

    @Override
    public int hashCode() { return Objects.hashCode(id); }

    @Override
    public String toString() { return "Vehiculo{id=" + id + ", matricula='" + matricula + "'}"; }
}
