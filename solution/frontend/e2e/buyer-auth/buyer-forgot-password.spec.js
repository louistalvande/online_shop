import { test, expect } from '@playwright/test';

// BUYER-FORGOT-PASSWORD — Forgot password page UI on the buyer portal.

test.describe('Buyer portal — Forgot password page', () => {
  test.beforeEach(async ({ page }) => {
    await page.goto('/forgot-password');
    await expect(page.getByRole('heading', { name: 'Mot de passe oublié' })).toBeVisible();
  });

  test('nominal — email input and submit button are visible', async ({ page }) => {
    await expect(page.locator('input[type="email"]')).toBeVisible();
    await expect(page.getByRole('button', { name: 'Envoyer le lien' })).toBeVisible();
  });

  test('nominal — back to login link is visible', async ({ page }) => {
    await expect(page.getByText('Retour à la connexion')).toBeVisible();
  });

  test('nominal — submitting a valid email shows success message', async ({ page }) => {
    await page.locator('input[type="email"]').fill('any@example.com');
    await page.getByRole('button', { name: 'Envoyer le lien' }).click();
    await expect(page.getByText('Si un compte correspond à cette adresse')).toBeVisible();
  });

  test('nominal — submitting unknown email also shows success (anti-enumeration)', async ({ page }) => {
    await page.locator('input[type="email"]').fill('no-such-user@example.com');
    await page.getByRole('button', { name: 'Envoyer le lien' }).click();
    await expect(page.getByText('Si un compte correspond à cette adresse')).toBeVisible();
  });

  test('nominal — back to login link navigates to login page', async ({ page }) => {
    await page.getByText('Retour à la connexion').click();
    await expect(page.getByRole('heading', { name: 'Se connecter' })).toBeVisible();
  });
});
