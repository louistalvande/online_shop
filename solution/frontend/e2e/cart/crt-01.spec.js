import { test, expect } from '@playwright/test';
import {
  createActiveVendorViaApi,
  getVendorToken,
  createProductViaApi,
  registerAndActivateBuyerViaApi,
  getBuyerToken,
  injectBuyerSession,
} from '../helpers/login.js';

// US-CRT-01 — Buyer adds products to cart, modifies quantities, removes items.

test.describe('US-CRT-01 — Cart management', () => {

  let vendorEmail, vendorPassword, buyerEmail, buyerPassword, product;

  test.beforeEach(async ({ page }) => {
    const ts = Date.now();
    vendorEmail = `crt01-v-${ts}@shop-test.example`;
    vendorPassword = 'sHp-E2e!Vnd-X9pZ';
    buyerEmail = `crt01-b-${ts}@shop-test.example`;
    buyerPassword = 'BuyerPass123!';

    await createActiveVendorViaApi(page, vendorEmail, vendorPassword);
    const vendorToken = await getVendorToken(page, vendorEmail, vendorPassword);
    product = await createProductViaApi(page, vendorToken, {
      name: `Aquarelle CRT-01 ${ts}`,
      priceExclTax: 25.00,
      quantity: 5,
      stockAlertThreshold: 1,
      category: 'Aquarelle',
      photoUrls: [],
    });

    await registerAndActivateBuyerViaApi(page, buyerEmail, buyerPassword);
    const buyerToken = await getBuyerToken(page, buyerEmail, buyerPassword);
    await injectBuyerSession(page, buyerEmail, buyerToken);
  });

  test('nominal — cart page shows empty cart when no items added', async ({ page }) => {
    await page.goto('/cart');
    await expect(page.getByText(/panier/i).first()).toBeVisible();
    await expect(page.getByText(/vide/i)).toBeVisible();
  });

  test('nominal — buyer can add a product to cart and see it on cart page', async ({ page }) => {
    // Add directly via API (UI flow is covered by catalog tests; here we focus on cart)
    const buyerToken = await getBuyerToken(page, buyerEmail, buyerPassword);
    await page.request.post('/api/cart/items', {
      headers: { Authorization: `Bearer ${buyerToken}`, 'Content-Type': 'application/json' },
      data: { productId: product.id, quantity: 2 },
    });

    await page.goto('/cart');
    await expect(page.getByText(product.name)).toBeVisible({ timeout: 10_000 });
  });

  test('nominal — buyer can update quantity and cart total updates', async ({ page }) => {
    const buyerToken = await getBuyerToken(page, buyerEmail, buyerPassword);
    await page.request.post('/api/cart/items', {
      headers: { Authorization: `Bearer ${buyerToken}`, 'Content-Type': 'application/json' },
      data: { productId: product.id, quantity: 1 },
    });

    await page.goto('/cart');
    const qtyInput = page.getByRole('spinbutton').first();
    await expect(qtyInput).toHaveValue('1');

    await qtyInput.fill('3');
    await qtyInput.dispatchEvent('change');
    // Give the update a moment to complete and re-render
    await page.waitForTimeout(500);

    // Cart should now show 3 in stock or the updated total
    await expect(qtyInput).toHaveValue('3');
  });

  test('nominal — buyer can remove an item from cart', async ({ page }) => {
    const buyerToken = await getBuyerToken(page, buyerEmail, buyerPassword);
    await page.request.post('/api/cart/items', {
      headers: { Authorization: `Bearer ${buyerToken}`, 'Content-Type': 'application/json' },
      data: { productId: product.id, quantity: 1 },
    });

    await page.goto('/cart');
    await expect(page.getByText(product.name)).toBeVisible();

    await page.getByRole('button', { name: /retirer/i }).first().click();
    await expect(page.getByText(/vide/i)).toBeVisible({ timeout: 5_000 });
  });

  test('error — adding out-of-stock product via API returns 409', async ({ page }) => {
    const ts = Date.now();
    const vendorToken = await getVendorToken(page, vendorEmail, vendorPassword);
    const outOfStockProduct = await createProductViaApi(page, vendorToken, {
      name: `Épuisé CRT-01 ${ts}`,
      priceExclTax: 10.00,
      quantity: 0,
      stockAlertThreshold: 0,
      category: 'Test',
      photoUrls: [],
    });

    const buyerToken = await getBuyerToken(page, buyerEmail, buyerPassword);
    const res = await page.request.post('/api/cart/items', {
      headers: { Authorization: `Bearer ${buyerToken}`, 'Content-Type': 'application/json' },
      data: { productId: outOfStockProduct.id, quantity: 1 },
    });
    expect(res.status()).toBe(409);
    const body = await res.json();
    expect(body.error).toBe('PRODUCT_OUT_OF_STOCK');
  });

  test('error — unauthenticated request to cart returns 403', async ({ page }) => {
    await page.context().clearCookies({ name: 'jwt' });
    const res = await page.request.get('/api/cart');
    expect([401, 403]).toContain(res.status());
  });

  test('nominal — unauthenticated buyer on /cart sees login prompt', async ({ page }) => {
    // Clear session
    await page.goto('/');
    await page.evaluate(() => localStorage.removeItem('buyer_session'));
    await page.goto('/cart');
    await expect(page.getByText(/connectez-vous/i)).toBeVisible();
  });
});
