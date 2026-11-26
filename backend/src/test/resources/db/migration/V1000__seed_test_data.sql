INSERT INTO city (name) VALUES
    ('Lublin'),   -- 1
    ('Chełm'),    -- 2
    ('Zamość');   -- 3

INSERT INTO district (city_id, name) VALUES
    (1, 'Wieniawa'),  -- 1
    (1, 'Czechów'),   -- 2
    (2, 'Centrum');   -- 3

INSERT INTO service_category (name, active) VALUES
    ('Hydraulik', true),   -- 1
    ('Elektryk', true),    -- 2
    ('Kominiarz', false);  -- 3
