import { test, expect } from '@playwright/test';
import { API_URL, registerAndActivateBuyerViaApi, getBuyerToken } from '../helpers/login.js';

// US-SEC-02 — CSRF protection: SameSite=Strict cookie + CORS restriction on unknown origins.

test.describe('US-SEC-02 — CORS and cookie security attributes', () => {

  test('login Set-Cookie carries HttpOnly and SameSite=Strict', async ({ page }) => {
    const email = `sec02a-${Date.now()}@shop-test.example`;
    const password = 'BuyerPass123!';
    await registerAndActivateBuyerViaApi(page, email, password);

    const res = await page.request.post(`${API_URL}/api/auth/login`, {
      data: { email, password },
    });
    expect(res.status()).toBe(200);

    const setCookie = res.headers()['set-cookie'] ?? '';
    expect(setCookie).toMatch(/HttpOnly/i);
    expect(setCookie).toMatch(/SameSite=Strict/i);
  });

  test('preflight OPTIONS from an unknown origin is rejected', async ({ page }) => {
    const res = await page.request.fetch(`${API_URL}/api/auth/login`, {
      method: 'OPTIONS',
      headers: {
        Origin: 'http://evil.attacker.example',
        'Access-Control-Request-Method': 'POST',
        'Access-Control-Request-Headers': 'Content-Type',
      },
    });
    // Spring returns 403 for origins not in the allowlist
    expect(res.status()).toBe(403);
  });

  test('preflight OPTIONS from an allowed origin is accepted', async ({ page }) => {
    const allowedOrigin = process.env.BUYER_URL ?? 'http://buyer.localhost';
    const res = await page.request.fetch(`${API_URL}/api/auth/login`, {
      method: 'OPTIONS',
      headers: {
        Origin: allowedOrigin,
        'Access-Control-Request-Method': 'POST',
        'Access-Control-Request-Headers': 'Content-Type',
      },
    });
    expect([200, 204]).toContain(res.status());
    const acao = res.headers()['access-control-allow-origin'] ?? '';
    expect(acao).toBe(allowedOrigin);
  });

  test('credentialed request from an allowed origin receives ACAO header', async ({ page }) => {
    const email = `sec02b-${Date.now()}@shop-test.example`;
    const password = 'BuyerPass123!';
    await registerAndActivateBuyerViaApi(page, email, password);

    const allowedOrigin = process.env.BUYER_URL ?? 'http://buyer.localhost';
    const res = await page.request.post(`${API_URL}/api/auth/login`, {
      headers: {
        Origin: allowedOrigin,
        'Content-Type': 'application/json',
      },
      data: { email, password },
    });
    expect(res.status()).toBe(200);
    const acao = res.headers()['access-control-allow-origin'] ?? '';
    expect(acao).toBe(allowedOrigin);
    const acac = res.headers()['access-control-allow-credentials'] ?? '';
    expect(acac).toBe('true');
  });

  test('authenticated API endpoint rejects request from unknown origin at CORS level', async ({ page }) => {
    const email = `sec02c-${Date.now()}@shop-test.example`;
    const password = 'BuyerPass123!';
    await registerAndActivateBuyerViaApi(page, email, password);
    const token = await getBuyerToken(page, email, password);

    const res = await page.request.get(`${API_URL}/api/me`, {
      headers: {
        Authorization: `Bearer ${token}`,
        Origin: 'http://evil.attacker.example',
      },
    });
    // The browser would block this based on missing ACAO header; the server returns 403 on preflight
    // For actual requests, the response lacks ACAO — verify it is absent
    const acao = res.headers()['access-control-allow-origin'] ?? '';
    expect(acao).not.toBe('http://evil.attacker.example');
  });

});
