# SfaAI Platform

## Vapi Webhook Integration

The platform can receive webhook notifications from Vapi when calls are completed. These webhooks provide call logs with conversation history, audio recordings, and other metadata.

### Consolidated DTO Architecture

The platform uses a consolidated DTO approach for handling Vapi webhooks:

- `VapiCallLogDTO` serves as a multi-purpose DTO that can:
  - Accept any JSON schema from Vapi's webhook payload
  - Store structured conversation data
  - Map to/from database entities
  - Provide validation for required fields

### Webhook Endpoint

```
POST /api/webhooks/vapi/call-logs
```

This endpoint accepts JSON payloads from Vapi in any format. The system will extract relevant fields including:

- Call ID
- Assistant ID
- Call timestamps
- Audio URL
- Transcript
- Message history

### Example Webhook Payload

```json
{
  "call": {
    "id": "call_123456789",
    "status": "completed",
    "startTime": 1687452378,
    "endTime": 1687452498,
    "recordingUrl": "https://storage.vapi.ai/recordings/call_123456789.mp3"
  },
  "assistant": {
    "id": "asst_abcdef123456",
    "name": "Customer Service Bot"
  },
  "transcript": {
    "text": "User: Hello\nAssistant: Hi there, how can I help you today?"
  },
  "messages": [
    {
      "role": "user",
      "content": "Hello",
      "timestamp": 1687452380
    },
    {
      "role": "assistant",
      "content": "Hi there, how can I help you today?",
      "timestamp": 1687452385
    }
  ]
}
```

### Audio Storage

When a webhook includes an audio URL, the system can:

1. Download the audio file from the provided URL
2. Store it locally or in a cloud storage bucket
3. Update the reference in the database with the new storage location

Audio files can be accessed later via the API.

### Database Storage

All webhook data is stored in the `voice_log` table with relationship to:

- Client
- Agent
- Vapi Assistant

The system also stores structured conversation data as JSON for later analysis.
