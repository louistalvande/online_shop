import { test, expect } from '@playwright/test';
import { loginAsAdmin, createActiveAccountViaApi } from '../helpers/login.js';

// ADM-USER-DETAIL — Clicking a user row opens the read-only UserDetailModal.

test.describe('Admin console — User detail modal', () => {
  let account;

  test.beforeAll(async ({ browser }) => {
    const page = await browser.newPage();
    account = await createActiveAccountViaApi(page, {
      firstName: 'Detail',
      lastName: 'ViewTest',
      email: `adm-detail-${Date.now()}@shop-test.example`,
      role: 'BUYER',
    });
    await page.close();
  });

  test.beforeEach(async ({ page }) => {
    await loginAsAdmin(page);
    await page.goto('/admin/#users');
  });

  test('nominal — clicking a user row opens the detail modal', async ({ page }) => {
    await page.locator('tr').filter({ hasText: account.email }).first().click();
    await expect(page.getByRole('button', { name: 'Fermer' })).toBeVisible();
  });

  test('nominal — modal shows the account full name', async ({ page }) => {
    await page.locator('tr').filter({ hasText: account.email }).first().click();
    await expect(page.getByRole('button', { name: 'Fermer' })).toBeVisible();
    // "Detail ViewTest" appears exactly once inside the modal overlay
    await expect(page.locator('div').filter({ hasText: /^Detail ViewTest$/ }).first()).toBeVisible();
  });

  test('nominal — close button dismisses the modal', async ({ page }) => {
    await page.locator('tr').filter({ hasText: account.email }).first().click();
    await expect(page.getByRole('button', { name: 'Fermer' })).toBeVisible();
    await page.getByRole('button', { name: 'Fermer' }).click();
    await expect(page.getByRole('button', { name: 'Fermer' })).not.toBeVisible();
  });
});
