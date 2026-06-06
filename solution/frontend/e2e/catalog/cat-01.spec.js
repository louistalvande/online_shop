import { test, expect } from '@playwright/test';
import {
  createActiveVendorViaApi, getVendorToken, injectVendorSession,
} from '../helpers/login.js';

// US-CAT-01 — Vendor creates a product.

test.describe('US-CAT-01 — Create product', () => {

  test('nominal — fills form, submits, product appears in list', async ({ page }) => {
    const email = `cat01-${Date.now()}@shop-test.example`;
    const password = 'sHp-E2e!Vnd-X9pZ';

    await createActiveVendorViaApi(page, email, password);
    const token = await getVendorToken(page, email, password);
    await injectVendorSession(page, email, token);
    await page.reload();

    // Navigate to catalog
    await page.getByRole('link', { name: 'Catalogue' }).click();
    await expect(page.getByRole('heading', { name: 'Catalogue produits' })).toBeVisible();

    // Open creation modal
    await page.getByRole('button', { name: 'Ajouter un produit' }).click();
    await expect(page.getByRole('heading', { name: 'Ajouter un produit' })).toBeVisible();

    // Fill required fields
    await page.getByLabel('Nom').fill('Aquarelle forêt');
    await page.getByLabel('Prix HT (€)').fill('29.90');
    await page.getByLabel('Quantité disponible').fill('10');
    await page.getByLabel('Seuil d\'alerte stock').fill('3');
    await page.getByLabel('Type de produit').fill('Aquarelle');

    // Submit
    await page.getByRole('button', { name: 'Enregistrer', exact: true }).click();

    // Modal closed, product appears in table
    await expect(page.getByRole('heading', { name: 'Ajouter un produit' })).not.toBeVisible();
    await expect(page.getByText('Aquarelle forêt').first()).toBeVisible();
    await expect(page.getByText('Publié').first()).toBeVisible();
  });

  test('error — name blank shows validation error', async ({ page }) => {
    const email = `cat01b-${Date.now()}@shop-test.example`;
    const password = 'sHp-E2e!Vnd-X9pZ';

    await createActiveVendorViaApi(page, email, password);
    const token = await getVendorToken(page, email, password);
    await injectVendorSession(page, email, token);
    await page.reload();

    await page.getByRole('link', { name: 'Catalogue' }).click();
    await page.getByRole('button', { name: 'Ajouter un produit' }).click();

    await page.getByLabel('Prix HT (€)').fill('29.90');
    await page.getByLabel('Quantité disponible').fill('5');
    await page.getByRole('button', { name: 'Enregistrer', exact: true }).click();

    await expect(page.getByText('Le nom du produit est obligatoire')).toBeVisible();
    await expect(page.getByRole('heading', { name: 'Ajouter un produit' })).toBeVisible();
  });

  test('error — price missing shows validation error', async ({ page }) => {
    const email = `cat01c-${Date.now()}@shop-test.example`;
    const password = 'sHp-E2e!Vnd-X9pZ';

    await createActiveVendorViaApi(page, email, password);
    const token = await getVendorToken(page, email, password);
    await injectVendorSession(page, email, token);
    await page.reload();

    await page.getByRole('link', { name: 'Catalogue' }).click();
    await page.getByRole('button', { name: 'Ajouter un produit' }).click();

    await page.getByLabel('Nom').fill('Test produit');
    await page.getByLabel('Quantité disponible').fill('5');
    await page.getByRole('button', { name: 'Enregistrer', exact: true }).click();

    await expect(page.getByText('Le prix HT doit être un nombre positif')).toBeVisible();
  });

  // BES-VND-016 — classify products by type and theme (free-text entry)
  test('type and theme — saved and visible in product details', async ({ page }) => {
    const email = `cat01d-${Date.now()}@shop-test.example`;
    const password = 'sHp-E2e!Vnd-X9pZ';

    await createActiveVendorViaApi(page, email, password);
    const token = await getVendorToken(page, email, password);
    await injectVendorSession(page, email, token);
    await page.reload();

    await page.getByRole('link', { name: 'Catalogue' }).click();
    await page.getByRole('button', { name: 'Ajouter un produit' }).click();

    await page.getByLabel('Nom').fill('Poster paysage');
    await page.getByLabel('Prix HT (€)').fill('19.90');
    await page.getByLabel('Quantité disponible').fill('5');
    // Fill both classification axes (BES-VND-016)
    await page.getByLabel('Type de produit').fill('Poster');
    await page.getByLabel('Thème / occasion').fill('Paysage');

    await page.getByRole('button', { name: 'Enregistrer', exact: true }).click();
    await expect(page.getByRole('heading', { name: 'Ajouter un produit' })).not.toBeVisible();

    // Re-open the product to verify both fields were persisted
    await page.getByText('Poster paysage').first().click();
    await expect(page.getByLabel('Type de produit')).toHaveValue('Poster');
    await expect(page.getByLabel('Thème / occasion')).toHaveValue('Paysage');
  });

  // BES-VND-016 — combobox: previously-used values appear as datalist suggestions
  test('type combobox — existing type appears as suggestion for next product', async ({ page }) => {
    const email = `cat01e-${Date.now()}@shop-test.example`;
    const password = 'sHp-E2e!Vnd-X9pZ';

    await createActiveVendorViaApi(page, email, password);
    const token = await getVendorToken(page, email, password);
    await injectVendorSession(page, email, token);
    await page.reload();

    await page.getByRole('link', { name: 'Catalogue' }).click();

    // Create first product with a specific type
    await page.getByRole('button', { name: 'Ajouter un produit' }).click();
    await page.getByLabel('Nom').fill('Carte anniversaire');
    await page.getByLabel('Prix HT (€)').fill('5.00');
    await page.getByLabel('Quantité disponible').fill('20');
    await page.getByLabel('Type de produit').fill('Carte');
    await page.getByLabel('Thème / occasion').fill('Anniversaire');
    await page.getByRole('button', { name: 'Enregistrer', exact: true }).click();
    await expect(page.getByRole('heading', { name: 'Ajouter un produit' })).not.toBeVisible();

    // Open a second product form — the type 'Carte' must appear in the datalist
    await page.getByRole('button', { name: 'Ajouter un produit' }).click();
    await expect(page.locator('#pf-category-list option[value="Carte"]')).toBeAttached();
    await expect(page.locator('#pf-theme-list option[value="Anniversaire"]')).toBeAttached();
  });

});
