import { test, expect } from '@playwright/test';
import {
  createActiveVendorViaApi, getVendorToken, injectVendorSession, createProductViaApi,
} from '../helpers/login.js';

// US-CAT-03 — Vendor archives a product.

test.describe('US-CAT-03 — Archive product', () => {

  test('nominal — archive removes product from default list, visible when toggle enabled', async ({ page }) => {
    const email = `cat03-${Date.now()}@shop-test.example`;
    const password = 'VendorPass123!';

    await createActiveVendorViaApi(page, email, password);
    const token = await getVendorToken(page, email, password);

    await createProductViaApi(page, token, {
      name: 'Pastel à archiver',
      priceExclTax: 19.00,
      quantity: 3,
      stockAlertThreshold: 0,
      photoUrls: [],
    });

    await injectVendorSession(page, email, token);
    await page.reload();
    await page.getByRole('link', { name: 'Catalogue' }).click();

    await expect(page.getByText('Pastel à archiver')).toBeVisible();

    // Click archive, confirm dialog
    page.on('dialog', dialog => dialog.accept());
    const row = page.getByText('Pastel à archiver').locator('..').locator('..');
    await row.getByRole('button', { name: 'Archiver' }).click();

    // Product disappears from default (published-only) view
    await expect(page.getByText('Pastel à archiver')).not.toBeVisible();

    // Enable "show archived" toggle
    await page.getByLabel('Afficher les produits archivés').check();
    await expect(page.getByText('Pastel à archiver')).toBeVisible();
    await expect(page.getByText('Archivé', { exact: true })).toBeVisible();
  });

  test('archived product has no Archive button', async ({ page }) => {
    const email = `cat03b-${Date.now()}@shop-test.example`;
    const password = 'VendorPass123!';

    await createActiveVendorViaApi(page, email, password);
    const token = await getVendorToken(page, email, password);

    await createProductViaApi(page, token, {
      name: 'Produit à archiver',
      priceExclTax: 12.00,
      quantity: 1,
      stockAlertThreshold: 0,
      photoUrls: [],
    });

    await injectVendorSession(page, email, token);
    await page.reload();
    await page.getByRole('link', { name: 'Catalogue' }).click();

    page.on('dialog', dialog => dialog.accept());
    const row = page.getByText('Produit à archiver').locator('..').locator('..');
    await row.getByRole('button', { name: 'Archiver' }).click();

    await page.getByLabel('Afficher les produits archivés').check();

    // Archived row has no Archive button
    const archivedRow = page.getByText('Produit à archiver').locator('..').locator('..');
    await expect(archivedRow.getByRole('button', { name: 'Archiver' })).not.toBeVisible();
  });

});
