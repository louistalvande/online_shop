export const API_URL = process.env.API_URL ?? 'http://localhost:8080';
const ADMIN_EMAIL = process.env.ADMIN_EMAIL ?? 'admin@onlineshop.com';
const ADMIN_PASSWORD = process.env.ADMIN_PASSWORD ?? 'Admin123456!';

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
 * Registers a buyer account via the auth endpoint (password set at registration), then
 * force-activates it via the admin API so it can log in immediately.
 * Returns the account object from the admin list.
 *
 * @param {object} page      Playwright Page
 * @param {string} email     the buyer's email
 * @param {string} password  the plaintext password (min 12 chars)
 */
export async function registerAndActivateBuyerViaApi(page, email, password) {
  // Step 1 — self-register (sets passwordHash in DB)
  await page.request.post(`${API_URL}/api/auth/register`, {
    data: { email, password, firstName: 'Test', lastName: 'User' },
  });

  // Step 2 — find the account by email via admin API
  const token = await getAdminToken(page);
  const listRes = await page.request.get(`${API_URL}/api/admin/accounts`, {
    headers: { Authorization: `Bearer ${token}` },
  });
  const accounts = await listRes.json();
  const account = accounts.find((a) => a.email === email);

  // Step 3 — force-activate so the account is ACTIVE
  await page.request.patch(`${API_URL}/api/admin/accounts/${account.id}/force-activate`, {
    headers: { Authorization: `Bearer ${token}` },
  });
  return account;
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

/**
 * Creates an active vendor account by registering as a buyer (which sets the password),
 * activating the account, then changing the role to VENDOR via admin API.
 *
 * @param {object} page      Playwright Page
 * @param {string} email     the vendor's email
 * @param {string} password  the plaintext password (min 12 chars)
 */
export async function createActiveVendorViaApi(page, email, password) {
  await registerAndActivateBuyerViaApi(page, email, password);
  const token = await getAdminToken(page);
  const listRes = await page.request.get(`${API_URL}/api/admin/accounts`, {
    headers: { Authorization: `Bearer ${token}` },
  });
  const accounts = await listRes.json();
  const account = accounts.find((a) => a.email === email);
  await page.request.patch(`${API_URL}/api/admin/accounts/${account.id}`, {
    headers: { Authorization: `Bearer ${token}` },
    data: { role: 'VENDOR' },
  });
  return account;
}

/**
 * Obtains a vendor JWT by logging in via the REST API.
 *
 * @param {object} page      Playwright Page
 * @param {string} email     vendor email
 * @param {string} password  vendor password
 */
export async function getVendorToken(page, email, password) {
  const res = await page.request.post(`${API_URL}/api/auth/login`, {
    data: { email, password },
  });
  const body = await res.json();
  return body.token;
}

/**
 * Injects a vendor session into localStorage so tests can skip the UI login step.
 *
 * @param {object} page      Playwright Page
 * @param {string} email     vendor email
 * @param {string} token     vendor JWT
 */
export async function injectVendorSession(page, email, token) {
  await page.goto('/');
  await page.evaluate(({ email, token }) => {
    localStorage.setItem('vendor_session', JSON.stringify({ email, token }));
  }, { email, token });
}

/**
 * Obtains a buyer JWT by logging in via the REST API.
 *
 * @param {object} page      Playwright Page
 * @param {string} email     buyer email
 * @param {string} password  buyer password
 */
export async function getBuyerToken(page, email, password) {
  const res = await page.request.post(`${API_URL}/api/auth/login`, {
    data: { email, password },
  });
  const body = await res.json();
  return body.token;
}

/**
 * Injects a buyer session into localStorage so tests can skip the UI login step.
 *
 * @param {object} page   Playwright Page
 * @param {string} email  buyer email
 * @param {string} token  buyer JWT
 */
export async function injectBuyerSession(page, email, token) {
  await page.goto('/');
  await page.evaluate(({ email, token }) => {
    localStorage.setItem('buyer_session', JSON.stringify({ email, token }));
  }, { email, token });
}

/**
 * Creates a product for a vendor via the REST API.
 *
 * @param {object} page      Playwright Page
 * @param {string} token     vendor JWT
 * @param {object} payload   product fields
 */
export async function createProductViaApi(page, token, payload) {
  const res = await page.request.post(`${API_URL}/api/vendor/products`, {
    headers: { Authorization: `Bearer ${token}`, 'Content-Type': 'application/json' },
    data: payload,
  });
  return res.json();
}
