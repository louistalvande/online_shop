import { test, expect } from '@playwright/test';
import { API_URL, registerAndActivateBuyerViaApi } from '../helpers/login.js';

// SEC-AUTH-003 — Brute-force protection: account is locked after 5 consecutive failed login attempts.

const BAD_PASSWORD  = 'WrongPass!99Zz';
const GOOD_PASSWORD = 'Correct123!Ab#';
const MAX_ATTEMPTS  = 5;

test.describe('SEC-AUTH-003 — Brute-force login protection', () => {
  let accountEmail;

  test.beforeEach(async ({ page }) => {
    // Fresh active buyer account with a known password for each test
    const ts = Date.now();
    accountEmail = `sec-auth-003-${ts}@shop-test.example`;
    await registerAndActivateBuyerViaApi(page, accountEmail, GOOD_PASSWORD);
  });

  /**
   * Nominal — 5 failed attempts trigger lockout; the 6th returns 429.
   */
  test('nominal — locked after 5 failed attempts returns 429', async ({ page }) => {
    for (let i = 0; i < MAX_ATTEMPTS; i++) {
      const res = await page.request.post(`${API_URL}/api/auth/login`, {
        data: { email: accountEmail, password: BAD_PASSWORD },
      });
      expect(res.status()).toBe(401);
    }

    const blocked = await page.request.post(`${API_URL}/api/auth/login`, {
      data: { email: accountEmail, password: BAD_PASSWORD },
    });
    expect(blocked.status()).toBe(429);

    const body = await blocked.json();
    expect(body.error).toBe('TOO_MANY_ATTEMPTS');
    expect(body.message).toBeTruthy();
  });

  /**
   * Correct password is also rejected with 429 while the account is locked.
   * Confirms the brute-force check runs before credential validation.
   */
  test('correct password still blocked with 429 while locked', async ({ page }) => {
    for (let i = 0; i < MAX_ATTEMPTS; i++) {
      await page.request.post(`${API_URL}/api/auth/login`, {
        data: { email: accountEmail, password: BAD_PASSWORD },
      });
    }

    const res = await page.request.post(`${API_URL}/api/auth/login`, {
      data: { email: accountEmail, password: GOOD_PASSWORD },
    });
    expect(res.status()).toBe(429);
  });

  /**
   * A successful login resets the failure counter so a new lockout window starts fresh.
   */
  test('successful login resets the failure counter', async ({ page }) => {
    // 4 failures — one below threshold
    for (let i = 0; i < MAX_ATTEMPTS - 1; i++) {
      const res = await page.request.post(`${API_URL}/api/auth/login`, {
        data: { email: accountEmail, password: BAD_PASSWORD },
      });
      expect(res.status()).toBe(401);
    }

    // Correct credentials — must succeed and reset the counter
    const success = await page.request.post(`${API_URL}/api/auth/login`, {
      data: { email: accountEmail, password: GOOD_PASSWORD },
    });
    expect(success.status()).toBe(200);

    // After reset: 4 more failures must return 401, not 429
    for (let i = 0; i < MAX_ATTEMPTS - 1; i++) {
      const res = await page.request.post(`${API_URL}/api/auth/login`, {
        data: { email: accountEmail, password: BAD_PASSWORD },
      });
      expect(res.status()).toBe(401);
    }
  });
});
