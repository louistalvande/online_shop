import { test, expect } from '@playwright/test';
import { loginAsAdmin, createAccountViaApi, createActiveAccountViaApi } from '../helpers/login.js';

// US-ADM-02 — Suspend an active account.

test.describe('US-ADM-02 — Suspend account', () => {
  test('nominal — suspends an active account, status changes to Suspendu', async ({ page }) => {
    const account = await createActiveAccountViaApi(page, {
      firstName: 'Suspend',
      lastName: 'Test',
      email: `adm02-${Date.now()}@shop-test.example`,
      role: 'BUYER',
    });
    await loginAsAdmin(page);
    await page.goto('/#users');

    const row = page.locator('tr').filter({ hasText: account.email });
    await row.locator('button').last().click();
    await page.getByText('Suspendre').click();

    await expect(page.getByRole('heading', { name: 'Suspendre le compte' })).toBeVisible();
    await page.getByRole('button', { name: 'Suspendre' }).click();

    await expect(page.getByText('Compte suspendu.')).toBeVisible();
    await expect(row.getByText('Suspendu')).toBeVisible();
  });

  test('error — suspend action is not available on a pending account', async ({ page }) => {
    const account = await createAccountViaApi(page, {
      firstName: 'Pending',
      lastName: 'Test',
      email: `adm02-pending-${Date.now()}@shop-test.example`,
      role: 'BUYER',
    });
    await loginAsAdmin(page);
    await page.goto('/#users');

    const row = page.locator('tr').filter({ hasText: account.email });
    await row.locator('button').last().click();
    await expect(page.getByText('Suspendre')).not.toBeVisible();
    await page.keyboard.press('Escape');
  });
});
