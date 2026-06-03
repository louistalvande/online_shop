import { test, expect } from '@playwright/test';
import { loginAsAdmin } from '../helpers/login.js';

// ADM-PROFILE — Admin profile page: personal info update and password security tab.

async function openProfile(page) {
  await loginAsAdmin(page);
  await page.locator('.user-menu-trigger').click();
  await page.getByText('Configuration').click();
  await expect(page.getByRole('heading', { name: 'Mon profil' })).toBeVisible();
}

test.describe('Admin console — Profile page', () => {
  // ── Profile tab ──────────────────────────────────────────────────────────

  test('nominal — profile tab is shown by default with pre-filled fields', async ({ page }) => {
    await openProfile(page);
    await expect(page.locator('#firstName')).toHaveValue('Admin');
    await expect(page.locator('#lastName')).toHaveValue('System');
    await expect(page.locator('#email')).toHaveAttribute('readonly', '');
  });

  test('nominal — update phone saves and shows success message', async ({ page }) => {
    await openProfile(page);
    await page.locator('#phone').fill('+33 6 00 00 00 01');
    await page.getByRole('button', { name: 'Enregistrer les modifications' }).click();
    await expect(page.getByRole('status')).toContainText('Profil mis à jour.');
    // Restore empty phone
    await page.locator('#phone').fill('');
    await page.getByRole('button', { name: 'Enregistrer les modifications' }).click();
  });

  test('nominal — cancel button returns to dashboard', async ({ page }) => {
    await openProfile(page);
    await page.getByRole('button', { name: 'Annuler' }).click();
    await expect(page.getByRole('heading', { name: 'Vue d\'ensemble' })).toBeVisible();
  });

  test('nominal — language dropdown contains FR and EN options', async ({ page }) => {
    await openProfile(page);
    await expect(page.locator('#language option[value="FR"]')).toHaveText('Français');
    await expect(page.locator('#language option[value="EN"]')).toHaveText('English');
  });

  // ── Security tab ─────────────────────────────────────────────────────────

  test('nominal — security tab shows password fields', async ({ page }) => {
    await openProfile(page);
    await page.getByRole('button', { name: 'Sécurité' }).click();
    await expect(page.locator('#currentPassword')).toBeVisible();
    await expect(page.locator('#newPassword')).toBeVisible();
    await expect(page.locator('#confirmPassword')).toBeVisible();
  });

  test('error — mismatched passwords show validation error', async ({ page }) => {
    await openProfile(page);
    await page.getByRole('button', { name: 'Sécurité' }).click();
    await page.locator('#currentPassword').fill('Admin123456!');
    await page.locator('#newPassword').fill('NewPass@2026!');
    await page.locator('#confirmPassword').fill('DifferentPass@2026!');
    await page.getByRole('button', { name: 'Changer le mot de passe' }).click();
    await expect(page.getByRole('alert')).toContainText('Les mots de passe ne correspondent pas.');
  });

  test('error — wrong current password shows error', async ({ page }) => {
    await openProfile(page);
    await page.getByRole('button', { name: 'Sécurité' }).click();
    await page.locator('#currentPassword').fill('wrong-current-password');
    await page.locator('#newPassword').fill('NewPass@2026!');
    await page.locator('#confirmPassword').fill('NewPass@2026!');
    await page.getByRole('button', { name: 'Changer le mot de passe' }).click();
    await expect(page.getByRole('alert')).toContainText('Mot de passe actuel incorrect.');
  });
});
