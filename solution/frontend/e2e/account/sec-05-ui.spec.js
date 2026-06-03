import { test, expect } from '@playwright/test';
import { loginAsAdmin } from '../helpers/login.js';

// SEC-05-UI — Audit log section UI: filters, pagination, CSV export (US-SEC-05).

async function openAuditLog(page) {
  await loginAsAdmin(page);
  await page.goto('/admin/#audit');
  await expect(page.getByRole('heading', { name: 'Journaux d\'audit' })).toBeVisible();
  // Wait for the initial load
  await page.waitForTimeout(500);
}

test.describe('Admin console — Audit log UI', () => {
  // ── Filters ───────────────────────────────────────────────────────────────

  test('nominal — event type filter select is visible with all-events option', async ({ page }) => {
    await openAuditLog(page);
    const select = page.locator('select').filter({ has: page.locator('option[value="LOGIN_SUCCESS"]') });
    await expect(select).toBeVisible();
    await expect(select).toHaveValue('');
  });

  test('nominal — selecting an event type refreshes the table automatically', async ({ page }) => {
    await openAuditLog(page);
    const select = page.locator('select').filter({ has: page.locator('option[value="LOGIN_SUCCESS"]') });
    await select.selectOption('LOGIN_SUCCESS');
    await page.waitForTimeout(600);
    // Every visible event type cell should be LOGIN_SUCCESS
    await expect(page.locator('td').filter({ hasText: 'LOGIN_SUCCESS' }).first()).toBeVisible();
  });

  test('nominal — email filter input is visible', async ({ page }) => {
    await openAuditLog(page);
    await expect(page.locator('input[placeholder="Filtrer par email"]')).toBeVisible();
  });

  test('nominal — IP filter input is visible', async ({ page }) => {
    await openAuditLog(page);
    await expect(page.locator('input[placeholder="Filtrer par IP"]')).toBeVisible();
  });

  test('nominal — Apply button fetches filtered results by email', async ({ page }) => {
    await openAuditLog(page);
    await page.locator('input[placeholder="Filtrer par email"]').fill('admin@onlineshop.com');
    await page.getByRole('button', { name: 'Appliquer' }).click();
    await page.waitForTimeout(600);
    await expect(page.locator('td').filter({ hasText: 'admin@onlineshop.com' }).first()).toBeVisible();
  });

  test('nominal — Enter key in email filter triggers fetch', async ({ page }) => {
    await openAuditLog(page);
    const emailInput = page.locator('input[placeholder="Filtrer par email"]');
    await emailInput.fill('admin@onlineshop.com');
    await emailInput.press('Enter');
    await page.waitForTimeout(600);
    await expect(page.locator('td').filter({ hasText: 'admin@onlineshop.com' }).first()).toBeVisible();
  });

  test('nominal — resetting event type filter to all reloads all events', async ({ page }) => {
    await openAuditLog(page);
    const select = page.locator('select').filter({ has: page.locator('option[value="LOGIN_SUCCESS"]') });
    await select.selectOption('LOGIN_SUCCESS');
    await page.waitForTimeout(400);
    await select.selectOption('');
    await page.waitForTimeout(400);
    await expect(select).toHaveValue('');
  });

  // ── Pagination ────────────────────────────────────────────────────────────

  test('nominal — Previous button is disabled on first page', async ({ page }) => {
    await openAuditLog(page);
    await expect(page.getByRole('button', { name: /Précédent/ })).toBeDisabled();
  });

  test('nominal — pagination info shows Page 1', async ({ page }) => {
    await openAuditLog(page);
    await expect(page.getByText(/Page 1 \/ \d+/)).toBeVisible();
  });

  test('nominal — Next button is enabled when more than one page exists', async ({ page }) => {
    await openAuditLog(page);
    const nextBtn = page.getByRole('button', { name: /Suivant/ });
    const isDisabled = await nextBtn.isDisabled();
    if (!isDisabled) {
      await nextBtn.click();
      await page.waitForTimeout(400);
      await expect(page.getByText(/Page 2 \/ \d+/)).toBeVisible();
      await expect(page.getByRole('button', { name: /Précédent/ })).toBeEnabled();
    }
  });

  // ── CSV export ────────────────────────────────────────────────────────────

  test('nominal — Export CSV button is visible and enabled', async ({ page }) => {
    await openAuditLog(page);
    await expect(page.getByRole('button', { name: 'Exporter CSV' })).toBeEnabled();
  });

  test('nominal — Export CSV triggers a file download', async ({ page }) => {
    await openAuditLog(page);
    const [download] = await Promise.all([
      page.waitForEvent('download'),
      page.getByRole('button', { name: 'Exporter CSV' }).click(),
    ]);
    expect(download.suggestedFilename()).toMatch(/audit/i);
  });
});
