package alicanteweb.erp.service;

import alicanteweb.erp.repository.ClienteRepository;
import org.springframework.stereotype.Service;

@Service
public class ClienteService {

    ClienteRepository clienteRepository;
    public long countClientes(){
        return 32;
    }

}
