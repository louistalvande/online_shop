import { test, expect } from '@playwright/test';
import {
  API_URL,
  createActiveVendorViaApi, getVendorToken, injectVendorSession,
  registerAndActivateBuyerViaApi, getBuyerToken,
  createCarrierViaApi, createProductViaApi, createAddressViaApi,
} from '../helpers/login.js';

// VND-ORDER-SHIP — Vendor declares shipment UI (US-EXP-01 IHM) and mark-in-preparation UI.

const VENDOR_EMAIL    = `vnd-ship-${Date.now()}@shop-test.example`;
const VENDOR_PASSWORD = 'VndShip!Secure2026';
const BUYER_PASSWORD  = 'ByrShip!Secure2026';

async function createOrderAndConfirmWire(request, vendorToken, carrierId, productId) {
  const buyerEmail = `ship-buyer-${Date.now()}@shop-test.example`;
  const p = { request };
  await registerAndActivateBuyerViaApi(p, buyerEmail, BUYER_PASSWORD);
  const buyerToken = await getBuyerToken(p, buyerEmail, BUYER_PASSWORD);
  const address = await createAddressViaApi(p, buyerToken, {
    label: 'Home', recipientName: 'Test', addressLine: '1 rue Test',
    city: 'Paris', postalCode: '75001', countryCode: 'FR', makeDefault: true,
  });
  await request.post(`${API_URL}/api/cart/items`, {
    headers: { Authorization: `Bearer ${buyerToken}` },
    data: { productId, quantity: 1 },
  });
  const initRes = await request.post(`${API_URL}/api/orders`, {
    headers: { Authorization: `Bearer ${buyerToken}` },
    data: { addressId: address.id, carrierId, paymentMethod: 'WIRE_TRANSFER' },
  });
  const orderId = (await initRes.json()).orderId;
  await request.post(`${API_URL}/api/vendor/orders/${orderId}/confirm-wire`, {
    headers: { Authorization: `Bearer ${vendorToken}` },
  });
  return orderId;
}

test.describe('Vendor — Shipment declaration UI', () => {
  let vendorToken, carrierId, productId;

  test.beforeAll(async ({ browser }) => {
    const page = await browser.newPage();
    await createActiveVendorViaApi(page, VENDOR_EMAIL, VENDOR_PASSWORD);
    vendorToken = await getVendorToken(page, VENDOR_EMAIL, VENDOR_PASSWORD);
    const carrier = await createCarrierViaApi(page, {
      name: `Carrier-Ship-${Date.now()}`, trackingUrl: 'https://track.example.com/', supportedCountries: ['FR'],
    });
    carrierId = carrier.id;
    const product = await createProductViaApi(page, vendorToken, {
      name: 'Product-Ship', priceExclTax: 40.00, quantity: 30, status: 'PUBLISHED',
    });
    productId = product.id;
    await page.close();
  });

  // ── Mark in preparation ───────────────────────────────────────────────────

  test('nominal — AWAITING_PROCESSING shows Mettre en préparation section', async ({ page, request }) => {
    const orderId = await createOrderAndConfirmWire(request, vendorToken, carrierId, productId);
    const token = await getVendorToken(page, VENDOR_EMAIL, VENDOR_PASSWORD);
    await injectVendorSession(page, VENDOR_EMAIL, token);
    await page.goto(`/vendor/orders/${orderId}`);

    await expect(page.getByText('Mettre en préparation')).toBeVisible();
    await expect(page.getByRole('button', { name: 'Marquer en préparation' })).toBeVisible();
  });

  test('nominal — mark in preparation opens confirm modal', async ({ page, request }) => {
    const orderId = await createOrderAndConfirmWire(request, vendorToken, carrierId, productId);
    const token = await getVendorToken(page, VENDOR_EMAIL, VENDOR_PASSWORD);
    await injectVendorSession(page, VENDOR_EMAIL, token);
    await page.goto(`/vendor/orders/${orderId}`);

    await page.getByRole('button', { name: 'Marquer en préparation' }).click();
    await expect(page.getByRole('dialog')).toBeVisible();
    await expect(page.getByRole('dialog').getByText('Mettre en préparation')).toBeVisible();
  });

  test('nominal — confirming preparation transitions order to En préparation', async ({ page, request }) => {
    const orderId = await createOrderAndConfirmWire(request, vendorToken, carrierId, productId);
    const token = await getVendorToken(page, VENDOR_EMAIL, VENDOR_PASSWORD);
    await injectVendorSession(page, VENDOR_EMAIL, token);
    await page.goto(`/vendor/orders/${orderId}`);

    await page.getByRole('button', { name: 'Marquer en préparation' }).click();
    await page.getByRole('dialog').getByRole('button', { name: 'Marquer en préparation' }).click();
    await expect(page.getByText('En préparation')).toBeVisible();
  });

  // ── Shipment declaration ──────────────────────────────────────────────────

  test('nominal — IN_PREPARATION shows shipment section with tracking input', async ({ page, request }) => {
    const orderId = await createOrderAndConfirmWire(request, vendorToken, carrierId, productId);
    // Advance to IN_PREPARATION
    await request.post(`${API_URL}/api/vendor/orders/${orderId}/prepare`, {
      headers: { Authorization: `Bearer ${vendorToken}` },
    });

    const token = await getVendorToken(page, VENDOR_EMAIL, VENDOR_PASSWORD);
    await injectVendorSession(page, VENDOR_EMAIL, token);
    await page.goto(`/vendor/orders/${orderId}`);

    await expect(page.getByText('Déclarer l\'expédition')).toBeVisible();
    await expect(page.getByPlaceholder('Numéro de suivi')).toBeVisible();
    await expect(page.getByRole('button', { name: 'Valider l\'expédition' })).toBeVisible();
  });

  test('nominal — ship button is disabled when tracking input is empty', async ({ page, request }) => {
    const orderId = await createOrderAndConfirmWire(request, vendorToken, carrierId, productId);
    await request.post(`${API_URL}/api/vendor/orders/${orderId}/prepare`, {
      headers: { Authorization: `Bearer ${vendorToken}` },
    });

    const token = await getVendorToken(page, VENDOR_EMAIL, VENDOR_PASSWORD);
    await injectVendorSession(page, VENDOR_EMAIL, token);
    await page.goto(`/vendor/orders/${orderId}`);

    await expect(page.getByRole('button', { name: 'Valider l\'expédition' })).toBeDisabled();
  });

  test('nominal — ship button is enabled when tracking number is filled', async ({ page, request }) => {
    const orderId = await createOrderAndConfirmWire(request, vendorToken, carrierId, productId);
    await request.post(`${API_URL}/api/vendor/orders/${orderId}/prepare`, {
      headers: { Authorization: `Bearer ${vendorToken}` },
    });

    const token = await getVendorToken(page, VENDOR_EMAIL, VENDOR_PASSWORD);
    await injectVendorSession(page, VENDOR_EMAIL, token);
    await page.goto(`/vendor/orders/${orderId}`);

    await page.getByPlaceholder('Numéro de suivi').fill('1Z999AA10123456784');
    await expect(page.getByRole('button', { name: 'Valider l\'expédition' })).toBeEnabled();
  });

  test('nominal — declaring shipment opens confirm modal', async ({ page, request }) => {
    const orderId = await createOrderAndConfirmWire(request, vendorToken, carrierId, productId);
    await request.post(`${API_URL}/api/vendor/orders/${orderId}/prepare`, {
      headers: { Authorization: `Bearer ${vendorToken}` },
    });

    const token = await getVendorToken(page, VENDOR_EMAIL, VENDOR_PASSWORD);
    await injectVendorSession(page, VENDOR_EMAIL, token);
    await page.goto(`/vendor/orders/${orderId}`);

    await page.getByPlaceholder('Numéro de suivi').fill('1Z999AA10123456784');
    await page.getByRole('button', { name: 'Valider l\'expédition' }).click();
    await expect(page.getByRole('dialog')).toBeVisible();
    await expect(page.getByRole('dialog').getByText('Déclarer l\'expédition')).toBeVisible();
  });

  test('nominal — confirming shipment transitions order to Expédiée', async ({ page, request }) => {
    const orderId = await createOrderAndConfirmWire(request, vendorToken, carrierId, productId);
    await request.post(`${API_URL}/api/vendor/orders/${orderId}/prepare`, {
      headers: { Authorization: `Bearer ${vendorToken}` },
    });

    const token = await getVendorToken(page, VENDOR_EMAIL, VENDOR_PASSWORD);
    await injectVendorSession(page, VENDOR_EMAIL, token);
    await page.goto(`/vendor/orders/${orderId}`);

    await page.getByPlaceholder('Numéro de suivi').fill('1Z999AA10123456784');
    await page.getByRole('button', { name: 'Valider l\'expédition' }).click();
    await page.getByRole('dialog').getByRole('button', { name: 'Valider l\'expédition' }).click();
    await expect(page.getByText('Statut').first()).toBeVisible();
    await expect(page.getByText(/Expédiée/).first()).toBeVisible();
  });
});
