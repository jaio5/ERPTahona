package alicanteweb.erp.entities;

import jakarta.persistence.*;

import java.util.LinkedHashSet;
import java.util.Set;

@Entity
@Table(name = "clientes")
public class Cliente {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "codigo", nullable = false, length = 50)
    private String codigo;

    @Column(name = "nombre")
    private String nombre;

    @Column(name = "cif", length = 50)
    private String cif;

    @Column(name = "direccion")
    private String direccion;

    @Column(name = "poblacion", length = 100)
    private String poblacion;

    @Column(name = "codigo_postal", length = 20)
    private String codigoPostal;

    @Column(name = "provincia", length = 50)
    private String provincia;

    @Lob
    @Column(name = "notas")
    private String notas;

    @OneToMany(mappedBy = "cliente")
    private Set<AlbaranesVenta> albaranesVentas = new LinkedHashSet<>();

    @OneToMany(mappedBy = "cliente")
    private Set<DireccionesenvioNew> direccionesenvioNews = new LinkedHashSet<>();

    @OneToMany(mappedBy = "cliente")
    private Set<Factura> facturas = new LinkedHashSet<>();

    @OneToMany(mappedBy = "cliente")
    private Set<Pedido> pedidos = new LinkedHashSet<>();

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

    public String getCif() {
        return cif;
    }

    public void setCif(String cif) {
        this.cif = cif;
    }

    public String getDireccion() {
        return direccion;
    }

    public void setDireccion(String direccion) {
        this.direccion = direccion;
    }

    public String getPoblacion() {
        return poblacion;
    }

    public void setPoblacion(String poblacion) {
        this.poblacion = poblacion;
    }

    public String getCodigoPostal() {
        return codigoPostal;
    }

    public void setCodigoPostal(String codigoPostal) {
        this.codigoPostal = codigoPostal;
    }

    public String getProvincia() {
        return provincia;
    }

    public void setProvincia(String provincia) {
        this.provincia = provincia;
    }

    public String getNotas() {
        return notas;
    }

    public void setNotas(String notas) {
        this.notas = notas;
    }

    public Set<AlbaranesVenta> getAlbaranesVentas() {
        return albaranesVentas;
    }

    public void setAlbaranesVentas(Set<AlbaranesVenta> albaranesVentas) {
        this.albaranesVentas = albaranesVentas;
    }

    public Set<DireccionesenvioNew> getDireccionesenvioNews() {
        return direccionesenvioNews;
    }

    public void setDireccionesenvioNews(Set<DireccionesenvioNew> direccionesenvioNews) {
        this.direccionesenvioNews = direccionesenvioNews;
    }

    public Set<Factura> getFacturas() {
        return facturas;
    }

    public void setFacturas(Set<Factura> facturas) {
        this.facturas = facturas;
    }

    public Set<Pedido> getPedidos() {
        return pedidos;
    }

    public void setPedidos(Set<Pedido> pedidos) {
        this.pedidos = pedidos;
    }

    @Override
    public String toString() {
        // Mostrar el nombre si existe, si no el código, para mostrar en ComboBox
        if (this.nombre != null && !this.nombre.isBlank()) return this.nombre;
        if (this.codigo != null) return this.codigo;
        return "Cliente #" + (this.id != null ? this.id : "?");
    }

}