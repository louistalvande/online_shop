import { test, expect } from '@playwright/test';
import {
  createActiveVendorViaApi,
  getVendorToken,
  createProductViaApi,
  registerAndActivateBuyerViaApi,
  getBuyerToken,
  injectBuyerSession,
} from '../helpers/login.js';

// US-CRT-02 — Cart is automatically saved and fully restored on reconnection.

test.describe('US-CRT-02 — Cart persistence', () => {

  test('nominal — cart is persisted and restored after logout + login', async ({ page }) => {
    const ts = Date.now();
    const vendorEmail = `crt02-v-${ts}@shop-test.example`;
    const vendorPassword = 'VendorPass123!';
    const buyerEmail = `crt02-b-${ts}@shop-test.example`;
    const buyerPassword = 'BuyerPass123!';

    // Set up vendor and product
    await createActiveVendorViaApi(page, vendorEmail, vendorPassword);
    const vendorToken = await getVendorToken(page, vendorEmail, vendorPassword);
    const product = await createProductViaApi(page, vendorToken, {
      name: `Persistance CRT-02 ${ts}`,
      priceExclTax: 18.00,
      quantity: 10,
      stockAlertThreshold: 2,
      category: 'Aquarelle',
      photoUrls: [],
    });

    // Register and activate buyer, then log in
    await registerAndActivateBuyerViaApi(page, buyerEmail, buyerPassword);
    let buyerToken = await getBuyerToken(page, buyerEmail, buyerPassword);
    await injectBuyerSession(page, buyerEmail, buyerToken);

    // Add item to cart via API (simulates adding from catalog page)
    await page.request.post('/api/cart/items', {
      headers: { Authorization: `Bearer ${buyerToken}`, 'Content-Type': 'application/json' },
      data: { productId: product.id, quantity: 3 },
    });

    // Verify cart has item
    await page.goto('/cart');
    await expect(page.getByText(product.name)).toBeVisible({ timeout: 10_000 });

    // Simulate logout by clearing the session
    await page.evaluate(() => localStorage.removeItem('buyer_session'));
    await page.goto('/');

    // Log in again by injecting a fresh session
    buyerToken = await getBuyerToken(page, buyerEmail, buyerPassword);
    await injectBuyerSession(page, buyerEmail, buyerToken);

    // Cart must still contain the product after re-authentication (US-CRT-02)
    await page.goto('/cart');
    await expect(page.getByText(product.name)).toBeVisible({ timeout: 10_000 });
    await expect(page.getByRole('spinbutton').first()).toHaveValue('3');
  });

  test('nominal — cart API returns saved items after session expiry simulation', async ({ page }) => {
    const ts = Date.now();
    const vendorEmail = `crt02c-v-${ts}@shop-test.example`;
    const vendorPassword = 'VendorPass123!';
    const buyerEmail = `crt02c-b-${ts}@shop-test.example`;
    const buyerPassword = 'BuyerPass123!';

    await createActiveVendorViaApi(page, vendorEmail, vendorPassword);
    const vendorToken = await getVendorToken(page, vendorEmail, vendorPassword);
    const product = await createProductViaApi(page, vendorToken, {
      name: `Persistance2 CRT-02 ${ts}`,
      priceExclTax: 22.00,
      quantity: 8,
      stockAlertThreshold: 1,
      category: 'Aquarelle',
      photoUrls: [],
    });

    await registerAndActivateBuyerViaApi(page, buyerEmail, buyerPassword);
    const buyerToken = await getBuyerToken(page, buyerEmail, buyerPassword);

    // Add item directly via API with first token
    await page.request.post('/api/cart/items', {
      headers: { Authorization: `Bearer ${buyerToken}`, 'Content-Type': 'application/json' },
      data: { productId: product.id, quantity: 2 },
    });

    // Obtain a new token (re-login) and verify cart is still there
    const newToken = await getBuyerToken(page, buyerEmail, buyerPassword);
    const cartRes = await page.request.get('/api/cart', {
      headers: { Authorization: `Bearer ${newToken}` },
    });
    expect(cartRes.ok()).toBeTruthy();
    const cart = await cartRes.json();
    expect(cart.items.length).toBe(1);
    expect(cart.items[0].productId).toBe(product.id);
    expect(cart.items[0].quantity).toBe(2);
  });
});
