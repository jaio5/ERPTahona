/* ── Utilidades globales de formato ── */
function formatMoneyEs(value) {
    if (value === null || value === undefined) return '0,00 €';
    return Number(value).toLocaleString('es-ES', { minimumFractionDigits: 2, maximumFractionDigits: 2 }) + ' €';
}

installCsrfFetchProtection();

document.addEventListener("DOMContentLoaded", () => {
    initThemeToggle();
    initHistoryBack();
    initSidebarToggle();
    initToasts();
    initFormProtection();
    initTableSearch();
    initTomSelect();
    initBootstrapValidation();
    initDeclarativeActions();
});

function initDeclarativeActions() {
    document.querySelectorAll("[data-confirm]").forEach(element => {
        element.addEventListener("click", event => {
            if (!window.confirm(element.dataset.confirm)) event.preventDefault();
        });
    });
    document.querySelectorAll("[data-navigate]").forEach(element => {
        element.addEventListener("click", () => {
            window.location.assign(element.dataset.navigate);
        });
    });
    // Selects de filtro que envían su formulario al cambiar (sustituye a onchange inline, bloqueado por CSP)
    document.querySelectorAll("select[data-autosubmit]").forEach(element => {
        element.addEventListener("change", () => {
            element.closest("form")?.submit();
        });
    });
}

/*
 * Conserva CSRF habilitado también para los fetch() definidos dentro de
 * plantillas Thymeleaf. Solo modifica peticiones mutables al mismo origen.
 */
function installCsrfFetchProtection() {
    const token = document.querySelector('meta[name="csrf-token"]')?.content;
    const headerName = document.querySelector('meta[name="csrf-header"]')?.content || "X-CSRF-TOKEN";
    if (!token || window.fetch.csrfProtected) return;

    const nativeFetch = window.fetch.bind(window);
    const protectedFetch = (input, init = {}) => {
        const method = String(init.method || (input instanceof Request ? input.method : "GET")).toUpperCase();
        const url = new URL(input instanceof Request ? input.url : String(input), window.location.href);
        if (url.origin === window.location.origin && ["POST", "PUT", "PATCH", "DELETE"].includes(method)) {
            const headers = new Headers(input instanceof Request ? input.headers : undefined);
            new Headers(init.headers || {}).forEach((value, name) => headers.set(name, value));
            headers.set(headerName, token);
            init = {...init, headers};
        }
        return nativeFetch(input, init);
    };
    protectedFetch.csrfProtected = true;
    window.fetch = protectedFetch;
}

/* ── Conmutador de tema claro/oscuro (botón #temaToggle de la barra superior) ──
 * El tema inicial lo fija /js/tema.js en <head>; aquí solo se alterna y se guarda.
 * Emite "erp:tema" para que las vistas con gráficos repinten sus colores.
 */
function initThemeToggle() {
    const boton = document.getElementById("temaToggle");
    if (!boton) return;

    const raiz = document.documentElement;
    const actual = () => (raiz.getAttribute("data-bs-theme") === "dark" ? "dark" : "light");
    const rotular = () => {
        const oscuro = actual() === "dark";
        const texto = oscuro ? "Cambiar a modo claro" : "Cambiar a modo oscuro";
        boton.setAttribute("aria-label", texto);
        boton.setAttribute("title", texto);
    };
    rotular();

    boton.addEventListener("click", () => {
        const nuevo = actual() === "dark" ? "light" : "dark";
        raiz.setAttribute("data-bs-theme", nuevo);
        if (window.erpTema) {
            window.erpTema.guardar(nuevo);
        } else {
            try { localStorage.setItem("erp_tema", nuevo); } catch (e) { /* sin almacenamiento */ }
        }
        rotular();
        document.dispatchEvent(new CustomEvent("erp:tema", { detail: { tema: nuevo } }));
    });
    // Cambio del sistema sin preferencia guardada (lo aplica tema.js): actualizar el rótulo
    document.addEventListener("erp:tema", rotular);
}

/* ── Botones "Volver atrás" (sustituye a href="javascript:…", bloqueado por la CSP) ── */
function initHistoryBack() {
    document.querySelectorAll("[data-history-back]").forEach(element => {
        element.addEventListener("click", event => {
            if (window.history.length > 1) {
                event.preventDefault();
                window.history.back();
            }
        });
    });
}

/* ── Sidebar toggle (mobile) ── */
function initSidebarToggle() {
    const sidebar = document.getElementById("mainSidebar");
    const toggle = document.getElementById("sidebarToggle");
    const overlay = document.getElementById("sidebarOverlay");
    if (!sidebar || !toggle) return;

    const setOpen = open => {
        sidebar.classList.toggle("is-open", open);
        toggle.setAttribute("aria-expanded", String(open));
    };
    toggle.setAttribute("aria-expanded", String(sidebar.classList.contains("is-open")));

    toggle.addEventListener("click", () => {
        setOpen(!sidebar.classList.contains("is-open"));
    });
    if (overlay) {
        overlay.addEventListener("click", () => setOpen(false));
    }
    // Escape cierra el menú móvil y devuelve el foco al botón que lo abrió
    document.addEventListener("keydown", event => {
        if (event.key !== "Escape" || !sidebar.classList.contains("is-open")) return;
        setOpen(false);
        toggle.focus();
    });
}

/* ── Toasts: los de éxito se ocultan solos; los marcados data-bs-autohide="false" (errores) no ── */
function initToasts() {
    document.querySelectorAll("#toast-container .toast").forEach(el => {
        const toast = new bootstrap.Toast(el, { delay: 4500, autohide: el.dataset.bsAutohide !== "false" });
        toast.show();
    });
}

/* ── Form protection (loading state + anti-double-click) ──
 * Solo formularios POST: los filtros GET (filter-bar) no se bloquean.
 * El listener se registra antes que initBootstrapValidation, así que la validación
 * se comprueba aquí: un formulario inválido no llega a enviarse y el botón no debe
 * quedarse bloqueado. El bloqueo se aplica en el siguiente tick para respetar a
 * cualquier otro listener (Alpine, confirmaciones) que cancele el envío después.
 * El atributo data-no-double-click marca botones críticos; hoy todos los POST
 * llevan esta protección, así que es solo informativo.
 */
const textosOriginalesBoton = new WeakMap();

function initFormProtection() {
    document.querySelectorAll("form").forEach(form => {
        if ((form.getAttribute("method") || "get").toLowerCase() !== "post") return;
        form.addEventListener("submit", event => {
            if (event.defaultPrevented
                || (form.classList.contains("needs-validation") && !form.checkValidity())) {
                return;
            }
            // Segundo envío (p. ej. Intro en un campo) mientras el primero sigue en curso
            if (form.dataset.submitting === "true") {
                event.preventDefault();
                return;
            }
            // Los formularios que abren otra pestaña (PDF, impresión) no dejan la página esperando
            if ((form.getAttribute("target") || "").toLowerCase() === "_blank") return;

            const btn = event.submitter || form.querySelector('[type="submit"], button:not([type])');
            form.dataset.submitting = "true";
            setTimeout(() => {
                if (event.defaultPrevented) {
                    delete form.dataset.submitting;
                    return;
                }
                bloquearBotonEnvio(form, btn);
            }, 0);
        });
    });

    // Al volver con "Atrás" el navegador puede restaurar la página desde la caché (bfcache)
    // con los botones aún bloqueados: se rehabilitan con su texto original.
    window.addEventListener("pageshow", event => {
        if (!event.persisted) return;
        document.querySelectorAll('[data-submitting="true"]').forEach(el => {
            if (el.tagName === "FORM") delete el.dataset.submitting;
            else restaurarBotonEnvio(el);
        });
    });
}

function bloquearBotonEnvio(form, btn) {
    // Red de seguridad: si la respuesta no llega (descarga, error de red) se rehabilita a los 10 s
    setTimeout(() => {
        delete form.dataset.submitting;
        if (btn) restaurarBotonEnvio(btn);
    }, 10000);
    if (!btn || btn.dataset.submitting === "true") return;

    btn.dataset.submitting = "true";
    const texto = btn.dataset.loadingText || "Procesando…";
    if (btn.tagName === "INPUT") {
        textosOriginalesBoton.set(btn, btn.value);
        btn.value = texto;
    } else {
        textosOriginalesBoton.set(btn, btn.innerHTML);
        // Botones solo con icono (p. ej. cerrar sesión): spinner sin texto visible para no ensanchar la barra
        const soloIcono = btn.textContent.trim() === "";
        btn.innerHTML = '<span class="spinner-border spinner-border-sm' + (soloIcono ? '' : ' me-1')
            + '" aria-hidden="true"></span><span' + (soloIcono ? ' class="visually-hidden"' : '') + '></span>';
        btn.lastElementChild.textContent = texto;
    }
    btn.disabled = true;
}

function restaurarBotonEnvio(btn) {
    if (btn.dataset.submitting !== "true") return;
    const original = textosOriginalesBoton.get(btn);
    if (original !== undefined) {
        if (btn.tagName === "INPUT") btn.value = original;
        else btn.innerHTML = original;
        textosOriginalesBoton.delete(btn);
    }
    btn.disabled = false;
    delete btn.dataset.submitting;
}

/* ── Table search with debounce ── */

/**
 * Tablas de listado, las unicas a las que se les añade buscador y sobre las que filtra.
 * Se excluyen las de lineas de documento (.lines-table) y las fichas en formato tabla
 * (.doc-table): son pares etiqueta/valor, no filas homogeneas que tenga sentido filtrar.
 */
function searchableTables() {
    return document.querySelectorAll("main table:not(.lines-table):not(.doc-table)");
}

function initTableSearch() {
    searchableTables().forEach((table, index) => {
        if (table.dataset.searchReady === "true") return;
        table.dataset.searchReady = "true";

        if (hasSearchBefore(table)) {
            bindExistingSearch(table);
            return;
        }

        const search = buildSearch(index);
        // Si ya hay un filter-bar (p. ej. con un desplegable) sin buscador, integramos el
        // buscador en esa misma fila en vez de apilar un segundo filter-bar debajo.
        const existingBar = filterBarBefore(table);
        if (existingBar && !existingBar.querySelector(".filter-bar-search")) {
            existingBar.insertBefore(search.searchWrap, existingBar.firstChild);
        } else {
            insertSearchBeforeTable(table, search.wrapper);
        }
        search.input.dataset.liveSearchBound = "true";
        let debounceTimer;
        search.input.addEventListener("input", () => {
            clearTimeout(debounceTimer);
            debounceTimer = setTimeout(() => {
                searchableTables().forEach(t => filterTable(t, search.input.value));
            }, 280);
        });
    });
}

function hasSearchBefore(table) {
    const container = document.querySelector("main");
    if (!container) return false;
    const inputs = Array.from(container.querySelectorAll('input[type="search"], input[name="q"]'));
    return inputs.some(input => input.compareDocumentPosition(table) & Node.DOCUMENT_POSITION_FOLLOWING);
}

function bindExistingSearch(table) {
    const container = document.querySelector("main");
    if (!container) return;
    const input = Array.from(container.querySelectorAll('input[type="search"], input[name="q"]'))
        .find(candidate => candidate.compareDocumentPosition(table) & Node.DOCUMENT_POSITION_FOLLOWING);
    if (input && !input.dataset.liveSearchBound) {
        input.dataset.liveSearchBound = "true";
        let debounceTimer;
        input.addEventListener("input", () => {
            clearTimeout(debounceTimer);
            debounceTimer = setTimeout(() => {
                searchableTables().forEach(t => filterTable(t, input.value));
            }, 280);
        });
    }
}

function insertSearchBeforeTable(table, search) {
    const tableCard = table.closest(".table-card");
    if (tableCard && tableCard.parentElement) {
        tableCard.parentElement.insertBefore(search, tableCard);
        return;
    }
    table.parentElement?.insertBefore(search, table);
}

function buildSearch(index) {
    const wrapper = document.createElement("div");
    wrapper.className = "filter-bar mb-3 list-search";
    const searchWrap = document.createElement("div");
    searchWrap.className = "filter-bar-search";
    const icon = document.createElement("i");
    icon.className = "bi bi-search";
    const input = document.createElement("input");
    input.type = "search";
    input.name = `tableSearch${index}`;
    input.className = "form-control form-control-sm";
    input.placeholder = "Buscar...";
    input.setAttribute("aria-label", "Buscar en la tabla");
    input.autocomplete = "off";
    searchWrap.appendChild(icon);
    searchWrap.appendChild(input);
    wrapper.appendChild(searchWrap);
    return { wrapper, searchWrap, input };
}

/** Devuelve un .filter-bar existente inmediatamente antes de la tabla (saltando avisos), o null. */
function filterBarBefore(table) {
    const tableCard = table.closest(".table-card") || table;
    let el = tableCard.previousElementSibling;
    while (el) {
        if (el.classList && el.classList.contains("filter-bar")) return el;
        if (el.classList && (el.classList.contains("alert") || el.tagName === "SCRIPT")) {
            el = el.previousElementSibling;
            continue;
        }
        return null;
    }
    return null;
}

function filterTable(table, query) {
    const normalized = normalize(query);
    const rows = table.tBodies.length ? Array.from(table.tBodies[0].rows) : [];
    rows.forEach(row => {
        row.hidden = normalized !== "" && !normalize(row.textContent).includes(normalized);
    });
}

function normalize(value) {
    return String(value || "")
        .toLowerCase()
        .normalize("NFD")
        .replace(/[̀-ͯ]/g, "")
        .trim();
}

/* ── Tom Select: searchable dropdowns ── */
function initTomSelect() {
    if (typeof TomSelect === "undefined") return;
    document.querySelectorAll("select[data-searchable]").forEach(el => {
        if (el.tomselect) return;
        new TomSelect(el, {
            maxOptions: 500,
            searchField: ["text"],
            placeholder: el.getAttribute("data-placeholder") || undefined,
            render: {
                no_results: () => '<div class="no-results">Sin resultados</div>'
            }
        });
    });
}

/* ── Bootstrap form validation ── */
function initBootstrapValidation() {
    document.querySelectorAll("form.needs-validation").forEach(form => {
        form.addEventListener("submit", function(event) {
            if (!form.checkValidity()) {
                event.preventDefault();
                event.stopPropagation();
            }
            form.classList.add("was-validated");
        });
    });
}
