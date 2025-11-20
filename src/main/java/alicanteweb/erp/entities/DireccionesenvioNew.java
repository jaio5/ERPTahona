package alicanteweb.erp.entities;

import jakarta.persistence.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Entity
@Table(name = "direccionesenvio_new")
public class DireccionesenvioNew {
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

    @Lob
    @Column(name = "direccion")
    private String direccion;

    @Lob
    @Column(name = "direccion2")
    private String direccion2;

    @Lob
    @Column(name = "poblacion")
    private String poblacion;

    @Lob
    @Column(name = "provincia")
    private String provincia;

    @Lob
    @Column(name = "cp")
    private String cp;

    @Lob
    @Column(name = "telefono")
    private String telefono;

    @Lob
    @Column(name = "notas")
    private String notas;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Cliente getCliente() {
        return cliente;
    }

    public void setCliente(Cliente cliente) {
        this.cliente = cliente;
    }

    public Integer getCodigoDireccion() {
        return codigoDireccion;
    }

    public void setCodigoDireccion(Integer codigoDireccion) {
        this.codigoDireccion = codigoDireccion;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getDireccion() {
        return direccion;
    }

    public void setDireccion(String direccion) {
        this.direccion = direccion;
    }

    public String getDireccion2() {
        return direccion2;
    }

    public void setDireccion2(String direccion2) {
        this.direccion2 = direccion2;
    }

    public String getPoblacion() {
        return poblacion;
    }

    public void setPoblacion(String poblacion) {
        this.poblacion = poblacion;
    }

    public String getProvincia() {
        return provincia;
    }

    public void setProvincia(String provincia) {
        this.provincia = provincia;
    }

    public String getCp() {
        return cp;
    }

    public void setCp(String cp) {
        this.cp = cp;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public String getNotas() {
        return notas;
    }

    public void setNotas(String notas) {
        this.notas = notas;
    }

}