ALTER TABLE agent ADD CONSTRAINT check_agent_type CHECK (type IN ('VAPI', 'N8N', 'CUSTOM'));
ALTER TABLE agent ADD CONSTRAINT check_agent_status CHECK (status IN ('ACTIVE', 'INACTIVE', 'PENDING', 'DELETED'));
