import { test, expect } from '@playwright/test';
import { loginAsAdmin, createCarrierViaApi } from '../helpers/login.js';

// US-ADM-07 — Edit an existing carrier (name, countries, tracking URL).

test.describe('US-ADM-07 — Edit carrier', () => {
  let carrier;

  test.beforeEach(async ({ page }) => {
    carrier = await createCarrierViaApi(page, {
      name: `EditCarrier-${Date.now()}`,
      trackingUrl: 'https://track.example.com',
      supportedCountries: ['FR'],
    });
    await loginAsAdmin(page);
    await page.goto('/admin/#carriers');
  });

  test('nominal — edits carrier name, changes are reflected immediately', async ({ page }) => {
    const updatedName = `${carrier.name}-updated`;
    const row = page.locator('tr').filter({ hasText: carrier.name });

    await row.locator('button').last().click();
    await page.getByText('Modifier').click();

    await expect(page.getByText('Modifier le transporteur')).toBeVisible();
    await page.getByLabel('Nom', { exact: true }).clear();
    await page.getByLabel('Nom', { exact: true }).fill(updatedName);
    await page.getByRole('button', { name: 'Enregistrer' }).click();

    await expect(page.getByText('Transporteur mis à jour.')).toBeVisible();
    await expect(page.getByText(updatedName)).toBeVisible();
  });

  test('nominal — edits tracking URL', async ({ page }) => {
    const row = page.locator('tr').filter({ hasText: carrier.name });

    await row.locator('button').last().click();
    await page.getByText('Modifier').click();

    await page.getByLabel('URL de suivi').clear();
    await page.getByLabel('URL de suivi').fill('https://new-track.example.com');
    await page.getByRole('button', { name: 'Enregistrer' }).click();

    await expect(page.getByText('Transporteur mis à jour.')).toBeVisible();
  });

  test('error — removing all countries shows validation error', async ({ page }) => {
    const row = page.locator('tr').filter({ hasText: carrier.name });

    await row.locator('button').last().click();
    await page.getByText('Modifier').click();

    // Wait for countries to load before unchecking
    await page.locator('input[type="checkbox"]').first().waitFor({ state: 'visible' });
    const checked = page.locator('input[type="checkbox"]:checked');
    const count = await checked.count();
    for (let i = 0; i < count; i++) {
      await checked.first().uncheck();
    }

    await page.getByRole('button', { name: 'Enregistrer' }).click();
    await expect(page.getByText('Sélectionnez au moins un pays.')).toBeVisible();
  });
});
