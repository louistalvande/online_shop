import { test, expect } from '@playwright/test';
import { createActiveVendorViaApi, getVendorToken, injectVendorSession, API_URL } from '../helpers/login.js';

// US-SEO-05 — Vendor integrates third-party tools (Google Analytics 4, Google Search Console, Bing Webmaster).

const VENDOR_EMAIL    = `vendor-seo05-${Date.now()}@shop.test`;
const VENDOR_PASSWORD = 'VendorSeo05!';

test.describe('US-SEO-05 — Third-party tools integration', () => {
  let vendorToken;

  test.beforeAll(async ({ browser }) => {
    const page = await browser.newPage();
    await createActiveVendorViaApi(page, VENDOR_EMAIL, VENDOR_PASSWORD);
    vendorToken = await getVendorToken(page, VENDOR_EMAIL, VENDOR_PASSWORD);
    await page.close();
  });

  test.beforeEach(async ({ page }) => {
    await injectVendorSession(page, VENDOR_EMAIL, vendorToken);
  });

  // --- nominal: save GA4 ID and verification codes via API ---

  test('nominal — API saves GA4 ID and verification codes', async ({ page }) => {
    const res = await page.request.put(`${API_URL}/api/vendor/seo`, {
      headers: { Authorization: `Bearer ${vendorToken}`, 'Content-Type': 'application/json' },
      data: {
        ga4Id: 'G-TEST12345',
        googleVerification: 'google-token-abc',
        bingVerification: 'bing-token-xyz',
      },
    });
    expect(res.ok()).toBeTruthy();
    const body = await res.json();
    expect(body.ga4Id).toBe('G-TEST12345');
    expect(body.googleVerification).toBe('google-token-abc');
    expect(body.bingVerification).toBe('bing-token-xyz');
  });

  // --- nominal: Google verification meta tag appears in buyer portal ---

  test('nominal — Google Search Console meta tag appears in buyer portal', async ({ page }) => {
    await page.request.put(`${API_URL}/api/vendor/seo`, {
      headers: { Authorization: `Bearer ${vendorToken}`, 'Content-Type': 'application/json' },
      data: { googleVerification: 'google-verify-test-token' },
    });

    await page.goto('/');
    await expect(page.locator('meta[name="google-site-verification"]')).toHaveAttribute(
      'content',
      'google-verify-test-token'
    );
  });

  // --- nominal: Bing verification meta tag appears in buyer portal ---

  test('nominal — Bing Webmaster meta tag appears in buyer portal', async ({ page }) => {
    await page.request.put(`${API_URL}/api/vendor/seo`, {
      headers: { Authorization: `Bearer ${vendorToken}`, 'Content-Type': 'application/json' },
      data: { bingVerification: 'bing-verify-test-token' },
    });

    await page.goto('/');
    await expect(page.locator('meta[name="msvalidate.01"]')).toHaveAttribute(
      'content',
      'bing-verify-test-token'
    );
  });

  // --- nominal: GA4 script injected in buyer portal when ga4Id is set ---

  test('nominal — GA4 gtag script is injected in buyer portal when ga4Id is set', async ({ page }) => {
    await page.request.put(`${API_URL}/api/vendor/seo`, {
      headers: { Authorization: `Bearer ${vendorToken}`, 'Content-Type': 'application/json' },
      data: { ga4Id: 'G-TESTSCRIPT' },
    });

    await page.goto('/');
    const gtagScript = page.locator('script[src*="googletagmanager.com"]');
    await expect(gtagScript).toHaveAttribute('src', /G-TESTSCRIPT/);
  });

  // --- nominal: Outils tiers tab is visible in vendor SEO page ---

  test('nominal — Outils tiers tab is accessible in the SEO page', async ({ page }) => {
    await page.goto('/vendor/');
    await page.getByText('Référencement (SEO)').click();
    await page.getByText('Outils tiers').click();

    await expect(page.getByLabel('Code de vérification Google Search Console')).toBeVisible();
    await expect(page.getByLabel('ID de Google Analytics 4 (G-XXXXXXXXXX)')).toBeVisible();
    await expect(page.getByLabel('Code de vérification Bing Webmaster')).toBeVisible();
  });

  // --- nominal: saving tools configuration from UI persists correctly ---

  test('nominal — saving from UI persists tools configuration', async ({ page }) => {
    await page.goto('/vendor/');
    await page.getByText('Référencement (SEO)').click();
    await page.getByText('Outils tiers').click();

    await page.getByLabel('ID de Google Analytics 4 (G-XXXXXXXXXX)').fill('G-UI12345');
    await page.getByRole('button', { name: 'Enregistrer' }).click();

    await expect(page.getByText('Configuration enregistrée.')).toBeVisible();
  });

  // --- error: unauthenticated PUT to /api/vendor/seo returns 401 ---

  test('error — unauthenticated request to /api/vendor/seo returns 401', async ({ page }) => {
    const res = await page.request.put(`${API_URL}/api/vendor/seo`, {
      headers: { 'Content-Type': 'application/json' },
      data: { ga4Id: 'G-NOPE' },
    });
    expect(res.status()).toBe(401);
  });
});
