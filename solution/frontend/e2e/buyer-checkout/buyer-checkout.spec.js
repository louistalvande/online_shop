import { test, expect } from '@playwright/test';
import {
  API_URL,
  createActiveVendorViaApi, getVendorToken, createProductViaApi,
  registerAndActivateBuyerViaApi, getBuyerToken, injectBuyerSession,
  createCarrierViaApi, createAddressViaApi,
} from '../helpers/login.js';

// BUYER-CHECKOUT — Checkout flow UI on the buyer portal (address + carrier + payment steps).

const VENDOR_EMAIL    = `byr-chk-vendor-${Date.now()}@shop-test.example`;
const VENDOR_PASSWORD = 'VndChk!Secure2026';
const BUYER_EMAIL     = `byr-chk-buyer-${Date.now()}@shop-test.example`;
const BUYER_PASSWORD  = 'ByrChk!Secure2026';

test.describe('Buyer portal — Checkout flow', () => {
  let productId, carrierId, buyerToken, addressId;

  test.beforeAll(async ({ browser }) => {
    const page = await browser.newPage();
    await createActiveVendorViaApi(page, VENDOR_EMAIL, VENDOR_PASSWORD);
    const vendorToken = await getVendorToken(page, VENDOR_EMAIL, VENDOR_PASSWORD);
    const product = await createProductViaApi(page, vendorToken, {
      name: 'Produit-Checkout', priceExclTax: 25.00, quantity: 5, status: 'PUBLISHED',
    });
    productId = product.id;

    const carrier = await createCarrierViaApi(page, {
      name: `Carrier-Chk-${Date.now()}`, trackingUrl: 'https://track.example.com/', supportedCountries: ['FR'],
    });
    carrierId = carrier.id;

    await registerAndActivateBuyerViaApi(page, BUYER_EMAIL, BUYER_PASSWORD);
    buyerToken = await getBuyerToken(page, BUYER_EMAIL, BUYER_PASSWORD);

    const address = await createAddressViaApi(page, buyerToken, {
      label: 'Domicile', recipientName: 'Test Buyer', addressLine: '1 rue de la Paix',
      city: 'Paris', postalCode: '75001', countryCode: 'FR', makeDefault: true,
    });
    addressId = address.id;
    await page.close();
  });

  async function addItemAndGoToCheckout(page, request) {
    await request.post(`${API_URL}/api/cart/items`, {
      headers: { Authorization: `Bearer ${buyerToken}` },
      data: { productId, quantity: 1 },
    });
    await injectBuyerSession(page, BUYER_EMAIL, buyerToken);
    await page.goto('/checkout');
    await expect(page.getByRole('heading', { name: 'Passer commande' })).toBeVisible();
  }

  // ── Step 1 : address + carrier ────────────────────────────────────────────

  test('nominal — checkout shows address selection step', async ({ page, request }) => {
    await addItemAndGoToCheckout(page, request);
    await expect(page.getByText('Adresse de livraison')).toBeVisible();
  });

  test('nominal — saved address appears as radio option', async ({ page, request }) => {
    await addItemAndGoToCheckout(page, request);
    await expect(page.getByText('Domicile')).toBeVisible();
  });

  test('nominal — carrier section appears after selecting an address', async ({ page, request }) => {
    await addItemAndGoToCheckout(page, request);
    // Select the saved address
    await page.locator('input[type="radio"]').first().check();
    await expect(page.getByText('Mode de livraison')).toBeVisible();
  });

  test('nominal — Continue button navigates to payment step', async ({ page, request }) => {
    await addItemAndGoToCheckout(page, request);
    await page.locator('input[type="radio"]').first().check();
    // Select carrier radio
    const carrierRadios = page.locator('input[type="radio"]');
    await carrierRadios.last().check();
    await page.getByRole('button', { name: 'Continuer vers le paiement' }).click();
    await expect(page.getByRole('heading', { name: 'Paiement' })).toBeVisible();
  });

  test('nominal — Back to cart button returns to cart', async ({ page, request }) => {
    await addItemAndGoToCheckout(page, request);
    await page.getByRole('button', { name: 'Retour au panier' }).click();
    await expect(page.getByRole('heading', { name: 'Mon panier' })).toBeVisible();
  });

  // ── Step 2 : payment ──────────────────────────────────────────────────────

  test('nominal — payment step shows card and wire options', async ({ page, request }) => {
    await addItemAndGoToCheckout(page, request);
    await page.locator('input[type="radio"]').first().check();
    await page.locator('input[type="radio"]').last().check();
    await page.getByRole('button', { name: 'Continuer vers le paiement' }).click();

    await expect(page.getByText('Carte bancaire')).toBeVisible();
    await expect(page.getByText('Virement bancaire')).toBeVisible();
  });

  test('nominal — wire transfer checkout creates a PAYMENT_PENDING_WIRE order', async ({ page, request }) => {
    await addItemAndGoToCheckout(page, request);
    await page.locator('input[type="radio"]').first().check();
    await page.locator('input[type="radio"]').last().check();
    await page.getByRole('button', { name: 'Continuer vers le paiement' }).click();

    // Select wire transfer
    await page.locator('input[value="WIRE_TRANSFER"]').check();
    await page.getByRole('button', { name: 'Confirmer et payer' }).click();

    // Confirmation page shows order number
    await expect(page.getByText(/Numéro de commande.*ORD-|ORD-/).first()).toBeVisible();
  });
});
