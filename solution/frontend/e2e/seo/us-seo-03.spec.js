import { test, expect } from '@playwright/test';
import { createActiveVendorViaApi, getVendorToken, API_URL } from '../helpers/login.js';

// US-SEO-03 — Dynamic sitemap.xml and robots.txt generation.

const VENDOR_EMAIL    = `vendor-seo03-${Date.now()}@shop.test`;
const VENDOR_PASSWORD = 'VendorSeo03!';

test.describe('US-SEO-03 — Sitemap and robots.txt generation', () => {
  let vendorToken;

  test.beforeAll(async ({ browser }) => {
    const page = await browser.newPage();
    await createActiveVendorViaApi(page, VENDOR_EMAIL, VENDOR_PASSWORD);
    vendorToken = await getVendorToken(page, VENDOR_EMAIL, VENDOR_PASSWORD);
    await page.close();
  });

  // --- nominal: sitemap returns valid XML ---

  test('nominal — GET /api/public/sitemap.xml returns valid XML sitemap', async ({ page }) => {
    const res = await page.request.get(`${API_URL}/api/public/sitemap.xml`);
    expect(res.ok()).toBeTruthy();
    const body = await res.text();
    expect(body).toContain('<?xml');
    expect(body).toContain('<urlset');
    expect(body).toContain('</urlset>');
  });

  // --- nominal: robots.txt contains User-agent and Sitemap ---

  test('nominal — GET /api/public/robots.txt returns valid robots.txt', async ({ page }) => {
    const res = await page.request.get(`${API_URL}/api/public/robots.txt`);
    expect(res.ok()).toBeTruthy();
    const body = await res.text();
    expect(body).toContain('User-agent: *');
    expect(body).toContain('Sitemap:');
  });

  // --- nominal: custom disallow paths appear in robots.txt ---

  test('nominal — custom disallow paths appear in robots.txt', async ({ page }) => {
    await page.request.put(`${API_URL}/api/vendor/seo`, {
      headers: { Authorization: `Bearer ${vendorToken}`, 'Content-Type': 'application/json' },
      data: { robotsDisallowPaths: '/cart\n/checkout' },
    });

    const res = await page.request.get(`${API_URL}/api/public/robots.txt`);
    const body = await res.text();
    expect(body).toContain('Disallow: /cart');
    expect(body).toContain('Disallow: /checkout');
  });

  // --- nominal: sitemap includes /catalog when indexCatalog is true ---

  test('nominal — sitemap includes /catalog when indexCatalog is true', async ({ page }) => {
    await page.request.put(`${API_URL}/api/vendor/seo`, {
      headers: { Authorization: `Bearer ${vendorToken}`, 'Content-Type': 'application/json' },
      data: { indexCatalog: true, indexProducts: true },
    });

    const res = await page.request.get(`${API_URL}/api/public/sitemap.xml`);
    const body = await res.text();
    expect(body).toContain('/catalog');
  });

  // --- nominal: sitemap excludes /catalog when indexCatalog is false ---

  test('nominal — sitemap excludes /catalog when indexCatalog is false', async ({ page }) => {
    await page.request.put(`${API_URL}/api/vendor/seo`, {
      headers: { Authorization: `Bearer ${vendorToken}`, 'Content-Type': 'application/json' },
      data: { indexCatalog: false, indexProducts: false },
    });

    const res = await page.request.get(`${API_URL}/api/public/sitemap.xml`);
    const body = await res.text();
    expect(body).not.toContain('/catalog');
  });
});
