import { defineConfig, devices } from '@playwright/test';

export default defineConfig({
  testDir: '.',
  fullyParallel: false,
  retries: 0,
  use: {
    screenshot: 'only-on-failure',
    video: 'retain-on-failure',
  },
  projects: [
    {
      name: 'admin-console',
      testMatch: ['account/**/*.spec.js', 'carrier/**/*.spec.js'],
      use: {
        ...devices['Desktop Chrome'],
        baseURL: process.env.ADMIN_URL ?? 'http://admin.localhost',
      },
    },
    {
      name: 'buyer-portal',
      testMatch: ['registration/**/*.spec.js', 'profile/prf-01.spec.js', 'shop/**/*.spec.js'],
      use: {
        ...devices['Desktop Chrome'],
        baseURL: process.env.BUYER_URL ?? 'http://buyer.localhost',
      },
    },
    {
      name: 'vendor-portal',
      testMatch: ['profile/prf-02.spec.js', 'catalog/**/*.spec.js'],
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
