export const API_URL = process.env.API_URL ?? 'http://localhost:8080';
const ADMIN_EMAIL = process.env.ADMIN_EMAIL ?? 'admin@onlineshop.com';
const ADMIN_PASSWORD = process.env.ADMIN_PASSWORD ?? 'Admin1234!';

/**
 * Logs in as admin through the UI.
 * Handles the first-login password-setup step if requiresPasswordSetup is returned.
 */
export async function loginAsAdmin(page) {
  await page.goto('/admin/');
  await page.getByLabel('Adresse email').fill(ADMIN_EMAIL);
  await page.getByLabel('Mot de passe', { exact: true }).fill(ADMIN_PASSWORD);
  await page.getByRole('button', { name: 'Se connecter' }).click();

  // Handle must_change_password setup step (first login only)
  const setupTitle = page.getByText('Définir votre mot de passe');
  if (await setupTitle.isVisible({ timeout: 3000 }).catch(() => false)) {
    await page.getByLabel('Nouveau mot de passe', { exact: true }).fill(ADMIN_PASSWORD);
    await page.getByLabel('Confirmer le mot de passe', { exact: true }).fill(ADMIN_PASSWORD);
    await page.getByRole('button', { name: 'Enregistrer et accéder' }).click();
  }

  await page.waitForSelector('text=Vue d\'ensemble', { timeout: 10000 });
}

/**
 * Obtains a short-lived admin JWT via the REST API (no UI).
 */
export async function getAdminToken(page) {
  const res = await page.request.post(`${API_URL}/api/auth/login`, {
    data: { email: ADMIN_EMAIL, password: ADMIN_PASSWORD },
  });
  const body = await res.json();
  return body.token;
}

/**
 * Creates an account via the REST API and returns the created AccountResponse.
 */
export async function createAccountViaApi(page, payload) {
  const token = await getAdminToken(page);
  const res = await page.request.post(`${API_URL}/api/admin/accounts`, {
    headers: { Authorization: `Bearer ${token}` },
    data: payload,
  });
  return res.json();
}

/**
 * Force-activates a PENDING account via the admin API (bypasses email verification).
 */
export async function activateAccountViaApi(page, id) {
  const token = await getAdminToken(page);
  const res = await page.request.patch(`${API_URL}/api/admin/accounts/${id}/force-activate`, {
    headers: { Authorization: `Bearer ${token}` },
  });
  return res.json();
}

/**
 * Creates an account via the REST API and immediately force-activates it.
 * Returns the AccountResponse with status ACTIVE.
 */
export async function createActiveAccountViaApi(page, payload) {
  const account = await createAccountViaApi(page, payload);
  await activateAccountViaApi(page, account.id);
  return account;
}

/**
 * Suspends an ACTIVE account via the admin API.
 */
export async function suspendAccountViaApi(page, id) {
  const token = await getAdminToken(page);
  const res = await page.request.patch(`${API_URL}/api/admin/accounts/${id}/suspend`, {
    headers: { Authorization: `Bearer ${token}` },
  });
  return res.json();
}

/**
 * Creates a carrier via the REST API and returns the created CarrierResponse.
 */
export async function createCarrierViaApi(page, payload) {
  const token = await getAdminToken(page);
  const res = await page.request.post(`${API_URL}/api/admin/carriers`, {
    headers: { Authorization: `Bearer ${token}` },
    data: payload,
  });
  return res.json();
}
