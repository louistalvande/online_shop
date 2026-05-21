import { test, expect } from '@playwright/test';

// US-REG-01 — A visitor can create a buyer account via the registration form.

test.describe('US-REG-01 — Buyer registration', () => {
  test.beforeEach(async ({ page }) => {
    await page.goto('/register');
  });

  test('nominal — submits valid form and shows success message', async ({ page }) => {
    const email = `reg01-${Date.now()}@shop-test.example`;

    await page.getByLabel('Prénom').fill('Alice');
    await page.getByLabel('Nom', { exact: true }).fill('Martin');
    await page.getByLabel('Email').fill(email);
    await page.getByLabel('Mot de passe', { exact: true }).fill('sHp-E2e!Reg-X9pZ');
    await page.getByLabel('Confirmer le mot de passe').fill('sHp-E2e!Reg-X9pZ');

    await page.getByRole('button', { name: 'Créer mon compte' }).click();

    await expect(
      page.getByText('Compte créé ! Vérifiez votre boîte email pour activer votre compte.')
    ).toBeVisible();
  });

  test('error — duplicate email shows inline error', async ({ page }) => {
    const email = `reg01-dup-${Date.now()}@shop-test.example`;

    // Register once
    await page.getByLabel('Prénom').fill('Alice');
    await page.getByLabel('Nom', { exact: true }).fill('Martin');
    await page.getByLabel('Email').fill(email);
    await page.getByLabel('Mot de passe', { exact: true }).fill('sHp-E2e!Reg-X9pZ');
    await page.getByLabel('Confirmer le mot de passe').fill('sHp-E2e!Reg-X9pZ');
    await page.getByRole('button', { name: 'Créer mon compte' }).click();
    await expect(page.getByText('Compte créé !')).toBeVisible();

    // Try again with same email
    await page.goto('/register');
    await page.getByLabel('Prénom').fill('Bob');
    await page.getByLabel('Nom', { exact: true }).fill('Dupont');
    await page.getByLabel('Email').fill(email);
    await page.getByLabel('Mot de passe', { exact: true }).fill('sHp-E2e!Reg-X9pZ');
    await page.getByLabel('Confirmer le mot de passe').fill('sHp-E2e!Reg-X9pZ');
    await page.getByRole('button', { name: 'Créer mon compte' }).click();

    await expect(page.getByText('Cette adresse email est déjà utilisée.')).toBeVisible();
  });

  test('error — mismatched passwords shows inline error', async ({ page }) => {
    await page.getByLabel('Prénom').fill('Alice');
    await page.getByLabel('Nom', { exact: true }).fill('Martin');
    await page.getByLabel('Email').fill(`reg01-mismatch-${Date.now()}@shop-test.example`);
    await page.getByLabel('Mot de passe', { exact: true }).fill('sHp-E2e!Reg-X9pZ');
    await page.getByLabel('Confirmer le mot de passe').fill('Different1!');

    await page.getByRole('button', { name: 'Créer mon compte' }).click();

    await expect(page.getByText('Les mots de passe ne correspondent pas.')).toBeVisible();
  });

  test('navigation — register page has a link to login', async ({ page }) => {
    await expect(page.getByText('Se connecter')).toBeVisible();
  });
});
