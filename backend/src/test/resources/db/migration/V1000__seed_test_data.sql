-- city
INSERT INTO city (name) VALUES
    ('Lublin'),   -- 1
    ('Chełm'),    -- 2
    ('Zamość');   -- 3

-- district
INSERT INTO district (city_id, name) VALUES
    (1, 'Wieniawa'),  -- 1
    (1, 'Czechów'),   -- 2
    (2, 'Centrum');   -- 3

-- service_category
INSERT INTO service_category (name, active) VALUES
    ('Hydraulik', true),   -- 1
    ('Elektryk', true),    -- 2
    ('Kominiarz', false);  -- 3

-- account
INSERT INTO account (email, password_hash, first_name, last_name, phone) VALUES
    ('seed.anna@example.com', 'hash', 'Anna', 'Kowalska', '+48600000001'),  -- 1
    ('seed.jan@example.com', 'hash', 'Jan', 'Nowak', '+48600000002');       -- 2

-- client
INSERT INTO client (first_name, last_name, phone, street, house_number, apartment_number, postal_code, district_id) VALUES
    ('Janina', 'Nowak', '+48600123456', 'ul. Lipowa', '12A', '5', '20-001', 1),     -- 1
    ('Zofia', 'Wiśniewska', '+48600123457', 'ul. Długa', '3', NULL, '20-002', 2),   -- 2
    ('Henryk', 'Zieliński', '+48600123458', 'ul. Krótka', '7', NULL, '22-100', 3),  -- 3
    ('Maria', 'Wójcik', '+48600123459', 'ul. Polna', '1', NULL, '20-003', 1);      -- 4

-- service_request
INSERT INTO service_request (client_id, category_id, description, preferred_from, preferred_to, notify_target,
                             estimate_min_minor, estimate_max_minor, estimated_at, expires_at, status, channel,
                             created_by, created_at, updated_at) VALUES
    (1, 1, 'Cieknie kran w kuchni, trzeba wymienić uszczelkę.', '2026-12-01T08:00:00Z', '2026-12-01T14:00:00Z', 'CLIENT',
     NULL, NULL, NULL, '2026-12-04T10:00:00Z', 'NEW', 'WEB_FORM', 1, '2026-11-20T10:00:00Z', '2026-11-20T10:00:00Z'),        -- 1
    (2, 2, 'Nie działa gniazdko w łazience.', NULL, NULL, 'CAREGIVER',
     8000, 15000, '2026-11-21T12:00:00Z', '2026-12-05T10:00:00Z', 'PRICED', 'WEB_FORM', 1, '2026-11-21T10:00:00Z', '2026-11-21T12:00:00Z'), -- 2
    (3, 1, 'Zapchany odpływ w wannie.', NULL, NULL, 'CLIENT',
     NULL, NULL, NULL, '2026-12-06T10:00:00Z', 'ACCEPTED', 'WEB_FORM', 2, '2026-11-22T10:00:00Z', '2026-11-22T10:00:00Z'),   -- 3
    (4, 1, 'Kapie woda spod zlewu.', NULL, NULL, 'CLIENT',
     NULL, NULL, NULL, '2026-11-01T10:00:00Z', 'NEW', 'WEB_FORM', NULL, '2026-10-18T10:00:00Z', '2026-10-18T10:00:00Z');    -- 4
