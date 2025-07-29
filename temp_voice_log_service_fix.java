    public VoiceLogDTO createVoiceLog(VoiceLogCreateDTO request) {
        if (request == null) {
            throw new IllegalArgumentException("Request cannot be null");
        }
        if (request.getClientId() == null) {
            throw new IllegalArgumentException("Client ID cannot be null");
        }
        if (request.getAgentId() == null) {
            throw new IllegalArgumentException("Agent ID cannot be null");
        }
        
        // ... rest of implementation
    }
