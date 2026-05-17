import { test, expect } from '@playwright/test';
import {
  createActiveVendorViaApi, getVendorToken, injectVendorSession,
} from '../helpers/login.js';

// US-CAT-01 — Vendor creates a product.

test.describe('US-CAT-01 — Create product', () => {

  test('nominal — fills form, submits, product appears in list', async ({ page }) => {
    const email = `cat01-${Date.now()}@shop-test.example`;
    const password = 'VendorPass123!';

    await createActiveVendorViaApi(page, email, password);
    const token = await getVendorToken(page, email, password);
    await injectVendorSession(page, email, token);
    await page.reload();

    // Navigate to catalog
    await page.getByText('Catalogue').click();
    await expect(page.getByRole('heading', { name: 'Catalogue produits' })).toBeVisible();

    // Open creation modal
    await page.getByRole('button', { name: 'Ajouter un produit' }).click();
    await expect(page.getByRole('heading', { name: 'Ajouter un produit' })).toBeVisible();

    // Fill required fields
    await page.getByLabel('Nom').fill('Aquarelle forêt');
    await page.getByLabel('Prix HT (€)').fill('29.90');
    await page.getByLabel('Quantité disponible').fill('10');
    await page.getByLabel('Seuil d\'alerte stock').fill('3');
    await page.getByLabel('Catégorie').fill('Aquarelle');

    // Submit
    await page.getByRole('button', { name: 'Enregistrer' }).click();

    // Modal closed, product appears in table
    await expect(page.getByRole('heading', { name: 'Ajouter un produit' })).not.toBeVisible();
    await expect(page.getByText('Aquarelle forêt')).toBeVisible();
    await expect(page.getByText('Publié')).toBeVisible();
  });

  test('error — name blank shows validation error', async ({ page }) => {
    const email = `cat01b-${Date.now()}@shop-test.example`;
    const password = 'VendorPass123!';

    await createActiveVendorViaApi(page, email, password);
    const token = await getVendorToken(page, email, password);
    await injectVendorSession(page, email, token);
    await page.reload();

    await page.getByText('Catalogue').click();
    await page.getByRole('button', { name: 'Ajouter un produit' }).click();

    await page.getByLabel('Prix HT (€)').fill('29.90');
    await page.getByLabel('Quantité disponible').fill('5');
    await page.getByRole('button', { name: 'Enregistrer' }).click();

    await expect(page.getByText('Le nom du produit est obligatoire')).toBeVisible();
    await expect(page.getByRole('heading', { name: 'Ajouter un produit' })).toBeVisible();
  });

  test('error — price missing shows validation error', async ({ page }) => {
    const email = `cat01c-${Date.now()}@shop-test.example`;
    const password = 'VendorPass123!';

    await createActiveVendorViaApi(page, email, password);
    const token = await getVendorToken(page, email, password);
    await injectVendorSession(page, email, token);
    await page.reload();

    await page.getByText('Catalogue').click();
    await page.getByRole('button', { name: 'Ajouter un produit' }).click();

    await page.getByLabel('Nom').fill('Test produit');
    await page.getByLabel('Quantité disponible').fill('5');
    await page.getByRole('button', { name: 'Enregistrer' }).click();

    await expect(page.getByText('Le prix HT doit être un nombre positif')).toBeVisible();
  });

});
