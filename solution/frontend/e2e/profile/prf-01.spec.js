import { test, expect } from '@playwright/test';
import { API_URL, registerAndActivateBuyerViaApi } from '../helpers/login.js';

// US-PRF-01 — Buyer updates their own profile.

/** Logs in via the buyer portal modal and waits for the session to be stored. */
async function loginBuyer(page, email, password) {
  await page.goto('/login');
  await page.getByLabel('Email').fill(email);
  await page.getByLabel('Mot de passe', { exact: true }).fill(password);
  await page.locator('form').getByRole('button', { name: 'Se connecter' }).click();
  // The login is a modal — URL stays at /login; wait for session to be stored.
  await page.waitForFunction(() => !!localStorage.getItem('buyer_session'));
}

test.describe('US-PRF-01 — Buyer profile', () => {

  test('nominal — pre-filled form, update personal info, shows confirmation', async ({ page }) => {
    const email = `prf01-${Date.now()}@shop-test.example`;
    const password = 'BuyerPass123!';

    await registerAndActivateBuyerViaApi(page, email, password);

    await loginBuyer(page, email, password);

    // Navigate to profile
    await page.goto('/profile');

    // Form is pre-filled with registration first/last name
    await expect(page.getByLabel('Prénom', { exact: true })).toHaveValue('Test');
    await expect(page.getByLabel('Nom', { exact: true })).toHaveValue('User');

    // Email field is read-only
    const emailInput = page.getByLabel('Adresse email');
    await expect(emailInput).toHaveValue(email);
    await expect(emailInput).toHaveAttribute('readonly');

    // Update first name and phone
    await page.getByLabel('Prénom', { exact: true }).fill('Alice');
    await page.getByLabel('Téléphone').fill('0601020304');

    await page.getByRole('button', { name: 'Enregistrer les modifications' }).click();

    await expect(page.getByRole('status')).toContainText('Profil mis à jour');
  });

  test('security tab — wrong current password shows error', async ({ page }) => {
    const email = `prf01b-${Date.now()}@shop-test.example`;
    const password = 'BuyerPass123!';

    await registerAndActivateBuyerViaApi(page, email, password);

    await loginBuyer(page, email, password);

    await page.goto('/profile');
    await page.getByRole('button', { name: 'Sécurité' }).click();

    await page.getByLabel('Mot de passe actuel').fill('WrongPassword!');
    await page.getByLabel('Nouveau mot de passe', { exact: true }).fill('NewPass123!');
    await page.getByLabel('Confirmer le nouveau mot de passe').fill('NewPass123!');
    await page.getByRole('button', { name: 'Changer le mot de passe' }).click();

    await expect(page.getByRole('alert')).toContainText('Mot de passe actuel incorrect');
  });

  test('security tab — successful password change, can login with new password', async ({ page }) => {
    const email = `prf01c-${Date.now()}@shop-test.example`;
    const password = 'BuyerPass123!';
    const newPassword = 'NewBuyerPass456!';

    await registerAndActivateBuyerViaApi(page, email, password);

    await loginBuyer(page, email, password);

    await page.goto('/profile');
    await page.getByRole('button', { name: 'Sécurité' }).click();

    await page.getByLabel('Mot de passe actuel').fill(password);
    await page.getByLabel('Nouveau mot de passe', { exact: true }).fill(newPassword);
    await page.getByLabel('Confirmer le nouveau mot de passe').fill(newPassword);
    await page.getByRole('button', { name: 'Changer le mot de passe' }).click();

    await expect(page.getByRole('status')).toContainText('Mot de passe mis à jour');

    // Login with new password succeeds
    const res = await page.request.post(`${API_URL}/api/auth/login`, {
      data: { email, password: newPassword },
    });
    expect(res.status()).toBe(200);
  });

});
