-- Fix the assistant_id constraint issue for ElevenLabs voice logs
-- This migration ensures that assistant_id can be null when elevenlabs_assistant_id is set

-- First, drop any existing not null constraint on assistant_id
ALTER TABLE voice_log 
    ALTER COLUMN assistant_id DROP NOT NULL;

-- Add a check constraint to ensure at least one assistant is set
-- This ensures data integrity while allowing either assistant_id or elevenlabs_assistant_id to be null
ALTER TABLE voice_log 
    ADD CONSTRAINT check_assistant_set 
    CHECK (
        (assistant_id IS NOT NULL AND elevenlabs_assistant_id IS NULL) OR
        (assistant_id IS NULL AND elevenlabs_assistant_id IS NOT NULL)
    ); 