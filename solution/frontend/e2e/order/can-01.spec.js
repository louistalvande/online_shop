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

const BUYER_EMAIL = `can01-buyer-${Date.now()}@example.com`;
const BUYER_PASSWORD = 'Buyer123456!';
const VENDOR_EMAIL = `can01-vendor-${Date.now()}@example.com`;
const VENDOR_PASSWORD = 'Vendor123456!';

test.describe('US-CAN-01 — Buyer cancels order before shipment', () => {
  let buyerToken;
  let vendorToken;
  let carrierId;
  let productId;

  test.beforeAll(async ({ request }) => {
    const p = { request };
    const carrier = await createCarrierViaApi(p, {
      name: 'TestCarrier CAN01',
      trackingUrl: 'https://track.example.com/',
      supportedCountries: ['FR'],
    });
    carrierId = carrier.id;

    await createActiveVendorViaApi(p, VENDOR_EMAIL, VENDOR_PASSWORD);
    vendorToken = await getVendorToken(p, VENDOR_EMAIL, VENDOR_PASSWORD);

    const product = await createProductViaApi(p, vendorToken, {
      name: 'CAN01 Product',
      priceExclTax: 10.00,
      quantity: 10,
      category: 'PAINTING',
      stockAlertThreshold: 2,
    });
    productId = product.id;

    await registerAndActivateBuyerViaApi(p, BUYER_EMAIL, BUYER_PASSWORD);
    buyerToken = await getBuyerToken(p, BUYER_EMAIL, BUYER_PASSWORD);
  });

  async function addToCartAndCheckoutWire(request) {
    await request.post(`${API_URL}/api/cart/items`, {
      headers: { Authorization: `Bearer ${buyerToken}`, 'Content-Type': 'application/json' },
      data: { productId, quantity: 1 },
    });
    const res = await request.post(`${API_URL}/api/orders`, {
      headers: { Authorization: `Bearer ${buyerToken}`, 'Content-Type': 'application/json' },
      data: {
        deliveryAddressLine: '1 rue Test',
        deliveryCity: 'Paris',
        deliveryPostalCode: '75001',
        deliveryCountryCode: 'FR',
        carrierId,
        paymentMethod: 'WIRE_TRANSFER',
      },
    });
    const body = await res.json();
    return body.orderId;
  }

  async function addToCartAndCheckoutCard(request) {
    await request.post(`${API_URL}/api/cart/items`, {
      headers: { Authorization: `Bearer ${buyerToken}`, 'Content-Type': 'application/json' },
      data: { productId, quantity: 1 },
    });
    const initRes = await request.post(`${API_URL}/api/orders`, {
      headers: { Authorization: `Bearer ${buyerToken}`, 'Content-Type': 'application/json' },
      data: {
        deliveryAddressLine: '1 rue Test',
        deliveryCity: 'Paris',
        deliveryPostalCode: '75001',
        deliveryCountryCode: 'FR',
        carrierId,
        paymentMethod: 'CARD',
      },
    });
    const init = await initRes.json();
    // Confirm card payment (stub always succeeds)
    const confirmRes = await request.post(`${API_URL}/api/orders/${init.orderId}/confirm-payment`, {
      headers: { Authorization: `Bearer ${buyerToken}` },
    });
    const order = await confirmRes.json();
    return order.id;
  }

  test('wire order — cancelled with IBAN transitions to WIRE_REFUND_IN_PROGRESS', async ({ request }) => {
    const orderId = await addToCartAndCheckoutWire(request);

    const res = await request.post(`${API_URL}/api/orders/${orderId}/cancel`, {
      headers: { Authorization: `Bearer ${buyerToken}`, 'Content-Type': 'application/json' },
      data: { buyerIban: 'FR7630006000011234567890189' },
    });

    expect(res.status()).toBe(200);
    const body = await res.json();
    expect(body.status).toBe('WIRE_REFUND_IN_PROGRESS');
    expect(body.buyerIban).toBe('FR7630006000011234567890189');
  });

  test('card order — cancelled transitions to CANCELLED', async ({ request }) => {
    const orderId = await addToCartAndCheckoutCard(request);

    const res = await request.post(`${API_URL}/api/orders/${orderId}/cancel`, {
      headers: { Authorization: `Bearer ${buyerToken}`, 'Content-Type': 'application/json' },
      data: {},
    });

    expect(res.status()).toBe(200);
    const body = await res.json();
    expect(body.status).toBe('CANCELLED');
  });

  test('wire order — missing IBAN returns 422', async ({ request }) => {
    const orderId = await addToCartAndCheckoutWire(request);

    const res = await request.post(`${API_URL}/api/orders/${orderId}/cancel`, {
      headers: { Authorization: `Bearer ${buyerToken}`, 'Content-Type': 'application/json' },
      data: {},
    });

    expect(res.status()).toBe(422);
    const body = await res.json();
    expect(body.error).toBe('MISSING_BUYER_IBAN');
  });

  test('already cancelled order — returns 409', async ({ request }) => {
    const orderId = await addToCartAndCheckoutWire(request);

    // Cancel once
    await request.post(`${API_URL}/api/orders/${orderId}/cancel`, {
      headers: { Authorization: `Bearer ${buyerToken}`, 'Content-Type': 'application/json' },
      data: { buyerIban: 'FR7630006000011234567890189' },
    });

    // Cancel again — should fail
    const res = await request.post(`${API_URL}/api/orders/${orderId}/cancel`, {
      headers: { Authorization: `Bearer ${buyerToken}`, 'Content-Type': 'application/json' },
      data: { buyerIban: 'FR7630006000011234567890189' },
    });
    expect(res.status()).toBe(409);
  });

  test('unauthenticated request — returns 401', async ({ request }) => {
    const res = await request.post(`${API_URL}/api/orders/${crypto.randomUUID()}/cancel`, {
      headers: { 'Content-Type': 'application/json' },
      data: {},
    });
    expect(res.status()).toBe(401);
  });
});
