import { chromium } from 'playwright';
import { writeFileSync, mkdirSync } from 'fs';

const BASE = 'http://localhost:8080';
const OUT  = 'scripts/ui-test-results';
mkdirSync(OUT, { recursive: true });

const issues = [];
let sc = 0;

async function shot(page, name) {
  await page.screenshot({ path: `${OUT}/${String(++sc).padStart(3,'0')}-${name}.png`, fullPage: true });
}

function fail(msg) { issues.push('❌ ' + msg); console.log('❌ ' + msg); }
function warn(msg) { issues.push('⚠️  ' + msg); console.log('⚠️  ' + msg); }
function ok(msg)   { console.log('✅ ' + msg); }

// Espera toast y devuelve tipo y mensaje
async function waitToast(page) {
  try {
    await page.waitForSelector('#toast-container .toast', { timeout: 4000 });
    const ok2 = await page.locator('.toast.text-bg-success').count() > 0;
    const err = await page.locator('.toast.text-bg-danger').count() > 0;
    const msg = await page.locator('.toast .toast-body span').first().textContent().catch(() => '');
    return { ok: ok2, err, msg: msg.trim() };
  } catch {
    return { ok: false, err: false, msg: '' };
  }
}

// Guardar formulario (botón en .form-card, no el de logout del topbar)
async function submitForm(page) {
  await page.locator('.form-card [type=submit], .form-card button[type=submit]').first().click();
}

(async () => {
  const browser = await chromium.launch({ headless: true });
  const ctx = await browser.newContext({ viewport: { width: 1400, height: 900 } });
  const page = await ctx.newPage();

  const jsErrors = [];
  page.on('console', msg => {
    if (msg.type() === 'error' && !msg.text().includes('source map') && !msg.text().includes('favicon'))
      jsErrors.push(msg.text().substring(0, 150));
  });
  page.on('pageerror', err => jsErrors.push(err.message.substring(0, 150)));

  // ─── LOGIN ───────────────────────────────────────────────────────────────
  console.log('\n═══ LOGIN ═══');
  await page.goto(`${BASE}/web/login`);
  await shot(page, 'login');
  await page.fill('#username', 'admin');
  await page.fill('#password', 'admin');
  await page.locator('#loginForm [type=submit], form [type=submit]').first().click();
  await page.waitForURL('**/dashboard', { timeout: 8000 });
  await page.waitForLoadState('networkidle');
  ok('Login admin/admin → dashboard');
  await shot(page, 'dashboard');

  // KPIs
  const kpis = await page.locator('.kpi-card').count();
  kpis >= 4 ? ok(`Dashboard: ${kpis} KPI cards`) : fail(`Dashboard: solo ${kpis} KPIs (esperados ≥4)`);

  // Gráficos canvas
  const canvases = await page.locator('canvas').count();
  canvases > 0 ? ok(`Dashboard: ${canvases} gráfico(s) canvas`) : warn('Dashboard: sin gráficos canvas');

  // ─── CLIENTES ────────────────────────────────────────────────────────────
  console.log('\n═══ CLIENTES ═══');
  await page.goto(`${BASE}/web/clientes`);
  await page.waitForLoadState('networkidle');
  await shot(page, 'clientes-lista');
  const hayTablaClientes = await page.locator('table').count() > 0;
  hayTablaClientes ? ok('Clientes: tabla presente') : fail('Clientes: sin tabla en lista');

  // Formulario nuevo
  await page.goto(`${BASE}/web/clientes/nuevo`);
  await page.waitForLoadState('networkidle');
  await shot(page, 'clientes-formulario');

  for (const [id, label] of [['codigo','Código'],['nombre','Razón social'],['cif','CIF/NIF'],['email','Email'],['telefono','Teléfono']]) {
    await page.locator(`#${id}`).count() > 0 ? ok(`Clientes form: campo #${id}`) : fail(`Clientes form: falta campo #${id}`);
  }

  // Envío vacío — validación HTML5 debe bloquear
  await submitForm(page);
  await page.waitForTimeout(600);
  const urlTrasSubmitVacio = page.url();
  if (urlTrasSubmitVacio.includes('/nuevo') || urlTrasSubmitVacio.includes('/clientes') && !urlTrasSubmitVacio.endsWith('/clientes')) {
    ok('Clientes: submit vacío bloqueado (stayed on form)');
  } else if (urlTrasSubmitVacio.endsWith('/clientes')) {
    // Navegó a la lista — puede ser con error o éxito vacío
    const t = await waitToast(page);
    if (t.err) warn(`Clientes: submit vacío fue al servidor → error: "${t.msg}"`);
    else warn('Clientes: submit vacío navegó a la lista sin error — validación client-side no bloqueó');
    await page.goto(`${BASE}/web/clientes/nuevo`);
    await page.waitForLoadState('networkidle');
  }
  await shot(page, 'clientes-validacion');

  // Guardar cliente válido
  await page.fill('#codigo', 'UI-CLI-01');
  await page.fill('#nombre', 'Cliente UI Test');
  await page.fill('#cif', '12345678A');
  await page.fill('#email', 'ui@test.com');
  await submitForm(page);
  await page.waitForURL('**/clientes', { timeout: 6000 });
  const tCli = await waitToast(page);
  tCli.ok ? ok(`Clientes: guardado OK`) : fail(`Clientes: error al guardar — "${tCli.msg}"`);
  await shot(page, 'clientes-guardado');

  // Duplicado
  await page.goto(`${BASE}/web/clientes/nuevo`);
  await page.fill('#codigo', 'UI-CLI-01');
  await page.fill('#nombre', 'Dup');
  await submitForm(page);
  await page.waitForURL('**/clientes', { timeout: 6000 });
  const tDup = await waitToast(page);
  tDup.err ? ok(`Clientes: duplicado detectado ("${tDup.msg}")`) : warn('Clientes: duplicado no dio error');

  // ID inexistente
  await page.goto(`${BASE}/web/clientes/99999`);
  await page.waitForURL('**/clientes', { timeout: 4000 });
  const tNF = await waitToast(page);
  tNF.err ? ok('Clientes: ID inexistente → toast error') : warn('Clientes: ID inexistente sin toast error');

  // ─── ARTÍCULOS ───────────────────────────────────────────────────────────
  console.log('\n═══ ARTÍCULOS ═══');
  await page.goto(`${BASE}/web/articulos/nuevo`);
  await page.waitForLoadState('networkidle');
  await shot(page, 'articulos-formulario');

  for (const id of ['codigo','nombre','pvp','iva','coste','stock','alergenos']) {
    await page.locator(`#${id}`).count() > 0 ? ok(`Artículos form: #${id}`) : fail(`Artículos form: falta #${id}`);
  }

  await page.fill('#codigo', 'UI-ART-01');
  await page.fill('#nombre', 'Barra de Pan UI');
  await page.fill('#pvp', '1.20');
  await page.selectOption('#iva', '10');
  await submitForm(page);
  await page.waitForURL('**/articulos', { timeout: 6000 });
  const tArt = await waitToast(page);
  tArt.ok ? ok('Artículos: guardado OK') : fail(`Artículos: error "${tArt.msg}"`);
  await shot(page, 'articulos-guardado');

  // ─── PROVEEDORES ─────────────────────────────────────────────────────────
  console.log('\n═══ PROVEEDORES ═══');
  await page.goto(`${BASE}/web/proveedores/nuevo`);
  await page.waitForLoadState('networkidle');
  await shot(page, 'proveedores-formulario');

  for (const id of ['codigo','nombre']) {
    await page.locator(`#${id}`).count() > 0 ? ok(`Proveedores form: #${id}`) : fail(`Proveedores form: falta #${id}`);
  }
  await page.fill('#codigo', 'UI-PROV-01');
  await page.fill('#nombre', 'Harinas SA');
  if (await page.locator('#cif').count() > 0) await page.fill('#cif', 'B12345678');
  await submitForm(page);
  await page.waitForURL('**/proveedores', { timeout: 6000 });
  const tProv = await waitToast(page);
  tProv.ok ? ok('Proveedores: guardado OK') : fail(`Proveedores: error "${tProv.msg}"`);

  // ─── ALMACENES ───────────────────────────────────────────────────────────
  console.log('\n═══ ALMACENES ═══');
  await page.goto(`${BASE}/web/almacenes/nuevo`);
  await page.waitForLoadState('networkidle');
  await page.fill('#codigo', 'UI-ALM-01');
  await page.fill('#nombre', 'Almacén Principal UI');
  await submitForm(page);
  await page.waitForURL('**/almacenes', { timeout: 6000 });
  const tAlm = await waitToast(page);
  tAlm.ok ? ok('Almacenes: guardado OK') : fail(`Almacenes: error "${tAlm.msg}"`);

  // ─── FACTURAS ────────────────────────────────────────────────────────────
  console.log('\n═══ FACTURAS ═══');
  await page.goto(`${BASE}/web/facturas/nueva`);
  await page.waitForLoadState('networkidle');
  await shot(page, 'facturas-formulario');

  const selCliFac = await page.locator('[name=clienteId]').count();
  selCliFac > 0 ? ok('Facturas: selector clienteId presente') : fail('Facturas: falta selector clienteId');

  const fechaFac = await page.locator('[name=fecha]').count();
  fechaFac > 0 ? ok('Facturas: campo fecha presente') : fail('Facturas: falta campo fecha');

  // Botón añadir línea
  const addLineBtn = page.locator('#addLineBtn, button:has-text("Añadir línea"), .add-line-btn');
  await addLineBtn.count() > 0 ? ok('Facturas: botón añadir línea') : fail('Facturas: falta botón añadir línea');

  // Tabla de líneas
  const linesTable = await page.locator('.lines-table, #albaranLineasBody, #facturLineasBody, tbody').count();
  linesTable > 0 ? ok('Facturas: tabla de líneas presente') : warn('Facturas: tabla de líneas no encontrada');

  // Intentar añadir línea y guardar factura completa
  try {
    // Seleccionar el primer cliente disponible
    const clienteOptions = await page.locator('[name=clienteId] option').count();
    if (clienteOptions > 1) {
      await page.locator('[name=clienteId]').selectOption({ index: 1 });
      ok(`Facturas: ${clienteOptions - 1} clientes disponibles`);
    } else {
      warn('Facturas: no hay clientes en el selector');
    }

    // Añadir línea
    if (await addLineBtn.count() > 0) {
      await addLineBtn.first().click();
      await page.waitForTimeout(500);
      await shot(page, 'facturas-con-linea');

      // Rellenar línea
      const artSel = page.locator('[name=lineaArticuloId]').first();
      if (await artSel.count() > 0) {
        const artOpts = await artSel.locator('option').count();
        if (artOpts > 1) await artSel.selectOption({ index: 1 });
      }
      const cantInput = page.locator('[name=lineaCantidad]').first();
      if (await cantInput.count() > 0) await cantInput.fill('2');

      await submitForm(page);
      await page.waitForURL('**/facturas', { timeout: 8000 });
      const tFac = await waitToast(page);
      tFac.ok ? ok(`Facturas: creada con éxito`) : fail(`Facturas: error al crear — "${tFac.msg}"`);
      await shot(page, 'facturas-guardada');
    }
  } catch (e) {
    fail(`Facturas: excepción al crear — ${e.message.substring(0, 100)}`);
    await shot(page, 'facturas-error');
  }

  // ─── ALBARANES ───────────────────────────────────────────────────────────
  console.log('\n═══ ALBARANES ═══');
  await page.goto(`${BASE}/web/albaranes/nuevo`);
  await page.waitForLoadState('networkidle');
  await shot(page, 'albaranes-formulario');
  await page.locator('[name=clienteId]').count() > 0
    ? ok('Albaranes: selector cliente OK')
    : fail('Albaranes: falta selector cliente');

  // ─── PEDIDOS VENTA ───────────────────────────────────────────────────────
  console.log('\n═══ PEDIDOS VENTA ═══');
  await page.goto(`${BASE}/web/pedidos-venta/nuevo`);
  await page.waitForLoadState('networkidle');
  await shot(page, 'pedidos-formulario');
  await page.locator('[name=clienteId]').count() > 0
    ? ok('Pedidos venta: selector cliente OK')
    : fail('Pedidos venta: falta selector cliente');

  // ─── PRESUPUESTOS ────────────────────────────────────────────────────────
  console.log('\n═══ PRESUPUESTOS ═══');
  await page.goto(`${BASE}/web/presupuestos/nuevo`);
  await page.waitForLoadState('networkidle');
  await shot(page, 'presupuestos-formulario');
  await page.locator('[name=clienteId]').count() > 0
    ? ok('Presupuestos: selector cliente OK')
    : fail('Presupuestos: falta selector cliente');

  // ─── PRODUCCIÓN ──────────────────────────────────────────────────────────
  console.log('\n═══ PRODUCCIÓN ═══');
  for (const [ruta, nombre] of [
    ['/web/ordenes-produccion/nuevo', 'Orden producción'],
    ['/web/recetas/nueva', 'Receta'],
    ['/web/horneadas/nueva', 'Horneada'],
  ]) {
    try {
      await page.goto(`${BASE}${ruta}`);
      await page.waitForLoadState('networkidle');
      const cur = page.url();
      cur.includes('error') || cur.includes('login')
        ? fail(`${nombre}: redirigió a ${cur}`)
        : ok(`${nombre}: formulario carga OK`);
    } catch(e) {
      fail(`${nombre}: error — ${e.message.substring(0,80)}`);
    }
  }
  await shot(page, 'produccion-horneada');

  // ─── LISTAS PRINCIPALES ──────────────────────────────────────────────────
  console.log('\n═══ LISTAS ═══');
  const listas = [
    ['/web/ordenes-produccion','Órdenes producción'],
    ['/web/lotes','Lotes'],
    ['/web/mermas','Mermas'],
    ['/web/hojas-ruta','Hojas de ruta'],
    ['/web/rutas','Rutas'],
    ['/web/devoluciones','Devoluciones'],
    ['/web/facturas-compra','Facturas compra'],
    ['/web/pedidos-compra','Pedidos compra'],
    ['/web/recepciones','Recepciones'],
    ['/web/tesoreria','Tesorería'],
    ['/web/tesoreria/extractos','Extractos banco'],
    ['/web/contabilidad','Contabilidad'],
    ['/web/contabilidad/balance','Balance'],
    ['/web/modelo347','Modelo 347'],
    ['/web/reportes','Informes'],
    ['/web/empresa','Empresa'],
    ['/web/usuarios','Usuarios'],
    ['/web/backups','Backups'],
    ['/web/appcc','APPCC'],
    ['/web/vehiculos','Vehículos'],
    ['/web/verifactu','Verifactu'],
    ['/web/buscar?q=pan','Búsqueda'],
    ['/web/calendario','Calendario'],
    ['/web/planificador','Planificador'],
  ];
  for (const [ruta, nombre] of listas) {
    await page.goto(`${BASE}${ruta}`);
    await page.waitForLoadState('networkidle');
    const cur = page.url();
    if (cur.includes('login')) fail(`${nombre}: requiere login (sesión perdida?)`);
    else if (cur.includes('/error')) fail(`${nombre}: redirigió a página de error`);
    else ok(`${nombre}: OK (${cur.split('/web/')[1]?.split('?')[0] || 'root'})`);
  }
  await shot(page, 'tesoreria');
  await shot(page, 'reportes');

  // ─── RUTAS LEGACY ────────────────────────────────────────────────────────
  console.log('\n═══ LEGACY REDIRECTS ═══');
  for (const [from, to] of [
    ['/web/caja', 'tesoreria'],
    ['/web/asientos', 'contabilidad'],
    ['/web/backup', 'backups'],
  ]) {
    await page.goto(`${BASE}${from}`);
    await page.waitForLoadState('networkidle');
    page.url().includes(to)
      ? ok(`Legacy ${from} → ${to} ✓`)
      : fail(`Legacy ${from} → fue a ${page.url()} en vez de ${to}`);
  }

  // ─── BÚSQUEDA EN LISTAS (filtro inline) ──────────────────────────────────
  console.log('\n═══ FILTRO EN LISTAS ═══');
  await page.goto(`${BASE}/web/articulos`);
  await page.waitForLoadState('networkidle');
  const searchInput = page.locator('input[type=search], input[name^=tableSearch]').first();
  if (await searchInput.count() > 0) {
    await searchInput.fill('UI-ART-01');
    await page.waitForTimeout(400);
    const visibleRows = await page.locator('tbody tr:not([hidden])').count();
    await shot(page, 'articulos-filtro');
    ok(`Artículos: filtro inline muestra ${visibleRows} fila(s)`);
  } else {
    warn('Artículos: no se encontró input de búsqueda en lista');
  }

  // ─── NAVEGACIÓN SIDEBAR ──────────────────────────────────────────────────
  console.log('\n═══ SIDEBAR ═══');
  await page.goto(`${BASE}/web/dashboard`);
  await page.waitForLoadState('networkidle');
  const sidebar = page.locator('.sidebar');
  await sidebar.isVisible() ? ok('Sidebar: visible en desktop') : fail('Sidebar: no visible en desktop');

  // Comprobar ítem activo
  const activeLink = await page.locator('.sidebar-link.active').count();
  activeLink > 0 ? ok('Sidebar: ítem activo marcado') : warn('Sidebar: sin ítem activo');

  // Toggle en móvil
  await page.setViewportSize({ width: 375, height: 812 });
  await page.waitForTimeout(300);
  await shot(page, 'mobile-view');
  const toggleBtn = page.locator('#sidebarToggle');
  if (await toggleBtn.isVisible()) {
    await toggleBtn.click();
    await page.waitForTimeout(300);
    await shot(page, 'mobile-sidebar-open');
    ok('Sidebar: toggle móvil funciona');
  } else {
    warn('Sidebar: toggle móvil no visible');
  }
  await page.setViewportSize({ width: 1400, height: 900 });

  // ─── ERRORES JS ──────────────────────────────────────────────────────────
  console.log('\n═══ ERRORES JS ═══');
  const unique = [...new Set(jsErrors)];
  if (unique.length === 0) ok('Sin errores JavaScript en consola');
  else unique.forEach(e => fail(`JS: ${e}`));

  // ─── RESUMEN ─────────────────────────────────────────────────────────────
  await browser.close();
  console.log('\n══════════════════════════════════════════════════════');
  console.log(`RESUMEN — ${issues.length} problema(s) encontrado(s):`);
  console.log('══════════════════════════════════════════════════════');
  issues.length === 0 ? console.log('✅ Todo OK') : issues.forEach(i => console.log(i));
  writeFileSync(`${OUT}/issues.json`, JSON.stringify(issues, null, 2));
})();
