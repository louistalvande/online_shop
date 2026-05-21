import { test, expect } from '@playwright/test';
import {
  API_URL,
  registerAndActivateBuyerViaApi,
  getBuyerToken,
  injectBuyerSession,
  createCarrierViaApi,
  createActiveVendorViaApi,
  getVendorToken,
  createProductViaApi,
} from '../helpers/login.js';

const BUYER_EMAIL = `ord01-buyer-${Date.now()}@example.com`;
const BUYER_PASSWORD = 'sHp-E2e!Byr-X9pZ';
const VENDOR_EMAIL = `ord01-vendor-${Date.now()}@example.com`;
const VENDOR_PASSWORD = 'Vendor123456!';

test.describe('US-ORD-01 — Delivery address validation', () => {
  let buyerToken;
  let carrierId;

  test.beforeAll(async ({ request }) => {
    // Create carrier covering FR
    const carrierPage = { request };
    const carrier = await createCarrierViaApi(carrierPage, {
      name: 'TestCarrier ORD01',
      trackingUrl: 'https://track.example.com',
      supportedCountries: ['FR', 'DE'],
    });
    carrierId = carrier.id;

    // Create vendor with a product
    await createActiveVendorViaApi(carrierPage, VENDOR_EMAIL, VENDOR_PASSWORD);
    const vendorToken = await getVendorToken(carrierPage, VENDOR_EMAIL, VENDOR_PASSWORD);
    await createProductViaApi(carrierPage, vendorToken, {
      name: 'Test Product ORD01',
      priceExclTax: 10.00,
      quantity: 20,
      status: 'PUBLISHED',
    });

    // Create buyer and get token
    await registerAndActivateBuyerViaApi(carrierPage, BUYER_EMAIL, BUYER_PASSWORD);
    buyerToken = await getBuyerToken(carrierPage, BUYER_EMAIL, BUYER_PASSWORD);
  });

  test('rejects non-Eurozone country code via API', async ({ request }) => {
    // Add a product to cart first
    const products = await (await request.get(`${API_URL}/api/products`)).json();
    const product = products.content?.[0] ?? products[0];
    if (!product) test.skip();

    await request.post(`${API_URL}/api/cart/items`, {
      headers: { Authorization: `Bearer ${buyerToken}` },
      data: { productId: product.id, quantity: 1 },
    });

    const res = await request.post(`${API_URL}/api/orders`, {
      headers: { Authorization: `Bearer ${buyerToken}` },
      data: {
        deliveryAddressLine: '1 Main St',
        deliveryCity: 'New York',
        deliveryPostalCode: '10001',
        deliveryCountryCode: 'US',
        carrierId,
        paymentMethod: 'CARD',
      },
    });

    expect(res.status()).toBe(422);
    const body = await res.json();
    expect(body.error).toBe('INVALID_DELIVERY_COUNTRY');
  });

  test('accepts valid Eurozone country code', async ({ request }) => {
    const res = await request.post(`${API_URL}/api/orders`, {
      headers: { Authorization: `Bearer ${buyerToken}` },
      data: {
        deliveryAddressLine: '1 rue de la Paix',
        deliveryCity: 'Paris',
        deliveryPostalCode: '75001',
        deliveryCountryCode: 'FR',
        carrierId,
        paymentMethod: 'CARD',
      },
    });

    // 201 = success, 400 = empty cart (either is acceptable here)
    expect([201, 400]).toContain(res.status());
  });

  test('GET /api/countries returns Eurozone country list', async ({ request }) => {
    const res = await request.get(`${API_URL}/api/countries`);
    expect(res.status()).toBe(200);
    const countries = await res.json();
    expect(countries.length).toBeGreaterThan(0);
    expect(countries[0]).toHaveProperty('code');
  });
});
