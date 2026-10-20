/*
 * Tema claro/oscuro. Se carga en <head> (sin defer) para fijar data-bs-theme
 * antes del primer pintado y evitar el destello de tema equivocado.
 * Preferencia guardada: localStorage "erp_tema" = "light" | "dark".
 * Sin preferencia guardada se sigue al sistema (prefers-color-scheme).
 */
(function () {
    var CLAVE = "erp_tema";
    var raiz = document.documentElement;

    function leerPreferencia() {
        try {
            var v = window.localStorage.getItem(CLAVE);
            return v === "light" || v === "dark" ? v : null;
        } catch (e) {
            return null;
        }
    }

    function temaSistema() {
        return window.matchMedia && window.matchMedia("(prefers-color-scheme: dark)").matches ? "dark" : "light";
    }

    function aplicar(tema) {
        raiz.setAttribute("data-bs-theme", tema);
    }

    aplicar(leerPreferencia() || temaSistema());

    // Si el usuario no ha elegido, el tema acompaña al del sistema en vivo
    if (window.matchMedia) {
        var mq = window.matchMedia("(prefers-color-scheme: dark)");
        var alCambiar = function () {
            if (!leerPreferencia()) {
                aplicar(temaSistema());
                document.dispatchEvent(new CustomEvent("erp:tema", { detail: { tema: temaSistema() } }));
            }
        };
        if (mq.addEventListener) mq.addEventListener("change", alCambiar);
        else if (mq.addListener) mq.addListener(alCambiar);
    }

    window.erpTema = {
        CLAVE: CLAVE,
        actual: function () { return raiz.getAttribute("data-bs-theme") === "dark" ? "dark" : "light"; },
        aplicar: aplicar,
        guardar: function (tema) {
            try { window.localStorage.setItem(CLAVE, tema); } catch (e) { /* sin almacenamiento: solo esta sesión */ }
        }
    };
})();
