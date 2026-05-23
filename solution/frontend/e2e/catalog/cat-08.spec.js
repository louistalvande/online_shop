import { test, expect } from '@playwright/test';
import { createActiveVendorViaApi, getVendorToken, injectVendorSession, createProductViaApi } from '../helpers/login.js';

// US-CAT-08 — Vendor updates stock levels for multiple products from the catalog list view.

test.describe('US-CAT-08 — Bulk stock update', () => {
  let email, password, token, productA, productB;

  test.beforeEach(async ({ page }) => {
    email = `cat08-${Date.now()}@shop-test.example`;
    password = 'sHp-E2e!Vnd-X9pZ';

    await createActiveVendorViaApi(page, email, password);
    token = await getVendorToken(page, email, password);

    productA = await createProductViaApi(page, token, {
      name: 'Produit Stock A',
      priceExclTax: 25.00,
      quantity: 10,
      stockAlertThreshold: 3,
      photoUrls: [],
    });
    productB = await createProductViaApi(page, token, {
      name: 'Produit Stock B',
      priceExclTax: 30.00,
      quantity: 5,
      stockAlertThreshold: 2,
      photoUrls: [],
    });

    await injectVendorSession(page, email, token);
    await page.reload();
    await page.getByRole('link', { name: 'Catalogue' }).click();
    await expect(page.getByRole('heading', { name: 'Catalogue produits' })).toBeVisible();
  });

  test('nominal — edits quantities for two products, save button appears, table refreshes', async ({ page }) => {
    const rowA = page.locator('tr').filter({ hasText: 'Produit Stock A' });
    const rowB = page.locator('tr').filter({ hasText: 'Produit Stock B' });

    // Save button should not be visible yet
    await expect(page.getByRole('button', { name: 'Enregistrer les modifications' })).not.toBeVisible();

    // Edit quantity for product A: 10 → 20
    await rowA.locator('input[type="number"]').first().fill('20');
    // Edit quantity for product B: 5 → 1
    await rowB.locator('input[type="number"]').first().fill('1');

    // Save banner appears with count
    await expect(page.getByRole('button', { name: 'Enregistrer les modifications' })).toBeVisible();
    await expect(page.getByText('2 modification(s) en attente')).toBeVisible();

    await page.getByRole('button', { name: 'Enregistrer les modifications' }).click();

    // Success feedback
    await expect(page.getByText('2 stock(s) mis à jour.')).toBeVisible();

    // Save banner disappears
    await expect(page.getByRole('button', { name: 'Enregistrer les modifications' })).not.toBeVisible();

    // Inputs reflect new values
    await expect(rowA.locator('input[type="number"]').first()).toHaveValue('20');
    await expect(rowB.locator('input[type="number"]').first()).toHaveValue('1');
  });

  test('nominal — stock alert raised when quantity drops below threshold', async ({ page }) => {
    const rowA = page.locator('tr').filter({ hasText: 'Produit Stock A' });

    // Set quantity to 2 — below threshold of 3
    await rowA.locator('input[type="number"]').first().fill('2');
    await page.getByRole('button', { name: 'Enregistrer les modifications' }).click();

    await expect(page.getByText('1 stock(s) mis à jour.')).toBeVisible();
    // Alert panel should appear for Produit Stock A
    await expect(page.getByText('Alertes stock')).toBeVisible();
    await expect(page.getByText('Produit Stock A')).toBeVisible();
  });

  test('cancel — discard changes resets inputs to original values', async ({ page }) => {
    const rowA = page.locator('tr').filter({ hasText: 'Produit Stock A' });
    const qtyInput = rowA.locator('input[type="number"]').first();

    await qtyInput.fill('99');
    await expect(page.getByRole('button', { name: 'Enregistrer les modifications' })).toBeVisible();

    await page.getByRole('button', { name: 'Annuler les modifications' }).click();

    // Banner gone
    await expect(page.getByRole('button', { name: 'Enregistrer les modifications' })).not.toBeVisible();
    // Input reset to original value
    await expect(qtyInput).toHaveValue('10');
  });

  test('nominal — modifying value back to original removes it from pending changes', async ({ page }) => {
    const rowA = page.locator('tr').filter({ hasText: 'Produit Stock A' });
    const qtyInput = rowA.locator('input[type="number"]').first();

    await qtyInput.fill('20');
    await expect(page.getByText('1 modification(s) en attente')).toBeVisible();

    // Reset to original value
    await qtyInput.fill('10');
    // Banner should disappear since no pending changes remain
    await expect(page.getByRole('button', { name: 'Enregistrer les modifications' })).not.toBeVisible();
  });
});
