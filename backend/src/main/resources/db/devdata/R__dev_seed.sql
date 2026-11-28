-- service_category
INSERT INTO service_category (name, active) VALUES
    ('Hydraulik', true),
    ('Elektryk', true),
    ('Kominiarz', false)
ON CONFLICT DO NOTHING;

-- city
INSERT INTO city (name) VALUES
    ('Warszawa')
ON CONFLICT DO NOTHING;

-- district
INSERT INTO district (city_id, name) VALUES
    ((SELECT id FROM city WHERE name = 'Warszawa'), 'Bemowo'),
    ((SELECT id FROM city WHERE name = 'Warszawa'), 'Białołęka'),
    ((SELECT id FROM city WHERE name = 'Warszawa'), 'Bielany'),
    ((SELECT id FROM city WHERE name = 'Warszawa'), 'Mokotów'),
    ((SELECT id FROM city WHERE name = 'Warszawa'), 'Ochota'),
    ((SELECT id FROM city WHERE name = 'Warszawa'), 'Praga-Południe'),
    ((SELECT id FROM city WHERE name = 'Warszawa'), 'Praga-Północ'),
    ((SELECT id FROM city WHERE name = 'Warszawa'), 'Rembertów'),
    ((SELECT id FROM city WHERE name = 'Warszawa'), 'Śródmieście'),
    ((SELECT id FROM city WHERE name = 'Warszawa'), 'Targówek'),
    ((SELECT id FROM city WHERE name = 'Warszawa'), 'Ursus'),
    ((SELECT id FROM city WHERE name = 'Warszawa'), 'Ursynów'),
    ((SELECT id FROM city WHERE name = 'Warszawa'), 'Wawer'),
    ((SELECT id FROM city WHERE name = 'Warszawa'), 'Wesoła'),
    ((SELECT id FROM city WHERE name = 'Warszawa'), 'Wilanów'),
    ((SELECT id FROM city WHERE name = 'Warszawa'), 'Włochy'),
    ((SELECT id FROM city WHERE name = 'Warszawa'), 'Wola'),
    ((SELECT id FROM city WHERE name = 'Warszawa'), 'Żoliborz')
ON CONFLICT DO NOTHING;

-- password for every account: haslo123
CREATE EXTENSION IF NOT EXISTS pgcrypto;

-- account
INSERT INTO account (email, password_hash, first_name, last_name, phone) VALUES
    ('client@drobnapomoc.dev',     crypt('haslo123', gen_salt('bf', 10)), 'Anna',    'Klient',     '+48600000001'),
    ('caregiver@drobnapomoc.dev',  crypt('haslo123', gen_salt('bf', 10)), 'Barbara', 'Opiekun',    '+48600000002'),
    ('contractor@drobnapomoc.dev', crypt('haslo123', gen_salt('bf', 10)), 'Czesław', 'Fachowiec',  '+48600000003'),
    ('consultant@drobnapomoc.dev', crypt('haslo123', gen_salt('bf', 10)), 'Dorota',  'Konsultant', '+48600000004'),
    ('admin@drobnapomoc.dev',      crypt('haslo123', gen_salt('bf', 10)), 'Edward',  'Admin',      '+48600000005')
ON CONFLICT DO NOTHING;

-- account_role
INSERT INTO account_role (account_id, role) VALUES
    ((SELECT id FROM account WHERE email = 'client@drobnapomoc.dev'), 'CLIENT'),
    ((SELECT id FROM account WHERE email = 'caregiver@drobnapomoc.dev'), 'CAREGIVER'),
    ((SELECT id FROM account WHERE email = 'contractor@drobnapomoc.dev'), 'CONTRACTOR'),
    ((SELECT id FROM account WHERE email = 'consultant@drobnapomoc.dev'), 'CONSULTANT'),
    ((SELECT id FROM account WHERE email = 'admin@drobnapomoc.dev'), 'CLIENT'),
    ((SELECT id FROM account WHERE email = 'admin@drobnapomoc.dev'), 'CAREGIVER'),
    ((SELECT id FROM account WHERE email = 'admin@drobnapomoc.dev'), 'CONTRACTOR'),
    ((SELECT id FROM account WHERE email = 'admin@drobnapomoc.dev'), 'CONSULTANT'),
    ((SELECT id FROM account WHERE email = 'admin@drobnapomoc.dev'), 'ADMIN')
ON CONFLICT DO NOTHING;

-- contractor_profile
INSERT INTO contractor_profile (account_id, verification_status, bio, verified_by, verified_at) VALUES
    (
        (SELECT id FROM account WHERE email = 'contractor@drobnapomoc.dev'),
        'APPROVED',
        'Doświadczony hydraulik i elektryk, szybkie terminy.',
        (SELECT id FROM account WHERE email = 'admin@drobnapomoc.dev'),
        now()
    ),
    (
        (SELECT id FROM account WHERE email = 'admin@drobnapomoc.dev'),
        'APPROVED',
        'Konto testowe administratora z rolą wykonawcy.',
        (SELECT id FROM account WHERE email = 'admin@drobnapomoc.dev'),
        now()
    )
ON CONFLICT DO NOTHING;

-- contractor_category
INSERT INTO contractor_category (contractor_id, category_id) VALUES
    ((SELECT id FROM account WHERE email = 'contractor@drobnapomoc.dev'), (SELECT id FROM service_category WHERE name = 'Hydraulik')),
    ((SELECT id FROM account WHERE email = 'contractor@drobnapomoc.dev'), (SELECT id FROM service_category WHERE name = 'Elektryk')),
    ((SELECT id FROM account WHERE email = 'admin@drobnapomoc.dev'), (SELECT id FROM service_category WHERE name = 'Hydraulik')),
    ((SELECT id FROM account WHERE email = 'admin@drobnapomoc.dev'), (SELECT id FROM service_category WHERE name = 'Elektryk'))
ON CONFLICT DO NOTHING;

-- contractor_district
INSERT INTO contractor_district (contractor_id, district_id) VALUES
    ((SELECT id FROM account WHERE email = 'contractor@drobnapomoc.dev'), (SELECT id FROM district WHERE name = 'Mokotów')),
    ((SELECT id FROM account WHERE email = 'contractor@drobnapomoc.dev'), (SELECT id FROM district WHERE name = 'Śródmieście')),
    ((SELECT id FROM account WHERE email = 'contractor@drobnapomoc.dev'), (SELECT id FROM district WHERE name = 'Wola')),
    ((SELECT id FROM account WHERE email = 'admin@drobnapomoc.dev'), (SELECT id FROM district WHERE name = 'Mokotów')),
    ((SELECT id FROM account WHERE email = 'admin@drobnapomoc.dev'), (SELECT id FROM district WHERE name = 'Śródmieście')),
    ((SELECT id FROM account WHERE email = 'admin@drobnapomoc.dev'), (SELECT id FROM district WHERE name = 'Wola'))
ON CONFLICT DO NOTHING;
