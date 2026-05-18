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
const VENDOR_EMAIL = `vnd02-vendor-${Date.now()}@example.com`;
const VENDOR_PASSWORD = 'Vendor123456!';

test.describe('US-VND-02 — Vendor wire transfer confirmation and rejection', () => {
  let vendorToken;
  let carrierId;
  let productId;

  test.beforeAll(async ({ request }) => {
    const p = { request };
    const carrier = await createCarrierViaApi(p, {
      name: 'TestCarrier VND02',
      trackingUrl: 'https://track.example.com/',
      supportedCountries: ['FR'],
    });
    carrierId = carrier.id;

    await createActiveVendorViaApi(p, VENDOR_EMAIL, VENDOR_PASSWORD);
    vendorToken = await getVendorToken(p, VENDOR_EMAIL, VENDOR_PASSWORD);
    const product = await createProductViaApi(p, vendorToken, {
      name: 'Test Product VND02',
      priceExclTax: 40.00,
      quantity: 10,
      status: 'PUBLISHED',
    });
    productId = product.id;
  });

  /** Places a wire order and returns its ID. */
  async function placeWireOrder(request) {
    const buyerEmail = `vnd02-buyer-${Date.now()}@example.com`;
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
        deliveryCity: 'Lyon',
        deliveryPostalCode: '69001',
        deliveryCountryCode: 'FR',
        carrierId,
        paymentMethod: 'WIRE_TRANSFER',
      },
    });
    expect(initRes.status()).toBe(201);
    const body = await initRes.json();
    return body.orderId;
  }

  test('confirm-wire transitions order to AWAITING_PROCESSING', async ({ request }) => {
    const orderId = await placeWireOrder(request);

    const res = await request.post(`${API_URL}/api/vendor/orders/${orderId}/confirm-wire`, {
      headers: { Authorization: `Bearer ${vendorToken}` },
    });
    expect(res.status()).toBe(200);
    const order = await res.json();
    expect(order.status).toBe('AWAITING_PROCESSING');
  });

  test('reject-wire transitions order to CANCELLED', async ({ request }) => {
    const orderId = await placeWireOrder(request);

    const res = await request.post(`${API_URL}/api/vendor/orders/${orderId}/reject-wire`, {
      headers: { Authorization: `Bearer ${vendorToken}` },
    });
    expect(res.status()).toBe(200);
    const order = await res.json();
    expect(order.status).toBe('CANCELLED');
  });

  test('confirm-wire on already-confirmed order returns 409', async ({ request }) => {
    const orderId = await placeWireOrder(request);

    // confirm once
    await request.post(`${API_URL}/api/vendor/orders/${orderId}/confirm-wire`, {
      headers: { Authorization: `Bearer ${vendorToken}` },
    });
    // confirm again — should fail
    const res = await request.post(`${API_URL}/api/vendor/orders/${orderId}/confirm-wire`, {
      headers: { Authorization: `Bearer ${vendorToken}` },
    });
    expect(res.status()).toBe(409);
  });

  test('reject-wire on a CARD order returns 409', async ({ request }) => {
    // Place a CARD order
    const buyerEmail = `vnd02-card-${Date.now()}@example.com`;
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
        deliveryAddressLine: '2 rue Test',
        deliveryCity: 'Paris',
        deliveryPostalCode: '75001',
        deliveryCountryCode: 'FR',
        carrierId,
        paymentMethod: 'CARD',
      },
    });
    const body = await initRes.json();
    const cardOrderId = body.orderId;

    const res = await request.post(`${API_URL}/api/vendor/orders/${cardOrderId}/reject-wire`, {
      headers: { Authorization: `Bearer ${vendorToken}` },
    });
    expect(res.status()).toBe(409);
  });
});
