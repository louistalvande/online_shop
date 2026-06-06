import { test, expect } from '@playwright/test';
import {
  API_URL,
  createActiveVendorViaApi, getVendorToken, injectVendorSession,
  registerAndActivateBuyerViaApi, getBuyerToken,
  createCarrierViaApi, createProductViaApi, createAddressViaApi,
} from '../helpers/login.js';

// VND-DASHBOARD — Vendor back-office dashboard UI.

const VENDOR_EMAIL    = `vnd-dash-${Date.now()}@shop-test.example`;
const VENDOR_PASSWORD = 'VndDash!Secure2026';
const BUYER_PASSWORD  = 'ByrDash!Secure2026';

test.describe('Vendor dashboard', () => {
  let vendorToken;

  test.beforeAll(async ({ browser }) => {
    const page = await browser.newPage();
    await createActiveVendorViaApi(page, VENDOR_EMAIL, VENDOR_PASSWORD);
    vendorToken = await getVendorToken(page, VENDOR_EMAIL, VENDOR_PASSWORD);

    // Seed an order so the recent orders table and "Voir tout" are shown
    const carrier = await createCarrierViaApi(page, {
      name: `Carrier-Dash-${Date.now()}`, trackingUrl: 'https://track.example.com/', supportedCountries: ['FR'],
    });
    const product = await createProductViaApi(page, vendorToken, {
      name: 'Product-Dash', priceExclTax: 25.00, quantity: 5, status: 'PUBLISHED',
    });
    const buyerEmail = `dash-buyer-${Date.now()}@shop-test.example`;
    await registerAndActivateBuyerViaApi(page, buyerEmail, BUYER_PASSWORD);
    const buyerToken = await getBuyerToken(page, buyerEmail, BUYER_PASSWORD);
    const address = await createAddressViaApi(page, buyerToken, {
      label: 'Home', recipientName: 'Test', addressLine: '1 rue Test',
      city: 'Paris', postalCode: '75001', countryCode: 'FR', makeDefault: true,
    });
    await page.request.post(`${API_URL}/api/cart/items`, {
      headers: { Authorization: `Bearer ${buyerToken}` },
      data: { productId: product.id, quantity: 1 },
    });
    await page.request.post(`${API_URL}/api/orders`, {
      headers: { Authorization: `Bearer ${buyerToken}` },
      data: { addressId: address.id, carrierId: carrier.id, paymentMethod: 'WIRE_TRANSFER' },
    });
    await page.close();
  });

  test.beforeEach(async ({ page }) => {
    const token = await getVendorToken(page, VENDOR_EMAIL, VENDOR_PASSWORD);
    await injectVendorSession(page, VENDOR_EMAIL, token);
    await page.goto('/vendor/');
    await expect(page.getByRole('heading', { name: 'Tableau de bord' })).toBeVisible();
  });

  test('nominal — stats cards are displayed', async ({ page }) => {
    await expect(page.getByText('Commandes en attente')).toBeVisible();
    await expect(page.getByText('Revenus du mois')).toBeVisible();
  });

  test('nominal — recent orders section is displayed', async ({ page }) => {
    await expect(page.getByText('Commandes récentes')).toBeVisible();
  });

  test('nominal — navigation links are all present', async ({ page }) => {
    await expect(page.getByRole('link', { name: 'Catalogue' })).toBeVisible();
    await expect(page.getByRole('link', { name: 'Commandes' })).toBeVisible();
    await expect(page.getByRole('link', { name: 'Rapports' })).toBeVisible();
    await expect(page.getByRole('link', { name: 'Identité visuelle' })).toBeVisible();
  });

  test('nominal — Voir tout button leads to orders list', async ({ page }) => {
    // "Voir tout" is a button or link — visible only when there are recent orders
    const viewAll = page.getByRole('link', { name: 'Voir tout' })
      .or(page.getByRole('button', { name: 'Voir tout' }));
    await expect(viewAll).toBeVisible();
    await viewAll.click();
    await expect(page.getByRole('heading', { name: 'Mes commandes' })).toBeVisible();
  });

  test('nominal — recent orders table row shows a Détails button', async ({ page }) => {
    // Dashboard uses a <Button>, not a link, for row actions
    await expect(page.getByRole('button', { name: 'Détails' }).first()).toBeVisible();
  });

  test('nominal — clicking Détails in recent orders navigates to order detail', async ({ page }) => {
    await page.getByRole('button', { name: 'Détails' }).first().click();
    await expect(page.getByRole('heading', { name: /Commande/ })).toBeVisible();
  });
});
