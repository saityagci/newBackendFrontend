package com.sfaai.sfaai.dto;

import org.springframework.stereotype.Component;

/**
 * Example class showing how to extract values from the Vapi webhook payload
 */
@Component
public class VapiMessageExtractorExample {

    /**
     * Sample method showing how to extract values from the Vapi webhook payload DTO
     * @param payload The webhook payload DTO
     */
    public void extractValuesExample(VapiWebhookPayloadDTO payload) {
        if (payload == null) {
            System.out.println("Payload is null");
            return;
        }

        // Extract values using the new structured accessors
        String callId = payload.getCallId();
        String assistantId = payload.getAssistantId();
        String callerPhoneNumber = payload.getCallerPhoneNumber();
        String summary = payload.getSummary();
        String transcript = payload.getTranscript();
        String audioUrl = payload.getAudioUrl();
        Long timestamp = payload.getTimestamp();

        // Print the extracted values
        System.out.println("Call ID: " + callId);
        System.out.println("Assistant ID: " + assistantId);
        System.out.println("Caller Phone Number: " + callerPhoneNumber);
        System.out.println("Summary: " + summary);
        System.out.println("Transcript: " + transcript);
        System.out.println("Audio URL: " + audioUrl);
        System.out.println("Timestamp: " + timestamp);

        // Alternative direct access using dot notation with existing helper methods
        String callIdAlt = payload.getStringValue("message.call.id");
        String assistantIdAlt = payload.getStringValue("message.assistant.id");
        String callerPhoneNumberAlt = payload.getStringValue("message.call.customer.number");
        String summaryAlt = payload.getStringValue("message.analysis.summary");
        String transcriptAlt = payload.getStringValue("message.artifact.transcript");
        String audioUrlAlt = payload.getStringValue("message.artifact.recordingUrl");
        Number timestampAlt = payload.getNumberValue("message.timestamp");

        // Using direct structure access
        if (payload.getMessage() != null) {
            // Call ID
            if (payload.getMessage().getCall() != null) {
                callId = payload.getMessage().getCall().getId();

                // Caller Phone Number
                if (payload.getMessage().getCall().getCustomer() != null) {
                    callerPhoneNumber = payload.getMessage().getCall().getCustomer().getNumber();
                }
            }

            // Assistant ID
            if (payload.getMessage().getAssistant() != null) {
                assistantId = payload.getMessage().getAssistant().getId();
            }

            // Summary
            if (payload.getMessage().getAnalysis() != null) {
                summary = payload.getMessage().getAnalysis().getSummary();
            }

            // Transcript and Audio URL
            if (payload.getMessage().getArtifact() != null) {
                transcript = payload.getMessage().getArtifact().getTranscript();
                audioUrl = payload.getMessage().getArtifact().getRecordingUrl();
            }

            // Timestamp
            timestamp = payload.getMessage().getTimestamp();
        }
    }
}
