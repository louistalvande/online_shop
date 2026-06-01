import { test, expect } from '@playwright/test';
import {
  createActiveVendorViaApi, getVendorToken, injectVendorSession, createProductViaApi,
} from '../helpers/login.js';

// US-CAT-02 — Vendor edits an existing product.

test.describe('US-CAT-02 — Edit product', () => {

  test('nominal — opens edit form pre-filled, saves changes, list reflects update', async ({ page }) => {
    const ts = Date.now();
    const email = `cat02-${ts}@shop-test.example`;
    const password = 'sHp-E2e!Vnd-X9pZ';
    const productName = `Huile sur toile ${ts}`;
    const updatedName = `Huile sur toile — édition spéciale ${ts}`;

    await createActiveVendorViaApi(page, email, password);
    const token = await getVendorToken(page, email, password);

    // Create a product via API
    await createProductViaApi(page, token, {
      name: productName,
      priceExclTax: 49.00,
      quantity: 5,
      stockAlertThreshold: 2,
      photoUrls: [],
    });

    await injectVendorSession(page, email, token);
    await page.reload();
    await page.getByRole('link', { name: 'Catalogue' }).click();

    // Product is visible
    await expect(page.getByText(productName).first()).toBeVisible();

    // Open edit modal
    await page.getByText(productName).first().locator('..').locator('..').getByRole('button', { name: 'Modifier' }).click();
    await expect(page.getByRole('heading', { name: 'Modifier le produit' })).toBeVisible();

    // Form is pre-filled
    await expect(page.getByLabel('Nom')).toHaveValue(productName);
    await expect(page.getByLabel('Prix HT (€)')).toHaveValue('49');

    // Update name
    await page.getByLabel('Nom').fill(updatedName);
    await page.getByRole('button', { name: 'Enregistrer' }).click();

    await expect(page.getByRole('heading', { name: 'Modifier le produit' })).not.toBeVisible();
    await expect(page.getByText(updatedName).first()).toBeVisible();
  });

});
