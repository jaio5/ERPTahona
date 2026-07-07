package alicanteweb.erp.controller.web;

import java.util.Arrays;
import java.util.List;

public final class BreadcrumbBuilder {

    public record Crumb(String label, String url) {}

    private BreadcrumbBuilder() {}

    public static List<Crumb> of(Crumb... crumbs) {
        return Arrays.asList(crumbs);
    }

    public static Crumb link(String label, String url) {
        return new Crumb(label, url);
    }

    /** Migaja raíz "Inicio" que apunta al panel; presente en casi todas las vistas. */
    public static Crumb inicio() {
        return link("Inicio", "/web/dashboard");
    }

    public static Crumb active(String label) {
        return new Crumb(label, null);
    }
}
