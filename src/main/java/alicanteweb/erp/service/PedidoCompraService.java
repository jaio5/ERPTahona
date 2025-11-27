package alicanteweb.erp.service;

import alicanteweb.erp.entities.PedidoCompra;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
public class PedidoCompraService {

    // Simula la capa de repositorio. En una app real, esto sería una interfaz que extiende de JpaRepository.

    public List<PedidoCompra> findAll() {
        // Devuelve una lista vacía para que la aplicación pueda arrancar.
        // En el futuro, esto sería: return repository.findAll();
        return Collections.emptyList();
    }

    public List<PedidoCompra> buscarPorFiltro(String filtro) {
        // Simula la búsqueda por filtro.
        // En el futuro, esto podría ser: return repository.findByNumeroContainingOrProveedorContaining(filtro, filtro);
        return Collections.emptyList();
    }

    public PedidoCompra save(PedidoCompra pedido) {
        // Simula el guardado.
        System.out.println("Guardando pedido: " + pedido.getNumero());
        return pedido;
    }

    public void delete(PedidoCompra pedido) {
        // Simula el borrado.
        System.out.println("Borrando pedido: " + pedido.getNumero());
    }
}
