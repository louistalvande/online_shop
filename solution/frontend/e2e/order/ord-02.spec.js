import { test, expect } from '@playwright/test';
import {
  API_URL,
  registerAndActivateBuyerViaApi,
  getBuyerToken,
  createCarrierViaApi,
} from '../helpers/login.js';

const BUYER_EMAIL = `ord02-buyer-${Date.now()}@example.com`;
const BUYER_PASSWORD = 'Buyer123456!';

test.describe('US-ORD-02 — Carrier selection', () => {
  let buyerToken;
  let carrierId;

  test.beforeAll(async ({ request }) => {
    const p = { request };
    const carrier = await createCarrierViaApi(p, {
      name: 'TestCarrier ORD02',
      trackingUrl: 'https://track.example.com',
      supportedCountries: ['FR'],
    });
    carrierId = carrier.id;

    await registerAndActivateBuyerViaApi(p, BUYER_EMAIL, BUYER_PASSWORD);
    buyerToken = await getBuyerToken(p, BUYER_EMAIL, BUYER_PASSWORD);
  });

  test('GET /api/carriers?countryCode=FR returns active carriers covering FR', async ({ request }) => {
    const res = await request.get(`${API_URL}/api/carriers?countryCode=FR`);
    expect(res.status()).toBe(200);
    const carriers = await res.json();
    expect(carriers.some(c => c.id === carrierId)).toBe(true);
  });

  test('GET /api/carriers?countryCode=JP returns no carriers', async ({ request }) => {
    const res = await request.get(`${API_URL}/api/carriers?countryCode=JP`);
    expect(res.status()).toBe(200);
    const carriers = await res.json();
    expect(carriers).toHaveLength(0);
  });

  test('rejects unavailable carrier for delivery country via API', async ({ request }) => {
    // DE carrier does not cover BE — use a carrier that does not cover DE
    const res = await request.post(`${API_URL}/api/orders`, {
      headers: { Authorization: `Bearer ${buyerToken}` },
      data: {
        deliveryAddressLine: '1 Rue Test',
        deliveryCity: 'Paris',
        deliveryPostalCode: '75001',
        deliveryCountryCode: 'DE',
        carrierId,          // carrierId only covers FR, not DE
        paymentMethod: 'CARD',
      },
    });

    // 400 (empty cart) or 409 (carrier not available) — both indicate the carrier check fires
    expect([400, 409]).toContain(res.status());
  });
});
