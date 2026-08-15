-- ============================================================
-- V1__init_schema.sql
-- Single-restaurant MVP, but every tenant-owned table carries
-- restaurant_id + an index on it, so multi-tenancy is a scoping
-- change later, not a schema rewrite.
-- ============================================================

CREATE EXTENSION IF NOT EXISTS "pgcrypto"; -- gen_random_uuid()

-- ============================================================
-- TENANT / IDENTITY
-- ============================================================

CREATE TABLE restaurants (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name            VARCHAR(200) NOT NULL,
    cuisine_type    VARCHAR(120),
    phone           VARCHAR(30),
    currency        VARCHAR(10) NOT NULL DEFAULT 'INR',
    status          VARCHAR(20) NOT NULL DEFAULT 'OPEN', -- OPEN, CLOSED, PAUSED
    address_line    VARCHAR(300),
    city            VARCHAR(120),
    state           VARCHAR(120),
    postal_code     VARCHAR(20),
    country         VARCHAR(120) DEFAULT 'India',
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE restaurant_hours (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    restaurant_id   UUID NOT NULL REFERENCES restaurants(id) ON DELETE CASCADE,
    day_of_week     SMALLINT NOT NULL CHECK (day_of_week BETWEEN 0 AND 6), -- 0=Sunday
    open_time       TIME,
    close_time      TIME,
    is_closed       BOOLEAN NOT NULL DEFAULT false,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    UNIQUE (restaurant_id, day_of_week)
);
CREATE INDEX idx_restaurant_hours_restaurant ON restaurant_hours(restaurant_id);

CREATE TABLE roles (
    id      UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name    VARCHAR(50) NOT NULL UNIQUE -- SUPER_ADMIN, RESTAURANT_OWNER, MANAGER, STAFF, SUPPORT_AGENT
);

CREATE TABLE users (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    restaurant_id   UUID REFERENCES restaurants(id) ON DELETE CASCADE, -- NULL for SUPER_ADMIN
    role_id         UUID NOT NULL REFERENCES roles(id),
    full_name       VARCHAR(150) NOT NULL,
    email           VARCHAR(200) NOT NULL UNIQUE,
    password_hash   VARCHAR(255) NOT NULL,
    active          BOOLEAN NOT NULL DEFAULT true,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX idx_users_restaurant ON users(restaurant_id);

-- ============================================================
-- MENU
-- ============================================================

CREATE TABLE menu_categories (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    restaurant_id   UUID NOT NULL REFERENCES restaurants(id) ON DELETE CASCADE,
    name            VARCHAR(120) NOT NULL,
    display_order   INTEGER NOT NULL DEFAULT 0,
    active          BOOLEAN NOT NULL DEFAULT true,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    UNIQUE (restaurant_id, name)
);
CREATE INDEX idx_menu_categories_restaurant ON menu_categories(restaurant_id);

CREATE TABLE menu_items (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    restaurant_id   UUID NOT NULL REFERENCES restaurants(id) ON DELETE CASCADE,
    category_id     UUID NOT NULL REFERENCES menu_categories(id) ON DELETE RESTRICT,
    name            VARCHAR(150) NOT NULL,
    description     TEXT,
    price           NUMERIC(10,2) NOT NULL CHECK (price >= 0),
    vegetarian      BOOLEAN NOT NULL DEFAULT false,
    spicy_level     SMALLINT DEFAULT 0 CHECK (spicy_level BETWEEN 0 AND 3),
    available       BOOLEAN NOT NULL DEFAULT true,
    image_url       VARCHAR(500),
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX idx_menu_items_restaurant ON menu_items(restaurant_id);
CREATE INDEX idx_menu_items_category ON menu_items(category_id);
CREATE INDEX idx_menu_items_restaurant_available ON menu_items(restaurant_id, available);

CREATE TABLE menu_item_options (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    menu_item_id    UUID NOT NULL REFERENCES menu_items(id) ON DELETE CASCADE,
    name            VARCHAR(120) NOT NULL, -- e.g. "Half", "Full", "Extra spicy"
    price_delta     NUMERIC(10,2) NOT NULL DEFAULT 0,
    active          BOOLEAN NOT NULL DEFAULT true,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX idx_menu_item_options_item ON menu_item_options(menu_item_id);

CREATE TABLE inventory (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    menu_item_id    UUID NOT NULL UNIQUE REFERENCES menu_items(id) ON DELETE CASCADE,
    track_stock     BOOLEAN NOT NULL DEFAULT false,
    stock_count     INTEGER,
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- ============================================================
-- DELIVERY
-- ============================================================

CREATE TABLE delivery_zones (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    restaurant_id   UUID NOT NULL REFERENCES restaurants(id) ON DELETE CASCADE,
    name            VARCHAR(150) NOT NULL,
    postal_codes    TEXT[], -- simple MVP matching; can move to geo lookup later
    delivery_fee    NUMERIC(10,2) NOT NULL DEFAULT 0,
    min_order_amount NUMERIC(10,2) NOT NULL DEFAULT 0,
    estimated_minutes INTEGER,
    active          BOOLEAN NOT NULL DEFAULT true,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX idx_delivery_zones_restaurant ON delivery_zones(restaurant_id);

-- ============================================================
-- CUSTOMERS / CONVERSATIONS / MESSAGES
-- ============================================================

CREATE TABLE customers (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    restaurant_id   UUID NOT NULL REFERENCES restaurants(id) ON DELETE CASCADE,
    whatsapp_number VARCHAR(30) NOT NULL,
    display_name    VARCHAR(150),
    email           VARCHAR(200),
    default_address VARCHAR(300),
    notes           TEXT,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    UNIQUE (restaurant_id, whatsapp_number)
);
CREATE INDEX idx_customers_restaurant ON customers(restaurant_id);

CREATE TABLE conversations (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    restaurant_id       UUID NOT NULL REFERENCES restaurants(id) ON DELETE CASCADE,
    customer_id         UUID NOT NULL REFERENCES customers(id) ON DELETE CASCADE,
    mode                VARCHAR(20) NOT NULL DEFAULT 'AI_ACTIVE', -- AI_ACTIVE, HUMAN_PENDING, HUMAN_ACTIVE, CLOSED
    assigned_user_id    UUID REFERENCES users(id),
    current_order_id    UUID, -- FK added after orders table exists
    last_intent         VARCHAR(50),
    summary             TEXT,
    created_at          TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at          TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX idx_conversations_restaurant ON conversations(restaurant_id);
CREATE INDEX idx_conversations_customer ON conversations(customer_id);
CREATE INDEX idx_conversations_restaurant_mode ON conversations(restaurant_id, mode);

CREATE TABLE messages (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    conversation_id     UUID NOT NULL REFERENCES conversations(id) ON DELETE CASCADE,
    direction           VARCHAR(10) NOT NULL, -- INBOUND, OUTBOUND
    sender_type         VARCHAR(10) NOT NULL, -- CUSTOMER, AI, AGENT
    sender_user_id      UUID REFERENCES users(id),
    content             TEXT NOT NULL,
    wa_message_id       VARCHAR(150), -- Meta message id, unique when present
    tool_calls          JSONB,        -- record of any tool_use blocks for this AI turn
    created_at          TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX idx_messages_conversation ON messages(conversation_id, created_at);
CREATE UNIQUE INDEX uq_messages_wa_message_id ON messages(wa_message_id) WHERE wa_message_id IS NOT NULL;

-- ============================================================
-- ORDERS
-- ============================================================

CREATE TABLE orders (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    restaurant_id       UUID NOT NULL REFERENCES restaurants(id) ON DELETE CASCADE,
    customer_id         UUID NOT NULL REFERENCES customers(id),
    conversation_id     UUID REFERENCES conversations(id),
    delivery_zone_id    UUID REFERENCES delivery_zones(id), -- NULL => takeaway
    fulfillment_type    VARCHAR(20) NOT NULL DEFAULT 'DELIVERY', -- DELIVERY, TAKEAWAY
    status              VARCHAR(30) NOT NULL DEFAULT 'DRAFT',
        -- DRAFT, CONFIRMATION_REQUIRED, CONFIRMED, PREPARING, READY,
        -- OUT_FOR_DELIVERY, DELIVERED, CANCELLED
    delivery_address    VARCHAR(300),
    special_instructions TEXT,
    subtotal            NUMERIC(10,2) NOT NULL DEFAULT 0,
    delivery_fee        NUMERIC(10,2) NOT NULL DEFAULT 0,
    discount_total       NUMERIC(10,2) NOT NULL DEFAULT 0,
    tax_total            NUMERIC(10,2) NOT NULL DEFAULT 0,
    total                NUMERIC(10,2) NOT NULL DEFAULT 0,
    promotion_id         UUID, -- FK added after promotions table exists
    idempotency_key      VARCHAR(150) NOT NULL,
    created_at           TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at           TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX idx_orders_restaurant ON orders(restaurant_id);
CREATE INDEX idx_orders_customer ON orders(customer_id);
CREATE INDEX idx_orders_restaurant_status ON orders(restaurant_id, status);
CREATE UNIQUE INDEX uq_orders_idempotency_key ON orders(idempotency_key);

ALTER TABLE conversations
    ADD CONSTRAINT fk_conversations_current_order
    FOREIGN KEY (current_order_id) REFERENCES orders(id);

CREATE TABLE order_items (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    order_id        UUID NOT NULL REFERENCES orders(id) ON DELETE CASCADE,
    menu_item_id    UUID NOT NULL REFERENCES menu_items(id),
    item_name_snapshot  VARCHAR(150) NOT NULL, -- price/name at time of order, immutable
    unit_price_snapshot NUMERIC(10,2) NOT NULL,
    quantity        INTEGER NOT NULL CHECK (quantity > 0),
    line_total      NUMERIC(10,2) NOT NULL,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX idx_order_items_order ON order_items(order_id);

CREATE TABLE order_item_options (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    order_item_id       UUID NOT NULL REFERENCES order_items(id) ON DELETE CASCADE,
    menu_item_option_id UUID NOT NULL REFERENCES menu_item_options(id),
    option_name_snapshot VARCHAR(120) NOT NULL,
    price_delta_snapshot NUMERIC(10,2) NOT NULL
);
CREATE INDEX idx_order_item_options_order_item ON order_item_options(order_item_id);

-- ============================================================
-- PROMOTIONS
-- ============================================================

CREATE TABLE promotions (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    restaurant_id   UUID NOT NULL REFERENCES restaurants(id) ON DELETE CASCADE,
    code            VARCHAR(50) NOT NULL,
    description     VARCHAR(300),
    discount_type   VARCHAR(20) NOT NULL, -- PERCENT, FIXED_AMOUNT
    discount_value  NUMERIC(10,2) NOT NULL,
    valid_from      TIMESTAMPTZ NOT NULL,
    valid_until     TIMESTAMPTZ NOT NULL,
    active          BOOLEAN NOT NULL DEFAULT true,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    UNIQUE (restaurant_id, code)
);
CREATE INDEX idx_promotions_restaurant ON promotions(restaurant_id);

ALTER TABLE orders
    ADD CONSTRAINT fk_orders_promotion
    FOREIGN KEY (promotion_id) REFERENCES promotions(id);

-- ============================================================
-- KNOWLEDGE BASE
-- ============================================================

CREATE TABLE knowledge_documents (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    restaurant_id   UUID NOT NULL REFERENCES restaurants(id) ON DELETE CASCADE,
    title           VARCHAR(200) NOT NULL,
    category        VARCHAR(60), -- POLICY, FAQ, HOURS, DELIVERY, REFUND, ALLERGY, OTHER
    content         TEXT NOT NULL,
    active          BOOLEAN NOT NULL DEFAULT true,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX idx_knowledge_documents_restaurant ON knowledge_documents(restaurant_id);
-- Simple text search now; interface allows swapping to pgvector later.
CREATE INDEX idx_knowledge_documents_fts ON knowledge_documents USING GIN (to_tsvector('english', title || ' ' || content));

-- ============================================================
-- SUPPORT / LEADS
-- ============================================================

CREATE TABLE support_tickets (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    restaurant_id   UUID NOT NULL REFERENCES restaurants(id) ON DELETE CASCADE,
    conversation_id UUID REFERENCES conversations(id),
    order_id        UUID REFERENCES orders(id),
    issue           TEXT NOT NULL,
    priority        VARCHAR(20) NOT NULL DEFAULT 'NORMAL', -- LOW, NORMAL, HIGH, URGENT
    status          VARCHAR(20) NOT NULL DEFAULT 'OPEN', -- OPEN, IN_PROGRESS, RESOLVED, CLOSED
    assigned_user_id UUID REFERENCES users(id),
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX idx_support_tickets_restaurant ON support_tickets(restaurant_id);
CREATE INDEX idx_support_tickets_restaurant_status ON support_tickets(restaurant_id, status);

CREATE TABLE leads (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    restaurant_id   UUID NOT NULL REFERENCES restaurants(id) ON DELETE CASCADE,
    customer_id     UUID REFERENCES customers(id),
    conversation_id UUID REFERENCES conversations(id),
    source          VARCHAR(50), -- CATERING, BULK_ORDER, EVENT, REPEAT_INTENT
    intent_notes    TEXT,
    estimated_value NUMERIC(12,2),
    status          VARCHAR(20) NOT NULL DEFAULT 'NEW', -- NEW, CONTACTED, QUALIFIED, CONVERTED, LOST
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX idx_leads_restaurant ON leads(restaurant_id);
CREATE INDEX idx_leads_restaurant_status ON leads(restaurant_id, status);

-- ============================================================
-- AUTOMATION
-- ============================================================

CREATE TABLE automation_rules (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    restaurant_id   UUID NOT NULL REFERENCES restaurants(id) ON DELETE CASCADE,
    name            VARCHAR(150) NOT NULL,
    trigger_type    VARCHAR(50) NOT NULL, -- INTENT_DETECTED, ORDER_STATUS_CHANGED, LEAD_CREATED, ...
    condition_json  JSONB NOT NULL DEFAULT '{}',
    action_type     VARCHAR(50) NOT NULL, -- CREATE_LEAD, CREATE_TICKET, ESCALATE, NOTIFY, SEND_FOLLOWUP
    action_config_json JSONB NOT NULL DEFAULT '{}',
    active          BOOLEAN NOT NULL DEFAULT true,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX idx_automation_rules_restaurant ON automation_rules(restaurant_id);

-- ============================================================
-- PLATFORM: AUDIT + WEBHOOK IDEMPOTENCY
-- ============================================================

CREATE TABLE audit_logs (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    restaurant_id   UUID REFERENCES restaurants(id),
    actor_id        UUID, -- user id, or null for AI/SYSTEM
    actor_type      VARCHAR(20) NOT NULL, -- USER, AI, SYSTEM
    action          VARCHAR(100) NOT NULL,
    target_type     VARCHAR(60),
    target_id       UUID,
    result          VARCHAR(20) NOT NULL, -- SUCCESS, FAILURE, DENIED
    correlation_id  VARCHAR(100),
    details_json    JSONB,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX idx_audit_logs_restaurant ON audit_logs(restaurant_id);
CREATE INDEX idx_audit_logs_correlation ON audit_logs(correlation_id);
CREATE INDEX idx_audit_logs_target ON audit_logs(target_type, target_id);

CREATE TABLE webhook_events (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    wa_message_id   VARCHAR(150) NOT NULL,
    payload_json    JSONB NOT NULL,
    status          VARCHAR(20) NOT NULL DEFAULT 'RECEIVED', -- RECEIVED, PROCESSING, PROCESSED, FAILED
    processed_at    TIMESTAMPTZ,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE UNIQUE INDEX uq_webhook_events_wa_message_id ON webhook_events(wa_message_id);

-- ============================================================
-- SEED ROLES (fixed reference data, not tenant-scoped)
-- ============================================================

INSERT INTO roles (name) VALUES
    ('SUPER_ADMIN'),
    ('RESTAURANT_OWNER'),
    ('MANAGER'),
    ('STAFF'),
    ('SUPPORT_AGENT');
