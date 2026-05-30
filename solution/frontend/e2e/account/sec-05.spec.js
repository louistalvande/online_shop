import { test, expect } from '@playwright/test';
import { API_URL, getAdminToken, registerAndActivateBuyerViaApi, getBuyerToken } from '../helpers/login.js';

// US-SEC-05 — Admin audit log viewer: paginated query, filters, CSV export, access control.

test.describe('US-SEC-05 — Audit log', () => {

  test('GET /audit-logs returns paginated results for admin', async ({ page }) => {
    const adminToken = await getAdminToken(page);

    const res = await page.request.get(`${API_URL}/api/admin/audit-logs?page=0&size=10`, {
      headers: { Authorization: `Bearer ${adminToken}` },
    });
    expect(res.status()).toBe(200);

    const body = await res.json();
    expect(typeof body.totalElements).toBe('number');
    expect(Array.isArray(body.content)).toBe(true);
    expect(body.size).toBe(10);
    expect(body.page).toBe(0);
  });

  test('filter by eventType returns only matching entries', async ({ page }) => {
    const email = `sec05a-${Date.now()}@shop-test.example`;
    const password = 'BuyerPass123!';
    await registerAndActivateBuyerViaApi(page, email, password);

    const adminToken = await getAdminToken(page);
    const res = await page.request.get(
      `${API_URL}/api/admin/audit-logs?eventType=REGISTRATION&page=0&size=50`,
      { headers: { Authorization: `Bearer ${adminToken}` } },
    );
    expect(res.status()).toBe(200);
    const body = await res.json();
    expect(body.content.every((e) => e.eventType === 'REGISTRATION')).toBe(true);
  });

  test('filter by email returns only entries for that user', async ({ page }) => {
    const email = `sec05b-${Date.now()}@shop-test.example`;
    const password = 'BuyerPass123!';
    await registerAndActivateBuyerViaApi(page, email, password);

    const adminToken = await getAdminToken(page);
    const res = await page.request.get(
      `${API_URL}/api/admin/audit-logs?email=${encodeURIComponent(email)}&page=0&size=50`,
      { headers: { Authorization: `Bearer ${adminToken}` } },
    );
    expect(res.status()).toBe(200);
    const body = await res.json();
    expect(body.content.length).toBeGreaterThan(0);
    expect(body.content.every((e) => !e.email || e.email.includes(email))).toBe(true);
  });

  test('GET /audit-logs/export returns CSV with correct headers', async ({ page }) => {
    const adminToken = await getAdminToken(page);

    const res = await page.request.get(`${API_URL}/api/admin/audit-logs/export`, {
      headers: { Authorization: `Bearer ${adminToken}` },
    });
    expect(res.status()).toBe(200);

    const contentType = res.headers()['content-type'] ?? '';
    expect(contentType).toMatch(/text\/csv/i);

    const csv = await res.text();
    expect(csv.startsWith('id,eventType,email,ipAddress,details,occurredAt')).toBe(true);
  });

  test('CSV export with eventType filter contains only matching rows', async ({ page }) => {
    const adminToken = await getAdminToken(page);

    const res = await page.request.get(
      `${API_URL}/api/admin/audit-logs/export?eventType=LOGIN_SUCCESS`,
      { headers: { Authorization: `Bearer ${adminToken}` } },
    );
    expect(res.status()).toBe(200);
    const csv = await res.text();
    const dataLines = csv.split('\n').slice(1).filter(Boolean);
    expect(dataLines.every(line => line.includes('LOGIN_SUCCESS'))).toBe(true);
  });

  test('audit log endpoint returns 403 for non-admin (buyer)', async ({ page }) => {
    const email = `sec05c-${Date.now()}@shop-test.example`;
    const password = 'BuyerPass123!';
    await registerAndActivateBuyerViaApi(page, email, password);
    const buyerToken = await getBuyerToken(page, email, password);

    const res = await page.request.get(`${API_URL}/api/admin/audit-logs`, {
      headers: { Authorization: `Bearer ${buyerToken}` },
    });
    expect(res.status()).toBe(403);
  });

  test('audit log export returns 403 for non-admin', async ({ page }) => {
    const email = `sec05d-${Date.now()}@shop-test.example`;
    const password = 'BuyerPass123!';
    await registerAndActivateBuyerViaApi(page, email, password);
    const buyerToken = await getBuyerToken(page, email, password);

    const res = await page.request.get(`${API_URL}/api/admin/audit-logs/export`, {
      headers: { Authorization: `Bearer ${buyerToken}` },
    });
    expect(res.status()).toBe(403);
  });

});
