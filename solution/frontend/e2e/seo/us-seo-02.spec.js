import { test, expect } from '@playwright/test';
import { createActiveVendorViaApi, getVendorToken, injectVendorSession, API_URL } from '../helpers/login.js';

// US-SEO-02 — Vendor configures per-product SEO override (title, description, keywords, OG image).

const VENDOR_EMAIL    = `vendor-seo02-${Date.now()}@shop.test`;
const VENDOR_PASSWORD = 'VendorSeo02!';

async function createProduct(page, token) {
  const res = await page.request.post(`${API_URL}/api/vendor/catalog`, {
    headers: { Authorization: `Bearer ${token}`, 'Content-Type': 'application/json' },
    data: {
      name: `SEO Test Product ${Date.now()}`,
      priceExclTax: 19.99,
      quantity: 10,
      stockAlertThreshold: 2,
    },
  });
  expect(res.ok()).toBeTruthy();
  return res.json();
}

test.describe('US-SEO-02 — Per-product SEO override', () => {
  let vendorToken;
  let product;

  test.beforeAll(async ({ browser }) => {
    const page = await browser.newPage();
    await createActiveVendorViaApi(page, VENDOR_EMAIL, VENDOR_PASSWORD);
    vendorToken = await getVendorToken(page, VENDOR_EMAIL, VENDOR_PASSWORD);
    product = await createProduct(page, vendorToken);
    await page.close();
  });

  // --- nominal: save product SEO override via API ---

  test('nominal — PUT /api/vendor/seo/products/:id saves override', async ({ page }) => {
    const res = await page.request.put(`${API_URL}/api/vendor/seo/products/${product.id}`, {
      headers: { Authorization: `Bearer ${vendorToken}`, 'Content-Type': 'application/json' },
      data: {
        seoTitle: 'Custom Product Title',
        seoDescription: 'Custom product description for SEO.',
        seoKeywords: 'art, photo',
        ogImageUrl: null,
      },
    });
    expect(res.ok()).toBeTruthy();
    const body = await res.json();
    expect(body.seoTitle).toBe('Custom Product Title');
    expect(body.productId).toBe(product.id);
  });

  // --- nominal: GET returns saved override ---

  test('nominal — GET /api/vendor/seo/products/:id returns override', async ({ page }) => {
    await page.request.put(`${API_URL}/api/vendor/seo/products/${product.id}`, {
      headers: { Authorization: `Bearer ${vendorToken}`, 'Content-Type': 'application/json' },
      data: { seoTitle: 'Override for GET test' },
    });

    const res = await page.request.get(`${API_URL}/api/vendor/seo/products/${product.id}`, {
      headers: { Authorization: `Bearer ${vendorToken}` },
    });
    expect(res.ok()).toBeTruthy();
    const body = await res.json();
    expect(body.seoTitle).toBe('Override for GET test');
  });

  // --- nominal: DELETE removes override ---

  test('nominal — DELETE /api/vendor/seo/products/:id removes override, public returns 204', async ({ page }) => {
    await page.request.put(`${API_URL}/api/vendor/seo/products/${product.id}`, {
      headers: { Authorization: `Bearer ${vendorToken}`, 'Content-Type': 'application/json' },
      data: { seoTitle: 'To be deleted' },
    });

    const delRes = await page.request.delete(`${API_URL}/api/vendor/seo/products/${product.id}`, {
      headers: { Authorization: `Bearer ${vendorToken}` },
    });
    expect(delRes.status()).toBe(204);

    const publicRes = await page.request.get(`${API_URL}/api/public/seo/products/${product.id}`);
    expect(publicRes.status()).toBe(204);
  });

  // --- nominal: public endpoint returns override ---

  test('nominal — public endpoint returns product SEO override', async ({ page }) => {
    await page.request.put(`${API_URL}/api/vendor/seo/products/${product.id}`, {
      headers: { Authorization: `Bearer ${vendorToken}`, 'Content-Type': 'application/json' },
      data: { seoTitle: 'Public Product Title' },
    });

    const res = await page.request.get(`${API_URL}/api/public/seo/products/${product.id}`);
    expect(res.ok()).toBeTruthy();
    const body = await res.json();
    expect(body.seoTitle).toBe('Public Product Title');
  });

  // --- error: GET non-existent override returns 404 ---

  test('error — GET product SEO for product with no override returns 404', async ({ page }) => {
    await page.request.delete(`${API_URL}/api/vendor/seo/products/${product.id}`, {
      headers: { Authorization: `Bearer ${vendorToken}` },
    }).catch(() => {});

    const res = await page.request.get(`${API_URL}/api/vendor/seo/products/${product.id}`, {
      headers: { Authorization: `Bearer ${vendorToken}` },
    });
    expect(res.status()).toBe(404);
  });

  // --- UI: per-product tab shows product in list ---

  test('nominal — per-product tab shows product and edit button', async ({ page }) => {
    await injectVendorSession(page, VENDOR_EMAIL, vendorToken);
    await page.goto('/vendor/');
    await page.getByText('Référencement (SEO)').click();
    await page.getByText('Par produit').click();

    await expect(page.getByText(product.name)).toBeVisible();
    await expect(page.getByRole('button', { name: 'Modifier' }).first()).toBeVisible();
  });
});
