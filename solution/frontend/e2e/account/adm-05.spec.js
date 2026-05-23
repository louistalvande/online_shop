import { test, expect } from '@playwright/test';
import { loginAsAdmin, createAccountViaApi } from '../helpers/login.js';

// US-ADM-05 — Change a user's role (BUYER ↔ VENDOR).

test.describe('US-ADM-05 — Change account role', () => {
  let account;

  test.beforeEach(async ({ page }) => {
    account = await createAccountViaApi(page, {
      firstName: 'Role',
      lastName: 'Change',
      email: `adm05-${Date.now()}@shop-test.example`,
      role: 'BUYER',
    });
    await loginAsAdmin(page);
    await page.goto('/#users');
  });

  test('nominal — changes role from BUYER to VENDOR, takes effect immediately', async ({ page }) => {
    const row = page.locator('tr').filter({ hasText: account.email });
    await row.locator('button').last().click();
    await page.getByText('Éditer').click();

    await expect(page.getByText('Modifier le compte')).toBeVisible();
    await page.getByLabel('Rôle').selectOption('VENDOR');
    await page.getByRole('button', { name: 'Enregistrer' }).click();

    await expect(page.getByText('Compte mis à jour.')).toBeVisible();
    await expect(row.getByText('Vendeur')).toBeVisible();
  });

  test('nominal — changes role from VENDOR back to BUYER', async ({ page }) => {
    // First set to VENDOR
    const row = page.locator('tr').filter({ hasText: account.email });
    await row.locator('button').last().click();
    await page.getByText('Éditer').click();
    await page.getByLabel('Rôle').selectOption('VENDOR');
    const refreshPromise = page.waitForResponse(res => res.url().includes('/api/admin/accounts') && res.status() === 200);
    await page.getByRole('button', { name: 'Enregistrer' }).click();
    await refreshPromise;
    // Wait for table to reflect the VENDOR change
    await expect(row.getByText('Vendeur')).toBeVisible({ timeout: 10000 });

    // Then back to BUYER
    await row.locator('button').last().click();
    await page.getByText('Éditer').click();
    await page.getByLabel('Rôle').selectOption('BUYER');
    const refreshPromise2 = page.waitForResponse(res => res.url().includes('/api/admin/accounts') && res.status() === 200);
    await page.getByRole('button', { name: 'Enregistrer' }).click();
    await refreshPromise2;

    await expect(row.getByText('Acheteur')).toBeVisible({ timeout: 10000 });
  });
});
