import { test, expect } from '@playwright/test';
import {
  API_URL,
  createActiveVendorViaApi,
  getVendorToken,
  createProductViaApi,
  registerAndActivateBuyerViaApi,
  getBuyerToken,
  injectBuyerSession,
} from '../helpers/login.js';

// US-SHP-03 — Back-in-stock alert subscriptions.

test.describe('US-SHP-03 — Alertes de remise en stock', () => {
  let vendorEmail, vendorPassword, vendorToken;
  let buyerEmail, buyerPassword, buyerToken;
  let outOfStockProduct, inStockProduct;

  test.beforeEach(async ({ page }) => {
    const ts = Date.now();
    vendorEmail = `shp03-v-${ts}@shop-test.example`;
    vendorPassword = 'sHp-E2e!Vnd-X9pZ';
    buyerEmail = `shp03-b-${ts}@shop-test.example`;
    buyerPassword = 'BuyerPass123!';

    await createActiveVendorViaApi(page, vendorEmail, vendorPassword);
    vendorToken = await getVendorToken(page, vendorEmail, vendorPassword);

    outOfStockProduct = await createProductViaApi(page, vendorToken, {
      name: `Aquarelle SHP-03 ${ts}`,
      priceExclTax: 25.00,
      quantity: 0,
      stockAlertThreshold: 0,
      category: 'Aquarelle',
      photoUrls: [],
    });

    inStockProduct = await createProductViaApi(page, vendorToken, {
      name: `Pastel SHP-03 ${ts}`,
      priceExclTax: 15.00,
      quantity: 10,
      stockAlertThreshold: 2,
      category: 'Pastel',
      photoUrls: [],
    });

    await registerAndActivateBuyerViaApi(page, buyerEmail, buyerPassword);
    buyerToken = await getBuyerToken(page, buyerEmail, buyerPassword);
  });

  // ─── UI tests ────────────────────────────────────────────────────────────────

  test('nominal — bouton "M\'alerter" visible sur un produit indisponible', async ({ page }) => {
    await injectBuyerSession(page, buyerEmail, buyerToken);
    await page.goto(`/catalog/${outOfStockProduct.id}`);

    await expect(page.getByRole('button', { name: /M.alerter quand disponible/i })).toBeVisible({ timeout: 10000 });
  });

  test('nominal — clic "M\'alerter" abonne et change le bouton en "Alerte activée"', async ({ page }) => {
    await injectBuyerSession(page, buyerEmail, buyerToken);
    await page.goto(`/catalog/${outOfStockProduct.id}`);

    await page.getByRole('button', { name: /M.alerter quand disponible/i }).click();

    await expect(page.getByRole('button', { name: /Alerte activ/i })).toBeVisible({ timeout: 10000 });
  });

  test('nominal — abonnement apparaît dans l\'onglet "Mes alertes" du profil', async ({ page }) => {
    // Subscribe via API
    await page.request.post(`${API_URL}/api/profile/stock-subscriptions`, {
      headers: { Authorization: `Bearer ${buyerToken}`, 'Content-Type': 'application/json' },
      data: { productId: outOfStockProduct.id },
    });

    await injectBuyerSession(page, buyerEmail, buyerToken);
    await page.goto('/profile');

    await page.getByRole('button', { name: 'Mes alertes' }).click();

    await expect(page.getByText(outOfStockProduct.name)).toBeVisible({ timeout: 10000 });
  });

  test('nominal — annuler depuis le profil supprime l\'alerte', async ({ page }) => {
    // Subscribe via API
    await page.request.post(`${API_URL}/api/profile/stock-subscriptions`, {
      headers: { Authorization: `Bearer ${buyerToken}`, 'Content-Type': 'application/json' },
      data: { productId: outOfStockProduct.id },
    });

    await injectBuyerSession(page, buyerEmail, buyerToken);
    await page.goto('/profile');

    await page.getByRole('button', { name: 'Mes alertes' }).click();
    await expect(page.getByText(outOfStockProduct.name)).toBeVisible({ timeout: 10000 });

    await page.getByRole('button', { name: 'Annuler' }).first().click();

    await expect(page.getByText(outOfStockProduct.name)).not.toBeVisible({ timeout: 5000 });
  });

  // ─── API tests ───────────────────────────────────────────────────────────────

  test('API — subscribe returns 201 for an out-of-stock product', async ({ page }) => {
    const res = await page.request.post(`${API_URL}/api/profile/stock-subscriptions`, {
      headers: { Authorization: `Bearer ${buyerToken}`, 'Content-Type': 'application/json' },
      data: { productId: outOfStockProduct.id },
    });

    expect(res.status()).toBe(201);
    const body = await res.json();
    expect(body.productId).toBe(outOfStockProduct.id);
  });

  test('API — subscribe returns 409 ALREADY_SUBSCRIBED when subscribed twice', async ({ page }) => {
    // First subscription
    await page.request.post(`${API_URL}/api/profile/stock-subscriptions`, {
      headers: { Authorization: `Bearer ${buyerToken}`, 'Content-Type': 'application/json' },
      data: { productId: outOfStockProduct.id },
    });

    // Second subscription — must be rejected
    const res = await page.request.post(`${API_URL}/api/profile/stock-subscriptions`, {
      headers: { Authorization: `Bearer ${buyerToken}`, 'Content-Type': 'application/json' },
      data: { productId: outOfStockProduct.id },
    });

    expect(res.status()).toBe(409);
    const body = await res.json();
    expect(body.error).toBe('ALREADY_SUBSCRIBED');
  });

  test('API — subscribe returns 409 PRODUCT_IN_STOCK for an available product', async ({ page }) => {
    const res = await page.request.post(`${API_URL}/api/profile/stock-subscriptions`, {
      headers: { Authorization: `Bearer ${buyerToken}`, 'Content-Type': 'application/json' },
      data: { productId: inStockProduct.id },
    });

    expect(res.status()).toBe(409);
    const body = await res.json();
    expect(body.error).toBe('PRODUCT_IN_STOCK');
  });

  test('API — DELETE removes the subscription, buyer can re-subscribe afterwards', async ({ page }) => {
    // Subscribe
    await page.request.post(`${API_URL}/api/profile/stock-subscriptions`, {
      headers: { Authorization: `Bearer ${buyerToken}`, 'Content-Type': 'application/json' },
      data: { productId: outOfStockProduct.id },
    });

    // Cancel
    const del = await page.request.delete(
      `${API_URL}/api/profile/stock-subscriptions/${outOfStockProduct.id}`,
      { headers: { Authorization: `Bearer ${buyerToken}` } },
    );
    expect(del.status()).toBe(204);

    // Re-subscribe — must succeed (unique constraint freed)
    const resubscribe = await page.request.post(`${API_URL}/api/profile/stock-subscriptions`, {
      headers: { Authorization: `Bearer ${buyerToken}`, 'Content-Type': 'application/json' },
      data: { productId: outOfStockProduct.id },
    });
    expect(resubscribe.status()).toBe(201);
  });

  test('API — vendor restock deletes subscriptions, buyer list becomes empty', async ({ page }) => {
    // Subscribe
    await page.request.post(`${API_URL}/api/profile/stock-subscriptions`, {
      headers: { Authorization: `Bearer ${buyerToken}`, 'Content-Type': 'application/json' },
      data: { productId: outOfStockProduct.id },
    });

    // Verify subscription exists
    const before = await page.request.get(`${API_URL}/api/profile/stock-subscriptions`, {
      headers: { Authorization: `Bearer ${buyerToken}` },
    });
    expect((await before.json()).length).toBe(1);

    // Vendor restocks the product (0 → 5)
    await page.request.patch(`${API_URL}/api/vendor/products/${outOfStockProduct.id}/stock`, {
      headers: { Authorization: `Bearer ${vendorToken}`, 'Content-Type': 'application/json' },
      data: { quantity: 5, stockAlertThreshold: 0 },
    });

    // Subscription must have been deleted after notification
    const after = await page.request.get(`${API_URL}/api/profile/stock-subscriptions`, {
      headers: { Authorization: `Bearer ${buyerToken}` },
    });
    expect((await after.json()).length).toBe(0);
  });

  test('API — buyer can re-subscribe after vendor restocked (subscription freed)', async ({ page }) => {
    // Subscribe, vendor restocks
    await page.request.post(`${API_URL}/api/profile/stock-subscriptions`, {
      headers: { Authorization: `Bearer ${buyerToken}`, 'Content-Type': 'application/json' },
      data: { productId: outOfStockProduct.id },
    });
    await page.request.patch(`${API_URL}/api/vendor/products/${outOfStockProduct.id}/stock`, {
      headers: { Authorization: `Bearer ${vendorToken}`, 'Content-Type': 'application/json' },
      data: { quantity: 5, stockAlertThreshold: 0 },
    });

    // Vendor puts product out of stock again
    await page.request.patch(`${API_URL}/api/vendor/products/${outOfStockProduct.id}/stock`, {
      headers: { Authorization: `Bearer ${vendorToken}`, 'Content-Type': 'application/json' },
      data: { quantity: 0, stockAlertThreshold: 0 },
    });

    // Buyer re-subscribes — must succeed (old subscription was deleted, not just marked)
    const res = await page.request.post(`${API_URL}/api/profile/stock-subscriptions`, {
      headers: { Authorization: `Bearer ${buyerToken}`, 'Content-Type': 'application/json' },
      data: { productId: outOfStockProduct.id },
    });
    expect(res.status()).toBe(201);
  });

  test('API — unauthenticated request returns 401', async ({ page }) => {
    await page.context().clearCookies({ name: 'jwt' });
    const res = await page.request.get(`${API_URL}/api/profile/stock-subscriptions`);
    expect([401, 403]).toContain(res.status());
  });
});
