import { test, expect } from '@playwright/test';
import { API_URL, getAdminToken, registerAndActivateBuyerViaApi } from '../helpers/login.js';

// US-PRF-02 — Vendor updates their own profile (same rules as US-PRF-01).

/**
 * Creates an active VENDOR account: registers as buyer (sets password), force-activates,
 * then changes role to VENDOR via admin API.
 */
async function createActiveVendorViaApi(page, email, password) {
  await registerAndActivateBuyerViaApi(page, email, password);
  const token = await getAdminToken(page);
  const listRes = await page.request.get(`${API_URL}/api/admin/accounts`, {
    headers: { Authorization: `Bearer ${token}` },
  });
  const accounts = await listRes.json();
  const account = accounts.find(a => a.email === email);
  await page.request.patch(`${API_URL}/api/admin/accounts/${account.id}`, {
    headers: { Authorization: `Bearer ${token}` },
    data: { role: 'VENDOR' },
  });
  return account;
}

test.describe('US-PRF-02 — Vendor profile', () => {

  test('nominal — pre-filled form, update personal info, shows confirmation', async ({ page }) => {
    const email = `prf02-${Date.now()}@shop-test.example`;
    const password = 'VendorPass123!';

    await createActiveVendorViaApi(page, email, password);

    // Login via vendor portal
    await page.goto('/');
    await page.getByLabel('Adresse email').fill(email);
    await page.getByLabel('Mot de passe').fill(password);
    await page.getByRole('button', { name: 'Se connecter' }).click();
    await expect(page.getByRole('heading', { name: 'Tableau de bord' })).toBeVisible();

    // Navigate to profile
    await page.goto('/vendor/profile');

    // Form is pre-filled
    await expect(page.getByLabel('Prénom', { exact: true })).toHaveValue('Test');
    await expect(page.getByLabel('Nom', { exact: true })).toHaveValue('User');

    // Email is read-only
    const emailInput = page.getByLabel('Adresse email');
    await expect(emailInput).toHaveValue(email);
    await expect(emailInput).toHaveAttribute('readonly');

    // Update first name and city
    await page.getByLabel('Prénom', { exact: true }).fill('Marc');
    await page.getByLabel('Ville').fill('Lyon');

    await page.getByRole('button', { name: 'Enregistrer les modifications' }).click();

    await expect(page.getByRole('status')).toContainText('Profil mis à jour');
  });

  test('security tab — successful password change', async ({ page }) => {
    const email = `prf02b-${Date.now()}@shop-test.example`;
    const password = 'VendorPass123!';
    const newPassword = 'NewVendorPass456!';

    await createActiveVendorViaApi(page, email, password);

    await page.goto('/');
    await page.getByLabel('Adresse email').fill(email);
    await page.getByLabel('Mot de passe').fill(password);
    await page.getByRole('button', { name: 'Se connecter' }).click();
    await expect(page.getByRole('heading', { name: 'Tableau de bord' })).toBeVisible();

    await page.goto('/vendor/profile');
    await page.getByRole('button', { name: 'Sécurité' }).click();

    await page.getByLabel('Mot de passe actuel').fill(password);
    await page.getByLabel('Nouveau mot de passe', { exact: true }).fill(newPassword);
    await page.getByLabel('Confirmer le nouveau mot de passe').fill(newPassword);
    await page.getByRole('button', { name: 'Changer le mot de passe' }).click();

    await expect(page.getByRole('status')).toContainText('Mot de passe mis à jour');
  });

});
