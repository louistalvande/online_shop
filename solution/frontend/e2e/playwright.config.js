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
      testMatch: ['registration/**/*.spec.js'],
      use: {
        ...devices['Desktop Chrome'],
        baseURL: process.env.BUYER_URL ?? 'http://buyer.localhost',
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
  ],
});
