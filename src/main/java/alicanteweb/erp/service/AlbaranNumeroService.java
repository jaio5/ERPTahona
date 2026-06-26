package alicanteweb.erp.service;

import alicanteweb.erp.repository.AlbaranSerieSequenceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class AlbaranNumeroService {
    private static final String SERIE = "ALB";

    private final AlbaranSerieSequenceRepository sequenceRepository;

    public String generarNumero() {
        return generarNumero(LocalDate.now());
    }

    @Transactional
    public String generarNumero(LocalDate fecha) {
        int ejercicio = fecha != null ? fecha.getYear() : LocalDate.now().getYear();
        int filasAfectadas = sequenceRepository.reservarSiguiente(SERIE, ejercicio);
        long numero = filasAfectadas == 1 ? 1L : sequenceRepository.obtenerNumeroReservado();
        return String.format("%s-%d-%06d", SERIE, ejercicio, numero);
    }
}
