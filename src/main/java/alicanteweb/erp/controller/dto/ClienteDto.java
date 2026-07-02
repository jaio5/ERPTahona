package alicanteweb.erp.controller.dto;

import alicanteweb.erp.entities.Cliente;

import java.util.List;

public record ClienteDto(Long id,
                         String codigo,
                         String nombre,
                         String cif,
                         String telefono,
                         String email,
                         String direccion,
                         String poblacion,
                         String codigoPostal,
                         String provincia,
                         Boolean activo) {

    public static ClienteDto from(Cliente cliente) {
        return new ClienteDto(
                cliente.getId(),
                cliente.getCodigo(),
                cliente.getNombre(),
                cliente.getCif(),
                cliente.getTelefono(),
                cliente.getEmail(),
                cliente.getDireccion(),
                cliente.getPoblacion(),
                cliente.getCodigoPostal(),
                cliente.getProvincia(),
                cliente.getActivo()
        );
    }

    public Cliente toEntity(Cliente cliente) {
        applyTo(cliente);
        return cliente;
    }

    public void applyTo(Cliente cliente) {
        cliente.setCodigo(codigo);
        cliente.setNombre(nombre);
        cliente.setCif(cif);
        cliente.setTelefono(telefono);
        cliente.setEmail(email);
        cliente.setDireccion(direccion);
        cliente.setPoblacion(poblacion);
        cliente.setCodigoPostal(codigoPostal);
        cliente.setProvincia(provincia);
        cliente.setActivo(activo == null ? Boolean.TRUE : activo);
    }
}
