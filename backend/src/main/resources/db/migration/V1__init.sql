CREATE TYPE user_role AS ENUM(
    'GUARDIAN',
    'CONSULTANT',
    'PERFORMER'
);

CREATE TYPE job_status AS ENUM  (
    'PENDING',
    'DONE',
    'REQUESTED',
    'CANCELED'
);
CREATE TABLE clients(
                        id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                        firstname TEXT NOT NULL,
                        Lastname TEXT NOT NULL,
                        phone_number TEXT NOT NULL UNIQUE,
                        address TEXT NOT NULL
);

CREATE TABLE users(
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    password text NOT NULL ,
    email TEXT NOT NULL UNIQUE,
    firstname TEXT NOT NULL,
    Lastname TEXT NOT NULL,
    phone_number TEXT NOT NULL UNIQUE,
    role user_role,
    created_at TIMESTAMP NOT NULL DEFAULT now()
);

CREATE TABLE jobs(
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    client_id BIGINT NOT NULL references clients (id),
    job_orderer_id BIGINT references users (id),
    status job_status,
    address TEXT NOT NULL,
    performer_id BIGINT references users (id),
    price numeric,
    date TIMESTAMP,
    security_code INTEGER NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT now()
);


