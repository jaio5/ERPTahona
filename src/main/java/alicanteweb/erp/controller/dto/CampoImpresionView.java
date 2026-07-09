package alicanteweb.erp.controller.dto;

import lombok.Getter;

/**
 * Vista ya resuelta de un campo de impresión para las plantillas PDF: etiqueta y valor definitivos
 * (el valor de los campos de sistema ya viene calculado) y la zona donde pintarlo.
 */
@Getter
public class CampoImpresionView {

    private final String etiqueta;
    private final String valor;
    private final String ubicacion;

    public CampoImpresionView(String etiqueta, String valor, String ubicacion) {
        this.etiqueta = etiqueta;
        this.valor = valor;
        this.ubicacion = ubicacion;
    }
}
