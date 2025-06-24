package com.sfaai.sfaai.util;


import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

// Note: javax.crypto is still used even in Jakarta EE applications as these crypto classes were not migrated to jakarta namespace
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;




/**
 * Utility for verifying webhook signatures
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class WebhookSignatureVerifier {

    private final ObjectMapper objectMapper;

    @Value("${webhook.secret:default-webhook-secret}")
    private String webhookSecret;

    /**
     * Verify webhook signature
     *
     * @param signature The signature from webhook header
     * @param payload The webhook payload
     * @return true if signature is valid
     */
    public boolean verifySignature(String signature, Object payload) {
        try {
            // Convert payload to JSON string
            String payloadJson = objectMapper.writeValueAsString(payload);

            // Calculate expected signature
            String expectedSignature = calculateHmacSha256(payloadJson, webhookSecret);

            // Compare signatures
            return expectedSignature.equals(signature);
        } catch (Exception e) {
            log.error("Error verifying webhook signature", e);
            return false;
        }
    }

    /**
     * Calculate HMAC-SHA256 signature
     *
     * @param data The data to sign
     * @param key The secret key
     * @return Base64 encoded signature
     */
    private String calculateHmacSha256(String data, String key) 
            throws NoSuchAlgorithmException, InvalidKeyException {
        Mac sha256Hmac = Mac.getInstance("HmacSHA256");
        SecretKeySpec secretKey = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
        sha256Hmac.init(secretKey);
        byte[] hmacBytes = sha256Hmac.doFinal(data.getBytes(StandardCharsets.UTF_8));
        return Base64.getEncoder().encodeToString(hmacBytes);
    }
}
