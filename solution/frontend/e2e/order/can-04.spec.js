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

const BUYER_EMAIL = `can04-buyer-${Date.now()}@example.com`;
const BUYER_PASSWORD = 'sHp-E2e!Byr-X9pZ';
const VENDOR_EMAIL = `can04-vendor-${Date.now()}@example.com`;
const VENDOR_PASSWORD = 'Vendor123456!';

test.describe('US-CAN-04 — Vendor accepts post-shipment cancellation without return', () => {
  let buyerToken;
  let vendorToken;
  let carrierId;
  let productId;
  let addressId;

  test.beforeAll(async ({ request }) => {
    const p = { request };
    const carrier = await createCarrierViaApi(p, {
      name: 'TestCarrier CAN04',
      trackingUrl: 'https://track.example.com/',
      supportedCountries: ['FR'],
    });
    carrierId = carrier.id;

    await createActiveVendorViaApi(p, VENDOR_EMAIL, VENDOR_PASSWORD);
    vendorToken = await getVendorToken(p, VENDOR_EMAIL, VENDOR_PASSWORD);

    const product = await createProductViaApi(p, vendorToken, {
      name: 'CAN04 Product',
      priceExclTax: 10.00,
      quantity: 10,
      category: 'PAINTING',
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

  async function createShippedOrder(request, paymentMethod) {
    await request.post(`${API_URL}/api/cart/items`, {
      headers: { Authorization: `Bearer ${buyerToken}`, 'Content-Type': 'application/json' },
      data: { productId, quantity: 1 },
    });
    const initRes = await request.post(`${API_URL}/api/orders`, {
      headers: { Authorization: `Bearer ${buyerToken}`, 'Content-Type': 'application/json' },
      data: { addressId, carrierId, paymentMethod },
    });
    const init = await initRes.json();
    const orderId = init.orderId;

    if (paymentMethod === 'WIRE_TRANSFER') {
      await request.post(`${API_URL}/api/vendor/orders/${orderId}/confirm-wire`, {
        headers: { Authorization: `Bearer ${vendorToken}` },
      });
    } else {
      await request.post(`${API_URL}/api/orders/${orderId}/confirm-payment`, {
        headers: { Authorization: `Bearer ${buyerToken}` },
      });
    }

    await request.post(`${API_URL}/api/vendor/orders/${orderId}/ship`, {
      headers: { Authorization: `Bearer ${vendorToken}`, 'Content-Type': 'application/json' },
      data: { trackingNumber: 'TRACK-CAN04' },
    });
    return orderId;
  }

  test('waive-return wire transitions SHIPPED → WIRE_REFUND_IN_PROGRESS', async ({ request }) => {
    const orderId = await createShippedOrder(request, 'WIRE_TRANSFER');

    const res = await request.post(`${API_URL}/api/vendor/orders/${orderId}/waive-return`, {
      headers: { Authorization: `Bearer ${vendorToken}`, 'Content-Type': 'application/json' },
      data: { buyerIban: 'FR7630006000011234567890189' },
    });

    expect(res.status()).toBe(200);
    const body = await res.json();
    expect(body.status).toBe('WIRE_REFUND_IN_PROGRESS');
    expect(body.buyerIban).toBe('FR7630006000011234567890189');
  });

  test('waive-return card transitions SHIPPED → CANCELLED', async ({ request }) => {
    const orderId = await createShippedOrder(request, 'CARD');

    const res = await request.post(`${API_URL}/api/vendor/orders/${orderId}/waive-return`, {
      headers: { Authorization: `Bearer ${vendorToken}`, 'Content-Type': 'application/json' },
      data: {},
    });

    expect(res.status()).toBe(200);
    const body = await res.json();
    expect(body.status).toBe('CANCELLED');
  });

  test('waive-return wire missing IBAN returns 422', async ({ request }) => {
    const orderId = await createShippedOrder(request, 'WIRE_TRANSFER');

    const res = await request.post(`${API_URL}/api/vendor/orders/${orderId}/waive-return`, {
      headers: { Authorization: `Bearer ${vendorToken}`, 'Content-Type': 'application/json' },
      data: {},
    });

    expect(res.status()).toBe(422);
  });

  test('waive-return on non-shipped order returns 409', async ({ request }) => {
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

    const res = await request.post(`${API_URL}/api/vendor/orders/${init.orderId}/waive-return`, {
      headers: { Authorization: `Bearer ${vendorToken}`, 'Content-Type': 'application/json' },
      data: { buyerIban: 'FR7630006000011234567890189' },
    });
    expect(res.status()).toBe(409);
  });
});
