-- Additional agents
INSERT INTO agent (name, type, status, description, client_id)
VALUES ('Technical Support', 'CUSTOM', 'ACTIVE', 'Provides technical troubleshooting assistance', 3);

INSERT INTO agent (name, type, status, description, client_id)
VALUES ('Billing Assistant', 'N8N', 'ACTIVE', 'Handles billing inquiries and payment processing', 3);

INSERT INTO agent (name, type, status, description, client_id)
VALUES ('Product Information', 'CUSTOM', 'ACTIVE', 'Provides detailed product specifications and comparisons', 4);

INSERT INTO agent (name, type, status, description, client_id)
VALUES ('Customer Feedback', 'VAPI', 'ACTIVE', 'Collects and processes customer feedback', 4);
