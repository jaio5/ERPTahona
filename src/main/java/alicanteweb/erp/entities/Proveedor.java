package alicanteweb.erp.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.util.LinkedHashSet;
import java.util.Set;

@Getter
@Setter
@Entity
@Table(name = "proveedores")
public class Proveedor {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    @Size(max = 255)
    @Column(name = "nombre")
    private String nombre;

    @Size(max = 20)
    @Column(name = "cif", length = 20)
    private String cif;

    @Size(max = 50)
    @Column(name = "telefono", length = 50)
    private String telefono;

    @Size(max = 255)
    @Column(name = "email")
    private String email;

    @Size(max = 255)
    @Column(name = "direccion")
    private String direccion;

    @Size(max = 100)
    @Column(name = "ciudad", length = 100)
    private String ciudad;

    @Size(max = 100)
    @Column(name = "provincia", length = 100)
    private String provincia;

    @Size(max = 20)
    @Column(name = "cp", length = 20)
    private String cp;

    @Size(max = 100)
    @Column(name = "pais", length = 100)
    private String pais;

    @OneToMany(mappedBy = "idProveedor")
    private Set<PedidoCompra> pedidosCompras = new LinkedHashSet<>();

}