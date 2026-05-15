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

