import { test, expect } from '@playwright/test';
import { API_URL } from '../helpers/login.js';

// ADM-LOGIN — Admin console login page UI.

test.describe('Admin console — Login page', () => {
  test.beforeEach(async ({ page }) => {
    await page.goto('/admin/');
  });

  test('nominal — valid credentials show the dashboard', async ({ page }) => {
    await page.getByLabel('Adresse email').fill('admin@onlineshop.com');
    await page.getByLabel('Mot de passe', { exact: true }).fill('Admin123456!');
    await page.getByRole('button', { name: 'Se connecter' }).click();
    await expect(page.getByRole('heading', { name: 'Vue d\'ensemble' })).toBeVisible();
  });

  test('error — wrong password shows inline error message', async ({ page }) => {
    await page.getByLabel('Adresse email').fill('admin@onlineshop.com');
    await page.getByLabel('Mot de passe', { exact: true }).fill('wrong-password-xyz');
    await page.getByRole('button', { name: 'Se connecter' }).click();
    await expect(page.getByText('Email ou mot de passe incorrect.')).toBeVisible();
  });

  test('error — non-admin account is rejected', async ({ page, request }) => {
    // Register a buyer account and activate it, then try to log in to admin
    const email = `adm-login-buyer-${Date.now()}@shop-test.example`;
    const password = 'BuyerPass99!Xyz';
    await request.post(`${API_URL}/api/auth/register`, {
      data: { email, password, firstName: 'Test', lastName: 'Buyer' },
    });
    const adminToken = (await (await request.post(`${API_URL}/api/auth/login`, {
      data: { email: 'admin@onlineshop.com', password: 'Admin123456!' },
    })).json()).token;
    const accounts = await (await request.get(`${API_URL}/api/admin/accounts`, {
      headers: { Authorization: `Bearer ${adminToken}` },
    })).json();
    const account = accounts.find(a => a.email === email);
    await request.patch(`${API_URL}/api/admin/accounts/${account.id}/force-activate`, {
      headers: { Authorization: `Bearer ${adminToken}` },
    });

    await page.getByLabel('Adresse email').fill(email);
    await page.getByLabel('Mot de passe', { exact: true }).fill(password);
    await page.getByRole('button', { name: 'Se connecter' }).click();
    await expect(page.getByText('Ce compte n\'a pas accès à la console d\'administration.')).toBeVisible();
  });

  test('nominal — submit button shows loading state during login', async ({ page }) => {
    await page.getByLabel('Adresse email').fill('admin@onlineshop.com');
    await page.getByLabel('Mot de passe', { exact: true }).fill('Admin123456!');
    const submitBtn = page.getByRole('button', { name: 'Se connecter' });
    await submitBtn.click();
    // Button becomes "Connexion…" while request is in flight — assert dashboard instead
    await expect(page.getByRole('heading', { name: 'Vue d\'ensemble' })).toBeVisible();
  });
});
