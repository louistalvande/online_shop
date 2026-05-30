import { test, expect } from '@playwright/test';
import { API_URL, registerAndActivateBuyerViaApi, getAdminToken } from '../helpers/login.js';

// US-PRF-05 — Vendor exports the mailing list of consenting buyers as CSV.

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

async function registerBuyerWithConsent(page, email, password, marketingConsent) {
  await page.request.post(`${API_URL}/api/auth/register`, {
    data: { email, password, firstName: 'Test', lastName: 'User', marketingConsent },
  });
  const token = await getAdminToken(page);
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

async function loginAsVendor(page, email, password) {
  await page.goto('/');
  await page.getByLabel('Adresse email').fill(email);
  await page.getByLabel('Mot de passe').fill(password);
  await page.getByRole('button', { name: 'Se connecter' }).click();
  await expect(page.getByRole('heading', { name: 'Tableau de bord' })).toBeVisible();
}

test.describe('US-PRF-05 — Vendor mailing list export', () => {

  test('nominal — export button present on reports page', async ({ page }) => {
    const vendorEmail = `prf05-vendor-${Date.now()}@shop-test.example`;
    const password = 'BuyerPass123!';

    await createActiveVendorViaApi(page, vendorEmail, password);
    await loginAsVendor(page, vendorEmail, password);

    await page.getByRole('link', { name: 'Rapports' }).click();

    await expect(page.getByRole('button', { name: 'Exporter la liste mailing' })).toBeVisible();
  });

  test('nominal — CSV download triggered on button click', async ({ page }) => {
    const vendorEmail = `prf05b-vendor-${Date.now()}@shop-test.example`;
    const password = 'BuyerPass123!';
    const buyerEmail = `prf05b-buyer-${Date.now()}@shop-test.example`;

    await createActiveVendorViaApi(page, vendorEmail, password);
    await registerBuyerWithConsent(page, buyerEmail, password, true);
    await loginAsVendor(page, vendorEmail, password);

    await page.getByRole('link', { name: 'Rapports' }).click();

    const [download] = await Promise.all([
      page.waitForEvent('download'),
      page.getByRole('button', { name: 'Exporter la liste mailing' }).click(),
    ]);

    expect(download.suggestedFilename()).toBe('mailing-list.csv');
  });

  test('nominal — CSV contains consenting buyer, excludes non-consenting buyer', async ({ page }) => {
    const vendorEmail = `prf05c-vendor-${Date.now()}@shop-test.example`;
    const password = 'BuyerPass123!';
    const consentingEmail = `prf05c-yes-${Date.now()}@shop-test.example`;
    const nonConsentingEmail = `prf05c-no-${Date.now()}@shop-test.example`;

    await createActiveVendorViaApi(page, vendorEmail, password);
    await registerBuyerWithConsent(page, consentingEmail, password, true);
    await registerBuyerWithConsent(page, nonConsentingEmail, password, false);

    // Verify via REST API directly (faster than parsing a downloaded file)
    const vendorRes = await page.request.post(`${API_URL}/api/auth/login`, {
      data: { email: vendorEmail, password },
    });
    const { token } = await vendorRes.json();

    const exportRes = await page.request.get(`${API_URL}/api/vendor/marketing-consent/export`, {
      headers: { Authorization: `Bearer ${token}` },
    });
    expect(exportRes.status()).toBe(200);

    const csv = await exportRes.text();
    expect(csv).toContain(consentingEmail);
    expect(csv).not.toContain(nonConsentingEmail);
  });

  test('CSV contains header row with correct columns', async ({ page }) => {
    const vendorEmail = `prf05d-vendor-${Date.now()}@shop-test.example`;
    const password = 'BuyerPass123!';

    await createActiveVendorViaApi(page, vendorEmail, password);

    const vendorRes = await page.request.post(`${API_URL}/api/auth/login`, {
      data: { email: vendorEmail, password },
    });
    const { token } = await vendorRes.json();

    const exportRes = await page.request.get(`${API_URL}/api/vendor/marketing-consent/export`, {
      headers: { Authorization: `Bearer ${token}` },
    });
    const csv = await exportRes.text();
    const firstLine = csv.split('\n')[0];
    expect(firstLine).toBe('email,firstName,lastName');
  });

  test('buyer accounts are not allowed to call the export endpoint', async ({ page }) => {
    const buyerEmail = `prf05e-buyer-${Date.now()}@shop-test.example`;
    const password = 'BuyerPass123!';

    await registerBuyerWithConsent(page, buyerEmail, password, true);

    const buyerRes = await page.request.post(`${API_URL}/api/auth/login`, {
      data: { email: buyerEmail, password },
    });
    const { token } = await buyerRes.json();

    const exportRes = await page.request.get(`${API_URL}/api/vendor/marketing-consent/export`, {
      headers: { Authorization: `Bearer ${token}` },
    });
    expect(exportRes.status()).toBe(403);
  });

});
