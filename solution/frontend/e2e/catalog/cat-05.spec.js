import { test, expect } from '@playwright/test';
import {
  createActiveVendorViaApi, getVendorToken, injectVendorSession, createProductViaApi,
} from '../helpers/login.js';

// US-CAT-05 — Vendor receives stock alert notification when stock falls below threshold.

test.describe('US-CAT-05 — Stock alert notification', () => {

  test('nominal — product created below threshold generates alert visible in catalog', async ({ page }) => {
    const email = `cat05-${Date.now()}@shop-test.example`;
    const password = 'VendorPass123!';

    await createActiveVendorViaApi(page, email, password);
    const token = await getVendorToken(page, email, password);

    // Create product with quantity below threshold → alert triggered immediately
    await createProductViaApi(page, token, {
      name: 'Crayon alerte',
      priceExclTax: 5.00,
      quantity: 1,
      stockAlertThreshold: 5,
      photoUrls: [],
    });

    await injectVendorSession(page, email, token);
    await page.reload();
    await page.getByText('Catalogue').click();

    // Alert panel is visible in catalog page
    await expect(page.getByText('Alertes stock')).toBeVisible();
    await expect(page.getByText('Crayon alerte')).toBeVisible();
  });

  test('alert badge shown on Catalogue nav link when on dashboard', async ({ page }) => {
    const email = `cat05b-${Date.now()}@shop-test.example`;
    const password = 'VendorPass123!';

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

    // On dashboard, the Catalogue nav link has a badge
    await page.getByText('Tableau de bord').click();
    const catalogLink = page.getByText('Catalogue');
    // Wait for the badge to appear (App polls alerts on load)
    await expect(catalogLink.locator('..').getByText('1')).toBeVisible({ timeout: 5000 });
  });

  test('acknowledge — removes alert from the panel', async ({ page }) => {
    const email = `cat05c-${Date.now()}@shop-test.example`;
    const password = 'VendorPass123!';

    await createActiveVendorViaApi(page, email, password);
    const token = await getVendorToken(page, email, password);

    await createProductViaApi(page, token, {
      name: 'Aquarelle alerte',
      priceExclTax: 8.00,
      quantity: 1,
      stockAlertThreshold: 4,
      photoUrls: [],
    });

    await injectVendorSession(page, email, token);
    await page.reload();
    await page.getByText('Catalogue').click();

    await expect(page.getByText('Alertes stock')).toBeVisible();

    // Acknowledge the alert
    await page.getByRole('button', { name: 'Acquitter' }).click();

    // Alert panel disappears once all alerts are acknowledged
    await expect(page.getByText('Alertes stock')).not.toBeVisible();
  });

  test('no duplicate alert for same product when already pending', async ({ page }) => {
    const email = `cat05d-${Date.now()}@shop-test.example`;
    const password = 'VendorPass123!';

    await createActiveVendorViaApi(page, email, password);
    const token = await getVendorToken(page, email, password);

    const product = await createProductViaApi(page, token, {
      name: 'Produit test dédoublonnage',
      priceExclTax: 6.00,
      quantity: 1,
      stockAlertThreshold: 5,
      photoUrls: [],
    });

    // Update stock again — still below threshold, no duplicate alert
    await page.request.patch(
      `http://localhost:8080/api/vendor/products/${product.id}/stock`,
      {
        headers: { Authorization: `Bearer ${token}`, 'Content-Type': 'application/json' },
        data: { quantity: 2, stockAlertThreshold: 5 },
      },
    );

    await injectVendorSession(page, email, token);
    await page.reload();
    await page.getByText('Catalogue').click();

    // Only one alert for this product (not two)
    const acknowledgeButtons = page.getByRole('button', { name: 'Acquitter' });
    await expect(acknowledgeButtons).toHaveCount(1);
  });

});
