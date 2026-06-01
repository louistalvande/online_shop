import { test, expect } from '@playwright/test';
import {
  API_URL,
  registerAndActivateBuyerViaApi,
  getBuyerToken,
  createCarrierViaApi,
  createActiveVendorViaApi,
  getVendorToken,
  createProductViaApi,
  createAddressViaApi,
} from '../helpers/login.js';

const BUYER_EMAIL = `rpt01-buyer-${Date.now()}@example.com`;
const BUYER_PASSWORD = 'sHp-E2e!Byr-X9pZ';
const VENDOR_EMAIL = `rpt01-vendor-${Date.now()}@example.com`;
const VENDOR_PASSWORD = 'Vendor123456!';

test.describe('US-RPT-01 — Vendor sales report', () => {
  let buyerToken;
  let vendorToken;
  let carrierId;
  let productId;
  let addressId;
  const period = new Date().toISOString().slice(0, 7); // current YYYY-MM

  test.beforeAll(async ({ request }) => {
    const p = { request };
    const carrier = await createCarrierViaApi(p, {
      name: 'TestCarrier RPT01',
      trackingUrl: 'https://track.example.com/',
      supportedCountries: ['FR'],
    });
    carrierId = carrier.id;

    await createActiveVendorViaApi(p, VENDOR_EMAIL, VENDOR_PASSWORD);
    vendorToken = await getVendorToken(p, VENDOR_EMAIL, VENDOR_PASSWORD);

    const product = await createProductViaApi(p, vendorToken, {
      name: 'RPT01 Product',
      priceExclTax: 20.00,
      quantity: 50,
      category: 'PAINTING',
      stockAlertThreshold: 2,
    });
    productId = product.id;

    await registerAndActivateBuyerViaApi(p, BUYER_EMAIL, BUYER_PASSWORD);
    buyerToken = await getBuyerToken(p, BUYER_EMAIL, BUYER_PASSWORD);

    const address = await createAddressViaApi(p, buyerToken, {
      label: 'Home', addressLine: '1 rue Test', city: 'Paris',
      postalCode: '75001', countryCode: 'FR', makeDefault: true,
    });
    addressId = address.id;

    // Place and confirm one order so we have sales data for the current period
    await request.post(`${API_URL}/api/cart/items`, {
      headers: { Authorization: `Bearer ${buyerToken}`, 'Content-Type': 'application/json' },
      data: { productId, quantity: 2 },
    });
    const initRes = await request.post(`${API_URL}/api/orders`, {
      headers: { Authorization: `Bearer ${buyerToken}`, 'Content-Type': 'application/json' },
      data: { addressId, carrierId, paymentMethod: 'CARD' },
    });
    const init = await initRes.json();
    await request.post(`${API_URL}/api/orders/${init.orderId}/confirm-payment`, {
      headers: { Authorization: `Bearer ${buyerToken}` },
    });
  });

  test('nominal — vendor fetches sales report for current period', async ({ request }) => {
    const res = await request.get(`${API_URL}/api/vendor/reports/sales`, {
      headers: { Authorization: `Bearer ${vendorToken}` },
      params: { period },
    });

    expect(res.status()).toBe(200);
    const body = await res.json();
    expect(body.period).toBe(period);
    expect(body.metrics).toBeDefined();
    expect(body.metrics.orderCount).toBeGreaterThan(0);
    expect(Number(body.metrics.revenue)).toBeGreaterThan(0);
    expect(body.metrics.averageCartValue).toBeDefined();
    expect(body.metrics.cancellationRate).toBeDefined();
    expect(Array.isArray(body.topSellingProducts)).toBe(true);
    expect(body.topSellingProducts.length).toBeGreaterThan(0);
    expect(body.topSellingProducts[0].rank).toBe(1);
  });

  test('with category filter — returns filtered data for matching category', async ({ request }) => {
    const res = await request.get(`${API_URL}/api/vendor/reports/sales`, {
      headers: { Authorization: `Bearer ${vendorToken}` },
      params: { period, category: 'PAINTING' },
    });

    expect(res.status()).toBe(200);
    const body = await res.json();
    expect(body.category).toBe('PAINTING');
    expect(body.metrics.orderCount).toBeGreaterThan(0);
  });

  test('with category filter — unmatched category returns zero metrics', async ({ request }) => {
    const res = await request.get(`${API_URL}/api/vendor/reports/sales`, {
      headers: { Authorization: `Bearer ${vendorToken}` },
      params: { period, category: 'SCULPTURE' },
    });

    expect(res.status()).toBe(200);
    const body = await res.json();
    expect(body.metrics.orderCount).toBe(0);
    expect(Number(body.metrics.revenue)).toBe(0);
    expect(body.topSellingProducts).toHaveLength(0);
  });

  test('no orders in period — returns zero metrics and empty products', async ({ request }) => {
    const res = await request.get(`${API_URL}/api/vendor/reports/sales`, {
      headers: { Authorization: `Bearer ${vendorToken}` },
      params: { period: '2000-01' },
    });

    expect(res.status()).toBe(200);
    const body = await res.json();
    expect(body.metrics.orderCount).toBe(0);
    expect(Number(body.metrics.revenue)).toBe(0);
    expect(body.topSellingProducts).toHaveLength(0);
  });

  test('CSV export — returns text/csv file with expected content', async ({ request }) => {
    const res = await request.get(`${API_URL}/api/vendor/reports/sales/export`, {
      headers: { Authorization: `Bearer ${vendorToken}` },
      params: { period },
    });

    expect(res.status()).toBe(200);
    const contentType = res.headers()['content-type'];
    expect(contentType).toContain('text/csv');
    const contentDisposition = res.headers()['content-disposition'];
    expect(contentDisposition).toContain('sales-report-' + period + '.csv');
    const body = await res.text();
    expect(body).toContain('Sales Report');
    expect(body).toContain('Revenue');
    expect(body).toContain('Top Selling Products');
    expect(body).toContain('Rank,Product,Quantity Sold,Revenue Generated');
  });

  test('invalid period format — returns 400 INVALID_PERIOD', async ({ request }) => {
    const res = await request.get(`${API_URL}/api/vendor/reports/sales`, {
      headers: { Authorization: `Bearer ${vendorToken}` },
      params: { period: 'january-2025' },
    });

    expect(res.status()).toBe(400);
    const body = await res.json();
    expect(body.error).toBe('INVALID_PERIOD');
  });

  test('unauthenticated request — returns 401', async ({ request }) => {
    const res = await request.get(`${API_URL}/api/vendor/reports/sales`, {
      params: { period },
    });
    expect(res.status()).toBe(401);
  });
});
