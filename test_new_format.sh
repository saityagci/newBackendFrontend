#!/bin/bash

echo "Testing ElevenLabs Combined Transcript Format"
echo "============================================="

# Test the application is running
echo "1. Checking if application is running..."
if curl -s http://localhost:8880/actuator/health > /dev/null 2>&1; then
    echo "✅ Application is running"
else
    echo "❌ Application is not running"
    exit 1
fi

# Trigger manual sync
echo "2. Triggering manual sync..."
SYNC_RESPONSE=$(curl -s -X POST http://localhost:8880/api/elevenlabs/voice-logs/sync \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJhZG1pbiIsImlhdCI6MTczMDU5NzI5NiwiZXhwIjoxNzMwNjgzNjk2fQ.Ej8Ej8Ej8Ej8Ej8Ej8Ej8Ej8Ej8Ej8Ej8Ej8Ej8")

echo "Sync response: $SYNC_RESPONSE"

# Get a sample voice log to check the new format
echo "3. Fetching sample voice log..."
SAMPLE_LOG=$(curl -s http://localhost:8880/api/elevenlabs/voice-logs/conv_01jz1xt82je4ha51eqrcm70zcm \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJhZG1pbiIsImlhdCI6MTczMDU5NzI5NiwiZXhwIjoxNzMwNjgzNjk2fQ.Ej8Ej8Ej8Ej8Ej8Ej8Ej8Ej8Ej8Ej8Ej8Ej8Ej8")

echo "Sample log transcript:"
echo "$SAMPLE_LOG" | jq -r '.transcript' | head -20

echo "4. Testing complete!" 