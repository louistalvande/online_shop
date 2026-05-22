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

const BUYER_EMAIL = `ord05-buyer-${Date.now()}@example.com`;
const BUYER_PASSWORD = 'sHp-E2e!Byr-X9pZ';
const VENDOR_EMAIL = `ord05-vendor-${Date.now()}@example.com`;
const VENDOR_PASSWORD = 'Vendor123456!';

test.describe('US-ORD-05 — Order confirmation (listing and status)', () => {
  let buyerToken;
  let carrierId;
  let productId;
  let orderId;
  let addressId;

  test.beforeAll(async ({ request }) => {
    const p = { request };
    const carrier = await createCarrierViaApi(p, {
      name: 'TestCarrier ORD05',
      trackingUrl: 'https://track.example.com',
      supportedCountries: ['FR'],
    });
    carrierId = carrier.id;

    await createActiveVendorViaApi(p, VENDOR_EMAIL, VENDOR_PASSWORD);
    const vendorToken = await getVendorToken(p, VENDOR_EMAIL, VENDOR_PASSWORD);
    const product = await createProductViaApi(p, vendorToken, {
      name: 'Test Product ORD05',
      priceExclTax: 25.00,
      quantity: 10,
      status: 'PUBLISHED',
    });
    productId = product.id;

    await registerAndActivateBuyerViaApi(p, BUYER_EMAIL, BUYER_PASSWORD);
    buyerToken = await getBuyerToken(p, BUYER_EMAIL, BUYER_PASSWORD);

    const address = await createAddressViaApi(p, buyerToken, {
      label: 'Home', addressLine: '1 rue de la Paix', city: 'Paris',
      postalCode: '75001', countryCode: 'FR', makeDefault: true,
    });
    addressId = address.id;

    // Place and confirm a card order
    await request.post(`${API_URL}/api/cart/items`, {
      headers: { Authorization: `Bearer ${buyerToken}` },
      data: { productId, quantity: 1 },
    });
    const initRes = await request.post(`${API_URL}/api/orders`, {
      headers: { Authorization: `Bearer ${buyerToken}` },
      data: { addressId, carrierId, paymentMethod: 'CARD' },
    });
    const init = await initRes.json();
    orderId = init.orderId;
    await request.post(`${API_URL}/api/orders/${orderId}/confirm-payment`, {
      headers: { Authorization: `Bearer ${buyerToken}` },
    });
  });

  test('GET /api/orders returns the confirmed order', async ({ request }) => {
    const res = await request.get(`${API_URL}/api/orders`, {
      headers: { Authorization: `Bearer ${buyerToken}` },
    });
    expect(res.status()).toBe(200);
    const orders = await res.json();
    expect(orders.length).toBeGreaterThan(0);
    const order = orders.find(o => o.id === orderId);
    expect(order).toBeTruthy();
    expect(order.status).toBe('AWAITING_PROCESSING');
  });

  test('GET /api/orders/:id returns order details', async ({ request }) => {
    const res = await request.get(`${API_URL}/api/orders/${orderId}`, {
      headers: { Authorization: `Bearer ${buyerToken}` },
    });
    expect(res.status()).toBe(200);
    const order = await res.json();
    expect(order.id).toBe(orderId);
    expect(order.lines.length).toBeGreaterThan(0);
  });

  test('GET /api/orders/:id returns 404 for another buyer', async ({ request }) => {
    // Create a second buyer
    const email2 = `ord05-b2-${Date.now()}@example.com`;
    const p = { request };
    await registerAndActivateBuyerViaApi(p, email2, BUYER_PASSWORD);
    const token2 = await getBuyerToken(p, email2, BUYER_PASSWORD);

    const res = await request.get(`${API_URL}/api/orders/${orderId}`, {
      headers: { Authorization: `Bearer ${token2}` },
    });
    expect(res.status()).toBe(404);
  });
});
