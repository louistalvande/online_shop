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

const BUYER_EMAIL = `ord04-buyer-${Date.now()}@example.com`;
const BUYER_PASSWORD = 'Buyer123456!';
const VENDOR_EMAIL = `ord04-vendor-${Date.now()}@example.com`;
const VENDOR_PASSWORD = 'Vendor123456!';

test.describe('US-ORD-04 — Wire transfer payment', () => {
  let buyerToken;
  let carrierId;
  let productId;

  test.beforeAll(async ({ request }) => {
    const p = { request };
    const carrier = await createCarrierViaApi(p, {
      name: 'TestCarrier ORD04',
      trackingUrl: 'https://track.example.com',
      supportedCountries: ['FR'],
    });
    carrierId = carrier.id;

    await createActiveVendorViaApi(p, VENDOR_EMAIL, VENDOR_PASSWORD);
    const vendorToken = await getVendorToken(p, VENDOR_EMAIL, VENDOR_PASSWORD);
    const product = await createProductViaApi(p, vendorToken, {
      name: 'Test Product ORD04',
      priceExclTax: 20.00,
      quantity: 5,
      status: 'PUBLISHED',
    });
    productId = product.id;

    await registerAndActivateBuyerViaApi(p, BUYER_EMAIL, BUYER_PASSWORD);
    buyerToken = await getBuyerToken(p, BUYER_EMAIL, BUYER_PASSWORD);
  });

  test('wire checkout creates order in PAYMENT_PENDING_WIRE and returns bank details', async ({ request }) => {
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
        paymentMethod: 'WIRE_TRANSFER',
      },
    });

    expect(res.status()).toBe(201);
    const body = await res.json();
    expect(body.paymentMethod).toBe('WIRE_TRANSFER');
    expect(body.bankIban).toBeTruthy();
    expect(body.bankBic).toBeTruthy();
    expect(body.paymentReference).toMatch(/^ORD-/);
    expect(body.totalAmountTtc).toBeGreaterThan(0);
  });

  test('wire order cart is cleared after checkout', async ({ request }) => {
    // Add product to cart
    await request.post(`${API_URL}/api/cart/items`, {
      headers: { Authorization: `Bearer ${buyerToken}` },
      data: { productId, quantity: 1 },
    });

    await request.post(`${API_URL}/api/orders`, {
      headers: { Authorization: `Bearer ${buyerToken}` },
      data: {
        deliveryAddressLine: '2 rue Test',
        deliveryCity: 'Lyon',
        deliveryPostalCode: '69001',
        deliveryCountryCode: 'FR',
        carrierId,
        paymentMethod: 'WIRE_TRANSFER',
      },
    });

    // Cart should now be empty
    const cartRes = await request.get(`${API_URL}/api/cart`, {
      headers: { Authorization: `Bearer ${buyerToken}` },
    });
    expect(cartRes.status()).toBe(200);
    const cart = await cartRes.json();
    expect(cart.items).toHaveLength(0);
  });
});
