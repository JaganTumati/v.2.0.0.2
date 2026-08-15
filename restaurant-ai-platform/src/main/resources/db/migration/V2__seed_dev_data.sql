-- ============================================================
-- V2__seed_dev_data.sql
-- Seeds one restaurant (matching DEFAULT_RESTAURANT_ID in application.yml)
-- plus enough menu data to exercise the AI -> tool -> DB -> Claude loop.
-- Not exhaustive (Phase 24 mock mode will expand this to 30+ items).
-- ============================================================

INSERT INTO restaurants (id, name, cuisine_type, phone, currency, status, address_line, city, state, postal_code, country)
VALUES (
    '00000000-0000-0000-0000-000000000001',
    'Spice Route Kitchen',
    'Indian',
    '+91-9000000000',
    'INR',
    'OPEN',
    '12-3 Main Road',
    'Jaggayyapeta',
    'Andhra Pradesh',
    '521175',
    'India'
);

INSERT INTO restaurant_hours (restaurant_id, day_of_week, open_time, close_time, is_closed)
SELECT '00000000-0000-0000-0000-000000000001', d, '11:00', '22:30', false
FROM generate_series(0, 6) AS d;

-- Menu categories
INSERT INTO menu_categories (id, restaurant_id, name, display_order) VALUES
    ('10000000-0000-0000-0000-000000000001', '00000000-0000-0000-0000-000000000001', 'Biryani', 1),
    ('10000000-0000-0000-0000-000000000002', '00000000-0000-0000-0000-000000000001', 'Starters', 2),
    ('10000000-0000-0000-0000-000000000003', '00000000-0000-0000-0000-000000000001', 'Beverages', 3);

-- Menu items
INSERT INTO menu_items (id, restaurant_id, category_id, name, description, price, vegetarian, spicy_level, available) VALUES
    ('20000000-0000-0000-0000-000000000001', '00000000-0000-0000-0000-000000000001', '10000000-0000-0000-0000-000000000001',
     'Chicken Biryani', 'Aromatic basmati rice layered with spiced chicken', 249.00, false, 2, true),
    ('20000000-0000-0000-0000-000000000002', '00000000-0000-0000-0000-000000000001', '10000000-0000-0000-0000-000000000001',
     'Veg Biryani', 'Basmati rice with mixed vegetables and spices', 199.00, true, 1, true),
    ('20000000-0000-0000-0000-000000000003', '00000000-0000-0000-0000-000000000001', '10000000-0000-0000-0000-000000000002',
     'Chicken 65', 'Deep-fried spicy chicken bites', 189.00, false, 3, true),
    ('20000000-0000-0000-0000-000000000004', '00000000-0000-0000-0000-000000000001', '10000000-0000-0000-0000-000000000002',
     'Paneer Tikka', 'Grilled marinated cottage cheese skewers', 179.00, true, 2, true),
    ('20000000-0000-0000-0000-000000000005', '00000000-0000-0000-0000-000000000001', '10000000-0000-0000-0000-000000000003',
     'Pepsi', '300ml chilled soft drink', 60.00, true, 0, true),
    ('20000000-0000-0000-0000-000000000006', '00000000-0000-0000-0000-000000000001', '10000000-0000-0000-0000-000000000003',
     'Sweet Lassi', 'Traditional yogurt-based sweet drink', 80.00, true, 0, true);

INSERT INTO inventory (menu_item_id, track_stock, stock_count)
SELECT id, false, NULL FROM menu_items WHERE restaurant_id = '00000000-0000-0000-0000-000000000001';

-- Menu item options
INSERT INTO menu_item_options (menu_item_id, name, price_delta) VALUES
    ('20000000-0000-0000-0000-000000000001', 'Half', -60.00),
    ('20000000-0000-0000-0000-000000000001', 'Full', 0.00),
    ('20000000-0000-0000-0000-000000000001', 'Extra Spicy', 0.00);

-- Delivery zone
INSERT INTO delivery_zones (id, restaurant_id, name, postal_codes, delivery_fee, min_order_amount, estimated_minutes, active)
VALUES (
    '30000000-0000-0000-0000-000000000001',
    '00000000-0000-0000-0000-000000000001',
    'Jaggayyapeta Local',
    ARRAY['521175'],
    40.00,
    150.00,
    35,
    true
);

-- A promotion for testing get_promotions
INSERT INTO promotions (restaurant_id, code, description, discount_type, discount_value, valid_from, valid_until, active)
VALUES (
    '00000000-0000-0000-0000-000000000001',
    'WELCOME10',
    '10% off for first-time WhatsApp orders',
    'PERCENT',
    10.00,
    now() - interval '1 day',
    now() + interval '90 days',
    true
);

-- Knowledge base entries
INSERT INTO knowledge_documents (restaurant_id, title, category, content, active) VALUES
    ('00000000-0000-0000-0000-000000000001', 'Refund Policy', 'REFUND',
     'Refunds are issued to the original payment method within 3-5 business days if an order is cancelled before preparation begins, or if an item is confirmed missing/incorrect by our team.', true),
    ('00000000-0000-0000-0000-000000000001', 'Allergy Information', 'ALLERGY',
     'Our kitchen handles nuts, dairy, and gluten. We cannot guarantee zero cross-contact. Customers with severe allergies should confirm directly with our team before ordering.', true);

-- Note: the default dev/owner user is now seeded by DevUserSeeder at application
-- startup instead of here, since a real BCrypt hash needs Spring Security's
-- PasswordEncoder to generate correctly (see Phase 19 / config.AsyncConfig sibling
-- DevUserSeeder for details and the logged one-time dev password).
