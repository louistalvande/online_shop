import { resetDatabase } from './helpers/db-reset.js';

export default async function globalTeardown() {
  await resetDatabase();
}
