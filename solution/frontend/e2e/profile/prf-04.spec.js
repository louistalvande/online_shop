import { test, expect } from '@playwright/test';
import { API_URL, registerAndActivateBuyerViaApi, getBuyerToken, injectBuyerSession } from '../helpers/login.js';

// US-PRF-04 — Buyer opts in/out of marketing emails at registration and from their profile.

async function registerWithConsent(page, email, password, marketingConsent) {
  await page.request.post(`${API_URL}/api/auth/register`, {
    data: { email, password, firstName: 'Test', lastName: 'User', marketingConsent },
  });
  const token = await import('../helpers/login.js').then(m => m.getAdminToken(page));
  const listRes = await page.request.get(`${API_URL}/api/admin/accounts`, {
    headers: { Authorization: `Bearer ${token}` },
  });
  const accounts = await listRes.json();
  const account = accounts.find(a => a.email === email);
  await page.request.patch(`${API_URL}/api/admin/accounts/${account.id}/force-activate`, {
    headers: { Authorization: `Bearer ${token}` },
  });
  return account;
}

test.describe('US-PRF-04 — Marketing consent', () => {

  test('registration form — marketing consent checkbox is unchecked by default', async ({ page }) => {
    await page.goto('/register');
    const checkbox = page.getByRole('checkbox', { name: /offres et actualités commerciales/i });
    await expect(checkbox).not.toBeChecked();
  });

  test('nominal — register with consent=false, profile shows unchecked', async ({ page }) => {
    const email = `prf04a-${Date.now()}@shop-test.example`;
    const password = 'BuyerPass123!';

    await registerAndActivateBuyerViaApi(page, email, password);

    const token = await getBuyerToken(page, email, password);
    await injectBuyerSession(page, email, token);

    await page.goto('/profile');

    const checkbox = page.locator('input[type="checkbox"]').filter({ hasText: '' }).nth(0);
    // More reliable: find by label text
    const consentCheckbox = page.getByRole('checkbox', { name: /offres et actualités commerciales/i });
    await expect(consentCheckbox).not.toBeChecked();
  });

  test('nominal — register via UI with consent=true, profile shows checked', async ({ page }) => {
    const email = `prf04b-${Date.now()}@shop-test.example`;
    const password = 'BuyerPass123!';

    await registerWithConsent(page, email, password, true);

    const token = await getBuyerToken(page, email, password);
    await injectBuyerSession(page, email, token);

    await page.goto('/profile');

    const consentCheckbox = page.getByRole('checkbox', { name: /offres et actualités commerciales/i });
    await expect(consentCheckbox).toBeChecked();
  });

  test('nominal — enable consent from profile, shows confirmation, persists after reload', async ({ page }) => {
    const email = `prf04c-${Date.now()}@shop-test.example`;
    const password = 'BuyerPass123!';

    await registerAndActivateBuyerViaApi(page, email, password);

    const token = await getBuyerToken(page, email, password);
    await injectBuyerSession(page, email, token);

    await page.goto('/profile');

    const consentCheckbox = page.getByRole('checkbox', { name: /offres et actualités commerciales/i });
    await expect(consentCheckbox).not.toBeChecked();

    await consentCheckbox.check();
    await expect(consentCheckbox).toBeChecked();

    await page.getByRole('button', { name: 'Enregistrer les modifications' }).click();
    await expect(page.getByRole('status')).toContainText('Profil mis à jour');

    // Reload and verify persistence
    await page.reload();
    await expect(page.getByRole('checkbox', { name: /offres et actualités commerciales/i })).toBeChecked();
  });

  test('nominal — revoke consent from profile, persists after reload', async ({ page }) => {
    const email = `prf04d-${Date.now()}@shop-test.example`;
    const password = 'BuyerPass123!';

    await registerWithConsent(page, email, password, true);

    const token = await getBuyerToken(page, email, password);
    await injectBuyerSession(page, email, token);

    await page.goto('/profile');

    const consentCheckbox = page.getByRole('checkbox', { name: /offres et actualités commerciales/i });
    await expect(consentCheckbox).toBeChecked();

    await consentCheckbox.uncheck();
    await page.getByRole('button', { name: 'Enregistrer les modifications' }).click();
    await expect(page.getByRole('status')).toContainText('Profil mis à jour');

    await page.reload();
    await expect(page.getByRole('checkbox', { name: /offres et actualités commerciales/i })).not.toBeChecked();
  });

  test('revocation does not affect store access', async ({ page }) => {
    const email = `prf04e-${Date.now()}@shop-test.example`;
    const password = 'BuyerPass123!';

    await registerWithConsent(page, email, password, true);

    const token = await getBuyerToken(page, email, password);
    await injectBuyerSession(page, email, token);

    // Revoke consent
    await page.goto('/profile');
    await page.getByRole('checkbox', { name: /offres et actualités commerciales/i }).uncheck();
    await page.getByRole('button', { name: 'Enregistrer les modifications' }).click();
    await expect(page.getByRole('status')).toContainText('Profil mis à jour');

    // Catalog is still accessible
    await page.goto('/catalog');
    await expect(page).not.toHaveURL(/login/);
    await expect(page.locator('body')).not.toContainText('401');
  });

});
