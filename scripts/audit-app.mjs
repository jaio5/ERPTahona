import { chromium } from 'playwright';
const BASE = 'http://localhost:8080';
const browser = await chromium.launch({ headless: true });
const context = await browser.newContext({ baseURL: BASE });
const page = await context.newPage();

// Login
await page.goto(BASE + '/web/login');
await page.fill('[name="username"]', 'admin');
await page.fill('[name="password"]', 'admin');
await page.click('[type="submit"]');
await page.waitForURL('**/dashboard', { timeout: 5000 });

// Test old /web/app SPA route
const errorsWebApp = [];
page.on('console', msg => { if (msg.type() === 'error') errorsWebApp.push(msg.text().slice(0,120)); });
page.on('pageerror', err => { errorsWebApp.push('[pageerror] ' + err.message.slice(0,120)); });

await page.goto(BASE + '/web/app').catch(() => {});
await page.waitForTimeout(2000);
console.log('/web/app JS ERRORS:', errorsWebApp.length);
errorsWebApp.forEach(e => console.log('  ' + e));

// Test /web/ root (should redirect to what?)
const resp = await context.request.get(BASE + '/web/', { maxRedirects: 0 }).catch(() => null);
if (resp) console.log('/web/ -> status:', resp.status(), 'location:', resp.headers()['location']);

// Test recepciones (in old SPA module list but has new controller)
page.removeAllListeners('console'); page.removeAllListeners('pageerror');
const errorsRec = [];
page.on('console', msg => { if (msg.type() === 'error') errorsRec.push(msg.text().slice(0,120)); });
await page.goto(BASE + '/web/recepciones').catch(() => {});
await page.waitForTimeout(500);
const recStatus = await context.request.get(BASE + '/web/recepciones').then(r => r.status());
console.log('/web/recepciones status:', recStatus, 'JS errors:', errorsRec.length);
if (errorsRec.length) errorsRec.forEach(e => console.log('  ' + e));

await browser.close();
