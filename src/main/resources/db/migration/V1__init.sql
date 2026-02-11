-- V1: Initial schema – users, roles, auth tokens, resources, reservations
-- Platform: Resource Management and Reservation (banking context)

-- ============================================================
-- Extensions
-- ============================================================
CREATE EXTENSION IF NOT EXISTS "pgcrypto";   -- gen_random_uuid()
CREATE EXTENSION IF NOT EXISTS "btree_gist"; -- EXCLUDE constraint for range overlap

-- ============================================================
-- 1. Users
-- ============================================================
CREATE TABLE app_user (
    id            UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    email         VARCHAR(255) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    first_name    VARCHAR(100),
    last_name     VARCHAR(100),
    enabled       BOOLEAN   NOT NULL DEFAULT TRUE,
    created_at    TIMESTAMP NOT NULL DEFAULT now(),
    updated_at    TIMESTAMP NOT NULL DEFAULT now(),

    CONSTRAINT uq_app_user_email UNIQUE (email)
);

-- ============================================================
-- 2. Roles
-- ============================================================
CREATE TABLE role (
    id   SERIAL      PRIMARY KEY,
    name VARCHAR(50) NOT NULL,

    CONSTRAINT uq_role_name UNIQUE (name)
);

INSERT INTO role (name) VALUES
    ('USER'),
    ('EMPLOYEE'),
    ('MANAGER'),
    ('HEAD_OF_OPERATIONS'),
    ('ADMIN');

-- ============================================================
-- 3. User ↔ Role (many-to-many)
-- ============================================================
CREATE TABLE user_role (
    user_id UUID    NOT NULL REFERENCES app_user(id) ON DELETE CASCADE,
    role_id INTEGER NOT NULL REFERENCES role(id)     ON DELETE CASCADE,

    PRIMARY KEY (user_id, role_id)
);

-- ============================================================
-- 4. Refresh tokens (JWT rotation & revocation)
-- ============================================================
CREATE TABLE refresh_token (
    id         UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id    UUID         NOT NULL REFERENCES app_user(id) ON DELETE CASCADE,
    token_hash VARCHAR(128) NOT NULL,
    expires_at TIMESTAMP    NOT NULL,
    revoked    BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP    NOT NULL DEFAULT now(),

    CONSTRAINT uq_refresh_token_hash UNIQUE (token_hash)
);

CREATE INDEX idx_refresh_token_user ON refresh_token(user_id);

-- ============================================================
-- 5. Resource types (banking-context catalogue)
-- ============================================================
CREATE TABLE resource_type (
    id          SERIAL       PRIMARY KEY,
    name        VARCHAR(100) NOT NULL,
    description TEXT,

    CONSTRAINT uq_resource_type_name UNIQUE (name)
);

INSERT INTO resource_type (name, description) VALUES
    ('ROOM',                   'Sala de uso general'),
    ('CONFERENCE_ROOM',        'Sala de conferencias'),
    ('VIP_ROOM',               'Sala VIP para ejecutivos y clientes preferenciales'),
    ('CORPORATE_VEHICLE',      'Vehículo corporativo'),
    ('SHARED_TECH_EQUIPMENT',  'Equipo tecnológico compartido'),
    ('BILL_COUNTING_MACHINE',  'Máquina contadora de billetes');

-- ============================================================
-- 6. Resources
-- ============================================================
CREATE TABLE resource (
    id               UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name             VARCHAR(255) NOT NULL,
    description      TEXT,
    resource_type_id INTEGER      NOT NULL REFERENCES resource_type(id),
    location         VARCHAR(255),
    active           BOOLEAN   NOT NULL DEFAULT TRUE,
    created_at       TIMESTAMP NOT NULL DEFAULT now(),
    updated_at       TIMESTAMP NOT NULL DEFAULT now(),

    CONSTRAINT uq_resource_name UNIQUE (name)
);

CREATE INDEX idx_resource_type ON resource(resource_type_id);

-- ============================================================
-- 7. Reservations
-- ============================================================
CREATE TABLE reservation (
    id            UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    resource_id   UUID      NOT NULL REFERENCES resource(id),
    user_id       UUID      NOT NULL REFERENCES app_user(id),
    start_time    TIMESTAMP NOT NULL,
    end_time      TIMESTAMP NOT NULL,
    status        VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    notes         TEXT,
    cancelled_by  UUID      REFERENCES app_user(id),
    cancelled_at  TIMESTAMP,
    created_at    TIMESTAMP NOT NULL DEFAULT now(),
    updated_at    TIMESTAMP NOT NULL DEFAULT now(),

    CONSTRAINT chk_reservation_time   CHECK (end_time > start_time),
    CONSTRAINT chk_reservation_status CHECK (status IN ('ACTIVE', 'CANCELLED', 'COMPLETED'))
);

-- Prevent overlapping ACTIVE reservations for the same resource at database level
-- Uses tsrange + GiST exclusion constraint
CREATE EXCLUDE USING gist (
    resource_id WITH =,
    tsrange(start_time, end_time, '[)') WITH &&
) WHERE (status = 'ACTIVE');

CREATE INDEX idx_reservation_resource ON reservation(resource_id);
CREATE INDEX idx_reservation_user     ON reservation(user_id);
CREATE INDEX idx_reservation_status   ON reservation(status);
CREATE INDEX idx_reservation_times    ON reservation(start_time, end_time);
