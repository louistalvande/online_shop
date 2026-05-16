import { test, expect } from '@playwright/test';
import { loginAsAdmin, createActiveAccountViaApi, suspendAccountViaApi } from '../helpers/login.js';

// US-ADM-03 — Reactivate a suspended account.

test.describe('US-ADM-03 — Reactivate account', () => {
  test('nominal — reactivates a suspended account, status changes to Actif', async ({ page }) => {
    const account = await createActiveAccountViaApi(page, {
      firstName: 'Reactivate',
      lastName: 'Test',
      email: `adm03-${Date.now()}@shop-test.example`,
      role: 'BUYER',
    });
    await suspendAccountViaApi(page, account.id);
    await loginAsAdmin(page);
    await page.goto('/#users');

    const row = page.locator('tr').filter({ hasText: account.email });
    await row.locator('button').last().click();
    await page.getByText('Réactiver').click();

    await expect(page.getByRole('heading', { name: 'Réactiver le compte' })).toBeVisible();
    await page.getByRole('button', { name: 'Réactiver' }).click();

    await expect(page.getByText('Compte réactivé.')).toBeVisible();
    await expect(row.getByText('Actif')).toBeVisible();
  });

  test('error — reactivate action is not available on an active account', async ({ page }) => {
    const account = await createActiveAccountViaApi(page, {
      firstName: 'ReactivateErr',
      lastName: 'Test',
      email: `adm03-err-${Date.now()}@shop-test.example`,
      role: 'BUYER',
    });
    await suspendAccountViaApi(page, account.id);
    await loginAsAdmin(page);
    await page.goto('/#users');

    const row = page.locator('tr').filter({ hasText: account.email });
    // Reactivate via UI first (account is currently SUSPENDED)
    await row.locator('button').last().click();
    await page.getByText('Réactiver').click();
    await page.getByRole('button', { name: 'Réactiver' }).click();
    await expect(page.getByText('Compte réactivé.')).toBeVisible();

    // Account is now ACTIVE — Réactiver must not be in the menu
    await row.locator('button').last().click();
    await expect(page.getByText('Réactiver')).not.toBeVisible();
    await page.keyboard.press('Escape');
  });
});
