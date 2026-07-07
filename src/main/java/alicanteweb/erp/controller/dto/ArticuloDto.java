package alicanteweb.erp.controller.dto;

import alicanteweb.erp.entities.Articulo;

import java.math.BigDecimal;

public record ArticuloDto(Long id,
                          String codigo,
                          String nombre,
                          String descripcion,
                          String categoria,
                          String familia,
                          String unidad,
                          BigDecimal iva,
                          BigDecimal pvp,
                          BigDecimal coste,
                          BigDecimal stock,
                          BigDecimal stockMinimo,
                          Boolean activo) {

    public static ArticuloDto from(Articulo articulo) {
        return new ArticuloDto(
                articulo.getId(),
                articulo.getCodigo(),
                articulo.getNombre(),
                articulo.getDescripcion(),
                articulo.getCategoria(),
                articulo.getFamilia(),
                articulo.getUnidad(),
                articulo.getIva(),
                articulo.getPvp(),
                articulo.getCoste(),
                articulo.getStock(),
                articulo.getStockMinimo(),
                articulo.getActivo()
        );
    }

    public Articulo toEntity(Articulo articulo) {
        applyTo(articulo);
        return articulo;
    }

    public void applyTo(Articulo articulo) {
        articulo.setCodigo(codigo);
        articulo.setNombre(nombre);
        articulo.setDescripcion(descripcion);
        articulo.setCategoria(categoria);
        articulo.setFamilia(familia);
        articulo.setUnidad(unidad);
        articulo.setIva(iva);
        articulo.setPvp(pvp);
        articulo.setCoste(coste);
        articulo.setStock(stock);
        articulo.setStockMinimo(stockMinimo);
        articulo.setActivo(activo == null ? Boolean.TRUE : activo);
    }
}
