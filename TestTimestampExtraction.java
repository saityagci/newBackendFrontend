import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.io.IOException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

public class TestTimestampExtraction {
    public static void main(String[] args) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode conversation = mapper.readTree(new File("test_phone_extraction.json"));
            
            System.out.println("=== TIMESTAMP EXTRACTION TEST ===\n");
            
            // Test timestamp extraction logic
            LocalDateTime startedAt = null;
            LocalDateTime endedAt = null;
            
            // 1. Check for start_time_unix_secs in basic conversation
            System.out.println("1. Testing basic conversation start_time_unix_secs...");
            if (conversation.has("start_time_unix_secs")) {
                long startTimeUnix = conversation.get("start_time_unix_secs").asLong();
                startedAt = LocalDateTime.ofInstant(Instant.ofEpochSecond(startTimeUnix), ZoneOffset.UTC);
                System.out.println("   ✅ Found start_time_unix_secs: " + startTimeUnix + " -> " + startedAt);
            } else {
                System.out.println("   ❌ No start_time_unix_secs found in basic conversation");
            }
            
            // 2. Check for call_duration_secs in basic conversation
            System.out.println("\n2. Testing basic conversation call_duration_secs...");
            if (startedAt != null && conversation.has("call_duration_secs")) {
                long durationSecs = conversation.get("call_duration_secs").asLong();
                endedAt = startedAt.plusSeconds(durationSecs);
                System.out.println("   ✅ Found call_duration_secs: " + durationSecs + " -> endedAt: " + endedAt);
            } else {
                System.out.println("   ❌ No call_duration_secs found or no startedAt");
            }
            
            // 3. Check metadata if not found in basic conversation
            System.out.println("\n3. Testing metadata fields...");
            if (startedAt == null && conversation.has("metadata")) {
                JsonNode metadata = conversation.get("metadata");
                
                if (metadata.has("start_time_unix_secs")) {
                    long startTimeUnix = metadata.get("start_time_unix_secs").asLong();
                    startedAt = LocalDateTime.ofInstant(Instant.ofEpochSecond(startTimeUnix), ZoneOffset.UTC);
                    System.out.println("   ✅ Found metadata.start_time_unix_secs: " + startTimeUnix + " -> " + startedAt);
                } else {
                    System.out.println("   ❌ No metadata.start_time_unix_secs found");
                }
                
                if (startedAt != null && metadata.has("call_duration_secs")) {
                    long durationSecs = metadata.get("call_duration_secs").asLong();
                    endedAt = startedAt.plusSeconds(durationSecs);
                    System.out.println("   ✅ Found metadata.call_duration_secs: " + durationSecs + " -> endedAt: " + endedAt);
                } else {
                    System.out.println("   ❌ No metadata.call_duration_secs found or no startedAt");
                }
            } else {
                System.out.println("   ❌ No metadata found or already have startedAt");
            }
            
            // 4. Check fallback fields
            System.out.println("\n4. Testing fallback fields...");
            if (startedAt == null && conversation.has("created_at")) {
                System.out.println("   ✅ Found created_at: " + conversation.get("created_at").asText());
            } else {
                System.out.println("   ❌ No created_at found or already have startedAt");
            }
            
            if (endedAt == null && conversation.has("updated_at")) {
                System.out.println("   ✅ Found updated_at: " + conversation.get("updated_at").asText());
            } else {
                System.out.println("   ❌ No updated_at found or already have endedAt");
            }
            
            // 5. Final results
            System.out.println("\n=== FINAL RESULTS ===");
            System.out.println("startedAt: " + (startedAt != null ? startedAt : "null"));
            System.out.println("endedAt: " + (endedAt != null ? endedAt : "null"));
            
            // 6. Show available fields for debugging
            System.out.println("\n=== AVAILABLE FIELDS ===");
            System.out.println("Top-level fields:");
            conversation.fieldNames().forEachRemaining(field -> {
                System.out.println("  - " + field + ": " + conversation.get(field).getNodeType());
            });
            
            if (conversation.has("metadata")) {
                System.out.println("\nMetadata fields:");
                conversation.get("metadata").fieldNames().forEachRemaining(field -> {
                    System.out.println("  - " + field + ": " + conversation.get("metadata").get(field).getNodeType());
                });
            }
            
        } catch (IOException e) {
            System.err.println("Error reading JSON file: " + e.getMessage());
            e.printStackTrace();
        }
    }
} 