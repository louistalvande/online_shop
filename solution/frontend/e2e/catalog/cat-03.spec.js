import { test, expect } from '@playwright/test';
import {
  createActiveVendorViaApi, getVendorToken, injectVendorSession, createProductViaApi,
} from '../helpers/login.js';

// US-CAT-03 — Vendor archives a product.

test.describe('US-CAT-03 — Archive product', () => {

  test('nominal — archive removes product from default list, visible when toggle enabled', async ({ page }) => {
    const ts = Date.now();
    const email = `cat03-${ts}@shop-test.example`;
    const password = 'sHp-E2e!Vnd-X9pZ';
    const productName = `Pastel à archiver ${ts}`;

    await createActiveVendorViaApi(page, email, password);
    const token = await getVendorToken(page, email, password);

    await createProductViaApi(page, token, {
      name: productName,
      priceExclTax: 19.00,
      quantity: 3,
      stockAlertThreshold: 0,
      photoUrls: [],
    });

    await injectVendorSession(page, email, token);
    await page.reload();
    await page.getByRole('link', { name: 'Catalogue' }).click();

    await expect(page.getByText(productName).first()).toBeVisible();

    // Click archive, confirm dialog
    page.on('dialog', dialog => dialog.accept());
    const row = page.getByText(productName).first().locator('..').locator('..');
    await row.getByRole('button', { name: 'Archiver' }).click();

    // Product disappears from default (published-only) view
    await expect(page.getByText(productName)).not.toBeVisible();

    // Enable "show archived" toggle
    await page.getByLabel('Afficher les produits archivés').check();
    const archivedProductRow = page.getByText(productName).first().locator('..').locator('..');
    await expect(archivedProductRow).toBeVisible();
    await expect(archivedProductRow.getByText('Archivé', { exact: true })).toBeVisible();
  });

  test('archived product has no Archive button', async ({ page }) => {
    const ts2 = Date.now();
    const email = `cat03b-${ts2}@shop-test.example`;
    const password = 'sHp-E2e!Vnd-X9pZ';
    const productName2 = `Produit à archiver ${ts2}`;

    await createActiveVendorViaApi(page, email, password);
    const token = await getVendorToken(page, email, password);

    await createProductViaApi(page, token, {
      name: productName2,
      priceExclTax: 12.00,
      quantity: 1,
      stockAlertThreshold: 0,
      photoUrls: [],
    });

    await injectVendorSession(page, email, token);
    await page.reload();
    await page.getByRole('link', { name: 'Catalogue' }).click();

    page.on('dialog', dialog => dialog.accept());
    const row2 = page.getByText(productName2).first().locator('..').locator('..');
    await row2.getByRole('button', { name: 'Archiver' }).click();

    await page.getByLabel('Afficher les produits archivés').check();

    // Archived row has no Archive button
    const archivedRow = page.getByText(productName2).first().locator('..').locator('..');
    await expect(archivedRow.getByRole('button', { name: 'Archiver' })).not.toBeVisible();
  });

});
