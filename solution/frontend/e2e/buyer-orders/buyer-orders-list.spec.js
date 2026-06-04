import { test, expect } from '@playwright/test';
import {
  API_URL,
  createActiveVendorViaApi, getVendorToken, createProductViaApi,
  registerAndActivateBuyerViaApi, getBuyerToken, injectBuyerSession,
  createCarrierViaApi, createAddressViaApi,
} from '../helpers/login.js';

// BUYER-ORDERS-LIST — Buyer orders list page UI.

const VENDOR_EMAIL    = `byr-olist-vendor-${Date.now()}@shop-test.example`;
const VENDOR_PASSWORD = 'VndOList!Secure2026';
const BUYER_EMAIL     = `byr-olist-buyer-${Date.now()}@shop-test.example`;
const BUYER_PASSWORD  = 'ByrOList!Secure2026';

test.describe('Buyer portal — Orders list page', () => {
  let buyerToken, orderId;

  test.beforeAll(async ({ browser }) => {
    const page = await browser.newPage();
    await createActiveVendorViaApi(page, VENDOR_EMAIL, VENDOR_PASSWORD);
    const vendorToken = await getVendorToken(page, VENDOR_EMAIL, VENDOR_PASSWORD);
    const product = await createProductViaApi(page, vendorToken, {
      name: 'Produit-OList', priceExclTax: 20.00, quantity: 5, status: 'PUBLISHED',
    });
    const carrier = await createCarrierViaApi(page, {
      name: `Carrier-OList-${Date.now()}`, trackingUrl: 'https://track.example.com/', supportedCountries: ['FR'],
    });

    await registerAndActivateBuyerViaApi(page, BUYER_EMAIL, BUYER_PASSWORD);
    buyerToken = await getBuyerToken(page, BUYER_EMAIL, BUYER_PASSWORD);
    const address = await createAddressViaApi(page, buyerToken, {
      label: 'Home', recipientName: 'Test', addressLine: '1 rue Test',
      city: 'Paris', postalCode: '75001', countryCode: 'FR', makeDefault: true,
    });

    await page.request.post(`${API_URL}/api/cart/items`, {
      headers: { Authorization: `Bearer ${buyerToken}` },
      data: { productId: product.id, quantity: 1 },
    });
    const res = await page.request.post(`${API_URL}/api/orders`, {
      headers: { Authorization: `Bearer ${buyerToken}` },
      data: { addressId: address.id, carrierId: carrier.id, paymentMethod: 'WIRE_TRANSFER' },
    });
    const body = await res.json();
    orderId = body.orderId;
    await page.close();
  });

  test.beforeEach(async ({ page }) => {
    await injectBuyerSession(page, BUYER_EMAIL, buyerToken);
    await page.goto('/my-orders');
    await expect(page.getByRole('heading', { name: 'Mes commandes' })).toBeVisible();
  });

  test('nominal — orders table shows column headers', async ({ page }) => {
    await expect(page.getByText('Référence')).toBeVisible();
    await expect(page.getByText('Statut')).toBeVisible();
    await expect(page.getByText('Total TTC')).toBeVisible();
    await expect(page.getByText('Date')).toBeVisible();
  });

  test('nominal — at least one order row is present with a detail link', async ({ page }) => {
    await expect(page.getByRole('link', { name: 'Voir le détail' }).first()).toBeVisible();
  });

  test('nominal — order shows wire transfer status', async ({ page }) => {
    await expect(page.getByText('En attente de virement')).toBeVisible();
  });

  test('nominal — clicking Voir le détail navigates to order detail', async ({ page }) => {
    await page.getByRole('link', { name: 'Voir le détail' }).first().click();
    await expect(page.getByRole('heading', { name: /Commande/ })).toBeVisible();
  });

  test('nominal — orders page accessible via Mon compte → Mes commandes', async ({ page }) => {
    await page.goto('/');
    await page.locator('.user-menu-trigger').click();
    await page.getByText('Mes commandes').click();
    await expect(page.getByRole('heading', { name: 'Mes commandes' })).toBeVisible();
  });
});
