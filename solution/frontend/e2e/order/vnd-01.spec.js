import { test, expect } from '@playwright/test';
import {
  API_URL,
  registerAndActivateBuyerViaApi,
  getBuyerToken,
  createCarrierViaApi,
  createActiveVendorViaApi,
  getVendorToken,
  createProductViaApi,
} from '../helpers/login.js';

const BUYER_EMAIL = `vnd01-buyer-${Date.now()}@example.com`;
const BUYER_PASSWORD = 'sHp-E2e!Byr-X9pZ';
const VENDOR_EMAIL = `vnd01-vendor-${Date.now()}@example.com`;
const VENDOR_PASSWORD = 'Vendor123456!';

test.describe('US-VND-01 — Vendor order listing and details', () => {
  let buyerToken;
  let vendorToken;
  let carrierId;
  let productId;
  let orderId;

  test.beforeAll(async ({ request }) => {
    const p = { request };
    const carrier = await createCarrierViaApi(p, {
      name: 'TestCarrier VND01',
      trackingUrl: 'https://track.example.com/',
      supportedCountries: ['FR'],
    });
    carrierId = carrier.id;

    await createActiveVendorViaApi(p, VENDOR_EMAIL, VENDOR_PASSWORD);
    vendorToken = await getVendorToken(p, VENDOR_EMAIL, VENDOR_PASSWORD);
    const product = await createProductViaApi(p, vendorToken, {
      name: 'Test Product VND01',
      priceExclTax: 30.00,
      quantity: 5,
      status: 'PUBLISHED',
    });
    productId = product.id;

    await registerAndActivateBuyerViaApi(p, BUYER_EMAIL, BUYER_PASSWORD);
    buyerToken = await getBuyerToken(p, BUYER_EMAIL, BUYER_PASSWORD);

    // Place a wire transfer order
    await request.post(`${API_URL}/api/cart/items`, {
      headers: { Authorization: `Bearer ${buyerToken}` },
      data: { productId, quantity: 1 },
    });
    const initRes = await request.post(`${API_URL}/api/orders`, {
      headers: { Authorization: `Bearer ${buyerToken}` },
      data: {
        deliveryAddressLine: '1 rue de la Paix',
        deliveryCity: 'Paris',
        deliveryPostalCode: '75001',
        deliveryCountryCode: 'FR',
        carrierId,
        paymentMethod: 'WIRE_TRANSFER',
      },
    });
    expect(initRes.status()).toBe(201);
    const body = await initRes.json();
    orderId = body.orderId;
  });

  test('GET /api/vendor/orders returns the order placed by the buyer', async ({ request }) => {
    const res = await request.get(`${API_URL}/api/vendor/orders`, {
      headers: { Authorization: `Bearer ${vendorToken}` },
    });
    expect(res.status()).toBe(200);
    const orders = await res.json();
    expect(orders.length).toBeGreaterThan(0);
    const order = orders.find(o => o.id === orderId);
    expect(order).toBeTruthy();
    expect(order.status).toBe('PAYMENT_PENDING_WIRE');
  });

  test('GET /api/vendor/orders/:id returns order with lines', async ({ request }) => {
    const res = await request.get(`${API_URL}/api/vendor/orders/${orderId}`, {
      headers: { Authorization: `Bearer ${vendorToken}` },
    });
    expect(res.status()).toBe(200);
    const order = await res.json();
    expect(order.id).toBe(orderId);
    expect(order.lines.length).toBeGreaterThan(0);
    expect(order.lines[0].productName).toBe('Test Product VND01');
  });

  test('GET /api/vendor/orders/:id returns 404 for another vendor', async ({ request }) => {
    const otherEmail = `vnd01-other-${Date.now()}@example.com`;
    const p = { request };
    await createActiveVendorViaApi(p, otherEmail, VENDOR_PASSWORD);
    const otherToken = await getVendorToken(p, otherEmail, VENDOR_PASSWORD);

    const res = await request.get(`${API_URL}/api/vendor/orders/${orderId}`, {
      headers: { Authorization: `Bearer ${otherToken}` },
    });
    expect(res.status()).toBe(404);
  });

  test('GET /api/vendor/orders returns 401 for unauthenticated request', async ({ request }) => {
    const res = await request.get(`${API_URL}/api/vendor/orders`);
    expect(res.status()).toBe(401);
  });
});
