package alicanteweb.erp.controller.web;

import jakarta.servlet.http.HttpSession;
import org.springframework.ui.Model;
import java.util.List;
import java.util.function.*;

/**
 * Helper para controladores CRUD web. Reduce el boilerplate de la lista + formulario.
 * Cada módulo extiende sus propios endpoints, usando estos helpers para la lógica repetitiva.
 */
public final class CrudHelper {

    private CrudHelper() {}

    public interface CrudService<T> {
        List<T> findAll();
        T findById(Long id);
        T save(T entity);
        void delete(Long id);
    }

    /**
     * Prepara el modelo para una lista simple con búsqueda por texto.
     * @return true si hay que redirigir a login (sesión inválida)
     */
    public static <T> boolean prepararLista(HttpSession session, Model model,
                                              String modulo, String titulo,
                                              Supplier<List<T>> loader,
                                              Function<String, List<T>> filtrador,
                                              String q) {
        if (WebController.requireLogin(session)) return true;
        List<T> items = filtrador.apply(q != null && !q.isBlank() ? q : "");
        model.addAttribute("moduloActivo", modulo);
        model.addAttribute("titulo", titulo);
        model.addAttribute("items", items);
        model.addAttribute("q", q);
        return false;
    }

    /**
     * Búsqueda genérica por texto.
     */
    public static <T> Function<String, List<T>> filtroPorTexto(Supplier<List<T>> loader,
                                                                 BiFunction<T, String, Boolean> matcher) {
        return q -> {
            List<T> all = loader.get();
            if (q == null || q.isBlank()) return all;
            String t = q.toLowerCase();
            return all.stream().filter(item -> matcher.apply(item, t)).toList();
        };
    }
}
