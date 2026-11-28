CREATE TYPE contractor_verification_status AS ENUM (
    'PENDING',
    'APPROVED',
    'REJECTED',
    'SUSPENDED'
);

CREATE TABLE contractor_profile (
    account_id           bigint PRIMARY KEY REFERENCES account (id),
    verification_status  contractor_verification_status NOT NULL DEFAULT 'PENDING',
    bio                  text,
    application_note     text,
    rejection_reason     text,
    verified_by          bigint REFERENCES account (id),
    verified_at          timestamptz,
    submitted_at         timestamptz NOT NULL DEFAULT now(),
    CONSTRAINT contractor_profile_verification_chk
        CHECK (
            verification_status = 'PENDING'
            OR (verified_by IS NOT NULL AND verified_at IS NOT NULL)
        ),
    CONSTRAINT contractor_profile_rejection_reason_chk
        CHECK (
            verification_status <> 'REJECTED'
            OR rejection_reason IS NOT NULL
        )
);

CREATE INDEX contractor_profile_status_submitted_idx
    ON contractor_profile (verification_status, submitted_at);
CREATE INDEX contractor_profile_verified_by_idx
    ON contractor_profile (verified_by);

CREATE TABLE contractor_district (
    contractor_id  bigint NOT NULL REFERENCES contractor_profile (account_id),
    district_id    bigint NOT NULL REFERENCES district (id),
    PRIMARY KEY (contractor_id, district_id)
);

CREATE INDEX contractor_district_district_idx
    ON contractor_district (district_id);

CREATE TABLE contractor_category (
    contractor_id  bigint NOT NULL REFERENCES contractor_profile (account_id),
    category_id    bigint NOT NULL REFERENCES service_category (id),
    PRIMARY KEY (contractor_id, category_id)
);

CREATE INDEX contractor_category_category_idx
    ON contractor_category (category_id);

CREATE TYPE offer_status AS ENUM (
    'SUBMITTED',
    'ACCEPTED',
    'REJECTED',
    'WITHDRAWN',
    'CANCELLED'
);

CREATE TYPE decision_channel AS ENUM (
    'LINK',
    'APP',
    'CONSULTANT'
);

CREATE TABLE offer (
    id                          bigint GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    request_id                  bigint NOT NULL REFERENCES service_request (id),
    contractor_id               bigint NOT NULL REFERENCES contractor_profile (account_id),
    price_minor                 bigint NOT NULL,
    proposed_start              timestamptz NOT NULL,
    estimated_duration_minutes  integer,
    message                     text,
    status                      offer_status NOT NULL DEFAULT 'SUBMITTED',
    submitted_at                timestamptz NOT NULL DEFAULT now(),
    decided_at                  timestamptz,
    decided_via                 decision_channel,
    CONSTRAINT offer_price_positive_chk
        CHECK (price_minor > 0),
    CONSTRAINT offer_duration_positive_chk
        CHECK (estimated_duration_minutes IS NULL OR estimated_duration_minutes > 0),
    CONSTRAINT offer_decision_consistency_chk
        CHECK (
            (status = 'SUBMITTED' AND decided_at IS NULL AND decided_via IS NULL)
            OR (status = 'ACCEPTED' AND decided_at IS NOT NULL AND decided_via IS NOT NULL)
            OR (status IN ('REJECTED', 'WITHDRAWN', 'CANCELLED') AND decided_at IS NOT NULL)
        ),
    CONSTRAINT offer_id_request_uq UNIQUE (id, request_id),
    CONSTRAINT offer_id_contractor_uq UNIQUE (id, contractor_id)
);

CREATE INDEX offer_request_status_idx ON offer (request_id, status);
CREATE INDEX offer_contractor_status_idx ON offer (contractor_id, status);

CREATE UNIQUE INDEX offer_one_accepted_per_request_uq
    ON offer (request_id)
    WHERE status = 'ACCEPTED';

CREATE UNIQUE INDEX offer_one_submitted_per_contractor_uq
    ON offer (request_id, contractor_id)
    WHERE status = 'SUBMITTED';
