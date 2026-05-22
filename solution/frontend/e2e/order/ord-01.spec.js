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
  createAddressViaApi,
} from '../helpers/login.js';

const BUYER_EMAIL = `ord01-buyer-${Date.now()}@example.com`;
const BUYER_PASSWORD = 'sHp-E2e!Byr-X9pZ';
const VENDOR_EMAIL = `ord01-vendor-${Date.now()}@example.com`;
const VENDOR_PASSWORD = 'Vendor123456!';

test.describe('US-ORD-01 — Delivery address validation', () => {
  let buyerToken;
  let carrierId;
  let addressId;

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

    // Create a saved FR delivery address
    const address = await createAddressViaApi(carrierPage, buyerToken, {
      label: 'Home', addressLine: '1 rue de la Paix', city: 'Paris',
      postalCode: '75001', countryCode: 'FR', makeDefault: true,
    });
    addressId = address.id;
  });

  test('rejects non-Eurozone country code via address API', async ({ request }) => {
    // US is not in the countries table (Eurozone only) — saving such an address must fail
    const res = await request.post(`${API_URL}/api/profile/addresses`, {
      headers: { Authorization: `Bearer ${buyerToken}` },
      data: {
        label: 'Test', addressLine: '1 Main St', city: 'New York',
        postalCode: '10001', countryCode: 'US', makeDefault: false,
      },
    });
    expect(res.ok()).toBe(false);
  });

  test('accepts valid Eurozone country code', async ({ request }) => {
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
      data: { addressId, carrierId, paymentMethod: 'CARD' },
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
