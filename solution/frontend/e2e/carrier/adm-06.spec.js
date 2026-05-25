import { test, expect } from '@playwright/test';
import { loginAsAdmin } from '../helpers/login.js';

// US-ADM-06 — Add a carrier with name, covered euro-zone countries, and tracking URL.

test.describe('US-ADM-06 — Add carrier', () => {
  test.beforeEach(async ({ page }) => {
    await loginAsAdmin(page);
    await page.goto('/admin/#carriers');
  });

  test('nominal — creates a carrier with active status', async ({ page }) => {
    const name = `Carrier-${Date.now()}`;

    await page.getByRole('button', { name: 'Ajouter un transporteur' }).click();
    await expect(page.getByRole('heading', { name: 'Ajouter un transporteur' })).toBeVisible();

    await page.getByLabel('Nom', { exact: true }).fill(name);
    await page.getByLabel('URL de suivi').fill('https://track.example.com');
    // Select the first available country checkbox
    await page.locator('input[type="checkbox"]').first().check();

    await page.getByRole('button', { name: 'Créer' }).click();

    await expect(page.getByText('Transporteur créé avec succès.')).toBeVisible();
    await expect(page.getByText(name)).toBeVisible();
    await expect(page.locator('tr').filter({ hasText: name }).getByText('Actif')).toBeVisible();
  });

  test('error — no country selected shows validation error', async ({ page }) => {
    await page.getByRole('button', { name: 'Ajouter un transporteur' }).click();
    await page.getByLabel('Nom', { exact: true }).fill(`Carrier-${Date.now()}`);
    await page.getByLabel('URL de suivi').fill('https://track.example.com');
    // Do NOT check any country

    await page.getByRole('button', { name: 'Créer' }).click();

    await expect(page.getByText('Sélectionnez au moins un pays.')).toBeVisible();
  });

  test('cancel — closes modal without creating a carrier', async ({ page }) => {
    await page.getByRole('button', { name: 'Ajouter un transporteur' }).click();
    await expect(page.getByRole('heading', { name: 'Ajouter un transporteur' })).toBeVisible();
    await page.getByRole('button', { name: 'Annuler' }).click();
    await expect(page.getByRole('heading', { name: 'Ajouter un transporteur' })).not.toBeVisible();
  });
});
