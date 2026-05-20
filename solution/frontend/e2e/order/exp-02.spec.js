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

const BUYER_EMAIL = `exp02-buyer-${Date.now()}@example.com`;
const BUYER_PASSWORD = 'Buyer123456!';
const VENDOR_EMAIL = `exp02-vendor-${Date.now()}@example.com`;
const VENDOR_PASSWORD = 'Vendor123456!';

test.describe('US-EXP-02 — Buyer tracks shipment from order detail', () => {
  let buyerToken;
  let vendorToken;
  let carrierId;
  let productId;
  let orderId;

  test.beforeAll(async ({ request }) => {
    const p = { request };
    const carrier = await createCarrierViaApi(p, {
      name: 'TestCarrier EXP02',
      trackingUrl: 'https://track.example.com/',
      supportedCountries: ['FR'],
    });
    carrierId = carrier.id;

    await createActiveVendorViaApi(p, VENDOR_EMAIL, VENDOR_PASSWORD);
    vendorToken = await getVendorToken(p, VENDOR_EMAIL, VENDOR_PASSWORD);
    const product = await createProductViaApi(p, vendorToken, {
      name: 'Test Product EXP02',
      priceExclTax: 45.00,
      quantity: 5,
      status: 'PUBLISHED',
    });
    productId = product.id;

    await registerAndActivateBuyerViaApi(p, BUYER_EMAIL, BUYER_PASSWORD);
    buyerToken = await getBuyerToken(p, BUYER_EMAIL, BUYER_PASSWORD);

    // Place order and confirm wire payment
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
    const body = await initRes.json();
    orderId = body.orderId;

    await request.post(`${API_URL}/api/vendor/orders/${orderId}/confirm-wire`, {
      headers: { Authorization: `Bearer ${vendorToken}` },
    });

    // Vendor ships the order
    await request.post(`${API_URL}/api/vendor/orders/${orderId}/ship`, {
      headers: { Authorization: `Bearer ${vendorToken}` },
      data: { trackingNumber: 'EXP02-TRACK-001' },
    });
  });

  test('GET /api/orders/:id returns SHIPPED status with tracking number', async ({ request }) => {
    const res = await request.get(`${API_URL}/api/orders/${orderId}`, {
      headers: { Authorization: `Bearer ${buyerToken}` },
    });
    expect(res.status()).toBe(200);
    const order = await res.json();
    expect(order.status).toBe('SHIPPED');
    expect(order.trackingNumber).toBe('EXP02-TRACK-001');
    expect(order.carrierTrackingUrl).toBeTruthy();
  });

  test('GET /api/orders returns the shipped order in the buyer list', async ({ request }) => {
    const res = await request.get(`${API_URL}/api/orders`, {
      headers: { Authorization: `Bearer ${buyerToken}` },
    });
    expect(res.status()).toBe(200);
    const orders = await res.json();
    const order = orders.find(o => o.id === orderId);
    expect(order).toBeTruthy();
    expect(order.status).toBe('SHIPPED');
    expect(order.trackingNumber).toBe('EXP02-TRACK-001');
  });
});
