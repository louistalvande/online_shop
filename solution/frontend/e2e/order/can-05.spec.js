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

const BUYER_EMAIL = `can05-buyer-${Date.now()}@example.com`;
const BUYER_PASSWORD = 'sHp-E2e!Byr-X9pZ';
const VENDOR_EMAIL = `can05-vendor-${Date.now()}@example.com`;
const VENDOR_PASSWORD = 'Vendor123456!';

test.describe('US-CAN-05 — Vendor confirms wire refund sent', () => {
  let buyerToken;
  let vendorToken;
  let carrierId;
  let productId;
  let addressId;

  test.beforeAll(async ({ request }) => {
    const p = { request };
    const carrier = await createCarrierViaApi(p, {
      name: 'TestCarrier CAN05',
      trackingUrl: 'https://track.example.com/',
      supportedCountries: ['FR'],
    });
    carrierId = carrier.id;

    await createActiveVendorViaApi(p, VENDOR_EMAIL, VENDOR_PASSWORD);
    vendorToken = await getVendorToken(p, VENDOR_EMAIL, VENDOR_PASSWORD);

    const product = await createProductViaApi(p, vendorToken, {
      name: 'CAN05 Product',
      priceExclTax: 10.00,
      quantity: 10,
      category: 'PAINTING',
      stockAlertThreshold: 2,
    });
    productId = product.id;

    await registerAndActivateBuyerViaApi(p, BUYER_EMAIL, BUYER_PASSWORD);
    buyerToken = await getBuyerToken(p, BUYER_EMAIL, BUYER_PASSWORD);

    const address = await createAddressViaApi(p, buyerToken, {
      label: 'Home', recipientName: 'Test Recipient', addressLine: '1 rue Test', city: 'Paris',
      postalCode: '75001', countryCode: 'FR', makeDefault: true,
    });
    addressId = address.id;
  });

  async function createWireRefundInProgressOrder(request) {
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

    // Confirm wire
    await request.post(`${API_URL}/api/vendor/orders/${orderId}/confirm-wire`, {
      headers: { Authorization: `Bearer ${vendorToken}` },
    });
    // Buyer cancels with IBAN
    await request.post(`${API_URL}/api/orders/${orderId}/cancel`, {
      headers: { Authorization: `Bearer ${buyerToken}`, 'Content-Type': 'application/json' },
      data: { buyerIban: 'FR7630006000011234567890189' },
    });
    return orderId;
  }

  test('confirm-wire-refund transitions WIRE_REFUND_IN_PROGRESS → CANCELLED', async ({ request }) => {
    const orderId = await createWireRefundInProgressOrder(request);

    const res = await request.post(`${API_URL}/api/vendor/orders/${orderId}/confirm-wire-refund`, {
      headers: { Authorization: `Bearer ${vendorToken}` },
    });

    expect(res.status()).toBe(200);
    const body = await res.json();
    expect(body.status).toBe('CANCELLED');
  });

  test('confirm-wire-refund on wrong state returns 409', async ({ request }) => {
    await request.post(`${API_URL}/api/cart/items`, {
      headers: { Authorization: `Bearer ${buyerToken}`, 'Content-Type': 'application/json' },
      data: { productId, quantity: 1 },
    });
    const initRes = await request.post(`${API_URL}/api/orders`, {
      headers: { Authorization: `Bearer ${buyerToken}`, 'Content-Type': 'application/json' },
      data: { addressId, carrierId, paymentMethod: 'WIRE_TRANSFER' },
    });
    const init = await initRes.json();
    await request.post(`${API_URL}/api/vendor/orders/${init.orderId}/confirm-wire`, {
      headers: { Authorization: `Bearer ${vendorToken}` },
    });

    // Not in WIRE_REFUND_IN_PROGRESS yet — just AWAITING_PROCESSING
    const res = await request.post(`${API_URL}/api/vendor/orders/${init.orderId}/confirm-wire-refund`, {
      headers: { Authorization: `Bearer ${vendorToken}` },
    });
    expect(res.status()).toBe(409);
  });

  test('double confirm-wire-refund returns 409', async ({ request }) => {
    const orderId = await createWireRefundInProgressOrder(request);

    await request.post(`${API_URL}/api/vendor/orders/${orderId}/confirm-wire-refund`, {
      headers: { Authorization: `Bearer ${vendorToken}` },
    });

    const res = await request.post(`${API_URL}/api/vendor/orders/${orderId}/confirm-wire-refund`, {
      headers: { Authorization: `Bearer ${vendorToken}` },
    });
    expect(res.status()).toBe(409);
  });

  test('unauthenticated request returns 401', async ({ request }) => {
    const res = await request.post(`${API_URL}/api/vendor/orders/${crypto.randomUUID()}/confirm-wire-refund`);
    expect(res.status()).toBe(401);
  });
});
