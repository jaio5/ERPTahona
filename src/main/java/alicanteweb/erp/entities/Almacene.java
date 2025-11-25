package alicanteweb.erp.entities;

import jakarta.persistence.*;

import java.util.LinkedHashSet;
import java.util.Set;

@Entity
@Table(name = "almacenes")
public class Almacene {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "codigo", nullable = false, length = 50)
    private String codigo;

    @Column(name = "nombre")
    private String nombre;

    @OneToMany(mappedBy = "almacen")
    private Set<AlbaranesVenta> albaranesVentas = new LinkedHashSet<>();

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public Set<AlbaranesVenta> getAlbaranesVentas() {
        return albaranesVentas;
    }

    public void setAlbaranesVentas(Set<AlbaranesVenta> albaranesVentas) {
        this.albaranesVentas = albaranesVentas;
    }

    @Override
    public String toString() {
        if (this.nombre != null && !this.nombre.isBlank()) return this.nombre;
        if (this.codigo != null) return this.codigo;
        return "Almacén #" + (this.id != null ? this.id : "?");
    }

}