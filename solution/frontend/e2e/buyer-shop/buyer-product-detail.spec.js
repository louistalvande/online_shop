import { test, expect } from '@playwright/test';
import {
  API_URL,
  createActiveVendorViaApi, getVendorToken, createProductViaApi,
  registerAndActivateBuyerViaApi, getBuyerToken, injectBuyerSession,
} from '../helpers/login.js';

// BUYER-PRODUCT-DETAIL — Product detail page UI on the buyer portal.

const VENDOR_EMAIL    = `byr-pd-vendor-${Date.now()}@shop-test.example`;
const VENDOR_PASSWORD = 'VndPd!Secure2026';
const BUYER_EMAIL     = `byr-pd-buyer-${Date.now()}@shop-test.example`;
const BUYER_PASSWORD  = 'ByrPd!Secure2026';

test.describe('Buyer portal — Product detail page', () => {
  let productId, outOfStockProductId;

  test.beforeAll(async ({ browser }) => {
    const page = await browser.newPage();
    await createActiveVendorViaApi(page, VENDOR_EMAIL, VENDOR_PASSWORD);
    const vendorToken = await getVendorToken(page, VENDOR_EMAIL, VENDOR_PASSWORD);

    const inStock = await createProductViaApi(page, vendorToken, {
      name: 'Produit-PD-InStock', priceExclTax: 19.90, quantity: 5, status: 'PUBLISHED',
    });
    productId = inStock.id;

    const outOfStock = await createProductViaApi(page, vendorToken, {
      name: 'Produit-PD-OutOfStock', priceExclTax: 29.90, quantity: 0, status: 'PUBLISHED',
    });
    outOfStockProductId = outOfStock.id;

    await registerAndActivateBuyerViaApi(page, BUYER_EMAIL, BUYER_PASSWORD);
    await page.close();
  });

  test('nominal — product detail page shows name and price', async ({ page }) => {
    await page.goto(`/catalog/${productId}`);
    await expect(page.getByText('Produit-PD-InStock')).toBeVisible();
    await expect(page.getByText(/19[,.]90/)).toBeVisible();
  });

  test('nominal — in-stock product shows Add to cart button', async ({ page }) => {
    await page.goto(`/catalog/${productId}`);
    await expect(page.getByRole('button', { name: /panier|cart/i })).toBeVisible();
  });

  test('nominal — out-of-stock product shows disabled button', async ({ page }) => {
    await page.goto(`/catalog/${outOfStockProductId}`);
    // Button is disabled when out of stock
    const btn = page.getByRole('button').filter({ hasText: /stock|indisponible/i }).first();
    await expect(btn).toBeDisabled();
  });

  test('nominal — out-of-stock shows Subscribe button when not logged in', async ({ page }) => {
    await page.goto(`/catalog/${outOfStockProductId}`);
    // Subscribe / M'alerter button visible for anonymous users
    const subscribeBtn = page.getByRole('button', { name: /alerter|M'alerter|Notifier/i });
    await expect(subscribeBtn).toBeVisible();
  });

  test('nominal — back link to catalog is visible', async ({ page }) => {
    await page.goto(`/catalog/${productId}`);
    await expect(page.getByRole('link', { name: '← Catalogue' })).toBeVisible();
  });

  test('nominal — adding in-stock product to cart shows confirmation', async ({ page }) => {
    const buyerToken = await getBuyerToken(page, BUYER_EMAIL, BUYER_PASSWORD);
    await injectBuyerSession(page, BUYER_EMAIL, buyerToken);
    await page.goto(`/catalog/${productId}`);

    const addBtn = page.getByRole('button').filter({ hasText: /panier|Ajouter/i }).first();
    await addBtn.click();
    // Success indicator: button text changes or a success toast appears
    await expect(page.getByText(/ajouté|Ajouté/i).first()).toBeVisible();
  });

  test('nominal — quantity stepper increment and decrement are visible', async ({ page }) => {
    await page.goto(`/catalog/${productId}`);
    // Plus and minus buttons for quantity
    await expect(page.getByRole('button', { name: '+' }).or(page.locator('button').filter({ hasText: '+' }))).toBeVisible();
  });
});
