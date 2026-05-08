package alicanteweb.erp.service.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ClienteDatosExternos {
    private String cif;
    private String nombre;
    private String direccion;
    private String codigoPostal;
    private String poblacion;
    private String provincia;
    private String telefono;
    private String email;
    private String estado;

    public boolean tieneDatosUtiles() {
        return tieneTexto(cif) || tieneTexto(nombre) || tieneTexto(direccion)
            || tieneTexto(poblacion) || tieneTexto(provincia);
    }

    private boolean tieneTexto(String valor) {
        return valor != null && !valor.trim().isEmpty();
    }
}
