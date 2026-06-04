const state = {
    view: "dashboard",
    rows: [],
    editing: null,
    lookupOptions: {},
    child: null
};

const entity = id => `/api/web/entities/${id}`;
const childEntity = id => `/api/web/children/${id}`;

const modules = [
    { id: "dashboard", label: "Resumen", type: "dashboard" },
    { id: "clientes", label: "Clientes", type: "table", endpoint: "/api/web/clientes" },
    { id: "articulos", label: "Articulos", type: "table", endpoint: "/api/web/articulos" },
    { id: "proveedores", label: "Proveedores", type: "table", endpoint: entity("proveedores") },
    { id: "presupuestos", label: "Presupuestos", type: "table", endpoint: entity("presupuestos") },
    { id: "pedidos-venta", label: "Pedidos venta", type: "table", endpoint: entity("pedidos-venta") },
    { id: "albaranes", label: "Albaranes", type: "table", endpoint: entity("albaranes") },
    { id: "facturas", label: "Facturas", type: "table", endpoint: entity("facturas") },
    { id: "pedidos-compra", label: "Pedidos compra", type: "table", endpoint: entity("pedidos-compra") },
    { id: "facturas-compra", label: "Facturas compra", type: "table", endpoint: entity("facturas-compra") },
    { id: "almacenes", label: "Almacenes", type: "table", endpoint: entity("almacenes") },
    { id: "recetas", label: "Recetas", type: "table", endpoint: entity("recetas") },
    { id: "ordenes-produccion", label: "Ordenes produccion", type: "table", endpoint: entity("ordenes-produccion") },
    { id: "horneadas", label: "Horneadas", type: "table", endpoint: entity("horneadas") },
    { id: "lotes", label: "Lotes", type: "table", endpoint: entity("lotes") },
    { id: "appcc", label: "APPCC", type: "table", endpoint: entity("appcc") },
    { id: "vehiculos", label: "Vehiculos", type: "table", endpoint: entity("vehiculos") },
    { id: "rutas-reparto", label: "Rutas reparto", type: "table", endpoint: entity("rutas-reparto") },
    { id: "hojas-ruta", label: "Hojas ruta", type: "table", endpoint: entity("hojas-ruta") },
    { id: "devoluciones", label: "Devoluciones", type: "table", endpoint: entity("devoluciones") },
    { id: "direcciones-envio", label: "Direcciones envio", type: "table", endpoint: entity("direcciones-envio") },
    { id: "asientos", label: "Asientos", type: "table", endpoint: entity("asientos") },
    { id: "plan-contable", label: "Plan contable", type: "table", endpoint: entity("plan-contable") },
    { id: "movimientos-caja", label: "Caja", type: "table", endpoint: entity("movimientos-caja") },
    { id: "cajas", label: "Cajas diarias", type: "table", endpoint: entity("cajas") },
    { id: "caja-movimientos", label: "Movimientos caja diaria", type: "table", endpoint: entity("caja-movimientos") },
    { id: "movimientos-banco", label: "Banco", type: "table", endpoint: entity("movimientos-banco") },
    { id: "bancos", label: "Cuentas bancarias", type: "table", endpoint: entity("bancos") },
    { id: "modelo347", label: "Modelo 347", type: "table", endpoint: entity("modelo347") },
    { id: "auditoria", label: "Auditoria", type: "table", endpoint: entity("auditoria") },
    { id: "verifactu-evidencias", label: "VeriFactu", type: "table", endpoint: entity("verifactu-evidencias") },
    { id: "usuarios", label: "Usuarios", type: "table", endpoint: entity("usuarios") },
    { id: "empresa", label: "Empresa", type: "table", endpoint: entity("empresa") }
];

const lookups = {
    clienteId: { endpoint: "/api/web/clientes", label: row => `${row.codigo || row.id} - ${row.nombre || ""}` },
    proveedorId: { endpoint: entity("proveedores"), label: row => `${row.codigo || row.id} - ${row.nombre || ""}` },
    articuloId: { endpoint: "/api/web/articulos", label: row => `${row.codigo || row.id} - ${row.nombre || row.descripcion || ""}` },
    articuloResultanteId: { endpoint: "/api/web/articulos", label: row => `${row.codigo || row.id} - ${row.nombre || row.descripcion || ""}` },
    recetaId: { endpoint: entity("recetas"), label: row => `${row.codigo || row.id} - ${row.nombre || ""}` },
    ordenProduccionId: { endpoint: entity("ordenes-produccion"), label: row => `${row.numero || row.id} - ${row.estado || ""}` },
    almacenId: { endpoint: entity("almacenes"), label: row => `${row.codigo || row.id} - ${row.nombre || ""}` },
    vehiculoId: { endpoint: entity("vehiculos"), label: row => `${row.matricula || row.id} ${row.marca || ""} ${row.modelo || ""}`.trim() },
    rutaId: { endpoint: entity("rutas-reparto"), label: row => `${row.codigo || row.id} - ${row.nombre || ""}` },
    loteId: { endpoint: entity("lotes"), label: row => `${row.codigo || row.id} - ${row.estado || ""}` },
    bancoId: { endpoint: entity("bancos"), label: row => `${row.nombre || row.id} ${row.iban || ""}`.trim() },
    cajaId: { endpoint: entity("cajas"), label: row => `${row.fechaApertura || row.id} - ${row.estado || ""}` },
    rolId: { endpoint: entity("roles"), label: row => `${row.nombre || row.id}` }
};
lookups.cuentaId = { endpoint: entity("plan-cuentas"), label: row => `${row.codigo || row.id} - ${row.nombre || row.descripcion || ""}` };
lookups.albaranId = { endpoint: entity("albaranes"), label: row => `${row.numero || row.id} - ${row.cliente || ""}` };
lookups.loteInsumoId = lookups.loteId;

const moduleDescriptions = {
    dashboard: "Resumen operativo y acceso a todos los modulos.",
    clientes: "Alta, edicion, baja y activacion de clientes.",
    articulos: "Catalogo, precios, IVA y stock.",
    proveedores: "Gestion de proveedores y condiciones de compra.",
    presupuestos: "Presupuestos comerciales vinculados a clientes.",
    "pedidos-venta": "Pedidos de venta y seguimiento por estado.",
    albaranes: "Albaranes de venta, cliente y total.",
    facturas: "Facturacion de ventas, cobros y estado.",
    "pedidos-compra": "Pedidos a proveedor y fechas de entrega.",
    "facturas-compra": "Facturas recibidas, vencimientos y pagos.",
    almacenes: "Almacenes, capacidad, disponibilidad y responsables.",
    recetas: "Recetas, rendimiento, alergenos e ingredientes.",
    "ordenes-produccion": "Planificacion, inicio y cierre de produccion.",
    horneadas: "Registro de horneadas, temperaturas y resultado.",
    lotes: "Trazabilidad, caducidades y stock por lote.",
    appcc: "Controles APPCC y acciones correctivas.",
    vehiculos: "Flota, capacidades y estado.",
    "rutas-reparto": "Rutas, vehiculo asignado, conductor y paradas.",
    "hojas-ruta": "Hojas de ruta, entregas, kilometros e incidencias.",
    devoluciones: "Devoluciones, lineas, aceptacion y rechazo.",
    "direcciones-envio": "Direcciones de envio por cliente.",
    asientos: "Asientos contables y totales debe/haber.",
    "plan-contable": "Plan de cuentas y cuentas activas.",
    "movimientos-caja": "Movimientos de caja por tipo e importe.",
    cajas: "Apertura, cierre y arqueo de cajas diarias.",
    "caja-movimientos": "Movimientos vinculados a una caja diaria.",
    "movimientos-banco": "Movimientos bancarios y conciliacion.",
    bancos: "Cuentas bancarias, saldo, IBAN y cuenta principal.",
    modelo347: "Declaracion anual de operaciones con terceros.",
    auditoria: "Registro de acciones, usuarios, entidad y resultado.",
    "verifactu-evidencias": "Evidencias, hashes y estado de registros VeriFactu.",
    usuarios: "Usuarios, roles, activacion y cambio de contrasena.",
    empresa: "Datos de empresa y configuracion VeriFactu."
};

const commonPartyFields = [
    ["codigo", "Codigo", "text", true],
    ["nombre", "Nombre", "text", true],
    ["cif", "CIF", "text"],
    ["telefono", "Telefono", "tel"],
    ["email", "Email", "email"],
    ["direccion", "Direccion", "text"],
    ["poblacion", "Poblacion", "text"],
    ["codigoPostal", "Codigo postal", "text"],
    ["provincia", "Provincia", "text"],
    ["activo", "Activo", "checkbox"]
];

const schemas = {
    clientes: {
        title: "Cliente",
        columns: [["codigo", "Codigo"], ["nombre", "Razon social"], ["cif", "CIF/NIF"], ["telefono", "Telefono"], ["poblacion", "Poblacion"], ["activo", "Estado"]],
        fields: commonPartyFields.map(field => {
            if (field[0] === "nombre") return ["nombre", "Razon social", "text", true];
            if (field[0] === "cif") return ["cif", "CIF/NIF", "text"];
            if (field[0] === "email") return ["email", "Correo", "email"];
            return field;
        })
    },
    articulos: {
        title: "Articulo",
        columns: [["codigo", "Codigo"], ["nombre", "Nombre"], ["categoria", "Categoria"], ["pvp", "PVP"], ["stock", "Stock"], ["activo", "Estado"]],
        fields: [["codigo", "Codigo", "text", true], ["nombre", "Nombre", "text", true], ["descripcion", "Descripcion", "text"], ["categoria", "Categoria", "text"], ["familia", "Familia", "text"], ["unidad", "Unidad", "text"], ["iva", "IVA", "number"], ["pvp", "PVP", "number"], ["coste", "Coste", "number"], ["stock", "Stock", "number"], ["stockMinimo", "Stock minimo", "number"], ["activo", "Activo", "checkbox"]]
    },
    proveedores: {
        title: "Proveedor",
        columns: [["codigo", "Codigo"], ["nombre", "Nombre"], ["cif", "CIF"], ["telefono", "Telefono"], ["poblacion", "Poblacion"], ["activo", "Estado"]],
        fields: [...commonPartyFields.slice(0, 9), ["personaContacto", "Contacto", "text"], ["formaPago", "Forma pago", "text"], ["diasPago", "Dias pago", "number"], ["activo", "Activo", "checkbox"]]
    },
    presupuestos: documentSchema("Presupuesto", true),
    "pedidos-venta": documentSchema("Pedido venta", true),
    albaranes: documentSchema("Albaran", true),
    facturas: {
        title: "Factura",
        columns: [["serie", "Serie"], ["numero", "Numero"], ["fecha", "Fecha"], ["cliente", "Cliente"], ["estado", "Estado"], ["total", "Total"], ["pagada", "Pagada"]],
        fields: [["serie", "Serie", "text"], ["numero", "Numero", "text", true], ["fecha", "Fecha", "date"], ["clienteId", "Cliente", "select"], ["estado", "Estado", "text"], ["tipoFactura", "Tipo", "text"], ["baseImponible", "Base", "number"], ["totalIva", "IVA", "number"], ["total", "Total", "number"], ["pagado", "Pagado", "number"], ["pagada", "Pagada", "checkbox"], ["medioCobro", "Medio cobro", "text"], ["fechaVencimiento", "Vencimiento", "date"], ["observaciones", "Observaciones", "textarea"]]
    },
    "pedidos-compra": {
        title: "Pedido compra",
        columns: [["numero", "Numero"], ["fecha", "Fecha"], ["proveedor", "Proveedor"], ["estado", "Estado"], ["total", "Total"]],
        fields: [["numero", "Numero", "text", true], ["fecha", "Fecha", "date", true], ["fechaEsperadaEntrega", "Entrega esperada", "date"], ["proveedorId", "Proveedor", "select", true], ["estado", "Estado", "text"], ["total", "Total", "number"], ["observaciones", "Observaciones", "textarea"]]
    },
    "facturas-compra": {
        title: "Factura compra",
        columns: [["numero", "Numero"], ["fecha", "Fecha"], ["proveedor", "Proveedor"], ["estado", "Estado"], ["total", "Total"]],
        fields: [["numero", "Numero", "text", true], ["fecha", "Fecha", "date", true], ["proveedorId", "Proveedor", "select", true], ["estado", "Estado", "text"], ["baseImponible", "Base", "number"], ["importeIva", "IVA", "number"], ["total", "Total", "number"], ["pagada", "Pagada", "checkbox"], ["fechaVencimiento", "Vencimiento", "date"], ["formaPago", "Forma pago", "text"], ["observaciones", "Observaciones", "textarea"]]
    },
    almacenes: simpleSchema("Almacen", [["codigo", "Codigo"], ["nombre", "Nombre"], ["localidad", "Localidad"], ["activo", "Estado"]], [["codigo", "Codigo", "text", true], ["nombre", "Nombre", "text", true], ["localidad", "Localidad", "text"], ["responsable", "Responsable", "text"], ["capacidad", "Capacidad", "number"], ["disponible", "Disponible", "number"], ["descripcion", "Descripcion", "textarea"], ["activo", "Activo", "checkbox"]]),
    recetas: simpleSchema("Receta", [["codigo", "Codigo"], ["nombre", "Nombre"], ["rendimientoCantidad", "Rendimiento"], ["unidadRendimiento", "Unidad"], ["activo", "Estado"]], [["codigo", "Codigo", "text", true], ["nombre", "Nombre", "text", true], ["descripcion", "Descripcion", "textarea"], ["tiempoPreparacion", "Preparacion min", "number"], ["tiempoHorneado", "Horneado min", "number"], ["temperaturaHorneado", "Temperatura", "number"], ["rendimientoCantidad", "Rendimiento", "number"], ["unidadRendimiento", "Unidad", "text"], ["articuloResultanteId", "Articulo resultante", "select"], ["alergenos", "Alergenos", "textarea"], ["activo", "Activo", "checkbox"]]),
    "ordenes-produccion": simpleSchema("Orden produccion", [["numero", "Numero"], ["fecha", "Fecha"], ["receta", "Receta"], ["estado", "Estado"], ["cantidadPlanificada", "Planificado"]], [["numero", "Numero", "text", true], ["fecha", "Fecha", "date", true], ["recetaId", "Receta", "select"], ["articuloId", "Articulo", "select"], ["almacenId", "Almacen", "select"], ["estado", "Estado", "text"], ["cantidadPlanificada", "Cantidad planificada", "number"], ["cantidadProducida", "Cantidad producida", "number"], ["merma", "Merma", "number"], ["observaciones", "Observaciones", "textarea"]]),
    horneadas: simpleSchema("Horneada", [["fecha", "Fecha"], ["ordenProduccion", "Orden"], ["temperaturaInicial", "Temp. inicial"], ["temperaturaFinal", "Temp. final"], ["resultado", "Resultado"]], [["fecha", "Fecha", "date", true], ["ordenProduccionId", "Orden", "select"], ["horaInicio", "Hora inicio", "time"], ["horaFin", "Hora fin", "time"], ["temperaturaInicial", "Temp. inicial", "number"], ["temperaturaFinal", "Temp. final", "number"], ["humedadInicial", "Humedad inicial", "number"], ["tipoHorneada", "Tipo", "text"], ["cantidadProducida", "Cantidad", "number"], ["merma", "Merma", "number"], ["resultado", "Resultado", "text"], ["observaciones", "Observaciones", "textarea"]]),
    lotes: simpleSchema("Lote", [["codigo", "Codigo"], ["articulo", "Articulo"], ["fechaProduccion", "Produccion"], ["fechaCaducidad", "Caducidad"], ["cantidadActual", "Cantidad"]], [["codigo", "Codigo", "text", true], ["articuloId", "Articulo", "select", true], ["ordenProduccionId", "Orden", "select"], ["almacenId", "Almacen", "select"], ["fechaProduccion", "Produccion", "date", true], ["fechaCaducidad", "Caducidad", "date", true], ["cantidadInicial", "Cantidad inicial", "number"], ["cantidadActual", "Cantidad actual", "number"], ["estado", "Estado", "text"], ["origen", "Origen", "text"], ["numeroRegistroSanitario", "Registro sanitario", "text"], ["observaciones", "Observaciones", "textarea"]]),
    appcc: simpleSchema("Control APPCC", [["fecha", "Fecha"], ["puntoCritico", "Punto critico"], ["temperatura", "Temperatura"], ["resultado", "Resultado"], ["responsable", "Responsable"]], [["fecha", "Fecha", "date", true], ["hora", "Hora", "datetime-local", true], ["puntoCritico", "Punto critico", "text", true], ["descripcion", "Descripcion", "textarea"], ["temperatura", "Temperatura", "number"], ["unidadTemperatura", "Unidad", "text"], ["limiteCritico", "Limite critico", "number"], ["resultado", "Resultado", "text"], ["accionCorrectiva", "Accion correctiva", "textarea"], ["responsable", "Responsable", "text"], ["loteId", "Lote", "select"]]),
    vehiculos: simpleSchema("Vehiculo", [["matricula", "Matricula"], ["marca", "Marca"], ["modelo", "Modelo"], ["tipo", "Tipo"], ["activo", "Estado"]], [["matricula", "Matricula", "text", true], ["marca", "Marca", "text"], ["modelo", "Modelo", "text"], ["tipo", "Tipo", "text"], ["capacidadKg", "Capacidad kg", "number"], ["capacidadVolumen", "Volumen", "number"], ["consumoMedio", "Consumo medio", "number"], ["observaciones", "Observaciones", "textarea"], ["activo", "Activo", "checkbox"]]),
    "rutas-reparto": simpleSchema("Ruta reparto", [["codigo", "Codigo"], ["nombre", "Nombre"], ["vehiculo", "Vehiculo"], ["conductor", "Conductor"], ["activo", "Estado"]], [["codigo", "Codigo", "text", true], ["nombre", "Nombre", "text", true], ["vehiculoId", "Vehiculo", "select"], ["conductor", "Conductor", "text"], ["distanciaTotalKm", "Distancia km", "number"], ["tiempoEstimadoMinutos", "Tiempo min", "number"], ["descripcion", "Descripcion", "textarea"], ["activo", "Activo", "checkbox"]]),
    "hojas-ruta": simpleSchema("Hoja ruta", [["fecha", "Fecha"], ["ruta", "Ruta"], ["vehiculo", "Vehiculo"], ["conductor", "Conductor"], ["estado", "Estado"]], [["fecha", "Fecha", "date", true], ["rutaId", "Ruta", "select", true], ["vehiculoId", "Vehiculo", "select"], ["conductor", "Conductor", "text"], ["estado", "Estado", "text"], ["kmInicio", "Km inicio", "number"], ["kmFin", "Km fin", "number"], ["incidencias", "Incidencias", "textarea"], ["observaciones", "Observaciones", "textarea"]]),
    devoluciones: simpleSchema("Devolucion", [["numero", "Numero"], ["fecha", "Fecha"], ["cliente", "Cliente"], ["estado", "Estado"], ["importeTotal", "Importe"]], [["numero", "Numero", "text", true], ["fecha", "Fecha", "date"], ["clienteId", "Cliente", "select", true], ["estado", "Estado", "text"], ["motivo", "Motivo", "textarea"], ["importeTotal", "Importe", "number"], ["observaciones", "Observaciones", "textarea"]]),
    "direcciones-envio": simpleSchema("Direccion de envio", [["cliente", "Cliente"], ["codigoDireccion", "Codigo"], ["nombre", "Nombre"], ["poblacion", "Poblacion"], ["telefono", "Telefono"]], [["clienteId", "Cliente", "select"], ["codigoDireccion", "Codigo direccion", "number"], ["nombre", "Nombre", "text"], ["direccion", "Direccion", "textarea"], ["direccion2", "Direccion 2", "textarea"], ["poblacion", "Poblacion", "text"], ["provincia", "Provincia", "text"], ["cp", "CP", "text"], ["telefono", "Telefono", "tel"], ["notas", "Notas", "textarea"]]),
    asientos: simpleSchema("Asiento", [["numero", "Numero"], ["fecha", "Fecha"], ["concepto", "Concepto"], ["debe", "Debe"], ["haber", "Haber"]], [["numero", "Numero", "text", true], ["fecha", "Fecha", "date", true], ["concepto", "Concepto", "text", true], ["tipo", "Tipo", "text", true], ["descripcion", "Descripcion", "textarea"], ["debe", "Debe", "number"], ["haber", "Haber", "number"], ["observaciones", "Observaciones", "textarea"]]),
    "plan-contable": simpleSchema("Cuenta contable", [["codigo", "Codigo"], ["nombre", "Nombre"], ["tipo", "Tipo"], ["activa", "Activa"]], [["codigo", "Codigo", "text", true], ["nombre", "Nombre", "text", true], ["tipo", "Tipo", "text"], ["nivel", "Nivel", "number"], ["padreId", "Padre ID", "number"], ["activa", "Activa", "checkbox"]]),
    "movimientos-caja": simpleSchema("Movimiento caja", [["fecha", "Fecha"], ["tipo", "Tipo"], ["concepto", "Concepto"], ["importe", "Importe"]], [["fecha", "Fecha", "date"], ["tipo", "Tipo", "text"], ["concepto", "Concepto", "text"], ["importe", "Importe", "number"], ["observaciones", "Observaciones", "textarea"]]),
    cajas: simpleSchema("Caja diaria", [["fechaApertura", "Apertura"], ["fechaCierre", "Cierre"], ["saldoInicial", "Inicial"], ["saldoFinal", "Final"], ["estado", "Estado"]], [["fechaApertura", "Apertura", "datetime-local", true], ["fechaCierre", "Cierre", "datetime-local"], ["saldoInicial", "Saldo inicial", "number", true], ["saldoFinal", "Saldo final", "number"], ["saldoTeorico", "Saldo teorico", "number"], ["diferencia", "Diferencia", "number"], ["estado", "Estado", "text"], ["observaciones", "Observaciones", "textarea"]]),
    "caja-movimientos": simpleSchema("Movimiento de caja diaria", [["fecha", "Fecha"], ["caja", "Caja"], ["tipo", "Tipo"], ["concepto", "Concepto"], ["importe", "Importe"]], [["cajaId", "Caja", "select", true], ["tipo", "Tipo", "text", true], ["concepto", "Concepto", "text", true], ["importe", "Importe", "number", true], ["fecha", "Fecha", "datetime-local", true], ["formaPago", "Forma pago", "text"], ["referencia", "Referencia", "text"], ["observaciones", "Observaciones", "textarea"]]),
    "movimientos-banco": simpleSchema("Movimiento banco", [["fecha", "Fecha"], ["banco", "Banco"], ["concepto", "Concepto"], ["importe", "Importe"], ["conciliado", "Conciliado"]], [["fecha", "Fecha", "date", true], ["bancoId", "Banco", "select", true], ["tipo", "Tipo", "text"], ["concepto", "Concepto", "text"], ["importe", "Importe", "number", true], ["saldoResultante", "Saldo resultante", "number"], ["conciliado", "Conciliado", "checkbox"], ["fechaConciliacion", "Fecha conciliacion", "date"], ["observaciones", "Observaciones", "textarea"]]),
    bancos: simpleSchema("Cuenta bancaria", [["nombre", "Banco"], ["iban", "IBAN"], ["saldoActual", "Saldo"], ["principal", "Principal"], ["activo", "Estado"]], [["nombre", "Banco", "text", true], ["iban", "IBAN", "text"], ["swift", "SWIFT", "text"], ["numeroCuenta", "Numero cuenta", "text"], ["saldoActual", "Saldo", "number"], ["moneda", "Moneda", "text"], ["principal", "Principal", "checkbox"], ["activo", "Activo", "checkbox"], ["observaciones", "Observaciones", "textarea"]]),
    modelo347: simpleSchema("Registro Modelo 347", [["ejercicio", "Ejercicio"], ["nifDeclarado", "NIF"], ["nombreDeclarado", "Nombre"], ["tipoOperacion", "Tipo"], ["importeTotal", "Total"]], [["ejercicio", "Ejercicio", "number", true], ["nifDeclarado", "NIF", "text", true], ["nombreDeclarado", "Nombre", "text", true], ["tipoOperacion", "Tipo operacion", "text", true], ["claveOperacion", "Clave", "text"], ["esCliente", "Es cliente", "checkbox"], ["esProveedor", "Es proveedor", "checkbox"], ["importeT1", "Importe T1", "number"], ["importeT2", "Importe T2", "number"], ["importeT3", "Importe T3", "number"], ["importeT4", "Importe T4", "number"], ["importeTotal", "Importe total", "number"], ["importeAnual", "Importe anual", "number"], ["importeMetalicoTotal", "Metalico total", "number"], ["provincia", "Provincia", "text"], ["pais", "Pais", "text"], ["numeroOperaciones", "Operaciones", "number"], ["generado", "Generado", "checkbox"], ["observaciones", "Observaciones", "textarea"]]),
    auditoria: simpleSchema("Auditoria", [["fecha", "Fecha"], ["usuarioNombre", "Usuario"], ["tipoAccion", "Accion"], ["entidadTipo", "Entidad"], ["resultado", "Resultado"]], [["usuarioNombre", "Usuario", "text"], ["tipoAccion", "Accion", "text", true], ["fecha", "Fecha", "datetime-local", true], ["entidadTipo", "Entidad", "text"], ["entidadId", "Entidad ID", "text"], ["descripcion", "Descripcion", "textarea"], ["modulo", "Modulo", "text"], ["ip", "IP", "text"], ["userAgent", "User agent", "text"], ["resultado", "Resultado", "text"], ["mensajeError", "Mensaje error", "textarea"]]),
    "verifactu-evidencias": simpleSchema("Evidencia VeriFactu", [["createdAt", "Creacion"], ["serie", "Serie"], ["numero", "Numero"], ["facturaId", "Factura"], ["estado", "Estado"]], [["facturaId", "Factura ID", "text", true], ["serie", "Serie", "text"], ["numero", "Numero", "text"], ["estado", "Estado", "text"], ["hash", "Hash", "text", true], ["hashAnterior", "Hash anterior", "text"], ["certFingerprint", "Huella certificado", "text"], ["tipoRegistro", "Tipo registro", "text"], ["nifEmisor", "NIF emisor", "text"], ["codigoRespuestaAEAT", "Codigo AEAT", "text"], ["errorMessage", "Error", "textarea"], ["xmlGenerado", "XML", "textarea"]]),
    usuarios: simpleSchema("Usuario", [["username", "Usuario"], ["nombre", "Nombre"], ["email", "Email"], ["enabled", "Activo"]], [["username", "Usuario", "text", true], ["nombre", "Nombre", "text"], ["email", "Email", "email"], ["role", "Rol", "text"], ["rolId", "Rol", "select"], ["passwordNuevo", "Nueva contrasena", "password"], ["enabled", "Activo", "checkbox"]]),
    empresa: simpleSchema("Empresa", [["nombreEmpresa", "Nombre"], ["cif", "CIF"], ["telefono", "Telefono"], ["email", "Email"]], [["nombreEmpresa", "Nombre", "text", true], ["nombreComercial", "Nombre comercial", "text"], ["cif", "CIF", "text", true], ["direccion", "Direccion", "text"], ["ciudad", "Ciudad", "text"], ["provincia", "Provincia", "text"], ["codigoPostal", "Codigo postal", "text"], ["telefono", "Telefono", "tel"], ["email", "Email", "email"], ["web", "Web", "text"], ["registroSanitario", "Registro sanitario", "text"], ["verifactuHabilitado", "Verifactu", "checkbox"], ["activo", "Activo", "checkbox"]])
};

schemas.recetas.actions = [
    { id: "ingredientes", label: "Ingredientes", run: id => openChildManager("receta-ingredientes", id) }
];
schemas["ordenes-produccion"].actions = [
    { id: "iniciar", label: "Iniciar", run: id => request(`/api/produccion/ordenes/${id}/iniciar`, { method: "POST" }) },
    {
        id: "finalizar",
        label: "Finalizar",
        run: id => {
            const cantidad = prompt("Cantidad producida");
            if (cantidad === null) return null;
            const merma = prompt("Merma", "0");
            if (merma === null) return null;
            return request(`/api/produccion/ordenes/${id}/finalizar?cantidad=${encodeURIComponent(cantidad)}&merma=${encodeURIComponent(merma)}`, { method: "POST" });
        }
    }
];
schemas["rutas-reparto"].actions = [
    { id: "paradas", label: "Paradas", run: id => openChildManager("ruta-paradas", id) }
];
schemas["hojas-ruta"].actions = [
    { id: "entregas", label: "Entregas", run: id => openChildManager("hoja-ruta-entregas", id) },
    { id: "iniciar", label: "Iniciar", run: id => request(`/api/reparto/hojas/${id}/iniciar`, { method: "POST" }) },
    {
        id: "finalizar",
        label: "Finalizar",
        run: id => {
            const kmFin = prompt("Kilometros finales");
            if (kmFin === null) return null;
            const incidencias = prompt("Incidencias", "");
            if (incidencias === null) return null;
            return request(`/api/reparto/hojas/${id}/finalizar?kmFin=${encodeURIComponent(kmFin)}&incidencias=${encodeURIComponent(incidencias)}`, { method: "POST" });
        }
    }
];
schemas.devoluciones.actions = [
    { id: "lineas", label: "Lineas", run: id => openChildManager("devolucion-lineas", id) },
    { id: "aceptar", label: "Aceptar", run: id => request(`/api/devoluciones/${id}/aceptar`, { method: "POST" }) },
    {
        id: "rechazar",
        label: "Rechazar",
        run: id => {
            const motivo = prompt("Motivo del rechazo");
            if (motivo === null) return null;
            return request(`/api/devoluciones/${id}/rechazar?motivo=${encodeURIComponent(motivo)}`, { method: "POST" });
        }
    }
];
schemas.lotes.actions = [
    { id: "insumos", label: "Insumos", run: id => openChildManager("lote-insumos", id) },
    { id: "trazabilidad-adelante", label: "Trazabilidad salida", run: id => request(`/api/trazabilidad/lotes/${id}/trazabilidad-adelante`) },
    { id: "trazabilidad-atras", label: "Trazabilidad origen", run: id => request(`/api/trazabilidad/lotes/${id}/trazabilidad-atras`) }
];

for (const [moduleId, childId] of Object.entries({
    presupuestos: "presupuesto-lineas",
    "pedidos-venta": "pedido-lineas",
    albaranes: "albaran-lineas",
    facturas: "factura-lineas",
    "pedidos-compra": "pedido-compra-lineas",
    "facturas-compra": "factura-compra-lineas",
    asientos: "asiento-lineas"
})) {
    schemas[moduleId].actions = [{ id: "lineas", label: "Lineas", run: id => openChildManager(childId, id) }, ...(schemas[moduleId].actions || [])];
}

const commonLineColumns = [["articulo", "Articulo"], ["descripcion", "Descripcion"], ["cantidad", "Cantidad"], ["precioUnitario", "Precio"], ["importe", "Importe"]];
const commonLineFields = [["articuloId", "Articulo", "select", true], ["descripcion", "Descripcion", "textarea"], ["cantidad", "Cantidad", "number", true], ["precioUnitario", "Precio unitario", "number", true], ["descuento", "Descuento %", "number"], ["tipoIva", "IVA %", "number"], ["importe", "Importe", "number"], ["orden", "Orden", "number"]];

const childSchemas = {
    "pedido-lineas": {
        title: "Lineas de pedido",
        columns: [["articulo", "Articulo"], ["descripcion", "Descripcion"], ["cantidad", "Cantidad"], ["precio", "Precio"], ["descuento", "Dto."], ["iva", "IVA"]],
        fields: [["articuloId", "Articulo", "select", true], ["descripcion", "Descripcion", "textarea"], ["cantidad", "Cantidad", "number", true], ["precio", "Precio", "number", true], ["descuento", "Descuento %", "number"], ["iva", "IVA %", "number"]]
    },
    "presupuesto-lineas": { title: "Lineas de presupuesto", columns: commonLineColumns, fields: commonLineFields },
    "albaran-lineas": {
        title: "Lineas de albaran",
        columns: [["articulo", "Articulo"], ["descripcion", "Descripcion"], ["cantidad", "Cantidad"], ["precio", "Precio"], ["descuento", "Dto."], ["iva", "IVA"]],
        fields: [["articuloId", "Articulo", "select", true], ["descripcion", "Descripcion", "textarea"], ["cantidad", "Cantidad", "number", true], ["precio", "Precio", "number", true], ["descuento", "Descuento %", "number"], ["iva", "IVA %", "number"]]
    },
    "factura-lineas": {
        title: "Lineas de factura",
        columns: [["articulo", "Articulo"], ["descripcion", "Descripcion"], ["cantidad", "Cantidad"], ["precioUnitario", "Precio"], ["descuento", "Dto."], ["iva", "IVA"], ["total", "Total"]],
        fields: [["articuloId", "Articulo", "select", true], ["descripcion", "Descripcion", "textarea"], ["cantidad", "Cantidad", "number", true], ["precioUnitario", "Precio unitario", "number"], ["precio", "Precio", "number"], ["descuento", "Descuento %", "number"], ["iva", "IVA %", "number"], ["total", "Total", "number"]]
    },
    "pedido-compra-lineas": { title: "Lineas de pedido de compra", columns: [["articulo", "Articulo"], ["descripcion", "Descripcion"], ["cantidad", "Cantidad"], ["cantidadRecibida", "Recibida"], ["precioUnitario", "Precio"], ["importe", "Importe"]], fields: [["articuloId", "Articulo", "select", true], ["descripcion", "Descripcion", "textarea"], ["cantidad", "Cantidad", "number", true], ["cantidadRecibida", "Cantidad recibida", "number"], ["precioUnitario", "Precio unitario", "number", true], ["descuento", "Descuento %", "number"], ["tipoIva", "IVA %", "number"], ["importe", "Importe", "number"], ["orden", "Orden", "number"]] },
    "factura-compra-lineas": { title: "Lineas de factura de compra", columns: commonLineColumns, fields: commonLineFields },
    "receta-ingredientes": { title: "Ingredientes", columns: [["articulo", "Articulo"], ["cantidad", "Cantidad"], ["unidad", "Unidad"], ["orden", "Orden"], ["notas", "Notas"]], fields: [["articuloId", "Articulo", "select", true], ["cantidad", "Cantidad", "number", true], ["unidad", "Unidad", "text"], ["orden", "Orden", "number"], ["notas", "Notas", "textarea"]] },
    "ruta-paradas": { title: "Paradas de ruta", columns: [["orden", "Orden"], ["cliente", "Cliente"], ["horaEstimada", "Hora"], ["tiempoParadaMinutos", "Minutos"], ["notas", "Notas"]], fields: [["clienteId", "Cliente", "select", true], ["orden", "Orden", "number"], ["horaEstimada", "Hora estimada", "time"], ["tiempoParadaMinutos", "Minutos parada", "number"], ["notas", "Notas", "textarea"]] },
    "hoja-ruta-entregas": { title: "Entregas de hoja de ruta", columns: [["orden", "Orden"], ["cliente", "Cliente"], ["albaran", "Albaran"], ["estadoEntrega", "Estado"], ["entregado", "Entregado"], ["importeCobrado", "Cobrado"]], fields: [["clienteId", "Cliente", "select", true], ["albaranId", "Albaran", "select"], ["orden", "Orden", "number"], ["entregado", "Entregado", "checkbox"], ["estadoEntrega", "Estado", "text"], ["fechaEntrega", "Fecha entrega", "datetime-local"], ["personaRecepcion", "Persona recepcion", "text"], ["importeCobrado", "Importe cobrado", "number"], ["medioCobro", "Medio cobro", "text"], ["incidencia", "Incidencia", "textarea"], ["observaciones", "Observaciones", "textarea"]] },
    "devolucion-lineas": { title: "Lineas de devolucion", columns: [["articulo", "Articulo"], ["cantidad", "Cantidad"], ["precioUnitario", "Precio"], ["importe", "Importe"], ["motivo", "Motivo"], ["destino", "Destino"]], fields: [["articuloId", "Articulo", "select", true], ["cantidad", "Cantidad", "number", true], ["precioUnitario", "Precio unitario", "number"], ["importe", "Importe", "number"], ["motivo", "Motivo", "text"], ["destino", "Destino", "text"], ["loteId", "Lote", "select"]] },
    "asiento-lineas": { title: "Lineas contables", columns: [["cuenta", "Cuenta"], ["debe", "Debe"], ["haber", "Haber"], ["concepto", "Concepto"], ["orden", "Orden"]], fields: [["cuentaId", "Cuenta", "select", true], ["debe", "Debe", "number"], ["haber", "Haber", "number"], ["concepto", "Concepto", "text"], ["orden", "Orden", "number"]] },
    "lote-insumos": { title: "Insumos del lote", columns: [["loteInsumo", "Lote insumo"], ["cantidadUsada", "Cantidad usada"]], fields: [["loteInsumoId", "Lote insumo", "select", true], ["cantidadUsada", "Cantidad usada", "number", true]] }
};

const els = {
    nav: document.querySelector("#moduleNav"),
    quickModules: document.querySelector("#quickModules"),
    title: document.querySelector("#viewTitle"),
    subtitle: document.querySelector("#viewSubtitle"),
    status: document.querySelector("#connectionStatus"),
    metrics: document.querySelector("#metricsGrid"),
    dashboardView: document.querySelector("#dashboardView"),
    tableView: document.querySelector("#tableView"),
    tableHead: document.querySelector("#tableHead"),
    tableBody: document.querySelector("#tableBody"),
    search: document.querySelector("#searchInput"),
    newButton: document.querySelector("#newButton"),
    exportButton: document.querySelector("#exportButton"),
    recordCount: document.querySelector("#recordCount"),
    refresh: document.querySelector("#refreshButton"),
    dialog: document.querySelector("#entityDialog"),
    dialogTitle: document.querySelector("#dialogTitle"),
    form: document.querySelector("#entityForm"),
    formFields: document.querySelector("#formFields"),
    cancelButton: document.querySelector("#cancelButton"),
    closeEntityButton: document.querySelector("#closeEntityButton"),
    saveButton: document.querySelector("#saveButton"),
    infoDialog: document.querySelector("#infoDialog"),
    infoTitle: document.querySelector("#infoTitle"),
    infoContent: document.querySelector("#infoContent"),
    closeInfoButton: document.querySelector("#closeInfoButton")
};
Object.assign(els, {
    childDialog: document.querySelector("#childDialog"),
    childTitle: document.querySelector("#childTitle"),
    childSubtitle: document.querySelector("#childSubtitle"),
    childTableHead: document.querySelector("#childTableHead"),
    childTableBody: document.querySelector("#childTableBody"),
    childFormTitle: document.querySelector("#childFormTitle"),
    childForm: document.querySelector("#childForm"),
    childFormFields: document.querySelector("#childFormFields"),
    resetChildButton: document.querySelector("#resetChildButton"),
    saveChildButton: document.querySelector("#saveChildButton")
});

function documentSchema(title, withCustomer) {
    const relation = withCustomer ? ["clienteId", "Cliente", "select"] : ["proveedorId", "Proveedor", "select"];
    return {
        title,
        columns: [["numero", "Numero"], ["fecha", "Fecha"], [withCustomer ? "cliente" : "proveedor", withCustomer ? "Cliente" : "Proveedor"], ["estado", "Estado"], ["total", "Total"]],
        fields: [["numero", "Numero", "text", true], ["fecha", "Fecha", "date"], relation, ["estado", "Estado", "text"], ["total", "Total", "number"], ["observaciones", "Observaciones", "textarea"]]
    };
}

function simpleSchema(title, columns, fields) {
    return { title, columns, fields };
}

function init() {
    renderNav();
    bindEvents();
    selectView(document.body.dataset.activeModule || "dashboard", false);
}

function bindEvents() {
    els.refresh.addEventListener("click", () => loadCurrentView());
    els.search.addEventListener("input", () => renderTable());
    els.newButton.addEventListener("click", () => openEditor());
    els.exportButton.addEventListener("click", exportCurrentRows);
    els.saveButton.addEventListener("click", saveEntity);
    els.cancelButton.addEventListener("click", closeEditor);
    els.closeEntityButton.addEventListener("click", closeEditor);
    els.closeInfoButton.addEventListener("click", closeInfo);
    els.resetChildButton.addEventListener("click", resetChildForm);
    els.saveChildButton.addEventListener("click", saveChildEntity);
    document.querySelectorAll("[data-view]").forEach(button => {
        button.addEventListener("click", event => {
            event.preventDefault();
            selectView(button.dataset.view);
        });
    });
}

function closeEditor() {
    els.form.reset();
    els.form.classList.remove("was-validated");
    state.editing = null;
    bootstrap.Modal.getOrCreateInstance(els.dialog).hide();
}

function renderNav() {
    els.nav.innerHTML = modules.map(module => `<a class="nav-link" href="/web/${module.id === "dashboard" ? "" : module.id}" data-id="${module.id}"><i class="bi bi-grid-1x2"></i><span>${module.label}</span></a>`).join("");
    els.nav.querySelectorAll("[data-id]").forEach(button => button.addEventListener("click", event => {
        event.preventDefault();
        selectView(button.dataset.id);
    }));
    els.quickModules.innerHTML = modules.filter(module => module.id !== "dashboard")
        .map(module => `<a class="btn btn-light text-start" href="/web/${module.id}" data-id="${module.id}"><span>${module.label}</span></a>`).join("");
    els.quickModules.querySelectorAll("[data-id]").forEach(button => button.addEventListener("click", event => {
        event.preventDefault();
        selectView(button.dataset.id);
    }));
}

async function selectView(id, pushState = true) {
    state.view = id;
    els.nav.querySelectorAll("[data-id]").forEach(button => button.classList.toggle("active", button.dataset.id === id));
    if (pushState) {
        history.pushState({}, "", id === "dashboard" ? "/web" : `/web/${id}`);
    }
    await loadCurrentView();
}

async function loadCurrentView() {
    const module = modules.find(item => item.id === state.view) || modules[0];
    els.title.textContent = module.label;
    els.subtitle.textContent = moduleDescriptions[module.id] || "Gestion del modulo";
    showView(module.type === "dashboard" ? "dashboard" : "table");
    if (module.type === "dashboard") {
        await loadDashboard();
    } else {
        await loadTable(module);
    }
}

function showView(name) {
    els.dashboardView.classList.toggle("is-active", name === "dashboard");
    els.tableView.classList.toggle("is-active", name === "table");
}

async function request(url, options = {}) {
    const response = await fetch(url, { headers: { "Content-Type": "application/json" }, ...options });
    if (!response.ok) throw new Error(`HTTP ${response.status}`);
    setStatus("Conectado", "is-ok");
    return response.status === 204 ? null : response.json();
}

async function loadDashboard() {
    try {
        const data = await request("/api/web/resumen");
        const metrics = [["Clientes", data.clientes], ["Articulos", data.articulos], ["Proveedores", data.proveedores], ["Facturas", data.facturas], ["Pedidos venta", data.pedidosVenta], ["Pedidos compra", data.pedidosCompra]];
        els.metrics.innerHTML = metrics.map(([label, value]) => `<article class="metric"><span>${label}</span><strong>${value}</strong></article>`).join("");
    } catch (error) {
        setStatus("Sin conexion", "is-error");
        els.metrics.innerHTML = `<article class="metric"><span>Error</span><strong>${escapeHtml(error.message)}</strong></article>`;
    }
}

async function loadTable(module) {
    try {
        state.rows = await request(module.endpoint);
        els.search.value = "";
        renderTable();
    } catch (error) {
        setStatus("Sin conexion", "is-error");
        state.rows = [];
        renderTable(error);
    }
}

function renderTable(error) {
    const schema = schemas[state.view];
    if (!schema) return;
    const query = els.search.value.trim().toLowerCase();
    const rows = state.rows.filter(row => JSON.stringify(row).toLowerCase().includes(query));
    els.recordCount.textContent = `${rows.length} ${rows.length === 1 ? "registro" : "registros"}`;
    els.tableHead.innerHTML = `<tr>${schema.columns.map(([, label]) => `<th scope="col">${label}</th>`).join("")}<th scope="col">Acciones</th></tr>`;
    if (error) {
        els.tableBody.innerHTML = `<tr><td colspan="${schema.columns.length + 1}">No se pudo cargar: ${escapeHtml(error.message)}</td></tr>`;
        return;
    }
    els.tableBody.innerHTML = rows.map(row => {
        const cells = schema.columns.map(([key]) => `<td>${formatCell(row[key], key)}</td>`).join("");
        return `<tr>${cells}<td class="actions">${rowActions(row)}</td></tr>`;
    }).join("") || `<tr><td colspan="${schema.columns.length + 1}">No hay registros</td></tr>`;
    els.tableBody.querySelectorAll("[data-edit]").forEach(button => button.addEventListener("click", () => openEditor(Number(button.dataset.edit))));
    els.tableBody.querySelectorAll("[data-toggle]").forEach(button => button.addEventListener("click", () => toggleActive(Number(button.dataset.toggle), button.dataset.active === "true")));
    els.tableBody.querySelectorAll("[data-delete]").forEach(button => button.addEventListener("click", () => deleteEntity(Number(button.dataset.delete))));
    els.tableBody.querySelectorAll("[data-action]").forEach(button => {
        button.addEventListener("click", () => executeAction(Number(button.dataset.id), button.dataset.action));
    });
}

async function openChildManager(childId, parentId) {
    const schema = childSchemas[childId];
    if (!schema) {
        showInfo("Subregistros", { mensaje: "No hay configuracion web para este subregistro." });
        return null;
    }
    state.child = { childId, parentId, rows: [], editing: null };
    els.childTitle.textContent = schema.title;
    els.childSubtitle.textContent = `${schemas[state.view]?.title || "Registro"} #${parentId}`;
    await loadSchemaLookups(schema);
    resetChildForm();
    bootstrap.Modal.getOrCreateInstance(els.childDialog).show();
    await loadChildRows();
    return null;
}

async function loadChildRows() {
    const child = state.child;
    if (!child) return;
    try {
        child.rows = await request(`${childEntity(child.childId)}/${child.parentId}`);
        renderChildTable();
    } catch (error) {
        child.rows = [];
        renderChildTable(error);
    }
}

function renderChildTable(error) {
    const child = state.child;
    if (!child) return;
    const schema = childSchemas[child.childId];
    els.childTableHead.innerHTML = `<tr>${schema.columns.map(([, label]) => `<th scope="col">${label}</th>`).join("")}<th scope="col">Acciones</th></tr>`;
    if (error) {
        els.childTableBody.innerHTML = `<tr><td colspan="${schema.columns.length + 1}">No se pudo cargar: ${escapeHtml(error.message)}</td></tr>`;
        return;
    }
    els.childTableBody.innerHTML = child.rows.map(row => {
        const cells = schema.columns.map(([key]) => `<td>${formatCell(row[key], key)}</td>`).join("");
        return `<tr>${cells}<td class="actions"><button class="btn btn-sm btn-outline-primary" type="button" data-child-edit="${row.id}"><i class="bi bi-pencil"></i> Editar</button><button class="btn btn-sm btn-outline-danger" type="button" data-child-delete="${row.id}"><i class="bi bi-trash"></i> Eliminar</button></td></tr>`;
    }).join("") || `<tr><td colspan="${schema.columns.length + 1}">No hay registros</td></tr>`;
    els.childTableBody.querySelectorAll("[data-child-edit]").forEach(button => {
        button.addEventListener("click", () => editChildEntity(Number(button.dataset.childEdit)));
    });
    els.childTableBody.querySelectorAll("[data-child-delete]").forEach(button => {
        button.addEventListener("click", () => deleteChildEntity(Number(button.dataset.childDelete)));
    });
}

function resetChildForm() {
    const child = state.child;
    if (!child) return;
    const schema = childSchemas[child.childId];
    child.editing = {};
    els.childForm.reset();
    els.childForm.classList.remove("was-validated");
    els.childFormTitle.textContent = "Nuevo";
    els.childFormFields.innerHTML = schema.fields.map(([key, label, type, required]) => renderField(key, label, type, required, null)).join("");
}

function editChildEntity(id) {
    const child = state.child;
    if (!child) return;
    const schema = childSchemas[child.childId];
    const row = child.rows.find(item => item.id === id);
    child.editing = row || {};
    els.childForm.classList.remove("was-validated");
    els.childFormTitle.textContent = `Editar #${id}`;
    els.childFormFields.innerHTML = schema.fields.map(([key, label, type, required]) => renderField(key, label, type, required, child.editing[key])).join("");
}

async function saveChildEntity() {
    const child = state.child;
    if (!child) return;
    els.childForm.classList.add("was-validated");
    if (!els.childForm.reportValidity()) return;
    const schema = childSchemas[child.childId];
    const data = Object.fromEntries(new FormData(els.childForm).entries());
    for (const [key, , type] of schema.fields) {
        if (!(key in data)) continue;
        if (data[key] === "") data[key] = null;
        else if (type === "number" || type === "select") data[key] = Number(data[key]);
        else if (type === "checkbox") data[key] = data[key] === "true";
    }
    const id = child.editing?.id;
    try {
        await request(id ? `${childEntity(child.childId)}/${child.parentId}/${id}` : `${childEntity(child.childId)}/${child.parentId}`, {
            method: id ? "PUT" : "POST",
            body: JSON.stringify(data)
        });
        resetChildForm();
        await loadChildRows();
        await loadCurrentView();
    } catch (error) {
        setStatus("Error guardando linea", "is-error");
        showInfo("Error", { mensaje: error.message });
    }
}

async function deleteChildEntity(id) {
    const child = state.child;
    if (!child || !confirm("Eliminar este subregistro?")) return;
    try {
        await request(`${childEntity(child.childId)}/${child.parentId}/${id}`, { method: "DELETE" });
        resetChildForm();
        await loadChildRows();
        await loadCurrentView();
    } catch (error) {
        setStatus("Error eliminando linea", "is-error");
        showInfo("Error", { mensaje: error.message });
    }
}

function exportCurrentRows() {
    const schema = schemas[state.view];
    if (!schema) return;
    const query = els.search.value.trim().toLowerCase();
    const rows = state.rows.filter(row => JSON.stringify(row).toLowerCase().includes(query));
    const headers = schema.columns.map(([, label]) => label);
    const keys = schema.columns.map(([key]) => key);
    const csvRows = [
        headers.join(";"),
        ...rows.map(row => keys.map(key => csvValue(row[key])).join(";"))
    ];
    const blob = new Blob([csvRows.join("\n")], { type: "text/csv;charset=utf-8" });
    const link = document.createElement("a");
    link.href = URL.createObjectURL(blob);
    link.download = `${state.view}.csv`;
    link.click();
    URL.revokeObjectURL(link.href);
}

function rowActions(row) {
    const schema = schemas[state.view];
    const canToggle = schema.fields.some(([key]) => ["activo", "activa", "enabled"].includes(key));
    const actions = [`<button class="btn btn-sm btn-outline-primary" type="button" data-edit="${row.id}"><i class="bi bi-pencil"></i> Editar</button>`];
    if (canToggle && row.id) {
        const activeKey = ["activo", "activa", "enabled"].find(key => key in row) || "activo";
        const active = row[activeKey] !== false;
        actions.push(`<button class="btn btn-sm btn-outline-secondary" type="button" data-toggle="${row.id}" data-active="${active}">${active ? "Baja" : "Activar"}</button>`);
    } else if (row.id && modules.find(item => item.id === state.view)?.endpoint?.startsWith("/api/web/entities/")) {
        actions.push(`<button class="btn btn-sm btn-outline-danger" type="button" data-delete="${row.id}"><i class="bi bi-trash"></i> Eliminar</button>`);
    }
    for (const action of schema.actions || []) {
        actions.push(`<button class="btn btn-sm btn-outline-secondary" type="button" data-action="${action.id}" data-id="${row.id}">${action.label}</button>`);
    }
    return actions.join("");
}

function formatCell(value, key) {
    if (["activo", "activa", "enabled", "pagada", "verifactuHabilitado"].includes(key)) return `<span class="badge rounded-pill ${value === false ? "text-bg-secondary" : "text-bg-success"}">${value === false ? "No" : "Si"}</span>`;
    if (value === null || value === undefined || value === "") return "-";
    if (["total", "pvp", "coste", "precio", "precioUnitario", "importe", "importeTotal", "importeCobrado", "baseImponible", "totalIva", "importeIva", "pagado", "debe", "haber"].includes(key)) return `${Number(value).toFixed(2)} EUR`;
    return escapeHtml(String(value));
}

async function openEditor(id) {
    const schema = schemas[state.view];
    const row = id ? state.rows.find(item => item.id === id) : { activo: true };
    state.editing = row || { activo: true };
    await loadSchemaLookups(schema);
    els.dialogTitle.textContent = id ? `Editar ${schema.title}` : `Nuevo ${schema.title}`;
    els.formFields.innerHTML = schema.fields.map(([key, label, type, required]) => renderField(key, label, type, required, state.editing[key])).join("");
    bootstrap.Modal.getOrCreateInstance(els.dialog).show();
}

function renderField(key, label, type, required, value) {
    if (type === "select" || lookups[key]) {
        const options = state.lookupOptions[key] || [];
        const empty = `<option value="">Sin asignar</option>`;
        return `<label class="field form-label">${label}<select class="form-select" name="${key}"${required ? " required" : ""}>${empty}${options.map(option => `<option value="${option.id}" ${String(option.id) === String(value ?? "") ? "selected" : ""}>${escapeHtml(option.label)}</option>`).join("")}</select><span class="invalid-feedback">Completa este campo.</span></label>`;
    }
    if (type === "checkbox") {
        return `<label class="field form-label">${label}<select class="form-select" name="${key}"><option value="true" ${value !== false ? "selected" : ""}>Si</option><option value="false" ${value === false ? "selected" : ""}>No</option></select></label>`;
    }
    if (type === "textarea") {
        return `<label class="field field-wide form-label">${label}<textarea class="form-control" name="${key}"${required ? " required" : ""}>${escapeHtml(value ?? "")}</textarea><span class="invalid-feedback">Completa este campo.</span></label>`;
    }
    const step = type === "number" ? ` step="0.01"` : "";
    return `<label class="field form-label">${label}<input class="form-control" name="${key}" type="${type}" value="${escapeHtml(value ?? "")}"${required ? " required" : ""}${step}><span class="invalid-feedback">Completa este campo.</span></label>`;
}

async function loadSchemaLookups(schema) {
    const keys = schema.fields
        .map(([key, , type]) => type === "select" || lookups[key] ? key : null)
        .filter(Boolean);
    await Promise.all(keys.map(async key => {
        if (state.lookupOptions[key]) {
            return;
        }
        const config = lookups[key];
        if (!config) {
            state.lookupOptions[key] = [];
            return;
        }
        try {
            const rows = await request(config.endpoint);
            state.lookupOptions[key] = rows.map(row => ({ id: row.id, label: config.label(row) }));
        } catch {
            state.lookupOptions[key] = [];
        }
    }));
}

async function saveEntity() {
    els.form.classList.add("was-validated");
    if (!els.form.reportValidity()) return;
    const data = Object.fromEntries(new FormData(els.form).entries());
    const schema = schemas[state.view];
    for (const [key, , type] of schema.fields) {
        if (!(key in data)) continue;
        if (data[key] === "") data[key] = null;
        else if (type === "number" || type === "select") data[key] = Number(data[key]);
        else if (type === "checkbox") data[key] = data[key] === "true";
    }
    const module = modules.find(item => item.id === state.view);
    const id = state.editing?.id;
    try {
        await request(id ? `${module.endpoint}/${id}` : module.endpoint, { method: id ? "PUT" : "POST", body: JSON.stringify(data) });
        closeEditor();
        await loadCurrentView();
    } catch (error) {
        setStatus("Error guardando", "is-error");
    }
}

async function toggleActive(id, active) {
    const module = modules.find(item => item.id === state.view);
    const action = active ? "baja" : "activar";
    try {
        await request(`${module.endpoint}/${id}/${action}`, { method: "POST" });
        await loadCurrentView();
    } catch (error) {
        setStatus("Error", "is-error");
    }
}

async function deleteEntity(id) {
    const module = modules.find(item => item.id === state.view);
    if (!confirm("Eliminar este registro de forma permanente?")) {
        return;
    }
    try {
        await request(`${module.endpoint}/${id}`, { method: "DELETE" });
        await loadCurrentView();
    } catch (error) {
        setStatus("Error eliminando", "is-error");
    }
}

async function executeAction(id, actionId) {
    const schema = schemas[state.view];
    const action = (schema.actions || []).find(item => item.id === actionId);
    if (!action) {
        return;
    }
    try {
        const result = await action.run(id);
        if (result !== undefined && result !== null) {
            showInfo(action.label, result);
        }
        await loadCurrentView();
    } catch (error) {
        setStatus("Error en accion", "is-error");
        showInfo("Error", { mensaje: error.message });
    }
}

function showInfo(title, data) {
    els.infoTitle.textContent = title;
    els.infoContent.textContent = typeof data === "string" ? data : JSON.stringify(data, null, 2);
    bootstrap.Modal.getOrCreateInstance(els.infoDialog).show();
}

function closeInfo() {
    bootstrap.Modal.getOrCreateInstance(els.infoDialog).hide();
}

function setStatus(text, className) {
    els.status.textContent = text;
    els.status.className = `status-pill ${className || ""}`;
}

function escapeHtml(value) {
    return String(value).replace(/[&<>"']/g, char => ({ "&": "&amp;", "<": "&lt;", ">": "&gt;", "\"": "&quot;", "'": "&#39;" }[char]));
}

function csvValue(value) {
    if (value === null || value === undefined) {
        return "";
    }
    return `"${String(value).replace(/"/g, '""')}"`;
}

init();
