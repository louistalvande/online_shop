import { test, expect } from '@playwright/test';
import {
  API_URL,
  createActiveVendorViaApi, getVendorToken, injectVendorSession,
  registerAndActivateBuyerViaApi, getBuyerToken,
  createCarrierViaApi, createProductViaApi, createAddressViaApi,
} from '../helpers/login.js';

// VND-ORDERS-LIST — Vendor orders list page UI.

const VENDOR_EMAIL    = `vnd-list-${Date.now()}@shop-test.example`;
const VENDOR_PASSWORD = 'VndList!Secure2026';
const BUYER_PASSWORD  = 'ByrList!Secure2026';

test.describe('Vendor orders list', () => {
  let vendorToken, carrierId, productId;

  test.beforeAll(async ({ browser }) => {
    const page = await browser.newPage();
    await createActiveVendorViaApi(page, VENDOR_EMAIL, VENDOR_PASSWORD);
    vendorToken = await getVendorToken(page, VENDOR_EMAIL, VENDOR_PASSWORD);

    const carrier = await createCarrierViaApi(page, {
      name: `Carrier-List-${Date.now()}`,
      trackingUrl: 'https://track.example.com/',
      supportedCountries: ['FR'],
    });
    carrierId = carrier.id;

    const product = await createProductViaApi(page, vendorToken, {
      name: 'Product-List', priceExclTax: 20.00, quantity: 10, status: 'PUBLISHED',
    });
    productId = product.id;

    // Place one wire order so the list is not empty
    const buyerEmail = `vnd-list-buyer-${Date.now()}@shop-test.example`;
    await registerAndActivateBuyerViaApi(page, buyerEmail, BUYER_PASSWORD);
    const buyerToken = await getBuyerToken(page, buyerEmail, BUYER_PASSWORD);
    const address = await createAddressViaApi(page, buyerToken, {
      label: 'Home', recipientName: 'Test', addressLine: '1 rue Test',
      city: 'Paris', postalCode: '75001', countryCode: 'FR', makeDefault: true,
    });
    await page.request.post(`${API_URL}/api/cart/items`, {
      headers: { Authorization: `Bearer ${buyerToken}` },
      data: { productId, quantity: 1 },
    });
    await page.request.post(`${API_URL}/api/orders`, {
      headers: { Authorization: `Bearer ${buyerToken}` },
      data: { addressId: address.id, carrierId, paymentMethod: 'WIRE_TRANSFER' },
    });
    await page.close();
  });

  test.beforeEach(async ({ page }) => {
    const token = await getVendorToken(page, VENDOR_EMAIL, VENDOR_PASSWORD);
    await injectVendorSession(page, VENDOR_EMAIL, token);
    await page.goto('/vendor/orders');
    await expect(page.getByRole('heading', { name: 'Mes commandes' })).toBeVisible();
  });

  test('nominal — orders list table shows column headers', async ({ page }) => {
    await expect(page.getByText('Référence')).toBeVisible();
    await expect(page.getByText('Statut')).toBeVisible();
    await expect(page.getByText('Total')).toBeVisible();
    await expect(page.getByText('Mode de paiement')).toBeVisible();
    await expect(page.getByText('Date')).toBeVisible();
  });

  test('nominal — at least one order row with a Détails link is present', async ({ page }) => {
    await expect(page.getByRole('link', { name: 'Détails' }).first()).toBeVisible();
  });

  test('nominal — clicking Détails navigates to order detail page', async ({ page }) => {
    await page.getByRole('link', { name: 'Détails' }).first().click();
    // The order detail shows a heading like "Commande ORD-XXXX"
    await expect(page.getByRole('heading', { name: /Commande/ })).toBeVisible();
  });

  test('nominal — orders page is accessible via Commandes nav link', async ({ page }) => {
    await page.goto('/vendor/');
    await page.getByRole('link', { name: 'Commandes' }).click();
    await expect(page.getByRole('heading', { name: 'Mes commandes' })).toBeVisible();
  });
});
