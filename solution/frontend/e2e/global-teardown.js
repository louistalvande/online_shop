import pg from 'pg';

const { Client } = pg;

// Seed rows preserved after teardown — accounts and carriers created by Flyway V1.
const SEED_EMAILS   = ['admin@onlineshop.com', 'mlloubiere@gmail.com', 'louis.talvande@gmail.com'];
const SEED_CARRIERS = ['La Poste'];

export default async function globalTeardown() {
  const client = new Client({
    host:     process.env.DB_HOST     ?? 'localhost',
    port:     parseInt(process.env.DB_PORT ?? '5432'),
    database: process.env.DB_NAME     ?? 'shop',
    user:     process.env.DB_USER     ?? 'shop',
    password: process.env.DB_PASSWORD ?? 'dev',
  });

  await client.connect();
  try {
    await client.query('BEGIN');

    // Leaf tables first (respect FK dependency order).
    await client.query('DELETE FROM back_in_stock_subscriptions');
    await client.query('DELETE FROM order_lines');
    await client.query('DELETE FROM cart_item');
    await client.query('DELETE FROM stock_alerts');
    await client.query('DELETE FROM product_photos');
    await client.query('DELETE FROM activation_tokens');
    await client.query('DELETE FROM password_reset_tokens');

    // Mid-level: orders must precede delivery_address (non-cascading FK).
    await client.query('DELETE FROM orders');
    await client.query('DELETE FROM cart');
    await client.query('DELETE FROM delivery_address');
    await client.query('DELETE FROM products');
    await client.query('DELETE FROM announcements');

    // Partitioned table — DELETE propagates to all partitions.
    await client.query('DELETE FROM audit_log');

    // Partial cleans: keep Flyway seed rows.
    await client.query(
      `DELETE FROM accounts WHERE email != ALL($1)`,
      [SEED_EMAILS],
    );
    await client.query(
      `DELETE FROM carrier_countries
         WHERE carrier_id NOT IN (SELECT id FROM carriers WHERE name = ANY($1))`,
      [SEED_CARRIERS],
    );
    await client.query(
      `DELETE FROM carriers WHERE name != ALL($1)`,
      [SEED_CARRIERS],
    );

    // Reset mutable platform settings to Flyway seed values.
    await client.query(`UPDATE platform_settings SET value = 'false'    WHERE key = 'maintenance_mode'`);
    await client.query(`UPDATE platform_settings SET value = '#4e8b82'  WHERE key = 'shop_accent_color'`);
    await client.query(`UPDATE platform_settings SET value = '#f2f6f5'  WHERE key = 'shop_bg_color'`);

    await client.query('COMMIT');
    console.log('[teardown] database reset to seed state');
  } catch (err) {
    await client.query('ROLLBACK');
    console.error('[teardown] rollback —', err.message);
    throw err;
  } finally {
    await client.end();
  }
}
