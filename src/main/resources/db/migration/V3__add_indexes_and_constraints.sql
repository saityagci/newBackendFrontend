ALTER TABLE agent ADD CONSTRAINT check_agent_type CHECK (type IN ('N8N', 'CUSTOM', 'ELEVENLABS'));
ALTER TABLE agent ADD CONSTRAINT check_agent_status CHECK (status IN ('ACTIVE', 'INACTIVE', 'PENDING', 'DELETED'));
