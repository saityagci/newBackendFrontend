# ElevenLabs Combined Transcript Format Test

## New Format Example

The new transcript format will combine both the detailed conversation and the summary in the same column:

```
AI: Hello. This is Laura from Noya. How may I assist you today?
User: Hi, Laura. Wanna make reservation for tomorrow, 6 PM.
AI: Of course. May I have your name, please?
User: Uh, this is Sai. This is John from, uh, John.
AI: Hi, Sai. Or is it John? Just wanna make sure I've got the right name. Could you clarify for me?
User: John.
AI: Got it, John. How many guests will be joining you tomorrow at 6 PM?
User: I got a receipt.
AI: Alright, John. How many guests will be joining you tomorrow?

Summary: John called Noya to make a reservation for tomorrow at 6 PM. The AI agent confirmed his name as John after initial confusion. However, the call ended before the number of guests for the reservation could be confirmed.
```

## What Changed

1. **Detailed Transcript**: Full conversation with AI/User format
2. **Summary**: Concise summary of the conversation
3. **Combined**: Both parts in the same `transcript` column
4. **Format**: Detailed transcript followed by "\n\nSummary: " + summary

## Testing Steps

1. Run manual sync: `POST /api/elevenlabs/voice-logs/sync`
2. Check existing records: `GET /api/elevenlabs/voice-logs`
3. Verify new format in database

## Expected Behavior

- New conversations will have both detailed transcript and summary
- Existing conversations will be updated on next sync
- Fallback to summary-only if detailed transcript unavailable
- Fallback to detailed-only if summary unavailable 