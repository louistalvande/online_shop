import { test, expect } from '@playwright/test';
import { loginAsAdmin, createAccountViaApi } from '../helpers/login.js';

// US-ADM-04 — Delete an account (active or suspended) with mandatory confirmation.

test.describe('US-ADM-04 — Delete account', () => {
  let account;

  test.beforeEach(async ({ page }) => {
    account = await createAccountViaApi(page, {
      firstName: 'Delete',
      lastName: 'Test',
      email: `adm04-${Date.now()}@shop-test.example`,
      role: 'BUYER',
    });
    await loginAsAdmin(page);
    await page.goto('/admin/#users');
  });

  test('nominal — deletes an account after confirmation, account disappears from list', async ({ page }) => {
    const row = page.locator('tr').filter({ hasText: account.email });
    await row.locator('button').last().click();
    await page.getByText('Supprimer').click();

    await expect(page.getByRole('heading', { name: 'Supprimer le compte' })).toBeVisible();
    await page.getByRole('button', { name: 'Supprimer' }).click();

    await expect(page.getByText('Compte supprimé.')).toBeVisible();
    // The list re-fetches after deletion; reload to assert the account is truly gone.
    await page.reload();
    await expect(page.locator('tr').filter({ hasText: account.email })).toHaveCount(0);
  });

  test('cancel — dismissing the confirmation dialog does not delete the account', async ({ page }) => {
    const row = page.locator('tr').filter({ hasText: account.email });
    await row.locator('button').last().click();
    await page.getByText('Supprimer').click();

    await expect(page.getByRole('heading', { name: 'Supprimer le compte' })).toBeVisible();
    await page.getByRole('button', { name: 'Annuler' }).click();

    await expect(page.getByRole('heading', { name: 'Supprimer le compte' })).not.toBeVisible();
    await expect(page.locator('tr').filter({ hasText: account.email })).toBeVisible();
  });
});
