-- V1: initial schema — account domain (US-ADM-01)

CREATE TABLE accounts (
    id            UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    email         VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    first_name    VARCHAR(100) NOT NULL,
    last_name     VARCHAR(100) NOT NULL,
    role          VARCHAR(20)  NOT NULL,
    status        VARCHAR(20)  NOT NULL,
    created_at    TIMESTAMP    NOT NULL DEFAULT NOW()
);

INSERT INTO accounts (id, email, password_hash, first_name, last_name, role, status, created_at)
VALUES (
  gen_random_uuid(),
  'admin@onlineshop.com',
  '$2a$10$ChRjfMxH2qKlmI5L.uqmV.SXW8NKJI59ML0gLTbPjXvObG2.lEOD6',
  'Admin',
  'System',
  'ADMIN',
  'ACTIVE',
  NOW()
);
