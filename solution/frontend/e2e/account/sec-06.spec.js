import { test, expect } from '@playwright/test';
import { API_URL, getAdminToken, registerAndActivateBuyerViaApi, getBuyerToken } from '../helpers/login.js';

// US-SEC-06 — Audit log partition integrity: the /partitions endpoint lists PostgreSQL partitions.

test.describe('US-SEC-06 — Audit log partitions', () => {

  test('GET /partitions returns 200 for admin with an array', async ({ page }) => {
    const adminToken = await getAdminToken(page);

    const res = await page.request.get(`${API_URL}/api/admin/audit-logs/partitions`, {
      headers: { Authorization: `Bearer ${adminToken}` },
    });
    expect(res.status()).toBe(200);

    const body = await res.json();
    expect(Array.isArray(body)).toBe(true);
  });

  test('each partition entry has partitionName, rowCount and sizeBytes fields', async ({ page }) => {
    const adminToken = await getAdminToken(page);

    // Trigger at least one audit log entry so some partitions have data
    await page.request.post(`${API_URL}/api/auth/login`, {
      data: { email: 'nonexistent@example.com', password: 'wrong' },
    });

    const res = await page.request.get(`${API_URL}/api/admin/audit-logs/partitions`, {
      headers: { Authorization: `Bearer ${adminToken}` },
    });
    const partitions = await res.json();

    // At least the named partitions for the current year should exist
    expect(partitions.length).toBeGreaterThan(0);

    for (const p of partitions) {
      expect(typeof p.partitionName).toBe('string');
      expect(p.partitionName).toMatch(/^audit_log/);
      expect(typeof p.rowCount).toBe('number');
      expect(typeof p.sizeBytes).toBe('number');
    }
  });

  test('partition names follow the expected naming pattern', async ({ page }) => {
    const adminToken = await getAdminToken(page);

    const res = await page.request.get(`${API_URL}/api/admin/audit-logs/partitions`, {
      headers: { Authorization: `Bearer ${adminToken}` },
    });
    const partitions = await res.json();

    const namedPartitions = partitions.filter(p => p.partitionName !== 'audit_log_default');
    // Named partitions follow audit_log_YYYY_MM or audit_log_default pattern
    for (const p of namedPartitions) {
      expect(p.partitionName).toMatch(/^audit_log_(\d{4}_\d{2}|default)$/);
    }
  });

  test('current month partition exists in the list', async ({ page }) => {
    const adminToken = await getAdminToken(page);

    const now = new Date();
    const year = now.getUTCFullYear();
    const month = String(now.getUTCMonth() + 1).padStart(2, '0');
    const expectedPartition = `audit_log_${year}_${month}`;

    const res = await page.request.get(`${API_URL}/api/admin/audit-logs/partitions`, {
      headers: { Authorization: `Bearer ${adminToken}` },
    });
    const partitions = await res.json();
    const names = partitions.map(p => p.partitionName);

    expect(names).toContain(expectedPartition);
  });

  test('GET /partitions returns 403 for non-admin (buyer)', async ({ page }) => {
    const email = `sec06a-${Date.now()}@shop-test.example`;
    const password = 'BuyerPass123!';
    await registerAndActivateBuyerViaApi(page, email, password);
    const buyerToken = await getBuyerToken(page, email, password);

    const res = await page.request.get(`${API_URL}/api/admin/audit-logs/partitions`, {
      headers: { Authorization: `Bearer ${buyerToken}` },
    });
    expect(res.status()).toBe(403);
  });

  test('GET /partitions returns 401 for unauthenticated request', async ({ page }) => {
    const res = await page.request.get(`${API_URL}/api/admin/audit-logs/partitions`);
    expect(res.status()).toBe(401);
  });

});
