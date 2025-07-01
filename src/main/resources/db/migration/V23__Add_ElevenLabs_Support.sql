-- Create ElevenLabs assistant table
CREATE TABLE elevenlabs_assistant (
    assistant_id VARCHAR(255) PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    voice_id VARCHAR(255),
    voice_name VARCHAR(255),
    model_id VARCHAR(255),
    raw_data TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
    last_synced_at TIMESTAMP
);

-- Alter voice_log table to support ElevenLabs
ALTER TABLE voice_log 
    ADD COLUMN elevenlabs_assistant_id VARCHAR(255),
    ADD CONSTRAINT fk_voice_log_elevenlabs_assistant 
    FOREIGN KEY (elevenlabs_assistant_id) 
    REFERENCES elevenlabs_assistant(assistant_id);

-- Make assistant_id nullable to support both providers
ALTER TABLE voice_log 
    ALTER COLUMN assistant_id DROP NOT NULL;

-- Alter provider column to be an enum
ALTER TABLE voice_log 
    ALTER COLUMN provider TYPE VARCHAR(20);

-- Create index for performance
CREATE INDEX idx_voice_log_elevenlabs_assistant_id ON voice_log(elevenlabs_assistant_id);
