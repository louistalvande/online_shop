import { test, expect } from '@playwright/test';
import {
  createActiveVendorViaApi, getVendorToken, injectVendorSession, createProductViaApi,
  API_URL,
} from '../helpers/login.js';

// US-CAT-04 — Vendor consults and updates stock levels.

test.describe('US-CAT-04 — Stock management', () => {

  test('product list shows quantity and threshold columns', async ({ page }) => {
    const email = `cat04-${Date.now()}@shop-test.example`;
    const password = 'sHp-E2e!Vnd-X9pZ';

    await createActiveVendorViaApi(page, email, password);
    const token = await getVendorToken(page, email, password);

    await createProductViaApi(page, token, {
      name: 'Encre de chine',
      priceExclTax: 14.50,
      quantity: 8,
      stockAlertThreshold: 3,
      photoUrls: [],
    });

    await injectVendorSession(page, email, token);
    await page.reload();
    await page.getByRole('link', { name: 'Catalogue' }).click();

    // Stock and threshold columns visible
    await expect(page.getByRole('columnheader', { name: 'Qté' })).toBeVisible();
    await expect(page.getByRole('columnheader', { name: 'Seuil' })).toBeVisible();
    const productRow = page.locator('tr').filter({ hasText: 'Encre de chine' });
    await expect(productRow.getByText('8')).toBeVisible();
    await expect(productRow.getByText('OK')).toBeVisible();
  });

  test('product with quantity=0 shows "Rupture" badge', async ({ page }) => {
    const email = `cat04b-${Date.now()}@shop-test.example`;
    const password = 'sHp-E2e!Vnd-X9pZ';

    await createActiveVendorViaApi(page, email, password);
    const token = await getVendorToken(page, email, password);

    await createProductViaApi(page, token, {
      name: 'Produit épuisé',
      priceExclTax: 9.90,
      quantity: 0,
      stockAlertThreshold: 5,
      photoUrls: [],
    });

    await injectVendorSession(page, email, token);
    await page.reload();
    await page.getByRole('link', { name: 'Catalogue' }).click();

    await expect(page.getByText('Rupture')).toBeVisible();
  });

  test('product with quantity below threshold shows "Stock bas" badge', async ({ page }) => {
    const email = `cat04c-${Date.now()}@shop-test.example`;
    const password = 'sHp-E2e!Vnd-X9pZ';

    await createActiveVendorViaApi(page, email, password);
    const token = await getVendorToken(page, email, password);

    await createProductViaApi(page, token, {
      name: 'Produit stock bas',
      priceExclTax: 7.50,
      quantity: 2,
      stockAlertThreshold: 5,
      photoUrls: [],
    });

    await injectVendorSession(page, email, token);
    await page.reload();
    await page.getByRole('link', { name: 'Catalogue' }).click();

    await expect(page.getByText('Stock bas', { exact: true })).toBeVisible();
  });

  test('stock update via API is reflected in product list', async ({ page }) => {
    const email = `cat04d-${Date.now()}@shop-test.example`;
    const password = 'sHp-E2e!Vnd-X9pZ';

    await createActiveVendorViaApi(page, email, password);
    const token = await getVendorToken(page, email, password);

    const product = await createProductViaApi(page, token, {
      name: 'Gouache à mettre à jour',
      priceExclTax: 11.00,
      quantity: 5,
      stockAlertThreshold: 2,
      photoUrls: [],
    });

    // Update stock via API
    await page.request.patch(`${API_URL}/api/vendor/products/${product.id}/stock`, {
      headers: { Authorization: `Bearer ${token}`, 'Content-Type': 'application/json' },
      data: { quantity: 20, stockAlertThreshold: 3 },
    });

    await injectVendorSession(page, email, token);
    await page.reload();
    await page.getByRole('link', { name: 'Catalogue' }).click();

    await expect(page.getByRole('cell', { name: '20' })).toBeVisible();
  });

});
