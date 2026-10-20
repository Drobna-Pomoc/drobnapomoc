CREATE TYPE user_role AS ENUM(
    'GUARDIAN',
    'CONSULTANT',
    'PERFORMER'
);

CREATE TYPE job_status AS ENUM  (
    'PENDING',
    'REQUESTED',
    'DONE',
    'COMPLETED',
    'CANCELED'
);

CREATE TYPE job_category AS ENUM (
    'SHOPPING',
    'MINOR REPAIRS',
    'HOME SUPPORT',
    'SUPPORT WITH VISITS',
    'TECHNICAL SUPPORT',
    'JUST HELP'
);

CREATE TYPE job_payment_method AS ENUM (
    'CASH',
    'ONLINE'
);

CREATE TABLE clients(
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    firstname TEXT NOT NULL,
    lastname TEXT NOT NULL,
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
    role user_role NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT now()
);

CREATE TABLE jobs(
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    client_id BIGINT NOT NULL references clients (id),
    job_orderer_id BIGINT references users (id),
    status job_status NOT NULL,
    address TEXT NOT NULL,
    performer_id BIGINT references users (id),
    price NUMERIC,
    estimated_price NUMERIC,
    payment_method job_payment_method,
    date TIMESTAMP,
    security_code INTEGER NOT NULL,
    category job_category NOT NULL ,
    created_at TIMESTAMP NOT NULL DEFAULT now()
);


