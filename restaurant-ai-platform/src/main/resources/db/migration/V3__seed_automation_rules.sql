-- ============================================================
-- V3__seed_automation_rules.sql
-- Two example rules exercising the automation engine end-to-end:
-- one order-triggered follow-up, one lead-value threshold alert.
-- ============================================================

INSERT INTO automation_rules (restaurant_id, name, trigger_type, condition_json, action_type, action_config_json, active)
VALUES (
    '00000000-0000-0000-0000-000000000001',
    'Delivered follow-up',
    'ORDER_STATUS_CHANGED',
    '{"status": "DELIVERED"}',
    'SEND_FOLLOWUP',
    -- "message" is used when we are inside WhatsApp's 24h customer service window
    -- (the normal case right after a delivery). "templateName" is the fallback
    -- Meta REQUIRES outside that window - replace with a real template name
    -- you have gotten approved in Meta Business Manager before relying on it.
    '{"message": "Thanks for your order! We hope you enjoyed it. Reply anytime if you need anything.", "templateName": "order_delivered_followup", "templateLanguageCode": "en_US", "templateParams": []}',
    true
);

INSERT INTO automation_rules (restaurant_id, name, trigger_type, condition_json, action_type, action_config_json, active)
VALUES (
    '00000000-0000-0000-0000-000000000001',
    'High-value lead alert',
    'LEAD_CREATED',
    '{"min_estimatedValue": 5000}',
    'NOTIFY',
    '{"message": "A high-value catering/bulk lead just came in - follow up soon."}',
    true
);
