import { chromium } from '@playwright/test';
import { resetDatabase } from './helpers/db-reset.js';

const VENDOR_URL = process.env.VENDOR_URL ?? 'http://vendor-backoffice';
const ADMIN_URL  = process.env.ADMIN_URL  ?? 'http://admin-console';
const BUYER_URL  = process.env.BUYER_URL  ?? 'http://buyer-portal';

async function warmUp(browser, url, label) {
  const deadline = Date.now() + 180_000;
  while (Date.now() < deadline) {
    const page = await browser.newPage();
    try {
      await page.goto(url, { timeout: 60_000, waitUntil: 'networkidle' });
      await page.waitForSelector('body > *', { timeout: 10_000 });
      console.log(`[setup] ${label} warm`);
      return;
    } catch {
      console.log(`[setup] ${label} not ready — retrying in 5 s`);
    } finally {
      await page.close().catch(() => {});
    }
    await new Promise(r => setTimeout(r, 5_000));
  }
  console.warn(`[setup] ${label} did not respond within 3 min — continuing anyway`);
}

export default async function globalSetup() {
  // Reset DB before tests to guarantee a clean starting state even if a previous
  // run crashed before the teardown could execute.
  await resetDatabase();

  const browser = await chromium.launch();
  try {
    await warmUp(browser, VENDOR_URL, 'vendor-backoffice');
    await warmUp(browser, ADMIN_URL,  'admin-console');
    await warmUp(browser, BUYER_URL,  'buyer-portal');
  } finally {
    await browser.close();
  }
  console.log('[setup] all SPAs warmed up');
}
