import { test, expect } from '@playwright/test';
import { createActiveVendorViaApi, getVendorToken, injectVendorSession, API_URL } from '../helpers/login.js';

// UCSA-16 — Vendor personalises the shop visual identity: logo, hero banner, accent colour.

const VENDOR_EMAIL    = `vendor-vi-${Date.now()}@shop.test`;
const VENDOR_PASSWORD = 'VndVI!Secure-2026';

// Minimal 1×1 white PNG — valid image accepted by the backend.
const PNG_1X1 = Buffer.from(
  'iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAIAAACQd1PeAAAADklEQVQI12P4z8BQDwADhQGAWjR9awAAAABJRU5ErkJggg==',
  'base64'
);

const LOGO_FILE   = { name: 'logo.png',   mimeType: 'image/png', buffer: PNG_1X1 };
const BANNER_FILE = { name: 'banner.png', mimeType: 'image/png', buffer: PNG_1X1 };
const TEXT_FILE   = { name: 'bad.txt',    mimeType: 'text/plain', buffer: Buffer.from('not an image') };

async function navigateToVisualIdentity(page) {
  await page.goto('/vendor/');
  await page.getByText('Identité visuelle').click();
  await expect(page.getByRole('heading', { name: 'Identité visuelle', level: 1 })).toBeVisible();
}

async function deleteLogoViaApi(token, request) {
  await request.delete(`${API_URL}/api/me/logo`, {
    headers: { Authorization: `Bearer ${token}` },
  }).catch(() => {});
}

async function deleteBannerViaApi(token, request) {
  await request.delete(`${API_URL}/api/me/banner`, {
    headers: { Authorization: `Bearer ${token}` },
  }).catch(() => {});
}

test.describe('UCSA-16 — Visual identity', () => {
  let vendorToken;

  test.beforeAll(async ({ browser }) => {
    const page = await browser.newPage();
    await createActiveVendorViaApi(page, VENDOR_EMAIL, VENDOR_PASSWORD);
    vendorToken = await getVendorToken(page, VENDOR_EMAIL, VENDOR_PASSWORD);
    await page.close();
  });

  test.afterAll(async ({ request }) => {
    await deleteLogoViaApi(vendorToken, request);
    await deleteBannerViaApi(vendorToken, request);
  });

  test.beforeEach(async ({ page }) => {
    await injectVendorSession(page, VENDOR_EMAIL, vendorToken);
    await navigateToVisualIdentity(page);
  });

  // ── Logo ──────────────────────────────────────────────────────────────────

  test('nominal — uploads logo, success message shown and preview appears', async ({ page }) => {
    await page.locator('#vi-logo-upload').setInputFiles(LOGO_FILE);
    await expect(page.getByText('Logo mis à jour avec succès.')).toBeVisible();
    // Preview <img> must appear in the logo section
    const logoSection = page.locator('section').filter({ hasText: 'Logo de la boutique' });
    await expect(logoSection.locator('img')).toBeVisible();
  });

  test('nominal — uploaded logo URL is present in public theme API', async ({ page }) => {
    await page.locator('#vi-logo-upload').setInputFiles(LOGO_FILE);
    await expect(page.getByText('Logo mis à jour avec succès.')).toBeVisible();

    const res = await page.request.get(`${API_URL}/api/public/theme`);
    expect(res.ok()).toBeTruthy();
    const theme = await res.json();
    expect(theme.logoUrl).toBeTruthy();
    expect(theme.logoUrl).toContain('vendor-logo.png');
  });

  test('nominal — deletes logo, success message shown and placeholder restored', async ({ page }) => {
    // Seed a logo first
    await page.locator('#vi-logo-upload').setInputFiles(LOGO_FILE);
    await expect(page.getByText('Logo mis à jour avec succès.')).toBeVisible();

    const logoSection = page.locator('section').filter({ hasText: 'Logo de la boutique' });
    await logoSection.getByRole('button', { name: 'Supprimer' }).click();
    await expect(page.getByText('Logo supprimé.')).toBeVisible();
    // Preview <img> must disappear; only the placeholder div remains
    await expect(logoSection.locator('img')).not.toBeVisible();

    // API must confirm logo is gone
    const res = await page.request.get(`${API_URL}/api/public/theme`);
    const theme = await res.json();
    expect(theme.logoUrl).toBeNull();
  });

  test('error — uploading unsupported file type for logo shows error alert', async ({ page }) => {
    await page.locator('#vi-logo-upload').setInputFiles(TEXT_FILE);
    await expect(page.getByRole('alert')).toBeVisible();
  });

  // ── Banner ────────────────────────────────────────────────────────────────

  test('nominal — uploads banner, success message shown and preview appears', async ({ page }) => {
    await page.locator('#vi-banner-upload').setInputFiles(BANNER_FILE);
    await expect(page.getByText('Bannière mise à jour avec succès.')).toBeVisible();
    const bannerSection = page.locator('section').filter({ hasText: "Bannière de la page d'accueil" });
    await expect(bannerSection.locator('img')).toBeVisible();
  });

  test('nominal — uploaded banner URL is present in public theme API', async ({ page }) => {
    await page.locator('#vi-banner-upload').setInputFiles(BANNER_FILE);
    await expect(page.getByText('Bannière mise à jour avec succès.')).toBeVisible();

    const res = await page.request.get(`${API_URL}/api/public/theme`);
    expect(res.ok()).toBeTruthy();
    const theme = await res.json();
    expect(theme.bannerUrl).toBeTruthy();
    expect(theme.bannerUrl).toContain('vendor-banner.png');
  });

  test('nominal — deletes banner, success message shown and placeholder restored', async ({ page }) => {
    await page.locator('#vi-banner-upload').setInputFiles(BANNER_FILE);
    await expect(page.getByText('Bannière mise à jour avec succès.')).toBeVisible();

    const bannerSection = page.locator('section').filter({ hasText: "Bannière de la page d'accueil" });
    await bannerSection.getByRole('button', { name: 'Supprimer' }).click();
    await expect(page.getByText('Bannière supprimée.')).toBeVisible();
    await expect(bannerSection.locator('img')).not.toBeVisible();

    const res = await page.request.get(`${API_URL}/api/public/theme`);
    const theme = await res.json();
    expect(theme.bannerUrl).toBeNull();
  });

  test('error — uploading unsupported file type for banner shows error alert', async ({ page }) => {
    await page.locator('#vi-banner-upload').setInputFiles(TEXT_FILE);
    await expect(page.getByRole('alert')).toBeVisible();
  });

  // ── Accent colour ─────────────────────────────────────────────────────────

  test('nominal — changes accent colour and saves successfully', async ({ page }) => {
    const hexInput = page.locator('input[placeholder="#4e8b82"]');
    await hexInput.fill('#c0392b');
    await page.getByRole('button', { name: 'Enregistrer les modifications' }).click();
    await expect(page.getByText('Couleur mise à jour.')).toBeVisible();

    // Restore default to avoid side-effects on other tests
    await hexInput.fill('#4e8b82');
    await page.getByRole('button', { name: 'Enregistrer les modifications' }).click();
  });

  test('nominal — reset button restores default accent colour #4e8b82', async ({ page }) => {
    const hexInput = page.locator('input[placeholder="#4e8b82"]');
    // Set a non-default colour so the reset button appears
    await hexInput.fill('#123456');
    await expect(page.getByRole('button', { name: 'Réinitialiser' })).toBeVisible();

    await page.getByRole('button', { name: 'Réinitialiser' }).click();
    await expect(hexInput).toHaveValue('#4e8b82');
    // Reset button disappears when value equals default
    await expect(page.getByRole('button', { name: 'Réinitialiser' })).not.toBeVisible();
  });

  // ── Cross-portal visibility ───────────────────────────────────────────────

  test('nominal — logo uploaded via vendor appears in buyer portal theme API', async ({ page }) => {
    await page.locator('#vi-logo-upload').setInputFiles(LOGO_FILE);
    await expect(page.getByText('Logo mis à jour avec succès.')).toBeVisible();

    const buyerUrl = process.env.BUYER_URL ?? 'http://buyer.localhost';
    const res = await page.request.get(`${buyerUrl}/api/public/theme`);
    const theme = await res.json();
    expect(theme.logoUrl).toContain('vendor-logo.png');
  });

  test('nominal — banner uploaded via vendor appears in buyer portal theme API', async ({ page }) => {
    await page.locator('#vi-banner-upload').setInputFiles(BANNER_FILE);
    await expect(page.getByText('Bannière mise à jour avec succès.')).toBeVisible();

    const buyerUrl = process.env.BUYER_URL ?? 'http://buyer.localhost';
    const res = await page.request.get(`${buyerUrl}/api/public/theme`);
    const theme = await res.json();
    expect(theme.bannerUrl).toContain('vendor-banner.png');
  });
});
