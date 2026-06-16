import { test, expect } from '@playwright/test';
import { createActiveVendorViaApi, getVendorToken, injectVendorSession, API_URL } from '../helpers/login.js';

// US-SEO-01 — Vendor configures the shop-wide SEO settings (title, description, keywords, OG image, canonical URL).

const VENDOR_EMAIL    = `vendor-seo01-${Date.now()}@shop.test`;
const VENDOR_PASSWORD = 'VendorSeo01!';

test.describe('US-SEO-01 — Shop-wide SEO configuration', () => {
  let vendorToken;

  test.beforeAll(async ({ browser }) => {
    const page = await browser.newPage();
    await createActiveVendorViaApi(page, VENDOR_EMAIL, VENDOR_PASSWORD);
    vendorToken = await getVendorToken(page, VENDOR_EMAIL, VENDOR_PASSWORD);
    await page.close();
  });

  test.beforeEach(async ({ page }) => {
    await injectVendorSession(page, VENDOR_EMAIL, vendorToken);
    await page.goto('/vendor/');
    await page.getByText('Référencement (SEO)').click();
    await expect(page.getByRole('heading', { name: 'Référencement naturel' })).toBeVisible();
  });

  // --- nominal: save global SEO settings ---

  test('nominal — saves SEO title and description and persists them on reload', async ({ page }) => {
    await page.getByText('SEO Global').click();

    await page.getByLabel('Titre SEO').fill('Ma Boutique de Photos');
    await page.getByLabel('Description').fill('Découvrez nos illustrations uniques.');
    await page.getByRole('button', { name: 'Enregistrer' }).click();

    await expect(page.getByText('Configuration enregistrée.')).toBeVisible();

    // Reload and verify persistence
    await page.reload();
    await page.getByText('Référencement (SEO)').click();
    await expect(page.getByLabel('Titre SEO')).toHaveValue('Ma Boutique de Photos');
    await expect(page.getByLabel('Description')).toHaveValue('Découvrez nos illustrations uniques.');
  });

  // --- nominal: save via API and verify response ---

  test('nominal — API PUT /api/vendor/seo returns saved configuration', async ({ page }) => {
    const res = await page.request.put(`${API_URL}/api/vendor/seo`, {
      headers: { Authorization: `Bearer ${vendorToken}`, 'Content-Type': 'application/json' },
      data: {
        seoTitle: 'SEO Title via API',
        seoDescription: 'Description via API',
        seoKeywords: 'photo, art, illustration',
        indexProducts: true,
        indexCatalog: true,
      },
    });
    expect(res.ok()).toBeTruthy();
    const body = await res.json();
    expect(body.seoTitle).toBe('SEO Title via API');
    expect(body.seoKeywords).toBe('photo, art, illustration');
  });

  // --- nominal: public endpoint reflects saved settings ---

  test('nominal — public endpoint returns saved SEO configuration', async ({ page }) => {
    await page.request.put(`${API_URL}/api/vendor/seo`, {
      headers: { Authorization: `Bearer ${vendorToken}`, 'Content-Type': 'application/json' },
      data: { seoTitle: 'Public SEO Title' },
    });

    const res = await page.request.get(`${API_URL}/api/public/seo`);
    expect(res.ok()).toBeTruthy();
    const body = await res.json();
    expect(body.seoTitle).toBe('Public SEO Title');
  });

  // --- nominal: meta tags appear in buyer portal home page ---

  test('nominal — SEO title appears in buyer portal page title', async ({ page }) => {
    await page.request.put(`${API_URL}/api/vendor/seo`, {
      headers: { Authorization: `Bearer ${vendorToken}`, 'Content-Type': 'application/json' },
      data: { seoTitle: 'Boutique Test SEO' },
    });

    await page.goto('/');
    await expect(page).toHaveTitle(/Boutique Test SEO/);
  });
});
