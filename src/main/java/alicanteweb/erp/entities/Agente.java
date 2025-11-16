package alicanteweb.erp.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Table(name = "agentes")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Agente {

    @Id
    @Column(name = "CodigoAgente", length = 500)
    private String codigoAgente;

    @Column(name = "NombreAgente", length = 500)
    private String nombreAgente;

    @Column(name = "Notas", length = 500)
    private String notas;

    @Column(name = "_Comision", precision = 15, scale = 2)
    private BigDecimal comision;

    @Column(name = "Domicilio", length = 500)
    private String domicilio;

    @Column(name = "Poblacion", length = 500)
    private String poblacion;

    @Column(name = "CodigoPostal", length = 500)
    private String codigoPostal;

    @Column(name = "Provincia", length = 500)
    private String provincia;

    @Column(name = "CIF", length = 500)
    private String cif;
}

