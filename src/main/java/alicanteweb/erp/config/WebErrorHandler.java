package alicanteweb.erp.config;

import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.resource.NoResourceFoundException;

@ControllerAdvice("alicanteweb.erp.controller.web")
public class WebErrorHandler {

    private static final Logger log = LoggerFactory.getLogger(WebErrorHandler.class);

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public String handleError(HttpServletRequest req, Exception e, Model model) {
        log.error("Error en {}: {}", req.getRequestURI(), e.getMessage(), e);
        model.addAttribute("titulo", "Error");
        model.addAttribute("codigo", 500);
        model.addAttribute("mensaje", e.getMessage() != null ? e.getMessage() : "Error interno del servidor");
        model.addAttribute("url", req.getRequestURI());
        return "error";
    }

    @ExceptionHandler(NoResourceFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String notFound(HttpServletRequest req, Model model) {
        model.addAttribute("titulo", "No encontrado");
        model.addAttribute("codigo", 404);
        model.addAttribute("mensaje", "La página solicitada no existe");
        model.addAttribute("url", req.getRequestURI());
        return "error";
    }
}
