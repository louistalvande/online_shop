import { test, expect } from '@playwright/test';
import {
  createActiveVendorViaApi, getVendorToken, injectVendorSession, createProductViaApi, API_URL,
} from '../helpers/login.js';

// US-CAT-05 — Vendor receives stock alert notification when stock falls below threshold.

test.describe('US-CAT-05 — Stock alert notification', () => {

  test('nominal — product created below threshold generates alert visible in catalog', async ({ page }) => {
    const ts = Date.now();
    const email = `cat05-${ts}@shop-test.example`;
    const password = 'sHp-E2e!Vnd-X9pZ';
    const productName = `Crayon alerte ${ts}`;

    await createActiveVendorViaApi(page, email, password);
    const token = await getVendorToken(page, email, password);

    // Create product with quantity below threshold → alert triggered immediately
    await createProductViaApi(page, token, {
      name: productName,
      priceExclTax: 5.00,
      quantity: 1,
      stockAlertThreshold: 5,
      photoUrls: [],
    });

    await injectVendorSession(page, email, token);
    await page.reload();
    await page.getByRole('link', { name: 'Catalogue' }).click();

    // Alert panel is visible in catalog page
    await expect(page.getByText('Alertes stock')).toBeVisible();
    await expect(page.getByRole('cell', { name: productName }).first()).toBeVisible();
  });

  test('alert badge shown on Catalogue nav link when on dashboard', async ({ page }) => {
    const email = `cat05b-${Date.now()}@shop-test.example`;
    const password = 'sHp-E2e!Vnd-X9pZ';

    await createActiveVendorViaApi(page, email, password);
    const token = await getVendorToken(page, email, password);

    await createProductViaApi(page, token, {
      name: 'Stylo stock faible',
      priceExclTax: 3.50,
      quantity: 0,
      stockAlertThreshold: 2,
      photoUrls: [],
    });

    await injectVendorSession(page, email, token);
    await page.reload();

    // On dashboard, the Catalogue nav link has a badge (count may be > 1 due to shared DB)
    await page.getByRole('link', { name: 'Tableau de bord' }).click();
    const catalogLink = page.getByRole('link', { name: 'Catalogue' });
    // Wait for any badge count to appear (App polls alerts on load)
    await expect(catalogLink.locator('..').locator('span').filter({ hasText: /^\d+$/ }).first()).toBeVisible({ timeout: 5000 });
  });

  test('acknowledge — removes alert from the panel', async ({ page }) => {
    const email = `cat05c-${Date.now()}@shop-test.example`;
    const password = 'sHp-E2e!Vnd-X9pZ';

    await createActiveVendorViaApi(page, email, password);
    const token = await getVendorToken(page, email, password);

    const ts = Date.now();
    const alertProductName = `Aquarelle alerte ${ts}`;
    await createProductViaApi(page, token, {
      name: alertProductName,
      priceExclTax: 8.00,
      quantity: 1,
      stockAlertThreshold: 4,
      photoUrls: [],
    });

    await injectVendorSession(page, email, token);
    await page.reload();
    await page.getByRole('link', { name: 'Catalogue' }).click();

    await expect(page.getByText('Alertes stock')).toBeVisible();

    // Acknowledge the specific alert for this product:
    // navigate from <strong>PRODUCT_NAME</strong> → parent <span> → parent <div> (the alert row)
    const alertStrong = page.locator('strong').filter({ hasText: alertProductName });
    const alertRowDiv = alertStrong.locator('../..');  // strong → span → row div
    await alertRowDiv.getByRole('button', { name: 'Acquitter' }).click();

    // That product's alert row should disappear
    await expect(page.locator('strong', { hasText: alertProductName })).not.toBeVisible();
  });

  test('no duplicate alert for same product when already pending', async ({ page }) => {
    const ts = Date.now();
    const email = `cat05d-${ts}@shop-test.example`;
    const password = 'sHp-E2e!Vnd-X9pZ';
    const dedupName = `Produit test dédoublonnage ${ts}`;

    await createActiveVendorViaApi(page, email, password);
    const token = await getVendorToken(page, email, password);

    const product = await createProductViaApi(page, token, {
      name: dedupName,
      priceExclTax: 6.00,
      quantity: 1,
      stockAlertThreshold: 5,
      photoUrls: [],
    });

    // Update stock again — still below threshold, no duplicate alert
    await page.request.patch(
      `${API_URL}/api/vendor/products/${product.id}/stock`,
      {
        headers: { Authorization: `Bearer ${token}`, 'Content-Type': 'application/json' },
        data: { quantity: 2, stockAlertThreshold: 5 },
      },
    );

    await injectVendorSession(page, email, token);
    await page.reload();
    await page.getByRole('link', { name: 'Catalogue' }).click();

    // Only one alert row for this specific product (not two — no duplicate)
    const thisProductAlerts = page.locator('strong', { hasText: dedupName });
    await expect(thisProductAlerts).toHaveCount(1);
  });

});
