import { test, expect } from '@playwright/test';
import { API_URL, registerAndActivateBuyerViaApi, getBuyerToken } from '../helpers/login.js';

// US-SEC-01 — JWT stored in HttpOnly cookie; sliding 30-min window; explicit logout blacklists token.

test.describe('US-SEC-01 — HttpOnly cookie auth', () => {

  test('login sets the jwt HttpOnly cookie', async ({ page }) => {
    const email = `sec01a-${Date.now()}@shop-test.example`;
    const password = 'BuyerPass123!';
    await registerAndActivateBuyerViaApi(page, email, password);

    const res = await page.request.post(`${API_URL}/api/auth/login`, {
      data: { email, password },
    });
    expect(res.status()).toBe(200);

    const cookies = await page.context().cookies();
    const jwtCookie = cookies.find(c => c.name === 'jwt');
    // Cookie may not be present when calling the API directly due to domain scoping — verify via Set-Cookie header instead
    const setCookie = res.headers()['set-cookie'] ?? '';
    expect(setCookie).toMatch(/jwt=/);
    expect(setCookie).toMatch(/HttpOnly/i);
    expect(setCookie).toMatch(/SameSite=Strict/i);
  });

  test('authenticated request works with cookie (no Authorization header)', async ({ page }) => {
    const email = `sec01b-${Date.now()}@shop-test.example`;
    const password = 'BuyerPass123!';
    await registerAndActivateBuyerViaApi(page, email, password);

    // Login via page.request — Playwright stores the cookie in its context
    await page.request.post(`${API_URL}/api/auth/login`, { data: { email, password } });

    // GET /api/me with no Authorization header — cookie should authenticate the request
    const meRes = await page.request.get(`${API_URL}/api/me`);
    expect(meRes.status()).toBe(200);
    const profile = await meRes.json();
    expect(profile.email).toBe(email);
  });

  test('logout blacklists the token — subsequent Bearer request returns 401', async ({ page }) => {
    const email = `sec01c-${Date.now()}@shop-test.example`;
    const password = 'BuyerPass123!';
    await registerAndActivateBuyerViaApi(page, email, password);

    const token = await getBuyerToken(page, email, password);

    // Verify token works before logout
    const before = await page.request.get(`${API_URL}/api/me`, {
      headers: { Authorization: `Bearer ${token}` },
    });
    expect(before.status()).toBe(200);

    // Logout — blacklists the token
    await page.request.post(`${API_URL}/api/auth/logout`, {
      headers: { Authorization: `Bearer ${token}` },
    });

    // Same token must now be rejected
    const after = await page.request.get(`${API_URL}/api/me`, {
      headers: { Authorization: `Bearer ${token}` },
    });
    expect(after.status()).toBe(401);
  });

  test('logout clears the jwt cookie', async ({ page }) => {
    const email = `sec01d-${Date.now()}@shop-test.example`;
    const password = 'BuyerPass123!';
    await registerAndActivateBuyerViaApi(page, email, password);

    await page.request.post(`${API_URL}/api/auth/login`, { data: { email, password } });

    const logoutRes = await page.request.post(`${API_URL}/api/auth/logout`);
    expect(logoutRes.status()).toBe(204);

    const setCookie = logoutRes.headers()['set-cookie'] ?? '';
    expect(setCookie).toMatch(/jwt=;|Max-Age=0/i);
  });

  test('expired or unauthenticated request returns 401', async ({ page }) => {
    const res = await page.request.get(`${API_URL}/api/me`);
    expect(res.status()).toBe(401);
  });

  test('login response includes role field — browser no longer needs to decode JWT', async ({ page }) => {
    const email = `sec01e-${Date.now()}@shop-test.example`;
    const password = 'BuyerPass123!';
    await registerAndActivateBuyerViaApi(page, email, password);

    const res = await page.request.post(`${API_URL}/api/auth/login`, { data: { email, password } });
    const data = await res.json();
    expect(data.role).toBe('BUYER');
    expect(data.email).toBe(email);
  });

});
