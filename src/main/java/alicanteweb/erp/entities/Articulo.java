package alicanteweb.erp.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Table(name = "articulos")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Articulo {

    @Id
    @Column(name = "CodigoArticulo", length = 500)
    private String codigoArticulo;

    @Column(name = "DescripcionCorta", length = 500)
    private String descripcionCorta;

    @Column(name = "DescripcionArticulo", length = 500)
    private String descripcionArticulo;

    @Column(name = "Unidad", length = 500)
    private String unidad;

    @Column(name = "FamiliaArticulo", length = 500)
    private String familiaArticulo;

    @Column(name = "Tipo_de_IVA")
    private Integer tipoDeIva;

    @Column(name = "SubCuentaVentas")
    private Integer subCuentaVentas;

    @Column(name = "SubCuentaCompras")
    private Integer subCuentaCompras;

    @Column(name = "UsoInterno", length = 500)
    private String usoInterno;

    @Column(name = "Compuesto", length = 500)
    private String compuesto;

    @Column(name = "Servicio", length = 500)
    private String servicio;

    @Column(name = "CosteArticulo", precision = 15, scale = 2)
    private BigDecimal costeArticulo;

    @Column(name = "CosteMedio", precision = 15, scale = 2)
    private BigDecimal costeMedio;

    @Column(name = "UltimoCoste", precision = 15, scale = 2)
    private BigDecimal ultimoCoste;

    @Column(name = "PVP1", precision = 15, scale = 2)
    private BigDecimal pvp1;

    @Column(name = "PVP2", precision = 15, scale = 2)
    private BigDecimal pvp2;

    @Column(name = "PVP3", precision = 15, scale = 2)
    private BigDecimal pvp3;

    @Column(name = "PVP4", precision = 15, scale = 2)
    private BigDecimal pvp4;

    @Column(name = "PVP5", precision = 15, scale = 2)
    private BigDecimal pvp5;

    @Column(name = "MinimoStock")
    private Integer minimoStock;

    @Column(name = "MaximoStock")
    private Integer maximoStock;

    @Column(name = "MinStockPorAlmacen")
    private Integer minStockPorAlmacen;

    @Column(name = "MaxStockPorAlmacen")
    private Integer maxStockPorAlmacen;

    @Column(name = "Descuento1", precision = 15, scale = 2)
    private BigDecimal descuento1;

    @Column(name = "Descuento2", precision = 15, scale = 2)
    private BigDecimal descuento2;

    @Column(name = "Descuento3", precision = 15, scale = 2)
    private BigDecimal descuento3;

    @Column(name = "Descuento4", precision = 15, scale = 2)
    private BigDecimal descuento4;

    @Column(name = "Descuento5", precision = 15, scale = 2)
    private BigDecimal descuento5;

    @Column(name = "ProveedorDefecto", length = 500)
    private String proveedorDefecto;

    @Column(name = "DiasFabricacion", precision = 15, scale = 2)
    private BigDecimal diasFabricacion;

    @Column(name = "CodigoTablaArticulo", length = 500)
    private String codigoTablaArticulo;

    @Column(name = "ValorFila", length = 500)
    private String valorFila;

    @Column(name = "ValorColumna", length = 500)
    private String valorColumna;
}

