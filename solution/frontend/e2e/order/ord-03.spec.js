import { test, expect } from '@playwright/test';
import {
  API_URL,
  registerAndActivateBuyerViaApi,
  getBuyerToken,
  injectBuyerSession,
  createCarrierViaApi,
  createActiveVendorViaApi,
  getVendorToken,
  createProductViaApi,
} from '../helpers/login.js';

const BUYER_EMAIL = `ord03-buyer-${Date.now()}@example.com`;
const BUYER_PASSWORD = 'Buyer123456!';
const VENDOR_EMAIL = `ord03-vendor-${Date.now()}@example.com`;
const VENDOR_PASSWORD = 'Vendor123456!';

test.describe('US-ORD-03 — Card payment', () => {
  let buyerToken;
  let carrierId;
  let productId;

  test.beforeAll(async ({ request }) => {
    const p = { request };
    const carrier = await createCarrierViaApi(p, {
      name: 'TestCarrier ORD03',
      trackingUrl: 'https://track.example.com',
      supportedCountries: ['FR'],
    });
    carrierId = carrier.id;

    await createActiveVendorViaApi(p, VENDOR_EMAIL, VENDOR_PASSWORD);
    const vendorToken = await getVendorToken(p, VENDOR_EMAIL, VENDOR_PASSWORD);
    const product = await createProductViaApi(p, vendorToken, {
      name: 'Test Product ORD03',
      priceExclTax: 15.00,
      quantity: 10,
      status: 'PUBLISHED',
    });
    productId = product.id;

    await registerAndActivateBuyerViaApi(p, BUYER_EMAIL, BUYER_PASSWORD);
    buyerToken = await getBuyerToken(p, BUYER_EMAIL, BUYER_PASSWORD);
  });

  test('card checkout creates order in PAYMENT_PENDING_CARD and returns clientSecret', async ({ request }) => {
    // Add product to cart
    await request.post(`${API_URL}/api/cart/items`, {
      headers: { Authorization: `Bearer ${buyerToken}` },
      data: { productId, quantity: 1 },
    });

    const res = await request.post(`${API_URL}/api/orders`, {
      headers: { Authorization: `Bearer ${buyerToken}` },
      data: {
        deliveryAddressLine: '1 rue de la Paix',
        deliveryCity: 'Paris',
        deliveryPostalCode: '75001',
        deliveryCountryCode: 'FR',
        carrierId,
        paymentMethod: 'CARD',
      },
    });

    expect(res.status()).toBe(201);
    const body = await res.json();
    expect(body.orderId).toBeTruthy();
    expect(body.orderNumber).toMatch(/^ORD-/);
    expect(body.clientSecret).toBeTruthy();
    expect(body.paymentMethod).toBe('CARD');
  });

  test('confirm-payment transitions order to AWAITING_PROCESSING (stub mode)', async ({ request }) => {
    // Add product to cart again
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
        paymentMethod: 'CARD',
      },
    });
    expect(initRes.status()).toBe(201);
    const init = await initRes.json();

    const confirmRes = await request.post(`${API_URL}/api/orders/${init.orderId}/confirm-payment`, {
      headers: { Authorization: `Bearer ${buyerToken}` },
    });

    expect(confirmRes.status()).toBe(200);
    const order = await confirmRes.json();
    expect(order.status).toBe('AWAITING_PROCESSING');
  });

  test('unauthenticated checkout returns 403', async ({ request }) => {
    const res = await request.post(`${API_URL}/api/orders`, {
      data: {
        deliveryAddressLine: '1 rue Test',
        deliveryCity: 'Paris',
        deliveryPostalCode: '75001',
        deliveryCountryCode: 'FR',
        carrierId,
        paymentMethod: 'CARD',
      },
    });
    expect(res.status()).toBe(403);
  });
});
