import { test, expect } from '@playwright/test';
import { createActiveVendorViaApi, getVendorToken, injectVendorSession, API_URL } from '../helpers/login.js';

// UCSA-16 — Vendor personalises the shop visual identity: logo, hero banner, accent colour.

const VENDOR_EMAIL    = `vendor-vi-${Date.now()}@shop.test`;
const VENDOR_PASSWORD = 'VndVI!Secure-2026';

// Minimal 1×1 white PNG — generated via Python zlib/struct, valid for Java ImageIO.
const PNG_1X1 = Buffer.from(
  'iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAIAAACQd1PeAAAADElEQVR4nGP4//8/AAX+Av4N70a4AAAAAElFTkSuQmCC',
  'base64'
);

const LOGO_FILE   = { name: 'logo.png',   mimeType: 'image/png', buffer: PNG_1X1 };
const BANNER_FILE = { name: 'banner.png', mimeType: 'image/png', buffer: PNG_1X1 };
const TEXT_FILE   = { name: 'bad.txt',    mimeType: 'text/plain', buffer: Buffer.from('not an image') };

async function loginAsVendor(page) {
  await page.goto('/vendor/');
  await page.getByLabel('Adresse email').fill(VENDOR_EMAIL);
  await page.getByLabel('Mot de passe').fill(VENDOR_PASSWORD);
  await page.getByRole('button', { name: 'Se connecter' }).click();
  await expect(page.getByRole('heading', { name: 'Tableau de bord' })).toBeVisible({ timeout: 10000 });
}

async function navigateToVisualIdentity(page) {
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

  test.afterEach(async ({ request }) => {
    await request.patch(`${API_URL}/api/vendor/shop/theme`, {
      headers: { Authorization: `Bearer ${vendorToken}` },
      data: { accentColor: '#4e8b82', bgColor: '#f2f6f5' },
    });
  });

  test.afterAll(async ({ request }) => {
    await deleteLogoViaApi(vendorToken, request);
    await deleteBannerViaApi(vendorToken, request);
  });

  test.beforeEach(async ({ page }) => {
    await loginAsVendor(page);
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
    await page.getByRole('button', { name: 'Enregistrer' }).click();
    await expect(page.getByText('Couleurs mises à jour.')).toBeVisible();
  });

  test('nominal — reset button restores default accent colour #4e8b82', async ({ page }) => {
    const hexInput = page.locator('input[placeholder="#4e8b82"]');
    // Set a non-default colour so the reset button appears
    await hexInput.fill('#123456');
    const colorSection = page.locator('section').filter({ hasText: 'Couleurs de la boutique' });
    await expect(colorSection.getByRole('button', { name: 'Réinitialiser' }).first()).toBeVisible();

    await colorSection.getByRole('button', { name: 'Réinitialiser' }).first().click();
    await expect(hexInput).toHaveValue('#4e8b82');
  });

  // ── Background colour ─────────────────────────────────────────────────────

  test('nominal — changes background colour and saves successfully', async ({ page }) => {
    const hexInput = page.locator('input[placeholder="#f2f6f5"]');
    await hexInput.fill('#ffffff');
    await page.getByRole('button', { name: 'Enregistrer' }).click();
    await expect(page.getByText('Couleurs mises à jour.')).toBeVisible();
  });

  test('nominal — bgColor persisted in public theme API after save', async ({ page }) => {
    const hexInput = page.locator('input[placeholder="#f2f6f5"]');
    await hexInput.fill('#e8f0fe');
    await page.getByRole('button', { name: 'Enregistrer' }).click();
    await expect(page.getByText('Couleurs mises à jour.')).toBeVisible();

    const res = await page.request.get(`${API_URL}/api/public/theme`);
    expect(res.ok()).toBeTruthy();
    const theme = await res.json();
    expect(theme.bgColor).toBe('#e8f0fe');
  });

  test('nominal — reset button restores default background colour #f2f6f5', async ({ page }) => {
    const hexInput = page.locator('input[placeholder="#f2f6f5"]');
    await hexInput.fill('#abcdef');
    const colorSection = page.locator('section').filter({ hasText: 'Couleurs de la boutique' });
    // The bg reset button is the second Réinitialiser in the section
    await expect(colorSection.getByRole('button', { name: 'Réinitialiser' }).last()).toBeVisible();

    await colorSection.getByRole('button', { name: 'Réinitialiser' }).last().click();
    await expect(hexInput).toHaveValue('#f2f6f5');
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
