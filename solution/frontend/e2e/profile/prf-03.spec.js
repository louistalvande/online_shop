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

const BUYER_EMAIL = `prf03-buyer-${Date.now()}@example.com`;
const BUYER_PASSWORD = 'sHp-E2e!Byr-X9pZ';

test.describe('US-PRF-03 — Buyer address book', () => {
  let buyerToken;

  test.beforeAll(async ({ request }) => {
    const p = { request };
    await registerAndActivateBuyerViaApi(p, BUYER_EMAIL, BUYER_PASSWORD);
    buyerToken = await getBuyerToken(p, BUYER_EMAIL, BUYER_PASSWORD);
  });

  // ─── GET /api/profile/addresses ────────────────────────────────────────────

  test('GET returns empty list initially', async ({ request }) => {
    const res = await request.get(`${API_URL}/api/profile/addresses`, {
      headers: { Authorization: `Bearer ${buyerToken}` },
    });
    expect(res.status()).toBe(200);
    const body = await res.json();
    expect(Array.isArray(body)).toBe(true);
    expect(body).toHaveLength(0);
  });

  // ─── POST /api/profile/addresses ───────────────────────────────────────────

  test('POST creates address and returns 201 with body', async ({ request }) => {
    const res = await request.post(`${API_URL}/api/profile/addresses`, {
      headers: { Authorization: `Bearer ${buyerToken}` },
      data: {
        label: 'Home', recipientName: 'Marie Dupont', addressLine: '1 rue de la Paix', city: 'Paris',
        postalCode: '75001', countryCode: 'FR', makeDefault: false,
      },
    });
    expect(res.status()).toBe(201);
    const body = await res.json();
    expect(body.id).toBeTruthy();
    expect(body.label).toBe('Home');
    expect(body.addressLine).toBe('1 rue de la Paix');
    expect(body.city).toBe('Paris');
    expect(body.postalCode).toBe('75001');
    expect(body.countryCode).toBe('FR');
  });

  test('POST first address is automatically the default', async ({ request }) => {
    const email = `prf03-default-${Date.now()}@example.com`;
    const p = { request };
    await registerAndActivateBuyerViaApi(p, email, BUYER_PASSWORD);
    const token = await getBuyerToken(p, email, BUYER_PASSWORD);

    const res = await request.post(`${API_URL}/api/profile/addresses`, {
      headers: { Authorization: `Bearer ${token}` },
      data: {
        label: 'Work', recipientName: 'Jean Martin', addressLine: '10 avenue Test', city: 'Lyon',
        postalCode: '69001', countryCode: 'FR', makeDefault: false,
      },
    });
    expect(res.status()).toBe(201);
    const body = await res.json();
    expect(body.default).toBe(true);
  });

  test('POST with makeDefault=true clears previous default', async ({ request }) => {
    const email = `prf03-mkdef-${Date.now()}@example.com`;
    const p = { request };
    await registerAndActivateBuyerViaApi(p, email, BUYER_PASSWORD);
    const token = await getBuyerToken(p, email, BUYER_PASSWORD);

    // First address (auto-default)
    const first = await createAddressViaApi(p, token, {
      label: 'First', recipientName: 'Test Recipient', addressLine: '1 rue Test', city: 'Paris',
      postalCode: '75001', countryCode: 'FR', makeDefault: false,
    });
    expect(first.default).toBe(true);

    // Second address with makeDefault=true
    await request.post(`${API_URL}/api/profile/addresses`, {
      headers: { Authorization: `Bearer ${token}` },
      data: {
        label: 'Second', recipientName: 'Test Recipient', addressLine: '2 rue Test', city: 'Paris',
        postalCode: '75002', countryCode: 'FR', makeDefault: true,
      },
    });

    // List — first should no longer be default
    const listRes = await request.get(`${API_URL}/api/profile/addresses`, {
      headers: { Authorization: `Bearer ${token}` },
    });
    const addresses = await listRes.json();
    const firstInList = addresses.find(a => a.id === first.id);
    expect(firstInList.default).toBe(false);
    const secondInList = addresses.find(a => a.label === 'Second');
    expect(secondInList.default).toBe(true);
  });

  test('POST with non-Eurozone countryCode is rejected', async ({ request }) => {
    const res = await request.post(`${API_URL}/api/profile/addresses`, {
      headers: { Authorization: `Bearer ${buyerToken}` },
      data: {
        label: 'US Home', recipientName: 'John Doe', addressLine: '1 Main St', city: 'New York',
        postalCode: '10001', countryCode: 'US', makeDefault: false,
      },
    });
    expect(res.ok()).toBe(false);
  });

  test('POST unauthenticated returns 401', async ({ request }) => {
    const res = await request.post(`${API_URL}/api/profile/addresses`, {
      data: {
        label: 'Home', recipientName: 'Test Recipient', addressLine: '1 rue Test', city: 'Paris',
        postalCode: '75001', countryCode: 'FR', makeDefault: false,
      },
    });
    expect(res.status()).toBe(401);
  });

  // ─── PUT /api/profile/addresses/:id ────────────────────────────────────────

  test('PUT updates label and city, returns 200', async ({ request }) => {
    const email = `prf03-upd-${Date.now()}@example.com`;
    const p = { request };
    await registerAndActivateBuyerViaApi(p, email, BUYER_PASSWORD);
    const token = await getBuyerToken(p, email, BUYER_PASSWORD);

    const address = await createAddressViaApi(p, token, {
      label: 'Old Label', recipientName: 'Test Recipient', addressLine: '1 rue Test', city: 'Paris',
      postalCode: '75001', countryCode: 'FR', makeDefault: false,
    });

    const res = await request.put(`${API_URL}/api/profile/addresses/${address.id}`, {
      headers: { Authorization: `Bearer ${token}` },
      data: {
        label: 'New Label', recipientName: 'Test Recipient', addressLine: '1 rue Test', city: 'Marseille',
        postalCode: '13001', countryCode: 'FR', makeDefault: false,
      },
    });
    expect(res.status()).toBe(200);
    const body = await res.json();
    expect(body.label).toBe('New Label');
    expect(body.city).toBe('Marseille');
  });

  test('PUT with makeDefault=true promotes address to default', async ({ request }) => {
    const email = `prf03-promdef-${Date.now()}@example.com`;
    const p = { request };
    await registerAndActivateBuyerViaApi(p, email, BUYER_PASSWORD);
    const token = await getBuyerToken(p, email, BUYER_PASSWORD);

    const first = await createAddressViaApi(p, token, {
      label: 'First', recipientName: 'Test Recipient', addressLine: '1 rue Test', city: 'Paris',
      postalCode: '75001', countryCode: 'FR', makeDefault: false,
    });
    const second = await createAddressViaApi(p, token, {
      label: 'Second', recipientName: 'Test Recipient', addressLine: '2 rue Test', city: 'Lyon',
      postalCode: '69001', countryCode: 'FR', makeDefault: false,
    });

    // Promote second to default
    const res = await request.put(`${API_URL}/api/profile/addresses/${second.id}`, {
      headers: { Authorization: `Bearer ${token}` },
      data: {
        label: 'Second', recipientName: 'Test Recipient', addressLine: '2 rue Test', city: 'Lyon',
        postalCode: '69001', countryCode: 'FR', makeDefault: true,
      },
    });
    expect(res.status()).toBe(200);
    const body = await res.json();
    expect(body.default).toBe(true);
  });

  test('PUT non-existent address returns 404', async ({ request }) => {
    const res = await request.put(`${API_URL}/api/profile/addresses/${crypto.randomUUID()}`, {
      headers: { Authorization: `Bearer ${buyerToken}` },
      data: {
        label: 'X', recipientName: 'Test Recipient', addressLine: '1 rue X', city: 'Paris',
        postalCode: '75001', countryCode: 'FR', makeDefault: false,
      },
    });
    expect(res.status()).toBe(404);
  });

  // ─── PATCH /api/profile/addresses/:id/default ──────────────────────────────

  test('PATCH /default sets address as default and clears previous', async ({ request }) => {
    const email = `prf03-setdef-${Date.now()}@example.com`;
    const p = { request };
    await registerAndActivateBuyerViaApi(p, email, BUYER_PASSWORD);
    const token = await getBuyerToken(p, email, BUYER_PASSWORD);

    const first = await createAddressViaApi(p, token, {
      label: 'First', recipientName: 'Test Recipient', addressLine: '1 rue Test', city: 'Paris',
      postalCode: '75001', countryCode: 'FR', makeDefault: false,
    });
    const second = await createAddressViaApi(p, token, {
      label: 'Second', recipientName: 'Test Recipient', addressLine: '2 rue Test', city: 'Lyon',
      postalCode: '69001', countryCode: 'FR', makeDefault: false,
    });

    const res = await request.patch(`${API_URL}/api/profile/addresses/${second.id}/default`, {
      headers: { Authorization: `Bearer ${token}` },
    });
    expect(res.status()).toBe(200);
    const body = await res.json();
    expect(body.id).toBe(second.id);
    expect(body.default).toBe(true);

    // Verify first is no longer default
    const listRes = await request.get(`${API_URL}/api/profile/addresses`, {
      headers: { Authorization: `Bearer ${token}` },
    });
    const addresses = await listRes.json();
    const firstInList = addresses.find(a => a.id === first.id);
    expect(firstInList.default).toBe(false);
  });

  test('PATCH /default non-existent address returns 404', async ({ request }) => {
    const res = await request.patch(`${API_URL}/api/profile/addresses/${crypto.randomUUID()}/default`, {
      headers: { Authorization: `Bearer ${buyerToken}` },
    });
    expect(res.status()).toBe(404);
  });

  // ─── DELETE /api/profile/addresses/:id ─────────────────────────────────────

  test('DELETE soft-deletes a non-default address and returns 204', async ({ request }) => {
    const email = `prf03-del-${Date.now()}@example.com`;
    const p = { request };
    await registerAndActivateBuyerViaApi(p, email, BUYER_PASSWORD);
    const token = await getBuyerToken(p, email, BUYER_PASSWORD);

    const first = await createAddressViaApi(p, token, {
      label: 'Keep', recipientName: 'Test Recipient', addressLine: '1 rue Test', city: 'Paris',
      postalCode: '75001', countryCode: 'FR', makeDefault: false,
    });
    const second = await createAddressViaApi(p, token, {
      label: 'Delete Me', recipientName: 'Test Recipient', addressLine: '2 rue Test', city: 'Lyon',
      postalCode: '69001', countryCode: 'FR', makeDefault: false,
    });

    const res = await request.delete(`${API_URL}/api/profile/addresses/${second.id}`, {
      headers: { Authorization: `Bearer ${token}` },
    });
    expect(res.status()).toBe(204);

    // Address no longer in list
    const listRes = await request.get(`${API_URL}/api/profile/addresses`, {
      headers: { Authorization: `Bearer ${token}` },
    });
    const addresses = await listRes.json();
    expect(addresses.find(a => a.id === second.id)).toBeUndefined();
  });

  test('DELETE last address returns 409', async ({ request }) => {
    const email = `prf03-last-${Date.now()}@example.com`;
    const p = { request };
    await registerAndActivateBuyerViaApi(p, email, BUYER_PASSWORD);
    const token = await getBuyerToken(p, email, BUYER_PASSWORD);

    const address = await createAddressViaApi(p, token, {
      label: 'Only', recipientName: 'Test Recipient', addressLine: '1 rue Test', city: 'Paris',
      postalCode: '75001', countryCode: 'FR', makeDefault: false,
    });

    const res = await request.delete(`${API_URL}/api/profile/addresses/${address.id}`, {
      headers: { Authorization: `Bearer ${token}` },
    });
    expect(res.status()).toBe(409);
    const body = await res.json();
    expect(body.error).toBe('LAST_ACTIVE_ADDRESS');
  });

  test('DELETE non-existent address returns 404', async ({ request }) => {
    const res = await request.delete(`${API_URL}/api/profile/addresses/${crypto.randomUUID()}`, {
      headers: { Authorization: `Bearer ${buyerToken}` },
    });
    expect(res.status()).toBe(404);
  });

  test('DELETE unauthenticated returns 401', async ({ request }) => {
    const res = await request.delete(`${API_URL}/api/profile/addresses/${crypto.randomUUID()}`);
    expect(res.status()).toBe(401);
  });

  // ─── Integration: saved address used in order checkout ─────────────────────

  test('saved address can be used to place an order', async ({ request }) => {
    const email = `prf03-order-${Date.now()}@example.com`;
    const vendorEmail = `prf03-vendor-${Date.now()}@example.com`;
    const p = { request };

    const carrier = await createCarrierViaApi(p, {
      name: 'TestCarrier PRF03',
      trackingUrl: 'https://track.example.com/',
      supportedCountries: ['FR'],
    });

    await createActiveVendorViaApi(p, vendorEmail, BUYER_PASSWORD);
    const vendorTok = await getVendorToken(p, vendorEmail, BUYER_PASSWORD);
    const product = await createProductViaApi(p, vendorTok, {
      name: 'PRF03 Product',
      priceExclTax: 12.00,
      quantity: 5,
      status: 'PUBLISHED',
    });

    await registerAndActivateBuyerViaApi(p, email, BUYER_PASSWORD);
    const token = await getBuyerToken(p, email, BUYER_PASSWORD);

    const address = await createAddressViaApi(p, token, {
      label: 'Checkout Address', recipientName: 'Test Recipient', addressLine: '5 rue de Rivoli', city: 'Paris',
      postalCode: '75001', countryCode: 'FR', makeDefault: true,
    });

    await request.post(`${API_URL}/api/cart/items`, {
      headers: { Authorization: `Bearer ${token}` },
      data: { productId: product.id, quantity: 1 },
    });

    const orderRes = await request.post(`${API_URL}/api/orders`, {
      headers: { Authorization: `Bearer ${token}` },
      data: { addressId: address.id, carrierId: carrier.id, paymentMethod: 'CARD' },
    });

    expect(orderRes.status()).toBe(201);
    const order = await orderRes.json();
    expect(order.orderId).toBeTruthy();
    expect(order.paymentMethod).toBe('CARD');
  });
});
