import { test, expect } from '@playwright/test';

// US-REG-02 — Activate account via email link.

test.describe('US-REG-02 — Account activation', () => {

  test('token not found → shows already-active message with login link', async ({ page }) => {
    await page.goto('/activate?token=00000000-0000-0000-0000-000000000000');
    await expect(page.getByText("Votre compte est peut-être déjà actif.")).toBeVisible();
    await expect(page.getByRole('link', { name: 'Se connecter' })).toBeVisible();
  });

  test('expired token → shows error message and resend form', async ({ page }) => {
    await page.route('**/api/auth/activate', route =>
      route.fulfill({
        status: 410,
        contentType: 'application/json',
        body: JSON.stringify({ error: 'TOKEN_EXPIRED', message: 'expired' }),
      })
    );

    await page.goto('/activate?token=expired-token-uuid');
    await expect(page.getByText("Ce lien d'activation a expiré.")).toBeVisible();
    await expect(page.getByLabel("Votre adresse email")).toBeVisible();
    await expect(page.getByRole('button', { name: 'Recevoir un nouveau lien' })).toBeVisible();
  });

});
