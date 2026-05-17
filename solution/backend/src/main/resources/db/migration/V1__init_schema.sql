-- V1: initial schema — account domain (US-ADM-01)

CREATE TABLE accounts (
    id                   UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    email                VARCHAR(255) NOT NULL UNIQUE,
    password_hash        VARCHAR(255),
    first_name           VARCHAR(100) NOT NULL,
    last_name            VARCHAR(100) NOT NULL,
    role                 VARCHAR(20)  NOT NULL,
    status               VARCHAR(20)  NOT NULL,
    language             VARCHAR(2)   NOT NULL DEFAULT 'FR',
    must_change_password BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at           TIMESTAMP    NOT NULL DEFAULT NOW()
);

INSERT INTO accounts (id, email, password_hash, first_name, last_name, role, status, must_change_password, created_at)
VALUES (
  gen_random_uuid(),
  'admin@onlineshop.com',
  '$2a$10$eKsMdkG5gSwl1oclTnBad.NVyVPzDaFxT7tZt1TuRFyCuEMQJvHSm', -- admin
  'Admin',
  'System',
  'ADMIN',
  'ACTIVE',
  TRUE,
  NOW()
);


-- Activation tokens for admin-created accounts (US-ADM-01) and buyer self-registration (US-REG-01)
CREATE TABLE activation_tokens (
    token      VARCHAR(36)  PRIMARY KEY,
    account_id UUID         NOT NULL REFERENCES accounts(id) ON DELETE CASCADE,
    expires_at TIMESTAMP    NOT NULL
);


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

