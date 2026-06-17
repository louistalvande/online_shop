import { test, expect } from '@playwright/test';
import { createActiveVendorViaApi, getVendorToken, injectVendorSession, API_URL } from '../helpers/login.js';

// US-SEO-04 — Vendor controls which pages can be indexed by search engines (indexation settings).

const VENDOR_EMAIL    = `vendor-seo04-${Date.now()}@shop.test`;
const VENDOR_PASSWORD = 'VendorSeo04!';

test.describe('US-SEO-04 — Indexation control', () => {
  let vendorToken;

  test.beforeAll(async ({ browser }) => {
    const page = await browser.newPage();
    await createActiveVendorViaApi(page, VENDOR_EMAIL, VENDOR_PASSWORD);
    vendorToken = await getVendorToken(page, VENDOR_EMAIL, VENDOR_PASSWORD);
    await page.close();
  });

  test.beforeEach(async ({ page }) => {
    await injectVendorSession(page, VENDOR_EMAIL, vendorToken);
  });

  // --- nominal: save indexation settings via API ---

  test('nominal — API saves indexProducts and indexCatalog flags', async ({ page }) => {
    const res = await page.request.put(`${API_URL}/api/vendor/seo`, {
      headers: { Authorization: `Bearer ${vendorToken}`, 'Content-Type': 'application/json' },
      data: { indexProducts: false, indexCatalog: false },
    });
    expect(res.ok()).toBeTruthy();
    const body = await res.json();
    expect(body.indexProducts).toBe(false);
    expect(body.indexCatalog).toBe(false);
  });

  // --- nominal: noindex on catalog page when indexCatalog is false ---

  test('nominal — catalog page has noindex meta tag when indexCatalog is false', async ({ page }) => {
    await page.request.put(`${API_URL}/api/vendor/seo`, {
      headers: { Authorization: `Bearer ${vendorToken}`, 'Content-Type': 'application/json' },
      data: { indexCatalog: false },
    });

    await page.goto('/catalog');
    const robots = await page.locator('meta[name="robots"]').getAttribute('content');
    expect(robots).toContain('noindex');
  });

  // --- nominal: robots.txt disallows /catalog/ when indexProducts is false ---

  test('nominal — robots.txt disallows product pages when indexProducts is false', async ({ page }) => {
    await page.request.put(`${API_URL}/api/vendor/seo`, {
      headers: { Authorization: `Bearer ${vendorToken}`, 'Content-Type': 'application/json' },
      data: { indexProducts: false },
    });

    const res = await page.request.get(`${API_URL}/api/public/robots.txt`);
    const body = await res.text();
    expect(body).toContain('Disallow: /catalog/');
  });

  // --- nominal: indexation tab is visible in vendor UI ---

  test('nominal — indexation tab is accessible in the SEO page', async ({ page }) => {
    await page.goto('/vendor/');
    await page.getByText('Référencement (SEO)').click();
    await page.getByText('Indexation').click();

    await expect(page.getByLabel('Indexer les pages produits')).toBeVisible();
    await expect(page.getByLabel('Indexer la page catalogue')).toBeVisible();
    await expect(page.getByLabel('Indexer les pages compte')).toBeVisible();
    await expect(page.getByLabel('Indexer la page panier')).toBeVisible();
  });

  // --- nominal: indexAccount/indexCart flags saved via API ---

  test('nominal — API saves indexAccount and indexCart flags', async ({ page }) => {
    const res = await page.request.put(`${API_URL}/api/vendor/seo`, {
      headers: { Authorization: `Bearer ${vendorToken}`, 'Content-Type': 'application/json' },
      data: { indexAccount: false, indexCart: false },
    });
    expect(res.ok()).toBeTruthy();
    const body = await res.json();
    expect(body.indexAccount).toBe(false);
    expect(body.indexCart).toBe(false);
  });

  // --- nominal: robots.txt disallows /account and /cart when index flags are false ---

  test('nominal — robots.txt disallows /account and /cart when index flags are false', async ({ page }) => {
    await page.request.put(`${API_URL}/api/vendor/seo`, {
      headers: { Authorization: `Bearer ${vendorToken}`, 'Content-Type': 'application/json' },
      data: { indexAccount: false, indexCart: false },
    });

    const res = await page.request.get(`${API_URL}/api/public/robots.txt`);
    const body = await res.text();
    expect(body).toContain('Disallow: /account');
    expect(body).toContain('Disallow: /cart');
  });

  // --- nominal: sitemap.xml has Cache-Control: public header ---

  test('nominal — sitemap.xml response includes Cache-Control: public, max-age=3600', async ({ page }) => {
    const res = await page.request.get(`${API_URL}/api/public/sitemap.xml`);
    expect(res.ok()).toBeTruthy();
    const cacheControl = res.headers()['cache-control'];
    expect(cacheControl).toContain('max-age=3600');
    expect(cacheControl).toContain('public');
  });

  // --- nominal: robots.txt has Cache-Control: public header ---

  test('nominal — robots.txt response includes Cache-Control: public, max-age=3600', async ({ page }) => {
    const res = await page.request.get(`${API_URL}/api/public/robots.txt`);
    expect(res.ok()).toBeTruthy();
    const cacheControl = res.headers()['cache-control'];
    expect(cacheControl).toContain('max-age=3600');
    expect(cacheControl).toContain('public');
  });
});
