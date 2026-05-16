import { test, expect } from '@playwright/test';
import { loginAsAdmin, createCarrierViaApi } from '../helpers/login.js';

// US-ADM-09 — Delete a carrier not referenced by any active shipment, with mandatory confirmation.

test.describe('US-ADM-09 — Delete carrier', () => {
  let carrier;

  test.beforeEach(async ({ page }) => {
    carrier = await createCarrierViaApi(page, {
      name: `DeleteCarrier-${Date.now()}`,
      trackingUrl: 'https://track.example.com',
      supportedCountries: ['FR'],
    });
    await loginAsAdmin(page);
    await page.goto('/#carriers');
  });

  test('nominal — deletes a carrier after confirmation, carrier disappears from list', async ({ page }) => {
    const row = page.locator('tr').filter({ hasText: carrier.name });

    await row.locator('button').last().click();
    await page.getByText('Supprimer').click();

    await expect(page.getByText('Supprimer le transporteur')).toBeVisible();
    await page.getByRole('button', { name: 'Supprimer' }).click();

    await expect(page.getByText('Transporteur supprimé.')).toBeVisible();
    await expect(page.getByText(carrier.name)).not.toBeVisible();
  });

  test('cancel — dismissing the confirmation dialog does not delete the carrier', async ({ page }) => {
    const row = page.locator('tr').filter({ hasText: carrier.name });

    await row.locator('button').last().click();
    await page.getByText('Supprimer').click();

    await expect(page.getByText('Supprimer le transporteur')).toBeVisible();
    await page.getByRole('button', { name: 'Annuler' }).click();

    await expect(page.getByText('Supprimer le transporteur')).not.toBeVisible();
    await expect(page.getByText(carrier.name)).toBeVisible();
  });
});
