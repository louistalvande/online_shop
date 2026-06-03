import { test, expect } from '@playwright/test';
import {
  API_URL,
  createActiveVendorViaApi, getVendorToken, createProductViaApi,
  registerAndActivateBuyerViaApi, getBuyerToken, injectBuyerSession,
  createCarrierViaApi, createAddressViaApi,
} from '../helpers/login.js';

// BUYER-ORDER-DETAIL — Buyer order detail page UI: status, lines, cancel form.
// Each test refreshes tokens to avoid 30-min JWT expiry issues across the full suite.

const VENDOR_EMAIL    = `byr-odet-vendor-${Date.now()}@shop-test.example`;
const VENDOR_PASSWORD = 'VndODet!Secure2026';
const BUYER_EMAIL     = `byr-odet-buyer-${Date.now()}@shop-test.example`;
const BUYER_PASSWORD  = 'ByrODet!Secure2026';

test.describe('Buyer portal — Order detail page', () => {
  let productId, carrierId, addressId;

  test.beforeAll(async ({ browser }) => {
    const page = await browser.newPage();
    await createActiveVendorViaApi(page, VENDOR_EMAIL, VENDOR_PASSWORD);
    const vendorToken = await getVendorToken(page, VENDOR_EMAIL, VENDOR_PASSWORD);
    const product = await createProductViaApi(page, vendorToken, {
      name: 'Produit-ODet', priceExclTax: 30.00, quantity: 50, status: 'PUBLISHED',
    });
    productId = product.id;
    const carrier = await createCarrierViaApi(page, {
      name: `Carrier-ODet-${Date.now()}`, trackingUrl: 'https://track.example.com/', supportedCountries: ['FR'],
    });
    carrierId = carrier.id;
    await registerAndActivateBuyerViaApi(page, BUYER_EMAIL, BUYER_PASSWORD);
    const buyerToken = await getBuyerToken(page, BUYER_EMAIL, BUYER_PASSWORD);
    const address = await createAddressViaApi(page, buyerToken, {
      label: 'Home', recipientName: 'Test', addressLine: '1 rue Test',
      city: 'Paris', postalCode: '75001', countryCode: 'FR', makeDefault: true,
    });
    addressId = address.id;
    await page.close();
  });

  // Helper that always uses fresh tokens to avoid 30-min expiry across the full suite.
  async function placeWireOrder(page, request) {
    const freshBuyerToken = await getBuyerToken(page, BUYER_EMAIL, BUYER_PASSWORD);
    await request.post(`${API_URL}/api/cart/items`, {
      headers: { Authorization: `Bearer ${freshBuyerToken}` },
      data: { productId, quantity: 1 },
    });
    const res = await request.post(`${API_URL}/api/orders`, {
      headers: { Authorization: `Bearer ${freshBuyerToken}` },
      data: { addressId, carrierId, paymentMethod: 'WIRE_TRANSFER' },
    });
    const body = await res.json();
    return { orderId: body.orderId, buyerToken: freshBuyerToken };
  }

  // ── Page structure ────────────────────────────────────────────────────────

  test('nominal — order detail shows heading with order number', async ({ page, request }) => {
    const { orderId, buyerToken } = await placeWireOrder(page, request);
    await injectBuyerSession(page, BUYER_EMAIL, buyerToken);
    await page.goto(`/my-orders/${orderId}`);
    await expect(page.getByRole('heading', { name: /Commande/ })).toBeVisible();
  });

  test('nominal — delivery address section is shown', async ({ page, request }) => {
    const { orderId, buyerToken } = await placeWireOrder(page, request);
    await injectBuyerSession(page, BUYER_EMAIL, buyerToken);
    await page.goto(`/my-orders/${orderId}`);
    await expect(page.getByText('Adresse de livraison')).toBeVisible();
  });

  test('nominal — order lines section is shown', async ({ page, request }) => {
    const { orderId, buyerToken } = await placeWireOrder(page, request);
    await injectBuyerSession(page, BUYER_EMAIL, buyerToken);
    await page.goto(`/my-orders/${orderId}`);
    await expect(page.getByText('Articles commandés')).toBeVisible();
    await expect(page.getByText('Produit-ODet')).toBeVisible();
  });

  test('nominal — back to orders link is visible', async ({ page, request }) => {
    const { orderId, buyerToken } = await placeWireOrder(page, request);
    await injectBuyerSession(page, BUYER_EMAIL, buyerToken);
    await page.goto(`/my-orders/${orderId}`);
    await expect(page.getByRole('link', { name: /commandes/i })).toBeVisible();
  });

  // ── Cancel form (AWAITING_PROCESSING via wire confirm) ────────────────────

  test('nominal — AWAITING_PROCESSING order shows cancel section', async ({ page, request }) => {
    const { orderId, buyerToken } = await placeWireOrder(page, request);
    const vendorToken = await getVendorToken(page, VENDOR_EMAIL, VENDOR_PASSWORD);
    await request.post(`${API_URL}/api/vendor/orders/${orderId}/confirm-wire`, {
      headers: { Authorization: `Bearer ${vendorToken}` },
    });
    await injectBuyerSession(page, BUYER_EMAIL, buyerToken);
    await page.goto(`/my-orders/${orderId}`);
    await expect(page.getByRole('heading', { name: 'Annuler la commande' })).toBeVisible();
  });

  test('nominal — wire order cancel form shows IBAN input', async ({ page, request }) => {
    const { orderId, buyerToken } = await placeWireOrder(page, request);
    const vendorToken = await getVendorToken(page, VENDOR_EMAIL, VENDOR_PASSWORD);
    await request.post(`${API_URL}/api/vendor/orders/${orderId}/confirm-wire`, {
      headers: { Authorization: `Bearer ${vendorToken}` },
    });
    await injectBuyerSession(page, BUYER_EMAIL, buyerToken);
    await page.goto(`/my-orders/${orderId}`);
    await expect(page.getByPlaceholder('FR76 3000 6000 0112 3456 7890 189')).toBeVisible();
  });
});
