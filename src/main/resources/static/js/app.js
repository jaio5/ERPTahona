/* ── Utilidades globales de formato ── */
function formatMoneyEs(value) {
    if (value === null || value === undefined) return '0,00 €';
    return Number(value).toLocaleString('es-ES', { minimumFractionDigits: 2, maximumFractionDigits: 2 }) + ' €';
}

installCsrfFetchProtection();

document.addEventListener("DOMContentLoaded", () => {
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

/* ── Sidebar toggle (mobile) ── */
function initSidebarToggle() {
    const sidebar = document.getElementById("mainSidebar");
    const toggle = document.getElementById("sidebarToggle");
    const overlay = document.getElementById("sidebarOverlay");
    if (!sidebar || !toggle) return;

    toggle.addEventListener("click", () => {
        sidebar.classList.toggle("is-open");
    });
    if (overlay) {
        overlay.addEventListener("click", () => {
            sidebar.classList.remove("is-open");
        });
    }
}

/* ── Toast auto-dismiss ── */
function initToasts() {
    document.querySelectorAll("#toast-container .toast").forEach(el => {
        const toast = new bootstrap.Toast(el, { delay: 4500, autohide: true });
        toast.show();
    });
}

/* ── Form protection (loading state + anti-double-click) ── */
function initFormProtection() {
    document.querySelectorAll("form").forEach(form => {
        form.addEventListener("submit", function() {
            const btn = form.querySelector('[type="submit"]');
                if (btn && !btn.dataset.noDoubleClick) {
                // Prevent double-click
                if (btn.dataset.submitting === "true") {
                    return;
                }
                btn.dataset.submitting = "true";
                const originalText = btn.innerHTML;
                btn.innerHTML = '<span class="spinner-border spinner-border-sm me-1" role="status"></span>Guardando...';
                btn.disabled = true;
                // Re-enable after 10s as safety net
                setTimeout(() => {
                    btn.disabled = false;
                    btn.innerHTML = originalText;
                    delete btn.dataset.submitting;
                }, 10000);
            }
        });
    });
}

/* ── Table search with debounce ── */
function initTableSearch() {
    document.querySelectorAll("main table").forEach((table, index) => {
        if (table.classList.contains("lines-table")) return;
        if (table.dataset.searchReady === "true") return;
        table.dataset.searchReady = "true";

        if (hasSearchBefore(table)) {
            bindExistingSearch(table);
            return;
        }

        const search = buildSearch(index);
        insertSearchBeforeTable(table, search.wrapper);
        search.input.dataset.liveSearchBound = "true";
        let debounceTimer;
        search.input.addEventListener("input", () => {
            clearTimeout(debounceTimer);
            debounceTimer = setTimeout(() => {
                document.querySelectorAll("main table").forEach(t => filterTable(t, search.input.value));
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
                document.querySelectorAll("main table").forEach(t => filterTable(t, input.value));
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
    input.autocomplete = "off";
    searchWrap.appendChild(icon);
    searchWrap.appendChild(input);
    wrapper.appendChild(searchWrap);
    return { wrapper, input };
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
