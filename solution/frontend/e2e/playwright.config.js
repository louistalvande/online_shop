import { defineConfig, devices } from '@playwright/test';

export default defineConfig({
  testDir: '.',
  globalSetup: './global-setup.js',
  globalTeardown: './global-teardown.js',
  fullyParallel: false,
  retries: 0,
  timeout: 120000,
  expect: { timeout: 15000 },
  reporter: [
    ['html', { outputFolder: 'playwright-report', open: 'never' }],
    ['list'],
  ],
  use: {
    screenshot: 'on',
    video: 'retain-on-failure',
    actionTimeout: 30000,
    navigationTimeout: 60000,
  },
  projects: [
    {
      name: 'admin-console',
      testMatch: ['account/**/*.spec.js', 'carrier/**/*.spec.js', 'settings/**/*.spec.js'],
      use: {
        ...devices['Desktop Chrome'],
        baseURL: process.env.ADMIN_URL ?? 'http://admin.localhost',
      },
    },
    {
      name: 'buyer-portal',
      testMatch: ['registration/**/*.spec.js', 'profile/prf-01.spec.js', 'profile/prf-03.spec.js', 'profile/prf-04.spec.js', 'shop/**/*.spec.js', 'i18n/**/*.spec.js', 'buyer-auth/**/*.spec.js', 'buyer-shop/**/*.spec.js', 'buyer-cart/**/*.spec.js', 'buyer-checkout/**/*.spec.js', 'buyer-orders/**/*.spec.js'],
      use: {
        ...devices['Desktop Chrome'],
        baseURL: process.env.BUYER_URL ?? 'http://buyer.localhost',
      },
    },
    {
      name: 'vendor-portal',
      testMatch: ['profile/prf-02.spec.js', 'profile/prf-05.spec.js', 'catalog/**/*.spec.js', 'announcement/**/*.spec.js', 'visual-identity/**/*.spec.js', 'vendor-dashboard/**/*.spec.js', 'vendor-orders/**/*.spec.js', 'reports/**/*.spec.js'],
      use: {
        ...devices['Desktop Chrome'],
        baseURL: process.env.VENDOR_URL ?? 'http://vendor.localhost',
      },
    },
    {
      name: 'auth',
      testMatch: ['auth/**/*.spec.js'],
      use: {
        ...devices['Desktop Chrome'],
        baseURL: process.env.BUYER_URL ?? 'http://buyer.localhost',
      },
    },
    {
      name: 'cart',
      testMatch: ['cart/**/*.spec.js'],
      use: {
        ...devices['Desktop Chrome'],
        baseURL: process.env.BUYER_URL ?? 'http://buyer.localhost',
      },
    },
    {
      name: 'order',
      testMatch: ['order/**/*.spec.js'],
      use: {
        ...devices['Desktop Chrome'],
        baseURL: process.env.BUYER_URL ?? 'http://buyer.localhost',
      },
    },
  ],
});
