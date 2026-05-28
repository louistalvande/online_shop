-- V1: initial schema — account domain (US-ADM-01) + profile fields (US-PRF-01, US-PRF-02)

CREATE TABLE accounts (
    id                   UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    email                VARCHAR(255) NOT NULL UNIQUE,
    password_hash        VARCHAR(255),
    first_name           VARCHAR(100) NOT NULL,
    last_name            VARCHAR(100) NOT NULL,
    role                 VARCHAR(20)  NOT NULL,
    status               VARCHAR(20)  NOT NULL,
    language             VARCHAR(2)   NOT NULL DEFAULT 'FR',
    must_change_password BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at           TIMESTAMP    NOT NULL DEFAULT NOW(),
    phone                VARCHAR(20),
    -- SEC-PWD-003/004 (CPA-17): NULL = no expiry (BUYER), set for ADMIN
    password_expires_at  TIMESTAMPTZ,
    -- SEC-PWD-005 (CPA-17): flag for immediate revocation
    password_revoked     BOOLEAN      NOT NULL DEFAULT FALSE,
    -- SEC-AUTH-007/008 (CPA-15): TOTP MFA for VENDOR and ADMIN
    totp_secret          VARCHAR(64),
    totp_enabled         BOOLEAN      NOT NULL DEFAULT FALSE
);

-- Admin password expires in 2 years (SEC-PWD-004 / CPA-17)
INSERT INTO accounts (id, email, password_hash, first_name, last_name, role, status, must_change_password, created_at, password_expires_at)
VALUES (
  gen_random_uuid(),
  'admin@onlineshop.com',
  '$2a$12$p8qx.P/UPC0iBPtwiliBFO9fyoFK7k9ciql2mOOCWvgB.24.prp0O', -- Admin123456!
  'Admin',
  'System',
  'ADMIN',
  'ACTIVE',
  FALSE,
  NOW(),
  NOW() + INTERVAL '2 years'
);

-- Seed vendor account
INSERT INTO accounts (id, email, password_hash, first_name, last_name, role, status, must_change_password, created_at)
VALUES (
  gen_random_uuid(),
  'mlloubiere@gmail.com',
  '$2a$12$p8qx.P/UPC0iBPtwiliBFO9fyoFK7k9ciql2mOOCWvgB.24.prp0O', -- Admin123456!
  'Vendor',
  'Seed',
  'VENDOR',
  'ACTIVE',
  FALSE,
  NOW()
);

-- Seed buyer account
INSERT INTO accounts (id, email, password_hash, first_name, last_name, role, status, must_change_password, created_at)
VALUES (
  gen_random_uuid(),
  'louis.talvande@gmail.com',
  '$2a$12$p8qx.P/UPC0iBPtwiliBFO9fyoFK7k9ciql2mOOCWvgB.24.prp0O', -- Admin123456!
  'Louis',
  'Talvande',
  'BUYER',
  'ACTIVE',
  FALSE,
  NOW()
);


-- Activation tokens for admin-created accounts (US-ADM-01) and buyer self-registration (US-REG-01)
CREATE TABLE activation_tokens (
    token      VARCHAR(36)  PRIMARY KEY,
    account_id UUID         NOT NULL REFERENCES accounts(id) ON DELETE CASCADE,
    expires_at TIMESTAMP    NOT NULL
);


-- Password reset tokens — SEC-PWD-006 (CPA-17): temp unique link, 1 h TTL
CREATE TABLE password_reset_tokens (
    token      VARCHAR(36)  PRIMARY KEY,
    account_id UUID         NOT NULL REFERENCES accounts(id) ON DELETE CASCADE,
    expires_at TIMESTAMPTZ  NOT NULL,
    used       BOOLEAN      NOT NULL DEFAULT FALSE
);

CREATE INDEX idx_pwd_reset_tokens_account_id ON password_reset_tokens (account_id);


-- Eurozone country reference data (CS-04)
CREATE TABLE countries (
    code    VARCHAR(2)   PRIMARY KEY,
    name_fr VARCHAR(100) NOT NULL,
    name_en VARCHAR(100) NOT NULL
);

INSERT INTO countries (code, name_fr, name_en) VALUES
    ('AT', 'Autriche',   'Austria'),
    ('BE', 'Belgique',   'Belgium'),
    ('CY', 'Chypre',     'Cyprus'),
    ('DE', 'Allemagne',  'Germany'),
    ('EE', 'Estonie',    'Estonia'),
    ('ES', 'Espagne',    'Spain'),
    ('FI', 'Finlande',   'Finland'),
    ('FR', 'France',     'France'),
    ('GR', 'Grèce',      'Greece'),
    ('HR', 'Croatie',    'Croatia'),
    ('IE', 'Irlande',    'Ireland'),
    ('IT', 'Italie',     'Italy'),
    ('LT', 'Lituanie',   'Lithuania'),
    ('LU', 'Luxembourg', 'Luxembourg'),
    ('LV', 'Lettonie',   'Latvia'),
    ('MT', 'Malte',      'Malta'),
    ('NL', 'Pays-Bas',   'Netherlands'),
    ('PT', 'Portugal',   'Portugal'),
    ('SI', 'Slovénie',   'Slovenia'),
    ('SK', 'Slovaquie',  'Slovakia');


-- Carriers (US-ADM-06)
CREATE TABLE carriers (
    id           UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    name         VARCHAR(100) NOT NULL,
    tracking_url VARCHAR(500) NOT NULL,
    active       BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at   TIMESTAMP    NOT NULL DEFAULT NOW()
);

CREATE TABLE carrier_countries (
    carrier_id   UUID       NOT NULL REFERENCES carriers(id) ON DELETE CASCADE,
    country_code VARCHAR(2) NOT NULL REFERENCES countries(code),
    PRIMARY KEY (carrier_id, country_code)
);

-- La Poste seed data
INSERT INTO carriers (id, name, tracking_url, active, created_at)
VALUES (gen_random_uuid(), 'La Poste', 'https://www.laposte.fr/outils/suivre-vos-envois', TRUE, NOW());

INSERT INTO carrier_countries (carrier_id, country_code)
SELECT id, 'FR' FROM carriers WHERE name = 'La Poste';


-- Security audit log (CS-15 / SEC-LOG-001 / CPA-13)
CREATE TABLE audit_log (
    id          BIGSERIAL    PRIMARY KEY,
    event_type  VARCHAR(50)  NOT NULL,
    email       VARCHAR(255),
    ip_address  VARCHAR(45),
    details     TEXT,
    occurred_at TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_audit_log_email       ON audit_log (email);
CREATE INDEX idx_audit_log_event_type  ON audit_log (event_type);
CREATE INDEX idx_audit_log_occurred_at ON audit_log (occurred_at);


-- Product catalog domain (US-CAT-01..05)

CREATE TABLE products (
    id                    UUID          PRIMARY KEY DEFAULT gen_random_uuid(),
    name                  VARCHAR(200)  NOT NULL,
    description           TEXT,
    price_excl_tax        NUMERIC(10,2) NOT NULL,
    category              VARCHAR(100),
    quantity              INTEGER       NOT NULL DEFAULT 0,
    stock_alert_threshold INTEGER       NOT NULL DEFAULT 0,
    status                VARCHAR(20)   NOT NULL DEFAULT 'PUBLISHED',
    created_at            TIMESTAMP     NOT NULL DEFAULT NOW(),
    updated_at            TIMESTAMP     NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_products_status    ON products (status);

CREATE TABLE product_photos (
    id         UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    product_id UUID         NOT NULL REFERENCES products(id) ON DELETE CASCADE,
    url        VARCHAR(500) NOT NULL,
    sort_order INTEGER      NOT NULL DEFAULT 0
);

CREATE INDEX idx_product_photos_product_id ON product_photos (product_id);

-- One alert record per threshold-crossing event (US-CAT-05)
CREATE TABLE stock_alerts (
    id           UUID      PRIMARY KEY DEFAULT gen_random_uuid(),
    product_id   UUID      NOT NULL REFERENCES products(id) ON DELETE CASCADE,
    triggered_at TIMESTAMP NOT NULL DEFAULT NOW(),
    acknowledged BOOLEAN   NOT NULL DEFAULT FALSE
);

CREATE INDEX idx_stock_alerts_product_id  ON stock_alerts (product_id);
CREATE INDEX idx_stock_alerts_acknowledged ON stock_alerts (acknowledged);


-- Cart domain (US-CRT-01, US-CRT-02)
-- One persistent cart per authenticated buyer, with individual line items.

CREATE TABLE cart (
    id         UUID      PRIMARY KEY DEFAULT gen_random_uuid(),
    buyer_id   UUID      NOT NULL REFERENCES accounts(id) ON DELETE CASCADE,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
    UNIQUE (buyer_id)
);

CREATE INDEX idx_cart_buyer_id ON cart (buyer_id);

CREATE TABLE cart_item (
    id         UUID    PRIMARY KEY DEFAULT gen_random_uuid(),
    cart_id    UUID    NOT NULL REFERENCES cart(id) ON DELETE CASCADE,
    product_id UUID    NOT NULL REFERENCES products(id),
    quantity   INTEGER NOT NULL CHECK (quantity > 0),
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
    UNIQUE (cart_id, product_id)
);

CREATE INDEX idx_cart_item_cart_id ON cart_item (cart_id);


-- Buyer delivery address book (US-PRF-03).
-- Soft-deleted rows (deleted = true) are hidden from the buyer but kept to preserve
-- referential integrity with orders that were placed using that address.

CREATE TABLE delivery_address (
    id           UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    account_id   UUID         NOT NULL REFERENCES accounts(id),
    label        VARCHAR(100) NOT NULL,
    address_line VARCHAR(255) NOT NULL,
    city         VARCHAR(100) NOT NULL,
    postal_code  VARCHAR(20)  NOT NULL,
    country_code VARCHAR(2)   NOT NULL REFERENCES countries(code),
    is_default   BOOLEAN      NOT NULL DEFAULT FALSE,
    deleted      BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at   TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_at   TIMESTAMP    NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_delivery_address_account_id ON delivery_address (account_id);


-- Order domain (US-ORD-01..05).
-- Carrier data is snapshotted at order creation to remain independent of later carrier edits.
-- Delivery address is referenced by FK (not copied) — soft-delete on delivery_address preserves integrity.

CREATE TABLE orders (
    id                       UUID          PRIMARY KEY DEFAULT gen_random_uuid(),
    order_number             VARCHAR(50)   NOT NULL UNIQUE,
    buyer_id                 UUID          NOT NULL REFERENCES accounts(id),
    delivery_address_id      UUID          NOT NULL REFERENCES delivery_address(id),
    carrier_id               UUID          NOT NULL REFERENCES carriers(id),
    carrier_name             VARCHAR(100)  NOT NULL,
    carrier_tracking_url     VARCHAR(500)  NOT NULL,
    payment_method           VARCHAR(20)   NOT NULL,
    status                   VARCHAR(50)   NOT NULL,
    total_amount_ttc         NUMERIC(10,2) NOT NULL,
    stripe_payment_intent_id VARCHAR(100),
    buyer_iban               VARCHAR(34),
    tracking_number          VARCHAR(100),
    cancellation_reason      VARCHAR(500),
    created_at               TIMESTAMP     NOT NULL DEFAULT NOW(),
    updated_at               TIMESTAMP     NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_orders_buyer_id    ON orders (buyer_id);
CREATE INDEX idx_orders_status      ON orders (status);
CREATE INDEX idx_orders_order_number ON orders (order_number);

CREATE TABLE order_lines (
    id                  UUID          PRIMARY KEY DEFAULT gen_random_uuid(),
    order_id            UUID          NOT NULL REFERENCES orders(id) ON DELETE CASCADE,
    product_id          UUID          REFERENCES products(id) ON DELETE SET NULL,
    product_name        VARCHAR(200)  NOT NULL,
    unit_price_excl_tax NUMERIC(10,2) NOT NULL,
    unit_price_ttc      NUMERIC(10,2) NOT NULL,
    quantity            INTEGER       NOT NULL CHECK (quantity > 0),
    line_total_ttc      NUMERIC(10,2) NOT NULL
);

CREATE INDEX idx_order_lines_order_id ON order_lines (order_id);


-- Platform settings (US-ADM-10 — maintenance mode toggle)
CREATE TABLE platform_settings (
    key   VARCHAR(100) NOT NULL PRIMARY KEY,
    value VARCHAR(500) NOT NULL
);

INSERT INTO platform_settings (key, value) VALUES ('maintenance_mode', 'false');
INSERT INTO platform_settings (key, value) VALUES ('shop_accent_color', '#4e8b82');


-- Scrolling announcements (US-ANN-01 — FS-V11)
-- image_orientation is auto-detected from image dimensions: LANDSCAPE if width > height, else PORTRAIT.

CREATE TABLE announcements (
    id                UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    vendor_id         UUID         NOT NULL REFERENCES accounts(id),
    content_type      VARCHAR(20)  NOT NULL,
    text_content      VARCHAR(500),
    image_url         VARCHAR(500),
    image_orientation VARCHAR(20),
    redirect_url      VARCHAR(500),
    sort_order        INTEGER      NOT NULL DEFAULT 0,
    active            BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at        TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_at        TIMESTAMP    NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_announcements_vendor_id  ON announcements (vendor_id);
CREATE INDEX idx_announcements_active     ON announcements (active);


