import { test, expect } from '@playwright/test';
import { loginAsAdmin } from '../helpers/login.js';

// US-ADM-10 — Maintenance mode toggle.
// Admin can enable and disable maintenance mode from the dashboard.
// When enabled, buyer and vendor portals show the maintenance page.

const ADMIN_URL = process.env.ADMIN_URL ?? 'http://admin.localhost';
const BUYER_URL = process.env.BUYER_URL ?? 'http://buyer.localhost';
const VENDOR_URL = process.env.VENDOR_URL ?? 'http://vendor.localhost';
const API_BASE = process.env.API_URL ?? process.env.API_BASE ?? 'http://localhost:8080';

async function disableMaintenanceViaApi(request) {
  const loginRes = await request.post(`${API_BASE}/api/auth/login`, {
    data: { email: 'admin@onlineshop.com', password: 'Admin123456!' },
  });
  const { token } = await loginRes.json();
  await request.patch(`${API_BASE}/api/admin/settings/maintenance`, {
    data: { active: false },
    headers: { Authorization: `Bearer ${token}` },
  });
}

test.describe('US-ADM-10 — Maintenance mode', () => {
  test.afterEach(async ({ request }) => {
    // Always restore platform to operational state after each test
    await disableMaintenanceViaApi(request);
  });

  test('nominal — admin can enable maintenance mode and status badge updates', async ({ page }) => {
    await loginAsAdmin(page);
    await page.goto(`${ADMIN_URL}/`);

    await expect(page.getByRole('heading', { name: 'Maintenance' })).toBeVisible();
    await expect(page.getByText('Opérationnel')).toBeVisible();

    await page.getByTestId('maintenance-toggle').click();

    await expect(page.getByText('Mode maintenance activé.')).toBeVisible();
    await expect(page.getByText('En maintenance')).toBeVisible();
  });

  test('nominal — admin can disable maintenance mode after enabling it', async ({ page }) => {
    await loginAsAdmin(page);
    await page.goto(`${ADMIN_URL}/`);

    // Enable
    await page.getByTestId('maintenance-toggle').click();
    await expect(page.getByText('Mode maintenance activé.')).toBeVisible();

    // Disable
    await page.getByTestId('maintenance-toggle').click();
    await expect(page.getByText('Mode maintenance désactivé.')).toBeVisible();
    await expect(page.getByText('Opérationnel')).toBeVisible();
  });

  test('nominal — buyer portal shows maintenance page when mode is enabled', async ({ page, request }) => {
    // Enable maintenance via API directly
    const loginRes = await request.post(`${API_BASE}/api/auth/login`, {
      data: { email: 'admin@onlineshop.com', password: 'Admin123456!' },
    });
    const { token } = await loginRes.json();
    await request.patch(`${API_BASE}/api/admin/settings/maintenance`, {
      data: { active: true },
      headers: { Authorization: `Bearer ${token}` },
    });

    await page.goto(`${BUYER_URL}/`);
    await expect(page.getByRole('heading', { name: /maintenance/i })).toBeVisible();
  });

  test('nominal — vendor portal shows maintenance page when mode is enabled', async ({ page, request }) => {
    const loginRes = await request.post(`${API_BASE}/api/auth/login`, {
      data: { email: 'admin@onlineshop.com', password: 'Admin123456!' },
    });
    const { token } = await loginRes.json();
    await request.patch(`${API_BASE}/api/admin/settings/maintenance`, {
      data: { active: true },
      headers: { Authorization: `Bearer ${token}` },
    });

    await page.goto(`${VENDOR_URL}/`);
    await expect(page.getByRole('heading', { name: /maintenance/i })).toBeVisible();
  });

  test('nominal — admin console remains accessible when maintenance is enabled', async ({ page, request }) => {
    const loginRes = await request.post(`${API_BASE}/api/auth/login`, {
      data: { email: 'admin@onlineshop.com', password: 'Admin123456!' },
    });
    const { token } = await loginRes.json();
    await request.patch(`${API_BASE}/api/admin/settings/maintenance`, {
      data: { active: true },
      headers: { Authorization: `Bearer ${token}` },
    });

    // Admin API must still respond 200 for admin callers
    const statusRes = await request.get(`${API_BASE}/api/admin/settings/maintenance`, {
      headers: { Authorization: `Bearer ${token}` },
    });
    expect(statusRes.status()).toBe(200);
    const body = await statusRes.json();
    expect(body.active).toBe(true);
  });

  test('nominal — public maintenance endpoint is always reachable', async ({ request }) => {
    const res = await request.get(`${API_BASE}/api/public/maintenance`);
    expect(res.status()).toBe(200);
    const body = await res.json();
    expect(typeof body.active).toBe('boolean');
  });

  test('error — non-admin API call returns 503 when maintenance is active', async ({ request }) => {
    // Enable maintenance
    const loginRes = await request.post(`${API_BASE}/api/auth/login`, {
      data: { email: 'admin@onlineshop.com', password: 'Admin123456!' },
    });
    const { token } = await loginRes.json();
    await request.patch(`${API_BASE}/api/admin/settings/maintenance`, {
      data: { active: true },
      headers: { Authorization: `Bearer ${token}` },
    });

    // Authenticated buyer call to a protected endpoint must receive 503
    const buyerEmail = `maint-503-${Date.now()}@shop-test.example`;
    const regRes = await request.post(`${API_BASE}/api/auth/register`, {
      data: { email: buyerEmail, password: 'BuyerMaint!99Zx', firstName: 'Test', lastName: 'Maint' },
    });
    // Re-use admin token to activate the buyer
    const adminList = await request.get(`${API_BASE}/api/admin/accounts`, {
      headers: { Authorization: `Bearer ${token}` },
    });
    const accounts = await adminList.json();
    const buyer = accounts.find(a => a.email === buyerEmail);
    if (buyer) {
      await request.patch(`${API_BASE}/api/admin/accounts/${buyer.id}/force-activate`, {
        headers: { Authorization: `Bearer ${token}` },
      });
      const buyerAuth = await request.post(`${API_BASE}/api/auth/login`, {
        data: { email: buyerEmail, password: 'BuyerMaint!99Zx' },
      });
      const { token: buyerToken } = await buyerAuth.json();
      const protectedRes = await request.get(`${API_BASE}/api/profile`, {
        headers: { Authorization: `Bearer ${buyerToken}` },
      });
      expect(protectedRes.status()).toBe(503);
    }
  });
});
