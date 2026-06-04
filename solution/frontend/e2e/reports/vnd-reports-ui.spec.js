import { test, expect } from '@playwright/test';
import {
  API_URL,
  createActiveVendorViaApi, getVendorToken, injectVendorSession,
  registerAndActivateBuyerViaApi, getBuyerToken,
  createCarrierViaApi, createProductViaApi, createAddressViaApi,
} from '../helpers/login.js';

// VND-REPORTS-UI — Vendor reports page: filters, KPI cards, CSV exports (US-RPT-01 IHM).

const VENDOR_EMAIL    = `vnd-rpt-${Date.now()}@shop-test.example`;
const VENDOR_PASSWORD = 'VndRpt!Secure2026';
const BUYER_PASSWORD  = 'ByrRpt!Secure2026';

test.describe('Vendor reports page', () => {
  let vendorToken;

  test.beforeAll(async ({ browser }) => {
    const page = await browser.newPage();
    await createActiveVendorViaApi(page, VENDOR_EMAIL, VENDOR_PASSWORD);
    vendorToken = await getVendorToken(page, VENDOR_EMAIL, VENDOR_PASSWORD);

    // Seed at least one confirmed order so KPIs are non-zero
    const carrier = await createCarrierViaApi(page, {
      name: `Carrier-Rpt-${Date.now()}`, trackingUrl: 'https://track.example.com/', supportedCountries: ['FR'],
    });
    const product = await createProductViaApi(page, vendorToken, {
      name: 'Product-Rpt', priceExclTax: 50.00, quantity: 10, status: 'PUBLISHED',
    });
    const buyerEmail = `rpt-buyer-${Date.now()}@shop-test.example`;
    await registerAndActivateBuyerViaApi(page, buyerEmail, BUYER_PASSWORD);
    const buyerToken = await getBuyerToken(page, buyerEmail, BUYER_PASSWORD);
    const address = await createAddressViaApi(page, buyerToken, {
      label: 'Home', recipientName: 'Test', addressLine: '1 rue Test',
      city: 'Paris', postalCode: '75001', countryCode: 'FR', makeDefault: true,
    });
    await page.request.post(`${API_URL}/api/cart/items`, {
      headers: { Authorization: `Bearer ${buyerToken}` },
      data: { productId: product.id, quantity: 1 },
    });
    const orderRes = await page.request.post(`${API_URL}/api/orders`, {
      headers: { Authorization: `Bearer ${buyerToken}` },
      data: { addressId: address.id, carrierId: carrier.id, paymentMethod: 'WIRE_TRANSFER' },
    });
    const { orderId } = await orderRes.json();
    await page.request.post(`${API_URL}/api/vendor/orders/${orderId}/confirm-wire`, {
      headers: { Authorization: `Bearer ${vendorToken}` },
    });
    await page.close();
  });

  test.beforeEach(async ({ page }) => {
    const token = await getVendorToken(page, VENDOR_EMAIL, VENDOR_PASSWORD);
    await injectVendorSession(page, VENDOR_EMAIL, token);
    await page.goto('/vendor/');
    await page.getByRole('link', { name: 'Rapports' }).click();
    await expect(page.getByRole('heading', { name: 'Rapports de ventes' })).toBeVisible();
  });

  // ── Filter form ───────────────────────────────────────────────────────────

  test('nominal — date filter inputs are visible', async ({ page }) => {
    // Date inputs are type="date" — check by type since labels may not have htmlFor
    const dateInputs = page.locator('input[type="date"]');
    await expect(dateInputs.first()).toBeVisible();
    await expect(dateInputs.last()).toBeVisible();
  });

  test('nominal — filter form labels Du and Au are visible', async ({ page }) => {
    await expect(page.getByText('Du', { exact: true })).toBeVisible();
    await expect(page.getByText('Au', { exact: true })).toBeVisible();
  });

  test('nominal — Apply button is visible', async ({ page }) => {
    await expect(page.getByRole('button', { name: 'Appliquer' })).toBeVisible();
  });

  test('nominal — category filter input is visible', async ({ page }) => {
    await expect(page.getByPlaceholder('Toutes')).toBeVisible();
  });

  test('nominal — Appliquer loads KPI cards', async ({ page }) => {
    await page.getByRole('button', { name: 'Appliquer' }).click();
    await expect(page.getByText('Chiffre d\'affaires')).toBeVisible();
    // Scope "Commandes" to main content to avoid nav link collision
    await expect(page.locator('main, [role="main"]').getByText('Commandes').or(
      page.locator('.card, [class*="card"]').getByText('Commandes')
    ).first()).toBeVisible();
    await expect(page.getByText('Panier moyen')).toBeVisible();
    await expect(page.getByText('Taux d\'annulation')).toBeVisible();
  });

  test('nominal — Export CSV button appears after applying filters', async ({ page }) => {
    await page.getByRole('button', { name: 'Appliquer' }).click();
    await expect(page.getByRole('button', { name: 'Exporter en CSV' })).toBeVisible();
  });

  test('nominal — Export CSV triggers a download', async ({ page }) => {
    await page.getByRole('button', { name: 'Appliquer' }).click();
    await expect(page.getByRole('button', { name: 'Exporter en CSV' })).toBeVisible();
    const [download] = await Promise.all([
      page.waitForEvent('download'),
      page.getByRole('button', { name: 'Exporter en CSV' }).click(),
    ]);
    expect(download.suggestedFilename()).toBeTruthy();
  });

  // ── Mailing list export ───────────────────────────────────────────────────

  test('nominal — mailing list heading is visible', async ({ page }) => {
    await expect(page.getByRole('heading', { name: 'Liste mailing' })).toBeVisible();
    await expect(page.getByRole('button', { name: 'Exporter la liste mailing' })).toBeVisible();
  });

  test('nominal — mailing list export triggers a download', async ({ page }) => {
    const [download] = await Promise.all([
      page.waitForEvent('download'),
      page.getByRole('button', { name: 'Exporter la liste mailing' }).click(),
    ]);
    expect(download.suggestedFilename()).toBeTruthy();
  });
});
