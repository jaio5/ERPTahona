package alicanteweb.erp.config;

import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.resource.NoResourceFoundException;

@ControllerAdvice("alicanteweb.erp.controller.web")
public class WebErrorHandler {

    private static final Logger log = LoggerFactory.getLogger(WebErrorHandler.class);

    @ExceptionHandler(NoResourceFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String notFound(HttpServletRequest req, Model model) {
        model.addAttribute("titulo", "No encontrado");
        model.addAttribute("codigo", 404);
        model.addAttribute("mensaje", "La página solicitada no existe");
        model.addAttribute("url", req.getRequestURI());
        return "error";
    }

    @ExceptionHandler(AccessDeniedException.class)
    public String accessDenied(HttpServletRequest req, Model model) {
        log.warn("Acceso denegado a {}", req.getRequestURI());
        return "redirect:/web/acceso-denegado";
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public String dataIntegrity(HttpServletRequest req, DataIntegrityViolationException e, Model model) {
        log.error("Violación de integridad en {}: {}", req.getRequestURI(), e.getMessage());
        model.addAttribute("titulo", "Conflicto de datos");
        model.addAttribute("codigo", 409);
        model.addAttribute("mensaje", "No se puede completar la operación: el registro está en uso o tiene datos duplicados");
        model.addAttribute("url", req.getRequestURI());
        return "error";
    }

    @ExceptionHandler({ObjectOptimisticLockingFailureException.class, OptimisticLockingFailureException.class})
    @ResponseStatus(HttpStatus.CONFLICT)
    public String optimisticLock(HttpServletRequest req, Exception e, Model model) {
        log.warn("Conflicto de edición concurrente en {}: {}", req.getRequestURI(), e.getMessage());
        model.addAttribute("titulo", "Conflicto de edición");
        model.addAttribute("codigo", 409);
        model.addAttribute("mensaje", "Otro usuario ha modificado este registro mientras lo editabas. Recarga la página e inténtalo de nuevo.");
        model.addAttribute("url", req.getRequestURI());
        return "error";
    }

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public String illegalArgument(HttpServletRequest req, IllegalArgumentException e, Model model) {
        log.warn("Argumento inválido en {}: {}", req.getRequestURI(), e.getMessage());
        model.addAttribute("titulo", "Solicitud inválida");
        model.addAttribute("codigo", 400);
        model.addAttribute("mensaje", "La solicitud contiene datos inválidos. Revisa los campos e inténtalo de nuevo.");
        model.addAttribute("url", req.getRequestURI());
        return "error";
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public String handleError(HttpServletRequest req, Exception e, Model model) {
        log.error("Error en {}: {}", req.getRequestURI(), e.getMessage(), e);
        model.addAttribute("titulo", "Error interno");
        model.addAttribute("codigo", 500);
        model.addAttribute("mensaje", "Ha ocurrido un error inesperado. Por favor, contacta con el administrador.");
        model.addAttribute("url", req.getRequestURI());
        return "error";
    }
}
