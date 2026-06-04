import { test, expect } from '@playwright/test';
import {
  API_URL,
  createActiveVendorViaApi, getVendorToken, createProductViaApi,
  registerAndActivateBuyerViaApi, getBuyerToken, injectBuyerSession,
} from '../helpers/login.js';

// BUYER-CART-UI — Cart page UI on the buyer portal.

const VENDOR_EMAIL    = `byr-cart-vendor-${Date.now()}@shop-test.example`;
const VENDOR_PASSWORD = 'VndCart!Secure2026';
const BUYER_EMAIL     = `byr-cart-buyer-${Date.now()}@shop-test.example`;
const BUYER_PASSWORD  = 'ByrCart!Secure2026';

test.describe('Buyer portal — Cart page', () => {
  let productId, buyerToken;

  test.beforeAll(async ({ browser }) => {
    const page = await browser.newPage();
    await createActiveVendorViaApi(page, VENDOR_EMAIL, VENDOR_PASSWORD);
    const vendorToken = await getVendorToken(page, VENDOR_EMAIL, VENDOR_PASSWORD);
    const product = await createProductViaApi(page, vendorToken, {
      name: 'Produit-Cart', priceExclTax: 15.00, quantity: 10, status: 'PUBLISHED',
    });
    productId = product.id;
    await registerAndActivateBuyerViaApi(page, BUYER_EMAIL, BUYER_PASSWORD);
    buyerToken = await getBuyerToken(page, BUYER_EMAIL, BUYER_PASSWORD);
    await page.close();
  });

  test('nominal — empty cart shows empty state message', async ({ page }) => {
    await injectBuyerSession(page, BUYER_EMAIL, buyerToken);
    await page.goto('/cart');
    await expect(page.getByText('Votre panier est vide.')).toBeVisible();
  });

  test('nominal — unauthenticated user sees login prompt on /cart', async ({ page }) => {
    await page.goto('/cart');
    await expect(page.getByText('Connectez-vous pour accéder à votre panier.')).toBeVisible();
  });

  test('nominal — cart page title is visible', async ({ page }) => {
    await injectBuyerSession(page, BUYER_EMAIL, buyerToken);
    await page.goto('/cart');
    await expect(page.getByRole('heading', { name: 'Mon panier' })).toBeVisible();
  });

  test('nominal — cart with item shows product name, price, quantity and remove button', async ({ page, request }) => {
    // Add product to cart via API
    await request.post(`${API_URL}/api/cart/items`, {
      headers: { Authorization: `Bearer ${buyerToken}` },
      data: { productId, quantity: 1 },
    });

    await injectBuyerSession(page, BUYER_EMAIL, buyerToken);
    await page.goto('/cart');

    await expect(page.getByText('Produit-Cart')).toBeVisible();
    await expect(page.getByRole('button', { name: 'Retirer' })).toBeVisible();
    await expect(page.getByText('Total TTC')).toBeVisible();
  });

  test('nominal — cart shows Passer commande button when items present', async ({ page, request }) => {
    await request.post(`${API_URL}/api/cart/items`, {
      headers: { Authorization: `Bearer ${buyerToken}` },
      data: { productId, quantity: 1 },
    });

    await injectBuyerSession(page, BUYER_EMAIL, buyerToken);
    await page.goto('/cart');
    await expect(page.getByRole('button', { name: 'Passer commande' }).or(
      page.getByRole('link', { name: 'Passer commande' })
    )).toBeVisible();
  });

  test('nominal — removing item empties the cart', async ({ page, request }) => {
    await request.post(`${API_URL}/api/cart/items`, {
      headers: { Authorization: `Bearer ${buyerToken}` },
      data: { productId, quantity: 1 },
    });

    await injectBuyerSession(page, BUYER_EMAIL, buyerToken);
    await page.goto('/cart');
    await expect(page.getByText('Produit-Cart')).toBeVisible();

    await page.getByRole('button', { name: 'Retirer' }).click();
    await expect(page.getByText('Votre panier est vide.')).toBeVisible();
  });

  test('nominal — column headers are displayed', async ({ page, request }) => {
    await request.post(`${API_URL}/api/cart/items`, {
      headers: { Authorization: `Bearer ${buyerToken}` },
      data: { productId, quantity: 1 },
    });

    await injectBuyerSession(page, BUYER_EMAIL, buyerToken);
    await page.goto('/cart');
    await expect(page.getByRole('columnheader', { name: 'Produit' })).toBeVisible();
    await expect(page.getByRole('columnheader', { name: 'Prix TTC' })).toBeVisible();
    await expect(page.getByRole('columnheader', { name: 'Quantité' })).toBeVisible();
  });
});
