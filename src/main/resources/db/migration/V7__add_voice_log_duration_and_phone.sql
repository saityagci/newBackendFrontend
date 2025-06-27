-- Add phone_number and duration_minutes columns to voice_log table
ALTER TABLE voice_log
ADD COLUMN IF NOT EXISTS phone_number VARCHAR(32);

ALTER TABLE voice_log
ADD COLUMN IF NOT EXISTS duration_minutes FLOAT;

-- Update existing duration_minutes values where possible
UPDATE voice_log
SET duration_minutes = EXTRACT(EPOCH FROM (ended_at - started_at))/60
WHERE ended_at IS NOT NULL AND started_at IS NOT NULL AND duration_minutes IS NULL;
