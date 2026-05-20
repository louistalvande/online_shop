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

const BUYER_PASSWORD = 'Buyer123456!';
const VENDOR_EMAIL = `exp01-vendor-${Date.now()}@example.com`;
const VENDOR_PASSWORD = 'Vendor123456!';

test.describe('US-EXP-01 — Vendor declares shipment with tracking number', () => {
  let vendorToken;
  let carrierId;
  let productId;

  test.beforeAll(async ({ request }) => {
    const p = { request };
    const carrier = await createCarrierViaApi(p, {
      name: 'TestCarrier EXP01',
      trackingUrl: 'https://track.example.com/',
      supportedCountries: ['FR'],
    });
    carrierId = carrier.id;

    await createActiveVendorViaApi(p, VENDOR_EMAIL, VENDOR_PASSWORD);
    vendorToken = await getVendorToken(p, VENDOR_EMAIL, VENDOR_PASSWORD);
    const product = await createProductViaApi(p, vendorToken, {
      name: 'Test Product EXP01',
      priceExclTax: 50.00,
      quantity: 10,
      status: 'PUBLISHED',
    });
    productId = product.id;
  });

  async function placeAndConfirmOrder(request) {
    const buyerEmail = `exp01-buyer-${Date.now()}@example.com`;
    const p = { request };
    await registerAndActivateBuyerViaApi(p, buyerEmail, BUYER_PASSWORD);
    const buyerToken = await getBuyerToken(p, buyerEmail, BUYER_PASSWORD);

    await request.post(`${API_URL}/api/cart/items`, {
      headers: { Authorization: `Bearer ${buyerToken}` },
      data: { productId, quantity: 1 },
    });
    const initRes = await request.post(`${API_URL}/api/orders`, {
      headers: { Authorization: `Bearer ${buyerToken}` },
      data: {
        deliveryAddressLine: '1 rue Test',
        deliveryCity: 'Paris',
        deliveryPostalCode: '75001',
        deliveryCountryCode: 'FR',
        carrierId,
        paymentMethod: 'WIRE_TRANSFER',
      },
    });
    const body = await initRes.json();
    const orderId = body.orderId;

    // Vendor confirms the wire payment
    await request.post(`${API_URL}/api/vendor/orders/${orderId}/confirm-wire`, {
      headers: { Authorization: `Bearer ${vendorToken}` },
    });
    return orderId;
  }

  test('ship endpoint transitions order to SHIPPED with tracking number', async ({ request }) => {
    const orderId = await placeAndConfirmOrder(request);

    const res = await request.post(`${API_URL}/api/vendor/orders/${orderId}/ship`, {
      headers: { Authorization: `Bearer ${vendorToken}` },
      data: { trackingNumber: '1Z999AA10123456784' },
    });
    expect(res.status()).toBe(200);
    const order = await res.json();
    expect(order.status).toBe('SHIPPED');
    expect(order.trackingNumber).toBe('1Z999AA10123456784');
  });

  test('ship with empty tracking number returns 400', async ({ request }) => {
    const orderId = await placeAndConfirmOrder(request);

    const res = await request.post(`${API_URL}/api/vendor/orders/${orderId}/ship`, {
      headers: { Authorization: `Bearer ${vendorToken}` },
      data: { trackingNumber: '' },
    });
    expect(res.status()).toBe(400);
  });

  test('ship a SHIPPED order returns 409', async ({ request }) => {
    const orderId = await placeAndConfirmOrder(request);

    await request.post(`${API_URL}/api/vendor/orders/${orderId}/ship`, {
      headers: { Authorization: `Bearer ${vendorToken}` },
      data: { trackingNumber: 'FIRST-TRACKING' },
    });

    const res = await request.post(`${API_URL}/api/vendor/orders/${orderId}/ship`, {
      headers: { Authorization: `Bearer ${vendorToken}` },
      data: { trackingNumber: 'SECOND-TRACKING' },
    });
    expect(res.status()).toBe(409);
  });

  test('unauthenticated ship returns 403', async ({ request }) => {
    const orderId = await placeAndConfirmOrder(request);

    const res = await request.post(`${API_URL}/api/vendor/orders/${orderId}/ship`, {
      data: { trackingNumber: 'SOME-TRACK' },
    });
    expect(res.status()).toBe(403);
  });
});
