/**
 * Test de flujos principales del ERP Tahona
 * Ejecutar DESPUÉS de que la app esté completamente iniciada:
 *   node test-erp-flows.mjs
 */

import { chromium } from 'playwright';

const BASE  = 'http://localhost:8080';
const RESULTS = [];

function ok(test, msg)   { RESULTS.push({test, ok: true,  msg}); console.log(`  ✓ ${msg}`); }
function fail(test, msg) { RESULTS.push({test, ok: false, msg}); console.error(`  ✗ ${msg}`); }

// ─── Login helper con reintento ───────────────────
async function login(page) {
  await page.goto(`${BASE}/web/login`);
  await page.waitForLoadState('load');

  const pairs = [['admin','admin'],['admin','1234'],['tahona','tahona'],['admin','password']];
  for (const [u,p] of pairs) {
    const userField = page.locator('input[name="username"], input[name="email"]').first();
    const passField = page.locator('input[type="password"]').first();
    if (await userField.count() === 0) break;
    await userField.fill(u);
    await passField.fill(p);
    await page.locator('button.btn-primary[type="submit"], .form-card button[type="submit"]').first().click();
    await page.waitForTimeout(1200);
    if (!page.url().includes('/login')) {
      console.log(`  → Sesión iniciada con ${u}/${p}`);
      return true;
    }
    await page.goto(`${BASE}/web/login`);
    await page.waitForTimeout(400);
  }
  return false;
}

// Garantiza sesión activa antes de cada test
async function ensureLoggedIn(page) {
  await page.goto(`${BASE}/web/dashboard`);
  await page.waitForTimeout(500);
  if (page.url().includes('/login')) {
    await login(page);
  }
}

// ─── TEST 1: Crear cliente ────────────────────────
async function testCrearCliente(page) {
  console.log('\n── TEST 1: Crear cliente ─────────────────────');
  await ensureLoggedIn(page);

  try {
    await page.goto(`${BASE}/web/clientes/nuevo`);
    await page.waitForLoadState('load');

    if (page.url().includes('/login')) {
      fail('cliente', 'Redirigido a login al acceder al formulario — sesión inválida'); return;
    }

    // Verificar campos requeridos por RD 1619/2012 art.6 (NIF del destinatario)
    const tieneCif = await page.locator('input[name="cif"]').count() > 0;
    if (tieneCif) ok('cliente', 'Campo CIF/NIF presente (RD 1619/2012 art.6.1.d)');
    else          fail('cliente', 'FALTA campo CIF/NIF — necesario para facturas españolas');

    // Rellenar formulario — nombres sin caracteres especiales para evitar problemas de encoding
    const fills = [
      ['codigo',      'CLI001'],
      ['nombre',      'Pasteleria Rodriguez SL'],
      ['cif',         'B87654321'],
      ['telefono',    '912345678'],
      ['email',       'info@rodriguez.es'],
      ['direccion',   'Calle Gran Via 55'],
      ['poblacion',   'Madrid'],
      ['codigoPostal','28013'],
    ];
    for (const [name, val] of fills) {
      const f = page.locator(`input[name="${name}"], textarea[name="${name}"]`).first();
      if (await f.count() > 0) await f.fill(val);
    }

    await page.locator('button.btn-primary[type="submit"], .form-card button[type="submit"]').first().click();
    await page.waitForTimeout(2000);

    const url = page.url();
    if (!url.includes('/nuevo') && !url.includes('/login')) {
      ok('cliente', `Cliente guardado — redirige a: ${url}`);
    } else if (url.includes('/login')) {
      fail('cliente', 'Sesión perdida al guardar cliente');
    } else {
      const content = await page.content();
      const errMsg  = content.match(/class="alert[^"]*"[^>]*>\s*([^<]{5,100})/);
      fail('cliente', `Error al guardar. Mensaje: ${errMsg ? errMsg[1].trim() : '(sin mensaje)'}`);
    }

    // Verificar en lista
    await page.goto(`${BASE}/web/clientes`);
    await page.waitForLoadState('load');
    const lista = await page.content();
    if (lista.includes('CLI001') || lista.includes('Rodriguez') || lista.includes('Rodriguez')) {
      ok('cliente', 'Cliente aparece en la lista');
    } else {
      fail('cliente', 'Cliente NO aparece en la lista (puede ser paginacion o error de guardado)');
    }

  } catch(e) { fail('cliente', `Excepcion: ${e.message}`); }
}

// ─── TEST 2: Crear artículo con IVA español ───────
async function testCrearArticulo(page) {
  console.log('\n── TEST 2: Crear articulo ─────────────────────');
  await ensureLoggedIn(page);

  try {
    await page.goto(`${BASE}/web/articulos/nuevo`);
    await page.waitForLoadState('load');

    if (page.url().includes('/login')) {
      fail('articulo', 'Redirigido a login'); return;
    }

    // Verificar tipos IVA válidos en España (Ley 37/1992 LIVA)
    const ivaSelect = page.locator('select[name="iva"]');
    const ivaInput  = page.locator('input[name="iva"]');
    if (await ivaSelect.count() > 0) {
      const vals = await ivaSelect.locator('option').evaluateAll(o => o.map(x => x.value));
      const tiene10 = vals.includes('10') || vals.includes('10.0');
      if (tiene10) ok('articulo', `Select IVA con tasas validas: ${vals.join(', ')}% — pan=10% correcto`);
      else         fail('articulo', `Select IVA sin tasa 10% — pan artesano deberia ser tipo reducido`);
    } else if (await ivaInput.count() > 0) {
      ok('articulo', 'Campo IVA presente (input numerico)');
    } else {
      fail('articulo', 'FALTA campo IVA — requerido por Ley 37/1992 LIVA');
    }

    // Rellenar
    const fills = [
      ['codigo',  'ART001'],
      ['nombre',  'Pan Artesano 500g'],
      ['pvp',     '2.50'],
      ['coste',   '0.80'],
    ];
    for (const [n,v] of fills) {
      const f = page.locator(`input[name="${n}"]`).first();
      if (await f.count() > 0) await f.fill(v);
    }

    // Seleccionar IVA 10% (tipo reducido — pan, bolleria)
    if (await ivaSelect.count() > 0) {
      const opts = await ivaSelect.locator('option').evaluateAll(o => o.map(x => x.value));
      if (opts.includes('10')) await ivaSelect.selectOption('10');
    } else if (await ivaInput.count() > 0) {
      await ivaInput.fill('10');
    }

    await page.locator('button.btn-primary[type="submit"], .form-card button[type="submit"]').first().click();
    await page.waitForTimeout(2000);

    const url = page.url();
    if (!url.includes('/nuevo') && !url.includes('/login')) {
      ok('articulo', 'Articulo guardado correctamente con IVA 10%');
    } else if (url.includes('/login')) {
      fail('articulo', 'Sesion perdida al guardar articulo');
    } else {
      fail('articulo', `Error al guardar articulo. URL: ${url}`);
    }

    // Verificar en lista
    await page.goto(`${BASE}/web/articulos`);
    await page.waitForLoadState('load');
    const lista = await page.content();
    if (lista.includes('ART001') || lista.includes('Pan Artesano')) {
      ok('articulo', 'Articulo aparece en la lista');
    } else {
      fail('articulo', 'Articulo NO aparece en la lista');
    }

  } catch(e) { fail('articulo', `Excepcion: ${e.message}`); }
}

// ─── TEST 3: Crear albarán ────────────────────────
async function testCrearAlbaran(page) {
  console.log('\n── TEST 3: Crear albaran ─────────────────────');
  await ensureLoggedIn(page);

  try {
    await page.goto(`${BASE}/web/albaranes/nuevo`);
    await page.waitForLoadState('load');

    if (page.url().includes('/login')) {
      fail('albaran', 'Redirigido a login al acceder al formulario'); return;
    }

    const content = await page.content();

    // Verificar estructura del formulario
    const checks = [
      [content.includes('clienteId'), 'Campo cliente presente'],
      [content.includes('fecha'),     'Campo fecha presente'],
      [content.includes('addLine') || content.includes('Anadir') || content.includes('Añadir'), 'Boton anadir linea presente'],
    ];
    for (const [pass, msg] of checks) {
      pass ? ok('albaran', msg) : fail('albaran', `FALTA: ${msg}`);
    }

    // Seleccionar cliente
    const clienteSel = page.locator('select[name="clienteId"]');
    if (await clienteSel.count() > 0) {
      const opts = await clienteSel.locator('option').all();
      if (opts.length > 1) {
        await clienteSel.selectOption({index: 1});
        const text = await clienteSel.inputValue();
        ok('albaran', `Cliente seleccionado (id=${text})`);
      } else {
        fail('albaran', 'Sin clientes disponibles — crear un cliente primero');
      }
    }

    // Fecha hoy
    const today = new Date().toISOString().split('T')[0];
    const fechaF = page.locator('input[name="fecha"]').first();
    if (await fechaF.count() > 0) await fechaF.fill(today);

    // Esperar JS del formulario
    await page.waitForTimeout(800);

    // Probar añadir línea de producto
    const addBtn = page.locator('#addLineBtn').first();
    if (await addBtn.count() > 0) {
      await addBtn.click();
      await page.waitForTimeout(600);

      const prodSels = page.locator('select[name="lineaArticuloId"]');
      const nRows    = await prodSels.count();
      ok('albaran', `Fila añadida al hacer clic en "Añadir producto" (${nRows} fila/s)`);

      if (nRows > 0) {
        const opts = await prodSels.first().locator('option').all();
        if (opts.length > 1) {
          await prodSels.first().selectOption({index: 1});
          await page.waitForTimeout(500); // esperar auto-relleno

          // Verificar auto-relleno de precio (articulosData JS map)
          const precioVal = await page.locator('.line-precio').first().inputValue();
          if (precioVal && precioVal !== '' && precioVal !== '0') {
            ok('albaran', `Precio auto-rellenado desde articulo: ${precioVal}`);
          } else {
            fail('albaran', `Precio NO auto-rellenado (valor: "${precioVal}")`);
          }

          // Poner cantidad
          const cantInput = page.locator('.line-cantidad').first();
          await cantInput.fill('5');
          await page.waitForTimeout(200);

          // Comprobar total
          const totalText = await page.locator('#totalDisplay').textContent();
          if (totalText && totalText !== '0,00 €' && totalText !== '—') {
            ok('albaran', `Total calculado correctamente: ${totalText.trim()}`);
          } else {
            fail('albaran', `Total NO calculado (muestra: "${totalText}")`);
          }

        } else {
          fail('albaran', 'Sin productos disponibles en el selector — crear articulo primero');
        }
      }
    } else {
      fail('albaran', 'No se encuentra boton #addLineBtn');
    }

    // Guardar
    await page.locator('button.btn-primary[type="submit"], .form-card button[type="submit"]').first().click();
    await page.waitForTimeout(2500);

    const urlPost = page.url();
    if (urlPost.includes('/login')) {
      fail('albaran', 'Sesion perdida al guardar albaran');
    } else if (urlPost.match(/\/web\/albaranes\/\d+$/)) {
      ok('albaran', `Albaran guardado — redirige al detalle: ${urlPost}`);

      // Verificar numero de albaran en el detalle
      const det = await page.content();
      const num = det.match(/ALB[^\s<"]+/) || det.match(/\d{4}[-\/]\d+/);
      if (num) ok('albaran', `Numero de albaran generado: ${num[0]}`);
      else     fail('albaran', 'Numero de albaran no visible en el detalle');

    } else if (!urlPost.includes('/nuevo')) {
      ok('albaran', `Albaran guardado — redirige a: ${urlPost}`);
    } else {
      const errContent = await page.content();
      const errMsg = errContent.match(/class="alert[^"]*"[^>]*>\s*([^<]{5,200})/);
      fail('albaran', `Error al guardar. ${errMsg ? errMsg[1].trim() : '(sin mensaje de error)'}`);
    }

  } catch(e) { fail('albaran', `Excepcion: ${e.message}`); }
}

// ─── TEST 4: Crear factura (RD 1619/2012) ─────────
async function testCrearFactura(page) {
  console.log('\n── TEST 4: Crear factura ──────────────────────');
  await ensureLoggedIn(page);

  try {
    await page.goto(`${BASE}/web/facturas/nuevo`);
    await page.waitForLoadState('load');

    if (page.url().includes('/login')) {
      fail('factura', 'Redirigido a login al acceder al formulario'); return;
    }

    const content = await page.content();

    // Art. 6 RD 1619/2012 — Contenido obligatorio de facturas
    const ley = [
      [content.includes('fecha'),                                   'Fecha expedicion (art.6.1.a)'],
      [content.includes('clienteId'),                               'Datos destinatario (art.6.1.d)'],
      [content.includes('lineaArticuloId') || content.includes('addLine'), 'Descripcion operaciones (art.6.1.e)'],
      [content.includes('lineaIva') || content.includes('IVA'),    'Tipo impositivo IVA (art.6.1.g)'],
      [content.includes('totalDisplay') || content.includes('total'), 'Cuota tributaria/Total (art.6.1.h)'],
      [content.includes('baseDisplay'),                             'Desglose base imponible (art.6.1.f)'],
    ];
    for (const [pass, msg] of ley) {
      pass ? ok('factura', msg) : fail('factura', `FALTA: ${msg}`);
    }

    // Verificar medio de cobro (buena practica AEAT)
    if (content.includes('medioCobro')) ok('factura', 'Campo medio de cobro presente');

    // Seleccionar cliente
    const clienteSel = page.locator('select[name="clienteId"]');
    if (await clienteSel.count() > 0) {
      const opts = await clienteSel.locator('option').all();
      if (opts.length > 1) await clienteSel.selectOption({index: 1});
    }

    // Fecha
    const today = new Date().toISOString().split('T')[0];
    await page.locator('input[name="fecha"]').first().fill(today);

    // Medio de cobro
    const medioSel = page.locator('select[name="medioCobro"]');
    if (await medioSel.count() > 0) await medioSel.selectOption('TRANSFERENCIA');

    // Esperar JS
    await page.waitForTimeout(800);

    // Añadir línea
    const addBtn = page.locator('#addLineBtn').first();
    if (await addBtn.count() > 0) {
      await addBtn.click();
      await page.waitForTimeout(600);

      const prodSels = page.locator('select[name="lineaArticuloId"]');
      if (await prodSels.count() > 0) {
        const opts = await prodSels.first().locator('option').all();
        if (opts.length > 1) {
          await prodSels.first().selectOption({index: 1});
          await page.waitForTimeout(500);

          // Precio y IVA auto-relleno
          const precioVal = await page.locator('.line-precio').first().inputValue();
          const ivaVal    = await page.locator('.line-iva').first().inputValue();

          if (precioVal && precioVal !== '' && precioVal !== '0') {
            ok('factura', `Precio auto-rellenado: ${precioVal} EUR`);
          } else {
            fail('factura', 'Precio NO auto-rellenado en factura');
          }

          if (ivaVal && ivaVal !== '' && ivaVal !== '0') {
            ok('factura', `IVA auto-rellenado: ${ivaVal}%`);
          } else {
            fail('factura', `IVA NO auto-rellenado (valor: "${ivaVal}")`);
          }

          await page.locator('.line-cantidad').first().fill('10');
          await page.waitForTimeout(400);

          // Desglose IVA (obligatorio art.6.1.f-h)
          const base  = await page.locator('#baseDisplay').textContent().catch(()=>'?');
          const iva   = await page.locator('#ivaDisplay').textContent().catch(()=>'?');
          const total = await page.locator('#totalDisplay').textContent().catch(()=>'?');

          if (base !== '0,00 €' && base !== '?' && iva !== '0,00 €') {
            ok('factura', `Desglose IVA correcto — Base: ${base} | IVA: ${iva} | Total: ${total}`);
          } else {
            fail('factura', `Desglose IVA incompleto — Base: ${base} | IVA: ${iva} | Total: ${total}`);
          }
        }
      }
    }

    // Guardar
    await page.locator('button.btn-primary[type="submit"], .form-card button[type="submit"]').first().click();
    await page.waitForTimeout(2500);

    const urlPost = page.url();
    if (urlPost.includes('/login')) {
      fail('factura', 'Sesion perdida al guardar factura');
    } else if (urlPost.match(/\/web\/facturas\/\d+$/)) {
      ok('factura', `Factura guardada — redirige al detalle: ${urlPost}`);

      const det = await page.content();

      // Numero correlativo obligatorio art.6.1.b
      const num = det.match(/FAC[^\s<"]+/) || det.match(/Factura\s+[\w\/\-]+/i);
      if (num) ok('factura', `Numero de factura asignado: ${num[0].trim()} (numeracion correlativa art.6.1.b)`);
      else     fail('factura', 'Numero de factura no visible — requerido art.6.1.b RD 1619/2012');

      // Estado BORRADOR (primer estado antes de emitir)
      if (det.includes('BORRADOR') || det.includes('borrador') || det.includes('DRAFT')) {
        ok('factura', 'Estado BORRADOR correcto (flujo: BORRADOR -> EMITIDA)');
      }

      // VeriFactu
      if (det.includes('VeriFactu') || det.includes('verifactu') || det.includes('CSV') || det.includes('csv')) {
        ok('factura', 'Soporte VeriFactu/CSV detectado ✓');
      }

      // Boton Emitir
      const emitirBtn = page.locator('form[action*="/emitir"] button, button:has-text("Emitir")').first();
      if (await emitirBtn.count() > 0) {
        ok('factura', 'Boton "Emitir" disponible en la factura borrador');

        // TEST EMITIR
        await emitirBtn.click();
        await page.waitForTimeout(2000);
        const urlEmit = page.url();

        if (urlEmit.match(/\/web\/facturas\/\d+$/)) {
          ok('factura', `Emitir redirige al detalle de la factura (correcto)`);
        } else {
          fail('factura', `Emitir redirige a: ${urlEmit} (deberia ser el detalle)`);
        }

        const detEmit = await page.content();
        if (detEmit.includes('EMITIDA') || detEmit.includes('emitida')) {
          ok('factura', 'Estado cambiado a EMITIDA correctamente');
        } else if (detEmit.toLowerCase().includes('exito') || detEmit.toLowerCase().includes('guardada')) {
          ok('factura', 'Mensaje de exito tras emitir');
        } else {
          fail('factura', 'Estado no cambia a EMITIDA tras emitir');
        }

      } else {
        fail('factura', 'Boton "Emitir" no encontrado en la factura');
      }

    } else if (!urlPost.includes('/nuevo')) {
      ok('factura', `Factura guardada — redirige a: ${urlPost}`);
    } else {
      const errContent = await page.content();
      const errMsg = errContent.match(/class="alert[^"]*"[^>]*>\s*([^<]{5,200})/);
      fail('factura', `Error al guardar factura. ${errMsg ? errMsg[1].trim() : '(sin mensaje)'}`);
    }

  } catch(e) { fail('factura', `Excepcion: ${e.message}`); }
}

// ─── TEST 5: Verificar datos empresa (emisor obligatorio) ──
async function testConfigEmpresa(page) {
  console.log('\n── TEST 5: Configuracion empresa ──────────────');
  await ensureLoggedIn(page);

  try {
    await page.goto(`${BASE}/web/empresa`);
    await page.waitForLoadState('load');

    if (page.url().includes('/login')) { fail('empresa', 'Sin acceso a config empresa'); return; }

    const content = await page.content();

    // Datos del emisor obligatorios en facturas (art.6.1.c RD 1619/2012)
    const campos = [
      [content.includes('razon') || content.includes('nombre'), 'Nombre/Razon social emisor (art.6.1.c)'],
      [content.includes('cif') || content.includes('nif'),  'NIF/CIF emisor (art.6.1.c)'],
      [content.includes('direccion') || content.includes('domicilio'), 'Domicilio fiscal emisor (art.6.1.c)'],
    ];
    for (const [pass, msg] of campos) {
      pass ? ok('empresa', msg) : fail('empresa', `FALTA: ${msg}`);
    }

    // VeriFactu config
    if (content.includes('verifactu') || content.includes('VeriFactu') || content.includes('software')) {
      ok('empresa', 'Configuracion VeriFactu presente (Reglamento Facturacion AEAT)');
    }

  } catch(e) { fail('empresa', `Excepcion: ${e.message}`); }
}

// ─── MAIN ─────────────────────────────────────────
(async () => {
  console.log('═══════════════════════════════════════════════');
  console.log('  ERP Tahona — Test flujos principales');
  console.log('  Marco legal: RD 1619/2012, Ley 37/1992 LIVA,');
  console.log('               Ley 58/2003 LGT, Reglamento VeriFactu');
  console.log('═══════════════════════════════════════════════');

  const browser = await chromium.launch({
    headless: false,
    slowMo: 60,
    args: ['--window-size=1400,900'],
    executablePath: process.env.PLAYWRIGHT_CHROMIUM_EXECUTABLE_PATH,
  });
  const context = await browser.newContext({ viewport: { width: 1400, height: 900 } });
  const page    = await context.newPage();

  const jsErrors = [];
  page.on('pageerror', e => jsErrors.push(e.message));
  page.on('console',  m => { if (m.type() === 'error') jsErrors.push(`[JS] ${m.text()}`); });

  try {
    console.log('\n── LOGIN ──────────────────────────────────────');
    const logged = await login(page);
    if (!logged) {
      console.error('  ✗ Login fallido. Verifica credenciales.');
      await browser.close(); process.exit(1);
    }
    ok('login', `Sesion activa en ${page.url()}`);

    await testCrearCliente(page);
    await testCrearArticulo(page);
    await testCrearAlbaran(page);
    await testCrearFactura(page);
    await testConfigEmpresa(page);

  } catch(e) {
    console.error('\nError inesperado en el test runner:', e.message);
  }

  // Errores JS capturados
  const errorsFiltered = jsErrors.filter(e => !e.includes('favicon') && !e.includes('404'));
  if (errorsFiltered.length > 0) {
    console.log('\n── ERRORES JS EN PAGINA ───────────────────────');
    errorsFiltered.slice(0,8).forEach(e => console.error(`  ✗ ${e}`));
  }

  // Resumen final
  const pass   = RESULTS.filter(r => r.ok).length;
  const total  = RESULTS.length;
  const groups = {};
  RESULTS.forEach(r => {
    if (!groups[r.test]) groups[r.test] = {pass:0, fail:0, msgs:[]};
    r.ok ? groups[r.test].pass++ : groups[r.test].fail++;
    if (!r.ok) groups[r.test].msgs.push(r.msg);
  });

  console.log('\n═══════════════════════════════════════════════');
  console.log('  RESUMEN');
  console.log('═══════════════════════════════════════════════');
  for (const [g, s] of Object.entries(groups)) {
    const icon = s.fail === 0 ? '✓' : '✗';
    console.log(`  ${icon} ${g.padEnd(10)}: ${s.pass}/${s.pass+s.fail} — ${s.fail > 0 ? s.msgs.join(' | ') : 'OK'}`);
  }
  console.log(`\n  Total: ${pass}/${total} checks pasados`);
  if (pass === total) console.log('  ✓ Todos los flujos funcionan correctamente');
  console.log('═══════════════════════════════════════════════');

  await page.waitForTimeout(1500);
  await browser.close();
  process.exit(pass === total ? 0 : 1);
})();
