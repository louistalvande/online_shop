import { test, expect } from '@playwright/test';
import { registerAndActivateBuyerViaApi } from '../helpers/login.js';

// BUYER-LOGIN — Login modal UI on the buyer portal.
// The modal is triggered by navigating to /login or clicking "Se connecter" in the header.

const BUYER_EMAIL    = `buyer-login-${Date.now()}@shop-test.example`;
const BUYER_PASSWORD = 'BuyerLogin!2026X';

test.describe('Buyer portal — Login modal', () => {
  test.beforeAll(async ({ browser }) => {
    const page = await browser.newPage();
    await registerAndActivateBuyerViaApi(page, BUYER_EMAIL, BUYER_PASSWORD);
    await page.close();
  });

  test.beforeEach(async ({ page }) => {
    // /login triggers App with openLogin=true, showing the modal
    await page.goto('/login');
    await expect(page.getByRole('heading', { name: 'Se connecter' })).toBeVisible({ timeout: 30000 });
  });

  test('nominal — modal shows email, password fields and submit button', async ({ page }) => {
    await expect(page.locator('input[type="email"]')).toBeVisible();
    await expect(page.locator('input[type="password"]')).toBeVisible();
    await expect(page.getByRole('button', { name: 'Se connecter' })).toBeVisible();
  });

  test('nominal — remember me checkbox is visible', async ({ page }) => {
    await expect(page.locator('input[type="checkbox"]')).toBeVisible();
  });

  test('nominal — forgot password link is visible', async ({ page }) => {
    await expect(page.getByText('Mot de passe oublié ?')).toBeVisible();
  });

  test('nominal — register link is visible', async ({ page }) => {
    await expect(page.getByText('S\'inscrire')).toBeVisible();
  });

  test('nominal — valid credentials log in and dismiss modal', async ({ page }) => {
    await page.locator('input[type="email"]').fill(BUYER_EMAIL);
    await page.locator('input[type="password"]').fill(BUYER_PASSWORD);
    await page.getByRole('button', { name: 'Se connecter' }).click();
    // Modal dismissed — header now shows user account menu
    await expect(page.getByRole('heading', { name: 'Se connecter' })).not.toBeVisible();
  });

  test('error — wrong password shows error message', async ({ page }) => {
    await page.locator('input[type="email"]').fill(BUYER_EMAIL);
    await page.locator('input[type="password"]').fill('wrong-password-xyz');
    await page.getByRole('button', { name: 'Se connecter' }).click();
    await expect(page.locator('.alert-error')).toContainText('Email ou mot de passe incorrect.');
  });

  test('nominal — clicking overlay closes the modal', async ({ page }) => {
    await page.locator('.modal-overlay').click({ position: { x: 5, y: 5 } });
    await expect(page.getByRole('heading', { name: 'Se connecter' })).not.toBeVisible();
  });

  test('nominal — header Se connecter opens the modal', async ({ page }) => {
    await page.goto('/');
    await page.locator('.user-menu-trigger').click();
    await page.getByText('Se connecter', { exact: true }).click();
    await expect(page.getByRole('heading', { name: 'Se connecter' })).toBeVisible();
  });
});
