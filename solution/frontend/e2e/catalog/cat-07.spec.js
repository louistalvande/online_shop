import { test, expect } from '@playwright/test';
import { createActiveVendorViaApi, getVendorToken, injectVendorSession } from '../helpers/login.js';

// US-CAT-07 — Vendor exports the product catalogue as a CSV file.

test.describe('US-CAT-07 — Export CSV products', () => {

  test('nominal — export triggers download with correct filename pattern', async ({ page }) => {
    const email = `cat07-${Date.now()}@shop-test.example`;
    const password = 'sHp-E2e!Vnd-X9pZ';

    await createActiveVendorViaApi(page, email, password);
    const token = await getVendorToken(page, email, password);
    await injectVendorSession(page, email, token);
    await page.reload();

    await page.getByRole('link', { name: 'Catalogue' }).click();
    await expect(page.getByRole('heading', { name: 'Catalogue produits' })).toBeVisible();

    // Expect a file download when clicking the Export CSV button
    const [download] = await Promise.all([
      page.waitForEvent('download'),
      page.getByRole('button', { name: 'Exporter CSV' }).click(),
    ]);

    expect(download.suggestedFilename()).toMatch(/^catalogue_export_\d{8}\.csv$/);
  });

  test('exported file contains UTF-8 BOM and correct header', async ({ page }) => {
    const email = `cat07b-${Date.now()}@shop-test.example`;
    const password = 'sHp-E2e!Vnd-X9pZ';

    await createActiveVendorViaApi(page, email, password);
    const token = await getVendorToken(page, email, password);
    await injectVendorSession(page, email, token);
    await page.reload();

    await page.getByRole('link', { name: 'Catalogue' }).click();

    const [download] = await Promise.all([
      page.waitForEvent('download'),
      page.getByRole('button', { name: 'Exporter CSV' }).click(),
    ]);

    const stream = await download.createReadStream();
    const chunks = [];
    for await (const chunk of stream) chunks.push(chunk);
    const buffer = Buffer.concat(chunks);

    // First 3 bytes must be the UTF-8 BOM (0xEF 0xBB 0xBF)
    expect(buffer[0]).toBe(0xEF);
    expect(buffer[1]).toBe(0xBB);
    expect(buffer[2]).toBe(0xBF);

    // After BOM, first line must be the header
    const csvContent = buffer.subarray(3).toString('utf8');
    const firstLine = csvContent.split('\n')[0];
    expect(firstLine).toBe('id,nom,description,prix,categorie,quantite,seuil_alerte,statut');
  });

  test('exported file includes both published and archived products', async ({ page, request }) => {
    const email = `cat07c-${Date.now()}@shop-test.example`;
    const password = 'sHp-E2e!Vnd-X9pZ';

    await createActiveVendorViaApi(page, email, password);
    const token = await getVendorToken(page, email, password);

    // Create one product and archive it via API
    const createRes = await request.post('/api/vendor/products', {
      headers: { Authorization: `Bearer ${token}`, 'Content-Type': 'application/json' },
      data: { name: 'Produit publié', priceExclTax: 10.00, quantity: 5, stockAlertThreshold: 0, photoUrls: [] },
    });
    const { id: publishedId } = await createRes.json();

    const archiveRes = await request.post('/api/vendor/products', {
      headers: { Authorization: `Bearer ${token}`, 'Content-Type': 'application/json' },
      data: { name: 'Produit archivé', priceExclTax: 20.00, quantity: 0, stockAlertThreshold: 0, photoUrls: [] },
    });
    const { id: archivedId } = await archiveRes.json();
    await request.patch(`/api/vendor/products/${archivedId}/archive`, {
      headers: { Authorization: `Bearer ${token}` },
    });

    await injectVendorSession(page, email, token);
    await page.reload();

    await page.getByRole('link', { name: 'Catalogue' }).click();

    const [download] = await Promise.all([
      page.waitForEvent('download'),
      page.getByRole('button', { name: 'Exporter CSV' }).click(),
    ]);

    const stream = await download.createReadStream();
    const chunks = [];
    for await (const chunk of stream) chunks.push(chunk);
    const csvContent = Buffer.concat(chunks).subarray(3).toString('utf8');

    expect(csvContent).toContain('Produit publié');
    expect(csvContent).toContain('PUBLISHED');
    expect(csvContent).toContain('Produit archivé');
    expect(csvContent).toContain('ARCHIVED');
  });

});
