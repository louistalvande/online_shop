import { test, expect } from '@playwright/test';
import { createActiveVendorViaApi, getVendorToken, createProductViaApi } from '../helpers/login.js';

// US-SHP-02 — Buyer filters products by category, max price, availability, or text search.

test.describe('US-SHP-02 — Filter and search catalog', () => {

  async function setupProducts(page) {
    const email = `shp02-${Date.now()}@shop-test.example`;
    const password = 'sHp-E2e!Vnd-X9pZ';
    await createActiveVendorViaApi(page, email, password);
    const token = await getVendorToken(page, email, password);

    await createProductViaApi(page, token, {
      name: 'Aquarelle montagne',
      priceExclTax: 49.90,
      quantity: 10,
      stockAlertThreshold: 2,
      category: 'Aquarelle',
      photoUrls: [],
    });
    await createProductViaApi(page, token, {
      name: 'Crayon forêt',
      priceExclTax: 19.90,
      quantity: 5,
      stockAlertThreshold: 1,
      category: 'Crayon',
      photoUrls: [],
    });
    await createProductViaApi(page, token, {
      name: 'Pastel épuisé',
      priceExclTax: 29.90,
      quantity: 0,
      stockAlertThreshold: 0,
      category: 'Pastel',
      photoUrls: [],
    });
  }

  test('filter — category filter narrows results', async ({ page }) => {
    await setupProducts(page);
    await page.goto('/catalog');

    await expect(page.getByText('Aquarelle montagne').first()).toBeVisible();

    // Fill category filter
    await page.getByLabel('Catégorie').fill('Aquarelle');
    await page.waitForTimeout(600); // debounce

    await expect(page.getByText('Aquarelle montagne').first()).toBeVisible();
    await expect(page.getByText('Crayon forêt')).not.toBeVisible();
  });

  test('filter — max price filter excludes expensive products', async ({ page }) => {
    await setupProducts(page);
    await page.goto('/catalog');

    // maxPrice TTC 30 € → HT ≤ 25 → excludes 49.90 HT (59.88 TTC), keeps 19.90 HT (23.88 TTC)
    await page.getByLabel('Prix max TTC (€)').fill('30');
    await page.waitForTimeout(600);

    await expect(page.getByText('Crayon forêt').first()).toBeVisible();
    await expect(page.getByText('Aquarelle montagne')).not.toBeVisible();
  });

  test('filter — inStockOnly hides out-of-stock products', async ({ page }) => {
    await setupProducts(page);
    await page.goto('/catalog');

    await expect(page.getByText('Pastel épuisé').first()).toBeVisible();

    await page.getByLabel('Disponible uniquement').check();
    await page.waitForTimeout(600);

    await expect(page.getByText('Pastel épuisé')).not.toBeVisible();
    await expect(page.getByText('Aquarelle montagne').first()).toBeVisible();
  });

  test('search — text search returns matching products', async ({ page }) => {
    await setupProducts(page);
    await page.goto('/catalog');

    await page.getByRole('searchbox').fill('montagne');
    await page.getByRole('button', { name: 'Rechercher' }).click();
    await page.waitForTimeout(600);

    await expect(page.getByText('Aquarelle montagne').first()).toBeVisible();
    await expect(page.getByText('Crayon forêt')).not.toBeVisible();
  });

  test('no results — shows empty state with reset button', async ({ page }) => {
    await page.goto('/catalog');

    await page.getByRole('searchbox').fill('xxxxxproduitinexistantxxxxx');
    await page.getByRole('button', { name: 'Rechercher' }).click();
    await page.waitForTimeout(600);

    await expect(page.getByText(/aucun produit/i)).toBeVisible();
    await expect(page.getByRole('button', { name: /réinitialiser/i }).first()).toBeVisible();
  });

  test('reset — reset button clears all filters and restores results', async ({ page }) => {
    await setupProducts(page);
    await page.goto('/catalog');

    // Apply a filter that hides everything
    await page.getByRole('searchbox').fill('xxxxxproduitinexistantxxxxx');
    await page.getByRole('button', { name: 'Rechercher' }).click();
    await page.waitForTimeout(600);
    await expect(page.getByText(/aucun produit/i)).toBeVisible();

    // Reset
    await page.getByRole('button', { name: /réinitialiser/i }).first().click();
    await page.waitForTimeout(600);

    await expect(page.getByText('Aquarelle montagne').first()).toBeVisible();
  });
});
