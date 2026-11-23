CREATE TABLE city (
    id    bigint GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    name  text NOT NULL UNIQUE
);

CREATE TABLE district (
    id       bigint GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    city_id  bigint NOT NULL REFERENCES city (id),
    name     text NOT NULL,
    UNIQUE (city_id, name)
);

CREATE INDEX district_city_idx ON district (city_id);

CREATE TABLE service_category (
    id      bigint GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    name    text NOT NULL UNIQUE,
    active  boolean NOT NULL DEFAULT true
);

CREATE TYPE account_status AS ENUM (
    'ACTIVE',
    'SUSPENDED'
);

CREATE TABLE account (
    id                  bigint GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    email               text NOT NULL,
    password_hash       text NOT NULL,
    first_name          text NOT NULL,
    last_name           text NOT NULL,
    phone               text NOT NULL,
    status              account_status NOT NULL DEFAULT 'ACTIVE',
    created_at          timestamptz NOT NULL DEFAULT now(),
    anonymized_at       timestamptz
);

CREATE UNIQUE INDEX account_email_uq ON account (lower(email));

CREATE TYPE account_role_type AS ENUM (
    'CLIENT',
    'CAREGIVER',
    'CONTRACTOR',
    'CONSULTANT',
    'ADMIN'
);

CREATE TABLE account_role (
    account_id  bigint NOT NULL REFERENCES account (id),
    role        account_role_type NOT NULL,
    created_at  timestamptz NOT NULL DEFAULT now(),
    PRIMARY KEY (account_id, role)
);

CREATE TABLE client (
    id                bigint GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    account_id        bigint UNIQUE REFERENCES account (id),
    first_name        text NOT NULL,
    last_name         text NOT NULL,
    phone             text NOT NULL,
    street            text NOT NULL,
    house_number      text NOT NULL,
    apartment_number  text,
    postal_code       text NOT NULL,
    district_id       bigint NOT NULL REFERENCES district (id),
    created_at        timestamptz NOT NULL DEFAULT now(),
    anonymized_at     timestamptz
);

CREATE INDEX client_phone_idx ON client (phone);
CREATE INDEX client_district_idx ON client (district_id);

CREATE TYPE request_channel AS ENUM (
    'WEB_FORM',
    'PHONE'
);

CREATE TYPE service_request_status AS ENUM (
    'NEW',
    'PRICED',
    'ACCEPTED',
    'IN_PROGRESS',
    'WORK_CONFIRMED',
    'COMPLETED',
    'DISPUTED',
    'UNDER_REVIEW',
    'EXPIRED',
    'CANCELLED'
);

CREATE TYPE notify_target AS ENUM (
    'CLIENT',
    'CAREGIVER'
);

CREATE TABLE service_request (
     id                  bigint GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
     client_id           bigint NOT NULL REFERENCES client (id),
     category_id         bigint NOT NULL REFERENCES service_category (id),
     description         text NOT NULL,
     preferred_from      timestamptz,
     preferred_to        timestamptz,
     notify_target       notify_target NOT NULL DEFAULT 'CLIENT',
     estimate_min_minor  bigint,
     estimate_max_minor  bigint,
     estimated_at        timestamptz,
     expires_at          timestamptz NOT NULL,
     status              service_request_status NOT NULL DEFAULT 'NEW',
     channel             request_channel NOT NULL,
     created_by          bigint REFERENCES account (id),
     cancel_reason       text,
     created_at          timestamptz NOT NULL DEFAULT now(),
     updated_at          timestamptz NOT NULL DEFAULT now(),
     CONSTRAINT service_request_phone_creator_chk
         CHECK (channel <> 'PHONE' OR created_by IS NOT NULL),
     CONSTRAINT service_request_preferred_range_chk
         CHECK (preferred_to IS NULL OR preferred_from IS NULL OR preferred_to >= preferred_from),
     CONSTRAINT service_request_estimate_chk
         CHECK (
             (estimate_min_minor IS NULL AND estimate_max_minor IS NULL AND estimated_at IS NULL)
             OR (estimate_min_minor IS NOT NULL AND estimate_max_minor IS NOT NULL AND estimated_at IS NOT NULL
                 AND estimate_min_minor > 0 AND estimate_max_minor >= estimate_min_minor)
         )
);

CREATE INDEX service_request_client_idx ON service_request (client_id, created_at DESC);
CREATE INDEX service_request_open_idx ON service_request (category_id, created_at DESC) WHERE status IN ('NEW', 'PRICED');
CREATE INDEX service_request_status_idx ON service_request (status, created_at DESC);
CREATE INDEX service_request_category_idx ON service_request (category_id);
CREATE INDEX service_request_creator_idx ON service_request (created_by, created_at DESC);
CREATE INDEX service_request_expiry_idx ON service_request (status, expires_at);
