import { test, expect } from '@playwright/test';
import { API_URL } from '../helpers/login.js';

// US-REG-03 — Resend activation link when token has expired.

test.describe('US-REG-03 — Resend activation link', () => {

  test('expired token → enter email → resend succeeds and shows confirmation', async ({ page }) => {
    const email = `reg03-${Date.now()}@shop-test.example`;

    await page.request.post(`${API_URL}/api/auth/register`, {
      data: { email, password: 'Test123456!', firstName: 'Test', lastName: 'User' },
    });

    await page.route('**/api/auth/activate', route =>
      route.fulfill({
        status: 410,
        contentType: 'application/json',
        body: JSON.stringify({ error: 'TOKEN_EXPIRED', message: 'expired' }),
      })
    );

    await page.goto('/activate?token=expired-token-uuid');
    await expect(page.getByText("Ce lien d'activation a expiré.")).toBeVisible();

    await page.getByLabel('Votre adresse email').fill(email);
    await page.getByRole('button', { name: 'Recevoir un nouveau lien' }).click();

    await expect(page.getByText(/nouveau lien/)).toBeVisible();
  });

  test('resend with unknown email → shows confirmation without revealing absence of account', async ({ page }) => {
    await page.route('**/api/auth/activate', route =>
      route.fulfill({
        status: 410,
        contentType: 'application/json',
        body: JSON.stringify({ error: 'TOKEN_EXPIRED', message: 'expired' }),
      })
    );

    await page.goto('/activate?token=expired-token-uuid');
    await page.getByLabel('Votre adresse email').fill('nobody@shop-test.example');
    await page.getByRole('button', { name: 'Recevoir un nouveau lien' }).click();

    await expect(page.getByText(/nouveau lien/)).toBeVisible();
  });

});
