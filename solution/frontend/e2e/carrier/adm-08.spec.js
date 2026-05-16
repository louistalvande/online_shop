import { test, expect } from '@playwright/test';
import { loginAsAdmin, createCarrierViaApi, getAdminToken, API_URL } from '../helpers/login.js';

// US-ADM-08 — Deactivate a carrier (without deleting it). Also covers re-activation.

test.describe('US-ADM-08 — Deactivate / activate carrier', () => {
  let carrier;

  test.beforeEach(async ({ page }) => {
    carrier = await createCarrierViaApi(page, {
      name: `DeactivateCarrier-${Date.now()}`,
      trackingUrl: 'https://track.example.com',
      supportedCountries: ['FR'],
    });
    await loginAsAdmin(page);
    await page.goto('/#carriers');
  });

  test('nominal — deactivates an active carrier, status changes to Inactif', async ({ page }) => {
    const row = page.locator('tr').filter({ hasText: carrier.name });

    await row.locator('button').last().click();
    await page.getByText('Désactiver').click();

    await expect(page.getByText('Transporteur désactivé.')).toBeVisible();
    await expect(row.getByText('Inactif')).toBeVisible();
  });

  test('nominal — reactivates an inactive carrier, status changes to Actif', async ({ page }) => {
    // Deactivate via API first
    const token = await getAdminToken(page);
    await page.request.patch(`${API_URL}/api/admin/carriers/${carrier.id}/deactivate`, {
      headers: { Authorization: `Bearer ${token}` },
    });
    await page.reload();

    const row = page.locator('tr').filter({ hasText: carrier.name });
    await row.locator('button').last().click();
    await page.getByText('Activer').click();

    await expect(page.getByText('Transporteur activé.')).toBeVisible();
    await expect(row.getByText('Actif')).toBeVisible();
  });
});
