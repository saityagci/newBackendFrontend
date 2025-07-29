#!/bin/bash

# Fix remaining test compilation issues

# Fix VoiceLogServiceTest - replace toEntity with createEntityFromDto
sed -i '' 's/verify(voiceLogMapper)\.toEntity(testVoiceLogCreateDTO);/verify(voiceLogMapper).createEntityFromDto(testVoiceLogCreateDTO);/g' src/test/java/com/sfaai/sfaai/service/VoiceLogServiceTest.java

# Fix WorkflowLogServiceTest - replace toEntity with createEntityFromDto  
sed -i '' 's/verify(workflowLogMapper)\.toEntity(testWorkflowLogCreateDTO);/verify(workflowLogMapper).createEntityFromDto(testWorkflowLogCreateDTO);/g' src/test/java/com/sfaai/sfaai/service/WorkflowLogServiceTest.java

echo "Fixed remaining test compilation issues" 