import { test, expect } from '@playwright/test';
import { loginAsAdmin } from '../helpers/login.js';

// US-ADM-01 — Create a buyer or vendor account from the admin console.

test.describe('US-ADM-01 — Create account', () => {
  test.beforeEach(async ({ page }) => {
    await loginAsAdmin(page);
    await page.goto('/#users');
  });

  test('nominal — creates a buyer account and shows it in the list', async ({ page }) => {
    const email = `adm01-${Date.now()}@shop-test.example`;

    await page.getByRole('button', { name: 'Ajouter un compte' }).click();
    await expect(page.getByText('Créer un compte')).toBeVisible();

    await page.getByLabel('Prénom').fill('Alice');
    await page.getByLabel('Nom', { exact: true }).fill('Martin');
    await page.getByLabel('Email').fill(email);
    await page.getByLabel('Rôle').selectOption('BUYER');

    await page.getByRole('button', { name: 'Créer le compte' }).click();

    await expect(page.getByText('Compte créé avec succès.')).toBeVisible();
    await expect(page.getByText(email)).toBeVisible();
  });

  test('nominal — creates a vendor account', async ({ page }) => {
    const email = `adm01-vendor-${Date.now()}@shop-test.example`;

    await page.getByRole('button', { name: 'Ajouter un compte' }).click();
    await page.getByLabel('Prénom').fill('Bob');
    await page.getByLabel('Nom', { exact: true }).fill('Vendeur');
    await page.getByLabel('Email').fill(email);
    await page.getByLabel('Rôle').selectOption('VENDOR');
    await page.getByRole('button', { name: 'Créer le compte' }).click();

    await expect(page.getByText('Compte créé avec succès.')).toBeVisible();
    await expect(page.getByText(email)).toBeVisible();
  });

  test('error — duplicate email shows inline error', async ({ page }) => {
    const email = `adm01-${Date.now()}@shop-test.example`;

    // Create the account a first time
    await page.getByRole('button', { name: 'Ajouter un compte' }).click();
    await page.getByLabel('Prénom').fill('Alice');
    await page.getByLabel('Nom', { exact: true }).fill('Martin');
    await page.getByLabel('Email').fill(email);
    await page.getByLabel('Rôle').selectOption('BUYER');
    await page.getByRole('button', { name: 'Créer le compte' }).click();
    await expect(page.getByText('Compte créé avec succès.')).toBeVisible();

    // Try to create again with the same email
    await page.getByRole('button', { name: 'Ajouter un compte' }).click();
    await page.getByLabel('Prénom').fill('Alice2');
    await page.getByLabel('Nom', { exact: true }).fill('Martin2');
    await page.getByLabel('Email').fill(email);
    await page.getByLabel('Rôle').selectOption('BUYER');
    await page.getByRole('button', { name: 'Créer le compte' }).click();

    await expect(page.getByText('Cette adresse email est déjà utilisée.')).toBeVisible();
  });

  test('cancel — closes the modal without creating an account', async ({ page }) => {
    await page.getByRole('button', { name: 'Ajouter un compte' }).click();
    await expect(page.getByText('Créer un compte')).toBeVisible();

    await page.getByRole('button', { name: 'Annuler' }).click();
    await expect(page.getByText('Créer un compte')).not.toBeVisible();
  });
});
