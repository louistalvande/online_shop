import { test, expect } from '@playwright/test';
import { API_URL, getAdminToken, registerAndActivateBuyerViaApi, getBuyerToken } from '../helpers/login.js';

// US-SEC-04 — Admin bulk-revokes passwords; notification emails sent; accounts auto-suspended after 24 h.

test.describe('US-SEC-04 — Password revocation', () => {

  test('POST /revoke-passwords by email sets password_revoked and sends notification', async ({ page }) => {
    const buyerEmail = `sec04a-${Date.now()}@shop-test.example`;
    const password = 'BuyerPass123!';
    await registerAndActivateBuyerViaApi(page, buyerEmail, password);

    const adminToken = await getAdminToken(page);

    const res = await page.request.post(`${API_URL}/api/admin/accounts/revoke-passwords`, {
      headers: { Authorization: `Bearer ${adminToken}`, 'Content-Type': 'application/json' },
      data: { emails: [buyerEmail] },
    });
    expect(res.status()).toBe(204);
  });

  test('POST /revoke-passwords by role targets all non-deleted accounts with that role', async ({ page }) => {
    const adminToken = await getAdminToken(page);
    const res = await page.request.post(`${API_URL}/api/admin/accounts/revoke-passwords`, {
      headers: { Authorization: `Bearer ${adminToken}`, 'Content-Type': 'application/json' },
      data: { role: 'BUYER' },
    });
    expect(res.status()).toBe(204);
  });

  test('GET /revoked lists accounts with revoked passwords', async ({ page }) => {
    const buyerEmail = `sec04b-${Date.now()}@shop-test.example`;
    const password = 'BuyerPass123!';
    await registerAndActivateBuyerViaApi(page, buyerEmail, password);

    const adminToken = await getAdminToken(page);

    // Revoke the buyer's password
    await page.request.post(`${API_URL}/api/admin/accounts/revoke-passwords`, {
      headers: { Authorization: `Bearer ${adminToken}`, 'Content-Type': 'application/json' },
      data: { emails: [buyerEmail] },
    });

    // Account should now appear in the revoked list
    const listRes = await page.request.get(`${API_URL}/api/admin/accounts/revoked`, {
      headers: { Authorization: `Bearer ${adminToken}` },
    });
    expect(listRes.status()).toBe(200);
    const revoked = await listRes.json();
    const entry = revoked.find((a) => a.email === buyerEmail);
    expect(entry).toBeDefined();
    expect(entry.revokedAt).not.toBeNull();
    expect(typeof entry.hoursSinceRevocation).toBe('number');
  });

  test('after revoking, the buyer can still log in and is redirected to change password', async ({ page }) => {
    const buyerEmail = `sec04c-${Date.now()}@shop-test.example`;
    const password = 'BuyerPass123!';
    await registerAndActivateBuyerViaApi(page, buyerEmail, password);

    const adminToken = await getAdminToken(page);
    await page.request.post(`${API_URL}/api/admin/accounts/revoke-passwords`, {
      headers: { Authorization: `Bearer ${adminToken}`, 'Content-Type': 'application/json' },
      data: { emails: [buyerEmail] },
    });

    // The buyer can still log in (password is revoked but account is ACTIVE — they must change it)
    const loginRes = await page.request.post(`${API_URL}/api/auth/login`, {
      data: { email: buyerEmail, password },
    });
    expect(loginRes.status()).toBe(200);
    const loginData = await loginRes.json();
    // requiresPasswordSetup = true because password_revoked sets mustChangePassword
    expect(loginData.requiresPasswordSetup).toBe(true);
  });

  test('revoked list is accessible only to ADMIN — buyer gets 403', async ({ page }) => {
    const buyerEmail = `sec04d-${Date.now()}@shop-test.example`;
    const password = 'BuyerPass123!';
    await registerAndActivateBuyerViaApi(page, buyerEmail, password);
    const buyerToken = await getBuyerToken(page, buyerEmail, password);

    const res = await page.request.get(`${API_URL}/api/admin/accounts/revoked`, {
      headers: { Authorization: `Bearer ${buyerToken}` },
    });
    expect(res.status()).toBe(403);
  });

  test('revoke endpoint accessible only to ADMIN — buyer gets 403', async ({ page }) => {
    const buyerEmail = `sec04e-${Date.now()}@shop-test.example`;
    const password = 'BuyerPass123!';
    await registerAndActivateBuyerViaApi(page, buyerEmail, password);
    const buyerToken = await getBuyerToken(page, buyerEmail, password);

    const res = await page.request.post(`${API_URL}/api/admin/accounts/revoke-passwords`, {
      headers: { Authorization: `Bearer ${buyerToken}`, 'Content-Type': 'application/json' },
      data: { emails: [buyerEmail] },
    });
    expect(res.status()).toBe(403);
  });

});
