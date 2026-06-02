import { test, expect } from '@playwright/test';
import { createActiveVendorViaApi, getVendorToken, injectVendorSession, createProductViaApi } from '../helpers/login.js';
import path from 'path';
import { fileURLToPath } from 'url';
import fs from 'fs';
import os from 'os';

// US-CAT-06 — Vendor imports products from a CSV file.

const VALID_HEADER = 'id,nom,description,prix,categorie,quantite,seuil_alerte';

/** Writes a temporary CSV file and returns its absolute path. */
function writeTempCsv(content) {
  const tmpPath = path.join(os.tmpdir(), `cat06-${Date.now()}.csv`);
  fs.writeFileSync(tmpPath, content, 'utf8');
  return tmpPath;
}

test.describe('US-CAT-06 — Import CSV products', () => {

  test('nominal — valid CSV creates products and shows report', async ({ page }) => {
    const email = `cat06-${Date.now()}@shop-test.example`;
    const password = 'sHp-E2e!Vnd-X9pZ';

    await createActiveVendorViaApi(page, email, password);
    const token = await getVendorToken(page, email, password);
    await injectVendorSession(page, email, token);
    await page.reload();

    await page.getByRole('link', { name: 'Catalogue' }).click();
    await expect(page.getByRole('heading', { name: 'Catalogue produits' })).toBeVisible();

    // Open CSV import modal
    await page.getByRole('button', { name: 'Importer CSV' }).click();
    await expect(page.getByRole('heading', { name: 'Importer des produits via CSV' })).toBeVisible();

    // Upload a valid CSV with two products (no id → creation path)
    const csvContent = [
      VALID_HEADER,
      ',Aquarelle forêt,,29.90,Aquarelle,10,3',
      ',Huile sur toile,Belle peinture,49.00,,5,0',
    ].join('\n');
    const csvPath = writeTempCsv(csvContent);

    const fileInput = page.locator('input[type="file"]');
    await fileInput.setInputFiles(csvPath);
    fs.unlinkSync(csvPath);

    // Report should appear with 2 created, 0 updated, 0 errors
    await expect(page.getByText('2 produit(s) créé(s), 0 mis à jour, 0 erreur(s).')).toBeVisible();
    await expect(page.getByText('Aquarelle forêt').first()).toBeVisible();
    await expect(page.getByText('Huile sur toile').first()).toBeVisible();

    // Close modal — products should appear in the list
    await page.getByRole('button', { name: 'Fermer' }).click();
    await expect(page.getByRole('heading', { name: 'Importer des produits via CSV' })).not.toBeVisible();
    await expect(page.getByText('Aquarelle forêt').first()).toBeVisible();
  });

  test('partial import — valid rows created, invalid rows show error', async ({ page }) => {
    const email = `cat06b-${Date.now()}@shop-test.example`;
    const password = 'sHp-E2e!Vnd-X9pZ';

    await createActiveVendorViaApi(page, email, password);
    const token = await getVendorToken(page, email, password);
    await injectVendorSession(page, email, token);
    await page.reload();

    await page.getByRole('link', { name: 'Catalogue' }).click();
    await page.getByRole('button', { name: 'Importer CSV' }).click();

    const csvContent = [
      VALID_HEADER,
      ',Produit valide,,15.00,,0,0',
      ',,Pas de nom,10.00,,0,0',   // missing name → error
    ].join('\n');
    const csvPath = writeTempCsv(csvContent);

    const fileInput = page.locator('input[type="file"]');
    await fileInput.setInputFiles(csvPath);
    fs.unlinkSync(csvPath);

    await expect(page.getByText('1 produit(s) créé(s), 0 mis à jour, 1 erreur(s).')).toBeVisible();

    // Row 2: created (second table on the page is the CSV result table)
    const resultTable = page.locator('table').last();
    const rows = resultTable.locator('tbody tr');
    await expect(rows.nth(0).getByText('Créé')).toBeVisible();
    // Row 3: error
    await expect(rows.nth(1).getByText('Erreur')).toBeVisible();
    await expect(rows.nth(1).getByText(/nom/i)).toBeVisible();
  });

  test('error — invalid header shows error message, no products imported', async ({ page }) => {
    const email = `cat06c-${Date.now()}@shop-test.example`;
    const password = 'sHp-E2e!Vnd-X9pZ';

    await createActiveVendorViaApi(page, email, password);
    const token = await getVendorToken(page, email, password);
    await injectVendorSession(page, email, token);
    await page.reload();

    await page.getByRole('link', { name: 'Catalogue' }).click();
    await page.getByRole('button', { name: 'Importer CSV' }).click();

    // CSV with wrong header
    const csvContent = 'name,desc,price\nProduit A,desc,10.00\n';
    const csvPath = writeTempCsv(csvContent);

    const fileInput = page.locator('input[type="file"]');
    await fileInput.setInputFiles(csvPath);
    fs.unlinkSync(csvPath);

    await expect(page.getByText(/En-tête CSV invalide/i)).toBeVisible();
  });

  test('cancel — closes modal without importing', async ({ page }) => {
    const email = `cat06d-${Date.now()}@shop-test.example`;
    const password = 'sHp-E2e!Vnd-X9pZ';

    await createActiveVendorViaApi(page, email, password);
    const token = await getVendorToken(page, email, password);
    await injectVendorSession(page, email, token);
    await page.reload();

    await page.getByRole('link', { name: 'Catalogue' }).click();
    await page.getByRole('button', { name: 'Importer CSV' }).click();
    await expect(page.getByRole('heading', { name: 'Importer des produits via CSV' })).toBeVisible();

    await page.getByRole('button', { name: 'Annuler' }).click();
    await expect(page.getByRole('heading', { name: 'Importer des produits via CSV' })).not.toBeVisible();
  });

  test('stock merge by id — CSV row with id updates stock only', async ({ page }) => {
    const email = `cat06e-${Date.now()}@shop-test.example`;
    const password = 'sHp-E2e!Vnd-X9pZ';

    await createActiveVendorViaApi(page, email, password);
    const token = await getVendorToken(page, email, password);
    await injectVendorSession(page, email, token);
    await page.reload();

    // Create a product via API so we have a known id
    const product = await createProductViaApi(page, token, {
      name: 'Tableau stock initial',
      priceExclTax: 35.00,
      quantity: 2,
      stockAlertThreshold: 1,
      photoUrls: [],
    });
    const productId = product.id;

    await page.getByRole('link', { name: 'Catalogue' }).click();
    await page.getByRole('button', { name: 'Importer CSV' }).click();
    await expect(page.getByRole('heading', { name: 'Importer des produits via CSV' })).toBeVisible();

    // Import CSV with the known id → stock-only merge
    const csvContent = [
      VALID_HEADER,
      `${productId},,,,,50,5`,
    ].join('\n');
    const csvPath = writeTempCsv(csvContent);

    const fileInput = page.locator('input[type="file"]');
    await fileInput.setInputFiles(csvPath);
    fs.unlinkSync(csvPath);

    // Report: 0 created, 1 updated, 0 errors
    await expect(page.getByText('0 produit(s) créé(s), 1 mis à jour, 0 erreur(s).')).toBeVisible();

    // The result row should show "Mis à jour"
    const resultTable = page.locator('table').last();
    const rows = resultTable.locator('tbody tr');
    await expect(rows.nth(0).getByText('Mis à jour')).toBeVisible();
    await expect(rows.nth(0).getByText('Tableau stock initial')).toBeVisible();
  });

});
