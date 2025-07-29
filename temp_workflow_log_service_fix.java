    public WorkflowLogDTO createWorkflowLog(WorkflowLogCreateDTO request) {
        if (request == null) {
            throw new IllegalArgumentException("Request cannot be null");
        }
        if (request.getClientId() == null) {
            throw new IllegalArgumentException("Client ID cannot be null");
        }
        
        // ... rest of implementation
    }
    
    public WorkflowLogDTO getWorkflowLogById(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("ID cannot be null");
        }
        // ... rest of implementation
    }
    
    public void deleteWorkflowLog(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("ID cannot be null");
        }
        // ... rest of implementation
    }
    
    public List<WorkflowLogDTO> getWorkflowLogsByClientId(Long clientId) {
        if (clientId == null) {
            throw new IllegalArgumentException("Client ID cannot be null");
        }
        // ... rest of implementation
    }
