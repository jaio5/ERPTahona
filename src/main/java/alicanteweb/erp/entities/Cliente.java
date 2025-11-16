package alicanteweb.erp.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Table(name = "clientes")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Cliente {

    @Id
    @Column(name = "Codigo", length = 500)
    private String codigo;

    @Column(name = "CodigoDeReferencia", length = 500)
    private String codigoDeReferencia;

    @Column(name = "Nombre", length = 500)
    private String nombre;

    @Column(name = "Domicilio", length = 500)
    private String domicilio;

    @Column(name = "Domicilio2", length = 500)
    private String domicilio2;

    @Column(name = "Poblacion", length = 500)
    private String poblacion;

    @Column(name = "CodigoPostal", length = 500)
    private String codigoPostal;

    @Column(name = "Provincia", length = 500)
    private String provincia;

    @Column(name = "PersonaContacto", length = 500)
    private String personaContacto;

    @Column(name = "Telefono", length = 500)
    private String telefono;

    @Column(name = "Fax", length = 500)
    private String fax;

    @Column(name = "CIF", length = 500)
    private String cif;

    @Column(name = "IVASN", length = 500)
    private String ivasn;

    @Column(name = "REQSN", length = 500)
    private String reqsn;

    @Column(name = "SubcuentaCliente")
    private Integer subcuentaCliente;

    @Column(name = "SubcuentaProveedor")
    private Integer subcuentaProveedor;

    @Column(name = "TipoDescuento")
    private Integer tipoDescuento;

    @Column(name = "Tarifa")
    private Integer tarifa;

    @Column(name = "SectoresActividad", length = 500)
    private String sectoresActividad;

    @Column(name = "FormaPago", length = 500)
    private String formaPago;

    @Column(name = "Agente", length = 500)
    private String agente;

    @Column(name = "Riesgo", precision = 15, scale = 2)
    private BigDecimal riesgo;

    @Column(name = "Banco", length = 500)
    private String banco;

    @Column(name = "DireccionBanco", length = 500)
    private String direccionBanco;

    @Column(name = "PoblacionBanco", length = 500)
    private String poblacionBanco;

    @Column(name = "ProvinciaBanco", length = 500)
    private String provinciaBanco;

    @Column(name = "AgenciaBanco", length = 500)
    private String agenciaBanco;

    @Column(name = "CuentaBanco", length = 500)
    private String cuentaBanco;

    @Column(name = "Notas", length = 500)
    private String notas;
}

