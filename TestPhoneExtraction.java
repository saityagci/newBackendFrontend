import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.io.IOException;

public class TestPhoneExtraction {
    public static void main(String[] args) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode conversation = mapper.readTree(new File("test_phone_extraction.json"));
            
            String phoneNumber = null;
            
            // Test the phone number extraction logic
            // 1. Check metadata.phone_call.external_number
            if (conversation.has("metadata")) {
                JsonNode metadata = conversation.get("metadata");
                if (metadata.has("phone_call") && metadata.get("phone_call").has("external_number")) {
                    phoneNumber = metadata.get("phone_call").get("external_number").asText(null);
                    System.out.println("Found phone number in metadata.phone_call.external_number: " + phoneNumber);
                }
            }
            
            // 2. Check conversation_initiation_client_data.dynamic_variables.system__caller_id
            if ((phoneNumber == null || phoneNumber.isEmpty()) && conversation.has("conversation_initiation_client_data")) {
                JsonNode dynVars = conversation.path("conversation_initiation_client_data").path("dynamic_variables");
                if (dynVars.has("system__caller_id")) {
                    phoneNumber = dynVars.get("system__caller_id").asText(null);
                    System.out.println("Found phone number in system__caller_id: " + phoneNumber);
                }
            }
            
            // 3. Check conversation_initiation_client_data.dynamic_variables.system__called_number
            if ((phoneNumber == null || phoneNumber.isEmpty()) && conversation.has("conversation_initiation_client_data")) {
                JsonNode dynVars = conversation.path("conversation_initiation_client_data").path("dynamic_variables");
                if (dynVars.has("system__called_number")) {
                    phoneNumber = dynVars.get("system__called_number").asText(null);
                    System.out.println("Found phone number in system__called_number: " + phoneNumber);
                }
            }
            
            System.out.println("Final extracted phoneNumber: " + phoneNumber);
            
            // Expected: +13476342847
            if ("+13476342847".equals(phoneNumber)) {
                System.out.println("✅ SUCCESS: Phone number extraction works correctly!");
            } else {
                System.out.println("❌ FAILED: Expected +13476342847, got " + phoneNumber);
            }
            
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
} 