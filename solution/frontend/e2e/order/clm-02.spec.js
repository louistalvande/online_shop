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

const BUYER_EMAIL = `clm02-buyer-${Date.now()}@example.com`;
const BUYER_PASSWORD = 'sHp-E2e!Byr-X9pZ';
const VENDOR_EMAIL = `clm02-vendor-${Date.now()}@example.com`;
const VENDOR_PASSWORD = 'Vendor123456!';

test.describe('US-CLM-02 — Vendor processes a claim', () => {
  let buyerToken;
  let vendorToken;
  let carrierId;
  let productId;
  let addressId;

  test.beforeAll(async ({ request }) => {
    const p = { request };
    const carrier = await createCarrierViaApi(p, {
      name: 'TestCarrier CLM02',
      trackingUrl: 'https://track.example.com/',
      supportedCountries: ['FR'],
    });
    carrierId = carrier.id;

    await createActiveVendorViaApi(p, VENDOR_EMAIL, VENDOR_PASSWORD);
    vendorToken = await getVendorToken(p, VENDOR_EMAIL, VENDOR_PASSWORD);

    const product = await createProductViaApi(p, vendorToken, {
      name: 'CLM02 Product',
      priceExclTax: 10.00,
      quantity: 50,
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

  async function createOpenClaim(request) {
    // Add to cart
    await request.post(`${API_URL}/api/cart/items`, {
      headers: { Authorization: `Bearer ${buyerToken}`, 'Content-Type': 'application/json' },
      data: { productId, quantity: 1 },
    });
    // Checkout with card
    const initRes = await request.post(`${API_URL}/api/orders`, {
      headers: { Authorization: `Bearer ${buyerToken}`, 'Content-Type': 'application/json' },
      data: { addressId, carrierId, paymentMethod: 'CARD' },
    });
    const init = await initRes.json();
    const orderId = init.orderId;
    // Confirm payment
    await request.post(`${API_URL}/api/orders/${orderId}/confirm-payment`, {
      headers: { Authorization: `Bearer ${buyerToken}` },
    });
    // Ship
    await request.post(`${API_URL}/api/vendor/orders/${orderId}/ship`, {
      headers: { Authorization: `Bearer ${vendorToken}`, 'Content-Type': 'application/json' },
      data: { trackingNumber: 'TRACK456' },
    });
    // Open claim
    const claimRes = await request.post(`${API_URL}/api/orders/${orderId}/claims`, {
      headers: { Authorization: `Bearer ${buyerToken}`, 'Content-Type': 'application/json' },
      data: { reason: 'DEFECTIVE_ITEM', message: 'Item arrived broken.' },
    });
    const claim = await claimRes.json();
    return claim.id;
  }

  test('vendor lists claims and sees the open one', async ({ request }) => {
    await createOpenClaim(request);

    const res = await request.get(`${API_URL}/api/vendor/claims`, {
      headers: { Authorization: `Bearer ${vendorToken}` },
    });

    expect(res.status()).toBe(200);
    const body = await res.json();
    expect(body.length).toBeGreaterThan(0);
    const openClaims = body.filter(c => c.status === 'OPEN');
    expect(openClaims.length).toBeGreaterThan(0);
  });

  test('vendor grants refund — claim transitions to CLOSED GRANTED', async ({ request }) => {
    const claimId = await createOpenClaim(request);

    const res = await request.post(`${API_URL}/api/vendor/claims/${claimId}/grant`, {
      headers: { Authorization: `Bearer ${vendorToken}` },
    });

    expect(res.status()).toBe(200);
    const body = await res.json();
    expect(body.status).toBe('CLOSED');
    expect(body.decision).toBe('GRANTED');
  });

  test('vendor refuses refund — claim transitions to CLOSED REFUSED', async ({ request }) => {
    const claimId = await createOpenClaim(request);

    const res = await request.post(`${API_URL}/api/vendor/claims/${claimId}/refuse`, {
      headers: { Authorization: `Bearer ${vendorToken}` },
    });

    expect(res.status()).toBe(200);
    const body = await res.json();
    expect(body.status).toBe('CLOSED');
    expect(body.decision).toBe('REFUSED');
  });

  test('grant on already closed claim — returns 409', async ({ request }) => {
    const claimId = await createOpenClaim(request);
    // Close it first
    await request.post(`${API_URL}/api/vendor/claims/${claimId}/refuse`, {
      headers: { Authorization: `Bearer ${vendorToken}` },
    });
    // Try to grant after closing
    const res = await request.post(`${API_URL}/api/vendor/claims/${claimId}/grant`, {
      headers: { Authorization: `Bearer ${vendorToken}` },
    });

    expect(res.status()).toBe(409);
    const body = await res.json();
    expect(body.error).toBe('INVALID_CLAIM_STATE');
  });

  test('claim not found — returns 404', async ({ request }) => {
    const res = await request.post(`${API_URL}/api/vendor/claims/${crypto.randomUUID()}/grant`, {
      headers: { Authorization: `Bearer ${vendorToken}` },
    });

    expect(res.status()).toBe(404);
    const body = await res.json();
    expect(body.error).toBe('CLAIM_NOT_FOUND');
  });

  test('unauthenticated request — returns 401', async ({ request }) => {
    const res = await request.get(`${API_URL}/api/vendor/claims`);
    expect(res.status()).toBe(401);
  });
});
