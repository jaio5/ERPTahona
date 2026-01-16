package alicanteweb.erp.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Entity
@Table(name = "almacenes")
public class Almacen {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Size(max = 50)
    @NotNull
    @Column(name = "codigo", nullable = false, length = 50)
    private String codigo;

    @Size(max = 255)
    @Column(name = "nombre")
    private String nombre;

    @Column(name = "activo")
    private Boolean activo = true;

    // Nuevos campos persistentes añadidos
    @Column(name = "descripcion", length = 1000)
    private String descripcion;

    @Column(name = "capacidad", precision = 10, scale = 2)
    private BigDecimal capacidad;

    @Column(name = "disponible", precision = 10, scale = 2)
    private BigDecimal disponible;

    @Column(name = "localidad", length = 255)
    private String localidad;

    @Column(name = "responsable", length = 255)
    private String responsable;

    // Lombok @Data generará getters/setters
}