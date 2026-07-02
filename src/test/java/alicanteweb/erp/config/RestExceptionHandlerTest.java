package alicanteweb.erp.config;

import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class RestExceptionHandlerTest {

    private final MockMvc mockMvc = MockMvcBuilders
            .standaloneSetup(new FailingController())
            .setControllerAdvice(new RestExceptionHandler())
            .build();

    @Test
    void devuelveErrorJsonConsistente() throws Exception {
        mockMvc.perform(get("/api/test/error"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("BAD_REQUEST"))
                .andExpect(jsonPath("$.message").value("dato inválido"))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.path").value("/api/test/error"));
    }

    @RestController
    static class FailingController {
        @GetMapping("/api/test/error")
        void fail() {
            throw new IllegalArgumentException("dato inválido");
        }
    }
}
