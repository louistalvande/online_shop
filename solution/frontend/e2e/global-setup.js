import { chromium } from '@playwright/test';

const VENDOR_URL = process.env.VENDOR_URL ?? 'http://vendor-backoffice';
const ADMIN_URL  = process.env.ADMIN_URL  ?? 'http://admin-console';
const BUYER_URL  = process.env.BUYER_URL  ?? 'http://buyer-portal';

// Force each SPA to compile its entry point so the first real test never waits on a cold Vite.
export default async function globalSetup() {
  const browser = await chromium.launch();
  try {
    await Promise.all([VENDOR_URL, ADMIN_URL, BUYER_URL].map(async (url) => {
      const page = await browser.newPage();
      try {
        await page.goto(url, { timeout: 90000, waitUntil: 'domcontentloaded' });
      } catch (_) {
        // Best-effort — a warm-up failure must not block the test run.
      } finally {
        await page.close();
      }
    }));
  } finally {
    await browser.close();
  }
  console.log('[setup] SPAs warmed up');
}
