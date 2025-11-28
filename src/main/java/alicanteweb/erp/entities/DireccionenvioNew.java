package alicanteweb.erp.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Getter
@Setter
@Entity
@Table(name = "direccionesenvio_new")
public class DireccionenvioNew {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "cliente_id")
    private Cliente cliente;

    @Column(name = "codigo_direccion")
    private Integer codigoDireccion;

    @Lob
    @Column(name = "nombre")
    private String nombre;

    @Size(max = 1024)
    @Column(name = "direccion", length = 1024)
    private String direccion;

    @Size(max = 1024)
    @Column(name = "direccion2", length = 1024)
    private String direccion2;

    @Size(max = 255)
    @Column(name = "poblacion", length = 255)
    private String poblacion;

    @Lob
    @Column(name = "provincia")
    private String provincia;

    @Size(max = 50)
    @Column(name = "cp", length = 50)
    private String cp;

    @Size(max = 50)
    @Column(name = "telefono", length = 50)
    private String telefono;

    @Lob
    @Column(name = "notas")
    private String notas;

}