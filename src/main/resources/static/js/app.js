document.addEventListener("DOMContentLoaded", () => {
    document.querySelectorAll("main table").forEach((table, index) => {
        if (table.dataset.searchReady === "true") {
            return;
        }
        table.dataset.searchReady = "true";

        if (hasSearchBefore(table)) {
            bindExistingSearch(table);
            return;
        }

        const search = buildSearch(index);
        insertSearchBeforeTable(table, search.wrapper);
        search.input.dataset.liveSearchBound = "true";
        search.input.addEventListener("input", () => {
            document.querySelectorAll("main table").forEach(candidate => filterTable(candidate, search.input.value));
        });
    });
});

function hasSearchBefore(table) {
    const container = document.querySelector("main");
    if (!container) {
        return false;
    }
    const inputs = Array.from(container.querySelectorAll('input[type="search"], input[name="q"]'));
    return inputs.some(input => input.compareDocumentPosition(table) & Node.DOCUMENT_POSITION_FOLLOWING);
}

function bindExistingSearch(table) {
    const container = document.querySelector("main");
    if (!container) {
        return;
    }
    const input = Array.from(container.querySelectorAll('input[type="search"], input[name="q"]'))
        .find(candidate => candidate.compareDocumentPosition(table) & Node.DOCUMENT_POSITION_FOLLOWING);
    if (input && !input.dataset.liveSearchBound) {
        input.dataset.liveSearchBound = "true";
        input.addEventListener("input", () => {
            document.querySelectorAll("main table").forEach(candidate => filterTable(candidate, input.value));
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
    wrapper.className = "row g-2 mb-3 list-search";

    const column = document.createElement("div");
    column.className = "col-md-5";

    const input = document.createElement("input");
    input.type = "search";
    input.name = `tableSearch${index}`;
    input.className = "form-control form-control-sm";
    input.placeholder = "Buscar...";
    input.autocomplete = "off";

    column.appendChild(input);
    wrapper.appendChild(column);
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
        .replace(/[\u0300-\u036f]/g, "")
        .trim();
}
