package alicanteweb.erp.controller.dto;

import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PageResponseTest {
    @Test
    void conservaMetadatosYMapeaContenido() {
        var page = new PageImpl<>(List.of(2, 3), PageRequest.of(1, 2), 5);
        var response = PageResponse.from(page, value -> "n" + value);

        assertEquals(List.of("n2", "n3"), response.items());
        assertEquals(5, response.total());
        assertEquals(3, response.pages());
        assertEquals(1, response.page());
        assertEquals(2, response.size());
    }
}
