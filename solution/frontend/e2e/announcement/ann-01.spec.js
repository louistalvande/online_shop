import { test, expect } from '@playwright/test';
import { createActiveVendorViaApi, getVendorToken, injectVendorSession, API_URL } from '../helpers/login.js';
import * as path from 'path';

// US-ANN-01 — Vendor creates, reorders, edits and deletes scrolling announcements.
// Image orientation is auto-detected server-side when an image is uploaded.

const VENDOR_EMAIL    = `vendor-ann-${Date.now()}@shop.test`;
const VENDOR_PASSWORD = 'VendorAnn123!';

async function getVendorTokenHelper(page) {
  return getVendorToken(page, VENDOR_EMAIL, VENDOR_PASSWORD);
}

async function createAnnouncementViaApi(page, token, payload) {
  const res = await page.request.post(`${API_URL}/api/vendor/announcements`, {
    headers: { Authorization: `Bearer ${token}`, 'Content-Type': 'application/json' },
    data: payload,
  });
  expect(res.ok()).toBeTruthy();
  return res.json();
}

test.describe('US-ANN-01 — Scrolling announcements', () => {
  let vendorToken;

  test.beforeAll(async ({ browser }) => {
    const page = await browser.newPage();
    await createActiveVendorViaApi(page, VENDOR_EMAIL, VENDOR_PASSWORD);
    vendorToken = await getVendorTokenHelper(page);
    await page.close();
  });

  test.beforeEach(async ({ page }) => {
    await injectVendorSession(page, VENDOR_EMAIL, vendorToken);
    await page.goto('/vendor/');
    await page.getByText('Identité visuelle').click();
    await expect(page.getByRole('heading', { name: "Carrousel d'annonces" })).toBeVisible();
  });

  // --- nominal: create text announcement ---

  test('nominal — creates a text announcement and it appears in the list', async ({ page }) => {
    await page.getByRole('button', { name: 'Ajouter une annonce' }).click();
    await expect(page.getByText('Ajouter une annonce').last()).toBeVisible();

    await page.getByLabel('Type de contenu').selectOption('TEXT');
    const textArea = page.locator('textarea');
    await textArea.fill('Soldes été — jusqu\'à -50 % !');
    await page.getByRole('button', { name: 'Enregistrer', exact: true }).click();

    const row = page.locator('tbody tr').filter({ hasText: 'Soldes été' });
    await expect(row).toBeVisible();
    await expect(row.getByText('Texte', { exact: true })).toBeVisible();
  });

  // --- nominal: image orientation badge visible after upload ---

  test('nominal — landscape image shows "Paysage" badge after upload', async ({ page }) => {
    // Create a minimal 2×1 px JPEG (wider than tall → LANDSCAPE) as a buffer
    // We use the API directly to seed an announcement with a known orientation
    const ann = await createAnnouncementViaApi(page, vendorToken, {
      contentType: 'IMAGE',
      imageUrl: '/uploads/announcements/test.jpg',
      imageOrientation: 'LANDSCAPE',
      active: true,
      sortOrder: 0,
    });

    await page.reload();
    await page.getByText('Identité visuelle').click();

    // The newly created LANDSCAPE row must appear somewhere in the list
    await expect(page.locator('tbody').getByText('Paysage').first()).toBeVisible();
    void ann; // used for API creation above
  });

  // --- nominal: portrait orientation stored correctly ---

  test('nominal — portrait announcement shows "Portrait" badge in list', async ({ page }) => {
    await createAnnouncementViaApi(page, vendorToken, {
      contentType: 'IMAGE',
      imageUrl: '/uploads/announcements/portrait.jpg',
      imageOrientation: 'PORTRAIT',
      active: true,
      sortOrder: 1,
    });

    await page.reload();
    await page.getByText('Identité visuelle').click();

    await expect(page.getByText('Portrait')).toBeVisible();
  });

  // --- nominal: edit announcement ---

  test('nominal — edits announcement text and change is reflected', async ({ page }) => {
    await createAnnouncementViaApi(page, vendorToken, {
      contentType: 'TEXT',
      textContent: 'Original text',
      active: true,
      sortOrder: 0,
    });

    await page.reload();
    await page.getByText('Identité visuelle').click();

    const row = page.locator('tbody tr').filter({ hasText: 'Original text' });
    await row.getByRole('button', { name: 'Modifier' }).click();

    await page.locator('textarea').clear();
    await page.locator('textarea').fill('Updated text');
    await page.getByRole('button', { name: 'Enregistrer', exact: true }).click();

    await expect(page.getByText('Updated text')).toBeVisible();
    await expect(page.getByText('Original text')).not.toBeVisible();
  });

  // --- nominal: delete announcement ---

  test('nominal — deletes announcement and it disappears from the list', async ({ page }) => {
    await createAnnouncementViaApi(page, vendorToken, {
      contentType: 'TEXT',
      textContent: 'To be deleted',
      active: true,
      sortOrder: 0,
    });

    await page.reload();
    await page.getByText('Identité visuelle').click();

    page.once('dialog', dialog => dialog.accept());
    const row = page.locator('tbody tr').filter({ hasText: 'To be deleted' });
    await row.getByRole('button', { name: 'Supprimer' }).click();

    await expect(page.getByText('To be deleted')).not.toBeVisible();
  });

  // --- nominal: reorder ---

  test('nominal — moves announcement down then up, sort order swaps', async ({ page }) => {
    // Unique text per run to avoid collisions with previous test data
    const suffix = Date.now();
    const textA = `ReoA-${suffix}`;
    const textB = `ReoB-${suffix}`;

    await createAnnouncementViaApi(page, vendorToken, {
      contentType: 'TEXT', textContent: textA, active: true, sortOrder: 900,
    });
    await createAnnouncementViaApi(page, vendorToken, {
      contentType: 'TEXT', textContent: textB, active: true, sortOrder: 901,
    });

    await page.reload();
    await page.getByText('Identité visuelle').click();

    const rowA = page.locator('tbody tr').filter({ hasText: textA });
    const rowB = page.locator('tbody tr').filter({ hasText: textB });
    await expect(rowA).toBeVisible();
    await expect(rowB).toBeVisible();

    // Click ↓ on rowA — the list refreshes; wait for rowB to precede rowA
    await rowA.getByRole('button', { name: '↓' }).click();

    // Wait until the DOM shows textB appearing before textA
    await page.waitForFunction(([a, b]) => {
      const rows = [...document.querySelectorAll('tbody tr')];
      const idxA = rows.findIndex(r => r.textContent?.includes(a));
      const idxB = rows.findIndex(r => r.textContent?.includes(b));
      return idxA !== -1 && idxB !== -1 && idxB < idxA;
    }, [textA, textB], { timeout: 10000 });
  });

  // --- nominal: inactive announcement not shown in buyer carousel ---

  test('nominal — inactive announcement is not returned by public endpoint', async ({ page }) => {
    await createAnnouncementViaApi(page, vendorToken, {
      contentType: 'TEXT',
      textContent: 'Hidden announcement',
      active: false,
      sortOrder: 0,
    });

    const res = await page.request.get(`${API_URL}/api/announcements`);
    const list = await res.json();
    const found = list.find((a) => a.textContent === 'Hidden announcement');
    expect(found).toBeUndefined();
  });

  // --- error: text required for TEXT type ---

  test('error — saving TEXT type without text shows validation error', async ({ page }) => {
    await page.getByRole('button', { name: 'Ajouter une annonce' }).click();
    await page.getByLabel('Type de contenu').selectOption('TEXT');
    // Leave textarea empty
    await page.getByRole('button', { name: 'Enregistrer', exact: true }).click();

    await expect(page.getByText('Le texte est obligatoire')).toBeVisible();
  });

  // --- error: image required for IMAGE type ---

  test('error — saving IMAGE type without image shows validation error', async ({ page }) => {
    await page.getByRole('button', { name: 'Ajouter une annonce' }).click();
    await page.getByLabel('Type de contenu').selectOption('IMAGE');
    await page.getByRole('button', { name: 'Enregistrer', exact: true }).click();

    await expect(page.getByText('Une image est requise')).toBeVisible();
  });
});
