-- Turn the seeded delivered-only follow-up into an update for every order
-- status transition. Preserve any user-customized version of that rule.
UPDATE automation_rules
SET name = 'Order status updates',
    condition_json = '{}'::jsonb,
    action_config_json = jsonb_build_object(
        'message', 'Order #{{orderReference}} changed from {{previousStatus}} to {{status}}.',
        'templateName', 'order_status_update',
        'templateLanguageCode', 'en_US',
        'templateParams', jsonb_build_array('{{orderReference}}', '{{previousStatus}}', '{{status}}')
    )
WHERE restaurant_id = '00000000-0000-0000-0000-000000000001'
  AND name = 'Delivered follow-up'
  AND trigger_type = 'ORDER_STATUS_CHANGED'
  AND action_type = 'SEND_FOLLOWUP'
  AND condition_json = '{"status":"DELIVERED"}'::jsonb
  AND action_config_json->>'message' = 'Thanks for your order! We hope you enjoyed it. Reply anytime if you need anything.';

INSERT INTO automation_rules (
    restaurant_id, name, trigger_type, condition_json, action_type, action_config_json, active
)
SELECT
    '00000000-0000-0000-0000-000000000001',
    'Order status updates',
    'ORDER_STATUS_CHANGED',
    '{}'::jsonb,
    'SEND_FOLLOWUP',
    jsonb_build_object(
        'message', 'Order #{{orderReference}} changed from {{previousStatus}} to {{status}}.',
        'templateName', 'order_status_update',
        'templateLanguageCode', 'en_US',
        'templateParams', jsonb_build_array('{{orderReference}}', '{{previousStatus}}', '{{status}}')
    ),
    true
WHERE NOT EXISTS (
    SELECT 1 FROM automation_rules
    WHERE restaurant_id = '00000000-0000-0000-0000-000000000001'
      AND name = 'Order status updates'
      AND trigger_type = 'ORDER_STATUS_CHANGED'
);