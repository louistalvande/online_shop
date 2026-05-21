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

const BUYER_EMAIL = `can03-buyer-${Date.now()}@example.com`;
const BUYER_PASSWORD = 'sHp-E2e!Byr-X9pZ';
const VENDOR_EMAIL = `can03-vendor-${Date.now()}@example.com`;
const VENDOR_PASSWORD = 'Vendor123456!';

test.describe('US-CAN-03 — Vendor accepts post-shipment cancellation with return', () => {
  let buyerToken;
  let vendorToken;
  let carrierId;
  let productId;

  test.beforeAll(async ({ request }) => {
    const p = { request };
    const carrier = await createCarrierViaApi(p, {
      name: 'TestCarrier CAN03',
      trackingUrl: 'https://track.example.com/',
      supportedCountries: ['FR'],
    });
    carrierId = carrier.id;

    await createActiveVendorViaApi(p, VENDOR_EMAIL, VENDOR_PASSWORD);
    vendorToken = await getVendorToken(p, VENDOR_EMAIL, VENDOR_PASSWORD);

    const product = await createProductViaApi(p, vendorToken, {
      name: 'CAN03 Product',
      priceExclTax: 10.00,
      quantity: 10,
      category: 'PAINTING',
      stockAlertThreshold: 2,
    });
    productId = product.id;

    await registerAndActivateBuyerViaApi(p, BUYER_EMAIL, BUYER_PASSWORD);
    buyerToken = await getBuyerToken(p, BUYER_EMAIL, BUYER_PASSWORD);
  });

  async function createShippedWireOrder(request) {
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
    await request.post(`${API_URL}/api/vendor/orders/${orderId}/confirm-wire`, {
      headers: { Authorization: `Bearer ${vendorToken}` },
    });
    await request.post(`${API_URL}/api/vendor/orders/${orderId}/ship`, {
      headers: { Authorization: `Bearer ${vendorToken}`, 'Content-Type': 'application/json' },
      data: { trackingNumber: 'TRACK-CAN03' },
    });
    return orderId;
  }

  test('accept-return transitions SHIPPED → PENDING_RETURN', async ({ request }) => {
    const orderId = await createShippedWireOrder(request);

    const res = await request.post(`${API_URL}/api/vendor/orders/${orderId}/accept-return`, {
      headers: { Authorization: `Bearer ${vendorToken}`, 'Content-Type': 'application/json' },
      data: { buyerIban: 'FR7630006000011234567890189' },
    });

    expect(res.status()).toBe(200);
    const body = await res.json();
    expect(body.status).toBe('PENDING_RETURN');
  });

  test('confirm-return wire transitions PENDING_RETURN → WIRE_REFUND_IN_PROGRESS', async ({ request }) => {
    const orderId = await createShippedWireOrder(request);

    await request.post(`${API_URL}/api/vendor/orders/${orderId}/accept-return`, {
      headers: { Authorization: `Bearer ${vendorToken}`, 'Content-Type': 'application/json' },
      data: { buyerIban: 'FR7630006000011234567890189' },
    });

    const res = await request.post(`${API_URL}/api/vendor/orders/${orderId}/confirm-return`, {
      headers: { Authorization: `Bearer ${vendorToken}` },
    });

    expect(res.status()).toBe(200);
    const body = await res.json();
    expect(body.status).toBe('WIRE_REFUND_IN_PROGRESS');
  });

  test('accept-return wire missing IBAN returns 422', async ({ request }) => {
    const orderId = await createShippedWireOrder(request);

    const res = await request.post(`${API_URL}/api/vendor/orders/${orderId}/accept-return`, {
      headers: { Authorization: `Bearer ${vendorToken}`, 'Content-Type': 'application/json' },
      data: {},
    });

    expect(res.status()).toBe(422);
    const body = await res.json();
    expect(body.error).toBe('MISSING_BUYER_IBAN');
  });

  test('accept-return on non-shipped order returns 409', async ({ request }) => {
    // Create a wire order but only confirm payment (not shipped)
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
    await request.post(`${API_URL}/api/vendor/orders/${init.orderId}/confirm-wire`, {
      headers: { Authorization: `Bearer ${vendorToken}` },
    });

    const res = await request.post(`${API_URL}/api/vendor/orders/${init.orderId}/accept-return`, {
      headers: { Authorization: `Bearer ${vendorToken}`, 'Content-Type': 'application/json' },
      data: { buyerIban: 'FR7630006000011234567890189' },
    });
    expect(res.status()).toBe(409);
  });
});
