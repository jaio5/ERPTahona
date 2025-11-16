package alicanteweb.erp.service;

import alicanteweb.erp.repository.ClienteRepository;
import alicanteweb.erp.entities.Cliente;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ClienteService {

    private final ClienteRepository clienteRepository;

    public ClienteService(ClienteRepository clienteRepository) {
        this.clienteRepository = clienteRepository;
    }

    public List<Cliente> findTop(int limit) {
        return clienteRepository.findAll(
                PageRequest.of(0, limit, Sort.by("codigo"))
        ).getContent();
    }

    public Cliente findById(String codigo) {
        return clienteRepository.findById(codigo).orElse(null);
    }

    public List<Cliente> findAll() {
        return clienteRepository.findAll();
    }
}

