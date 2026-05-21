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

const BUYER_EMAIL = `clm01-buyer-${Date.now()}@example.com`;
const BUYER_PASSWORD = 'sHp-E2e!Byr-X9pZ';
const VENDOR_EMAIL = `clm01-vendor-${Date.now()}@example.com`;
const VENDOR_PASSWORD = 'Vendor123456!';

test.describe('US-CLM-01 — Buyer opens a claim', () => {
  let buyerToken;
  let carrierId;
  let productId;

  test.beforeAll(async ({ request }) => {
    const p = { request };
    const carrier = await createCarrierViaApi(p, {
      name: 'TestCarrier CLM01',
      trackingUrl: 'https://track.example.com/',
      supportedCountries: ['FR'],
    });
    carrierId = carrier.id;

    await createActiveVendorViaApi(p, VENDOR_EMAIL, VENDOR_PASSWORD);
    const vendorToken = await getVendorToken(p, VENDOR_EMAIL, VENDOR_PASSWORD);

    const product = await createProductViaApi(p, vendorToken, {
      name: 'CLM01 Product',
      priceExclTax: 10.00,
      quantity: 20,
      category: 'PAINTING',
      stockAlertThreshold: 2,
    });
    productId = product.id;

    await registerAndActivateBuyerViaApi(p, BUYER_EMAIL, BUYER_PASSWORD);
    buyerToken = await getBuyerToken(p, BUYER_EMAIL, BUYER_PASSWORD);
  });

  async function createShippedOrder(request) {
    // Add to cart
    await request.post(`${API_URL}/api/cart/items`, {
      headers: { Authorization: `Bearer ${buyerToken}`, 'Content-Type': 'application/json' },
      data: { productId, quantity: 1 },
    });
    // Checkout with card
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
    const orderId = init.orderId;
    // Confirm card payment
    await request.post(`${API_URL}/api/orders/${orderId}/confirm-payment`, {
      headers: { Authorization: `Bearer ${buyerToken}` },
    });
    // Vendor ships the order
    const vendorToken = await getVendorToken({ request }, VENDOR_EMAIL, VENDOR_PASSWORD);
    await request.post(`${API_URL}/api/vendor/orders/${orderId}/ship`, {
      headers: { Authorization: `Bearer ${vendorToken}`, 'Content-Type': 'application/json' },
      data: { trackingNumber: 'TRACK123' },
    });
    return orderId;
  }

  test('nominal — buyer opens claim on shipped order', async ({ request }) => {
    const orderId = await createShippedOrder(request);

    const res = await request.post(`${API_URL}/api/orders/${orderId}/claims`, {
      headers: { Authorization: `Bearer ${buyerToken}`, 'Content-Type': 'application/json' },
      data: { reason: 'NON_RECEIPT', message: 'My order never arrived.' },
    });

    expect(res.status()).toBe(201);
    const body = await res.json();
    expect(body.status).toBe('OPEN');
    expect(body.reason).toBe('NON_RECEIPT');
    expect(body.orderId).toBe(orderId);
  });

  test('claim already open — second claim returns 409', async ({ request }) => {
    const orderId = await createShippedOrder(request);

    // First claim
    await request.post(`${API_URL}/api/orders/${orderId}/claims`, {
      headers: { Authorization: `Bearer ${buyerToken}`, 'Content-Type': 'application/json' },
      data: { reason: 'NON_RECEIPT', message: 'My order never arrived.' },
    });

    // Second claim on same order
    const res = await request.post(`${API_URL}/api/orders/${orderId}/claims`, {
      headers: { Authorization: `Bearer ${buyerToken}`, 'Content-Type': 'application/json' },
      data: { reason: 'DEFECTIVE_ITEM', message: 'Also broken.' },
    });

    expect(res.status()).toBe(409);
    const body = await res.json();
    expect(body.error).toBe('CLAIM_ALREADY_OPEN');
  });

  test('order not found — returns 404', async ({ request }) => {
    const res = await request.post(`${API_URL}/api/orders/${crypto.randomUUID()}/claims`, {
      headers: { Authorization: `Bearer ${buyerToken}`, 'Content-Type': 'application/json' },
      data: { reason: 'OTHER', message: 'Unknown order.' },
    });

    expect(res.status()).toBe(404);
    const body = await res.json();
    expect(body.error).toBe('ORDER_NOT_FOUND');
  });

  test('cancelled order — returns 409 invalid state', async ({ request }) => {
    // Create wire order and cancel it
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
        paymentMethod: 'WIRE_TRANSFER',
      },
    });
    const init = await initRes.json();
    const orderId = init.orderId;
    await request.post(`${API_URL}/api/orders/${orderId}/cancel`, {
      headers: { Authorization: `Bearer ${buyerToken}`, 'Content-Type': 'application/json' },
      data: { buyerIban: 'FR7630006000011234567890189' },
    });

    const res = await request.post(`${API_URL}/api/orders/${orderId}/claims`, {
      headers: { Authorization: `Bearer ${buyerToken}`, 'Content-Type': 'application/json' },
      data: { reason: 'OTHER', message: 'Just cancelled.' },
    });

    expect(res.status()).toBe(409);
    const body = await res.json();
    expect(body.error).toBe('INVALID_ORDER_STATE');
  });

  test('unauthenticated request — returns 401', async ({ request }) => {
    const res = await request.post(`${API_URL}/api/orders/${crypto.randomUUID()}/claims`, {
      headers: { 'Content-Type': 'application/json' },
      data: { reason: 'OTHER', message: 'No auth.' },
    });
    expect(res.status()).toBe(401);
  });

  test('missing reason — returns 400', async ({ request }) => {
    const orderId = await createShippedOrder(request);

    const res = await request.post(`${API_URL}/api/orders/${orderId}/claims`, {
      headers: { Authorization: `Bearer ${buyerToken}`, 'Content-Type': 'application/json' },
      data: { message: 'No reason provided.' },
    });

    expect(res.status()).toBe(400);
  });
});
