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

const BUYER_EMAIL = `can02-buyer-${Date.now()}@example.com`;
const BUYER_PASSWORD = 'sHp-E2e!Byr-X9pZ';
const VENDOR_EMAIL = `can02-vendor-${Date.now()}@example.com`;
const VENDOR_PASSWORD = 'Vendor123456!';

test.describe('US-CAN-02 — Vendor sees wire refund in progress after buyer cancellation', () => {
  let buyerToken;
  let vendorToken;
  let carrierId;
  let productId;
  let addressId;

  test.beforeAll(async ({ request }) => {
    const p = { request };
    const carrier = await createCarrierViaApi(p, {
      name: 'TestCarrier CAN02',
      trackingUrl: 'https://track.example.com/',
      supportedCountries: ['FR'],
    });
    carrierId = carrier.id;

    await createActiveVendorViaApi(p, VENDOR_EMAIL, VENDOR_PASSWORD);
    vendorToken = await getVendorToken(p, VENDOR_EMAIL, VENDOR_PASSWORD);

    const product = await createProductViaApi(p, vendorToken, {
      name: 'CAN02 Product',
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

  test('vendor order shows WIRE_REFUND_IN_PROGRESS with buyerIban after buyer wire cancellation', async ({ request }) => {
    // Buyer places wire order
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

    // Vendor confirms wire payment
    await request.post(`${API_URL}/api/vendor/orders/${orderId}/confirm-wire`, {
      headers: { Authorization: `Bearer ${vendorToken}` },
    });

    // Buyer cancels the order with IBAN
    await request.post(`${API_URL}/api/orders/${orderId}/cancel`, {
      headers: { Authorization: `Bearer ${buyerToken}`, 'Content-Type': 'application/json' },
      data: { buyerIban: 'FR7630006000011234567890189' },
    });

    // Vendor retrieves the order — should see WIRE_REFUND_IN_PROGRESS with buyerIban
    const vendorRes = await request.get(`${API_URL}/api/vendor/orders/${orderId}`, {
      headers: { Authorization: `Bearer ${vendorToken}` },
    });
    expect(vendorRes.status()).toBe(200);
    const order = await vendorRes.json();
    expect(order.status).toBe('WIRE_REFUND_IN_PROGRESS');
    expect(order.buyerIban).toBe('FR7630006000011234567890189');
    expect(order.totalAmountTtc).toBeGreaterThan(0);
  });

  test('card order cancelled by buyer — vendor sees CANCELLED', async ({ request }) => {
    // Buyer places card order
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
    const orderId = init.orderId;

    // Buyer cancels
    await request.post(`${API_URL}/api/orders/${orderId}/cancel`, {
      headers: { Authorization: `Bearer ${buyerToken}`, 'Content-Type': 'application/json' },
      data: {},
    });

    // Vendor sees CANCELLED
    const vendorRes = await request.get(`${API_URL}/api/vendor/orders/${orderId}`, {
      headers: { Authorization: `Bearer ${vendorToken}` },
    });
    const order = await vendorRes.json();
    expect(order.status).toBe('CANCELLED');
  });
});
