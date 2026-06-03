import { test, expect } from '@playwright/test';
import { loginAsAdmin, registerAndActivateBuyerViaApi } from '../helpers/login.js';

// ADM-REVOKE-UI — Password revocation modal UI (US-SEC-04).

async function openRevokeModal(page) {
  await loginAsAdmin(page);
  await page.goto('/admin/#security');
  await page.getByRole('button', { name: 'Révoquer des mots de passe' }).click();
  await expect(page.getByRole('heading', { name: 'Révoquer des mots de passe' })).toBeVisible();
}

test.describe('Admin console — Password revocation modal', () => {
  test('nominal — revoke button opens the modal with title and actions', async ({ page }) => {
    await openRevokeModal(page);
    await expect(page.getByRole('button', { name: 'Révoquer', exact: true })).toBeVisible();
    await expect(page.getByRole('button', { name: 'Annuler' })).toBeVisible();
  });

  test('nominal — cancel button closes the modal', async ({ page }) => {
    await openRevokeModal(page);
    await page.getByRole('button', { name: 'Annuler' }).click();
    await expect(page.getByRole('heading', { name: 'Révoquer des mots de passe' })).not.toBeVisible();
  });

  test('error — submitting with no role and no email shows validation error', async ({ page }) => {
    await openRevokeModal(page);
    await page.getByRole('button', { name: 'Révoquer', exact: true }).click();
    await expect(page.getByText('Sélectionnez un rôle ou saisissez au moins une adresse email.')).toBeVisible();
  });

  test('nominal — role dropdown contains all roles', async ({ page }) => {
    await openRevokeModal(page);
    const select = page.locator('select').filter({ has: page.locator('option[value="BUYER"]') }).first();
    await expect(select.locator('option[value="BUYER"]')).toHaveText('Acheteur');
    await expect(select.locator('option[value="VENDOR"]')).toHaveText('Vendeur');
    await expect(select.locator('option[value="ADMIN"]')).toHaveText('Administrateur');
  });

  test('nominal — revoke by email shows success snackbar', async ({ page }) => {
    const email = `revoke-ui-${Date.now()}@shop-test.example`;
    await registerAndActivateBuyerViaApi(page, email, 'BuyerRevUI!99Zx');
    await openRevokeModal(page);
    await page.locator('textarea').fill(email);
    await page.getByRole('button', { name: 'Révoquer', exact: true }).click();
    await expect(page.getByText('Mots de passe révoqués.')).toBeVisible();
    await expect(page.getByRole('heading', { name: 'Révoquer des mots de passe' })).not.toBeVisible();
  });

  test('nominal — revoke by role shows success snackbar', async ({ page }) => {
    await openRevokeModal(page);
    const select = page.locator('select').filter({ has: page.locator('option[value="BUYER"]') }).first();
    await select.selectOption('BUYER');
    await page.getByRole('button', { name: 'Révoquer', exact: true }).click();
    await expect(page.getByText('Mots de passe révoqués.')).toBeVisible();
  });

  test('nominal — revoked account appears in section table after revocation', async ({ page }) => {
    const email = `revoke-table-${Date.now()}@shop-test.example`;
    await registerAndActivateBuyerViaApi(page, email, 'BuyerRevTbl!99Zx');
    await openRevokeModal(page);
    await page.locator('textarea').fill(email);
    await page.getByRole('button', { name: 'Révoquer', exact: true }).click();
    await expect(page.getByText('Mots de passe révoqués.')).toBeVisible();
    await expect(page.locator('#security').getByText(email)).toBeVisible();
  });
});
