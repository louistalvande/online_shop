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

const BUYER_EMAIL = `can06-buyer-${Date.now()}@example.com`;
const BUYER_PASSWORD = 'sHp-E2e!Byr-X9pZ';
const VENDOR_EMAIL = `can06-vendor-${Date.now()}@example.com`;
const VENDOR_PASSWORD = 'Vendor123456!';

test.describe('US-CAN-06 — Buyer post-shipment cancellation request', () => {
  let buyerToken;
  let vendorToken;
  let carrierId;
  let productId;
  let addressId;

  test.beforeAll(async ({ request }) => {
    const p = { request };
    const carrier = await createCarrierViaApi(p, {
      name: 'TestCarrier CAN06',
      trackingUrl: 'https://track.example.com/',
      supportedCountries: ['FR'],
    });
    carrierId = carrier.id;

    await createActiveVendorViaApi(p, VENDOR_EMAIL, VENDOR_PASSWORD);
    vendorToken = await getVendorToken(p, VENDOR_EMAIL, VENDOR_PASSWORD);

    const product = await createProductViaApi(p, vendorToken, {
      name: 'CAN06 Product',
      priceExclTax: 15.00,
      quantity: 20,
      category: 'DRAWING',
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
  });

  async function createShippedCardOrder(request) {
    await request.post(`${API_URL}/api/cart/items`, {
      headers: { Authorization: `Bearer ${buyerToken}`, 'Content-Type': 'application/json' },
      data: { productId, quantity: 1 },
    });
    const initRes = await request.post(`${API_URL}/api/orders`, {
      headers: { Authorization: `Bearer ${buyerToken}`, 'Content-Type': 'application/json' },
      data: { addressId, carrierId, paymentMethod: 'CARD' },
    });
    const init = await initRes.json();
    const orderId = init.orderId;
    await request.post(`${API_URL}/api/orders/${orderId}/confirm-payment`, {
      headers: { Authorization: `Bearer ${buyerToken}` },
    });
    await request.post(`${API_URL}/api/vendor/orders/${orderId}/ship`, {
      headers: { Authorization: `Bearer ${vendorToken}`, 'Content-Type': 'application/json' },
      data: { trackingNumber: 'TRACK-CAN06-CARD' },
    });
    return orderId;
  }

  async function createShippedWireOrder(request) {
    await request.post(`${API_URL}/api/cart/items`, {
      headers: { Authorization: `Bearer ${buyerToken}`, 'Content-Type': 'application/json' },
      data: { productId, quantity: 1 },
    });
    const initRes = await request.post(`${API_URL}/api/orders`, {
      headers: { Authorization: `Bearer ${buyerToken}`, 'Content-Type': 'application/json' },
      data: { addressId, carrierId, paymentMethod: 'WIRE_TRANSFER' },
    });
    const init = await initRes.json();
    const orderId = init.orderId;
    await request.post(`${API_URL}/api/vendor/orders/${orderId}/confirm-wire`, {
      headers: { Authorization: `Bearer ${vendorToken}` },
    });
    await request.post(`${API_URL}/api/vendor/orders/${orderId}/ship`, {
      headers: { Authorization: `Bearer ${vendorToken}`, 'Content-Type': 'application/json' },
      data: { trackingNumber: 'TRACK-CAN06-WIRE' },
    });
    return orderId;
  }

  // ── Buyer requests cancellation ──────────────────────────────────────────

  test('buyer card order — request transitions SHIPPED → CANCELLATION_REQUESTED_AFTER_SHIPMENT', async ({ request }) => {
    const orderId = await createShippedCardOrder(request);

    const res = await request.post(`${API_URL}/api/orders/${orderId}/request-post-shipment-cancellation`, {
      headers: { Authorization: `Bearer ${buyerToken}`, 'Content-Type': 'application/json' },
      data: { reason: 'Product not as described' },
    });

    expect(res.status()).toBe(200);
    const body = await res.json();
    expect(body.status).toBe('CANCELLATION_REQUESTED_AFTER_SHIPMENT');
    expect(body.cancellationReason).toBe('Product not as described');
  });

  test('buyer wire order — request stores IBAN', async ({ request }) => {
    const orderId = await createShippedWireOrder(request);

    const res = await request.post(`${API_URL}/api/orders/${orderId}/request-post-shipment-cancellation`, {
      headers: { Authorization: `Bearer ${buyerToken}`, 'Content-Type': 'application/json' },
      data: { reason: 'Changed my mind', buyerIban: 'FR7630006000011234567890189' },
    });

    expect(res.status()).toBe(200);
    const body = await res.json();
    expect(body.status).toBe('CANCELLATION_REQUESTED_AFTER_SHIPMENT');
    expect(body.buyerIban).toBe('FR7630006000011234567890189');
  });

  test('wire order missing IBAN returns 422', async ({ request }) => {
    const orderId = await createShippedWireOrder(request);

    const res = await request.post(`${API_URL}/api/orders/${orderId}/request-post-shipment-cancellation`, {
      headers: { Authorization: `Bearer ${buyerToken}`, 'Content-Type': 'application/json' },
      data: { reason: 'Defective' },
    });

    expect(res.status()).toBe(422);
    const body = await res.json();
    expect(body.error).toBe('MISSING_BUYER_IBAN');
  });

  test('IN_PREPARATION order — request transitions to CANCELLATION_REQUESTED_AFTER_SHIPMENT', async ({ request }) => {
    await request.post(`${API_URL}/api/cart/items`, {
      headers: { Authorization: `Bearer ${buyerToken}`, 'Content-Type': 'application/json' },
      data: { productId, quantity: 1 },
    });
    const initRes = await request.post(`${API_URL}/api/orders`, {
      headers: { Authorization: `Bearer ${buyerToken}`, 'Content-Type': 'application/json' },
      data: { addressId, carrierId, paymentMethod: 'CARD' },
    });
    const init = await initRes.json();
    await request.post(`${API_URL}/api/orders/${init.orderId}/confirm-payment`, {
      headers: { Authorization: `Bearer ${buyerToken}` },
    });
    await request.post(`${API_URL}/api/vendor/orders/${init.orderId}/prepare`, {
      headers: { Authorization: `Bearer ${vendorToken}` },
    });

    const res = await request.post(`${API_URL}/api/orders/${init.orderId}/request-post-shipment-cancellation`, {
      headers: { Authorization: `Bearer ${buyerToken}`, 'Content-Type': 'application/json' },
      data: { reason: 'Changed my mind' },
    });
    expect(res.status()).toBe(200);
    const body = await res.json();
    expect(body.status).toBe('CANCELLATION_REQUESTED_AFTER_SHIPMENT');
  });

  test('AWAITING_PROCESSING order — request returns 409', async ({ request }) => {
    await request.post(`${API_URL}/api/cart/items`, {
      headers: { Authorization: `Bearer ${buyerToken}`, 'Content-Type': 'application/json' },
      data: { productId, quantity: 1 },
    });
    const initRes = await request.post(`${API_URL}/api/orders`, {
      headers: { Authorization: `Bearer ${buyerToken}`, 'Content-Type': 'application/json' },
      data: { addressId, carrierId, paymentMethod: 'CARD' },
    });
    const init = await initRes.json();
    await request.post(`${API_URL}/api/orders/${init.orderId}/confirm-payment`, {
      headers: { Authorization: `Bearer ${buyerToken}` },
    });

    const res = await request.post(`${API_URL}/api/orders/${init.orderId}/request-post-shipment-cancellation`, {
      headers: { Authorization: `Bearer ${buyerToken}`, 'Content-Type': 'application/json' },
      data: { reason: 'Reason' },
    });
    expect(res.status()).toBe(409);
  });

  // ── Vendor decisions ─────────────────────────────────────────────────────

  test('vendor refuses → CANCELLATION_REQUESTED_AFTER_SHIPMENT reverts to SHIPPED', async ({ request }) => {
    const orderId = await createShippedCardOrder(request);
    await request.post(`${API_URL}/api/orders/${orderId}/request-post-shipment-cancellation`, {
      headers: { Authorization: `Bearer ${buyerToken}`, 'Content-Type': 'application/json' },
      data: { reason: 'Too expensive elsewhere' },
    });

    const res = await request.post(`${API_URL}/api/vendor/orders/${orderId}/refuse-cancellation`, {
      headers: { Authorization: `Bearer ${vendorToken}` },
    });

    expect(res.status()).toBe(200);
    const body = await res.json();
    expect(body.status).toBe('SHIPPED');
  });

  test('vendor accepts with return → CANCELLATION_REQUESTED moves to PENDING_RETURN', async ({ request }) => {
    const orderId = await createShippedWireOrder(request);
    await request.post(`${API_URL}/api/orders/${orderId}/request-post-shipment-cancellation`, {
      headers: { Authorization: `Bearer ${buyerToken}`, 'Content-Type': 'application/json' },
      data: { reason: 'Not satisfied', buyerIban: 'FR7630006000011234567890189' },
    });

    const res = await request.post(`${API_URL}/api/vendor/orders/${orderId}/accept-return`, {
      headers: { Authorization: `Bearer ${vendorToken}`, 'Content-Type': 'application/json' },
      data: {},
    });

    expect(res.status()).toBe(200);
    const body = await res.json();
    expect(body.status).toBe('PENDING_RETURN');
  });

  test('vendor waives return → CANCELLATION_REQUESTED card moves to CANCELLED', async ({ request }) => {
    const orderId = await createShippedCardOrder(request);
    await request.post(`${API_URL}/api/orders/${orderId}/request-post-shipment-cancellation`, {
      headers: { Authorization: `Bearer ${buyerToken}`, 'Content-Type': 'application/json' },
      data: { reason: 'Wrong item received' },
    });

    const res = await request.post(`${API_URL}/api/vendor/orders/${orderId}/waive-return`, {
      headers: { Authorization: `Bearer ${vendorToken}`, 'Content-Type': 'application/json' },
      data: {},
    });

    expect(res.status()).toBe(200);
    const body = await res.json();
    expect(body.status).toBe('CANCELLED');
  });

  test('vendor refuse on wrong state returns 409', async ({ request }) => {
    const orderId = await createShippedCardOrder(request);

    const res = await request.post(`${API_URL}/api/vendor/orders/${orderId}/refuse-cancellation`, {
      headers: { Authorization: `Bearer ${vendorToken}` },
    });
    expect(res.status()).toBe(409);
  });
});
