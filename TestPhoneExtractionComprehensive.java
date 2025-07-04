import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class TestPhoneExtractionComprehensive {
    public static void main(String[] args) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode conversation = mapper.readTree(new File("test_phone_extraction.json"));
            
            System.out.println("=== COMPREHENSIVE PHONE NUMBER EXTRACTION TEST ===\n");
            
            // Test 1: Check metadata.phone_call.external_number
            String phoneNumber = null;
            System.out.println("1. Testing metadata.phone_call.external_number...");
            if (conversation.has("metadata")) {
                JsonNode metadata = conversation.get("metadata");
                if (metadata.has("phone_call") && metadata.get("phone_call").has("external_number")) {
                    phoneNumber = metadata.get("phone_call").get("external_number").asText(null);
                    System.out.println("   ✅ Found: " + phoneNumber);
                } else {
                    System.out.println("   ❌ Not found in metadata.phone_call.external_number");
                }
            } else {
                System.out.println("   ❌ No metadata field found");
            }
            
            // Test 2: Check conversation_initiation_client_data.dynamic_variables.system__caller_id
            System.out.println("\n2. Testing conversation_initiation_client_data.dynamic_variables.system__caller_id...");
            if (conversation.has("conversation_initiation_client_data")) {
                JsonNode dynVars = conversation.path("conversation_initiation_client_data").path("dynamic_variables");
                if (dynVars.has("system__caller_id")) {
                    String callerId = dynVars.get("system__caller_id").asText(null);
                    System.out.println("   ✅ Found: " + callerId);
                    if (phoneNumber == null) phoneNumber = callerId;
                } else {
                    System.out.println("   ❌ Not found in system__caller_id");
                }
            } else {
                System.out.println("   ❌ No conversation_initiation_client_data field found");
            }
            
            // Test 3: Check conversation_initiation_client_data.dynamic_variables.system__called_number
            System.out.println("\n3. Testing conversation_initiation_client_data.dynamic_variables.system__called_number...");
            if (conversation.has("conversation_initiation_client_data")) {
                JsonNode dynVars = conversation.path("conversation_initiation_client_data").path("dynamic_variables");
                if (dynVars.has("system__called_number")) {
                    String calledNumber = dynVars.get("system__called_number").asText(null);
                    System.out.println("   ✅ Found: " + calledNumber);
                    if (phoneNumber == null) phoneNumber = calledNumber;
                } else {
                    System.out.println("   ❌ Not found in system__called_number");
                }
            }
            
            // Test 4: Check direct phone_number field
            System.out.println("\n4. Testing direct phone_number field...");
            if (conversation.has("phone_number")) {
                String directPhone = conversation.get("phone_number").asText(null);
                System.out.println("   ✅ Found: " + directPhone);
                if (phoneNumber == null) phoneNumber = directPhone;
            } else {
                System.out.println("   ❌ No direct phone_number field found");
            }
            
            // Final result
            System.out.println("\n=== FINAL RESULT ===");
            System.out.println("Final extracted phoneNumber: " + phoneNumber);
            
            if (phoneNumber != null && !phoneNumber.isEmpty()) {
                System.out.println("✅ SUCCESS: Phone number extraction works correctly!");
            } else {
                System.out.println("❌ FAILURE: No phone number found!");
            }
            
            // Additional debugging: Show all available fields
            System.out.println("\n=== AVAILABLE FIELDS ===");
            List<String> fieldNames = new ArrayList<>();
            conversation.fieldNames().forEachRemaining(fieldNames::add);
            System.out.println("Top-level fields: " + fieldNames);
            
            // Check if metadata exists and show its fields
            if (conversation.has("metadata")) {
                JsonNode metadata = conversation.get("metadata");
                List<String> metadataFields = new ArrayList<>();
                metadata.fieldNames().forEachRemaining(metadataFields::add);
                System.out.println("Metadata fields: " + metadataFields);
                
                if (metadata.has("phone_call")) {
                    JsonNode phoneCall = metadata.get("phone_call");
                    List<String> phoneCallFields = new ArrayList<>();
                    phoneCall.fieldNames().forEachRemaining(phoneCallFields::add);
                    System.out.println("Phone call fields: " + phoneCallFields);
                }
            }
            
            // Check if conversation_initiation_client_data exists and show its fields
            if (conversation.has("conversation_initiation_client_data")) {
                JsonNode clientData = conversation.get("conversation_initiation_client_data");
                List<String> clientDataFields = new ArrayList<>();
                clientData.fieldNames().forEachRemaining(clientDataFields::add);
                System.out.println("Client data fields: " + clientDataFields);
                
                if (clientData.has("dynamic_variables")) {
                    JsonNode dynVars = clientData.get("dynamic_variables");
                    List<String> dynVarFields = new ArrayList<>();
                    dynVars.fieldNames().forEachRemaining(dynVarFields::add);
                    System.out.println("Dynamic variables fields: " + dynVarFields);
                }
            }
            
        } catch (IOException e) {
            System.err.println("Error reading JSON file: " + e.getMessage());
            e.printStackTrace();
        }
    }
} 