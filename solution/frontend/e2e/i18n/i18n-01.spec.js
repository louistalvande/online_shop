import { test, expect } from '@playwright/test';

// i18n-01 — UI language toggle on the buyer portal cycles FR → EN → ES → FR.

test.describe('i18n-01 — Language toggle', () => {
  test.beforeEach(async ({ page }) => {
    await page.goto('/');
  });

  test('nominal — default language is French', async ({ page }) => {
    await expect(page.locator('.lang-toggle')).toContainText('FR');
    await expect(page.getByText('Accueil')).toBeVisible();
  });

  test('nominal — first click switches UI to English', async ({ page }) => {
    await page.locator('.lang-toggle').click();

    await expect(page.locator('.lang-toggle')).toContainText('EN');
    await expect(page.getByText('Home')).toBeVisible();
  });

  test('nominal — second click switches UI to Spanish', async ({ page }) => {
    await page.locator('.lang-toggle').click(); // FR → EN
    await page.locator('.lang-toggle').click(); // EN → ES

    await expect(page.locator('.lang-toggle')).toContainText('ES');
    await expect(page.getByText('Inicio')).toBeVisible();
  });

  test('nominal — third click cycles back to French', async ({ page }) => {
    await page.locator('.lang-toggle').click(); // FR → EN
    await page.locator('.lang-toggle').click(); // EN → ES
    await page.locator('.lang-toggle').click(); // ES → FR

    await expect(page.locator('.lang-toggle')).toContainText('FR');
    await expect(page.getByText('Accueil')).toBeVisible();
  });
});
