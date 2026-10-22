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

CREATE INDEX district_city_idx on district (city_id);

CREATE TABLE service_category (
    id      bigint GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    name    text NOT NULL UNIQUE,
    active  boolean NOT NULL DEFAULT true
);

CREATE TYPE account_type AS ENUM (
    'CAREGIVER',
    'CONTRACTOR',
    'CONSULTANT'
);

CREATE TYPE account_status AS ENUM (
    'ACTIVE',
    'SUSPENDED'
);

CREATE TABLE account (
     id             bigint GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
     email          text NOT NULL,
     password_hash  text NOT NULL,
     first_name     text NOT NULL,
     last_name      text NOT NULL,
     phone          text NOT NULL,
     type           account_type NOT NULL,
     status         account_status NOT NULL,
     created_at     timestamptz NOT NULL DEFAULT now()
);

CREATE UNIQUE INDEX account_email_uq ON account (lower(email));

CREATE TABLE client (
    id                bigint GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    first_name        text NOT NULL,
    last_name         text NOT NULL,
    phone             text NOT NULL,
    street            text NOT NULL,
    house_number      text NOT NULL,
    apartment_number  text,
    postal_code       text NOT NULL,
    district_id       bigint NOT NULL REFERENCES district (id),
    created_at        timestamptz NOT NULL DEFAULT now()
);

CREATE INDEX client_phone_idx ON client (phone);
CREATE INDEX client_district_idx ON client (district_id);

CREATE TYPE service_request_status AS ENUM (
    'NEW',
    'PRICED',
    'ACCEPTED',
    'IN_PROGRESS',
    'COMPLETION_REPORTED',
    'COMPLETED',
    'DISPUTED',
    'CANCELLED'
);

CREATE TABLE service_request (
     id               bigint GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
     client_id        bigint NOT NULL REFERENCES client (id),
     category_id      bigint NOT NULL REFERENCES service_category (id),
     description      text NOT NULL,
     status           service_request_status NOT NULL DEFAULT 'NEW',
     created_by       bigint REFERENCES account (id), -- NULL if created by client
     cancel_reason    text,
     created_at       timestamptz NOT NULL DEFAULT now(),
     updated_at       timestamptz NOT NULL DEFAULT now()
);

CREATE INDEX service_request_open_idx ON service_request (client_id, created_at DESC) WHERE status IN ('NEW', 'PRICED');
CREATE INDEX service_request_status_idx ON service_request (status, created_at DESC);
CREATE INDEX service_request_category_idx ON service_request (category_id);
CREATE INDEX service_request_creator_idx ON service_request (created_by, created_at DESC);