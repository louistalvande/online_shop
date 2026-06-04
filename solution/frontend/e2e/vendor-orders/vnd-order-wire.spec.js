import { test, expect } from '@playwright/test';
import {
  API_URL,
  createActiveVendorViaApi, getVendorToken, injectVendorSession,
  registerAndActivateBuyerViaApi, getBuyerToken,
  createCarrierViaApi, createProductViaApi, createAddressViaApi,
} from '../helpers/login.js';

// VND-ORDER-WIRE — Vendor confirms or rejects wire transfer payment (US-VND-02 IHM).

const VENDOR_EMAIL    = `vnd-wire-${Date.now()}@shop-test.example`;
const VENDOR_PASSWORD = 'VndWire!Secure2026';
const BUYER_PASSWORD  = 'ByrWire!Secure2026';

async function createWireOrder(request, vendorToken, carrierId, productId) {
  const buyerEmail = `wire-buyer-${Date.now()}@shop-test.example`;
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
  const res = await request.post(`${API_URL}/api/orders`, {
    headers: { Authorization: `Bearer ${buyerToken}` },
    data: { addressId: address.id, carrierId, paymentMethod: 'WIRE_TRANSFER' },
  });
  const body = await res.json();
  return body.orderId;
}

test.describe('Vendor — Wire transfer confirmation UI', () => {
  let vendorToken, carrierId, productId;

  test.beforeAll(async ({ browser }) => {
    const page = await browser.newPage();
    await createActiveVendorViaApi(page, VENDOR_EMAIL, VENDOR_PASSWORD);
    vendorToken = await getVendorToken(page, VENDOR_EMAIL, VENDOR_PASSWORD);
    const carrier = await createCarrierViaApi(page, {
      name: `Carrier-Wire-${Date.now()}`, trackingUrl: 'https://track.example.com/', supportedCountries: ['FR'],
    });
    carrierId = carrier.id;
    const product = await createProductViaApi(page, vendorToken, {
      name: 'Product-Wire', priceExclTax: 30.00, quantity: 20, status: 'PUBLISHED',
    });
    productId = product.id;
    await page.close();
  });

  test('nominal — wire section and both action buttons are visible on PAYMENT_PENDING_WIRE order', async ({ page, request }) => {
    const orderId = await createWireOrder(request, vendorToken, carrierId, productId);
    const token = await getVendorToken(page, VENDOR_EMAIL, VENDOR_PASSWORD);
    await injectVendorSession(page, VENDOR_EMAIL, token);
    await page.goto(`/vendor/orders/${orderId}`);

    await expect(page.getByText('Confirmation du virement')).toBeVisible();
    await expect(page.getByRole('button', { name: 'Confirmer le virement' })).toBeVisible();
    await expect(page.getByRole('button', { name: 'Rejeter le virement' })).toBeVisible();
  });

  test('nominal — confirm wire opens confirmation modal', async ({ page, request }) => {
    const orderId = await createWireOrder(request, vendorToken, carrierId, productId);
    const token = await getVendorToken(page, VENDOR_EMAIL, VENDOR_PASSWORD);
    await injectVendorSession(page, VENDOR_EMAIL, token);
    await page.goto(`/vendor/orders/${orderId}`);

    await page.getByRole('button', { name: 'Confirmer le virement' }).click();
    await expect(page.getByRole('dialog')).toBeVisible();
    await expect(page.getByRole('dialog').getByRole('heading', { name: 'Confirmer le virement' })).toBeVisible();
  });

  test('nominal — confirm wire modal cancel dismisses without action', async ({ page, request }) => {
    const orderId = await createWireOrder(request, vendorToken, carrierId, productId);
    const token = await getVendorToken(page, VENDOR_EMAIL, VENDOR_PASSWORD);
    await injectVendorSession(page, VENDOR_EMAIL, token);
    await page.goto(`/vendor/orders/${orderId}`);

    await page.getByRole('button', { name: 'Confirmer le virement' }).click();
    await page.getByRole('dialog').getByRole('button', { name: 'Annuler' }).click();
    await expect(page.getByRole('dialog')).not.toBeVisible();
    // Wire section still visible — order not confirmed
    await expect(page.getByRole('button', { name: 'Confirmer le virement' })).toBeVisible();
  });

  test('nominal — confirming wire transitions order to En attente de traitement', async ({ page, request }) => {
    const orderId = await createWireOrder(request, vendorToken, carrierId, productId);
    const token = await getVendorToken(page, VENDOR_EMAIL, VENDOR_PASSWORD);
    await injectVendorSession(page, VENDOR_EMAIL, token);
    await page.goto(`/vendor/orders/${orderId}`);

    await page.getByRole('button', { name: 'Confirmer le virement' }).click();
    await page.getByRole('dialog').getByRole('button', { name: 'Confirmer le virement' }).click();
    await expect(page.getByText('En attente de traitement')).toBeVisible();
  });

  test('nominal — reject wire opens confirmation modal', async ({ page, request }) => {
    const orderId = await createWireOrder(request, vendorToken, carrierId, productId);
    const token = await getVendorToken(page, VENDOR_EMAIL, VENDOR_PASSWORD);
    await injectVendorSession(page, VENDOR_EMAIL, token);
    await page.goto(`/vendor/orders/${orderId}`);

    await page.getByRole('button', { name: 'Rejeter le virement' }).click();
    await expect(page.getByRole('dialog')).toBeVisible();
    await expect(page.getByRole('dialog').getByRole('heading', { name: 'Rejeter le virement' })).toBeVisible();
  });

  test('nominal — rejecting wire transitions order to Annulée', async ({ page, request }) => {
    const orderId = await createWireOrder(request, vendorToken, carrierId, productId);
    const token = await getVendorToken(page, VENDOR_EMAIL, VENDOR_PASSWORD);
    await injectVendorSession(page, VENDOR_EMAIL, token);
    await page.goto(`/vendor/orders/${orderId}`);

    await page.getByRole('button', { name: 'Rejeter le virement' }).click();
    await page.getByRole('dialog').getByRole('button', { name: 'Rejeter le virement' }).click();
    await expect(page.getByText('Annulée')).toBeVisible();
  });
});
